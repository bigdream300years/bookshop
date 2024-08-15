package com.shujia.one.entity;

import lombok.Data;

@Data
public class RestBean<T> {
    private int status;
    private T msg;
    //private T data;
    public boolean success;
    private RestBean(int status, T msg, boolean success) {
        this.status = status;
        this.msg = msg;
        this.success = success;
    }
    public static <T> RestBean<T> success() {
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
    }
}
