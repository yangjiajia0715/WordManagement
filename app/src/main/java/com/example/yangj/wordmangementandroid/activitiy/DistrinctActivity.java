package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.common.ResultListInfo;
import com.example.yangj.wordmangementandroid.common.Word;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author yangjiajia
 * @date 2018/7/25
 */
public class DistrinctActivity extends BaseActivity {
    private static final String TAG = "DistrinctActivity";
    @BindView(R.id.btn_word_district_1)
    Button mBtnWordDistrict1;
    @BindView(R.id.btn_word_district_2)
    Button mBtnWordDistrict2;
    @BindView(R.id.btn_word_district_3)
    Button mBtnWordDistrict3;
    private List<Word> mListAllWordsRelease;

    public static void start(Context context) {
        Intent starter = new Intent(context, DistrinctActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distrinct);
        ButterKnife.bind(this);
    }


    List<Word> parseDistrictFile(String path) {
        if (TextUtils.isEmpty(path)) {
            showAlertDialog("请选择要解析的文件！");
            return null;
        }

        File file = new File(path);

        if (!file.exists() || !file.isFile()) {
            Toast.makeText(this, "文件不存在" + file.getName(), Toast.LENGTH_SHORT).show();
            return null;
        }

//        //检查文件格式！！！
//        boolean rightFormat = checkWordFileFormat(file);
//        if (!rightFormat) {
//            return null;
//        }

        List<Word> wordList = new ArrayList<>();
        BufferedReader bufferedReader = null;
        try {
            FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            int index = 0;
            //之前会校验文件格式，所以这里大胆使用
            while ((line = bufferedReader.readLine()) != null) {
                String lineStr = line.trim();
                index++;

                int indexOf = lineStr.indexOf(",");
                Word word = new Word();
                String engSpell = lineStr.substring(0, indexOf).trim();
                String chineseSpell = lineStr.substring(indexOf, lineStr.length()).trim();

                word.setEnglishSpell(engSpell);
                word.setChineseSpell(chineseSpell);
                Log.d(TAG, "loadFile: engSpell=" + engSpell + ",chineseSpell=" + chineseSpell);

                wordList.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return wordList;
    }

    @OnClick({R.id.btn_word_district_1, R.id.btn_word_district_2, R.id.btn_word_district_3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_word_district_1:
                distrinct();
                break;
            case R.id.btn_word_district_2:
                break;
            case R.id.btn_word_district_3:
                break;
            default:
                break;
        }
    }

    private void distrinct() {
        if (mListAllWordsRelease == null || mListAllWordsRelease.isEmpty()) {
            Toast.makeText(this, "列表为空", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = Environment.getExternalStorageDirectory();
        file = new File(file, "wordManagement");
        String path = file.getPath() + File.separator + "去重之前单词列表文件.txt";
        List<Word> newWordList = parseDistrictFile(path);
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

                    }
                });
    }
}
