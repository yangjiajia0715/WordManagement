package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.common.AddLearningDailyPlan;
import com.example.yangj.wordmangementandroid.common.CourseInfo;
import com.example.yangj.wordmangementandroid.common.ResultListInfo;
import com.example.yangj.wordmangementandroid.common.Word;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * 排词
 * 格式要求：
 * 单词之间有空格用“=”代替
 * 单词必须存在
 * 三个单词
 * Created by yangjiajia on 2018/6/28.
 */
public class CourseLearnWordAddActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
    private static final int REQ_SELECT_LEARN_PLAN_FILE = 11;
    private static final String TAG = "CourseLearnWordAddActiv";
    private boolean ignoreTheSameWord = false;
    private int totalDays = 0;
    private int errorDays = 0;
    private List<String> questionNotEnough = new ArrayList<>();

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
    private List<Word> mListAllWordsRelease;
    private CourseInfo mCurCourseInfo;
    private int delIndex;
    private List<AddLearningDailyPlan> mLearningDailyPlanListFile;

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
        listAllWord();
        listAllCourse();
    }


    private void listAllWord() {
        ApiClient.getInstance()
                .listAll()
                .subscribe(new Observer<ResultListInfo<Word>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResultListInfo<Word> wordResultListInfo) {
                        mListAllWordsRelease = wordResultListInfo.getData();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: e " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
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
            mDatas.add(name + " editionId=" + editionId + ",courseId=" + courseInfo.getId());
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
//                new AlertDialog.Builder(this)
//                        .setMessage("确定全部删除？courseId:" + mCurCourseInfo.getId() + ",name=" + mCurCourseInfo.getName())
//                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                                deleteAll();
//                            }
//                        })
//                        .create()
//                        .show();
                break;
            case R.id.btn_learn_plan_3:
                int courseInfoId = mCurCourseInfo.getId();

//                if (courseInfoId == 3 || courseInfoId == 4
//                        || courseInfoId == 5
//                        || courseInfoId == 6) {
//                    showAlertDialog("返回！courseInfoId=" + courseInfoId);
//                    return;
//                }
                addLearnPlan(mLearningDailyPlanListFile);
                break;
        }
    }

    private void addLearnPlan(List<AddLearningDailyPlan> dailyPlans) {
        if (dailyPlans == null || dailyPlans.isEmpty()) {
            showAlertDialog("列表不能为空！");
            return;
        }

        totalDays = dailyPlans.size();
        boolean alreadyExist = false;
        StringBuilder oneStr = new StringBuilder();
        StringBuilder twoStr = new StringBuilder();
        List<CourseInfo.PlansBean> plans = mCurCourseInfo.getPlans();
        String someWord = "";

        int daysOrigin = 0;
        int daysNew = 0;
        for (CourseInfo.PlansBean plan : plans) {
            daysOrigin++;
            daysNew = 0;
            List<Integer> words = plan.getWords();
            oneStr.setLength(0);
            for (Integer wordId : words) {
                oneStr.append(wordId);
                oneStr.append("-");
            }

            for (AddLearningDailyPlan addLearningDailyPlan : dailyPlans) {
                daysNew++;
                List<Integer> integerList = addLearningDailyPlan.getWords();
                twoStr.setLength(0);

                for (Integer wordId : integerList) {
                    twoStr.append(wordId);
                    twoStr.append("-");
                }

                if (TextUtils.equals(oneStr, twoStr)) {
                    alreadyExist = true;
                    someWord = oneStr.toString();
                    break;
                }
            }

            if (alreadyExist) {
                break;
            }
        }

        if (alreadyExist && !ignoreTheSameWord) {
            new AlertDialog.Builder(this)
                    .setMessage("存在相同的排词！" + someWord
                            + "\n原数据：第" + daysOrigin + "天"
                            + "\n新数据：第" + daysNew + "天")
                    .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("仍然提交！", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ignoreTheSameWord = true;
                            Toast.makeText(CourseLearnWordAddActivity.this, "请再次点击提交！", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
            return;
        }

        showProgressDialog();

        Observable.fromIterable(dailyPlans)
                .concatMap(new Function<AddLearningDailyPlan, ObservableSource<ResponseBody>>() {
                    @Override
                    public ObservableSource<ResponseBody> apply(AddLearningDailyPlan dailyPlan) {
                        return ApiClient.getInstance().addLearningDailyPlan(dailyPlan);
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
                        try {
                            String string = responseBody.string();
                            JSONObject jsonObject = new JSONObject(string);
                            int code = jsonObject.optInt("code");
                            if (code == 10051) {
                                errorDays++;
                                questionNotEnough.add(jsonObject.optString("data"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressDialog();
                        errorDays++;
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                        hideProgressDialog();
                        StringBuilder sb = new StringBuilder();
                        sb.append("提交成功\n共");
                        sb.append(totalDays);
                        sb.append("天");
                        sb.append("\n");
                        sb.append("失败");
                        sb.append(errorDays);
                        sb.append("天");
                        sb.append("\n");
                        if (errorDays > 0 && !questionNotEnough.isEmpty()) {
                            sb.append("question不全的单词：");
                            sb.append("\n");
                            for (String s : questionNotEnough) {
                                sb.append(s);
                                sb.append("\n");
                            }
                        }
                        showAlertDialog(sb.toString());
                    }
                });
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
                mLearningDailyPlanListFile = parseLearnPlanFile(pathWord, mCurCourseInfo.getId(), mListAllWordsRelease);
                buildLearnPlanInfo(mLearningDailyPlanListFile);
                mLearnPlanPath = pathWord;
                mTvLearnPlanPath.setText("排词文件：" + fileWord.getName());
                break;
        }
    }

    public void buildLearnPlanInfo(List<AddLearningDailyPlan> dailyPlans) {
        StringBuilder stringBuilder = new StringBuilder();
        if (dailyPlans == null || dailyPlans.isEmpty()) {
            stringBuilder.append("解析失败！");
            mTvResult.setText(stringBuilder.toString());
            return;
        }

        int index = 0;
        for (AddLearningDailyPlan dailyPlan : dailyPlans) {
            if (index > 0 && index % 5 == 0) {
                stringBuilder.append("\n");
                stringBuilder.append("\n");
            }
            index++;
            int courseId = dailyPlan.getCourseId();
            List<Integer> words = dailyPlan.getWords();
            stringBuilder.append("courseId:");
            stringBuilder.append(courseId);
            stringBuilder.append(" 第");
            stringBuilder.append(index);
            stringBuilder.append("天: ");
            for (Integer id : words) {
                stringBuilder.append(id);
                stringBuilder.append(" ");
                Word wordFind = null;
                for (Word word : mListAllWordsRelease) {
                    if (word.id == id) {
                        wordFind = word;
                        break;
                    }
                }

                if (wordFind == null) {
                    stringBuilder.append("无");
                } else {
                    stringBuilder.append(wordFind.getEnglishSpell());
                }
                stringBuilder.append("；");
            }
            stringBuilder.append("\n");
        }
        mTvResult.setText(stringBuilder.toString());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mCurCourseInfo = mCourseInfosRelease.get(position);
        Log.d(TAG, "onItemSelected: name=" + mCurCourseInfo.getName());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "onNothingSelected: ");
    }
}
