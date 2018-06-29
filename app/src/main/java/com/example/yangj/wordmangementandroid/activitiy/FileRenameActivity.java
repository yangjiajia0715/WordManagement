package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangj.wordmangementandroid.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yangjiajia on 2018/6/29.
 */
public class FileRenameActivity extends BaseActivity {
    private static final int REQ_SELECT_RENAME = 755;
    private static final String TAG = "FileRenameActivity";

    @BindView(R.id.tv_rename_path)
    TextView mTvRenamePath;
    @BindView(R.id.btn_select_rename_file)
    Button mBtnSelectRenameFile;
    @BindView(R.id.btn_select_rename_file_2)
    Button mBtnSelectRenameFile2;
    @BindView(R.id.btn_select_rename_file_3)
    Button mBtnSelectRenameFile3;
    @BindView(R.id.tv_message)
    TextView mTvMessage;
    private String mNeedRenameDir;

    public static void start(Context context) {
        Intent starter = new Intent(context, FileRenameActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_rename);
        ButterKnife.bind(this);
        setTitle("重命名文件");
        FileSelectorActivity.startForResult(this, REQ_SELECT_RENAME);
        initData();
    }

    @Override
    void initData() {

    }

    @OnClick({R.id.btn_select_rename_file, R.id.btn_select_rename_file_2, R.id.btn_select_rename_file_3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_select_rename_file:
                if (checkDir(mNeedRenameDir)) {
                    rename(mNeedRenameDir);
                }
                break;
            case R.id.btn_select_rename_file_2:
                break;
            case R.id.btn_select_rename_file_3:
                break;
        }
    }

    private void rename(String needRenameDir) {
        File file = new File(needRenameDir);
        File[] files = file.listFiles();
        for (File fileWord : files) {
            String name = fileWord.getName().trim();
            String wordSpell = name.substring(name.indexOf(" ")).trim();

            File[] listFiles = fileWord.listFiles();
            if (listFiles == null) {
                continue;
            }

            boolean existSquare = false;
            boolean existRectError = false;
            boolean existRectRight = false;
            int index = 0;
            for (File listFile : listFiles) {
                String fileName = listFile.getName().trim();
                if (fileName.endsWith(".png")) {//只重命名文件
                    if (fileName.startsWith("00") || fileName.contains("rect-wrong-pic")) {
                        File fileNew = new File(fileWord, wordSpell + "-rect-wrong-pic.png");
                        Log.d(TAG, "rename: " + fileNew.getName());
                    } else {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(listFile.getPath(), options);
                        if (options.outWidth == options.outHeight) {//正方形
                            File fileNew = new File(fileWord, wordSpell + "-square-pic.png");
                            Log.d(TAG, "rename: " + fileNew.getName());
                        } else {
                            index++;
                            File fileNew = new File(fileWord, wordSpell + "-rect-wrong-pic"+index + ".png");
                            Log.d(TAG, "rename: " + fileNew.getName());
                        }
                    }
                }
            }
        }

    }

    boolean checkDir(String path) {
        if (TextUtils.isEmpty(path)) {
            showAlertDialog("请选择目录！");
            return false;
        }

        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            showAlertDialog("不是目录！" + file.getName());
            return false;
        }

        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            showAlertDialog("空目录！" + file.getName());
            return false;
        }

        for (File fileWord : files) {
            String name = fileWord.getName().trim();
            String[] split = name.split(" ");
            if (split.length < 2) {
                showAlertDialog("空格分割项小于2！" + name);
                return false;
            }

            int parseInt = Integer.parseInt(split[0]);
            if (parseInt <= 0) {
                showAlertDialog("空格Number需要大于0！" + name);
                return false;
            }

            String wordSpell = name.substring(split[0].length()).trim();
            //todo
            if (TextUtils.isEmpty(wordSpell)) {
                showAlertDialog("截取单词拼写失败：" + name);
                return false;
            }

            File[] listFiles = fileWord.listFiles();


        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "未选择", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (requestCode) {
            case REQ_SELECT_RENAME:
                String path = data.getStringExtra(Intent.EXTRA_TEXT);
                File fileWord = new File(path);
                if (!fileWord.exists() || !fileWord.isDirectory()) {
                    showProgressDialog("文件不存在，或者不是目录：" + fileWord.getName());
                    return;
                }
                mNeedRenameDir = path;
                mTvRenamePath.setText("需要重命名的文件所在的文件夹：" + fileWord.getName());
                break;
        }
    }
}
