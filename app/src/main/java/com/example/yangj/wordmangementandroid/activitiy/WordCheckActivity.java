package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;

import com.example.yangj.wordmangementandroid.R;
import com.example.yangj.wordmangementandroid.common.ResultListInfo;
import com.example.yangj.wordmangementandroid.common.Word;
import com.example.yangj.wordmangementandroid.retrofit.ApiClient;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by yangjiajia on 2018/6/25.
 */
public class WordCheckActivity extends BaseActivity {
    private static final String TAG = "WordCheckActivity";
    
    @BindView(R.id.btn_word_check)
    Button mBtnWordCheck;
    @BindView(R.id.tv_check_word_result)
    TextView mTvCheckWordResult;
    private List<Word> mWordList;

    public static void start(Context context) {
        Intent starter = new Intent(context, WordCheckActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_check);
        ButterKnife.bind(this);
        listAll();
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
                        mWordList = wordResultListInfo.getData();
//                        showProgressDialog("listAll size=" + mListAllWordsRelease.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: e " + e.getMessage());
//                        showProgressDialog("listAll onError=" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        checkWord(mWordList);
                    }
                });
    }

    private void checkWord(List<Word> wordList) {
        if (wordList == null || wordList.isEmpty()) {
            showAlertDialog("列表空");
            return;
        }

        StringBuilder sb = new StringBuilder();
        StringBuilder sbTemp = new StringBuilder();
        for (Word word : wordList) {
            sbTemp.setLength(0);
            sbTemp.append(word.getEnglishSpell());
            sbTemp.append(" Id:");
            sbTemp.append(word.id);
            sbTemp.append("\n");
            boolean isComplete = true;
            String spell = word.getEnglishSpell();
            if (spell.length() != spell.trim().length()) {
                isComplete = false;
                sbTemp.append("单词首尾含有空格！");
                sbTemp.append("\n");
            }

            String meaning = word.getMeaning();
            if (TextUtils.isEmpty(meaning)) {
                isComplete = false;
                sbTemp.append("没有释义");
                sbTemp.append("\n");
            }

            String sentence = word.getExampleSentence();
            if (TextUtils.isEmpty(sentence)) {
                isComplete = false;
                sbTemp.append("没有释义");
                sbTemp.append("\n");
            }

            String pronunciation = word.getEnglishPronunciation();
            if (TextUtils.isEmpty(pronunciation)) {
                isComplete = false;
                sbTemp.append("没有单词发音");
                sbTemp.append("\n");
            }

            String sentenceAudio = word.getExampleSentenceAudio();
            if (TextUtils.isEmpty(sentenceAudio)) {
                isComplete = false;
                sbTemp.append("没有例句发音");
                sbTemp.append("\n");
            }

            String image = word.getImage();
            if (!URLUtil.isNetworkUrl(image)) {
                isComplete = false;
                sbTemp.append("没有正方形图片");
                sbTemp.append("\n");
            }

            String rectangleImage = word.getRectangleImage();
            if (!URLUtil.isNetworkUrl(rectangleImage)) {
                isComplete = false;
                sbTemp.append("没有长方形图片");
                sbTemp.append("\n");
            }

            if (!isComplete) {
                sb.append(sbTemp.toString());
            }
            sbTemp.append("\n");
        }
        mTvCheckWordResult.setText(sb.toString());
    }


    @OnClick(R.id.btn_word_check)
    public void onViewClicked() {
    }
}
