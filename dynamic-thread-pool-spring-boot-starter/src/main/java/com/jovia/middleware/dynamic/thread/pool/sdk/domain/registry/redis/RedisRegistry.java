package com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.redis;

import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.IRegistry;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.valobj.RegistryEnumVO;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RList;

import java.time.Duration;
import java.util.List;

/**
 * redis 注册中心
 * @author Jay
 * @date 2025-10-19-16:13
 */
public class RedisRegistry implements IRegistry {
    
    private final Redisson redisson;

    public RedisRegistry(Redisson redisson) {
        this.redisson = redisson;
    }
    
    @Override
    public void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities) {
        RList<ThreadPoolConfigEntity> list = redisson.getList(RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey());
        list.delete();
        list.addAll(threadPoolEntities);
    }

    @Override
    public void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity) {
        String cacheKey = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + threadPoolConfigEntity.getAppName() + "_" + threadPoolConfigEntity.getThreadPoolName();
        RBucket<ThreadPoolConfigEntity> bucket = redisson.getBucket(cacheKey);
        bucket.set(threadPoolConfigEntity, Duration.ofDays(30));
    }
}
