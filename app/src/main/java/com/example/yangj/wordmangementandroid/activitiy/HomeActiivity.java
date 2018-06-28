package com.example.yangj.wordmangementandroid.activitiy;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.yangj.wordmangementandroid.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yangjiajia on 2018/6/21.
 */
public class HomeActiivity extends AppCompatActivity {
    @BindView(R.id.btn_word)
    Button mBtnWord;
    @BindView(R.id.btn_question_check)
    Button mBtnQuestionCheck;
    @BindView(R.id.btn_upload_question)
    Button mBtnUploadQuestion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_word
            , R.id.btn_question_check
            , R.id.btn_upload_question
            , R.id.btn_update_word
            , R.id.btn_course_add
            , R.id.btn_course_check
            , R.id.btn_word_check})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_word:
                MainActivity.start(this);
                break;
            case R.id.btn_question_check:
                QustionCheckActivity.start(this);
                break;
            case R.id.btn_upload_question:
//                UpdateWordActivity.start(this);
                break;
            case R.id.btn_update_word:
                UpdateWordActivity.start(this);
                break;
            case R.id.btn_word_check:
                WordCheckActivity.start(this);
                break;
            case R.id.btn_course_add:
                CourseLearnWordAddActivity.start(this);
                break;
            case R.id.btn_course_check:
                CourseCheckActivity.start(this);
                break;
        }
    }
}
