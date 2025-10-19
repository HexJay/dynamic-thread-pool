package com.jovia.middleware.dynamic.thread.pool.sdk.config;

import com.alibaba.fastjson.JSON;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.IRegistry;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.redis.RedisRegistry;
import com.jovia.middleware.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Jay
 * @date 2025/10/19 00:24
 * @description 动态配置入口
 */
@Configuration
@EnableScheduling
public class DynamicThreadPoolAutoConfig {

    private static final Logger log = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    /**
     * 注入 Redisson
     */
    @Bean
    public Redisson redisson(DynamicThreadPoolAutoConfigProperties properties) {
        // 单机模式
        Config config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE);

        config.setCodec(new StringCodec())
                .useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setKeepAlive(properties.isKeepAlive())
                .setRetryAttempts(properties.getRetryAttempts())
                .setPingConnectionInterval(properties.getPingInterval())
                .setConnectTimeout(properties.getConnectTimeout());

        log.info("动态线程池，redis 注册中心初始化完成。{} {}", properties.getHost(), properties.getPoolSize());

        return (Redisson) Redisson.create(config);
    }

    @Bean
    public IRegistry redisRegistry(Redisson redisson) {
        return new RedisRegistry(redisson);
    }
    
    @Bean
    public DynamicThreadPoolService dynamicThreadPoolService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        if (StringUtils.isBlank(applicationName)) {
            applicationName = "null";
            log.warn("动态线程池，启动提示。Spring Boot 应用未配置 spring.application.name 无法获取应用名称！");
        }
        log.info("线程池信息:{}", JSON.toJSONString(threadPoolExecutorMap.keySet()));

        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }
    
    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(IRegistry iRegistry, DynamicThreadPoolService dynamicThreadPoolService) {
        return new ThreadPoolDataReportJob(iRegistry, dynamicThreadPoolService);
    }
}
