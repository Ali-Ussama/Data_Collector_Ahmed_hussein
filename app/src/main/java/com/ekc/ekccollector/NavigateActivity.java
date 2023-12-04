package com.ekc.ekccollector;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigateActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.data_collection_card_view)
    LinearLayout mDataCollectionCV;

    @BindView(R.id.data_check_card_view)
    LinearLayout mDataCheckCV;

    NavigateActivity mCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_navigate);

            init();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            mCurrent = NavigateActivity.this;

            ButterKnife.bind(mCurrent);

            mDataCollectionCV.setOnClickListener(mCurrent);

            mDataCheckCV.setOnClickListener(mCurrent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.equals(mDataCollectionCV)) {
                handleDataCollection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDataCollection() {
        try {
            Intent intent = new Intent(mCurrent, com.ekc.ekccollector.collector.view.activities.map.MapActivity.class);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
