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
import com.example.yangj.wordmangementandroid.common.QuestionTitle;
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
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * 图片命名规则：
 * egg-rect-right-pic-1.png
 * egg-rect-right-pic-2.png
 * egg-rect-right-pic-3.png
 * egg-rect-wrong-pic.png
 * egg-square-pic.png
 * Created by yangjiajia on 2018/6/21.
 */
public class UpdateWordActivity extends BaseActivity {
    private static final String TAG = "UpdateWordActivity";

    @BindView(R.id.btn_upload_audio)
    Button mBtnUploadAudio;
    @BindView(R.id.btn_preview)
    Button mBtnPreview;
    @BindView(R.id.btn_update_word)
    Button mBtnUpdateWord;
    @BindView(R.id.tv_preview)
    TextView mTvPreview;

    private int uploadAudioSkipNumber = 0;
    private int uploadAudioTotalNumber = 0;
    private int uploadAudioFailedNumber = 0;

    private int uploadWordTotalNumber = 0;
    private int uploadWordSkipNumber = 0;
    private int uploadWordFailedNumber = 0;

    private int uploadAudioWordNotFoundNumber = 0;

    private static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "wordManagement" + File.separator;
    private String wordFilePath = BASE_PATH + "一下1-80.txt";
    //一上 资源汇总31-94 ,音频已上传，单词已更新 2018-6-21 20:08:39
//    private String mWordAudioDir = BASE_PATH + "一上 资源汇总31-94";//2018-6-21 20:08:39
    private String mWordAudioDir = BASE_PATH ;
//    private String mWordImagesDir = BASE_PATH + "一下资源汇总32-87";
//    private String mWordImagesDir = BASE_PATH + "一上 资源汇总31-94";
    private String mWordImagesDir = BASE_PATH + "test";
//    private String mWordImagesDir = BASE_PATH + "oneDown32-87b";
//    private String mWordImagesDir = BASE_PATH;
    private String mWordSentencePath = BASE_PATH + "一上31-94新例句.txt";
    private List<Word> mListAllWords;

    private OSS mOss;
    private OssTokenInfo mOssTokenInfo;
    private List<Word> needUploadWords = new ArrayList<>();
    private List<Question> needUpdateQustions = new ArrayList<>();
    int needUpdateQustionsSuccessNumber = 0;
    StringBuilder mStringBuilder = new StringBuilder();
    private List<Question> mQuestionListRelease;

    public static void start(Context context) {
        Intent starter = new Intent(context, UpdateWordActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_word);
        ButterKnife.bind(this);
        MyApp app = (MyApp) getApplication();
        mOssTokenInfo = app.getOssTokenInfo();
        mOss = app.getOss();
        listAllWord();
        listAllQuestions();
    }

    @OnClick({R.id.btn_upload_audio, R.id.btn_preview, R.id.btn_update_word
            , R.id.btn_load_sentence, R.id.btn_update_questions, R.id.btn_update_questions_api
            , R.id.btn_upload_word_image})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_upload_audio:
                uploadAudios(mWordAudioDir);
                break;
            case R.id.btn_load_sentence:
                showDialog("注意：只能解析仅仅有一个例句的文件");
                loadFile(mWordSentencePath);
                break;
            case R.id.btn_preview:
                preview();
                break;
            case R.id.btn_update_word:
                updateWords();
                break;
            case R.id.btn_update_questions:
                updateQustions();
                break;
            case R.id.btn_update_questions_api:
                updateQustionsApi();
                break;
            case R.id.btn_upload_word_image:
                uploadWordImages(mWordImagesDir);
                break;
        }
    }

    /**
     * 图片命名规则：egg
     * egg-rect-right-pic-1.png
     * egg-rect-right-pic-2.png
     * egg-rect-right-pic-3.png
     * egg-rect-wrong-pic.png
     * egg-square-pic.png
     */
    private void uploadWordImages(String imagePath) {
        needUploadWords.clear();
        File file = new File(imagePath);
        if (!file.exists()) {
            showDialog("目录不存在：" + file.getPath());
            return;
        }

        if (file.listFiles() == null) {
            showDialog("空目录：" + file.getPath());
            return;
        }

        File[] files = file.listFiles();
        for (File file1 : files) {
            String file1Name = file1.getName();
            Log.d(TAG, "uploadWordImages: getName=" + file1Name);
        }

        Observable.fromArray(file.listFiles())
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
                        return file.getName().contains("square-pic")
                                || file.getName().contains("rect-right-pic-1");
                    }
                })
//                .take(2)
                .subscribe(new Observer<File>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(File file) {
                        Log.d(TAG, "onNext: getName=" + file.getName());
//                        uploadFile(file.getPath());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });


    }

    private void updateQustionsApi() {
        if (needUpdateQustions == null || needUpdateQustions.isEmpty()) {
            Toast.makeText(this, "No needUpdateQustions!", Toast.LENGTH_SHORT).show();
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
                        showDialog("更新练习失败");
                    }

                    @Override
                    public void onComplete() {
                        showDialog("更新练习成功 共：" + needUpdateQustionsSuccessNumber);
                    }
                });

    }

    private void updateQustions() {
        if (needUploadWords == null
                || needUploadWords.isEmpty()
                || mQuestionListRelease == null
                || mQuestionListRelease.isEmpty()) {
            Toast.makeText(this, "空！", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Question question : mQuestionListRelease) {
            if (question.getType().equals(Question.Type.CHOOSE_WORD_BY_LISTEN_SENTENCE)
                    || question.getType().equals(Question.Type.CHOOSE_WORD_BY_READ_SENTENCE)) {
                for (Word word : needUploadWords) {
                    if (question.getWordId() == word.id) {
                        String englishSpell = word.getEnglishSpell();
                        String title = word.getExampleSentence();
                        int index = title.indexOf(englishSpell);
                        if (index > 0) {
                            title = title.replace(englishSpell, "__");
                        } else {
                            String substring = title.substring(0, englishSpell.length());
                            if (TextUtils.equals(englishSpell.toLowerCase(), substring.toLowerCase())) {
                                title = title.replace(substring, "__");
                            }
                        }
                        Log.d(TAG, "updateQustions: title=" + title);
                        QuestionTitle questionTitle = question.getTitle();
                        questionTitle.setTitle(title);
                        question.setTitle(questionTitle);
                        needUpdateQustions.add(question);
                        break;
                    }
                }
            }
        }
    }

    private void loadFile(String wordSentencePath) {
        File file = new File(wordSentencePath);
        if (!file.exists()) {
            Toast.makeText(this, "不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mListAllWords == null || mListAllWords.isEmpty()) {
            Toast.makeText(this, "No mListAllWords!", Toast.LENGTH_SHORT).show();
            return;
        }

        needUploadWords.clear();

        int total = 0;

        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line = null;
            List<String> lines = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                String twoChar = line.substring(0, 2);
                if (TextUtils.isDigitsOnly(twoChar)) {
                    if (lines.size() == 2) {
                        String wordLine = lines.get(0);
                        String wordSentence = lines.get(1);
                        wordLine = wordLine.substring(2, wordLine.length()).trim();
                        for (Word word : mListAllWords) {
                            if (TextUtils.equals(word.getEnglishSpell(), wordLine)) {
                                word.setExampleSentence(wordSentence);
                                needUploadWords.add(word);
                                total++;
                                Log.d(TAG, "loadFile: word=" + wordLine + ",Sentence=" + wordSentence);
                                break;
                            }
                        }
                        lines.clear();
                    }
                }
                lines.add(line);
            }
        } catch (Exception e) {
            Log.d(TAG, "loadFile: Exception=" + e.getMessage());
        }

        showDialog("共更新单词数：" + total);

    }

    private void updateWords() {
        if (needUploadWords == null) {
            Toast.makeText(this, "空", Toast.LENGTH_SHORT).show();
            return;
        }
//        Word word = needUploadWords.get(0);
//        needUploadWords = new ArrayList<>();
//        needUploadWords.add(word);
        Observable.fromIterable(needUploadWords)
                .distinct(new Function<Word, Integer>() {
                    @Override
                    public Integer apply(Word word) {
                        Log.d(TAG, "updateWords--apply: word=" + word.getEnglishSpell());
                        return word.id;
                    }
                })
                .concatMap(new Function<Word, ObservableSource<ResponseBody>>() {
                    @Override
                    public ObservableSource<ResponseBody> apply(Word word) {
                        return ApiClient.getInstance().updateWord(word);
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
                        uploadWordTotalNumber++;
                        Log.d(TAG, "updateWords--onNext: =");
                        try {
                            String string = responseBody.string();
                            Log.d(TAG, "updateWords--onNext: =" + string);
                        } catch (IOException e) {
                            uploadAudioFailedNumber++;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "updateWords--onError:getMessage =" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        showDialog("更新成功：共：" + uploadWordTotalNumber +
                                "\n失败：" + uploadAudioFailedNumber);
                    }
                });
    }

    private void preview() {
        mStringBuilder.setLength(0);

        if (needUploadWords == null) {
            Toast.makeText(this, "空", Toast.LENGTH_SHORT).show();
            return;
        }
        Observable.fromIterable(needUploadWords)
                .distinct(new Function<Word, Integer>() {
                    @Override
                    public Integer apply(Word word) {
                        return word.id;
                    }
                })
                .subscribe(new Observer<Word>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Word word) {
                        mStringBuilder.append(word.toString());
                        mStringBuilder.append("\n\n");
                        Log.d(TAG, "onNext: word=" + word);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(UpdateWordActivity.this, "error!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        mTvPreview.setText(mStringBuilder.toString());
                    }
                });
    }


    private void uploadAudios(String audioDir) {
        if (mListAllWords == null || mListAllWords.isEmpty()) {
            showDialog("No mListAllWords!");
            return;
        }

        File audioFile = new File(audioDir);
        if (!audioFile.exists() || !audioFile.isDirectory()) {
            showDialog("音频目录错误！audioDir=" + audioDir);
            return;
        }

        uploadAudioTotalNumber = 0;
        uploadAudioFailedNumber = 0;
        uploadAudioSkipNumber = 0;
        uploadAudioWordNotFoundNumber = 0;
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
                        boolean wordExist = false;
                        for (int i = mListAllWords.size() - 1; i >= 0; i--) {
                            Word word = mListAllWords.get(i);
                            String name = file.getName();
                            name = name.substring(0, name.lastIndexOf("."));
                            String spell = word.getEnglishSpell();
                            //约定：hello.mp3, hello1.mp3
                            if (TextUtils.equals(spell, name) || TextUtils.equals(spell + 1, name)) {
                                wordExist = true;
                                needUploadWords.add(word);
                                break;
                            }
                        }

                        if (!wordExist) {
                            uploadAudioSkipNumber++;
                        }
                        Log.d(TAG, "uploadAudios--test: wordExist=" + wordExist + ",getName=" + file.getName());
                        return wordExist;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<File>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(File file) {
                        Log.d(TAG, "uploadAudios--onNext: file=" + file.getPath());
                        uploadFile(file.getPath());
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

//        uploadFile(audioDir);
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
                        mListAllWords = wordResultListInfo.getData();
                    }

                    @Override
                    public void onError(Throwable e) {
                        showDialog("listAll onError=" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        showDialog("单词列表获取成功！共：" + mListAllWords.size());
                    }
                });
    }


    private void uploadFile(String path) {
        if (mOssTokenInfo == null || mOss == null) {
            showDialog("mOssTokenInfo or mOss不存在");
            return;
        }

        final File file = new File(path);
        if (!file.exists()) {
            showDialog("file 不存在 name=" + file.getName() + "\nwordFilePath=" + file.getPath());
            return;
        }

        if (mListAllWords == null || mListAllWords.isEmpty()) {
            showDialog("请先获取线上单词列表");
            return;
        }

//        UUID uuid = UUID.randomUUID();
        String objectKey = null;
        if (file.getName().endsWith(".mp3")) {
            objectKey = "courseware/audio/" + file.getName();
        } else {
            objectKey = "courseware/image/" + file.getName();
        }
        Log.d(TAG, "uploadFile: objectKey=" + objectKey);

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
                if ("mp3".equals(suffix)) {//mp3
                    if (mListAllWords != null) {
                        boolean find = false;
                        for (int i = mListAllWords.size() - 1; i >= 0; i--) {
                            Word word = mListAllWords.get(i);
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
                            Log.d(TAG, "uploadFile--onSuccess-find: objectKey=" + objectKey);
                        } else {
                            Log.e(TAG, "uploadFile--onSuccess-mWordListFile-not-find: objectKey=" + objectKey);
                        }

                    }
                } else if ("png".equals(suffix)) {//image
                    if (mListAllWords != null) {
                        String wordSpell = fileName.substring(0, fileName.indexOf("-"));
                        for (int i = mListAllWords.size() - 1; i >= 0; i--) {
                            Word word = mListAllWords.get(i);
//                            egg-rect-right-pic-3

                            if (TextUtils.equals(word.getEnglishSpell().toLowerCase(), wordSpell.toLowerCase())) {
                                if (fileName.contains("square-pic")) {
                                    word.setImage(url);
                                }

                                //默认取第一个
                                if (fileName.contains("rect-right-pic-1")) {
                                    word.setRectangleImage(url);
                                }
                                needUploadWords.add(word);
                                break;
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
}
