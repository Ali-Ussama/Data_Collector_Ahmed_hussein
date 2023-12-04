package com.ekc.ekccollector.collector.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefManager {

    public static final String KEY_SURVEYOR_CODE = "Surveyor Code";
    public static final String KEY_SURVEYOR_PASSWORD = "Surveyor Password";
    public static final String KEY_SURVEYOR_NAME = "Surveyor Name";
    public static final String KEY_IMAGES_LOADED_INTO_DB = "Images Loaded Into Db";


    private SharedPreferences prefs;

    public PrefManager(Context c) {
        prefs = PreferenceManager.getDefaultSharedPreferences(c);
    }

    public void saveString(String key, String value) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(key, value);
        edit.apply();
    }

    public void saveLong(String key, long value) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putLong(key, value);
        edit.apply();
    }

    public void saveBoolean(String key, boolean b) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(key, b);
        edit.apply();
    }

    public void saveInt(String key, int b) {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt(key, b);
        edit.apply();
    }

    public String readString(String key) {
        return prefs.getString(key, "");
    }

    public boolean readBoolean(String key) {
        return prefs.getBoolean(key, false);
    }

    public long readLong(String key) {
        return prefs.getLong(key, 0);
    }

    public int readInt(String key) {
        return prefs.getInt(key, 0);
    }
}
