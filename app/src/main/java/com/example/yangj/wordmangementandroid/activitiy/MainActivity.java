package com.example.yangj.wordmangementandroid.activitiy;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.common.Word;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION = 262;
    @BindView(R.id.btn_load_file)
    Button mBtnLoadFile;
    @BindView(R.id.btn_update_word)
    Button mBtnUpdateWord;
    @BindView(R.id.btn_update_questions)
    Button mBtnUpdateQuestions;
    @BindView(R.id.sv_log)
    TextView mSvLog;
    private List<Word> mWordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        requestPermission();
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(this
                , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
                , PERMISSION);
    }

    @OnClick({R.id.btn_load_file, R.id.btn_update_word, R.id.btn_update_questions})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_load_file:
//                String path = Environment.getExternalStorageDirectory().getPath()
//                        + File.separator + "wordManagement" + File.separator + "two_up.txt";
                String path = Environment.getExternalStorageDirectory().getPath()
                        + File.separator + "wordManagement" + File.separator + "word_list_test.txt";
                mWordList = loadFile(path);
                mBtnLoadFile.setEnabled(false);
                break;
            case R.id.btn_update_word:

                if (mWordList != null && !mWordList.isEmpty()) {
                    //flatMap 无序
                    //concatMap 有序

                    Observable.fromIterable(mWordList)
                            .concatMap(new Function<Word, ObservableSource<ResponseBody>>() {

                                @Override
                                public ObservableSource<ResponseBody> apply(Word word) throws Exception {
                                    Log.d(TAG, "apply: word=" + word.getEnglishSpell());
                                    return ApiClient.getInstance()
                                            .createWord(word);
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

                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                } else {
                    mBtnLoadFile.setEnabled(true);
                    Toast.makeText(this, "单词列表为空！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_update_questions:
                listAll();
                break;
        }
    }

    private void createWord() {
        Word word = new Word();
        word.setChineseSpell("Test1");
        word.setEnglishSpell("Test1");
        word.setMeaning("Test1 释义");
        word.setExampleSentence("Test1 sentence");

        ApiClient.getInstance()
                .createWord(word)
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void listAll() {
        ApiClient.getInstance()
                .listAll()
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: ");
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            Log.d(TAG, "onNext: responseBody=" + responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: e=" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    private List<Word> loadFile(String path) {
        List<Word> wordList = new ArrayList<>();
        File file = new File(path);
        Log.d(TAG, "loadFile: path=" + path);
        if (!file.exists()) {
            Toast.makeText(this, "文件不存在" + file.getName(), Toast.LENGTH_SHORT).show();
            return wordList;
        }

        BufferedReader bufferedReader = null;
        try {
            FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line = null;
            List<String> lines = new ArrayList<>();
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
//                line = line.trim();
                Log.d(TAG, "loadFile: line=" + line);

                if (!TextUtils.isEmpty(line)) {
                    String twoChar = line.substring(0, 2);
                    if (TextUtils.isDigitsOnly(twoChar)) {
                        lines.clear();
                    }
                    lines.add(line);
                } else {
                    //空行 为分隔符
                    if (lines.size() == 6) {
                        Word word = new Word();
                        for (int i = 0; i < lines.size(); i++) {
                            String lineStr = lines.get(i).trim();//trim
                            switch (i) {
                                case 0:
                                    word.id = Integer.parseInt(lineStr.substring(0, 2));
                                    int index0 = lineStr.lastIndexOf(" ");
                                    Log.d(TAG, "loadFile: lineStr=" + lineStr.length() + "--" + lineStr);
                                    Log.d(TAG, "loadFile: lineStr index0=" + index0);
                                    word.setEnglishSpell(lineStr.substring(2, index0).trim());//trim
                                    word.setMeaning(lineStr.substring(index0, lineStr.length()).trim());//trim
                                    word.setChineseSpell(word.getMeaning());

                                    stringBuilder.append(word.id);
                                    stringBuilder.append(" ");
                                    stringBuilder.append(word.getEnglishSpell());
                                    stringBuilder.append("---");
                                    stringBuilder.append(word.getChineseSpell());
                                    stringBuilder.append("\n");
                                    break;
                                case 1:
                                    int index1 = lineStr.lastIndexOf("例句0：");
                                    if (index1 >= 0) {
                                        word.setExampleSentence(lineStr.substring("例句0：".length(), lineStr.length()));

                                        stringBuilder.append(word.getExampleSentence());
                                        stringBuilder.append("\n");
                                    }
                                    break;
                                case 2:
                                    int index2 = lineStr.lastIndexOf("中文干扰项1：");
                                    if (index2 > 0) {
                                        String errorMeaning = lineStr.substring("中文干扰项1：".length(), lineStr.length());

                                        stringBuilder.append("errorMeaning=");
                                        stringBuilder.append(errorMeaning);
                                        stringBuilder.append("\n");
                                    }
                                    break;
                                case 3:
                                    int index3 = lineStr.lastIndexOf("分割正确项2：");
                                    if (index3 >= 0) {
                                        String lineRight = lineStr.substring("分割正确项2：".length(), lineStr.length());
                                        String[] splitRight = lineRight.split(" ");

                                        stringBuilder.append(lineRight);
                                        stringBuilder.append("\n");
                                    }
                                    break;
                                case 4:
                                    String engWrong = lineStr.substring("英文干扰项3：".length(), lineStr.length());
                                    stringBuilder.append(engWrong);
                                    stringBuilder.append("\n");
                                    break;
                                case 5:
                                    String splitError = lineStr.substring("分割干扰项4：".length(), lineStr.length());

                                    stringBuilder.append(splitError);
                                    stringBuilder.append("\n");
                                    break;
                            }
                        }
                        wordList.add(word);
                    } else {
                        Log.e(TAG, "loadFile: error 没有空格 ");
                        stringBuilder.append("-----error 没有空格------");
                        stringBuilder.append("\n");
                    }
                }

            }
            mSvLog.setText(stringBuilder.toString());
            Toast.makeText(this, "读取成功！", Toast.LENGTH_SHORT).show();
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

    /**
     * 老的文档平铺
     */
    private List<Word> loadFileFlat() {
        List<Word> wordList = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory().getPath()
                + File.separator + "wordManagement" + File.separator + "two_up.txt";
        File file = new File(path);
        File fileOutPut = new File(file.getParentFile(), "two_up_output.txt");
        Log.d(TAG, "loadFile: path=" + path);
        Log.d(TAG, "loadFile: file=" + file);
        if (!file.exists()) {
            Toast.makeText(this, "文件不存在" + file.getName(), Toast.LENGTH_SHORT).show();
            return wordList;
        }
        BufferedReader bufferedReader = null;
        StringBuilder stringBuffer = new StringBuilder();
        try {
            FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line = null;

            fileOutPut.createNewFile();
            FileWriter fileWriter = new FileWriter(fileOutPut);
            int lineNum = 0;
            while ((line = bufferedReader.readLine()) != null) {
//                line = line.trim();
                Log.d(TAG, "loadFile: line=" + line);


                String twoChar = line.substring(0, 2);
                if (TextUtils.isDigitsOnly(twoChar)) {
                    fileWriter.write(line);
                    fileWriter.write("\n");
                    stringBuffer.append(line);
                    stringBuffer.append("\n");
                } else {
                    int indexOf = line.indexOf("英文干扰项");
                    if (indexOf == -1) {
                        indexOf = line.indexOf("分割干扰项");
                    }
                    if (indexOf != -1) {
                        String substring = line.substring(0, indexOf);
                        String substring1 = line.substring(indexOf);

                        fileWriter.write(substring);
                        fileWriter.write("\n");

                        stringBuffer.append(substring);
                        stringBuffer.append("\n");

                        fileWriter.write(substring1);
                        fileWriter.write("\n");

                        stringBuffer.append(substring1);
                        stringBuffer.append("\n");
                    }

                }
                fileWriter.flush();
            }
            mSvLog.setText(stringBuffer.toString());
            Toast.makeText(this, "读取成功！", Toast.LENGTH_SHORT).show();
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
}
