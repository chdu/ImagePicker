package dev.chdu.picker.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.chdu.picker.R;
import dev.chdu.picker.util.ImageLoader;

/**
 * Created on 6/1/2016.
 */
public class PickImageAdaptor extends RecyclerView.Adapter<PickImageAdaptor.ListItemViewHolder> {

    private LayoutInflater inflater;
    private List<String> items;
    private String dirPath;
    private static Set<String> selectedImages = new HashSet<>();

    public PickImageAdaptor(Context context, List<String> items, String dirPath) {
        if (items == null) {
            throw new IllegalArgumentException("items must not be null");
        }
        inflater = LayoutInflater.from(context);
        this.items = items;
        this.dirPath = dirPath;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_recyclerview, parent, false);
        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ListItemViewHolder holder, int position) {
        final String filePath = dirPath + "/" + items.get(position);
        holder.image.setImageResource(R.drawable.pictures_no);
        holder.image.setColorFilter(null);
        holder.selectBtn.setImageResource(R.drawable.picture_unselected);

        ImageLoader.getInstance(3, ImageLoader.Type.LIFO).loadImage(filePath, holder.image);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImages.contains(filePath)) {
                    selectedImages.remove(filePath);
                    holder.image.setColorFilter(null);
                    holder.selectBtn.setImageResource(R.drawable.picture_unselected);
                } else {
                    selectedImages.add(filePath);
                    holder.image.setColorFilter(Color.parseColor("#77000000"));
                    holder.selectBtn.setImageResource(R.drawable.pictures_selected);
                }
            }
        });
        if (selectedImages.contains(filePath)) {
            holder.image.setColorFilter(Color.parseColor("#77000000"));
            holder.selectBtn.setImageResource(R.drawable.pictures_selected);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static final class ListItemViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageButton selectBtn;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image_view_item_recycle);
            selectBtn = (ImageButton) itemView.findViewById(R.id.image_button_select_recycle);
        }
    }
}
