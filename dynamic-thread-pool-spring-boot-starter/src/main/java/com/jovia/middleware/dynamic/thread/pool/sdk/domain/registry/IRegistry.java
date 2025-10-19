package com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry;

import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;

import java.util.List;

/**
 * 注册中心接口
 * @author Jay
 * @date 2025-10-19-16:12
 */
public interface IRegistry {
    void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities);

    void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity);
}
