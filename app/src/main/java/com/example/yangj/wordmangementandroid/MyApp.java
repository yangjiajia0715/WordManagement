package com.example.yangj.wordmangementandroid;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.example.yangj.wordmangementandroid.common.OssTokenInfo;
import com.example.yangj.wordmangementandroid.common.ResultBeanInfo;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by yangjiajia on 2018/6/19.
 */
public class MyApp extends Application {

    private static final String TAG = "MyApp";

    public OssTokenInfo getOssTokenInfo() {
        return mOssTokenInfo;
    }

    private OssTokenInfo mOssTokenInfo;

    public OSS getOss() {
        return oss;
    }

    private OSS oss = null;

    @Override
    public void onCreate() {
        super.onCreate();

        getOssToken();

        registerCallbacks();
    }

    private void getOssToken() {
        ApiClient.getInstance()
                .getOssToken()
                .subscribe(new Observer<ResultBeanInfo<OssTokenInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResultBeanInfo<OssTokenInfo> ossTokenInfoResultBeanInfo) {
                        mOssTokenInfo = ossTokenInfoResultBeanInfo.getData();
                        oss = initOSS(mOssTokenInfo);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(), "getOssToken error!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(getApplicationContext(), "oss init " + oss, Toast.LENGTH_SHORT).show();

                    }
                });

    }

    //初始化一个OssService用来上传下载
    public OSS initOSS(OssTokenInfo ossTokenInfo) {
        //如果希望直接使用accessKey来访问的时候，可以直接使用OSSPlainTextAKSKCredentialProvider来鉴权。
//        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);


        //使用自己的获取STSToken的类
//        OSSCredentialProvider credentialProvider = new STSGetter(stsServer);
        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider(ossTokenInfo.getAccessKeyId()
                , ossTokenInfo.getAccessKeySecret()
                , ossTokenInfo.getSecurityToken());

        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次

        OSS oss = new OSSClient(getApplicationContext(), ossTokenInfo.getEndpoint(), credentialProvider, conf);

//        return new OssService(oss, bucket, displayer);
        return oss;

    }


    /**
     * 测试用
     */
    private void registerCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.d(TAG, "MyApplication--onActivityCreated: " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d(TAG, "MyApplication--onActivityDestroyed: " + activity.getClass().getSimpleName());
            }
        });
    }

}
