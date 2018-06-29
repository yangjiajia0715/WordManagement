package com.example.yangj.wordmangementandroid.retrofit;

import com.alibaba.fastjson.JSON;
import com.example.yangj.wordmangementandroid.BuildConfig;
import com.example.yangj.wordmangementandroid.common.AddLearningDailyPlan;
import com.example.yangj.wordmangementandroid.common.CourseInfo;
import com.example.yangj.wordmangementandroid.common.DeleteLearningDailyPlan;
import com.example.yangj.wordmangementandroid.common.FileUploadInfo;
import com.example.yangj.wordmangementandroid.common.OssTokenInfo;
import com.example.yangj.wordmangementandroid.common.Question;
import com.example.yangj.wordmangementandroid.common.ResultBeanInfo;
import com.example.yangj.wordmangementandroid.common.ResultListInfo;
import com.example.yangj.wordmangementandroid.common.Word;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient
 * Created by yangjiajia on 2017/4/25 0025.
 */

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final int CONNECT_TIME_OUT = 20;//连接超时 20s
    private OkHttpClient okHttpClient;
    private final Retrofit retrofit;

    private static ApiClient INSTANCE;
    private final ApiServer apiServer;
    private final ApiQiBu mApiQiBu;

    public static ApiClient getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ApiClient();
        return INSTANCE;
    }

    private ApiClient() {
        OkHttpClient.Builder okhttpBuilder = new OkHttpClient.Builder()
//                .addInterceptor(new TestIntercept())
//                .addInterceptor(new Test2Intercept())
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            // 2018-5-17 21:17:38
//            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            okHttpClient.networkInterceptors().add(httpLoggingInterceptor);//异常

            //2018-5-17 21:18:34 之后
            //日志拦截器
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            okhttpBuilder.addInterceptor(logging);
        }
        okHttpClient = okhttpBuilder.build();

        retrofit = new Retrofit.Builder()
                .callFactory(okHttpClient)
                .baseUrl(BuildConfig.IP_ADDRESS)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiServer = retrofit.create(ApiServer.class);
        mApiQiBu = retrofit.create(ApiQiBu.class);
    }

    public Observable<ResultBeanInfo<FileUploadInfo>> uploadFile(@android.support.annotation.NonNull File file) {

        RequestBody body = MultipartBody.create(MediaType.parse("image/png"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), body);

        Retrofit retrofitFileUpload = retrofit.newBuilder()
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiServer apiServer1 = retrofitFileUpload.create(ApiServer.class);

        return apiServer1.uploadFile(part)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())//doOnndext
                ;

    }

    public Observable<ResultListInfo<Word>> listAll() {
        return mApiQiBu.listAll()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResultListInfo<Question>> listAllQuestions() {
        return mApiQiBu.listAllQuestions()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResultBeanInfo<Word>> createWord(Word word) {
        return mApiQiBu.create(word);
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseBody> updateWord(Word word) {
        return mApiQiBu.updateWord(word);
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResultBeanInfo<Question>> createQuestion(Question question) {
        return mApiQiBu.createQuestion(question);
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseBody> updateQuestion(Question question) {
        return mApiQiBu.updateQuestion(question);
    }

    public Observable<ResultBeanInfo<OssTokenInfo>> getOssToken() {
        return mApiQiBu.getSTSToken()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<CourseInfo>> listAllCourse() {
        return mApiQiBu.listAllCourse()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<ResponseBody, List<CourseInfo>>() {
                    @Override
                    public List<CourseInfo> apply(ResponseBody responseBody) throws Exception {
                        String string = responseBody.string();
                        JSONObject jsonObject = new JSONObject(string);
//                        String message = jsonObject.optString("message");
//                        JSONObject dataObject = jsonObject.optJSONObject("data");
                        JSONArray jsonArray = jsonObject.optJSONArray("data");
//                        JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                        if (jsonArray != null) {
                            return JSON.parseArray(jsonArray.toString(), CourseInfo.class);
                        }
                        return new ArrayList<>();
                    }
                });
    }

    public Observable<ResponseBody> addLearningDailyPlan(AddLearningDailyPlan dailyPlan) {
        return mApiQiBu.addLearningDailyPlan(dailyPlan);
    }

    public Observable<ResponseBody> deleteLearningDailyPlan(DeleteLearningDailyPlan deleteLearningDailyPlan) {
        return mApiQiBu.deleteLearningDailyPlan(deleteLearningDailyPlan);
    }

}
