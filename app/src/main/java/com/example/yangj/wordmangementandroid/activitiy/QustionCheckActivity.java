package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.common.Question;
import com.example.yangj.wordmangementandroid.common.ResultListInfo;
import com.example.yangj.wordmangementandroid.common.Word;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;

/**
 * 练习检查
 * Created by yangjiajia on 2018/6/21.
 */
public class QustionCheckActivity extends BaseActivity {
    private static final String TAG = "QustionCheckActivity";
    @BindView(R.id.btn_begin_check)
    Button mBtnBeginCheck;
    @BindView(R.id.tv_check_question_result)
    TextView mTvCheckQuestionResult;
    private List<Question> mQuestionListRelease;
    private StringBuilder mStringBuilder = new StringBuilder();
    private int mWordId1Count;
    List<GroupedObservable<Integer, Question>> groups = new ArrayList<>();
    private List<Word> mWordList;

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
        listAll();
    }

    private void listAll() {
        ApiClient.getInstance()
                .listAll()
                .subscribe(new Observer<ResultListInfo<Word>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResultListInfo<Word> wordResultListInfo) {
                        mWordList = wordResultListInfo.getData();
//                        showProgressDialog("listAll size=" + mListAllWordsRelease.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: e " + e.getMessage());
//                        showProgressDialog("listAll onError=" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void listAllQuestions() {
        showProgressDialog();
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
                        hideProgressDialog();
                        Log.d(TAG, "listAllQuestions--onNext e: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        hideProgressDialog();
                        mTvCheckQuestionResult.setText("共：" + mQuestionListRelease.size());
                        Log.d(TAG, "listAllQuestions--onComplete");
                    }
                });
    }

    @OnClick(R.id.btn_begin_check)
    public void onViewClicked() {
        if (mQuestionListRelease == null) {
            return;
        }
        groups.clear();
        showProgressDialog();

        Observable.fromIterable(mQuestionListRelease)
                .groupBy(new Function<Question, Integer>() {
                    @Override
                    public Integer apply(Question question) {
                        Log.d(TAG, "begin check apply: getWordId=" + question.getWordId());
                        return question.getWordId();
                    }
                })
                .subscribe(new Observer<GroupedObservable<Integer, Question>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(GroupedObservable<Integer, Question> groupedObservable) {
                        groups.add(groupedObservable);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        hideProgressDialog();

                        listGroups();
                    }
                });
    }

    private void listGroups() {
        Log.d(TAG, "listGroups--size: " + groups.size());

        //2200  5*200=1000ms
        Observable.interval(5, TimeUnit.MILLISECONDS)
                .map(new Function<Long, GroupedObservable<Integer, Question>>() {
                    @Override
                    public GroupedObservable<Integer, Question> apply(Long aLong) {
                        Log.d(TAG, "listGroups--aLong: " + aLong);
                        return groups.get(aLong.intValue());
                    }
                })
                .take(groups.size())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GroupedObservable<Integer, Question>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(GroupedObservable<Integer, Question> groupedObservable) {
                        groupedObservable.subscribe(new QuestionGroupObserver<>());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    class QuestionGroupObserver<T> implements Observer<T> {
        List<T> mQuestions = new ArrayList<>();

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(T t) {
            Question question = (Question) t;
            mQuestions.add(t);
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {
            Log.d(TAG, "listGroups--onNext: " + mQuestions.size());
            if (mQuestions.size() != 10) {
                Question question = (Question) mQuestions.get(0);

                mStringBuilder.append("wordId:");
                mStringBuilder.append(question.getWordId());
                mStringBuilder.append(" ");
                Word word = null;
                if (mWordList != null) {
                    for (Word item : mWordList) {
                        if (item.id == question.getWordId()) {
                            word = item;
                            break;
                        }
                    }
                }
                if (word != null) {
                    mStringBuilder.append(word.getEnglishSpell());
                }
                mStringBuilder.append(" ");
                if (mQuestions.size() > 10) {
                    mStringBuilder.append("多余10个练习");
                } else {
                    mStringBuilder.append("少于10个练习");
                }
//                mStringBuilder.append(question.getType());
                mStringBuilder.append("\n");
                mStringBuilder.append("\n");
            }
            mTvCheckQuestionResult.setText(mStringBuilder.toString());
//            Log.d(TAG, "listGroups--onNext: " + question.getWordId());
//            mStringBuilder.append(question.getWordId());
//            mStringBuilder.append(" ");
//            mStringBuilder.append(question.getType());
//            mStringBuilder.append("\n");
        }
    }
}
