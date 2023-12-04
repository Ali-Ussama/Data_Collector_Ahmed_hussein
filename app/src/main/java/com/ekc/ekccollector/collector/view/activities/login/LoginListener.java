package com.ekc.ekccollector.collector.view.activities.login;

import com.esri.arcgisruntime.geometry.Polygon;

public interface LoginListener {

    void onLogin(Polygon polygon, boolean status);

}
