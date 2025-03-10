package com.gumeng.utils;

/**
 * 功能：
 * 作者：Z
 * 日期：2025/3/9 下午11:22
 */
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {
    protected static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    protected static MessageDigest messageDigest = null;

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
            System.err.println(Md5Util.class.getName() + "初始化失败，MessageDigest不支持MD5Util.");
            nsaex.printStackTrace();
        }
    }

    public static String getMD5String(String s) {
        if (s == null) {
            return null;
        }
        return getMD5String(s.getBytes());
    }

    public static String getMD5String(byte[] bytes) {
        messageDigest.update(bytes);
        byte[] md5Bytes = messageDigest.digest();
        char[] md5Chars = new char[md5Bytes.length * 2];
        int k = 0;
        for (byte md5Byte : md5Bytes) {
            md5Chars[k++] = hexDigits[md5Byte >>> 4 & 0xf];
            md5Chars[k++] = hexDigits[md5Byte & 0xf];
        }
        return new String(md5Chars);
    }

    public static boolean matches(String input, String md5) {
        if (input == null || md5 == null) {
            return false;
        }
        return md5.equals(getMD5String(input));
    }

    public static void main(String[] args) {
        String input = "Hello, World!";
        String md5 = getMD5String(input);
        System.out.println("MD5哈希值: " + md5);
        System.out.println("匹配结果: " + matches(input, md5));
    }
}