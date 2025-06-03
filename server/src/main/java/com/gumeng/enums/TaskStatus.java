package com.gumeng.enums;

import lombok.Getter;

/**
 * 功能：任务状态判断
 * 作者：Z
 * 日期：2025/6/3 下午7:37
 */
@Getter
public enum TaskStatus {
    QUEUED("queued", "任务正在等待轮到它处理。"),
    RUNNING("running", "任务当前正在进行中。"),
    SUCCESS("success", "任务已成功完成。这些值可以用作最终结果。"),
    FAILED("failed", "任务失败，通常是由于我们这边的问题。请报告此情况以及寻求支持。"),
    CANCELLED("cancelled", "任务已取消。"),
    UNKNOWN("unknown", "无法确定任务的当前状态，这可能表示系统级别存在问题。如需帮助，请联系我们的支持团队。");

    private final String value;
    private final String message;

    TaskStatus(String value, String message) {
        this.value = value;
        this.message = message;
    }

    // 静态方法：通过value获取对应的TaskStatus对象
    public static TaskStatus fromValue(String value) {
        for (TaskStatus status : values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return UNKNOWN; // 如果没有匹配的枚举常量，则返回UNKNOWN
    }
}
