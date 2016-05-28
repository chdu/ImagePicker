package dev.chdu.picker.ui.window;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import dev.chdu.picker.R;
import dev.chdu.picker.bean.FolderBean;
import dev.chdu.picker.util.ImageLoader;

/**
 * Created by on 5/28/2016.
 */
public class ImageDirPopupWindow extends PopupWindow {

    private int mWidth;
    private int mHeight;
    private View mConvertView;
    private ListView mListView;

    private List<FolderBean> mDatas;

    public ImageDirPopupWindow(Context context, List<FolderBean> datas) {
        calWidthAndHeight(context);
        mConvertView = LayoutInflater.from(context).inflate(R.layout.popup_main, null);
        mDatas = datas;
        setContentView(mConvertView);
        setWidth(mWidth);
        setHeight(mHeight);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());

        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });

        initView(context);
        initEvent();
    }

    public interface OnDirSelectedListener {
        void onDirSelected(FolderBean bean);
    }

    private OnDirSelectedListener dirSelectedListener;

    public void setOnDirSelectedListener(OnDirSelectedListener listener) {
        dirSelectedListener = listener;
    }

    private void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListView != null) {
                    dirSelectedListener.onDirSelected(mDatas.get(position));
                }
            }
        });
    }

    private void initView(Context context) {
        mListView = (ListView) mConvertView.findViewById(R.id.list_view_dir);
        mListView.setAdapter(new ListDirAdapter(context, mDatas));
    }

    private void calWidthAndHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        mWidth = outMetrics.widthPixels;
        mHeight = (int) (outMetrics.heightPixels * 0.7);
    }

    private class ListDirAdapter extends ArrayAdapter<FolderBean> {
        private LayoutInflater inflater;

        public ListDirAdapter(Context context, List<FolderBean> objects) {
            super(context, 0, objects);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_popup_main, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.mImage = (ImageView) convertView.findViewById(R.id.image_view_item_image);
                viewHolder.mDirName = (TextView) convertView.findViewById(R.id.text_view_item_name);
                viewHolder.mPicNumber = (TextView) convertView.findViewById(R.id.text_view_item_count);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            FolderBean bean = getItem(position);
            viewHolder.mImage.setImageResource(R.drawable.pictures_no);
            ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(bean.getFirstImagePath(), viewHolder.mImage);
            viewHolder.mDirName.setText(bean.getName());
            viewHolder.mPicNumber.setText(bean.getCount() + "");

            return convertView;
        }

        private class ViewHolder {
            ImageView mImage;
            TextView mDirName;
            TextView mPicNumber;
        }

    }
}
