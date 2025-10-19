package com.jovia.test;

import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RTopic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

/**
 * @author Jay
 * @date 2025/10/19 00:25
 * @description
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ApiTest {
    
    @Resource
    private RTopic topic;
    
    @Resource
    private ApplicationContext applicationContext;
    
    @Test
    public void test() throws InterruptedException {
        String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        ThreadPoolConfigEntity config = new ThreadPoolConfigEntity();
        config.setAppName(applicationName);
        config.setThreadPoolName("threadPoolExecutor");
        config.setCorePoolSize(100);
        config.setMaximumPoolSize(200);
        topic.publish(config);
        
        new CountDownLatch(1).await();
    }
}
