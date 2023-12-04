package com.ekc.ekccollector.collector.model.models;

import com.google.gson.annotations.SerializedName;

public class ImagePostPOJO {

    @SerializedName("Date")
    private DateInfo dateInfo;

    @SerializedName("Survayor")
    private String survayor;

    @SerializedName("Layer")
    private String layerName;

    @SerializedName("Device")
    private String deviceNo;

    @SerializedName("ImageName")
    private String imageName;

    @SerializedName("ImageContent")
    private String imageContent;

    public DateInfo getDateInfo() {
        return dateInfo;
    }

    public void setDateInfo(DateInfo dateInfo) {
        this.dateInfo = dateInfo;
    }

    public String getSurvayor() {
        return survayor;
    }

    public void setSurvayor(String survayor) {
        this.survayor = survayor;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageContent() {
        return imageContent;
    }

    public void setImageContent(String imageContent) {
        this.imageContent = imageContent;
    }

    class DateInfo{

        @SerializedName("Day")
        String day;

        @SerializedName("Month")
        String month;

        @SerializedName("Year")
        String year;

        public DateInfo() {
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
    }

}
