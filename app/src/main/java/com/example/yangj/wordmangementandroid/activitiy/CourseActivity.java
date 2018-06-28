package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.common.CourseInfo;
import com.example.yangj.wordmangementandroid.common.ResultListInfo;
import com.example.yangj.wordmangementandroid.common.Word;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * Created by yangjiajia on 2018/6/27.
 */
public class CourseActivity extends BaseActivity {
    private static final String TAG = "CourseActivity";
    @BindView(R.id.tv_check_course_result)
    TextView mTvCheckCourseResult;
    private List<Word> mListAllWordsRelease;
    private List<CourseInfo> mCourseInfosRelease;
    private StringBuilder mStringBuilder = new StringBuilder();
    private int lineNumber = 0;
    private int dayNumber = 0;

    public static void start(Context context) {
        Intent starter = new Intent(context, CourseActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        ButterKnife.bind(this);
        listAll();
        listAllCourse();
    }

    private void listAll() {
        showProgressDialog();
        ApiClient.getInstance()
                .listAll()
                .subscribe(new Observer<ResultListInfo<Word>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResultListInfo<Word> wordResultListInfo) {
                        mListAllWordsRelease = wordResultListInfo.getData();
//                        showProgressDialog("listAll size=" + mListAllWordsRelease.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: e " + e.getMessage());
//                        showProgressDialog("listAll onError=" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        hideProgressDialog();
                    }
                });
    }

    private void listAllCourse() {
        ApiClient.getInstance()
                .listAllCourse()
                .subscribe(new Observer<List<CourseInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<CourseInfo> courseInfos) {
                        mCourseInfosRelease = courseInfos;
                        Log.d(TAG, "onNext: " + courseInfos.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }


    @OnClick({R.id.btn_course_check_1, R.id.btn_course_check_2, R.id.btn_course_check_3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_course_check_1:
                checkUnexistWord();
                break;
            case R.id.btn_course_check_2:
                break;
            case R.id.btn_course_check_3:
                break;
        }
    }

    private void checkUnexistWord() {
        lineNumber = 0;
        mStringBuilder.setLength(0);

        if (mCourseInfosRelease == null || mCourseInfosRelease.isEmpty()) {
            showAlertDialog("No mCourseInfosRelease");
            return;
        }

        if (mListAllWordsRelease == null || mListAllWordsRelease.isEmpty()) {
            showAlertDialog("No mListAllWordsRelease");
            return;
        }

        Observable.fromIterable(mCourseInfosRelease)
                .map(new Function<CourseInfo, List<CourseInfo.PlansBean>>() {
                    @Override
                    public List<CourseInfo.PlansBean> apply(CourseInfo courseInfo) {
                        mStringBuilder.append("-------------------------------------------------");
                        mStringBuilder.append("\n");
                        mStringBuilder.append(courseInfo.getName());
                        mStringBuilder.append(" EditionId:");
                        mStringBuilder.append(courseInfo.getEditionId());
                        mStringBuilder.append("\n");
                        mStringBuilder.append("\n");
                        dayNumber = 0;
                        return courseInfo.getPlans();
                    }
                })
                .flatMap(new Function<List<CourseInfo.PlansBean>, ObservableSource<CourseInfo.PlansBean>>() {
                    @Override
                    public ObservableSource<CourseInfo.PlansBean> apply(List<CourseInfo.PlansBean> plansBeans) throws Exception {
                        return Observable.fromIterable(plansBeans);
                    }
                })
                .flatMap(new Function<CourseInfo.PlansBean, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(CourseInfo.PlansBean plansBean) {
                        dayNumber++;
                        mStringBuilder.append("--------第");
                        mStringBuilder.append(dayNumber);
                        mStringBuilder.append("天--------");
                        if (plansBean.getWords().size() != 3) {
                            mStringBuilder.append("不够三个单词！");
                        }
                        mStringBuilder.append("\n");
                        return Observable.fromIterable(plansBean.getWords());
                    }
                })
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer wordId) {
                        Word word = null;
                        for (Word item : mListAllWordsRelease) {
                            if (wordId == item.id) {
                                word = item;
                                break;
                            }
                        }
                        lineNumber++;

                        mStringBuilder.append(lineNumber);
                        mStringBuilder.append(" ");
                        if (word == null) {
                            mStringBuilder.append("单词不存在：wordId:");
                            mStringBuilder.append(wordId);
                            mStringBuilder.append("\n");
                        } else {
                            mStringBuilder.append(word.getEnglishSpell());
                            mStringBuilder.append("\n");
                        }
                        Log.d(TAG, "onNext: wordId=" + wordId);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: e=" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mStringBuilder.append("-----检查结束-------");
                        mStringBuilder.append("\n");
                        mTvCheckCourseResult.setText(mStringBuilder.toString());
                    }
                });


    }
}
