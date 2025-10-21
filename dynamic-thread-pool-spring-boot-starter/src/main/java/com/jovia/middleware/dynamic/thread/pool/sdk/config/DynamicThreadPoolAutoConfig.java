package com.jovia.middleware.dynamic.thread.pool.sdk.config;

import com.alibaba.fastjson.JSON;
import com.jovia.middleware.dynamic.thread.pool.sdk.constant.RedisKeys;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.IRegistry;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.redis.RedisRegistry;
import com.jovia.middleware.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import com.jovia.middleware.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustListener;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
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

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.database:0}")
    private int database;

    public DynamicThreadPoolAutoConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    /**
     * 注入 Redisson
     */
    @Bean("thread-pool-redis")
    public RedissonClient redisson() {
        // 单机模式
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec())
                .useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database);

        return Redisson.create(config);
    }
    
    @Bean
    public IRegistry redisRegistry(RedissonClient redisson) {
        return new RedisRegistry(redisson);
    }
    
    @Bean
    public IDynamicThreadPoolService dynamicThreadPoolService(Map<String, ThreadPoolExecutor> threadPoolExecutorMap, RedissonClient redisson) {
        String appName = applicationContext.getEnvironment().getProperty("spring.application.name");
        if (StringUtils.isBlank(appName)) {
           throw new IllegalStateException("[DynamicThreadPool] 启动失败：未配置 spring.application.name，请在 application.yml 中配置。");
        }
        log.info("线程池信息:{}", JSON.toJSONString(threadPoolExecutorMap.keySet()));
        
        // 启动时，从配置中心获取配置
        RMap<String, String> appMap = redisson.getMap(RedisKeys.THREAD_POOL_CONFIGS + appName);
        
        Set<String> threadPoolKeys = threadPoolExecutorMap.keySet();
        for (String threadPoolKey : threadPoolKeys) {
            String json = appMap.get(threadPoolKey);
            if (StringUtils.isBlank(json)) {
                log.warn("线程池 [{}] 未在配置中心找到配置，使用默认参数", threadPoolKey);
                continue;
            }
            
            ThreadPoolConfigEntity threadPoolConfigEntity = JSON.parseObject(json, ThreadPoolConfigEntity.class);
            if (threadPoolConfigEntity == null) continue;
            if (threadPoolConfigEntity.getCorePoolSize() > threadPoolConfigEntity.getMaximumPoolSize()) {
                log.warn("配置错误：corePoolSize > maximumPoolSize for {}", threadPoolKey);
                continue;
            }

            log.info("初始配置:{}", JSON.toJSONString(threadPoolConfigEntity));
            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolKey);
            threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
            threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
        }

        return new DynamicThreadPoolService(appName, threadPoolExecutorMap);
    }
    
    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(IRegistry iRegistry, IDynamicThreadPoolService dynamicThreadPoolService) {
        return new ThreadPoolDataReportJob(iRegistry, dynamicThreadPoolService);
    }
    
    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(IRegistry iRegistry, IDynamicThreadPoolService dynamicThreadPoolService) {
        return new ThreadPoolConfigAdjustListener(iRegistry, dynamicThreadPoolService);
    }
    
    @Bean
    public RTopic rTopic(RedissonClient redisson, ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener) {
        String appName = applicationContext.getEnvironment().getProperty("spring.application.name");
        log.info("appName:{}", appName);
        RTopic topic = redisson.getTopic(RedisKeys.THREAD_POOL_CONFIG_TOPIC + appName);
        topic.addListener(String.class, threadPoolConfigAdjustListener);
        return topic;
    }
}
