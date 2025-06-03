package com.gumeng.enums;

import lombok.Getter;

@Getter
public enum AppCode {
    // HTTP 标准状态码
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源未找到"),
    SERVER_ERROR(500, "服务器错误"),

    // 自定义状态码
    PAYMENT_SUCCESS(1000, "支付成功"),
    PAYMENT_FAILED(1001, "支付失败"),
    PAYMENT_PENDING(1002, "支付处理中"),
    DATA_NOT_FOUND(2001, "数据未找到"),
    DATA_INVALID(2002, "数据无效"),
    DATA_CONFLICT(2003, "数据冲突"),

    //模型生成相关状态码
    MODEL_QUEUED(2001, "模型处于队列中"),
    MODEL_RUNNING(2002, "模型正在生成"),
    MODEL_SUCCESS(2003, "模型生成完成"),
    MODEL_FAILED(5001, "模型生成失败"),
    MODEL_CANCELLED(4003, "模型生成任务已取消"),
    MODEL_UNKNOWN(5006, "未知错误，你可以向我们反馈相关问题");

    private final int code;
    private final String msg;

    AppCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
