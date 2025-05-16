package com.gumeng.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标记在Controller方法上，用于自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    String operation() default "";
}