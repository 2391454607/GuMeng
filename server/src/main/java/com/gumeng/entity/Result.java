package com.gumeng.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
  功能：
  作者：Z
  日期：2025/3/9 下午10:50
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code; //状态码
    private String msg; //提示信息
    private T data; //响应数据


    //快速返回操作成功的响应结果（带响应数据）
    public static <E> Result<E> success(E data) {
        return new Result<>(200, "操作成功", data);
    }

    //快速返回操作成功的响应结果（无响应数据）
    public static Result success() {
        return new Result(200, "操作成功", null);
    }

    public static Result error(String msg) {
        return new Result(1, msg, null);
    }

}
