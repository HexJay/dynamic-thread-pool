package com.jovia.middleware.dynamic.thread.pool.sdk.trigger.job;

import com.alibaba.fastjson.JSON;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.IRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 线程池数据上报任务
 * @author Jay
 * @date 2025-10-19-16:47
 */
public class ThreadPoolDataReportJob {
    
    private final Logger logger = LoggerFactory.getLogger(ThreadPoolDataReportJob.class);
    private IRegistry registry;
    private IDynamicThreadPoolService dynamicThreadPoolService;

    public ThreadPoolDataReportJob(IRegistry registry, IDynamicThreadPoolService dynamicThreadPoolService) {
        this.registry = registry;
        this.dynamicThreadPoolService = dynamicThreadPoolService;
    }
    
    @Scheduled(cron = "*/5 * * * * ?")
    public void report() {
        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryAllThreadPools();
        registry.reportThreadPool(threadPoolConfigEntities);
        logger.info("动态线程池，上报线程池信息:{}", JSON.toJSONString(threadPoolConfigEntities));
        
        for (ThreadPoolConfigEntity threadPoolConfigEntity : threadPoolConfigEntities) {
            registry.reportThreadPoolConfigParameter(threadPoolConfigEntity);
            logger.info("动态线程池，上报线程池配置参数:{}", JSON.toJSONString(threadPoolConfigEntity));
        }
    }
}
