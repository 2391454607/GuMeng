package com.gumeng.code;

import lombok.Data;

/**
 * 功能：请求返回信息
 * 作者：Z
 * 日期：2025/4/2 下午11:11
 */
@Data
public class HttpResponse {

    private Integer code; //请求的状态码

    private String msg;   //返回状态信息

    private Object data;  //返回给前端的数据

    /**
     * 请求成功
     */
    public static HttpResponse success() {
        HttpResponse r = new HttpResponse();
        r.setCode(200);
        r.setMsg("success");
        return r;
    }

    public static HttpResponse success(Object data) {
        HttpResponse r = success();
        r.setData(data);
        return r;
    }

    public static HttpResponse error(){
        HttpResponse r = new HttpResponse();
        r.setCode(500);
        return r;
    }

    public static HttpResponse error(String msg){
        HttpResponse r = error();
        r.setMsg(msg);
        return r;
    }
}
