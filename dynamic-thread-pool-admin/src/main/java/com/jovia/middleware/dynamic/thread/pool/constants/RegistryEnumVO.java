package com.jovia.middleware.dynamic.thread.pool.constants;

/**
 * Redis配置中心 枚举值
 *
 * @author Jay
 * @date 2025-10-19-16:31
 */
public enum RegistryEnumVO {

    // 配置管理
    CONFIGS("configs", "线程池配置列表"),
    CONFIG_PARAM("config:param", "线程池配置参数"),
    MESSAGE_TOPIC("message:topic", "动态线程池监听主题");

    private final String key;
    private final String desc;

    RegistryEnumVO(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 获取完整的Redis Key（带统一前缀）
     */
    public String getFullKey() {
        return "dynamic:threadpool:" + key;
    }

    /**
     * 根据应用名称获取完整的Key（适用于多应用场景）
     */
    public String getFullKey(String applicationName) {
        return String.format("dynamic:threadpool:%s:%s", applicationName, key);
    }

    /**
     * 根据应用名称获取完整的Key（适用于多应用场景）
     */
    public String getFullKey(String applicationName, String poolName) {
        return String.format("dynamic:threadpool:%s:%s:%s", key, applicationName, poolName);
    }

    /**
     * 获取原始Key（不带前缀）
     */
    public String getOriginalKey() {
        return key;
    }
}
