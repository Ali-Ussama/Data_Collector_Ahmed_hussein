package com.ekc.ekccollector.collector.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ekc.ekccollector.R;
import com.ekc.ekccollector.collector.model.models.ImageBody;
import com.ekc.ekccollector.collector.view.activities.uploadImages.UploadImageListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UploadImageRecAdapter extends RecyclerView.Adapter<UploadImageRecAdapter.viewHolder> {
    private static final String TAG = "UploadImageRecAdapter";
    private List<ImageBody> data;
    private UploadImageListener viewListener;
    private Map<Integer, Integer> flags;
    private int uploadAction = 0;

    public UploadImageRecAdapter(List<ImageBody> data, Map<Integer, Integer> flags, UploadImageListener viewListener) {
        this.data = data;
        this.flags = flags;
        this.viewListener = viewListener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new viewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_image_rec_row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        try {
            Log.d(TAG, "onBindViewHolder: imageName " + data.get(position).getImageName());
//            Picasso.get().load(data.get(position).getCompressedFile()).into(holder.photoContent);

            if (flags.get(position) == 1) {
                Log.d(TAG, "onBindViewHolder: flag 1 - position = " + position);
                holder.photoContent.setAlpha(1f);
                holder.progressBar2.setVisibility(View.INVISIBLE);
                holder.icCheck2.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "onBindViewHolder: flag 0 - position = " + position);
                holder.photoContent.setAlpha(0.5f);
                holder.icCheck2.setVisibility(View.INVISIBLE);
                if (uploadAction == 1) {
                    holder.progressBar2.setVisibility(View.VISIBLE);
                }else{
                    holder.progressBar2.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDoneImage(int position) {
        if (position >= 0) {
            flags.put(position, 1);
            Log.d(TAG, "setDoneImage: position = " + position + " - flag value = " + flags.get(position));
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addItems(List<ImageBody> items) {
        this.data.addAll(items);
        notifyDataSetChanged();
    }

    public void setUploadAction(int uploadAction) {
        try {
            this.uploadAction = uploadAction;
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class viewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.upload_image_rec_row_item_image_name)
        TextView imageName;

        @BindView(R.id.upload_image_rec_row_item_progress_bar)
        ProgressBar progressBar;

        @BindView(R.id.iv_table_result)
        ImageView icCheck;

        //Second Experiment
        @BindView(R.id.upload_image_rec_row_item_photo_iv)
        ImageView photoContent;

        @BindView(R.id.upload_image_rec_row_item_progress_bar2)
        ProgressBar progressBar2;

        @BindView(R.id.iv_table_result2)
        ImageView icCheck2;


        viewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                ButterKnife.bind(this, itemView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}