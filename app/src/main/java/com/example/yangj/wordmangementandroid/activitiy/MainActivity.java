package com.example.yangj.wordmangementandroid.activitiy;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
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

import java.io.File;
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
    private static final int REQ_SELECT_WORD_FILE = 11;
    private static final int REQ_SELECT_IMAGES_FILE = 22;
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
    @BindView(R.id.tv_word_file_path)
    TextView mTvPath;
    @BindView(R.id.tv_question_file_path)
    TextView mTvQuestionFilePath;
    @BindView(R.id.btn_upload_questions_images)
    Button mBtnUploadQuestionsImages;
    //解析文件获取单词列表
    private List<Word> mWordListFile;
    //解析文件获取的同时保留练习信息，点击上传练习时再整合成Qustion
    private List<WordLoad> mWordLoadList = new ArrayList<>();
    private String wordFilePath;
    private String mWordImagesDir;
    private OssTokenInfo mOssTokenInfo;
    private StringBuilder logStringBuilder = new StringBuilder();
    private SimpleDateFormat mSdf = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());
    private int updateWordSkipNumber = 0;
    private int updateWordTotalNumber = 0;
    private int updateWordFailedNumber = 0;

    private int uploadQuestionTotalNumber = 0;
    private int uploadQuestionSkipedNumber = 0;

    private int uploadImagesTotalNumber = 0;

    private List<Word> mListAllWordsRelease;
    private List<Question> mQuestionListRelease;
    private List<Question> mQuestionListOnlyImages;

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
        requestPermission();
        listAll();
        listAllQuestions();
        MyApp app = (MyApp) getApplication();
        mOssTokenInfo = app.getOssTokenInfo();
    }

    private void listAllQuestions() {
        showProgressDialog("加载中，请稍后...");

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
                        hideProgressDialog();
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "listAllQuestions--onComplete");
                        hideProgressDialog();
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
            showProgressDialog("file 不存在 name=" + file.getName() + "\nwordFilePath=" + file.getPath());
            return;
        }

        if (mWordListFile == null || mWordListFile.isEmpty()) {
            showProgressDialog("请先解析文档，获取wordList");
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
                if (buildQustionsInfo(strBuilderQ, qustions)) {
                    WordListInfoActivity.start(this, strBuilderQ.toString());
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean buildQustionsInfo(StringBuilder strBuilderQ, List<Question> qustions) {
        if (qustions == null || qustions.isEmpty()) {
            showAlertDialog("No qustions!");
            return false;
        }

        strBuilderQ.append("共");
        strBuilderQ.append(qustions.size());
        strBuilderQ.append("题");
        strBuilderQ.append("\n\n");

        int unCompleteCount = 0;
        for (Question question : qustions) {
            if (question.getType().equals(Question.Type.MATCH_WORD_IMAGE)) {
                if (question.getOptions().size() != 3) {
                    strBuilderQ.append("wordId=");
                    strBuilderQ.append(question.getWordId());
                    strBuilderQ.append(" 词图关联 不够3张图片！");
                    strBuilderQ.append("\n");
                    unCompleteCount++;
                }
            }

            if (question.getType().equals(Question.Type.CHOOSE_IMAGE_BY_LISTEN_WORD)) {
                boolean complete = true;
                if (question.getOptions().size() != 2) {
                    strBuilderQ.append("wordId=");
                    strBuilderQ.append(question.getWordId());
                    strBuilderQ.append(" 词图关联 不够2张图片！");
                    strBuilderQ.append("\n");
                    complete = false;
                }

                List<Integer> answersIndex = question.getAnswersIndex();
                if (answersIndex == null || answersIndex.size() != 1) {
                    strBuilderQ.append("wordId=");
                    strBuilderQ.append(question.getWordId());
                    strBuilderQ.append(" 听词选图 没答案！！");
                    strBuilderQ.append("\n");
                    complete = false;
                }

                if (!complete) {
                    unCompleteCount++;
                }
            }
        }

        strBuilderQ.append("不完整的练习数：");
        strBuilderQ.append(unCompleteCount);
        strBuilderQ.append("\n");
        strBuilderQ.append("==================================");
        strBuilderQ.append("\n");

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

            strBuilderQ.append("wordId: ");
            strBuilderQ.append(qustion.getWordId());
            strBuilderQ.append("\n");
            //to chinese
            strBuilderQ.append("题型：");
            strBuilderQ.append(QuestionType2Chinese.getChinese(qustion.getType()));
            strBuilderQ.append("\n");
            List<String> options = qustion.getOptions();
            if (options != null && options.size() > 0) {
                int tempIndex = 0;
                for (String option : options) {
                    strBuilderQ.append("选项");
                    strBuilderQ.append(++tempIndex);
                    strBuilderQ.append(": ");
                    strBuilderQ.append(option);
                    strBuilderQ.append("\n");
                }
            } else {
                strBuilderQ.append("没选项======");
            }

            strBuilderQ.append("\n");

            List<Integer> answersIndex = qustion.getAnswersIndex();
            if (answersIndex != null && answersIndex.size() == 1) {
                strBuilderQ.append("答案:");
                for (Integer integer : answersIndex) {
                    strBuilderQ.append(((char) ('A' + integer)));
                    strBuilderQ.append(" ");
                }
                strBuilderQ.append("\n");
            } else {
                strBuilderQ.append("没答案================");
            }

            strBuilderQ.append("\n");
        }
        return true;
    }


    @OnClick({R.id.btn_load_file, R.id.btn_upload_questions_images,
            R.id.btn_select_word_file,
            R.id.btn_select_questions_images_file,
            R.id.btn_upload_word,
            R.id.btn_upload_questions_only_Images_preview,
            R.id.btn_upload_questions_only_Images,
            R.id.btn_upload_questions,
            R.id.btn_show_word_info})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_select_word_file:
                FileSelectorActivity.startForResult(this, REQ_SELECT_WORD_FILE);
                break;
            case R.id.btn_select_questions_images_file:
                FileSelectorActivity.startForResult(this, REQ_SELECT_IMAGES_FILE);
                break;
            case R.id.btn_load_file:
                mWordLoadList.clear();
                mWordListFile = parseFile(wordFilePath, mWordLoadList);
                if (mWordListFile != null && !mWordListFile.isEmpty()) {
                    showAlertDialog("解析成功！\n共：" + mWordListFile.size() + "个单词");
                    mBtnLoadFile.setEnabled(false);
                }
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
                uploadQustionsImages(mWordImagesDir);
                break;
            case R.id.btn_upload_questions:
                if (checkWordList()) {
                    List<Question> questionList = createQustions();
                    uploadQustions(questionList);
                }
                break;
            case R.id.btn_upload_questions_only_Images:
                if (mQuestionListOnlyImages == null || mQuestionListOnlyImages.isEmpty()) {
                    showAlertDialog("没有练习！");
                    return;
                }
                uploadQustions(mQuestionListOnlyImages);
                break;
            case R.id.btn_upload_questions_only_Images_preview:
                StringBuilder strBuilderQ = new StringBuilder();
                if (mQuestionListOnlyImages == null || mQuestionListOnlyImages.isEmpty()) {
                    showAlertDialog("请先上传图片！");
                    return;
                }

                if (buildQustionsInfo(strBuilderQ, mQuestionListOnlyImages)) {
                    WordListInfoActivity.start(this, strBuilderQ.toString());
                }
                break;
            case R.id.btn_show_word_info:
                if (checkWordList()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("共");
                    stringBuilder.append(mWordListFile.size());
                    stringBuilder.append("个单词！");
                    stringBuilder.append("\n");
                    stringBuilder.append("都有单词、释意和例句！");
                    stringBuilder.append("\n\n");
                    boolean complete = true;
                    for (int i = 0; i < mWordListFile.size(); i++) {
                        Word word = mWordListFile.get(i);
                        word.id = i + 1;
                        stringBuilder.append(word.toString());
                        stringBuilder.append("\n\n");

                        String wordSpell = word.getEnglishSpell();
                        wordSpell = wordSpell.replaceAll(" ", "");
                        wordSpell = wordSpell.replaceAll("-", "");
                        boolean matches = wordSpell.matches("^[a-zA-Z]*");
                        if (!matches) {
                            showAlertDialog("请检查单词拼写" + word.getEnglishSpell());
                            complete = false;
                            break;
                        }
                        if (TextUtils.isEmpty(word.getExampleSentence())) {
                            showAlertDialog("单词" + word.getEnglishSpell() + "没有例句");
                            complete = false;
                            break;
                        }

                        if (TextUtils.isEmpty(word.getMeaning())) {
                            showAlertDialog("单词" + word.getEnglishSpell() + "没有释意");
                            complete = false;
                            break;
                        }


                    }

                    if (complete) {
                        WordListInfoActivity.start(this, stringBuilder.toString());
                    }
                }
                break;
        }
    }

    private void uploadQustionsImages(String path) {
        if (TextUtils.isEmpty(path)) {
            showAlertDialog("请选择图片目录！");
            return;
        }

        if (mWordListFile == null || mWordListFile.isEmpty()) {
            showAlertDialog("请先解析文件");
            return;
        }

        if (mListAllWordsRelease == null || mListAllWordsRelease.isEmpty()) {
            showAlertDialog("No mListAllWordsRelease！");
            return;
        }

        //预习 和 听词选图
        mQuestionListOnlyImages = new ArrayList<>(mWordListFile.size() * 2);

        for (Word word : mWordListFile) {
            int wordId = 0;
            for (int i = mListAllWordsRelease.size() - 1; i >= 0; i--) {
                Word word1 = mListAllWordsRelease.get(i);
                if (word.getEnglishSpell().equalsIgnoreCase(word1.getEnglishSpell())) {
                    wordId = word1.id;
                    word.setRectangleImage(word1.getRectangleImage());
                    word.setImage(word1.getImage());
                    break;
                }
            }

            if (wordId == 0) {
                showAlertDialog("预创建questions失败！wordId == 0,word=" + word.getEnglishSpell());
                return;
            }

            //用于填充数据！
            String rectangleImage = word.getRectangleImage();
            if (!URLUtil.isNetworkUrl(rectangleImage)) {
                showAlertDialog("单词：" + word.getEnglishSpell() + " 没有矩形图片！");
                return;
            }

            //用于填充数据！
            String image = word.getImage();
            if (!URLUtil.isNetworkUrl(image)) {
                showAlertDialog("单词：" + word.getEnglishSpell() + " 没有正方形图片！");
                return;
            }

            Question questionMatch = new Question();
            questionMatch.setWordId(wordId);
            questionMatch.setType(Question.Type.MATCH_WORD_IMAGE);
            questionMatch.setOptions(new ArrayList<String>(3));
            mQuestionListOnlyImages.add(questionMatch);//////////////////temp

            Question questionChoose = new Question();
            questionChoose.setWordId(wordId);
            questionChoose.setType(Question.Type.CHOOSE_IMAGE_BY_LISTEN_WORD);
            ArrayList<String> options = new ArrayList<>(2);
            options.add(rectangleImage);
            questionChoose.setOptions(options);
            mQuestionListOnlyImages.add(questionChoose);
        }

        File fileDir = new File(path);
        if (!fileDir.exists() || !fileDir.isDirectory()) {
            showAlertDialog("图片目录错误！" + fileDir.getName());
            return;
        }

        File[] listFiles = fileDir.listFiles();
        if (listFiles == null || listFiles.length == 0) {
            showAlertDialog("空目录！" + fileDir.getName());
            return;
        }

        if (mWordListFile == null || mWordListFile.isEmpty()) {
            showAlertDialog("请先解析文件！");
            return;
        }
        uploadImagesTotalNumber = 0;

        showProgressDialog("图片上传中...");

        Observable.fromArray(listFiles)
                .concatMap(new Function<File, ObservableSource<File>>() {
                    @Override
                    public ObservableSource<File> apply(File file) {
                        return Observable.fromArray(file.listFiles());
                    }
                })
                .filter(new Predicate<File>() {
                    @Override
                    public boolean test(File file) {
                        return file.getName().endsWith(".png");
                    }
                })
                .filter(new Predicate<File>() {
                    @Override
                    public boolean test(File file) {
                        return file.getName().contains("-rect-");
//                        return file.getName().contains("rect-wrong-pic");/////////tmp
                    }
                })
//                .take(10)///////////temp
                .subscribe(new Observer<File>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(File file) {
                        Log.d(TAG, "onNext: getName=" + file.getName());
                        uploadImagesTotalNumber++;

                        uploadQustionFile(file.getPath());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");
                        hideProgressDialog();
                        showAlertDialog("上传图片失败！");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                        hideProgressDialog();
                        showAlertDialog("上传图片成功!\n共" + uploadImagesTotalNumber + "张");
                    }
                });

    }

    private void uploadQustionFile(String path) {
        if (mOssTokenInfo == null || mOss == null) {
            showAlertDialog("mOssTokenInfo or mOss不存在");
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            showAlertDialog("file 不存在 name=" + file.getName() + "\nwordFilePath=" + file.getPath());
            return;
        }

        if (mListAllWordsRelease == null || mListAllWordsRelease.isEmpty()) {
            showAlertDialog("请先获取线上单词列表");
            return;
        }

//        UUID uuid = UUID.randomUUID();
        String objectKey = null;
        if (file.getName().endsWith(".mp3")) {
            objectKey = "courseware/audio/" + file.getName();
        } else {
            objectKey = "courseware/image/" + file.getName();
        }
        Log.d(TAG, "uploadQustionFile: objectKey=" + objectKey);

        PutObjectRequest putObjectRequest = new PutObjectRequest(mOssTokenInfo.getBucket()
                , objectKey, path);

        mOss.asyncPutObject(putObjectRequest, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                String objectKey = request.getObjectKey();
                String url = mOssTokenInfo.getCdnEndpoint() + "/" + objectKey;
                String fileName = new File(objectKey).getName();
                String suffix = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
                fileName = fileName.substring(0, fileName.indexOf("."));//eg. cow, cow1,egg-rect-right-pic-3
                Log.d(TAG, "uploadQustionFile--onSuccess: fileName=" + fileName);
                if ("png".equals(suffix)) {//image
                    if (mListAllWordsRelease != null) {
                        String wordSpell = fileName.substring(0, fileName.indexOf("-rect-"));
                        Word word = null;
                        for (int i = mListAllWordsRelease.size() - 1; i >= 0; i--) {
                            Word wordI = mListAllWordsRelease.get(i);
                            if (TextUtils.equals(wordI.getEnglishSpell().toLowerCase(), wordSpell.toLowerCase())) {
                                word = wordI;
                                break;
                            }
                        }

                        if (word == null || word.id <= 0) {
                            Log.e(TAG, "uploadQustionFile 返回: " + wordSpell);
                            return;
                        }

                        int index = 0;
                        for (Question question : mQuestionListOnlyImages) {
                            index++;
                            if (question.getWordId() == word.id) {
                                if (fileName.contains("-rect-right-pic-") && question.getType().equals(Question.Type.MATCH_WORD_IMAGE)) {
                                    question.getOptions().add(url);
                                }

//                                table-rect-wrong-pic,随机生成答案
                                if (fileName.contains("rect-wrong-pic") && question.getType().equals(Question.Type.CHOOSE_IMAGE_BY_LISTEN_WORD)) {
                                    question.getOptions().add(url);
                                    List<String> options = question.getOptions();
                                    if (options.size() == 2) {
                                        List<String> optionsNew = new ArrayList<>();
                                        if (index % 2 == 0) {
                                            optionsNew.addAll(options);
                                        } else {
                                            for (int i = options.size() - 1; i >= 0; i--) {
                                                optionsNew.add(options.get(i));//倒置
                                            }
                                        }

                                        List<Integer> answerList = new ArrayList<>();
                                        for (int i = 0; i < optionsNew.size(); i++) {
                                            String opt = optionsNew.get(i);
                                            if (opt.contains("rect-right-pic")) {
                                                answerList.add(i);
                                                break;
                                            }
                                        }

                                        question.setOptions(optionsNew);
                                        question.setAnswersIndex(answerList);
                                    }
                                    break;
                                }
                            }
                        }

                    }
                }
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

    private List<Question> createQustions() {
        if (mListAllWordsRelease == null || mListAllWordsRelease.isEmpty()) {
            showAlertDialog("No ListAllWords!");
            return null;
        }

        List<Question> questionList = new ArrayList<>();
        for (int i = 0; i < mWordLoadList.size(); i++) {
            WordLoad wordLoad = mWordLoadList.get(i);
            for (int j = mListAllWordsRelease.size() - 1; j >= 0; j--) {
                Word wordRelease = mListAllWordsRelease.get(j);
                //每个单词10道题，
                if (TextUtils.equals(wordLoad.word, wordRelease.getEnglishSpell())) {
                    //MATCH_WORD_MEANING 词意关联
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
                    if (optionsMatchMeaning.size() == 2) {//校验
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
                        showAlertDialog("上传完成！" +
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
            showAlertDialog("No mQuestionListRelease!");
            return;
        }

        if (questions == null || questions.isEmpty()) {
            showAlertDialog("No questions!");
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
                .filter(new Predicate<Question>() {
                    @Override
                    public boolean test(Question question) {
                        boolean right = true;
                        List<String> options = question.getOptions();
                        List<Integer> answersIndex = question.getAnswersIndex();
                        switch (question.getType()) {
                            case MATCH_WORD_IMAGE:
                                right = options != null && options.size() == 3;
                                break;
                            case CHOOSE_IMAGE_BY_LISTEN_WORD:
                                right = options != null && options.size() == 2;
                                if (right) {
                                    right = answersIndex != null && answersIndex.size() == 1;
                                }
                                break;
                        }
                        if (!right) {
                            uploadQuestionSkipedNumber++;
                        }
                        return right;
                    }
                })
                .concatMap(new Function<Question, ObservableSource<ResultBeanInfo<Question>>>() {
                    @Override
                    public ObservableSource<ResultBeanInfo<Question>> apply(Question question) {
                        return ApiClient.getInstance()
                                .createQuestion(question);
                    }
                })
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
                        showAlertDialog("上传练习完成！" +
                                "\n共：" + uploadQuestionTotalNumber +
                                "\n跳过：" + uploadQuestionSkipedNumber);
                    }
                });
    }

    boolean checkWordList() {
        if (mWordListFile == null || mWordListFile.isEmpty()) {
            showAlertDialog("请先解析文件！");
//            showAlertDialog("请先解析文件，获取WordList");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "未选择", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (requestCode) {
            case REQ_SELECT_WORD_FILE:
                String pathWord = data.getStringExtra(Intent.EXTRA_TEXT);
                File fileWord = new File(pathWord);
                if (!fileWord.exists() || !fileWord.isFile()) {
                    showProgressDialog("文件不存在，或者不是文件：" + fileWord.getName());
                    return;
                }
                wordFilePath = pathWord;
                mTvPath.setText("单词文件：" + fileWord.getName());
                break;
            case REQ_SELECT_IMAGES_FILE:
                String pathImages = data.getStringExtra(Intent.EXTRA_TEXT);
                File fileImages = new File(pathImages);
                if (!fileImages.exists() || !fileImages.isDirectory()) {
                    showProgressDialog("文件不存在，或者不是目录：" + fileImages.getName());
                    return;
                }
                mWordImagesDir = pathImages;
                mTvQuestionFilePath.setText("图片目录：" + fileImages.getName());
                break;
        }
    }
}
