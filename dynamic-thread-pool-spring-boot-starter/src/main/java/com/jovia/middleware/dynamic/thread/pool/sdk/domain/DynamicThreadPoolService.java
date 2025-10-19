package com.jovia.middleware.dynamic.thread.pool.sdk.domain;

import com.alibaba.fastjson.JSON;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Jay
 * @date 2025-10-19-15:17
 */
public class DynamicThreadPoolService implements IDynamicThreadPoolService {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolService.class);
    private final Map<String, ThreadPoolExecutor> threadPoolMap;
    private final String appName;

    public DynamicThreadPoolService(String appName, Map<String, ThreadPoolExecutor> threadPoolMap) {
        this.appName = appName;
        this.threadPoolMap = threadPoolMap;
    }

    @Override
    public List<ThreadPoolConfigEntity> queryAllThreadPools() {
        Set<String> threadPoolBeanNames = threadPoolMap.keySet();
        List<ThreadPoolConfigEntity> list = new ArrayList<>(threadPoolBeanNames.size());
        for (String beanName : threadPoolBeanNames) {
            ThreadPoolExecutor threadPoolExecutor = threadPoolMap.get(beanName);
            ThreadPoolConfigEntity threadPoolConfigVO = new ThreadPoolConfigEntity(appName, beanName);
            threadPoolConfigVO.setCorePoolSize(threadPoolExecutor.getCorePoolSize());
            threadPoolConfigVO.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
            threadPoolConfigVO.setActiveCount(threadPoolExecutor.getActiveCount());
            threadPoolConfigVO.setPoolSize(threadPoolExecutor.getPoolSize());
            threadPoolConfigVO.setQueueType(threadPoolExecutor.getQueue().getClass().getSimpleName());
            threadPoolConfigVO.setQueueSize(threadPoolExecutor.getQueue().size());
            threadPoolConfigVO.setRemainingCapacity(threadPoolExecutor.getQueue().remainingCapacity());
            list.add(threadPoolConfigVO);
        }
        return list;
    }

    @Override
    public ThreadPoolConfigEntity queryThreadPoolByName(String threadPoolName) {
        ThreadPoolExecutor threadPoolExecutor = threadPoolMap.get(threadPoolName);
        ThreadPoolConfigEntity threadPoolConfigEntity = new ThreadPoolConfigEntity(appName, threadPoolName);
        if (threadPoolExecutor == null) {
            return threadPoolConfigEntity;
        }
        threadPoolConfigEntity.setCorePoolSize(threadPoolExecutor.getCorePoolSize());
        threadPoolConfigEntity.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
        threadPoolConfigEntity.setActiveCount(threadPoolExecutor.getActiveCount());
        threadPoolConfigEntity.setPoolSize(threadPoolExecutor.getPoolSize());
        threadPoolConfigEntity.setQueueType(threadPoolExecutor.getQueue().getClass().getSimpleName());
        threadPoolConfigEntity.setQueueSize(threadPoolExecutor.getQueue().size());
        threadPoolConfigEntity.setRemainingCapacity(threadPoolExecutor.getQueue().remainingCapacity());

        if (logger.isDebugEnabled()) {
            logger.info("动态线程池，配置查询 应用名:{} 线程名:{} 池化配置:{}", appName, threadPoolName, JSON.toJSONString(threadPoolConfigEntity));
        }

        return threadPoolConfigEntity;
    }

    @Override
    public void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        if (threadPoolConfigEntity == null || !Objects.equals(threadPoolConfigEntity.getAppName(), appName)) return;
        ThreadPoolExecutor threadPoolExecutor = threadPoolMap.get(threadPoolConfigEntity.getThreadPoolName());
        if (threadPoolExecutor == null) return;
        
        // 设置核心线程数和最大线程数,先更新最大线程数
        threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
        threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
    }
}
