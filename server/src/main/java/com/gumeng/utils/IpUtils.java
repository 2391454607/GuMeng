package com.gumeng.utils;

import com.github.jarod.qqwry.IPZone;
import com.github.jarod.qqwry.QQWry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * IP工具类，用于解析IP地址和获取归属地
 */
@Slf4j
@Component
public class IpUtils {

    private static QQWry qqWry;

//    初始化纯真ip库
    @Autowired
    public void setQqWry(QQWry qqWry) {
        IpUtils.qqWry = qqWry;
    }

    /**
     * 获取请求的真实IP地址
     *
     * @param request 请求对象
     * @return IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("x-forwarded-for");
        if (!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (!StringUtils.hasLength(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 对于通过多个代理的情况，第一个IP为客户端真实IP，多个IP按照','分割
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 获取IP归属地
     *
     * @param ip IP地址
     * @return 归属地信息
     */
    public static String getIpLocation(String ip) {
        // 检查IP库是否初始化
        if (qqWry == null) {
            return "未知";
        }
        
        // 处理内网IP和本地IP
        if (isInternalIp(ip)) {
            return "内网IP";
        }
        
        try {
            IPZone ipZone = qqWry.findIP(ip);
            if (ipZone == null) {
                return "未知";
            }
            
            String mainInfo = ipZone.getMainInfo();
            String subInfo = ipZone.getSubInfo();
            
            if (StringUtils.hasLength(subInfo) && !"未知".equals(subInfo)) {
                return mainInfo + " " + subInfo;
            }
            return mainInfo;
        } catch (Exception e) {
            log.error("获取IP归属地信息失败：{}", ip, e);
            return "未知";
        }
    }

    /**
     * 判断是否是内网IP
     *
     * @param ip IP地址
     * @return 是否是内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        
        // 检查是否是本地回环地址
        if ("127.0.0.1".equals(ip) || "localhost".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return true;
        }
        
        // 检查是否是IPv6地址
        if (ip.contains(":")) {
            return false;
        }
        
        // 拆分IP段
        String[] ipParts = ip.split("\\.");
        if (ipParts.length != 4) {
            return false;
        }
        
        // 检查是否属于内网IP段：10.x.x.x, 172.16.x.x-172.31.x.x, 192.168.x.x
        try {
            int firstOctet = Integer.parseInt(ipParts[0]);
            int secondOctet = Integer.parseInt(ipParts[1]);
            
            // 10.0.0.0 - 10.255.255.255
            if (firstOctet == 10) {
                return true;
            }
            
            // 172.16.0.0 - 172.31.255.255
            if (firstOctet == 172 && (secondOctet >= 16 && secondOctet <= 31)) {
                return true;
            }
            
            // 192.168.0.0 - 192.168.255.255
            return firstOctet == 192 && secondOctet == 168;
        } catch (NumberFormatException e) {
            return false;
        }
    }
} 