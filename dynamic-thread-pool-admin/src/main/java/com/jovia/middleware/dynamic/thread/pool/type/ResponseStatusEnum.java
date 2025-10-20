package com.jovia.middleware.dynamic.thread.pool.type;

/**
 * @author Jay
 * @date 2025-10-10-22:44
 */
public enum ResponseStatusEnum {

    /** 标准状态码 */
    OK(0, "ok"),
    BAD_REQUEST(400, "请求失败"),
    INTERNAL_SERVER_ERROR(500, "服务器出错"),

    /** 系统错误码 */
    UNKNOWN_ERROR(100, "未知异常"),
    DATABASE_ERROR(111, "数据库错误"),
    ILLEGAL_OPERATION(120, "非法操作"),

    /** 错误码 */
    ERROR(1000, "未知错误");

    /** 状态码 */
    private final int value;

    /** 附加信息 */
    private final String msg;

    ResponseStatusEnum(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    public int value() {
        return value;
    }

    public String msg() {
        return msg;
    }
}
