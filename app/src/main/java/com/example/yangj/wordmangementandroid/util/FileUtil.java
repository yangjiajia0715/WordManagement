package com.example.yangj.wordmangementandroid.util;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author yangjiajia
 * @date 2018/7/26
 */
public class FileUtil {
   static SimpleDateFormat mSdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

    static String BASE_PATH = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "wordManagement"+ File.separator;

    public static String getDistinctOutFilePath(){
        return BASE_PATH + "单词去重"+ mSdf.format(new Date()) + ".txt";
    }

}
