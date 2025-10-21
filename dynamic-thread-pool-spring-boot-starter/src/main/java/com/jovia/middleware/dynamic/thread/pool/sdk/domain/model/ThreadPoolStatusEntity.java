package com.jovia.middleware.dynamic.thread.pool.sdk.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 线程池配置实体对象
 *
 * @author Jay
 * @date 2025-10-19-15:04
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {
        "appName",
        "threadPoolName",
        "corePoolSize",
        "maximumPoolSize",
        "queueType"
})
public class ThreadPoolStatusEntity {
    
    public ThreadPoolStatusEntity(String appName, String threadPoolName) {
        this.appName = appName;
        this.threadPoolName = threadPoolName;
    }

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 线程池名称
     */
    private String threadPoolName;

    /**
     * 核心线程数
     */
    private int corePoolSize;

    /**
     * 最大线程数
     */
    private int maximumPoolSize;

    /**
     * 当前活跃线程数
     */
    private int activeCount;

    /**
     * 当前池中线程数
     */
    private int poolSize;

    /**
     * 队列类型
     */
    private String queueType;

    /**
     * 当前队列任务数
     */
    private int queueSize;

    /**
     * 队列剩余任务数
     */
    private int remainingCapacity;
}
