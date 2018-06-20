package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.yangj.wordmangementandroid.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yangjiajia on 2018/6/20.
 */
public class WordListInfoActivity extends AppCompatActivity {
    @BindView(R.id.tv_log)
    TextView mTvLog;
    private static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "wordManagement" + File.separator;

    public static void start(Context context, String logInfo) {
        Intent starter = new Intent(context, WordListInfoActivity.class);
        starter.putExtra("logInfo", logInfo);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_info);
        ButterKnife.bind(this);
        String logInfo = getIntent().getStringExtra("logInfo");
        mTvLog.setText(logInfo);

    }
}
