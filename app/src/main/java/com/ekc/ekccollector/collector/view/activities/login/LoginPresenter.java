package com.ekc.ekccollector.collector.view.activities.login;

import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.ekc.ekccollector.R;
import com.ekc.ekccollector.collector.model.PrefManager;
import com.ekc.ekccollector.collector.model.QueryConfig;
import com.ekc.ekccollector.collector.model.SaveUserOnline;
import com.ekc.ekccollector.collector.model.models.Columns;
import com.ekc.ekccollector.collector.model.models.User;
import com.ekc.ekccollector.collector.view.utils.Utilities;

import java.util.Iterator;

import javax.inject.Inject;

public class LoginPresenter {

    private static final String TAG = "LoginPresenter";
    private LoginListener listener;
    private LoginActivity context;
    private FeatureLayer surveyorLayer;
    private ServiceFeatureTable surveyorTable;
    private PrefManager mPrefManager;

    @Inject
    UserDatabase userDatabase;

    UserComponent userComponent;

    SaveUserOnline saveUserOnline;

    public LoginPresenter(LoginListener listener, LoginActivity context) {
        this.listener = listener;
        this.context = context;
        mPrefManager = new PrefManager(context);
        surveyorTable = new ServiceFeatureTable(context.getString(R.string.Surveyor));
        surveyorLayer = new FeatureLayer(surveyorTable);
        userComponent = DaggerUserComponent.create();
        surveyorLayer.loadAsync();

    }

    public void login(String email, String code) {
        try {

            surveyorLayer.addDoneLoadingListener(() -> {
                try {
                    querySurveyorAreaOnline(code, surveyorLayer.getSpatialReference(), surveyorLayer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void querySurveyorAreaOnline(String code, SpatialReference sp, FeatureLayer layer) {
        try {
            QueryParameters query = QueryConfig.getSurveyorAreaQuery(sp, code, true);

            layer.loadAsync();
            layer.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ListenableFuture<FeatureQueryResult> future = layer.getFeatureTable().queryFeaturesAsync(query);
                        // add done loading listener to fire when the selection returns

                        future.addDoneListener(() -> {
                            try {
                                Polygon polygon = null;
                                // call get on the future to get the result
                                FeatureQueryResult result = future.get();
                                // check there are some results
                                Iterator<Feature> resultIterator = result.iterator();
                                if (resultIterator.hasNext()) {
                                    while (resultIterator.hasNext()) {
                                        // get the extent of the first feature in the result to zoom to

                                        ArcGISFeature feature = (ArcGISFeature) resultIterator.next();
                                        feature.loadAsync();
                                        polygon = (Polygon) feature.getGeometry();
                                        // select the feature
//                        mFeatureLayer.selectFeature(feature);
                                        Log.i(TAG, "queryOnline(): Feature founded with id = " + feature.getAttributes().get(Columns.ObjectID));

                                    }
                                    if (listener != null) {
                                        listener.onLogin(polygon, true);
                                    }
                                } else {
                                    Log.e(TAG, "queryOnline(): No states found ");
                                    listener.onLogin(polygon, true);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                listener.onLogin(null, false);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            listener.onLogin(null, false);
        }
    }

    public void cacheSurveyorData(String code, String pass, String surveyorName) {
        try {
            if (mPrefManager != null) {
                mPrefManager.saveString(PrefManager.KEY_SURVEYOR_CODE, code);
                mPrefManager.saveString(PrefManager.KEY_SURVEYOR_PASSWORD, pass);
                mPrefManager.saveString(PrefManager.KEY_SURVEYOR_NAME, surveyorName);
            }

            SaveUserOnline(code, getDataTime(), surveyorName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDataTime() {
        String dateTime = "";
        try {
            dateTime = Utilities.getDateNow();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    private void SaveUserOnline(String code, String dateTime, String surveyorName) {
        User user = new User();
        user.setUsername(surveyorName);
        user.setCode(code);
        user.setDateTime(dateTime);
        user.setVersion(Utilities.getVersionInfo(context)[1]);
        if (Utilities.getDeviceId(context) == null || Utilities.getDeviceId(context).isEmpty()) {
            user.setImei("Android 10 - " + surveyorName);
        } else {
            user.setImei(Utilities.getDeviceId(context));
        }

        Log.i(TAG, "SaveUserOnline: IMEI = " + Utilities.getDeviceId(context));

        saveUserOnline = new SaveUserOnline();
        saveUserOnline.saveUserIntoFireStore(user);
    }

}
