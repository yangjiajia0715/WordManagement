package com.example.yangj.wordmangementandroid.common;

import java.io.Serializable;
import java.util.List;

/**
 * ResultListInfo
 * Created by yangjiajia on 2017/4/28 0028.
 */

public class ResultListInfo<T> implements Serializable {
    private List<T> datas;
    private int flag;
    private String msg;

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
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
