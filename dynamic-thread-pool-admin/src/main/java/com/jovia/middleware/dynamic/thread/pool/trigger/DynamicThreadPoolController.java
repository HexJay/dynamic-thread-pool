package com.jovia.middleware.dynamic.thread.pool.trigger;

import com.alibaba.fastjson.JSON;
import com.jovia.middleware.dynamic.thread.pool.constants.RedisKeys;
import com.jovia.middleware.dynamic.thread.pool.entity.ThreadPoolConfigEntity;
import com.jovia.middleware.dynamic.thread.pool.type.Response;
import com.jovia.middleware.dynamic.thread.pool.type.ResponseStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jay
 * @date 2025-10-20-23:04
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/dynamic_thread_pool/")
public class DynamicThreadPoolController {

    @Resource
    private RedissonClient redisson;

    @GetMapping(value = "query_thread_pool_list")
    public Response queryThreadPoolList() {
        try {
            // 获取所有 THREAD_POOL_CONFIGS:* 的 key
            KeysScanOptions options = KeysScanOptions.defaults().pattern(RedisKeys.THREAD_POOL_CONFIGS + "*");
            Iterable<String> keys = redisson.getKeys().getKeys(options);

            List<ThreadPoolConfigEntity> allThreadPools = new ArrayList<>();

            for (String key : keys) {
                RMap<String, String> appMap = redisson.getMap(key);
                Collection<String> values = appMap.values();

                values.stream()
                        .map(json -> JSON.parseObject(json, ThreadPoolConfigEntity.class))
                        .forEach(allThreadPools::add);
            }
            return Response.ok().addData("configs", allThreadPools);
        } catch (Exception e) {
            log.error("查询线程池数据异常:{}", e.getMessage());
            return Response.error("查询线程池数据异常");
        }
    }

    @GetMapping("query_thread_pool_config")
    public Response queryThreadPoolConfig(@RequestParam String appName, @RequestParam String poolName) {
        try {
            if (StringUtils.isBlank(appName) || StringUtils.isBlank(poolName)) {
                return Response.error("应用名或线程池名不能为空");
            }

            RMap<String, String> appMap = redisson.getMap(RedisKeys.THREAD_POOL_CONFIGS + appName);
            String json = appMap.get(poolName);
            if (json == null) {
                return Response.error("线程池配置不存在");
            }

            ThreadPoolConfigEntity config = JSON.parseObject(json, ThreadPoolConfigEntity.class);
            return Response.ok().addData("config", config);
        } catch (Exception e) {
            log.error("查询线程池配置异常:{}", e.getMessage());
            return Response.error("查询线程池配置异常");
        }
    }

    @PostMapping("update_thread_pool_config")
    public Response updateThreadPoolConfig(@RequestBody ThreadPoolConfigEntity config) {
        try {
            RTopic topic = redisson.getTopic(RedisKeys.THREAD_POOL_CONFIG_TOPIC + config.getAppName());
            topic.publish(JSON.toJSONString(config));
            log.info("线程池 [{}] 配置更新成功: {}", config.getThreadPoolName(), JSON.toJSONString(config));

            return new Response(ResponseStatusEnum.OK);
        } catch (Exception e) {
            log.error("更新线程池配置失败:{}", e.getMessage());
            return Response.error("更新线程池配置失败");
        }
    }
}
