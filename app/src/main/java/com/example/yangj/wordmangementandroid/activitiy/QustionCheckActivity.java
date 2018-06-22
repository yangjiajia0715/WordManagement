package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.common.Question;
import com.example.yangj.wordmangementandroid.common.ResultListInfo;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 练习检查
 * Created by yangjiajia on 2018/6/21.
 */
public class QustionCheckActivity extends AppCompatActivity {
    private static final String TAG = "QustionCheckActivity";
    @BindView(R.id.btn_begin_check)
    Button mBtnBeginCheck;
    @BindView(R.id.tv_check_question_result)
    TextView mTvCheckQuestionResult;
    private List<Question> mQuestionListRelease;

    public static void start(Context context) {
        Intent starter = new Intent(context, QustionCheckActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_check);
        ButterKnife.bind(this);
        listAllQuestions();
    }

    private void listAllQuestions() {
        ApiClient.getInstance()
                .listAllQuestions()
                .subscribe(new Observer<ResultListInfo<Question>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResultListInfo<Question> questionResultListInfo) {
                        mQuestionListRelease = questionResultListInfo.getData();
                        Log.d(TAG, "listAllQuestions--onNext: size=" + mQuestionListRelease.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "listAllQuestions--onNext e: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "listAllQuestions--onComplete");
                        Toast.makeText(QustionCheckActivity.this, "onComplete", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @OnClick(R.id.btn_begin_check)
    public void onViewClicked() {
        if (mQuestionListRelease == null) {
            return;
        }

//        Observable.fromIterable(mQuestionListRelease)
//                .groupBy(new Function<Question, Integer>() {
//                    @Override
//                    public Integer apply(Question question) throws Exception {
//                        return question.getWordId();
//                    }
//                })
//                .subscribe(new Observer<GroupedObservable<Integer, Question>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(GroupedObservable<Integer, Question> integerQuestionGroupedObservable) {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });

        StringBuilder stringBuilder = new StringBuilder();
        List<Question> questionList = new ArrayList<>();
        //SparseArray !!!
//        HashMap<Integer, String> hashMap = new HashMap<>();
        TreeMap<Integer, String> treeMap = new TreeMap<>();
        for (Question question : mQuestionListRelease) {
            String type = treeMap.get(question.getWordId());
            if (TextUtils.isEmpty(type)) {
                type = question.getType().toString();
            }else {
                type += ","+question.getType().toString();
            }
            treeMap.put(question.getWordId(), type);
        }

        Set<Map.Entry<Integer, String>> entrySet = treeMap.entrySet();
        for (Map.Entry<Integer, String> entry : entrySet) {
            Integer wordId = entry.getKey();
            String types = entry.getValue();
            int size = types.split(",").length;
            stringBuilder.append(" 单词：wordId=");
            stringBuilder.append(wordId);
            if (size > 10) {
                stringBuilder.append("多余10个单词---------------");
                stringBuilder.append("\n\n");
            } else if (size == 10) {

            } else {
                stringBuilder.append(" 缺少");
                stringBuilder.append(10 - size);
                stringBuilder.append("种类型");
                stringBuilder.append("\n\n");
            }
        }
        mTvCheckQuestionResult.setText(stringBuilder.toString());
    }
}
