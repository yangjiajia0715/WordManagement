package com.example.yangj.wordmangementandroid.activitiy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yangj.wordmangementandroid.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yangjiajia on 2018/6/22.
 */
public class FileSelectorActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    @BindView(R.id.list_view)
    ListView mListView;
    List<String> mDatas = new ArrayList<>();
    List<String> mDatasShow = new ArrayList<>();
    @BindView(R.id.tv_select_tips)
    TextView mTvSelectTips;
    @BindView(R.id.tv_select_count)
    TextView mTvSelectCount;
    private ArrayAdapter<String> mAdapter;
    private String mCurPath;

    public static void startForResult(Activity activity, int reqCode) {
        Intent starter = new Intent(activity, FileSelectorActivity.class);
        activity.startActivityForResult(starter, reqCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selector);
        ButterKnife.bind(this);
        setTitle("文件选择器");
        mTvSelectTips.setText("长按选择，返回即可！");
        initView();
        initData();
    }

    @Override
    void initView() {
        mAdapter = new ArrayAdapter<String>(this
                , android.R.layout.simple_list_item_1, mDatasShow);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
    }

    @Override
    void initData() {
        File file = new File(BASE_PATH);
        if (!file.exists()) {
            showProgressDialog("根目录不存在：" + file.getName());
            return;
        }

        fillDatas(BASE_PATH);
    }

    void fillDatas(String path) {
        mCurPath = path;

        mDatas.clear();
        mDatasShow.clear();
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            Toast.makeText(this, "空列表！", Toast.LENGTH_SHORT).show();
            return;
        }

        for (File file1 : files) {
            mDatas.add(file1.getPath());
        }

        //不起作用
        Collections.sort(mDatas, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                File file1 = new File(o1);
                File file2 = new File(o2);

                if (file1.isFile()) {
                    return -1;
                }

                if (file1.isDirectory()) {
                    return 1;
                }

                if (file2.isFile()) {
                    return -1;
                }

                if (file2.isDirectory()) {
                    return 1;
                }
                return 0;
            }
        });

        for (String pathItem : mDatas) {
            mDatasShow.add(new File(pathItem).getName());
        }

        mAdapter.notifyDataSetChanged();

        mTvSelectCount.setText("共" + mDatasShow.size() + "条数据");

        File fileBase = new File(BASE_PATH);
        File fileCur = new File(mCurPath);
        if (fileBase.getName().equals(fileCur.getName())) {
            setTitle("文件选择器");
        } else {
            setTitle(fileCur.getName());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String path = mDatas.get(position);
        File file = new File(path);
        if (file.isDirectory()) {
            fillDatas(path);
        } else {
            Toast.makeText(this, "文件" + file.getName(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String path = mDatas.get(position);
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TEXT, path);
        setResult(RESULT_OK, intent);
        Toast.makeText(this, "已选择：" + new File(path).getName(), Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (TextUtils.isEmpty(mCurPath)) {
            finish();
            return;
        }
        File file = new File(BASE_PATH);
        File fileCur = new File(mCurPath);
        if (file.getName().equals(fileCur.getName())) {
            finish();
        } else {
            fillDatas(fileCur.getParent());
        }


    }

    @OnClick(R.id.tv_select_count)
    public void onViewClicked() {
    }
}
