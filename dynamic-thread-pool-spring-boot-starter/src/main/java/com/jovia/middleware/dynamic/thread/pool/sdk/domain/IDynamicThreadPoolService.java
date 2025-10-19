package com.jovia.middleware.dynamic.thread.pool.sdk.domain;

import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @author Jay
 * @date 2025-10-19-15:03
 */
public interface IDynamicThreadPoolService {

    List<ThreadPoolConfigEntity> queryAllThreadPools();
    
    ThreadPoolConfigEntity queryThreadPoolByName(String threadPoolName);
    
    void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);
}
