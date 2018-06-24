package com.example.yangj.wordmangementandroid.activitiy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.example.yangj.wordmangementandroid.MyApp;
import com.example.yangj.wordmangementandroid.common.OssTokenInfo;

import java.io.File;

/**
 * Created by yangjiajia on 2018/6/22.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private OSS mOss;
    private OssTokenInfo mOssTokenInfo;
    private ProgressDialog mProgressDialog;
    private static final String TAG = "BaseActivity";
    static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "wordManagement" + File.separator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp app = (MyApp) getApplication();
        mOssTokenInfo = app.getOssTokenInfo();
        mOss = app.getOss();
    }

    void initView(){

    }

    void initData(){

    }

    void showProgressDialog(){
        showProgressDialog(null);
    }

    void showProgressDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            if (TextUtils.isEmpty(msg)) {
                mProgressDialog.setMessage("加载中，请稍后");
            } else {
                mProgressDialog.setMessage(msg);
            }
        }
        mProgressDialog.show();
    }

    void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }

    void showAlertDialog(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    void uploadFile(String path, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {
        if (mOssTokenInfo == null || mOss == null) {
            showProgressDialog("mOssTokenInfo or mOss不存在");
            return;
        }

        final File file = new File(path);
        if (!file.exists()) {
            showProgressDialog("file 不存在 name=" + file.getName() + "\nwordFilePath=" + file.getPath());
            return;
        }

        String objectKey = null;
        if (file.getName().endsWith(".mp3")) {
            objectKey = "courseware/audio/" + file.getName();
        } else {
            objectKey = "courseware/image/" + file.getName();
        }
        Log.d(TAG, "uploadFile: objectKey=" + objectKey);

        PutObjectRequest putObjectRequest = new PutObjectRequest(mOssTokenInfo.getBucket()
                , objectKey, path);

        mOss.asyncPutObject(putObjectRequest, completedCallback);

        try {
            mOss.putObject(putObjectRequest);
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }
}
