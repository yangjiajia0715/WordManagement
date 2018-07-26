package com.example.yangj.wordmangementandroid.activitiy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.common.ResultListInfo;
import com.example.yangj.wordmangementandroid.common.Word;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;
import com.example.yangj.wordmangementandroid.util.FileUtil;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
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
public class DistinctActivity extends BaseActivity {
    private static final String TAG = "DistinctActivity";
    private static final int REQ_SELECT_DISTRNCT_FILE = 110;
    @BindView(R.id.btn_word_district_1)
    Button mBtnWordDistrict1;
    @BindView(R.id.btn_word_district_2)
    Button mBtnWordDistrict2;
    @BindView(R.id.btn_word_district_3)
    Button mBtnWordDistrict3;
    private List<Word> mListAllWordsRelease;

    public static void start(Context context) {
        Intent starter = new Intent(context, DistinctActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distrinct);
        ButterKnife.bind(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
        listAll();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                String chineseSpell = lineStr.substring(indexOf + 1, lineStr.length()).trim();

                word.setEnglishSpell(engSpell);
                word.setChineseSpell(chineseSpell);
                Log.d(TAG, "loadFile: engSpell=" + engSpell + "  chineseSpell=" + chineseSpell);

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

                break;
            case R.id.btn_word_district_2:

                break;
            case R.id.btn_word_district_3:

                break;
            default:
                break;
        }
    }

    private void distinct(List<Word> newWordList) {

        if (mListAllWordsRelease == null || mListAllWordsRelease.isEmpty()) {
            Toast.makeText(this, "已有单词列表为空", Toast.LENGTH_SHORT).show();
            showAlertDialog("已有单词列表为空");
            return;
        }

        if (newWordList == null || newWordList.isEmpty()) {
            showAlertDialog("解析列表为空");
            return;
        }

        List<Word> distinctList = new ArrayList<>();

        for (Word wordNew : newWordList) {
            boolean exist = false;
            for (Word wordOrigin : mListAllWordsRelease) {
                //单词拼写相同
                if (TextUtils.equals(wordNew.getEnglishSpell().toLowerCase(), wordOrigin.getEnglishSpell().toLowerCase())) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                distinctList.add(wordNew);
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Word word : distinctList) {
            stringBuilder.append(word.getEnglishSpell());
            stringBuilder.append(" ");
            stringBuilder.append(word.getChineseSpell());
            stringBuilder.append("\n");
        }

        File outFile = new File(FileUtil.getDistinctOutFilePath());
        try {
            FileUtils.write(outFile, stringBuilder.toString(), Charset.forName("UTF-8"), false);
            showAlertDialog("去重文件已保存，fileName:" + outFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
            showAlertDialog("去重文件保存失败，e:" + e.getMessage());
        }
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
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressDialog();
                        Log.d(TAG, "onError: e " + e.getMessage());
                        showAlertDialog("获取单词列表失败！");
                    }

                    @Override
                    public void onComplete() {
                        hideProgressDialog();
                        FileSelectorActivity.startForResult(DistinctActivity.this, REQ_SELECT_DISTRNCT_FILE);
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
            case REQ_SELECT_DISTRNCT_FILE:
                String pathWord = data.getStringExtra(Intent.EXTRA_TEXT);
                File fileWord = new File(pathWord);
                if (!fileWord.exists() || !fileWord.isFile()) {
                    showProgressDialog("文件不存在，或者不是文件：" + fileWord.getName());
                    return;
                }
                List<Word> wordList = parseDistrictFile(fileWord.getPath());
                distinct(wordList);
                break;
            default:
                break;
        }
    }
}
