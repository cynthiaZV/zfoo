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

package com.zfoo.orm;

import com.zfoo.orm.accessor.IAccessor;
import com.zfoo.orm.manager.IOrmManager;
import com.zfoo.orm.model.IEntity;
import com.zfoo.orm.query.IQuery;
import com.zfoo.orm.query.IQueryBuilder;
import com.zfoo.scheduler.SchedulerContext;
import com.zfoo.scheduler.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

/**
 * @author godotg
 */
public class OrmContext implements ApplicationListener<ApplicationContextEvent>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(OrmContext.class);

    private static OrmContext instance;

    private ApplicationContext applicationContext;

    private IAccessor accessor;

    private IQuery query;

    private IOrmManager ormManager;

    private volatile boolean stop = false;

    public static ApplicationContext getApplicationContext() {
        return instance.applicationContext;
    }

    public static OrmContext getOrmContext() {
        return instance;
    }

    public static IAccessor getAccessor() {
        return instance.accessor;
    }

    public static <PK extends Comparable<PK>, E extends IEntity<PK>> IQueryBuilder<PK, E> getQuery(Class<E> entityClazz) {
        return instance.query.builder(entityClazz);
    }

    public static IOrmManager getOrmManager() {
        return instance.ormManager;
    }

    public static boolean isStop() {
        return instance.stop;
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            var stopWatch = new StopWatch();
            OrmContext.instance = this;
            instance.applicationContext = event.getApplicationContext();

            instance.accessor = applicationContext.getBean(IAccessor.class);
            instance.query = applicationContext.getBean(IQuery.class);
            instance.ormManager = applicationContext.getBean(IOrmManager.class);

            instance.ormManager.initBefore();
            instance.ormManager.inject();
            instance.ormManager.initAfter();

            logger.info("Orm started successfully and cost [{}] seconds", stopWatch.costSeconds());
        } else if (event instanceof ContextClosedEvent) {
            shutdown();
        }
    }

    public static synchronized void shutdown() {
        if (isStop()) {
            return;
        }
        instance.stop = true;
        SchedulerContext.shutdown();
        try {
            instance.ormManager
                    .getAllEntityCaches()
                    .forEach(it -> it.persistAllBlock());
            instance.ormManager.mongoClient().close();
        } catch (Exception e) {
            logger.error("Failed to close the MongoClient database connection", e);
        }
        logger.info("Orm shutdown gracefully.");
    }

    @Override
    public int getOrder() {
        return 1;
    }

}
