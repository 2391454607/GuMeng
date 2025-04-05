package com.gumeng.exception;

import com.gumeng.code.HttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 功能：全局异常处理
 * 作者：Z
 * 日期：2025/3/10 下午2:17
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理注册参数验证异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HttpResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return HttpResponse.error(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public HttpResponse handleException(Exception e){
        e.printStackTrace();
        return HttpResponse.error(StringUtils.hasLength(e.getMessage())? e.getMessage() : "操作失败");
//        return Result.error("输入参数不合法");
    }
}
