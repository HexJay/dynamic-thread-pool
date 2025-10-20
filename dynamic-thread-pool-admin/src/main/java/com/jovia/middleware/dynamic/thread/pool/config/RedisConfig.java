package com.jovia.middleware.dynamic.thread.pool.config;

import org.redisson.Redisson;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jay
 * @date 2025-10-20-23:16
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.database:0}")
    private int database;

    /**
     * 注入 Redisson
     */
    @Bean
    public Redisson redisson() {
        // 单机模式
        Config config = new Config();
        config.setCodec(new StringCodec())
                .useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database);

        return (Redisson) Redisson.create(config);

    }
}
