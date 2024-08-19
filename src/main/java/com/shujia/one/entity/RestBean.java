package com.shujia.one.entity;

import lombok.Data;
//封装信息类，封装向前端返回的信息
@Data
public class RestBean<T> {
    private int status;
    private String msg;
    private T data;
    public boolean success;
    private RestBean(int status, String msg, T data,boolean success) {
        this.status = status;
        this.msg = msg;
        this.data = data;
        this.success = success;
    }
    public static <T> RestBean<T> success() {
        return new RestBean<T>(200, null, null,true);
    }
    public static <T> RestBean<T> success(T data) {
        return new RestBean<T>(200,null, data, true);
    }
    public static <T> RestBean<T> success(String msg) {
        return new RestBean<T>(200,msg, null, true);
    }
    public static <T> RestBean<T> success(String msg, T data) {
        return new RestBean<T>(200,msg, data, true);
    }
    public static <T> RestBean<T> fail(int status) {
        return new RestBean<T>(status, null, null,false);
    }
    public static <T> RestBean<T> fail(int status, String msg) {
        return new RestBean<T>(status, msg, null,false);
    }
/*    public static <T> RestBean<T> success() {
        return new RestBean<T>(200, null, true);
    }
    public static <T> RestBean<T> success(T data) {
        return new RestBean<T>(200, data, true);
    }
    public static <T> RestBean<T> fail(int status) {
        return new RestBean<T>(status, null, false);
    }
    public static <T> RestBean<T> fail(int status, T data) {
        return new RestBean<T>(status, data, false);
    }*/
}
