/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.net.consumer.registry;

import com.zfoo.event.manager.EventBus;
import com.zfoo.net.NetContext;
import com.zfoo.net.consumer.event.ConsumerStartEvent;
import com.zfoo.net.consumer.event.ProviderStartEvent;
import com.zfoo.net.core.HostAndPort;
import com.zfoo.net.core.tcp.TcpClient;
import com.zfoo.net.core.tcp.TcpServer;
import com.zfoo.net.session.Session;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.collection.concurrent.ConcurrentArrayList;
import com.zfoo.protocol.collection.concurrent.ConcurrentHashSet;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.*;
import com.zfoo.scheduler.manager.SchedulerBus;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 服务注册，服务发现
 *
 * @author godotg
 */
public class ZookeeperRegistry implements IRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private static final long RETRY_SECONDS = 5;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(new ConfigThreadFactory());

    private static class ConfigThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        // config-p1-t1 = config-pool-1-thread-1
        ConfigThreadFactory() {
            this.group = Thread.currentThread().getThreadGroup();
            namePrefix = "config-p" + poolNumber.getAndIncrement() + "-t";
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new FastThreadLocalThread(group, runnable, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            t.setUncaughtExceptionHandler((thread, e) -> logger.error(thread.toString(), e));
            return t;
        }
    }


    private CuratorFramework curator;
    /**
     * provider的监听
     */
    private CuratorCache providerCuratorCache;
    /**
     * 本地consumer需要消费的provider集合
     */
    private final Set<Register> providerRegisterSet = new ConcurrentHashSet<>();
    /**
     * addListener中的cache全部会被添加到这个集合中，这个集合不包括providerCuratorCache
     */
    private final List<CuratorCache> listenerList = new ConcurrentArrayList<>();

    /**
     * 这个方法简单说就是分3大步骤：
     * 如果配置中配置的有自己是服务提供者，那么自己作为服务提供者先启动 // 此时尚未注册到zk中
     * 再启动zk // 干2件事：1.自己是服务提供者，将会注册到zk上 2.自己是服务消费者，会尝试创建TcpClient连接所有的自己要连接的provider
     * 监听服务提供者节点的变更 // 自己作为消费者，服务提供者的变更会影响到自己
     * <p>
     * 总结：这个注册中心模块最终的结果我认为是，影响到了
     * ClientSessionMap  // 自己作为消费者去连接服务提供者连上后，存的session
     * ServerSessionMap  // 自己作为服务器角色，很多客户端连接自己，连上来后，保存到这
     */

    private String rootPath = "/zfoo";
    private String providerRootPath = "/zfoo/provider";
    private String consumerRootPath = "/zfoo/consumer";

    @Override
    public void start() {
        var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();
        if (Objects.isNull(registryConfig)) {
            logger.info("Stand alone startup of singleton");
            return;
        }

        if (StringUtils.isNotBlank(registryConfig.getPath())) {
            rootPath = StringUtils.trim(registryConfig.getPath());
            providerRootPath = rootPath + "/provider";
            consumerRootPath = rootPath + "/consumer";
        }

        // 先启动本地服务提供者（服务提供者仅仅是一个TcpServer）
        startProvider();

        // 再启动curator框架 1.如果自己是服务提供者，将会注册自己到zk上 2.如果是服务消费者，会创建连接到自己关心的服务提供者上
        startCurator();

        // 检测服务提供者的增加或者减少。因为自己作为消费者的话，会影响到自己
        startProviderCache();
    }

    private void startProvider() {
        // 这个ProviderConfig包含了这个进程服务提供者信息
        var providerConfig = NetContext.getConfigManager().getLocalConfig().getProvider();

        // 这句意思是：提供了注册中心的配置(zk)，但是却没有服务提供者
        if (Objects.isNull(providerConfig)) {
            logger.info("Distributed startup with no providers");
            return;
        }

        // 服务提供者也仅仅是一个TcpServer
        // 这里可以看出并没有指定接口，是找一个可用的端口
        var providerHostAndPort = providerConfig.localHostAndPortOrDefault();
        var providerServer = new TcpServer(providerHostAndPort);
        providerServer.start();
        EventBus.post(ProviderStartEvent.valueOf(providerHostAndPort));
    }

    /**
     * 启动curator框架，并且在zk客户端连接上zk服务器时：保证创建好/zfoo /provider /consumer 3个“持久化”节点
     */
    private void startCurator() {
        var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();

        if (!registryConfig.getCenter().toLowerCase().matches("zookeeper")) {
            throw new IllegalArgumentException(StringUtils
                    .format("[center:{}]注册中心只能是zookeeper", JsonUtils.object2String(registryConfig)));
        }

        // 读取zk的配置，连接zk服务器
        var zookeeperConnectStr = HostAndPort.toHostAndPortListStr(HostAndPort.toHostAndPortList(registryConfig.getAddress().values()));
        var builder = CuratorFrameworkFactory.builder();
        builder.connectString(zookeeperConnectStr);
        if (registryConfig.hasZookeeperAuthor()) {
            builder.authorization("digest", StringUtils.bytes(registryConfig.toZookeeperAuthor()));
        }
        builder.sessionTimeoutMs(40_000);
        builder.connectionTimeoutMs(10_000);
        builder.retryPolicy(new RetryNTimes(1, 3_000));

        curator = builder.build();
        curator.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState state) {
                switch (state) {
                    // zk客户端与zk服务器失去了连接(忽略此情况，使用本地配置的缓存)
                    case LOST:
                        logger.error("[zookeeper:{}] lost connection, cache used", zookeeperConnectStr);
                        break;

                    // 暂停和只读这2种状态不检测
                    case SUSPENDED:
                    case READ_ONLY:
                        logger.warn("[zookeeper:{}] ignored [state{}]", zookeeperConnectStr, state);
                        break;

                    // zk客户端和zk服务器重连了
                    case CONNECTED:
                    case RECONNECTED:
                        // 检查3个持久化节点，不存在就创建
                        createZookeeperRootPath();
                        break;

                    default:
                        logger.error("[zookeeper:{}] unknown [state{}]", zookeeperConnectStr, state);
                }
            }
        }, executor);

        curator.start();
        try {
            curator.blockUntilConnected();
        } catch (Throwable t) {
            throw new RuntimeException("Start zookeeper exception", t);
        }
    }

    /**
     * 检查 /zfoo /zfoo/provider /zfoo/consumer 这3个“持久化”节点，不存在就创建
     */
    private void createZookeeperRootPath() {
        executor.execute(() -> {
            try {
                // /zfoo
                // 创建zookeeper的根路径
                var rootStat = curator.checkExists().forPath(rootPath);
                // 根节点不存在
                if (Objects.isNull(rootStat)) {
                    var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();
                    var builder = curator.create();
                    builder.creatingParentsIfNeeded();
                    // 检查zk连接授权
                    if (registryConfig.hasZookeeperAuthor()) {
                        var zookeeperAuthorStr = registryConfig.toZookeeperAuthor();
                        var aclList = List.of(new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(zookeeperAuthorStr))));
                        builder.withACL(aclList);
                    }
                    // 根节点是持久化节点
                    builder.withMode(CreateMode.PERSISTENT);
                    // 真正创建根节点
                    builder.forPath(rootPath, StringUtils.bytes(registryConfig.getCenter()));
                } else {
                    var registryConfig = NetContext.getConfigManager().getLocalConfig().getRegistry();
                    // 读取根节点上的数据
                    var bytes = curator.getData().storingStatIn(new Stat()).forPath(rootPath);
                    // 把根节点数据从二进制转string字符串
                    var rootPathData = StringUtils.bytesToString(bytes);

                    // 检查zookeeper根节点的内容
                    if (!rootPathData.equals(registryConfig.getCenter())) {
                        throw new RuntimeException(StringUtils.format("zookeeper rootPath[{}] misconfigured [{}]，expected [{}], check the relevant nodes and restart", rootPath, rootPathData, registryConfig.getCenter()));
                    }

                    // 检查zookeeper根节点的权限
                    if (registryConfig.hasZookeeperAuthor()) {
                        try {
                            var providerRootPathAclList = curator.getACL().forPath(rootPath);
                            AssertionUtils.notEmpty(providerRootPathAclList);
                            AssertionUtils.isTrue(providerRootPathAclList.size() == 1);
                            var zookeeperAuthorStr = registryConfig.toZookeeperAuthor();
                            var aclList = List.of(new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(zookeeperAuthorStr))));
                            AssertionUtils.isTrue(providerRootPathAclList.get(0).equals(aclList.get(0)));
                        } catch (Exception e) {
                            throw new RuntimeException(StringUtils.format("zookeeper rootPath[{}] permissions are misconfigured [{}]", rootPath, ExceptionUtils.getMessage(e)));
                        }
                    }

                }

                // /zfoo/provider
                // 检查服务提供者节点，不存在则创建
                var providerStat = curator.checkExists().forPath(providerRootPath);
                if (Objects.isNull(providerStat)) {
                    curator.create()
                            .withMode(CreateMode.PERSISTENT)
                            .forPath(providerRootPath, ArrayUtils.EMPTY_BYTE_ARRAY);
                }

                // /zfoo/consumer
                // 检查消费者节点，不存在则创建
                var consumerStat = curator.checkExists().forPath(consumerRootPath);
                if (Objects.isNull(consumerStat)) {
                    curator.create()
                            .withMode(CreateMode.PERSISTENT)
                            .forPath(consumerRootPath, ArrayUtils.EMPTY_BYTE_ARRAY);
                }

                // 如果自己是服务提供者，则注册自己
                // 如果自己是消费者，则创建连接到所有的自己关心的服务提供者
                initZookeeper();
            } catch (Throwable t) {
                logger.error("Zookeeper failed to create zookeeper root path, wait [{}] seconds to recreate", RETRY_SECONDS, t);
                SchedulerBus.schedule(() -> createZookeeperRootPath(), RETRY_SECONDS, TimeUnit.SECONDS);
            }
        });
    }

    private void startProviderCache() {
        // 初始化providerCache
        providerCuratorCache = CuratorCache.builder(curator, providerRootPath)
                .withExceptionHandler(e -> {
                    logger.error("providerCuratorCache unknown exception", e);
                    initZookeeper();
                })
                .build();

        providerCuratorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldData, ChildData newData) {
                switch (type) {
                    case NODE_CHANGED:
                        logger.error("No need to deal with [oldData:{}] [newData:{}]", childDataToString(oldData), childDataToString(newData));
                        initZookeeper();
                        break;
                    case NODE_CREATED: // 意味着有可能来了自己作为消费者需要关心的服务提供者
                        var providerStr = StringUtils.substringAfterFirst(newData.getPath(), providerRootPath + StringUtils.SLASH);
                        var providerRegister = Register.parseString(providerStr);
                        var localRegisterVO = NetContext.getConfigManager().getLocalConfig().toLocalRegister();
                        // 如果启动的Consumer是自己关心的Consumer，那么就会接下来尝试连接他们
                        // 这意味着：如果有多个Consumer启动，那么最后将全部连接上去
                        if (Register.providerHasConsumer(providerRegister, localRegisterVO)) {
                            providerRegisterSet.add(providerRegister);
                            checkConsumer();
                            logger.info("Discover new subscription service of provider [{}]", providerStr);
                        }
                        break;
                    case NODE_DELETED:
                        var oldProviderStr = StringUtils.substringAfterFirst(oldData.getPath(), providerRootPath + StringUtils.SLASH);
                        var oldProvider = Register.parseString(oldProviderStr);
                        if (providerRegisterSet.contains(oldProvider)) {
                            providerRegisterSet.remove(oldProvider);
                            checkConsumer();
                            logger.info("Unsubscribe from the service of provider [{}]", oldProviderStr);
                        }
                        break;
                    default:
                }
            }

            @Override
            public void initialized() {
                SchedulerBus.schedule(() -> initZookeeper(), 3, TimeUnit.SECONDS);
            }
        }, executor);

        providerCuratorCache.start();
    }

    private void initZookeeper() {
        executor.execute(() -> {
            try {
                // 既有Provider，又有Consumer信息，注册到zk上，这是一个临时节点
                // 这一步是：如果自己是服务提供者，就把自己注册上去
                initLocalProvider();

                // 自己是消费者，则连接所有自己关心的服务提供者
                initConsumerCache();
            } catch (Throwable t) {
                logger.error("Zookeeper failed to initialize, wait [{}] seconds to reinitialize", RETRY_SECONDS, t);
                SchedulerBus.schedule(() -> initZookeeper(), RETRY_SECONDS, TimeUnit.SECONDS);
            }
        });
    }

    /**
     * 如果自己是服务提供者，就把自己注册上去
     */
    private void initLocalProvider() throws Exception {
        var localRegisterVO = NetContext.getConfigManager().getLocalConfig().toLocalRegister();
        if (Objects.nonNull(localRegisterVO.getProviderConfig())) {
            var localProviderVoStr = localRegisterVO.toProviderString();
            var localProviderPath = providerRootPath + StringUtils.SLASH + localProviderVoStr;

            // /zfoo/provider
            // applicationNameTest | 192.168.1.104:12400 | provider:[myProviderModule-provider1, myProviderModule-provider2]
            var localProviderStat = curator.checkExists().forPath(localProviderPath);
            if (Objects.isNull(localProviderStat)) {
                curator.create()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(localProviderPath, StringUtils.EMPTY.getBytes());
                logger.info("Provider register successful at [{}]", localProviderVoStr);
            } else {
                // 如果服务提供者已经有节点了，防止这个节点是是上次来不及删除的临时节点
                var curatorSessionId = curator.getZookeeperClient().getZooKeeper().getSessionId();
                var providerNodeSessionId = localProviderStat.getEphemeralOwner();
                if (curatorSessionId != providerNodeSessionId) {
                    curator.delete()
                            .guaranteed()
                            .deletingChildrenIfNeeded()
                            .withVersion(localProviderStat.getVersion())
                            .forPath(localProviderPath);
                    throw new RuntimeException(StringUtils.format("session of curator[sessionId:{}] and providerNode[sessionId:{}] can not match, delete old old session data"
                            , curatorSessionId, providerNodeSessionId));
                }
            }
        }
    }

    /**
     * 遍历zk中的provider信息，从而找到所有自己关心的Provider，从而去连接他们
     * 注意：自己作为Provider那自己启动下就行了比较简单。 但是作为Consumer，那么会尝试连接所有已经注册到zk上来的服务器信息
     */
    private void initConsumerCache() throws Exception {
        // /zfoo/provider/applicationNameTest | 192.168.1.104:12400 | provider:[myProviderModule-provider1, myProviderModule-provider2]
        var localRegisterVO = NetContext.getConfigManager().getLocalConfig().toLocalRegister();
        // 初始化providerCacheSet
        // 遍历provider下注册的所有节点
        var remoteProviderSet = curator.getChildren().forPath(providerRootPath).stream()
                .filter(it -> StringUtils.isNotBlank(it) && !"null".equals(it))
                .map(it -> Register.parseString(it))
                .filter(it -> Objects.nonNull(it))
                // 检查是否这个节点是自己关心的节点
                .filter(it -> Register.providerHasConsumer(it, localRegisterVO))
                .collect(Collectors.toSet());

        providerRegisterSet.clear();

        // 将自己关心的节点存起来，接下来，将会开启TcpClient去连接这些Provider，连接上后，将会把这个session保存到ClientSessionMap中
        providerRegisterSet.addAll(remoteProviderSet);

        // 初始化consumer，providerCacheSet改变会导致消费者改变
        // 如果自己没有连接上远程消费者，则会一直尝试连接
        checkConsumer();
    }

    /**
     * 不管是节点增删，都会调用，因为：有可能自己作为消费者，服务提供者增加了也可能减少了，都要检测下
     */
    @Override
    public void checkConsumer() {
        if (curator == null) {
            return;
        }

        if (curator.getState() == CuratorFrameworkState.STOPPED) {
            return;
        }

        executor.execute(ThreadUtils.safeRunnable(() -> doCheckConsumer()));
    }

    /**
     * 检查下自己作为消费者，需要连接到的Provider是否全部连接上了，没连接上，就会创建TcpClient进行连接
     */
    private void doCheckConsumer() {
        if (curator.getState() != CuratorFrameworkState.STARTED) {
            logger.error("Curator has not been started yet, ignoring this consumer check");
            return;
        }

        var recheckFlag = false;

        for (var providerCache : providerRegisterSet) {
            // 先排除已经启动的consumer
            var consumerClientList = new ArrayList<Session>();
            NetContext.getSessionManager().forEachClientSession(new Consumer<Session>() {
                @Override
                public void accept(Session session) {
                    if (session.getConsumerRegister() != null && session.getConsumerRegister().equals(providerCache)) {
                        consumerClientList.add(session);
                    }
                }
            });

            // consumerClientList大于等于1，说明消费者连接成功了
            if (consumerClientList.size() == 1) {
                var consumer = consumerClientList.get(0);
                if (!SessionUtils.isActive(consumer)) {
                    recheckFlag = true;
                    NetContext.getSessionManager().removeClientSession(consumer);
                    logger.error("[consumer:{}] lost connection, removed from ClientSession", consumer);
                }
                continue;
            }

            if (consumerClientList.size() > 1) {
                logger.error("[consumerClientList:{}] are multiple duplicate [RegisterVO:{}]", consumerClientList, providerCache);
                continue;
            }

            try {
                // 自己作为消费者，要创建一个TcpClient去连接服务提供者
                var client = new TcpClient(HostAndPort.valueOf(providerCache.getProviderConfig().getAddress()));
                var session = client.start();
                if (session == null) {
                    recheckFlag = true;
                    continue;
                }

                session.setConsumerRegister(providerCache);
                logger.info("Consumer starts consuming the provider:[{}]", providerCache);
                EventBus.post(ConsumerStartEvent.valueOf(providerCache, session));
            } catch (Throwable t) {
                logger.error("[consumer:{}] failed to start, wait [{}] seconds to recheck consumer", providerCache, RETRY_SECONDS, t);
                recheckFlag = true;
            }
        }

        // 将自己的消费者消息写到 /consumer 的临时节点下
        updateConsumerData();

        if (recheckFlag) {
            SchedulerBus.schedule(() -> checkConsumer(), RETRY_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void updateConsumerData() {
        var list = new ArrayList<String>();
        NetContext.getSessionManager().forEachClientSession(session -> {
            var consumerAttribute = session.getConsumerRegister();
            if (consumerAttribute == null) {
                return;
            }
            var providerConfig = consumerAttribute.getProviderConfig();
            if (providerConfig == null) {
                return;
            }
            list.add(consumerAttribute.toProviderSimple());
        });

        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        // 将自己的消费者消息写到 /consumer 的临时节点下
        var localRegisterVO = NetContext.getConfigManager().getLocalConfig().toLocalRegister();
        var path = consumerRootPath + StringUtils.SLASH + localRegisterVO.toConsumerString();
        addData(path, StringUtils.bytes(JsonUtils.object2String(list)), CreateMode.EPHEMERAL);
    }

    @Override
    public void addData(String path, byte[] bytes, CreateMode mode) {
        try {
            var stat = curator.checkExists().forPath(path);

            if (Objects.isNull(stat)) {
                curator.create()
                        .creatingParentsIfNeeded()
                        .withMode(mode)
                        .forPath(path, bytes);
            } else {
                curator.setData().forPath(path, bytes);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除路径
     *
     * @param path
     */
    @Override
    public void removeData(String path) {
        try {
            curator.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询某个路径下的数据
     *
     * @param path
     * @return
     */
    @Override
    public byte[] queryData(String path) {
        try {
            return curator.getData().storingStatIn(new Stat()).forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 是否有某个路径
     */
    @Override
    public boolean haveNode(String path) {
        try {
            return Objects.nonNull(curator.checkExists().forPath(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询某个路径下的所有子路径
     */
    @Override
    public List<String> children(String path) {
        try {
            var children = curator.getChildren().forPath(path)
                    .stream()
                    .filter(it -> StringUtils.isNotBlank(it) && !"null".equals(it))
                    .toList();
            return children;
        } catch (Exception e) {
            logger.error("query children unknown exception", e);
        } catch (Throwable t) {
            logger.error("query children unknown error", t);
        }
        return Collections.emptyList();
    }

    @Override
    public String rootPath() {
        return rootPath;
    }

    /**
     * 查询所有服务信息(提供者+消费者信息)
     */
    @Override
    public List<Register> remoteProviderRegisters() {
        try {
            var remoteProviders = children(providerRootPath)
                    .stream()
                    .map(Register::parseString)
                    .filter(Objects::nonNull)
                    .toList();
            return remoteProviders;
        } catch (Exception e) {
            logger.error("remoteProviderRegisters unknown exception", e);
        } catch (Throwable t) {
            logger.error("remoteProviderRegisters unknown error", t);
        }
        return Collections.emptyList();
    }

    /**
     * 某个路径下发生数据变更（更新和创建）
     * 数据删除
     * 时，进行回调
     *
     * @param listenerPath   需要监听的路径
     * @param updateCallback 回调方法，第一个参数是路径，第二个是变化的内容
     * @param removeCallback 回调方法，第一个参数是路径，第二个是变化的内容
     */
    @Override
    public void addListener(String listenerPath, BiConsumer<String, byte[]> updateCallback, Consumer<String> removeCallback) {
        try {
            var providerStat = curator.checkExists().forPath(listenerPath);
            if (Objects.isNull(providerStat)) {
                curator.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(listenerPath, ArrayUtils.EMPTY_BYTE_ARRAY);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var listener = CuratorCache.builder(curator, listenerPath).build();
        listener.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldData, ChildData newData) {
                switch (type) {
                    case NODE_CHANGED:
                    case NODE_CREATED:
                        logger.info("listener child updated [oldData:{}] [newData:{}]", childDataToString(oldData), childDataToString(newData));
                        if (updateCallback != null) {
                            try {
                                updateCallback.accept(newData.getPath(), newData.getData());
                            } catch (Exception e) {
                                logger.error("listener child updated error", e);
                            }
                        }
                        break;
                    case NODE_DELETED:
                        if (removeCallback != null) {
                            removeCallback.accept(oldData.getPath());
                        }
                        break;
                    default:
                }
            }
        }, executor);
        listener.start();
        listenerList.add(listener);
    }

    @Override
    public void shutdown() {
        if (curator == null) {
            return;
        }
        try {
            var localRegisterVO = NetContext.getConfigManager().getLocalConfig().toLocalRegister();
            if (curator.getState() == CuratorFrameworkState.STARTED) {
                // 删除服务提供者的临时节点
                if (Objects.nonNull(localRegisterVO.getProviderConfig())) {
                    var localProviderPath = providerRootPath + StringUtils.SLASH + localRegisterVO.toProviderString();
                    var localProviderStat = curator.checkExists().forPath(localProviderPath);
                    if (Objects.nonNull(localProviderStat)) {
                        curator.delete().guaranteed().deletingChildrenIfNeeded().forPath(localProviderPath);
                    }
                }

                // 删除服务消费者的临时节点
                if (Objects.nonNull(localRegisterVO.getConsumerConfig())) {
                    var localConsumerPath = consumerRootPath + StringUtils.SLASH + localRegisterVO.toConsumerString();
                    var localConsumerStat = curator.checkExists().forPath(localConsumerPath);
                    if (Objects.nonNull(localConsumerStat)) {
                        curator.delete().guaranteed().deletingChildrenIfNeeded().forPath(localConsumerPath);
                    }
                }
            }
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getMessage(e));
        }

        try {
            listenerList.forEach(it -> IOUtils.closeIO(it));
            IOUtils.closeIO(providerCuratorCache, curator);
            ThreadUtils.shutdown(executor);
        } catch (Throwable e) {
            logger.error(ExceptionUtils.getMessage(e));
        }
    }

    private String childDataToString(ChildData childData) {
        if (childData == null) {
            return StringUtils.EMPTY;
        }

        // 只打印data数据比较小的内容
        if (childData.getData() == null || childData.getData().length <= 8) {
            return childData.toString();
        }

        return StringUtils.format("[path:{}] [stat:{}] [dataSize:{}]", childData.getPath(), childData.getStat(), childData.getData().length);
    }

}
