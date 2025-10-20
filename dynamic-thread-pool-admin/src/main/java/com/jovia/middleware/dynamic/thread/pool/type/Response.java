package com.jovia.middleware.dynamic.thread.pool.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jay
 * @date 2025-10-20-23:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    /** 状态码 */
    private Integer status;
    /** 消息 */
    private String msg;

    /** 数据 */
    private Map<String, Object> data = new HashMap<>();

    // 构造函数（枚举）
    public Response(ResponseStatusEnum statusEnum) {
        this.status = statusEnum.value();
        this.msg = statusEnum.msg();
    }

    /** ======= 工厂方法 ======= */
    public static  Response ok() {
        return new Response(ResponseStatusEnum.OK);
    }

    public static  Response error(String msg) {
        Response resp = new Response(ResponseStatusEnum.ERROR);
        resp.setMsg(msg);
        return resp;
    }

    /** ======= 链式操作 ======= */
    /**
     * 通过全局枚举字段直接装配
     * @param status 全局枚举状态码
     * @return 通用返回结果
     */
    public Response setStatus(ResponseStatusEnum status) {
        this.status = status.value();
        this.msg = status.msg();
        return this;
    }

    public Response setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public Response addData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public Response removeExtra(String key) {
        this.data.remove(key);
        return this;
    }

    /** ======= 辅助方法 ======= */
    public boolean isOk() {
        return status == ResponseStatusEnum.OK.value();
    }

    @Override
    public String toString() {
        return "[" + status + " : " + msg + "] data=" + data + ", extra=" + data;
    }
}
