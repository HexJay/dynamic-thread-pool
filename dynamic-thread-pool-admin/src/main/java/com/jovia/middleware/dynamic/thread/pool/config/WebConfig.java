package com.jovia.middleware.dynamic.thread.pool.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 * 
 * @author Jay
 * @date 2025-10-21
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 将根路径重定向到 index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}

