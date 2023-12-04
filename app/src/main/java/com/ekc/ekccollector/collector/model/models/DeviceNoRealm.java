package com.ekc.ekccollector.collector.model.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DeviceNoRealm extends RealmObject {

    private int deviceNo;

    private String surveyorName;

    public DeviceNoRealm() {
    }

    public int getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(int deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getSurveyorName() {
        return surveyorName;
    }

    public void setSurveyorName(String surveyorName) {
        this.surveyorName = surveyorName;
    }
}
