package com.jovia.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jay
 * @date 2025/10/19 00:48
 * @description
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "thread.pool.executor", ignoreInvalidFields = true)
public class ThreadPoolConfigProperties {
    /** 核心线程数 */
    private int corePoolSize;
    /** 最大线程数 */
    private int maxPoolSize;
    /** 线程最大空闲时间（单位：毫秒） */
    private long keepAliveTime;
    /** 阻塞队列容量 */
    private int blockQueueSize;
    /** 拒绝策略（如 CallerRunsPolicy, AbortPolicy 等） */
    private String policy;
}
