package com.jovia.middleware.dynamic.thread.pool.sdk.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.IRegistry;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jay
 * @date 2025-10-19-20:51
 */
public class ThreadPoolConfigAdjustListener implements MessageListener<ThreadPoolConfigEntity> {

    private final Logger logger = LoggerFactory.getLogger(ThreadPoolConfigAdjustListener.class);

    private final IRegistry registry;
    private final IDynamicThreadPoolService dynamicThreadPoolService;

    public ThreadPoolConfigAdjustListener(IRegistry registry, IDynamicThreadPoolService dynamicThreadPoolService) {
        this.registry = registry;
        this.dynamicThreadPoolService = dynamicThreadPoolService;
    }

    @Override
    public void onMessage(CharSequence channel, ThreadPoolConfigEntity msg) {
        logger.info("动态线程池 {} 配置更新, corePoolSize:{}, maximumPoolSize:{}", msg.getThreadPoolName(), msg.getCorePoolSize(), msg.getMaximumPoolSize());
        dynamicThreadPoolService.updateThreadPoolConfig(msg);
        
        // 上报数据
        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryAllThreadPools();
        registry.reportThreadPool(threadPoolConfigEntities);
        
        ThreadPoolConfigEntity threadPoolConfigEntity = dynamicThreadPoolService.queryThreadPoolByName(msg.getThreadPoolName());
        registry.reportThreadPoolConfigParameter(threadPoolConfigEntity);
        
        logger.info("动态线程池，上报线程池配置: {}", JSON.toJSONString(threadPoolConfigEntity));
    }
}
