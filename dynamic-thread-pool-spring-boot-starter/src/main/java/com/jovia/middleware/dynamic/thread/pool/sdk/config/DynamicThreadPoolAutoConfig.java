package com.jovia.middleware.dynamic.thread.pool.sdk.config;

import com.alibaba.fastjson.JSON;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.IRegistry;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.redis.RedisRegistry;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.valobj.RegistryEnumVO;
import com.jovia.middleware.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import com.jovia.middleware.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustListener;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
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
import java.util.Set;
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
    
    private final ApplicationContext applicationContext;

    public DynamicThreadPoolAutoConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 注入 Redisson
     */
    @Bean
    public Redisson redisson(DynamicThreadPoolAutoConfigProperties properties) {
        // 单机模式
        Config config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE);

        config.useSingleServer()
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
    public DynamicThreadPoolService dynamicThreadPoolService(Map<String, ThreadPoolExecutor> threadPoolExecutorMap, Redisson redisson) {
        String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        if (StringUtils.isBlank(applicationName)) {
            applicationName = "null";
            log.warn("动态线程池，启动提示。Spring Boot 应用未配置 spring.application.name 无法获取应用名称！");
        }
        log.info("线程池信息:{}", JSON.toJSONString(threadPoolExecutorMap.keySet()));
        
        // 启动时，从配置中心获取配置
        Set<String> threadPoolKeys = threadPoolExecutorMap.keySet();
        for (String threadPoolKey : threadPoolKeys) {
            String key = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + applicationName + "_" + threadPoolKey;
            ThreadPoolConfigEntity threadPoolConfigEntity = redisson.<ThreadPoolConfigEntity>getBucket(key).get();
            log.info("初始配置:{}", JSON.toJSONString(threadPoolConfigEntity));
            if (threadPoolConfigEntity == null) continue;

            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolKey);
            threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
            threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
        }

        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }
    
    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(IRegistry iRegistry, DynamicThreadPoolService dynamicThreadPoolService) {
        return new ThreadPoolDataReportJob(iRegistry, dynamicThreadPoolService);
    }
    
    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(IRegistry iRegistry, DynamicThreadPoolService dynamicThreadPoolService) {
        return new ThreadPoolConfigAdjustListener(iRegistry, dynamicThreadPoolService);
    }
    
    @Bean
    public RTopic rTopic(Redisson redisson, ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener) {
        String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        RTopic topic = redisson.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + applicationName);
        topic.addListener(ThreadPoolConfigEntity.class, threadPoolConfigAdjustListener);
        return topic;
    }
}
