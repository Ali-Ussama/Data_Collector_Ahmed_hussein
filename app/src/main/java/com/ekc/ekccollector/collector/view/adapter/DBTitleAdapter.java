package com.ekc.ekccollector.collector.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ekc.ekccollector.R;
import com.ekc.ekccollector.collector.view.activities.map.MapActivityListener;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DBTitleAdapter extends RecyclerView.Adapter<DBTitleAdapter.ViewHolder> {
    private ArrayList<String> titles;
    private HashMap<String, String> dbTitlesAndPaths;
    private MapActivityListener listener;

    public DBTitleAdapter(ArrayList<String> titles, HashMap<String, String> dbTitlesAndPaths, MapActivityListener listener) {
        this.titles = titles;
        this.dbTitlesAndPaths = dbTitlesAndPaths;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.db_title_row_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.dbTitleTv.setText(titles.get(position));
        holder.dbDeleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteDatabase(dbTitlesAndPaths.get(titles.get(position)), titles.get(position));
            }
        });

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDatabaseTitleSelected(titles.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public void updateDbList(String title) {
        try {
            titles.remove(title);
            dbTitlesAndPaths.remove(title);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.db_title_tv)
        TextView dbTitleTv;

        @BindView(R.id.db_title_delete_icon)
        ImageView dbDeleteIcon;

        @BindView(R.id.db_title_container)
        ConstraintLayout container;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
