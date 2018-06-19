package com.example.yangj.wordmangementandroid.retrofit;

import com.example.yangj.wordmangementandroid.common.FileUploadInfo;
import com.example.yangj.wordmangementandroid.common.ResultBeanInfo;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * ApiServer
 * Created by yangjiajia on 2017/4/19 0019.
 */

public interface ApiServer {

    @GET("LoginServer/px.json?dataType=course_find_by_stu")
    Observable<ResponseBody> getMic(@Query("stu_id") String stuId);

    @GET("LoginServer/px.json?dataType=course_find_by_stu")
    Observable<Response> getMicResponse(@Query("stu_id") String stuId);

    @Multipart
    @POST("LoginServer/px/file/upload.json")
    Observable<ResultBeanInfo<FileUploadInfo>> uploadFile(@Part MultipartBody.Part part);


    @Multipart
    @POST("LoginServer/px/file/uploadForOcr.json")
    Observable<ResponseBody> uploadFirstFile1(@Part MultipartBody.Part part);

    @Multipart
    @POST("LoginServer/px/file/uploadForOcr.json")
    Observable<ResponseBody> uploadFirstFile(@Part RequestBody body);

}
