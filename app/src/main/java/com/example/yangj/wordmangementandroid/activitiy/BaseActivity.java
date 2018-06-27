package com.example.yangj.wordmangementandroid.activitiy;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.example.yangj.wordmangementandroid.MyApp;
import com.example.yangj.wordmangementandroid.common.OssTokenInfo;
import com.example.yangj.wordmangementandroid.common.Word;
import com.example.yangj.wordmangementandroid.common.WordLoad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangjiajia on 2018/6/22.
 */
public abstract class BaseActivity extends AppCompatActivity {
    OSS mOss;
    OssTokenInfo mOssTokenInfo;
    private ProgressDialog mProgressDialog;
    private static final String TAG = "BaseActivity";
    static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "wordManagement" + File.separator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp app = (MyApp) getApplication();
        mOssTokenInfo = app.getOssTokenInfo();
        mOss = app.getOss();
    }

    void initView() {

    }

    void initData() {

    }

    void showProgressDialog() {
        showProgressDialog(null);
    }

    void showProgressDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
        }
        if (TextUtils.isEmpty(msg)) {
            mProgressDialog.setMessage("加载中，请稍后");
        } else {
            mProgressDialog.setMessage(msg);
        }
        mProgressDialog.show();
    }

    void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }

    void showAlertDialog(String msg) {
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

    void uploadFile(String path, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {
        if (mOssTokenInfo == null || mOss == null) {
            showProgressDialog("mOssTokenInfo or mOss不存在");
            return;
        }

        final File file = new File(path);
        if (!file.exists()) {
            showProgressDialog("file 不存在 name=" + file.getName() + "\nwordFilePath=" + file.getPath());
            return;
        }

        String objectKey = null;
        if (file.getName().endsWith(".mp3")) {
            objectKey = "courseware/audio/" + file.getName();
        } else {
            objectKey = "courseware/image/" + file.getName();
        }
        Log.d(TAG, "uploadFile: objectKey=" + objectKey);

        PutObjectRequest putObjectRequest = new PutObjectRequest(mOssTokenInfo.getBucket()
                , objectKey, path);

        mOss.asyncPutObject(putObjectRequest, completedCallback);

        try {
            mOss.putObject(putObjectRequest);
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    List<Word> parseFile(String path, List<WordLoad> wordLoadList) {
        if (TextUtils.isEmpty(path)) {
            showAlertDialog("请选择要解析的文件！");
            return null;
        }

        if (wordLoadList == null) {
            showAlertDialog("未指定wordLoadList！");
            return null;
        }

        File file = new File(path);

        if (!file.exists() || !file.isFile()) {
            Toast.makeText(this, "文件不存在" + file.getName(), Toast.LENGTH_SHORT).show();
            return null;
        }

        //检查文件格式！！！
        boolean rightFormat = checkWordFileFormat(file);
        if (!rightFormat) {
            return null;
        }

        List<Word> wordList = new ArrayList<>();
        BufferedReader bufferedReader = null;
        try {
            FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            int index = 0;
            int indexOf;
            Word word = null;
            WordLoad wordLoad = null;
            //之前会校验文件格式，所以这里大胆使用
            while ((line = bufferedReader.readLine()) != null) {
                String lineStr = line.trim();
                index++;
                Log.d(TAG, "loadFile: line=" + lineStr);
                switch (index) {
                    case 1:
                        word = new Word();
                        wordLoad = new WordLoad();
                        String numberStr = lineStr.substring(0, lineStr.indexOf(" "));
                        if (!TextUtils.isDigitsOnly(numberStr)) {
                            throw new IllegalArgumentException("格式错误！");
                        }
                        word.id = Integer.parseInt(numberStr);
                        indexOf = lineStr.lastIndexOf(" ");
                        Log.d(TAG, "loadFile: lineStrStr=" + numberStr.length() + "--" + numberStr);
                        word.setEnglishSpell(lineStr.substring(numberStr.length(), indexOf).trim());//trim
                        word.setMeaning(lineStr.substring(indexOf, lineStr.length()).trim());//trim
                        word.setChineseSpell(word.getMeaning());

                        wordLoad.word = word.getEnglishSpell();
                        break;
                    case 2:
                        int index1 = lineStr.lastIndexOf("例句0：");
                        if (index1 >= 0) {
                            word.setExampleSentence(lineStr.substring("例句0：".length(), lineStr.length()).trim());
                        } else {
                            word.setExampleSentence(lineStr.trim());
                        }
                        break;
                    case 3:
                        wordLoad.wordChineseWrong = lineStr.substring("中文干扰项1：".length(), lineStr.length()).trim();
                        break;
                    case 4:
                        wordLoad.wordEnglishWrong = lineStr.substring("英文干扰项3：".length(), lineStr.length()).trim();
                        break;
                    case 5:
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
                        break;
                    case 6:
                        String splitError = lineStr.substring("分割干扰项4：".length(), lineStr.length()).trim();
                        String[] splitErros = splitError.split(" ");

                        List<String> wrongOptions1 = new ArrayList<>();
                        List<String> wrongOptions2 = new ArrayList<>();

                        for (String s : splitErros) {
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

                        wordLoadList.add(wordLoad);
                        wordList.add(word);
                        break;
                    default:
                        throw new IllegalMonitorStateException("单词信息多余6行");
                }
                if (index == 6) {
                    index = 0;
                }
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

    /**
     * 检查文件格式是否满足要求:
     * 1、文件不能有空行
     * 2、每个单词有6行信息组成，且顺序不能变
     * 3、每个单词的开头行 序列号和单词之间得有空格
     * 例如：
     * 78 favourite 最喜爱的
     * What’s your favourite?
     * 中文干扰项1：不喜欢的
     * 英文干扰项3：favurite
     * 分割正确项2：favou  rite
     * 分割干扰项4：fevou  rate  favu  rete
     */
    private boolean checkWordFileFormat(File file) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            int index = 0;
            int lineNumber = 0;

            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;
                if (TextUtils.isEmpty(line)) {
                    showAlertDialog("文件格式不正确：\n不能含空行！\n行号：" + lineNumber);
                    return false;
                }

                line = line.trim();
                index++;
                int indexOf;
                switch (index) {
                    case 1://第1行：
                        //序列号：
                        indexOf = line.indexOf(" ");
                        if (indexOf == -1) {
                            showAlertDialog("文件格式不正确：\n序列号和单词之间得有空格\n行号：" + lineNumber);
                            return false;
                        }
                        String number = line.substring(0, line.indexOf(" "));
                        if (!TextUtils.isDigitsOnly(number)) {
                            showAlertDialog("文件格式不正确：\n序列号和单词之间得有空格！\n或者前两个单词信息不够6行。\n行号：" + lineNumber);
                            return false;
                        }
                        int lastIndexOf = line.lastIndexOf(" ");
                        String wordSpell = line.substring(number.length(), lastIndexOf).trim();
                        wordSpell = wordSpell.replaceAll(" ", "");
                        wordSpell = wordSpell.replaceAll("-", "");
                        boolean matches = wordSpell.matches("^[a-zA-Z]*");
                        if (!matches) {
                            showAlertDialog("文件格式不正确：\n单词含有非英文字符！\n行号：" + lineNumber);
                        }
                        break;
                    case 2://第2行：
                        break;
                    case 3://第3行：
                        indexOf = line.indexOf("中文干扰项1：");
                        if (indexOf == -1) {
                            showAlertDialog("文件格式不正确：\n没有以'中文干扰项1：'开头\n行号：" + lineNumber);
                            return false;
                        }
                        break;
                    case 4://第4行：
                        indexOf = line.indexOf("英文干扰项3：");
                        if (indexOf == -1) {
                            showAlertDialog("文件格式不正确：\n没有以'英文干扰项3：'开头\n行号：" + lineNumber);
                            return false;
                        }
                        break;
                    case 5://第5行：
                        indexOf = line.indexOf("分割正确项2：");
                        if (indexOf == -1) {
                            showAlertDialog("文件格式不正确：\n没有以'分割正确项2：'开头\n行号：" + lineNumber);
                            return false;
                        }
                        String rightSplit = line.substring("分割正确项2：".length(), line.length()).trim();
                        String[] split = rightSplit.split(" ");

                        int count1 = 0;
                        for (String s : split) {
                            if (!TextUtils.isEmpty(s)) {
                                count1++;
                            }
                        }
                        if (count1 < 2) {
                            showAlertDialog("文件格式不正确：\n分割正确项 少于两项\n行号：" + lineNumber);
                            return false;
                        }
                        break;
                    case 6://第6行：
                        indexOf = line.indexOf("分割干扰项4：");
                        if (indexOf == -1) {
                            showAlertDialog("文件格式不正确：\n没有以'分割干扰项4：'开头\n行号：" + lineNumber);
                            return false;
                        }
                        String errorSplit = line.substring("分割正确项2：".length(), line.length()).trim();
                        String[] split2 = errorSplit.split(" ");
                        int count2 = 0;
                        for (String s : split2) {
                            if (!TextUtils.isEmpty(s)) {
                                count2++;
                            }
                        }
                        if (count2 < 4) {
                            showAlertDialog("文件格式不正确：\n分割干扰项 少于4项\n行号：" + lineNumber);
                            return false;
                        }
                        break;
                    default:
                        showAlertDialog("文件格式不正确：\n单词信息多余6行\n行号：" + lineNumber);
                        return false;
                }

                if (index == 6) {
                    index = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
