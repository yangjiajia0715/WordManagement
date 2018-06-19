package com.example.yangj.wordmangementandroid.retrofit;

import com.example.yangj.wordmangementandroid.common.Word;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * 启步接口，调试用，连接本地数据库
 * Created by yangjiajia on 2018/5/17.
 */
public interface ApiQiBu {

    @GET("/word/listAll")
    Observable<ResponseBody> listAll();

    @POST("/word")
    Observable<ResponseBody> create(@Body Word word);

}
