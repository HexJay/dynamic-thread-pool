package com.jovia.middleware.dynamic.thread.pool.sdk.trigger.job;

import com.alibaba.fastjson.JSON;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.IRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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

    // 保存上一次上报的配置，用于检测变化
    private final Map<String, ThreadPoolConfigEntity> lastReported = new ConcurrentHashMap<>();
    
    @Scheduled(cron = "*/5 * * * * ?")
    public void report() {

        List<ThreadPoolConfigEntity> currentList  = dynamicThreadPoolService.queryAllThreadPools();

        for (ThreadPoolConfigEntity current : currentList) {
            ThreadPoolConfigEntity last = lastReported.get(current.getThreadPoolName());
            if (last == null || !Objects.equals(current, last)) {
                registry.reportThreadPoolConfig(current);
                lastReported.put(current.getThreadPoolName(), current);
                logger.info("检测到线程池配置变更，上报: {}", JSON.toJSONString(current));
            }
        }
    }
}
