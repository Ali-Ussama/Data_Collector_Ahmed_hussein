package com.ekc.ekccollector.collector.model.models;

import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;

public class OnlineQueryResult {

    private FeatureLayer featureLayer;

    private ServiceFeatureTable serviceFeatureTable;

    private ArcGISFeature feature;

    private GeodatabaseFeatureTable geodatabaseFeatureTable;

    private Feature featureOffline;

    private String objectID;

    public OnlineQueryResult() {
    }

    public ServiceFeatureTable getServiceFeatureTable() {
        return serviceFeatureTable;
    }

    public void setServiceFeatureTable(ServiceFeatureTable serviceFeatureTable) {
        this.serviceFeatureTable = serviceFeatureTable;
    }

    public FeatureLayer getFeatureLayer() {
        return featureLayer;
    }

    public void setFeatureLayer(FeatureLayer featureLayer) {
        this.featureLayer = featureLayer;
    }

    public ArcGISFeature getFeature() {
        return feature;
    }

    public void setFeature(ArcGISFeature feature) {
        this.feature = feature;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public GeodatabaseFeatureTable getGeodatabaseFeatureTable() {
        return geodatabaseFeatureTable;
    }

    public void setGeodatabaseFeatureTable(GeodatabaseFeatureTable geodatabaseFeatureTable) {
        this.geodatabaseFeatureTable = geodatabaseFeatureTable;
    }

    public Feature getFeatureOffline() {
        return featureOffline;
    }

    public void setFeatureOffline(Feature featureOffline) {
        this.featureOffline = featureOffline;
    }
}
