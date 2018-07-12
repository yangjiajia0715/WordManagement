package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
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
import com.example.yangj.wordmangementandroid.common.WordLoad;
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
import io.reactivex.functions.Consumer;
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
    private static final int REQ_SELECT_AUDIO_IMAGE_DIR = 11;
    private static final int REQ_SELECT_WORD_FILE = 12;

    @BindView(R.id.btn_upload_audio)
    Button mBtnUploadAudio;
    @BindView(R.id.btn_preview)
    Button mBtnPreview;
    @BindView(R.id.btn_update_word)
    Button mBtnUpdateWord;
    @BindView(R.id.tv_preview)
    TextView mTvPreview;
    @BindView(R.id.tv_audio_and_images_dir)
    TextView mTvAudioAndImagesDir;
    @BindView(R.id.btn_select_audio_and_images_dir)
    Button mBtnSelectAudioAndImagesDir;
    @BindView(R.id.tv_parse_file_path)
    TextView mTvParseFilePath;
    @BindView(R.id.btn_select_word_file)
    Button mBtnSelectWordFile;
    @BindView(R.id.btn_upload_word_image)
    Button mBtnUploadWordImage;

    private int uploadAudioSkipNumber = 0;
    private int uploadAudioTotalNumber = 0;
    private int uploadAudioFailedNumber = 0;

    private int uploadWordTotalNumber = 0;
    private int uploadWordSkipNumber = 0;
    private int uploadWordFailedNumber = 0;
    private int needUploadWordTotalNumber = 0;
    private int needUploadWordUnCompelteNumber = 0;

    int needUpdateQustionsSuccessNumber = 0;

    private int uploadAudioWordNotFoundNumber = 0;

    //一上 资源汇总31-94 ,音频已上传，单词已更新 2018-6-21 20:08:39
    private String mWordAudioAndImagesDir;
    private String mWordSentencePath = BASE_PATH + "一上31-94新例句.txt";
    private List<Word> mListAllWords;

    private OSS mOss;
    private OssTokenInfo mOssTokenInfo;
    private List<Word> needUploadWords = new ArrayList<>();
    private List<Question> needUpdateQustions = new ArrayList<>();
    private int uploadWordImageSuccessCount = 0;
    private int uploadWordImageFailCount = 0;

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
        setTitle("更新音频、单词图片");
        MyApp app = (MyApp) getApplication();
        mOssTokenInfo = app.getOssTokenInfo();
        mOss = app.getOss();
        listAllWord();
        listAllQuestions();
    }

    @OnClick({R.id.btn_upload_audio, R.id.btn_preview, R.id.btn_update_word
            , R.id.btn_select_audio_and_images_dir
            , R.id.btn_select_word_file
            , R.id.btn_load_sentence, R.id.btn_update_questions, R.id.btn_update_questions_api
            , R.id.btn_upload_word_image})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_select_word_file:
                FileSelectorActivity.startForResult(this, REQ_SELECT_WORD_FILE);
                break;
            case R.id.btn_select_audio_and_images_dir:
                FileSelectorActivity.startForResult(this, REQ_SELECT_AUDIO_IMAGE_DIR);
                break;
            case R.id.btn_upload_audio:
                uploadAudios(mWordAudioAndImagesDir);
                break;
            case R.id.btn_load_sentence:
                showAlertDialog("注意：只能解析仅仅有一个例句的文件");
                loadFileOnlySentence(mWordSentencePath);
                break;
            case R.id.btn_preview:
                previewNeedUpdateWord();
                break;
            case R.id.btn_update_word:
                updateWords();
                break;
            case R.id.btn_update_questions:
                updateQustions();
                break;
            case R.id.btn_update_questions_api:
                updateQustionsApi(needUpdateQustions);
                break;
            case R.id.btn_upload_word_image:
                uploadWordImages(mWordAudioAndImagesDir);
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
        uploadWordImageSuccessCount = 0;
        uploadWordImageFailCount = 0;
        needUploadWords.clear();
        if (TextUtils.isEmpty(imagePath)) {
            showAlertDialog("请先指定图片目录！");
            return;
        }
        File file = new File(imagePath);
        if (!file.exists()) {
            showAlertDialog("目录不存在：" + file.getPath());
            return;
        }

        File[] listFiles = file.listFiles();
        if (listFiles == null || listFiles.length == 0) {
            showAlertDialog("空目录：" + file.getPath());
            return;
        }

        for (File file1 : listFiles) {
            String file1Name = file1.getName();
            Log.d(TAG, "uploadWordImages: getName=" + file1Name);
        }

        showProgressDialog();
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
                        return file.getName().contains("square-pic")
                                || file.getName().contains("rect-right-pic-1");
                    }
                })
//                .take(2)/////////////////////////////
                .subscribe(new Observer<File>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(File file) {
                        Log.d(TAG, "onNext: getName=" + file.getName());
                        uploadWordImageSuccessCount++;
                        uploadFile(file.getPath());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");
                        uploadWordImageFailCount++;
                        showAlertDialog("上传失败");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                        hideProgressDialog();
                        showAlertDialog("上传完成\n成功：" + uploadWordImageFailCount
                                + "\n失败：" + uploadWordImageFailCount);
                    }
                });


    }

    private void updateQustionsApi(List<Question> needUpdateQustions) {
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
                        showAlertDialog("更新练习失败");
                    }

                    @Override
                    public void onComplete() {
                        showAlertDialog("更新练习成功 \n共：" + needUpdateQustionsSuccessNumber);
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

    private void loadFileOnlySentence(String wordSentencePath) {
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
                                Log.d(TAG, "loadFileOnlySentence: word=" + wordLine + ",Sentence=" + wordSentence);
                                break;
                            }
                        }
                        lines.clear();
                    }
                }
                lines.add(line);
            }
        } catch (Exception e) {
            Log.d(TAG, "loadFileOnlySentence: Exception=" + e.getMessage());
        }

        showProgressDialog("共更新单词数：" + total);

    }

    private void updateWords() {
        if (needUploadWords == null || needUploadWords.isEmpty()) {
            showAlertDialog("没有需要更新的单词！");
            return;
        }

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
                            uploadWordFailedNumber++;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "updateWords--onError:getMessage =" + e.getMessage());
                        showAlertDialog("更新单词失败：" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        showAlertDialog("更新单词成功：\n共：" + uploadWordTotalNumber +
                                "\n失败：" + uploadWordFailedNumber);
                    }
                });
    }

    private void previewNeedUpdateWord() {
        mStringBuilder.setLength(0);

        if (needUploadWords == null || needUploadWords.isEmpty()) {
            showAlertDialog("没有需要更新的单词");
            return;
        }
        needUploadWordTotalNumber = 0;
        needUploadWordUnCompelteNumber = 0;
        Disposable disposable = Observable.fromIterable(needUploadWords)
                .distinct(new Function<Word, Integer>() {
                    @Override
                    public Integer apply(Word word) {
                        return word.id;
                    }
                })
                .subscribe(new Consumer<Word>() {
                    @Override
                    public void accept(Word word) {
                        needUploadWordTotalNumber++;
                        if (!URLUtil.isNetworkUrl(word.getEnglishPronunciation())
                                || !URLUtil.isNetworkUrl(word.getExampleSentenceAudio())
                                || !URLUtil.isNetworkUrl(word.getImage())
                                || !URLUtil.isNetworkUrl(word.getRectangleImage())) {
                            needUploadWordUnCompelteNumber++;
                            mStringBuilder.append(word.id);
                            mStringBuilder.append(" ");
                            mStringBuilder.append(word.getEnglishSpell());
                            mStringBuilder.append(" 信息不全：音频、图片");
                            mStringBuilder.append("\n");
                        }
                    }
                });
        mStringBuilder.append("共");
        mStringBuilder.append(needUploadWordTotalNumber);
        mStringBuilder.append("个单词需要更新");
        mStringBuilder.append("\n\n");
        Observable.fromIterable(needUploadWords)
                .

                        distinct(new Function<Word, Integer>() {
                            @Override
                            public Integer apply(Word word) {
                                return word.id;
                            }
                        })
                .

                        subscribe(new Observer<Word>() {
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
                                WordListInfoActivity.start(UpdateWordActivity.this, mStringBuilder.toString());
                            }
                        });
    }


    private void uploadAudios(String audioDir) {
        if (TextUtils.isEmpty(audioDir)) {
            showAlertDialog("请先指定音频目录！");
            return;
        }

        if (mListAllWords == null || mListAllWords.isEmpty()) {
            showAlertDialog("No mListAllWords!");
            return;
        }

        File audioFile = new File(audioDir);
        if (!audioFile.exists() || !audioFile.isDirectory()) {
            showAlertDialog("音频目录错误！audioDir=" + audioDir);
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
            showProgressDialog("音频目录错误！audioDir=" + audioDir);
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
                        showAlertDialog("音频上传成功！"
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
                        showAlertDialog("listAll onError=" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
//                        showAlertDialog("单词列表获取成功！共：" + mListAllWords.size());
                    }
                });
    }


    private void uploadFile(String path) {
        if (mOssTokenInfo == null || mOss == null) {
            showAlertDialog("mOssTokenInfo or mOss不存在");
            return;
        }

        final File file = new File(path);
        if (!file.exists()) {
            showAlertDialog("file 不存在 name=" + file.getName() + "\nwordFilePath=" + file.getPath());
            return;
        }

        if (mListAllWords == null || mListAllWords.isEmpty()) {
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
        showProgressDialog();

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
                        hideProgressDialog();
                        Log.d(TAG, "listAllQuestions--onComplete");
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
            case REQ_SELECT_AUDIO_IMAGE_DIR:
                String audioAndImagePath = data.getStringExtra(Intent.EXTRA_TEXT);
                File fileWord = new File(audioAndImagePath);
                if (!fileWord.exists() || !fileWord.isDirectory()) {
                    showProgressDialog("文件不存在，或者不是目录：" + fileWord.getName());
                    return;
                }
                mWordAudioAndImagesDir = audioAndImagePath;
                mTvAudioAndImagesDir.setText("单词音频和图片所在的目录：" + fileWord.getName());
                break;
            case REQ_SELECT_WORD_FILE:
                String pathWord = data.getStringExtra(Intent.EXTRA_TEXT);
                File file = new File(pathWord);
                if (!file.exists() || !file.isFile()) {
                    showProgressDialog("文件不存在，或者不是文件：" + file.getName());
                    return;
                }
                List<WordLoad> wordLoadListParse = new ArrayList<>();
                List<Word> wordListParsed = parseFile(pathWord, wordLoadListParse);

                mTvParseFilePath.setText("需要解析的单词文件：" + file.getName());
                break;
        }
    }

}
