package com.example.yangj.wordmangementandroid.activitiy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
        initData();
    }

    @Override
    void initData() {

    }

    @OnClick({R.id.btn_select_rename_file, R.id.btn_select_rename_file_2, R.id.btn_select_rename_file_3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_select_rename_file:
                FileSelectorActivity.startForResult(this, REQ_SELECT_RENAME);
                break;
            case R.id.btn_select_rename_file_2:
                break;
            case R.id.btn_select_rename_file_3:
                break;
        }
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
                String pathWord = data.getStringExtra(Intent.EXTRA_TEXT);
                File fileWord = new File(pathWord);
//                if (!fileWord.exists() || !fileWord.isDirectory()) {
//                    showProgressDialog("文件不存在，或者不是目录：" + fileWord.getName());
//                    return;
//                }
                String path = "";
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                Log.d(TAG, "onActivityResult: getWidth=" + bitmap.getWidth());
                Log.d(TAG, "onActivityResult: getHeight=" + bitmap.getHeight());
                Log.d(TAG, "onActivityResult: getByteCount=" + bitmap.getByteCount());

                mTvRenamePath.setText("需要重命名的文件所在的文件夹：" + fileWord.getName());
                break;
        }
    }
}
