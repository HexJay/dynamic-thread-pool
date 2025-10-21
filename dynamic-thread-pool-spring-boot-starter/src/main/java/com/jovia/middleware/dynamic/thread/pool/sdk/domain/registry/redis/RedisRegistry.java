package com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.redis;

import com.alibaba.fastjson.JSON;
import com.jovia.middleware.dynamic.thread.pool.sdk.constant.RedisKeys;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.registry.IRegistry;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * redis 注册中心
 *
 * @author Jay
 * @date 2025-10-19-16:13
 */
public class RedisRegistry implements IRegistry {

    private final RedissonClient redisson;

    public RedisRegistry(RedissonClient redisson) {
        this.redisson = redisson;
    }

    @Override
    public void reportAllThreadPools(List<ThreadPoolConfigEntity> threadPoolEntities) {
        if (CollectionUtils.isEmpty(threadPoolEntities)) {
            return;
        }

        String appName = threadPoolEntities.get(0).getAppName();
        RMap<String, String> appMap = redisson.getMap(RedisKeys.THREAD_POOL_CONFIGS + appName);
        
        Map<String, String> map = new HashMap<>();
        for (ThreadPoolConfigEntity entity : threadPoolEntities) {
            map.put(entity.getThreadPoolName(), JSON.toJSONString(entity));
        }
        appMap.putAll(map);
    }

    @Override
    public void reportThreadPoolConfig(ThreadPoolConfigEntity entity) {
        if (entity == null) return;
        String appName = entity.getAppName();
        RMap<String, String> appMap = redisson.getMap(RedisKeys.THREAD_POOL_CONFIGS + appName);

        appMap.put(entity.getThreadPoolName(), JSON.toJSONString(entity));
    }
}
