package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.example.yangj.wordmangementandroid.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author yangjiajia
 * @date 2018/7/27
 */
public class GlideActivity extends BaseActivity {

    @BindView(R.id.btn_glide_1)
    Button mBtnGlide1;
    @BindView(R.id.btn_glide_2)
    Button mBtnGlide2;
    @BindView(R.id.btn_glide_3)
    Button mBtnGlide3;
    @BindView(R.id.btn_glide_4)
    Button mBtnGlide4;
    @BindView(R.id.btn_glide_5)
    Button mBtnGlide5;

    public static void start(Context context) {
        Intent starter = new Intent(context, GlideActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_glide_1, R.id.btn_glide_2, R.id.btn_glide_3, R.id.btn_glide_4, R.id.btn_glide_5})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_glide_1:
                break;
            case R.id.btn_glide_2:
                break;
            case R.id.btn_glide_3:
                break;
            case R.id.btn_glide_4:
                break;
            case R.id.btn_glide_5:
                break;
            default:
                break;
        }
    }
}
