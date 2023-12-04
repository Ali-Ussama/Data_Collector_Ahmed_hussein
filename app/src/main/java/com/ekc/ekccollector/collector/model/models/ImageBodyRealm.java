package com.ekc.ekccollector.collector.model.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ImageBodyRealm extends RealmObject {

    @PrimaryKey
    private String imageName;
    private String layerName;
    private String DeviceNo;
    private String date;
    private String day;
    private String month;
    private String year;
    private int sentIndicator;
    private String survayor;
    private String imageFile;
    private String compressedFile;

    public ImageBodyRealm() {
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getDeviceNo() {
        return DeviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        DeviceNo = deviceNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSentIndicator() {
        return sentIndicator;
    }

    public void setSentIndicator(int sentIndicator) {
        this.sentIndicator = sentIndicator;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSurvayor() {
        return survayor;
    }

    public void setSurvayor(String survayor) {
        this.survayor = survayor;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public String getCompressedFile() {
        return compressedFile;
    }

    public void setCompressedFile(String compressedFile) {
        this.compressedFile = compressedFile;
    }
}
