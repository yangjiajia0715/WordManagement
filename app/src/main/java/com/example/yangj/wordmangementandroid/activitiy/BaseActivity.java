package com.example.yangj.wordmangementandroid.activitiy;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by yangjiajia on 2018/6/22.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    void showDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("加载中，请稍后");
        }
        mProgressDialog.show();
    }

    void hideDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }
}
