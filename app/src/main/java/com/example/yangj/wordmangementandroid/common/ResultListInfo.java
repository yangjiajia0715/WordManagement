package com.example.yangj.wordmangementandroid.common;

import java.io.Serializable;
import java.util.List;

/**
 * ResultListInfo
 * Created by yangjiajia on 2017/4/28 0028.
 */

public class ResultListInfo<T> implements Serializable {
    private List<T> data;
    private int code;
    private String message;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
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
