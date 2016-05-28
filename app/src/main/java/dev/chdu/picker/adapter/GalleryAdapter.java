package dev.chdu.picker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.chdu.picker.R;
import dev.chdu.picker.util.ImageLoader;

public class GalleryAdapter extends BaseAdapter {

    private String mDirPath;
    private List<String> mDatas;
    private LayoutInflater mInflater;
    private static Set<String> mSelectedImage = new HashSet<>();
    private int mScreenWidth;

    public GalleryAdapter(Context context, List<String> datas, String dirPath) {
        mDirPath = dirPath;
        mDatas = datas;
        mInflater = LayoutInflater.from(context);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public String getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String filePath = mDirPath + "/" + getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mImage = (ImageView) convertView.findViewById(R.id.image_view_item);
            viewHolder.selectBtn = (ImageButton) convertView.findViewById(R.id.image_button_select);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mImage.setImageResource(R.drawable.pictures_no);
        viewHolder.selectBtn.setImageResource(R.drawable.picture_unselected);
        viewHolder.mImage.setColorFilter(null);

        viewHolder.mImage.setMaxWidth(mScreenWidth / 3);
        ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(filePath, viewHolder.mImage);

        viewHolder.mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedImage.contains(filePath)) {
                    mSelectedImage.remove(filePath);
                    viewHolder.mImage.setColorFilter(null);
                    viewHolder.selectBtn.setImageResource(R.drawable.picture_unselected);
                } else {
                    mSelectedImage.add(filePath);
                    viewHolder.mImage.setColorFilter(Color.parseColor("#77000000"));
                    viewHolder.selectBtn.setImageResource(R.drawable.pictures_selected);
                }
            }
        });
        if (mSelectedImage.contains(filePath)) {
            viewHolder.mImage.setColorFilter(Color.parseColor("#77000000"));
            viewHolder.selectBtn.setImageResource(R.drawable.pictures_selected);
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView mImage;
        ImageButton selectBtn;
    }
}
