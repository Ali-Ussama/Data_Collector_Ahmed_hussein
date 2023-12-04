package com.ekc.ekccollector.collector.view.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.ekc.ekccollector.collector.view.activities.uploadImages.UploadImageListener;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "DatePickerFragment";
    private UploadImageListener listener;

    public DatePickerFragment(UploadImageListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        try {
            month++;
            Log.d(TAG, "onDateSet: " + dayOfMonth + "/" + month + "/" + year);
            String date = String.valueOf(dayOfMonth).concat("/").concat(String.valueOf(month)).concat("/").concat(String.valueOf(year));
            String dayStr = String.valueOf(dayOfMonth);
            String monthStr = String.valueOf(month);
            String yearStr = String.valueOf(year);

            if (dayOfMonth <= 9) {
                dayStr = "0".concat(dayStr);
            }
            if (month <= 9) {
                monthStr = "0".concat(monthStr);
            }
            String today = dayStr.concat("/").concat(monthStr).concat("/").concat(yearStr);

            if (listener != null) {
                listener.onDatePickedUp(today, dayStr, monthStr, yearStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
