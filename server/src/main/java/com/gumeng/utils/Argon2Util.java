package com.gumeng.utils;


import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * 功能：Argon2密码哈希工具类
 * 作者：Z
 * 日期：2025/3/14 上午11:24
 */
public class Argon2Util {

    // 使用 Argon2id 变体
    private static final Argon2 ARGON2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    // 默认参数
    private static final int ITERATIONS = 2;          // 迭代次数
    private static final int MEMORY = 65536;          // 内存消耗（单位：KB）
    private static final int PARALLELISM = 1;         // 并行度

    /**
     * 哈希密码（使用默认参数）
     *
     * @param password 明文密码
     * @return 哈希后的密码
     */
    public static String hash(String password) {
        return ARGON2.hash(ITERATIONS, MEMORY, PARALLELISM, password.toCharArray());
    }

    /**
     * 哈希密码（自定义参数）
     *
     * @param password    明文密码
     * @param iterations  迭代次数
     * @param memory      内存消耗（单位：KB）
     * @param parallelism 并行度
     * @return 哈希后的密码
     */
    public static String hash(String password, int iterations, int memory, int parallelism) {
        return ARGON2.hash(iterations, memory, parallelism, password.toCharArray());
    }

    /**
     * 验证密码
     *
     * @param password       明文密码
     * @param hashedPassword 哈希后的密码
     * @return 是否匹配
     */
    public static boolean verify(String password, String hashedPassword) {
        return ARGON2.verify(hashedPassword, password.toCharArray());
    }

    /**
     * 检查哈希是否需要重新计算（例如参数变更时）
     *
     * @param hashedPassword 哈希后的密码
     * @return 是否需要重新计算
     */
    public static boolean needsRehash(String hashedPassword) {
        return ARGON2.needsRehash(hashedPassword, ITERATIONS, MEMORY, PARALLELISM);
    }


}
