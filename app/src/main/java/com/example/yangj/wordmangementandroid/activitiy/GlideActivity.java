package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
    @BindView(R.id.iv_glide_1)
    ImageView mIvGlide1;
    @BindView(R.id.iv_glide_2)
    ImageView mIvGlide2;
    @BindView(R.id.iv_glide_3)
    ImageView mIvGlide3;
    private static final String TAG = "GlideActivity";

    String mUrl = "http://p7de8v2jr.bkt.clouddn.com/A-fengjing.jpg";
    String mUrl2 = "http://p7de8v2jr.bkt.clouddn.com/C-meishi.jpg";
    String mUrl3 = "http://p7de8v2jr.bkt.clouddn.com/G-meishi.jpg";
    String mUrl4 = "http://p7de8v2jr.bkt.clouddn.com/E-keji.jpg";

    public static void start(Context context) {
        Intent starter = new Intent(context, GlideActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide);
        ButterKnife.bind(this);
        ImageView.ScaleType scaleType = mIvGlide1.getScaleType();
        Log.d(TAG, "onCreate: scaleType=" + scaleType);
        Log.d(TAG, "onCreate: getExternalCacheDir=" + getExternalCacheDir().getPath());

    }

    @OnClick({R.id.btn_glide_1, R.id.btn_glide_2, R.id.btn_glide_3, R.id.btn_glide_4, R.id.btn_glide_5})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_glide_1:
                Glide.with(this)
                        .load(mUrl)
                        .into(mIvGlide1);
                break;
            case R.id.btn_glide_2:
                Glide.with(this)
                        .load(mUrl2)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mIvGlide2);
                break;
            case R.id.btn_glide_3:
                Glide.with(this)
                        .load(mUrl3)
                        .placeholder(R.drawable.loading)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mIvGlide3);
                break;
            case R.id.btn_glide_4:
                Glide.with(this)
                        .load(mUrl3)
                        .placeholder(R.drawable.loading)
//                        .sizeMultiplier(0.5f)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mIvGlide3);
                break;
            case R.id.btn_glide_5:
                Glide.with(this)
                        .load(mUrl3)
                        .placeholder(R.drawable.loading)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mIvGlide3);

//                String tempURl = null;
//                Glide.with(this)
////                        .load(mUrl3)
//                        .load(tempURl)
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
////                        .fallback(R.drawable.homework)//优先级：1
//                        .error(R.drawable.xueba)//优先级：2
//                        .placeholder(R.drawable.loading)//优先级：3
//                        .override(100 * 3, 100 * 3)
//                        .into(mIvGlide3);
                break;
            default:
                break;
        }
    }
}
