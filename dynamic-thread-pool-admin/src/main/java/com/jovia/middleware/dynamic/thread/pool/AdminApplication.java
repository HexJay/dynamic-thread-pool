package com.jovia.middleware.dynamic.thread.pool;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Jay
 * @date 2025-10-20-23:08
 */
@SpringBootApplication
@Configurable
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}

