package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.common.Question;
import com.example.yangj.wordmangementandroid.common.QuestionTitle;
import com.example.yangj.wordmangementandroid.common.ResultListInfo;
import com.example.yangj.wordmangementandroid.common.Word;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;
import com.example.yangj.wordmangementandroid.util.QuestionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

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
    @BindView(R.id.btn_check_question_update)
    Button mBtnCheckQuestionUpdate;
    private List<Question> mQuestionListRelease;
    private StringBuilder mStringBuilder = new StringBuilder();
    private int mWordId1Count;
    List<GroupedObservable<Integer, Question>> groups = new ArrayList<>();
    private List<Word> mWordList;
    private List<Question> mNeedUpdateWords = new ArrayList<>();
    int needUpdateQustionsSuccessNumber = 0;
    int lineNumbers = 0;

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
                        mTvCheckQuestionResult.setText("共：" + mQuestionListRelease.size() + "道练习");
                        Log.d(TAG, "listAllQuestions--onComplete");
                        hideProgressDialog();
                    }
                });
    }

    private void beginCheck() {
        if (mQuestionListRelease == null || mQuestionListRelease.isEmpty()) {
            showAlertDialog("No mQuestionListRelease!");
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

        lineNumbers = 0;
        //2200  5*200=1000ms
        Observable.interval(10, TimeUnit.MILLISECONDS)
                .map(new Function<Long, GroupedObservable<Integer, Question>>() {
                    @Override
                    public GroupedObservable<Integer, Question> apply(Long aLong) {
                        Log.d(TAG, "listGroups--aLong: " + aLong);
                        return groups.get(aLong.intValue());
                    }
                })
                .take(groups.size())
//                .subscribeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
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


    @OnClick({R.id.btn_begin_check, R.id.btn_check_question_update})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_begin_check:
                beginCheck();
                break;
            case R.id.btn_check_question_update:
                updateQustionsApi(mNeedUpdateWords);
                break;
        }
    }

    class QuestionGroupObserver<T> implements Observer<T> {
        List<T> mQuestionsGroup = new ArrayList<>();

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(T t) {
            Question question = (Question) t;
            QuestionTitle title = question.getTitle();

            Word word = null;
            if (mWordList != null) {
                for (Word item : mWordList) {
                    if (item.id == question.getWordId()) {
                        word = item;
                        break;
                    }
                }
            }

            if (word == null) {
                mStringBuilder.append("没有对应的单词! wordId:");
                mStringBuilder.append(question.getWordId());
                mStringBuilder.append(" questionId:");
                mStringBuilder.append(question.getId());
                mStringBuilder.append("\n");
            }

            switch (question.getType()) {
                case CHOOSE_WORD_BY_LISTEN_SENTENCE:
                    if (title == null) {
                        lineNumbers++;
                        mStringBuilder.append(lineNumbers);
                        mStringBuilder.append(" 听文选词No QuestionTitle！wordId:");
                        mStringBuilder.append(question.getWordId());
                        mStringBuilder.append(" qId:");
                        mStringBuilder.append(question.getId());
                        mStringBuilder.append("\n");
                        checkAndCompleteQuestionTitle(question, word);
                    } else {
                        String titleTitle = title.getTitle();
                        if (!TextUtils.isEmpty(titleTitle)) {
                            if (titleTitle.contains("’s ") || titleTitle.contains("’re ")) {
                                lineNumbers++;
                                mStringBuilder.append(lineNumbers);
                                titleTitle = titleTitle.replaceAll("’s ", "'s ");
                                titleTitle = titleTitle.replaceAll("’re ", "'re ");
                                title.setTitle(titleTitle);
                                question.setTitle(title);
                                mNeedUpdateWords.add(question);///////////add
                                mStringBuilder.append("听文选词 含有中文 ' 已纠正,请提交！wordId:");
                                mStringBuilder.append(question.getWordId());
                                mStringBuilder.append(" qId:");
                                mStringBuilder.append(question.getId());
                                mStringBuilder.append("\n");
                                mStringBuilder.append("纠正为：");
                                mStringBuilder.append(titleTitle);
                                mStringBuilder.append("\n");
                                mStringBuilder.append("\n");
                            }
                        } else {
                            lineNumbers++;
                            mStringBuilder.append(lineNumbers);
                            mStringBuilder.append("听文选词No QuestionTitle！");
                            mStringBuilder.append(" qId:");
                            mStringBuilder.append(question.getId());
                            mStringBuilder.append("\n");
                            checkAndCompleteQuestionTitle(question, word);
                        }
                    }
                    break;
                case CHOOSE_WORD_BY_READ_SENTENCE://看句选词
                    if (title == null) {
                        lineNumbers++;
                        mStringBuilder.append(lineNumbers);
                        mStringBuilder.append(" 看句选词 No QuestionTitle！wordId:");
                        mStringBuilder.append(question.getWordId());
                        mStringBuilder.append(" qId:");
                        mStringBuilder.append(question.getId());
                        mStringBuilder.append("\n");
                        checkAndCompleteQuestionTitle(question, word);
                    } else {
                        String titleTitle = title.getTitle();
                        if (!TextUtils.isEmpty(titleTitle)) {
                            if (titleTitle.contains("’s ") || titleTitle.contains("’re ")) {
                                lineNumbers++;
                                mStringBuilder.append(lineNumbers);
                                titleTitle = titleTitle.replaceAll("’s ", "'s ");
                                titleTitle = titleTitle.replaceAll("’re ", "'re ");
                                title.setTitle(titleTitle);
                                question.setTitle(title);
                                mNeedUpdateWords.add(question);//////////add
                                mStringBuilder.append(" 看句选词 含有中文 ' 已纠正,请提交！wordId:");
                                mStringBuilder.append(question.getWordId());
                                mStringBuilder.append("\n");
                                mStringBuilder.append("纠正为：");
                                mStringBuilder.append(titleTitle);
                                mStringBuilder.append("\n");
                                mStringBuilder.append("\n");
                            }
                        } else {
                            lineNumbers++;
                            mStringBuilder.append(lineNumbers);
                            mStringBuilder.append(" 看句选词No QuestionTitle！");
                            mStringBuilder.append(" qId:");
                            mStringBuilder.append(question.getId());
                            mStringBuilder.append("\n");
                            checkAndCompleteQuestionTitle(question, word);
                        }
                    }
                    break;
            }
            mQuestionsGroup.add(t);
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {//检查每个单词的练习是否完整，只查不改
            Log.d(TAG, "listGroups--onNext: " + mQuestionsGroup.size());
            if (mQuestionsGroup.size() == 0) {
                mStringBuilder.append("mQuestionsGroup size = 0");
                mStringBuilder.append("\n");
            } else if (mQuestionsGroup.size() != 10) {
                Question question = (Question) mQuestionsGroup.get(0);

                mStringBuilder.append("wordId:");
                mStringBuilder.append(question.getWordId());
                mStringBuilder.append(" ");
                for (Word word : mWordList) {
                    if (word.id == question.getWordId()) {
                        mStringBuilder.append(word.getEnglishSpell());
                        break;
                    }
                }
//                mStringBuilder.append(" ");
//                mStringBuilder.append(QuestionType2Chinese.getChinese(question.getType()));
                mStringBuilder.append("\n");

//                List<String> options = question.getOptions();
//                if (options != null) {
//                    mStringBuilder.append("选项：");
//                    for (String s : options) {
//                        mStringBuilder.append(s);
//                        mStringBuilder.append(" ");
//                    }
//                    mStringBuilder.append("\n");
//                }

//                mStringBuilder.append(" ");
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
                } else {
                    mStringBuilder.append("没有对应的单词");
                }

                mStringBuilder.append(" ");
                if (mQuestionsGroup.size() > 10) {
                    mStringBuilder.append("多余10个练习");
                } else {
                    mStringBuilder.append("少于10个练习");
                }
                mStringBuilder.append("\n");
            }

//            if (!mQuestionsGroup.isEmpty()) {
//                Question question = (Question) mQuestionsGroup.get(0);
//                mStringBuilder.append("=============单词项结束========wordId:");
//                mStringBuilder.append(question.getWordId());
//                mStringBuilder.append("\n");
//            }

            String toString = mStringBuilder.toString();
//            if (TextUtils.isEmpty(toString)) {
////                mTvCheckQuestionResult.setText(mQuestionListRelease.size() + "个练习完整");
//            } else {
//            }
            mTvCheckQuestionResult.setText(toString);
        }
    }

    private void checkAndCompleteQuestionTitle(Question question, Word word) {
        if (word != null) {
            QuestionTitle questionTitle = QuestionHelper.createQuestionTitle(word);
            if (questionTitle == null || TextUtils.isEmpty(questionTitle.getTitle())) {
                mStringBuilder.append(" 未纠正！！！");
                mStringBuilder.append("\n");
            } else {
                question.setTitle(questionTitle);
                mNeedUpdateWords.add(question);//////////add
                mStringBuilder.append(" 已纠正为：");
                mStringBuilder.append(questionTitle.getTitle());
                mStringBuilder.append("\n");
            }

        } else {
            mStringBuilder.append("没有对应的单词! wordId:");
            mStringBuilder.append(question.getWordId());
            mStringBuilder.append("\n");
        }
    }

    private void updateQustionsApi(List<Question> needUpdateQustions) {
        if (needUpdateQustions == null || needUpdateQustions.isEmpty()) {
            showAlertDialog("No needUpdateQustions!");
            return;
        }

        Observable.fromIterable(needUpdateQustions)
//                .take(2)
                .concatMap(new Function<Question, ObservableSource<ResponseBody>>() {
                    @Override
                    public ObservableSource<ResponseBody> apply(Question question) {
                        Log.d(TAG, "onNext: getWordId=" + question.getWordId());
                        return ApiClient.getInstance()
                                .updateQuestion(question);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        Log.d(TAG, "onNext: ");
                        needUpdateQustionsSuccessNumber++;
                    }

                    @Override
                    public void onError(Throwable e) {
                        showAlertDialog("更新练习失败");
                    }

                    @Override
                    public void onComplete() {
                        showAlertDialog("更新练习成功 \n共：" + needUpdateQustionsSuccessNumber);
                    }
                });

    }
}
