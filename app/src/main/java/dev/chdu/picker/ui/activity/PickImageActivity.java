package dev.chdu.picker.ui.activity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.chdu.picker.R;
import dev.chdu.picker.adapter.PickImageAdaptor;
import dev.chdu.picker.bean.FolderBean;

public class PickImageActivity extends BaseActivity {

    private static final int DATA_LOADED = 0x110;
    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;
    private List<FolderBean> mFolderBeans = new ArrayList<>();
    private int maxCount;
    private File currentDir;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DATA_LOADED) {
                mProgressDialog.dismiss();
                dataToView();
            }
        }
    };
    private List<String> mImages;
    private PickImageAdaptor mAdapter;

    private void dataToView() {
        if (currentDir == null) {
            showSnakebar(R.string.no_image);
            return;
        }
        mImages = Arrays.asList(currentDir.list());
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        if (mRecyclerView == null) {
            showSnakebar(R.string.error);
            return;
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new PickImageAdaptor(this, mImages, currentDir.getAbsolutePath());
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layoutResID = R.layout.activity_pick_image;

        super.onCreate(savedInstanceState);

        initView();
        initData();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    }

    private void initData() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            showSnakebar(R.string.no_sdCard);
            return;
        }
        mProgressDialog = ProgressDialog.show(this, null, getString(R.string.loading));

        new Thread() {
            @Override
            public void run() {
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver cr = PickImageActivity.this.getContentResolver();
                Cursor cursor = cr.query(uri, null,
                        MediaStore.Images.Media.MIME_TYPE + " =? or " + MediaStore.Images.Media.MIME_TYPE + " =? ",
                        new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

                Set<String> dirPaths = new HashSet<>();
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null) {
                        continue;
                    }
                    String dirPath = parentFile.getAbsolutePath();
                    FolderBean folderBean;
                    if (dirPaths.contains(dirPath)) {
                        continue;
                    } else {
                        dirPaths.add(dirPath);
                        folderBean = new FolderBean();
                        folderBean.setDir(dirPath);
                        folderBean.setFirstImagePath(path);
                    }
                    if (parentFile.list() == null) {
                        continue;
                    }
                    int imageCount = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            return filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png");
                        }
                    }).length;
                    folderBean.setCount(imageCount);

                    mFolderBeans.add(folderBean);
                    if (imageCount > maxCount) {
                        maxCount = imageCount;
                        currentDir = parentFile;
                    }
                }
                cursor.close();

                mHandler.sendEmptyMessage(DATA_LOADED);
            }
        }.start();
    }

}
