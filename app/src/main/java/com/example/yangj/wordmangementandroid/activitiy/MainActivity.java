package com.example.yangj.wordmangementandroid.activitiy;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.example.yangj.wordmangementandroid.MyApp;
import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.common.OssTokenInfo;
import com.example.yangj.wordmangementandroid.common.Question;
import com.example.yangj.wordmangementandroid.common.ResultBeanInfo;
import com.example.yangj.wordmangementandroid.common.ResultListInfo;
import com.example.yangj.wordmangementandroid.common.Word;
import com.example.yangj.wordmangementandroid.common.WordLoad;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;
import com.example.yangj.wordmangementandroid.util.QuestionType2Chinese;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION = 262;
    @BindView(R.id.btn_load_file)
    Button mBtnLoadFile;
    @BindView(R.id.btn_upload_word)
    Button mBtnUpdateWord;
    @BindView(R.id.btn_upload_questions)
    Button mBtnUpdateQuestions;
    @BindView(R.id.sv_log)
    TextView mSvLog;
    @BindView(R.id.btn_upload_audio)
    Button mBtnUploadAudio;
    @BindView(R.id.btn_upload_word_image)
    Button mBtnUploadWordImage;
    @BindView(R.id.btn_show_word_info)
    Button mBtnShowWordInfo;
    //解析文件获取单词列表
    private List<Word> mWordListFile;
    //解析文件获取的同时保留练习信息，点击上传练习时再整合成Qustion
    private List<WordLoad> mWordLoadList = new ArrayList<>();
    private static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "wordManagement" + File.separator;
    //    String wordPath = BASE_PATH + "word_list_test.txt";
    String wordPath = BASE_PATH + "一下1-80.txt";
    private OSS mOss;
    private OssTokenInfo mOssTokenInfo;
    private StringBuilder logStringBuilder = new StringBuilder();
    private SimpleDateFormat mSdf = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());
    private int updateWordSkipNumber = 0;
    private int updateWordTotalNumber = 0;
    private int updateWordFailedNumber = 0;

    private int uploadAudioSkipNumber = 0;
    private int uploadAudioTotalNumber = 0;
    private int uploadAudioFailedNumber = 0;

    private int uploadQuestionTotalNumber = 0;
    private int uploadQuestionSkipedNumber = 0;

    private List<Word> mListAllWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        requestPermission();
        listAll();
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(this
                , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
                , PERMISSION);
    }

    @Nullable
    private void updateFile(String path) {
        if (mOssTokenInfo == null || mOss == null) {
            showDialog("mOssTokenInfo or mOss不存在");
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            showDialog("file 不存在 name=" + file.getName() + "\nwordPath=" + file.getPath());
            return;
        }

        if (mWordListFile == null || mWordListFile.isEmpty()) {
            showDialog("请先解析文档，获取wordList");
            return;
        }

//        UUID uuid = UUID.randomUUID();
        String objectKey = null;
        if (file.getName().endsWith(".mp3")) {
            objectKey = "courseware/audio/" + file.getName();
        } else {
            objectKey = "courseware/image/" + file.getName();
        }
        Log.d(TAG, "updateFile: objectKey=" + objectKey);

        PutObjectRequest putObjectRequest = new PutObjectRequest(mOssTokenInfo.getBucket()
                , objectKey, path);

        mOss.asyncPutObject(putObjectRequest, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                String objectKey = request.getObjectKey();
                String url = mOssTokenInfo.getCdnEndpoint() + "/" + objectKey;
                String fileName = new File(objectKey).getName();
                String suffix = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
                fileName = fileName.substring(0, fileName.indexOf("."));//eg. cow, cow1
                if ("mp3".equals(suffix)) {//mp3
                    if (mWordListFile != null) {
                        boolean find = false;
                        for (Word word : mWordListFile) {
                            if (TextUtils.equals(word.getEnglishSpell(), fileName)) {
                                find = true;
                                word.setEnglishPronunciation(url);
                                break;
                            } else if (TextUtils.equals(word.getEnglishSpell() + 1, fileName)) {
                                find = true;
                                word.setExampleSentenceAudio(url);
                                break;
                            }
                        }
                        if (find) {
                            Log.d(TAG, "updateFile--onSuccess-find: objectKey=" + objectKey);
                        } else {
                            Log.e(TAG, "updateFile--onSuccess-mWordListFile-not-find: objectKey=" + objectKey);
                        }

                    }
                } else {//image
                    if (mWordListFile != null) {
                        for (Word word : mWordListFile) {
//                            word.setImage(url);
//                            word.setRectangleImage(url);
                        }
                    }
                }

//                request.get
//                 onSuccess: courseware/audio/cow.mp3,fileName=cow
//                Log.d(TAG, "onSuccess: " + request.getObjectKey() + ",fileName=" + fileName);
//                 onSuccess: getETag=FA86230E1A440867E04CD4E84FBDDEF5
//                 onSuccess: getRequestId=5B29BDC1ACB2DB2870772C99
//                Log.d(TAG, "onSuccess: getETag=" + result.getETag());
//                Log.d(TAG, "onSuccess: getRequestId=" + result.getRequestId());
//                Log.d(TAG, "onSuccess: request=" + request);
//                Log.d(TAG, "onSuccess: result=" + result);
//                Set<Map.Entry<String, String>> entries = request.getCallbackParam().entrySet();
//                for (Map.Entry<String, String> entry : entries) {
//                    Log.d(TAG, "onSuccess callback: getKey=" + entry.getKey() + ",getValue=" + entry.getValue());
//                }
//                Map<String, String> responseHeader = result.getResponseHeader();
//
//                Set<Map.Entry<String, String>> entries1 = responseHeader.entrySet();
//                for (Map.Entry<String, String> stringEntry : entries1) {
//                    Log.d(TAG, "onSuccess header: getKey=" + stringEntry.getKey() + ",getValue=" + stringEntry.getValue());
//
//                }
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                Log.d(TAG, "onFailure: " + request.getObjectKey());
            }
        });

        try {
            mOss.putObject(putObjectRequest);
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    public void showDialog(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .create()
                .show();
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
                        mListAllWords = wordResultListInfo.getData();
                        showDialog("listAll size=" + mListAllWords.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        showDialog("listAll onError=" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_reset:
                mBtnLoadFile.setEnabled(true);
                mBtnUploadAudio.setEnabled(true);
                mBtnUploadWordImage.setEnabled(true);
                mBtnUpdateWord.setEnabled(true);
                mBtnUpdateQuestions.setEnabled(true);
                Toast.makeText(this, "已重置！", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.option_show_wordload:
                int index = 0;
                StringBuilder stringBuilder = new StringBuilder();
                for (WordLoad wordLoad : mWordLoadList) {
                    stringBuilder.append(index);
                    stringBuilder.append(" ");
                    stringBuilder.append(wordLoad.word);
                    stringBuilder.append("\n");
                    stringBuilder.append(wordLoad.wordEnglishWrong);
                    stringBuilder.append("\n");
                    stringBuilder.append(wordLoad.wordChineseWrong);
                    stringBuilder.append("\n");

                    for (String rightOption : wordLoad.rightOptions) {
                        stringBuilder.append(rightOption);
                        stringBuilder.append("--");
                    }
                    stringBuilder.append("\n");
                    for (String s : wordLoad.wrongOption1) {
                        stringBuilder.append(s);
                        stringBuilder.append("--");
                    }
                    stringBuilder.append("\n");
                    for (String s : wordLoad.wrongOption2) {
                        stringBuilder.append(s);
                        stringBuilder.append("--");
                    }
                    stringBuilder.append("\n");
                    stringBuilder.append("\n");

                    index++;
                }
                WordListInfoActivity.start(this, stringBuilder.toString());
                return true;
            case R.id.option_show_questions:
                StringBuilder strBuilderQ = new StringBuilder();
                List<Question> qustions = createQustions();
                if (qustions == null || qustions.isEmpty()) {
                    Toast.makeText(this, "No qustions!", Toast.LENGTH_SHORT).show();
                    return true;
                }

                int lastWordId = -1;
                int indexQ = 0;
                for (Question qustion : qustions) {
                    int wordId = qustion.getWordId();
                    if (lastWordId != -1) {
                        if (lastWordId != wordId) {
                            strBuilderQ.append("------------------index=");
                            strBuilderQ.append(indexQ);
                            indexQ++;
                        }
                        strBuilderQ.append("\n");
                    }
                    lastWordId = wordId;

                    strBuilderQ.append(qustion.getWordId());
                    strBuilderQ.append("\n");
                    //to chinese
                    strBuilderQ.append(QuestionType2Chinese.getChinese(qustion.getType()));
                    strBuilderQ.append("\n");
                    List<String> options = qustion.getOptions();
                    for (String option : options) {
                        strBuilderQ.append(option);
                        strBuilderQ.append("--");
                    }
                    strBuilderQ.append("\n");
                    List<Integer> answersIndex = qustion.getAnswersIndex();
                    for (Integer integer : answersIndex) {
                        strBuilderQ.append(integer);
                        strBuilderQ.append("--");
                    }
                    strBuilderQ.append("\n");
                }
                WordListInfoActivity.start(this, strBuilderQ.toString());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<Word> loadFile(String path) {
        mWordLoadList.clear();
        List<Word> wordList = new ArrayList<>();
        File file = new File(path);
        Log.d(TAG, "loadFile: wordPath=" + path);
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
                    //第一行 开始
                    if (TextUtils.isDigitsOnly(twoChar)) {
                        Word word = new Word();
                        WordLoad wordLoad = new WordLoad();
                        //空行 为分隔符
                        if (lines.size() == 6) {
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

                                        wordLoad.word = word.getEnglishSpell();
                                        stringBuilder.append("\n");
                                        stringBuilder.append(word.id);
                                        stringBuilder.append(" ");
                                        stringBuilder.append(word.getEnglishSpell());
                                        stringBuilder.append("---");
                                        stringBuilder.append(word.getChineseSpell());
                                        stringBuilder.append("\n");
                                        break;
                                    case 1://例句0
                                        int index1 = lineStr.lastIndexOf("例句0：");
                                        if (index1 >= 0) {
                                            word.setExampleSentence(lineStr.substring("例句0：".length(), lineStr.length()).trim());
                                            stringBuilder.append(word.getExampleSentence());
                                            stringBuilder.append("\n");
                                        } else {
                                            word.setExampleSentence(lineStr.trim());
                                            stringBuilder.append(word.getExampleSentence());
                                            stringBuilder.append("\n");
                                        }
                                        break;
                                    case 2://中文干扰项1
                                        String errorMeaning = lineStr.substring("中文干扰项1：".length(), lineStr.length());
                                        wordLoad.wordChineseWrong = errorMeaning;
//                                        stringBuilder.append("errorMeaning=");
                                        stringBuilder.append(errorMeaning);
                                        stringBuilder.append("\n");
                                        break;
                                    case 3://英文干扰项3
                                        String engWrong = lineStr.substring("英文干扰项3：".length(), lineStr.length()).trim();
                                        wordLoad.wordEnglishWrong = engWrong;
                                        stringBuilder.append(engWrong);
                                        stringBuilder.append("\n");
                                        break;
                                    case 4://分割正确项2
                                        int index3 = lineStr.lastIndexOf("分割正确项2：");
                                        if (index3 >= 0) {
                                            String lineRight = lineStr.substring("分割正确项2：".length(), lineStr.length()).trim();

                                            String[] split = lineRight.split(" ");
                                            List<String> rightOptions = new ArrayList<>();
                                            for (String s : split) {
                                                s = s.trim();
                                                if (!TextUtils.isEmpty(s)) {
                                                    rightOptions.add(s);
                                                }
                                            }
                                            wordLoad.rightOptions = rightOptions;
                                            stringBuilder.append(lineRight);
                                            stringBuilder.append("\n");
                                        }
                                        break;
                                    case 5://分割干扰项4
                                        String splitError = lineStr.substring("分割干扰项4：".length(), lineStr.length()).trim();
                                        String[] split = splitError.split(" ");

                                        List<String> wrongOptions1 = new ArrayList<>();
                                        List<String> wrongOptions2 = new ArrayList<>();

                                        for (String s : split) {
                                            s = s.trim();
                                            if (!TextUtils.isEmpty(s.trim())) {
                                                if (wrongOptions1.size() < 2) {
                                                    wrongOptions1.add(s);
                                                } else {
                                                    wrongOptions2.add(s);
                                                }
                                            }
                                        }

                                        wordLoad.wrongOption1 = wrongOptions1;
                                        wordLoad.wrongOption2 = wrongOptions2;

                                        stringBuilder.append(splitError);
                                        stringBuilder.append("\n");
                                        break;
                                }
                            }
                            mWordLoadList.add(wordLoad);
                            wordList.add(word);
                        } else {
                            Log.e(TAG, "loadFile: error 没有空格 id=" + word.id);
                            stringBuilder.append("-----error 没有空格------id=");
                            stringBuilder.append(word.id);
                            stringBuilder.append("  getEnglishSpell=");
                            stringBuilder.append(word.getEnglishSpell());
                            stringBuilder.append("\n");
                        }
                        //else

                        lines.clear();
                    }
                    lines.add(line);
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
        Log.d(TAG, "loadFile: wordPath=" + path);
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

    @OnClick({R.id.btn_load_file, R.id.btn_upload_audio, R.id.btn_upload_word_image,
            R.id.btn_upload_word, R.id.btn_upload_questions, R.id.btn_show_word_info})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_load_file:
                mWordListFile = loadFile(wordPath);
                mBtnLoadFile.setEnabled(false);
                break;
            case R.id.btn_upload_audio:
                String audioDir = BASE_PATH + "单词小超人 一下 音频32-87";
                if (checkWordList()) {
                    uploadAudios(audioDir);
                }
                break;
            case R.id.btn_upload_word_image:
                break;
            case R.id.btn_upload_word:
                if (mListAllWords == null || mListAllWords.isEmpty()) {
                    Toast.makeText(this, "No mListAllWords!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (checkWordList()) {
                    uploadWordList();
                } else {
                    mBtnLoadFile.setEnabled(true);
                }
                break;
            case R.id.btn_upload_questions:
                if (checkWordList()) {
                    List<Question> questionList = createQustions();
                    uploadQustions(questionList);
                }
                break;
            case R.id.btn_show_word_info:
                if (checkWordList()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < mWordListFile.size(); i++) {
                        Word word = mWordListFile.get(i);
                        word.id = i;
                        stringBuilder.append(word.toString());
                        stringBuilder.append("\n\n");
                    }
                    WordListInfoActivity.start(this, stringBuilder.toString());
                }
                break;
        }
    }

    private List<Question> createQustions() {
        if (mListAllWords == null || mListAllWords.isEmpty()) {
            showDialog("No ListAllWords!");
            return null;
        }

        List<Question> questionList = new ArrayList<>();
        for (int i = 0; i < mWordLoadList.size(); i++) {
            WordLoad wordLoad = mWordLoadList.get(i);
            for (int j = mListAllWords.size() - 1; j >= 0; j--) {
                Word wordRelease = mListAllWords.get(j);
                //每个单词10道题，
                if (TextUtils.equals(wordLoad.word, wordRelease.getEnglishSpell())) {
                    //CHOOSE_WORD_BY_LISTEN_SENTENCE：听文选词
                    //SPELL_WORD_BY_READ_IMAGE 看图拼写
                    //SPELL_WORD_BY_LISTEN_WORD 听词拼写
                    //1 2，1 3，1 4，2 3，2 4
                    Question questionSpellRead = new Question();
                    questionSpellRead.setType(Question.Type.SPELL_WORD_BY_READ_IMAGE);
                    questionSpellRead.setWordId(wordRelease.id);

                    List<String> rightOptions = wordLoad.rightOptions;
                    List<String> wrongOption1 = wordLoad.wrongOption1;
                    List<String> wrongOption2 = wordLoad.wrongOption2;
                    if (rightOptions == null || rightOptions.size() != 2) {
                        break;
                    }
                    if (wrongOption1 == null || wrongOption1.size() != 2) {
                        break;
                    }
                    if (wrongOption2 == null || wrongOption2.size() != 2) {
                        break;
                    }

                    //看图拼写
                    List<Integer> answerSpell1 = new ArrayList<>();
                    List<String> optionsSpell1 = new ArrayList<>();
                    questionSpell(i, rightOptions, wrongOption1, answerSpell1, optionsSpell1);//
                    questionSpellRead.setAnswersIndex(answerSpell1);
                    questionSpellRead.setOptions(optionsSpell1);

                    //听词拼写
                    Question questionSpellListen = new Question();
                    questionSpellListen.setType(Question.Type.SPELL_WORD_BY_LISTEN_WORD);
                    questionSpellListen.setWordId(wordRelease.id);
                    List<Integer> answerSpell2 = new ArrayList<>();
                    List<String> optionsSpell2 = new ArrayList<>();
                    questionSpell(i * 3, rightOptions, wrongOption2, answerSpell2, optionsSpell2);//
                    questionSpellListen.setAnswersIndex(answerSpell2);
                    questionSpellListen.setOptions(optionsSpell2);

                    questionList.add(questionSpellRead);
                    questionList.add(questionSpellListen);

                    break;
                }
            }
        }

        return questionList;
    }

    private void questionSpell(int i, List<String> rightOptions, List<String> wrongOption, List<Integer> answerIndex1, List<String> options1) {
        switch (i % 8) {//前五种都是顺序：1 2，1 3，1 4，2 3，2 4
            case 0://1 2，1 3，1 4，2 3，2 4
                answerIndex1.add(0);
                answerIndex1.add(1);
                options1.add(rightOptions.get(0));
                options1.add(rightOptions.get(1));
                options1.add(wrongOption.get(0));
                options1.add(wrongOption.get(1));
                break;
            case 1://1 2，1 3，1 4，2 3，2 4
                answerIndex1.add(0);
                answerIndex1.add(2);
                options1.add(rightOptions.get(0));
                options1.add(wrongOption.get(0));
                options1.add(rightOptions.get(1));
                options1.add(wrongOption.get(1));
                break;
            case 2://1 2，1 3，1 4，2 3，2 4
                answerIndex1.add(0);
                answerIndex1.add(3);
                options1.add(rightOptions.get(0));
                options1.add(wrongOption.get(0));
                options1.add(wrongOption.get(1));
                options1.add(rightOptions.get(1));
                break;
            case 3://1 2，1 3，1 4，2 3，2 4
                answerIndex1.add(1);
                answerIndex1.add(2);
                options1.add(wrongOption.get(0));
                options1.add(rightOptions.get(0));
                options1.add(rightOptions.get(1));
                options1.add(wrongOption.get(1));
                break;
            case 4://1 2，1 3，1 4，2 3，2 4
                answerIndex1.add(1);
                answerIndex1.add(3);
                options1.add(wrongOption.get(0));
                options1.add(rightOptions.get(0));
                options1.add(wrongOption.get(1));
                options1.add(rightOptions.get(1));
                break;
            case 5://倒序：2 0-------------0 1 2 3
                answerIndex1.add(2);
                answerIndex1.add(0);
                options1.add(rightOptions.get(1));
                options1.add(wrongOption.get(0));
                options1.add(rightOptions.get(0));
                options1.add(wrongOption.get(1));
                break;
            case 6://倒序：2 1-------------0 1 2 3
                answerIndex1.add(2);
                answerIndex1.add(1);
                options1.add(wrongOption.get(1));
                options1.add(rightOptions.get(1));
                options1.add(rightOptions.get(0));
                options1.add(wrongOption.get(0));
                break;
            case 7://倒序：3 2-------------0 1 2 3
                answerIndex1.add(3);
                answerIndex1.add(2);
                options1.add(wrongOption.get(1));
                options1.add(wrongOption.get(0));
                options1.add(rightOptions.get(1));
                options1.add(rightOptions.get(0));
                break;
            default:
                break;
        }
    }

    private void uploadAudios(String audioDir) {
        if (mListAllWords == null || mListAllWords.isEmpty()) {
            showDialog("No mListAllWords!");
            return;
        }

        MyApp app = (MyApp) getApplication();
        mOssTokenInfo = app.getOssTokenInfo();
        mOss = app.getOss();
        File audioFile = new File(audioDir);
        if (!audioFile.exists() || !audioFile.isDirectory()) {
            showDialog("音频目录错误！audioDir=" + audioDir);
            return;
        }

        uploadAudioTotalNumber = 0;
        uploadAudioFailedNumber = 0;
        uploadAudioSkipNumber = 0;
        File[] files = audioFile.listFiles();

        //校验是否是目录
        boolean isRightDir = true;
        for (File file : files) {
            if (file.isDirectory()) {
                File[] files1 = file.listFiles();
                if (files1.length == 0) {
                    Log.e(TAG, "uploadAudios: 空目录：" + file.getName());
                }
            } else {
                isRightDir = false;
                break;
            }
        }

        if (!isRightDir) {
            showDialog("音频目录错误！audioDir=" + audioDir);
            return;
        }

        mBtnUploadAudio.setEnabled(false);
        Observable.fromArray(files)
                .filter(new Predicate<File>() {
                    @Override
                    public boolean test(File file) {
                        return file.listFiles().length > 0;
                    }
                })
                .concatMap(new Function<File, ObservableSource<File>>() {
                    @Override
                    public ObservableSource<File> apply(final File file) {
                        File[] mp3Files = file.listFiles();
                        return Observable.fromArray(mp3Files);
                    }
                })
                .filter(new Predicate<File>() {
                    @Override
                    public boolean test(File file) {
                        return file.getPath().endsWith("mp3");
                    }
                })
                .filter(new Predicate<File>() {
                    @Override
                    public boolean test(File file) {
                        uploadAudioTotalNumber++;
                        boolean alreadyExist = false;
                        for (Word wordItem : mListAllWords) {
                            String englishPronunciation = wordItem.getEnglishPronunciation();
                            String sentenceAudio = wordItem.getExampleSentenceAudio();
                            Log.d(TAG, "uploadAudios--test:englishPronunciation= " + englishPronunciation);
                            Log.d(TAG, "uploadAudios--test: sentenceAudio=" + sentenceAudio);
                            String fileName = file.getName();
                            if (!TextUtils.isEmpty(englishPronunciation) && englishPronunciation.contains(fileName)) {
                                alreadyExist = true;
                                break;
                            }
                            if (!TextUtils.isEmpty(sentenceAudio) && sentenceAudio.contains(fileName)) {
                                alreadyExist = true;
                                break;
                            }
                        }
                        if (alreadyExist) {
                            uploadAudioSkipNumber++;
                        }
                        Log.d(TAG, "uploadAudios--test: alreadyExist=" + alreadyExist + ",getName=" + file.getName());
                        return !alreadyExist;
                    }
                })
                .delay(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<File>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(File file) {
                        Log.d(TAG, "uploadAudios--onNext: file=" + file.getPath());
//                        Log.d(TAG, "uploadAudios--onNext: file=" + file.getPath() + Thread.currentThread().getName());
                        updateFile(file.getPath());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "uploadAudios--onError: e=" + e.getMessage());
                        uploadAudioFailedNumber++;
                    }

                    @Override
                    public void onComplete() {
                        showDialog("音频上传成功！"
                                + "\n共：" + uploadAudioTotalNumber
                                + "\n跳过：" + uploadAudioSkipNumber
                                + "\n失败：" + uploadAudioFailedNumber);
                    }
                });

//        updateFile(audioDir);
    }

    private void uploadWordList() {
        mBtnUpdateWord.setEnabled(false);

        logStringBuilder.setLength(0);
//        Word word = mWordListFile.get(0);
//        mWordListFile = new ArrayList<>();
//        mWordListFile.add(word);

        updateWordTotalNumber = mWordListFile.size();
        updateWordSkipNumber = 0;
        updateWordFailedNumber = 0;

        Observable.fromIterable(mWordListFile)
                .filter(new Predicate<Word>() {
                    @Override
                    public boolean test(Word word) {
                        boolean complete = checkWordComplete(word);
                        if (!complete) {
                            updateWordSkipNumber++;
                            logStringBuilder.append("====信息不完整，已跳过 id=");
                            logStringBuilder.append(word.id);
                            logStringBuilder.append(",word=");
                            logStringBuilder.append(word.getEnglishSpell());
                            logStringBuilder.append("\n");
                        }

                        return complete;
                    }
                })
                .filter(new Predicate<Word>() {
                    @Override
                    public boolean test(Word word) {
                        boolean alreadyExist = false;
                        for (Word listAllWord : mListAllWords) {
                            if (TextUtils.equals(word.getEnglishSpell(), listAllWord.getEnglishSpell())) {
                                alreadyExist = true;
                                updateWordSkipNumber++;
                                break;
                            }
                        }
                        return !alreadyExist;
                    }
                })
                .concatMap(new Function<Word, ObservableSource<ResultBeanInfo<Word>>>() {

                    @Override
                    public ObservableSource<ResultBeanInfo<Word>> apply(Word word) throws Exception {
                        Log.d(TAG, "apply: word=" + word.getEnglishSpell());
                        logStringBuilder.append("----------------------------");
                        logStringBuilder.append("\n");
                        logStringBuilder.append(mSdf.format(new Date()));
                        logStringBuilder.append(" uploadWord apply=");
                        logStringBuilder.append(word.getEnglishSpell());
                        //RNewThreadScheduler-2
//                        logStringBuilder.append(" ");
//                        logStringBuilder.append(Thread.currentThread().getName());
                        logStringBuilder.append("\n");
                        return ApiClient.getInstance()
                                .createWord(word);
                    }
                })
                .delay(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
//                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResultBeanInfo<Word>>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResultBeanInfo<Word> wordResultBeanInfo) {
                        Word word = wordResultBeanInfo.getData();
                        logStringBuilder.append(mSdf.format(new Date()));
                        logStringBuilder.append(" uploadWord onNext=");
                        logStringBuilder.append(word.getEnglishSpell());
                        logStringBuilder.append("\n");
                    }

                    @Override
                    public void onError(Throwable e) {
                        logStringBuilder.append(" onError=");
                        logStringBuilder.append(e.getMessage());
                        logStringBuilder.append("\n");
                        updateWordFailedNumber++;
                    }

                    @Override
                    public void onComplete() {
                        showDialog("上传完成！" +
                                "\n共：" + updateWordTotalNumber +
                                "\n跳过：" + updateWordSkipNumber +
                                "\n失败：" + updateWordFailedNumber);
                        mSvLog.setText(logStringBuilder.toString());
                    }

                });
    }

    /**
     * 上传问题
     */
    private void uploadQustions(List<Question> questions) {

        if (questions == null || questions.isEmpty()) {
            showDialog("No questions!");
            return;
        }

//        Question question = questions.get(questions.size() - 1);
//        questions = new ArrayList<>();
//        questions.add(question);
        uploadQuestionTotalNumber = questions.size();
        uploadQuestionSkipedNumber = 0;
        Observable.fromIterable(questions)
                .concatMap(new Function<Question, ObservableSource<ResponseBody>>() {
                    @Override
                    public ObservableSource<ResponseBody> apply(Question question) {
                        return ApiClient.getInstance()
                                .createQuestion(question);
                    }
                })
                .delay(100, TimeUnit.MILLISECONDS)
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
                        showDialog("上传练习成功！"
                                + "\n共：" + uploadQuestionTotalNumber
                                + "\n跳过：" + uploadQuestionSkipedNumber);
                    }
                });
    }

    boolean checkWordList() {
        if (mWordListFile == null || mWordListFile.isEmpty()) {
            showDialog("请先解析文件，获取WordList");
            return false;
        }
        return true;
    }

    boolean checkWordComplete(Word word) {
        if (word == null) {
            return false;
        }
        if (TextUtils.isEmpty(word.getEnglishSpell())
                || TextUtils.isEmpty(word.getChineseSpell())
                || TextUtils.isEmpty(word.getMeaning())
                || TextUtils.isEmpty(word.getExampleSentence())) {
            return false;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("确定返回？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .create()
                .show();
//        super.onBackPressed();
    }
}
