package com.ekc.ekccollector.collector.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ekc.ekccollector.R;
import com.ekc.ekccollector.collector.model.models.OnlineQueryResult;
import com.ekc.ekccollector.collector.view.activities.map.MapActivity;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MultiResultRecAdapter extends RecyclerView.Adapter<MultiResultRecAdapter.viewHolder> {

    private static final String TAG = "MultiResultRecAdapter";
    private ArrayList<OnlineQueryResult> data;
    private MapActivity mCurrent;
    boolean mOnlineData;
    private OnlineQueryResult selectedResult;
    public MultiResultListener listener;
    public int selectedPosition = -1;

    public interface MultiResultListener {

        void onItemSelected(OnlineQueryResult onlineQueryResult, int position);

        void onEditItemSelected(OnlineQueryResult onlineQueryResult);
    }

    public MultiResultRecAdapter(ArrayList<OnlineQueryResult> data, MapActivity mCurrent, boolean onlineData, MultiResultListener listener) {
        this.data = data;
        this.mCurrent = mCurrent;
        this.mOnlineData = onlineData;
        this.listener = listener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        try {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_multi_result_row_item, parent, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder: data size = " + data.size());
        Log.i(TAG, "onBindViewHolder: layer = " + data.get(position).getFeatureLayer().getName());

        if (selectedResult != null) {
            if (data.get(position).getObjectID().equals(selectedResult.getObjectID()) && position == selectedPosition) {
                holder.mContainer.setBackgroundColor(mCurrent.getResources().getColor(R.color.bg_grey_light));
            } else {
                holder.mContainer.setBackgroundColor(mCurrent.getResources().getColor(R.color.white));
            }
        } else {
            holder.mContainer.setBackgroundColor(mCurrent.getResources().getColor(R.color.white));
        }

        setDrawable(holder, position);
        holder.mFeatureTitleTV.setText(data.get(position).getFeatureLayer().getName());

    }

    private void setDrawable(viewHolder holder, int position) {
        try {
            OnlineQueryResult result = data.get(position);
            if (mOnlineData) {
//                if (result.getFeatureLayer().equals(mCurrent.FCL_DistributionBoxLayer)) {
//                    holder.mFeatureTitleTV.setCompoundDrawablesRelative(mCurrent.getResources().getDrawable(R.drawable.rounded_ic_dist_box), null, null, null);
//                } else if (result.getFeatureLayer().equals(mCurrent.FCL_POLES_Layer)) {
//                    holder.mFeatureTitleTV.setCompoundDrawablesRelative(mCurrent.getResources().getDrawable(R.drawable.rounded_ic_poles), null, null, null);
//                } else if (result.getFeatureLayer().equals(mCurrent.LvOhCableLayer)) {
//                    holder.mFeatureTitleTV.setCompoundDrawablesRelative(mCurrent.getResources().getDrawable(R.drawable.rounded_ic_oh_lines), null, null, null);
//                } else if (result.getFeatureLayer().equals(mCurrent.MvOhCableLayer)) {
//                    holder.mFeatureTitleTV.setCompoundDrawablesRelative(mCurrent.getResources().getDrawable(R.drawable.rounded_ic_lv_panel), null, null, null);
//                } else if (result.getFeatureLayer().equals(mCurrent.SwitchgearAreaLayer)) {
//                    holder.mFeatureTitleTV.setCompoundDrawablesRelative(mCurrent.getResources().getDrawable(R.drawable.rounded_ic_rmu), null, null, null);
//                } else if (result.getFeatureLayer().equals(mCurrent.DynamicProtectiveDeviceLayer)) {
//                    holder.mFeatureTitleTV.setCompoundDrawablesRelative(mCurrent.getResources().getDrawable(R.drawable.rounded_ic_station), null, null, null);
//                } else if (result.getFeatureLayer().equals(mCurrent.FuseLayer)) {
//                    holder.mFeatureTitleTV.setCompoundDrawablesRelative(mCurrent.getResources().getDrawable(R.drawable.rounded_ic_substation), null, null, null);
//                } else if (result.getFeatureLayer().equals(mCurrent.LvdbAreaLayer)) {
//                    holder.mFeatureTitleTV.setCompoundDrawablesRelative(mCurrent.getResources().getDrawable(R.drawable.rounded_ic_mv_metering), null, null, null);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setSelectedResult(OnlineQueryResult onlineQueryResult, int position) {
        this.selectedResult = onlineQueryResult;
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.edit_mulit_result_row_item_feature_container)
        LinearLayout mContainer;

        @BindView(R.id.edit_mulit_result_row_item_feature_edit_ic)
        ImageView editIV;

        @BindView(R.id.edit_mulit_result_row_item_feature_title)
        TextView mFeatureTitleTV;

        viewHolder(@NonNull View itemView) {
            super(itemView);

            try {
                ButterKnife.bind(this, itemView);

                itemView.setOnClickListener(this);
                editIV.setOnClickListener(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            if (v.equals(editIV)) {
                handleEdit();
            } else {
                handleSelectItem();
            }
        }

        private void handleSelectItem() {
            try {
                if (listener != null) {
                    listener.onItemSelected(data.get(getAdapterPosition()), getAdapterPosition());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleEdit() {
            try {
                if (listener != null) {
                    listener.onEditItemSelected(data.get(getAdapterPosition()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
