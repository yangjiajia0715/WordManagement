package com.example.yangj.wordmangementandroid.activitiy;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientException;
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

public class MainActivity extends BaseActivity {
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
    @BindView(R.id.btn_upload_word_image)
    Button mBtnUploadWordImage;
    @BindView(R.id.btn_show_word_info)
    Button mBtnShowWordInfo;
    @BindView(R.id.tv_path)
    TextView mTvPath;
    //解析文件获取单词列表
    private List<Word> mWordListFile;
    //解析文件获取的同时保留练习信息，点击上传练习时再整合成Qustion
    private List<WordLoad> mWordLoadList = new ArrayList<>();
    private static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "wordManagement" + File.separator;
    private String wordFilePath = BASE_PATH + "一下1-80.txt";
    private String mWordImagesDir = BASE_PATH + "一下1-30-all";
    private OssTokenInfo mOssTokenInfo;
    private StringBuilder logStringBuilder = new StringBuilder();
    private SimpleDateFormat mSdf = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());
    private int updateWordSkipNumber = 0;
    private int updateWordTotalNumber = 0;
    private int updateWordFailedNumber = 0;

    private int uploadQuestionTotalNumber = 0;
    private int uploadQuestionSkipedNumber = 0;

    private List<Word> mListAllWordsRelease;
    private List<Question> mQuestionListRelease;

    public static void start(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setTitle("解析并上传单词");
        showPath();
        requestPermission();
        listAll();
        listAllQuestions();
        MyApp app = (MyApp) getApplication();
        mOssTokenInfo = app.getOssTokenInfo();
    }

    private void showPath() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("单词文件：");
        stringBuilder.append(new File(wordFilePath).getName());
        stringBuilder.append("\n");
        stringBuilder.append("单词图片目录：");
        stringBuilder.append(new File(mWordImagesDir).getName());
        mTvPath.setText(stringBuilder.toString());
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
                    }
                });
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(this
                , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}
                , PERMISSION);
    }

    @Nullable
    private void updateFile(String path) {

        File file = new File(path);
        if (!file.exists()) {
            showDialog("file 不存在 name=" + file.getName() + "\nwordFilePath=" + file.getPath());
            return;
        }

        if (mWordListFile == null || mWordListFile.isEmpty()) {
            showDialog("请先解析文档，获取wordList");
            return;
        }

        uploadFile(path, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                String objectKey = request.getObjectKey();
                String url = mOssTokenInfo.getCdnEndpoint() + "/" + objectKey;
                String fileName = new File(objectKey).getName();
                String suffix = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
                fileName = fileName.substring(0, fileName.indexOf("."));//eg. cow, cow1
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                Log.d(TAG, "onFailure: " + request.getObjectKey());
            }
        });

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
                        showDialog("listAll size=" + mListAllWordsRelease.size());
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
        Log.d(TAG, "loadFile: wordFilePath=" + path);
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
        Log.d(TAG, "loadFile: wordFilePath=" + path);
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

    @OnClick({R.id.btn_load_file, R.id.btn_upload_questions_images,
            R.id.btn_upload_word, R.id.btn_upload_questions, R.id.btn_show_word_info})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_load_file:
                mWordListFile = loadFile(wordFilePath);
                mBtnLoadFile.setEnabled(false);
                break;
            case R.id.btn_upload_word:
                if (mListAllWordsRelease == null || mListAllWordsRelease.isEmpty()) {
                    Toast.makeText(this, "No mListAllWordsRelease!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (checkWordList()) {
                    uploadWordList();
                } else {
                    mBtnLoadFile.setEnabled(true);
                }
                break;
            case R.id.btn_upload_questions_images:
                uploadQustionsImages("");
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

    private void uploadQustionsImages(String path) {

    }

    private List<Question> createQustions() {
        if (mListAllWordsRelease == null || mListAllWordsRelease.isEmpty()) {
            showDialog("No ListAllWords!");
            return null;
        }

        List<Question> questionList = new ArrayList<>();
        for (int i = 0; i < mWordLoadList.size(); i++) {
            WordLoad wordLoad = mWordLoadList.get(i);
            for (int j = mListAllWordsRelease.size() - 1; j >= 0; j--) {
                Word wordRelease = mListAllWordsRelease.get(j);
                //每个单词10道题，
                if (TextUtils.equals(wordLoad.word, wordRelease.getEnglishSpell())) {
                    //CHOOSE_WORD_BY_LISTEN_SENTENCE：听文选词
                    //SPELL_WORD_BY_READ_IMAGE 看图拼写
                    //SPELL_WORD_BY_LISTEN_WORD 听词拼写
                    //1 2，1 3，1 4，2 3，2 4
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

                    //词意关联 MATCH_WORD_MEANING
                    Question questionMatchMeaning = new Question();
                    questionMatchMeaning.setType(Question.Type.MATCH_WORD_MEANING);
                    questionMatchMeaning.setWordId(wordRelease.id);
                    List<Integer> answerMatchMeaning = new ArrayList<>();
                    List<String> optionsMatchMeaning = new ArrayList<>();
                    if (i % 2 == 0) {
                        answerMatchMeaning.add(0);
                        optionsMatchMeaning.add(wordRelease.getChineseSpell());
                        optionsMatchMeaning.add(wordLoad.wordChineseWrong);
                    } else {
                        answerMatchMeaning.add(1);
                        optionsMatchMeaning.add(wordLoad.wordChineseWrong);
                        optionsMatchMeaning.add(wordRelease.getChineseSpell());
                    }
                    questionMatchMeaning.setAnswersIndex(answerMatchMeaning);
                    questionMatchMeaning.setOptions(optionsMatchMeaning);
                    if (optionsMatchMeaning.size() != 2) {//校验
                        questionList.add(questionMatchMeaning);//add
                    }

                    //听文选词 CHOOSE_WORD_BY_LISTEN_SENTENCE
                    Question questionChooseWordListener = new Question();
                    questionChooseWordListener.setType(Question.Type.CHOOSE_WORD_BY_LISTEN_SENTENCE);
                    questionChooseWordListener.setWordId(wordRelease.id);
                    List<Integer> answerChooseListen = new ArrayList<>();
                    List<String> optionsChooseListen = new ArrayList<>();
                    if (i % 2 == 0) {
                        answerChooseListen.add(0);
                        optionsChooseListen.add(wordLoad.word);
                        optionsChooseListen.add(wordLoad.wordEnglishWrong);
                    } else {
                        answerChooseListen.add(1);
                        optionsChooseListen.add(wordLoad.wordEnglishWrong);
                        optionsChooseListen.add(wordLoad.word);
                    }
                    questionChooseWordListener.setOptions(optionsChooseListen);
                    questionChooseWordListener.setAnswersIndex(answerChooseListen);

                    questionList.add(questionChooseWordListener);//add

                    //看图拼写
                    Question questionSpellRead = new Question();
                    questionSpellRead.setType(Question.Type.SPELL_WORD_BY_READ_IMAGE);
                    questionSpellRead.setWordId(wordRelease.id);
                    List<Integer> answerSpell1 = new ArrayList<>();
                    List<String> optionsSpell1 = new ArrayList<>();
                    questionSpell(i, rightOptions, wrongOption1, answerSpell1, optionsSpell1);//
                    questionSpellRead.setAnswersIndex(answerSpell1);
                    questionSpellRead.setOptions(optionsSpell1);

                    questionList.add(questionSpellRead);//add

                    //听词拼写
                    Question questionSpellListen = new Question();
                    questionSpellListen.setType(Question.Type.SPELL_WORD_BY_LISTEN_WORD);
                    questionSpellListen.setWordId(wordRelease.id);
                    List<Integer> answerSpell2 = new ArrayList<>();
                    List<String> optionsSpell2 = new ArrayList<>();
                    questionSpell(i * 3, rightOptions, wrongOption2, answerSpell2, optionsSpell2);//
                    questionSpellListen.setAnswersIndex(answerSpell2);
                    questionSpellListen.setOptions(optionsSpell2);

                    questionList.add(questionSpellListen);//add

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

    private void uploadWordList() {
        mBtnUpdateWord.setEnabled(false);

        logStringBuilder.setLength(0);

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
                        for (Word listAllWord : mListAllWordsRelease) {
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
                    public ObservableSource<ResultBeanInfo<Word>> apply(Word word) {
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
                .subscribeOn(Schedulers.newThread())
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

        if (mQuestionListRelease == null || mQuestionListRelease.isEmpty()) {
            showDialog("No mQuestionListRelease!");
            return;
        }

        if (questions == null || questions.isEmpty()) {
            showDialog("No questions!");
            return;
        }

        uploadQuestionTotalNumber = questions.size();
        uploadQuestionSkipedNumber = 0;
        Observable.fromIterable(questions)
                .filter(new Predicate<Question>() {
                    @Override
                    public boolean test(Question question) {
                        boolean isAlreadyExist = false;
                        for (int i = mQuestionListRelease.size() - 1; i >= 0; i--) {
                            Question questionRelease = mQuestionListRelease.get(i);
                            if (questionRelease.getWordId() == question.getWordId()
                                    && questionRelease.getType().equals(question.getType())) {
                                isAlreadyExist = true;
                                Log.d(TAG, "uploadQustions--skip: getWordId=" + questionRelease.getWordId());
                                uploadQuestionSkipedNumber++;
                            }
                        }
                        return !isAlreadyExist;
                    }
                })
                .concatMap(new Function<Question, ObservableSource<ResultBeanInfo<Question>>>() {
                    @Override
                    public ObservableSource<ResultBeanInfo<Question>> apply(Question question) {
                        return ApiClient.getInstance()
                                .createQuestion(question);
                    }
                })
                .takeLast(1)////////////////////////////////////
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResultBeanInfo<Question>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResultBeanInfo<Question> questionResultBeanInfo) {
                        Log.d(TAG, "uploadQustions--onNext");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "uploadQustions--onError e=" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "uploadQustions--onComplete");
                        showDialog("上传练习完成！" +
                                "\n共：" + uploadQuestionTotalNumber +
                                "\n跳过：" + uploadQuestionSkipedNumber);
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
    }

}
