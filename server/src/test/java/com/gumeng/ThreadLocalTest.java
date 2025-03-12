package com.gumeng;

import org.junit.jupiter.api.Test;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/12 下午9:25
 */
public class ThreadLocalTest {

    @Test
    public void testThreadLocalSetAndGet() {
        //提供一个ThreadLocal对象
        ThreadLocal t1 = new ThreadLocal();

        //开启两个线程
        new Thread(() -> {
            t1.set("用户1");
            System.out.println(Thread.currentThread().getName()+":"+t1.get());
            System.out.println(Thread.currentThread().getName()+":"+t1.get());
            System.out.println(Thread.currentThread().getName()+":"+t1.get());
        },"一号线程1").start();

        new Thread(() -> {
            t1.set("用户2");
            System.out.println(Thread.currentThread().getName()+":"+t1.get());
            System.out.println(Thread.currentThread().getName()+":"+t1.get());
            System.out.println(Thread.currentThread().getName()+":"+t1.get());
        },"二号线程2").start();
    }
}
