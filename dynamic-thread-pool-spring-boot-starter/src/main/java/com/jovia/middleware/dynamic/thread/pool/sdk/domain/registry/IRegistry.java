package com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry;

import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;

import java.util.List;

/**
 * 注册中心接口
 * @author Jay
 * @date 2025-10-19-16:12
 */
public interface IRegistry {
    
    /**
     * 上报当前应用的全部线程池参数
     */
    void reportAllThreadPools(List<ThreadPoolConfigEntity> threadPoolEntities);

    /** 
     * 上报单个线程池参数
     */
    void reportThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);
}
