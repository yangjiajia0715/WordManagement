package com.example.yangj.wordmangementandroid.common;

/**
 * ResultBeanInfo
 * Created by yangjiajia on 2017/4/28 0028.
 */

public class ResultBeanInfo<R> implements BaseInfo {
    private R data;
    private int code;
    private String message;

    public R getData() {
        return data;
    }

    public void setData(R data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
