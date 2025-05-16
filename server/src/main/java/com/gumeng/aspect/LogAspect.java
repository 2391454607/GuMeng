package com.gumeng.aspect;

import com.gumeng.annotation.LogOperation;
import com.gumeng.domain.SysLog;
import com.gumeng.security.CustomUserDetails;
import com.gumeng.service.sys.SysLogService;
import com.gumeng.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 日志切面，用于自动记录系统操作日志
 */
@Aspect
@Component
public class LogAspect {

    @Autowired
    private SysLogService sysLogService;

    /**
     * 定义切点 - 拦截所有带有LogOperation注解的方法
     */
    @Pointcut("@annotation(com.gumeng.annotation.LogOperation)")
    public void logPointCut() {
    }

    /**
     * 处理完成后记录日志
     */
    @AfterReturning(value = "logPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, null, true);
    }

    /**
     * 处理异常后记录日志
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, false);
    }

    /**
     * 处理日志记录
     */
    private void handleLog(final JoinPoint joinPoint, final Exception e, boolean success) {
        try {
            // 获取当前请求对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();

            // 获取注解信息
            LogOperation logOperation = getLogOperation(joinPoint);
            if (logOperation == null) {
                return;
            }

            // 获取当前登录用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Integer userId = null;
            String username = "匿名用户";
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                userId = userDetails.getId();
                username = userDetails.getUsername();
            }

            // 获取IP地址和归属地
            String ip = IpUtils.getIpAddress(request);
            String location = IpUtils.getIpLocation(ip);

            // 创建日志对象
            SysLog sysLog = new SysLog();
            sysLog.setUserId(userId);
            sysLog.setUsername(username);
            sysLog.setModule(logOperation.module());
            sysLog.setOperation(logOperation.operation());
            sysLog.setIp(ip);
            sysLog.setLocation(location);
            sysLog.setStatus(success ? 1 : 0);
            sysLog.setCreateTime(LocalDateTime.now());
            sysLog.setIsDelete(0);

            // 保存日志
            sysLogService.save(sysLog);
        } catch (Exception ex) {
            // 记录日志出错，仅打印异常，不影响主业务
            ex.printStackTrace();
        }
    }

    /**
     * 获取方法上的注解
     */
    private LogOperation getLogOperation(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(LogOperation.class);
    }
}