package com.gumeng.utils;

/**
 * 功能：ThreadLocal对象
 * 作者：Z
 * 日期：2025/3/12 下午9:36
 */

public class ThreadLocalUtil {
    //提供ThreadLocal对象
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    //根据键获取值
    public static <T> T get(){
        return (T) THREAD_LOCAL.get();
    }

    //存储键值对
    public static void set(Object value){
        THREAD_LOCAL.set(value);
    }

    //清除 ThreadLocal 防止内存泄漏
    public static void remove(){
        THREAD_LOCAL.remove();
    }
}
