package com.example.yangj.wordmangementandroid.util;

/**
 * Created by yangjiajia on 2018/6/19.
 */
public class UploadHelper {
    private static UploadHelper sUploadHelper;
    private UploadHelper() {
    }

    public UploadHelper getUploadHelper(){
        if (sUploadHelper == null) {
            sUploadHelper = new UploadHelper();
        }
        return sUploadHelper;
    }


}
