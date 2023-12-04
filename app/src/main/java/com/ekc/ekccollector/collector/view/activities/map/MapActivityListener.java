package com.ekc.ekccollector.collector.view.activities.map;


import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.ekc.ekccollector.collector.model.models.OnlineQueryResult;

import java.util.ArrayList;

public interface MapActivityListener {

    void onQueryOnline(ArrayList<OnlineQueryResult> results, FeatureLayer featureLayer, Point point);

    void hideFragmentFromActivity();

    void onDownloadGeoDatabaseSuccess(boolean status, String folderPath, String geoDatabasePath,String plGeoDatabasePath);

    void onQueryOffline(ArrayList<OnlineQueryResult> results, FeatureLayer featureLayer, Point point);

    void onSyncSuccess(boolean status);

    void onShowOfflineViews(boolean status);

    void onDeleteFeature(boolean status);

    void onQuerySurveyorAreaOnline(ArrayList<Polygon> polygon);

    void onDeleteDatabase(String path,String title);

    void onDatabaseTitleSelected(String dbTitle);
}
