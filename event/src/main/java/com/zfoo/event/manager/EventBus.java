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
package com.zfoo.event.manager;

import com.zfoo.event.enhance.IEventReceiver;
import com.zfoo.event.model.ExceptionEvent;
import com.zfoo.event.model.IEvent;
import com.zfoo.event.model.TripleConsumer;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.RandomUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author godotg
 */
public abstract class EventBus {

    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);

    /**
     * EN: The size of the thread pool. Event's thread pool is often used to do time-consuming operations, so set it a little bigger
     * CN: 线程池的大小. event的线程池经常用来做一些耗时的操作，所以要设置大一点
     */
    public static final int EXECUTORS_SIZE = MathUtil.safeFindNextPositivePowerOfTwo(Math.max(Runtime.getRuntime().availableProcessors(), 4) * 2);
    private static final int EXECUTOR_MASK = EXECUTORS_SIZE - 1;

    private static final ExecutorService[] executors = new ExecutorService[EXECUTORS_SIZE];

    /**
     * event mapping
     */
    private static final Map<Class<? extends IEvent>, List<IEventReceiver>> receiverMap = new HashMap<>();

    /**
     * event exception handler
     */
    public static TripleConsumer<IEventReceiver, IEvent, Throwable> exceptionFunction = null;
    /**
     * event noReceiver handler
     */
    public static Consumer<IEvent> noReceiverFunction = event -> {};

    static {
        for (int i = 0; i < executors.length; i++) {
            var namedThreadFactory = new EventThreadFactory(i);
            var executor = Executors.newSingleThreadExecutor(namedThreadFactory);
            executors[i] = executor;
        }
    }

    public static class EventThreadFactory implements ThreadFactory {
        private final int poolNumber;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;

        public EventThreadFactory(int poolNumber) {
            this.group = Thread.currentThread().getThreadGroup();
            this.poolNumber = poolNumber;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            var threadName = StringUtils.format("event-p{}-t{}", poolNumber + 1, threadNumber.getAndIncrement());
            var thread = new FastThreadLocalThread(group, runnable, threadName);
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setUncaughtExceptionHandler((t, e) -> logger.error(t.toString(), e));
            var executor = executors[poolNumber];
            AssertionUtils.notNull(executor);
            ThreadUtils.registerSingleThreadExecutor(thread, executor);
            return thread;
        }
    }

    /**
     * Publish the event
     *
     * @param event Event object
     */
    public static void post(IEvent event) {
        if (event == null) {
            return;
        }
        var clazz = event.getClass();
        var receivers = receiverMap.get(clazz);
        if (CollectionUtils.isEmpty(receivers)) {
            noReceiverFunction.accept(event);
            return;
        }
        for (var receiver : receivers) {
            switch (receiver.bus()) {
                case CurrentThread -> doReceiver(receiver, event);
                case AsyncThread -> asyncExecute(event.executorHash(), () -> doReceiver(receiver, event));
//                case VirtualThread -> Thread.ofVirtual().name("virtual-on" + clazz.getSimpleName()).start(() -> doReceiver(receiver, event));
            }
        }
    }

    private static void doReceiver(IEventReceiver receiver, IEvent event) {
        try {
            receiver.invoke(event);
        } catch (Throwable t) {
            if (exceptionFunction == null) {
                logger.error("bean:[{}] event:[{}] unhandled exception", receiver.getBean().getClass().getSimpleName(), event.getClass().getSimpleName(), t);
            } else {
                exceptionFunction.accept(receiver, event, t);
            }
            post(new ExceptionEvent(receiver, event, t));
        }
    }

    public static void asyncExecute(Runnable runnable) {
        asyncExecute(RandomUtils.randomInt(), runnable);
    }

    /**
     * Use the event thread specified by the hashcode to execute the task
     */
    public static void asyncExecute(int hash, Runnable runnable) {
        executorOf(hash).execute(ThreadUtils.safeRunnable(runnable));
    }

    public static ExecutorService executorOf(int hash) {
        return executors[hash & EXECUTOR_MASK];
    }

    /**
     * Register the event and its counterpart observer
     */
    public static void registerEventReceiver(Class<? extends IEvent> eventType, IEventReceiver receiver) {
        receiverMap.computeIfAbsent(eventType, it -> new ArrayList<>(1)).add(receiver);
    }

}


