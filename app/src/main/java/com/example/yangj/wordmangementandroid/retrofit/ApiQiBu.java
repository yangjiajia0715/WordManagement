package com.example.yangj.wordmangementandroid.retrofit;

import com.example.yangj.wordmangementandroid.common.OssTokenInfo;
import com.example.yangj.wordmangementandroid.common.Question;
import com.example.yangj.wordmangementandroid.common.ResultBeanInfo;
import com.example.yangj.wordmangementandroid.common.ResultListInfo;
import com.example.yangj.wordmangementandroid.common.Word;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * 启步接口，调试用，连接本地数据库
 * Created by yangjiajia on 2018/5/17.
 */
public interface ApiQiBu {

    @GET("/word/listAll")
    Observable<ResultListInfo<Word>> listAll();

    @GET("/question/listAll")
    Observable<ResultListInfo<Question>> listAllQuestions();

    @POST("/word")
    Observable<ResultBeanInfo<Word>> create(@Body Word word);

    @PUT("/word")
    Observable<ResponseBody> updateWord(@Body Word word);

    @POST("/question")
    Observable<ResultBeanInfo<Question>> createQuestion(@Body Question question);

    @PUT("/question")
    Observable<ResponseBody> updateQuestion(@Body Question question);

    @GET("/oss/getSTSToken")
    Observable<ResultBeanInfo<OssTokenInfo>> getSTSToken();

    /**
     * 课程及排词信息
     */
    @GET("/course/listAll")
    Observable<ResponseBody> listAllCourse();

}
