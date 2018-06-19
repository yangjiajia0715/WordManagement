package com.example.yangj.wordmangementandroid.common;

/**
 * ResultBeanInfo
 * Created by yangjiajia on 2017/4/28 0028.
 */

public class ResultBeanInfo<R> implements BaseInfo {
    private R data;
    private int flag;
    private String msg;

    public R getData() {
        return data;
    }

    public void setData(R data) {
        this.data = data;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
