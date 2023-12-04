package com.ekc.ekccollector.collector.view.activities.uploadImages;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewAnimator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ekc.ekccollector.R;
import com.ekc.ekccollector.collector.model.models.ImageBody;
import com.ekc.ekccollector.collector.view.adapter.UploadImageRecAdapter;
import com.ekc.ekccollector.collector.view.fragments.DatePickerFragment;
import com.ekc.ekccollector.collector.view.utils.DateUtils;
import com.ekc.ekccollector.collector.view.utils.Utilities;
import com.mancj.slimchart.SlimChart;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UploadImageActivity extends AppCompatActivity implements UploadImageListener {

    @BindView(R.id.activity_upload_image_slim_chart)
    SlimChart slimChart;

    @BindView(R.id.activity_upload_image_view_animator)
    ViewAnimator viewAnimator;

    @BindView(R.id.activity_upload_image_date_picker_container)
    CardView datePickerContainer;

    @BindView(R.id.activity_upload_image_date_picker_btn)
    Button mDatePickerBtn;

    @BindView(R.id.activity_upload_image_header_lbl)
    TextView mUploadingHeader;

    @BindView(R.id.activity_upload_image_date_picker_tv)
    TextView mDatePickerTV;

    @BindView(R.id.activity_upload_image_name_lbl)
    TextView imageNameTV;

    @BindView(R.id.activity_upload_image_images_recycler)
    RecyclerView mImagesRV;

    @BindView(R.id.activity_upload_image_progress_status_tv)
    TextView progressStatusTV;

    @BindView(R.id.activity_upload_image_progress_target_tv)
    TextView progressTargetTV;

    @BindView(R.id.image_counts_progressBar)
    ProgressBar imagesCountProgressBar;

    UploadImageRecAdapter mAdapter;

    DatePickerFragment datePickerFragment;

    private static final String TAG = "UploadImageActivity";
    UploadImageActivity mCurrent;
    List<ImageBody> imageBodies;
    Map<Integer, Integer> map;
    UploadImagePresenter presenter;
    int progress = 0;
    int lastUploadImageIndex = 0;
    int dayOfMonth, month, year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        init();
    }

    private void init() {
        try {
            ButterKnife.bind(this);

            initToolbar();
            mCurrent = this;
            imageBodies = new ArrayList<>();
            presenter = new UploadImagePresenter(mCurrent, this);
            datePickerFragment = new DatePickerFragment(this);

            getTodayDate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getTodayDate() {
        try {
            String today = DateUtils.getCurrentFormattedDate(DateUtils.FORMAT_DATE_SLASH_UPLOAD);
            String[] split = today.split("/");
            dayOfMonth = Integer.parseInt(split[0]);
            month = Integer.parseInt(split[1]);
            year = Integer.parseInt(split[2]);
            String dayStr = String.valueOf(dayOfMonth);
            String monthStr = String.valueOf(month);
            String yearStr = String.valueOf(year);

            if (dayOfMonth <= 9) {
                dayStr = "0".concat(dayStr);
            }
            if (month <= 9) {
                monthStr = "0".concat(monthStr);
            }
            today = dayStr.concat("/").concat(monthStr).concat("/").concat(yearStr);
            onDatePickedUp(today, dayStr, monthStr, yearStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initToolbar() {
        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRecycler() {
        try {
            GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
            map = new HashMap<>(imageBodies.size());
            for (int i = 0; i < imageBodies.size(); i++) {
                map.put(i, 0);
            }
            mAdapter = new UploadImageRecAdapter(imageBodies, map, this);
            mImagesRV.setLayoutManager(layoutManager);
            mImagesRV.setNestedScrollingEnabled(true);
            mImagesRV.setAdapter(mAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDatePickedUp(String date, String dayOfMonth, String month, String year) {
        try {
            Log.d(TAG, "onDatePickedUp: is called");
            mDatePickerTV.setVisibility(View.VISIBLE);
            mDatePickerTV.setText(date);

            if (datePickerFragment.isVisible()) {
                datePickerFragment.dismiss();
            }
            Log.d(TAG, "onDatePickedUp: displaying upload child");
            viewAnimator.setDisplayedChild(0);

            if (presenter != null) {
                presenter.init(dayOfMonth, month, year);
                presenter.loadImages2(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageUploaded(int index) {
        try {
            lastUploadImageIndex = index;
            if (index <= imageBodies.size())
                handleProgress(index + 1);
            if (imageBodies != null && !imageBodies.isEmpty() && (index + 1) < imageBodies.size()) {
                presenter.uploadNextImage(index + 1);
            } else {
                Utilities.showToast(mCurrent, getString(R.string.images_uploaded_successfully));
//                progressBarLoading.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "onImageUploaded: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleProgress(int index) {
        try {
            Log.d(TAG, "handleProgress: is called");
            Log.d(TAG, "handleProgress: mIndex = " + (double) index);
            double x = (double) index / imageBodies.size();
            double y = x * 100;
//            progress = (int) ((mIndex / (imageBodies.size())) * 100);
            progress = (int) y;

            Log.d(TAG, "handleProgress: x = " + x + " - y = " + y + " - progress = " + progress + " size = " + imageBodies.size());
            Log.d(TAG, "handleProgress: ((double) (index / imageBodies.size()) * 100) = " + (((double) index / (imageBodies.size())) * 100));
            runOnUiThread(() -> {
                try {

//                    mAdapter.setDoneImage(index - 1);
                    if (index < imageBodies.size()) {
                        imageNameTV.setText(imageBodies.get(index).getImageName());
                    }
                    //states
                    final float[] states = new float[2];
                    states[0] = 100;
                    states[1] = progress;
                    String s = Math.round(progress) + "%";
                    slimChart.setStats(states);
                    slimChart.setText(s);
                    imagesCountProgressBar.setProgress(index);
                    progressStatusTV.setText(String.valueOf(index));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onImageFailed(Throwable throwable, int index) {
        try {
            Log.d(TAG, "onImageFailed: is called");
            lastUploadImageIndex = index;
            if (throwable instanceof ConnectException) {
//                Utilities.showInfoDialog(mCurrent, getString(R.string.no_network_connection), getString(R.string.connection_failed));
                Utilities.showToast(mCurrent, getString(R.string.connection_failed));
                displayOnResumeButton();
            } else if (throwable instanceof FileNotFoundException) {
                if (imageBodies != null && !imageBodies.isEmpty() && index < imageBodies.size()) {
                    handleProgress(index + 1);
                    presenter.uploadNextImage(index + 1);
                }
            } else if (throwable instanceof SocketException) {
                displayOnResumeButton();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayOnResumeButton() {
        try {
            Log.d(TAG, "displayOnResumeButton: is called");
            mDatePickerBtn.setText(getString(R.string.resume));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onImageLoadedFromStorage(List<ImageBody> mImageBodies, List<File> mImageFiles) {
        runOnUiThread(() -> {
            try {
                Log.d(TAG, "onImageLoadedFromStorage: is called");
                if (mImageBodies != null && !mImageBodies.isEmpty()) {
                    imageBodies = mImageBodies;
//                    initRecycler();
                    imagesCountProgressBar.setMax(imageBodies.size() - 1);
                    imagesCountProgressBar.setProgress(0);
                    viewAnimator.setDisplayedChild(1);
                    mUploadingHeader.setVisibility(View.GONE);
                    imageNameTV.setVisibility(View.GONE);
                    slimChart.setVisibility(View.GONE);
                    imageNameTV.setText(imageBodies.get(0).getImageName());
                    progressTargetTV.setText(String.valueOf(imageBodies.size()));
                    progressStatusTV.setText("0");
                    displayProgressChart();
                } else {
                    viewAnimator.setDisplayedChild(2);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void displayProgressChart() {
        try {
            float total = imageBodies.size();
            float value = lastUploadImageIndex;
//            float percent = (value / total) * 100;
            float percent = progress;
            //states
            final float[] states = new float[2];
            states[0] = 100;
            states[1] = percent;
            slimChart.setStats(states);
            //colors
            int[] colors = new int[3];
//            String blue = "#" + Integer.toHexString(ContextCompat.getColor(mCurrent, R.color.colorPrimary));
            String green = "#" + Integer.toHexString(ContextCompat.getColor(mCurrent, R.color.green));
//            colors[0] = Color.parseColor(blue);
            colors[1] = Color.parseColor(green); // green
            colors[0] = Color.parseColor("#e0e0e0"); // grey
            slimChart.setColors(colors);
            //text
            String s = Math.round(percent) + "%";
            slimChart.setText(s);
            slimChart.setRoundEdges(true);
            slimChart.setStartAnimationDuration(1000);
            slimChart.playStartAnimation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            presenter.dispatchBgThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDatePickerAction(View view) {
        try {
            if (datePickerFragment != null) {
                datePickerFragment.show(getSupportFragmentManager(), datePickerFragment.getTag());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUploadImageAction(View view) {
        try {
            if (presenter != null) {
                if (imageBodies != null && !imageBodies.isEmpty()) {
//                    mAdapter.setUploadAction(1);
                    if (mDatePickerBtn.getText().equals(getString(R.string.resume))) {
                        mDatePickerBtn.setText(getString(R.string.upload_now));
                    }
                    mUploadingHeader.setVisibility(View.VISIBLE);
                    imageNameTV.setVisibility(View.VISIBLE);
                    slimChart.setVisibility(View.VISIBLE);
                    presenter.uploadNextImage(lastUploadImageIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}