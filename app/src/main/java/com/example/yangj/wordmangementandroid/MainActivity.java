package com.example.yangj.wordmangementandroid;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
                loadFile();
                break;
            case R.id.btn_update_word:
                break;
            case R.id.btn_update_questions:
                break;
        }
    }

    private void loadFile() {
//        \wordManagement
        String path = Environment.getExternalStorageDirectory().getPath()
                + File.separator + "wordManagement" + File.separator + "two_up.txt";
//        String pathOut = Environment.getExternalStorageDirectory().getPath()
//                + "wordManagement" + File.separator + "two_up_output.txt";
        File file = new File(path);
        File fileOutPut = new File(file.getParentFile(), "two_up_output.txt");
        Log.d(TAG, "loadFile: path=" + path);
        Log.d(TAG, "loadFile: file=" + file);
        if (!file.exists()) {
            Toast.makeText(this, "文件不存在" + file.getName(), Toast.LENGTH_SHORT).show();
            return;
        }
        BufferedReader bufferedReader = null;
        StringBuilder stringBuffer = new StringBuilder();
        try {
//            FileInputStream fileInputStream = new FileInputStream(file);
//            BufferedReader bufferedReader = new BufferedReader(new BufferedInputStream(fileInputStream));
//            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
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
    }

//    onp
}
