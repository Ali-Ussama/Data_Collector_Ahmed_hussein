package com.ekc.ekccollector.collector.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ekc.ekccollector.R;
import com.ekc.ekccollector.collector.view.activities.map.MapActivity;
import com.ekc.ekccollector.collector.view.fragments.EditFragListener;
import com.ekc.ekccollector.collector.view.utils.Utilities;
import com.github.clans.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AttachmentRVAdapter extends RecyclerView.Adapter<AttachmentRVAdapter.viewHolder> {


    private static final String TAG = "AttachmentRVAdapter";
    public List<File> data;
    public MapActivity mCurrent;
    public EditFragListener listener;

    public AttachmentRVAdapter(List<File> data, MapActivity mCurrent, EditFragListener listener) {
        this.data = data;
        this.mCurrent = mCurrent;
        this.listener = listener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offline_rv_row_item, parent, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new viewHolder(view);
    }

    public void addImageBitmap(File image) {
        try {
            data.add(image);
            notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshList(List<File> images, int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        try {
            Picasso.get().load(data.get(position)).into(holder.imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.offline_attachments_rv_row_item_image_view)
        ImageView imageView;

        @BindView(R.id.offline_attachments_rv_row_item_delete_fab)
        FloatingActionButton deleteImageFab;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                ButterKnife.bind(this, itemView);

                deleteImageFab.setOnClickListener(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            try {
                if (v.equals(deleteImageFab)) {
                    deleteImage(getAdapterPosition());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void deleteImage(int position) {
            try {
                Utilities.showConfirmationDialog(mCurrent, mCurrent.getString(R.string.are_you_sure_to_delete_image), mCurrent.getString(R.string.yes), mCurrent.getString(R.string.no), new Utilities.CallBack() {
                    @Override
                    public void OnPositiveClicked(MaterialDialog dlg) {
                        try {
                            dlg.dismiss();
                            File image = data.get(position);
                            if (listener != null) {
                                listener.onDeleteImage(image, position);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void OnNegativeClicked(MaterialDialog dlg) {
                        dlg.dismiss();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
