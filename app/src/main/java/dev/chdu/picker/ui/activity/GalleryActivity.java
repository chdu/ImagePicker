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
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.chdu.picker.R;
import dev.chdu.picker.adapter.GalleryAdapter;
import dev.chdu.picker.bean.FolderBean;
import dev.chdu.picker.ui.window.ImageDirPopupWindow;

public class GalleryActivity extends AppCompatActivity {

    private GridView mGridView;
    private GalleryAdapter mAdapter;
    private RelativeLayout mLayoutBottom;
    private TextView mTextViewDirName;
    private TextView mTextViewDirCount;
    private ProgressDialog mProgressDialog;
    private ImageDirPopupWindow mPopupWindow;

    private File mCurrentDir;
    private int mMaxCount;
    private List<String> mImages;
    private List<FolderBean> mFolderBeans = new ArrayList<>();

    private static final int DATA_LOADED = 0x110;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DATA_LOADED) {
                mProgressDialog.dismiss();
                dataToView();
                initDirPopupWindow();
            }
        }
    };

    private void initDirPopupWindow() {
        mPopupWindow = new ImageDirPopupWindow(this, mFolderBeans);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lightOn();
            }
        });
        mPopupWindow.setOnDirSelectedListener(new ImageDirPopupWindow.OnDirSelectedListener() {
            @Override
            public void onDirSelected(FolderBean bean) {
                mCurrentDir = new File(bean.getDir());
                mImages = Arrays.asList(mCurrentDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png");
                    }
                }));
                mAdapter = new GalleryAdapter(GalleryActivity.this, mImages, mCurrentDir.getAbsolutePath());
                mGridView.setAdapter(mAdapter);
                mTextViewDirCount.setText(mMaxCount + "");
                mTextViewDirName.setText(mCurrentDir.getName());
                mPopupWindow.dismiss();
            }
        });
    }

    private void lightOn() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
    }

    private void dataToView() {
        if (mCurrentDir == null) {
            Toast.makeText(this, "未扫描到任何图片", Toast.LENGTH_SHORT).show();
            return;
        }
        mImages = Arrays.asList(mCurrentDir.list());
        mAdapter = new GalleryAdapter(this, mImages, mCurrentDir.getAbsolutePath());
        mGridView.setAdapter(mAdapter);
        mTextViewDirCount.setText(mMaxCount + "");
        mTextViewDirName.setText(mCurrentDir.getName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        intiView();
        initDate();
        initEvent();
    }

    private void intiView() {
        mGridView = (GridView) findViewById(R.id.grid_view);
        mLayoutBottom = (RelativeLayout) findViewById(R.id.relative_layout_bottom);
        mTextViewDirName = (TextView) findViewById(R.id.text_view_dir_name);
        mTextViewDirCount = (TextView) findViewById(R.id.text_view_dir_count);
    }

    /**
     * 利用ContentProvider扫描手机图片
     */
    private void initDate() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(GalleryActivity.this, "当前存储卡不可用！", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressDialog = ProgressDialog.show(this, null, "正在加载...");

        new Thread() {
            @Override
            public void run() {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver cr = GalleryActivity.this.getContentResolver();
                Cursor cursor = cr.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + " =? or " + MediaStore.Images.Media.MIME_TYPE + " =? ",
                        new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

                Set<String> mDirPaths = new HashSet<>();
                while (cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null) {
                        continue;
                    }
                    String dirPath = parentFile.getAbsolutePath();
                    FolderBean folderBean;
                    if (mDirPaths.contains(dirPath)) {
                        continue;
                    } else {
                        mDirPaths.add(dirPath);
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
                    if (imageCount > mMaxCount) {
                        mMaxCount = imageCount;
                        mCurrentDir = parentFile;
                    }
                }
                cursor.close();

                mHandler.sendEmptyMessage(DATA_LOADED);
            }
        }.start();
    }

    private void initEvent() {
        mLayoutBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.showAsDropDown(mLayoutBottom, 0, 0);
                lightOff();
            }
        });
    }

    private void lightOff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;
        getWindow().setAttributes(lp);
    }

}
