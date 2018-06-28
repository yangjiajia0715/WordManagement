package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.common.CourseInfo;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by yangjiajia on 2018/6/28.
 */
public class CourseLearnWordAddActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
    private static final int REQ_SELECT_LEARN_PLAN_FILE = 11;
    private static final String TAG = "CourseLearnWordAddActiv";

    @BindView(R.id.spiner)
    Spinner mSpiner;
    @BindView(R.id.tv_learn_plan_path)
    TextView mTvLearnPlanPath;
    @BindView(R.id.btn_learn_plan_1)
    Button mBtnLearnPlan1;
    @BindView(R.id.btn_learn_plan_2)
    Button mBtnLearnPlan2;
    @BindView(R.id.btn_learn_plan_3)
    Button mBtnLearnPlan3;
    @BindView(R.id.tv_result)
    TextView mTvResult;
    private String mLearnPlanPath;
    private List<CourseInfo> mCourseInfosRelease;
    private ArrayAdapter<String> mAdapter;
    private List<String> mDatas;

    public static void start(Context context) {
        Intent starter = new Intent(context, CourseLearnWordAddActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_learn);
        ButterKnife.bind(this);
        setTitle("排词");
        listAllCourse();
    }

    @Override
    void initData() {//onComplete 中调用
        if (mCourseInfosRelease == null) {
            Toast.makeText(this, "NO mCourseInfosRelease!", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatas = new ArrayList<>();
        for (CourseInfo courseInfo : mCourseInfosRelease) {
            String name = courseInfo.getName();
            int editionId = courseInfo.getEditionId();
            mDatas.add(name + " editionId=" + editionId);
        }

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDatas);
        mSpiner.setAdapter(mAdapter);
        mSpiner.setOnItemSelectedListener(this);
    }

    @Override
    void initView() {

    }

    private void listAllCourse() {
        showProgressDialog();

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
                        initData();
                        hideProgressDialog();
                    }
                });
    }

    @OnClick({R.id.btn_learn_plan_1, R.id.btn_learn_plan_2, R.id.btn_learn_plan_3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_learn_plan_1:
                FileSelectorActivity.startForResult(this, REQ_SELECT_LEARN_PLAN_FILE);
                break;
            case R.id.btn_learn_plan_2:
                break;
            case R.id.btn_learn_plan_3:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "未选择", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (requestCode) {
            case REQ_SELECT_LEARN_PLAN_FILE:
                String pathWord = data.getStringExtra(Intent.EXTRA_TEXT);
                File fileWord = new File(pathWord);
                if (!fileWord.exists() || !fileWord.isFile()) {
                    showProgressDialog("文件不存在，或者不是文件：" + fileWord.getName());
                    return;
                }
                mLearnPlanPath = pathWord;
                mTvLearnPlanPath.setText("排词文件：" + fileWord.getName());
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        CourseInfo courseInfo = mCourseInfosRelease.get(position);
        Log.d(TAG, "onItemSelected: name=" + courseInfo.getName());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "onNothingSelected: ");
    }
}
