package com.ekc.ekccollector.collector.view.activities.map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.ekc.ekccollector.R;
import com.ekc.ekccollector.collector.DataCollectionApplication;
import com.ekc.ekccollector.collector.model.PrefManager;
import com.ekc.ekccollector.collector.model.QueryConfig;
import com.ekc.ekccollector.collector.model.models.BookMark;
import com.ekc.ekccollector.collector.model.models.Columns;
import com.ekc.ekccollector.collector.model.models.OConstants;
import com.ekc.ekccollector.collector.model.models.OnlineQueryResult;
import com.ekc.ekccollector.collector.model.singleton.ThreadSingleton;
import com.ekc.ekccollector.collector.view.activities.uploadImages.UploadImageActivity;
import com.ekc.ekccollector.collector.view.utils.ImageUtils;
import com.ekc.ekccollector.collector.view.utils.Utilities;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.data.SyncModel;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.io.RequestConfiguration;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseJob;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask;
import com.esri.arcgisruntime.tasks.geodatabase.SyncGeodatabaseJob;
import com.esri.arcgisruntime.tasks.geodatabase.SyncGeodatabaseParameters;
import com.esri.arcgisruntime.tasks.geodatabase.SyncLayerOption;
import com.esri.arcgisruntime.tasks.geodatabase.SyncLayerResult;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MapPresenter implements ImageUtilsListener {

    private static final String ROOT_GEO_DATABASE_PATH = "geodatabase";
    public static String IMAGE_FOLDER_NAME = "AJC_Collector";
    private final String TAG = "MapPresenter";
    private MapActivityListener listener;
    private MapActivity mCurrent;
    private ArrayList<OnlineQueryResult> mOnlineQueryResults;

    private File geodatabaseFile, databaseFile, pl_geodatabaseFile, pl_databaseFile;
    private String localGeodatabasePath, localPLGeodatabasePath;
    private ImageUtils imageUtils;
    private PrefManager prefManager;

    private int loadOfflineTablesTryCount = 0;
    ThreadSingleton threadSingleton;

    MapPresenter(MapActivityListener listener, MapActivity mCurrent, ThreadSingleton threadSingleton) {
        this.listener = listener;
        this.mCurrent = mCurrent;
        prefManager = new PrefManager(mCurrent);
        imageUtils = new ImageUtils(this, prefManager);
//        this.threadSingleton = threadSingleton;//TODO Delete,Not Needed anymore 2/6/2020
    }

    void prepareQueryResult() {
        mOnlineQueryResults = new ArrayList<>();
    }

    ArrayList<OnlineQueryResult> getOnlineQueryResults() {
        return mOnlineQueryResults;
    }

    void queryOnline(ArrayList<OnlineQueryResult> mOnlineQueryResults, Point point, SpatialReference sp, ServiceFeatureTable mServiceFeatureTable, FeatureLayer mFeatureLayer) {
        try {
            Log.i(TAG, "queryOnline(): is Called ");

            QueryParameters query = QueryConfig.getQuery(point, sp, true);

            final ListenableFuture<FeatureQueryResult> future = mServiceFeatureTable.queryFeaturesAsync(query);
            // add done loading listener to fire when the selection returns

            future.addDoneListener(() -> {
                try {
                    // call get on the future to get the result
                    FeatureQueryResult result = future.get();
                    // check there are some results
                    Iterator<Feature> resultIterator = result.iterator();
                    if (resultIterator.hasNext()) {
                        while (resultIterator.hasNext()) {
                            // get the extent of the first feature in the result to zoom to

                            ArcGISFeature feature = (ArcGISFeature) resultIterator.next();
                            feature.loadAsync();
                            OnlineQueryResult mOnlineQueryResult = new OnlineQueryResult();
                            mOnlineQueryResult.setFeature(feature);
                            mOnlineQueryResult.setServiceFeatureTable(mServiceFeatureTable);
                            mOnlineQueryResult.setFeatureLayer(mFeatureLayer);
                            mOnlineQueryResult.setObjectID(String.valueOf(feature.getAttributes().get(Columns.ObjectID)));
                            // select the feature
//                        mFeatureLayer.selectFeature(feature);
                            Log.i(TAG, "queryOnline(): Feature founded with id = " + feature.getAttributes().get(Columns.ObjectID));

                            mOnlineQueryResults.add(mOnlineQueryResult);
                        }
                        if (listener != null) {
                            listener.onQueryOnline(mOnlineQueryResults, mFeatureLayer, point);
                        }
                    } else {
                        Log.e(TAG, "queryOnline(): No states found ");
                        listener.onQueryOnline(mOnlineQueryResults, mFeatureLayer, point);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onQueryOnline(mOnlineQueryResults, mFeatureLayer, point);
                }
            });

        } catch (
                Exception e) {
            e.printStackTrace();
            listener.onQueryOnline(mOnlineQueryResults, mFeatureLayer, point);
        }
    }

    void queryOffline(ArrayList<OnlineQueryResult> mOnlineQueryResults, Point point, SpatialReference sp, GeodatabaseFeatureTable mGeodatabaseFeatureTable, FeatureLayer mFeatureLayer) {
        try {
            Log.i(TAG, "queryOffline(): is Called ");
            loadOfflineTablesTryCount++;
            QueryParameters query = QueryConfig.getQuery(point, sp, true);

            mGeodatabaseFeatureTable.loadAsync();
            mGeodatabaseFeatureTable.addDoneLoadingListener(() -> {
                try {
                    if (mGeodatabaseFeatureTable.getLoadStatus() == LoadStatus.LOADED) {
                        Log.e(TAG, "queryOffline: mGeodatabaseFeatureTable " + mGeodatabaseFeatureTable.getDisplayName() + " LOADED");
                        loadOfflineTablesTryCount = 0;
                        final ListenableFuture<FeatureQueryResult> future = mGeodatabaseFeatureTable.queryFeaturesAsync(query);
                        future.addDoneListener(() -> {
                            try {
                                // call get on the future to get the result
                                FeatureQueryResult result = future.get();
                                // check there are some results
                                Iterator<Feature> resultIterator = result.iterator();
                                if (resultIterator.hasNext()) {
                                    while (resultIterator.hasNext()) {
                                        // get the extent of the first feature in the result to zoom to

                                        Feature feature = resultIterator.next();

                                        OnlineQueryResult mOnlineQueryResult = new OnlineQueryResult();
                                        mOnlineQueryResult.setFeatureOffline(feature);
                                        mOnlineQueryResult.setGeodatabaseFeatureTable(mGeodatabaseFeatureTable);
                                        mOnlineQueryResult.setFeatureLayer(mFeatureLayer);
                                        mOnlineQueryResult.setObjectID(String.valueOf(feature.getAttributes().get(Columns.ObjectID)));
                                        // select the feature
                                        //                        mFeatureLayer.selectFeature(feature);
                                        Log.i(TAG, "queryOffline(): Feature founded with id = " + feature.getAttributes().get(Columns.ObjectID));

                                        mOnlineQueryResults.add(mOnlineQueryResult);
                                    }
                                    if (listener != null) {
                                        listener.onQueryOffline(mOnlineQueryResults, mFeatureLayer, point);
                                    }
                                } else {
                                    Log.e(TAG, "queryOffline(): No states found ");
                                    listener.onQueryOffline(mOnlineQueryResults, mFeatureLayer, point);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                listener.onQueryOffline(mOnlineQueryResults, mFeatureLayer, point);
                            }
                        });
                    } else if (mGeodatabaseFeatureTable.getLoadStatus() == LoadStatus.FAILED_TO_LOAD && loadOfflineTablesTryCount <= 3) {
                        Log.e(TAG, "queryOffline: mGeodatabaseFeatureTable " + mGeodatabaseFeatureTable.getDisplayName() + "FAILED_TO_LOAD");
                        queryOffline(mOnlineQueryResults, point, sp, mGeodatabaseFeatureTable, mFeatureLayer);
                    } else if (mGeodatabaseFeatureTable.getLoadStatus() == LoadStatus.NOT_LOADED && loadOfflineTablesTryCount <= 3) {
                        Log.e(TAG, "queryOffline: mGeodatabaseFeatureTable " + mGeodatabaseFeatureTable.getDisplayName() + " NOT_LOADED");
                        queryOffline(mOnlineQueryResults, point, sp, mGeodatabaseFeatureTable, mFeatureLayer);
                    } else {
                        loadOfflineTablesTryCount = 0;
                        listener.onQueryOffline(mOnlineQueryResults, mFeatureLayer, point);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loadOfflineTablesTryCount = 0;
                    listener.onQueryOffline(mOnlineQueryResults, mFeatureLayer, point);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            loadOfflineTablesTryCount = 0;
            listener.onQueryOffline(mOnlineQueryResults, mFeatureLayer, point);
        }
    }

    public void updateFeatureOnline(OnlineQueryResult result, String code, String deviceNo, String typeCode, String surveyorName, int commentCode, int status) {
        try {

//        ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable("");
//        shapefileFeatureTable.

            final FeatureLayer featureLayer = result.getServiceFeatureTable().getFeatureLayer();
            featureLayer.selectFeature(result.getFeature());

            final ListenableFuture<FeatureQueryResult> selected = featureLayer.getSelectedFeaturesAsync();
            FeatureQueryResult features = null;
            try {
                features = selected.get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // check there is at least one selected feature
            if (!features.iterator().hasNext()) {
                Log.e(TAG, "No selected features");
            }

            // get the first selected feature and load it
            final ArcGISFeature feature = (ArcGISFeature) features.iterator().next();
            feature.loadAsync();

            feature.addDoneLoadingListener(() -> {
                // now feature is loaded we can update it; change attribute and geometry (here the point geometry is moved North)
                try {
                    feature.getAttributes().put(Columns.Code, code);
                    feature.getAttributes().put(Columns.Device_No, deviceNo);
                    feature.getAttributes().put(Columns.Type, typeCode);
                    feature.getAttributes().put(Columns.Status, status);
                    feature.getAttributes().put(Columns.SurveyorName, surveyorName);
                    feature.getAttributes().put(Columns.Comments, commentCode);

                    result.getServiceFeatureTable().updateFeatureAsync(feature).get();

                    if (result.getServiceFeatureTable() instanceof ServiceFeatureTable) {
                        ServiceFeatureTable serviceFeatureTable = (ServiceFeatureTable) result.getServiceFeatureTable();

                        // can call getUpdatedFeaturesCountAsync to verify number of updates to be applied before calling applyEditsAsync

                        try {
                            if (featureLayer != null) {
                                featureLayer.clearSelection();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        final List<FeatureEditResult> featureEditResults = serviceFeatureTable.applyEditsAsync().get();
                        listener.hideFragmentFromActivity();

                        Log.i(TAG, "updateFeatureOnline(): getGeodatabaseFeatureTable calling update Feature Async");
                        Log.i(TAG, "updateFeatureOnline(): getGeodatabaseFeatureTable name = " + serviceFeatureTable.getTableName());
                        Log.i(TAG, "updateFeatureOnline(): Feature type = " + feature.getAttributes().get(Columns.Type));
                        Log.i(TAG, "updateFeatureOnline(): Feature code = " + feature.getAttributes().get(Columns.Code));
                        Log.i(TAG, "updateFeatureOnline(): Feature status = " + feature.getAttributes().get(Columns.Status));
                        Log.i(TAG, "updateFeatureOnline(): Feature comment = " + feature.getAttributes().get(Columns.Comments));
                        Log.i(TAG, "updateFeatureOnline(): Surveyor Name = " + feature.getAttributes().get(Columns.SurveyorName));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.hideFragmentFromActivity();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            listener.hideFragmentFromActivity();
        }
    }

    public void updateFeatureOffline(OnlineQueryResult result, String code, String deviceNo, String typeCode, String surveyorName, int commentCode, int status) {
        try {
            Log.i(TAG, "updateFeatureOffline(): is called");

            final FeatureLayer featureLayer = result.getGeodatabaseFeatureTable().getFeatureLayer();
            featureLayer.selectFeature(result.getFeatureOffline());

            final ListenableFuture<FeatureQueryResult> selected = featureLayer.getSelectedFeaturesAsync();
            FeatureQueryResult features = null;
            try {
                features = selected.get();
            } catch (Exception e) {
                e.printStackTrace();
                Utilities.dismissLoadingDialog();
            }

            // check there is at least one selected feature
            if (!features.iterator().hasNext()) {
                Log.e(TAG, "No selected features");
            }

            // get the first selected feature and load it
            final Feature feature = features.iterator().next();

            // now feature is loaded we can update it; change attribute and geometry (here the point geometry is moved North)
            try {
                feature.getAttributes().put(Columns.Code, code);
                feature.getAttributes().put(Columns.Device_No, deviceNo);
                feature.getAttributes().put(Columns.Type, typeCode);
                feature.getAttributes().put(Columns.Status, status);
                feature.getAttributes().put(Columns.SurveyorName, surveyorName);
                feature.getAttributes().put(Columns.Comments, commentCode);

                result.getGeodatabaseFeatureTable().updateFeatureAsync(feature).addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "updateFeatureOffline(): Feature Updated");
                        try {
                            if (featureLayer != null) {
                                featureLayer.clearSelection();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        listener.hideFragmentFromActivity();

                        Log.i(TAG, "updateFeatureOffline(): getGeodatabaseFeatureTable calling update Feature Async");
                        Log.i(TAG, "updateFeatureOffline(): getGeodatabaseFeatureTable name = " + result.getGeodatabaseFeatureTable().getTableName());
                        Log.i(TAG, "updateFeatureOffline(): Feature type = " + feature.getAttributes().get(Columns.Type));
                        Log.i(TAG, "updateFeatureOffline(): Feature code = " + feature.getAttributes().get(Columns.Code));
                        Log.i(TAG, "updateFeatureOffline(): Feature status = " + feature.getAttributes().get(Columns.Status));
                        Log.i(TAG, "updateFeatureOnline(): Feature comment = " + feature.getAttributes().get(Columns.Comments));
                        Log.i(TAG, "updateFeatureOffline(): Surveyor Name = " + feature.getAttributes().get(Columns.SurveyorName));


                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Utilities.dismissLoadingDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utilities.dismissLoadingDialog();
        }
    }

    public void deleteFeatureOnline(OnlineQueryResult result) {
        Log.i(TAG, "deleteFeature(): is called");

        // get selected features from the layer for this ArcGISFeatureTable
        // query feature layer to find element by id
        QueryParameters queryParameters = new QueryParameters();
        queryParameters.setWhereClause(String.format("OBJECTID = %s", result.getObjectID()));

        final FeatureLayer featureLayer = result.getFeatureLayer();
        final ListenableFuture<FeatureQueryResult> selected = featureLayer.getFeatureTable().queryFeaturesAsync(queryParameters);
        FeatureQueryResult features;
        Feature foundFeature = null;
        try {

            try {
                // check result has a feature
                if (selected.get().iterator().hasNext()) {
                    // attempt to get first feature from result as it should be the only feature
                    foundFeature = selected.get().iterator().next();
                    // delete found features
                    Log.i(TAG, "deleteFeature(): feature has been founded");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            features = selected.get();

            if (foundFeature != null) {
                // delete the selected features
                Log.i(TAG, "deleteFeature(): founded feature not null");
                result.getServiceFeatureTable().deleteFeatureAsync(foundFeature).get();

                //if dealing with ServiceFeatureTable, apply edits after making updates; if editing locally, then edits can
                // be synchronized at some point using the SyncGeodatabaseTask.
                if (result.getServiceFeatureTable() instanceof ServiceFeatureTable) {
                    Log.i(TAG, "deleteFeature(): feature table is ServiceFeatureTable");

                    ServiceFeatureTable serviceFeatureTable = result.getServiceFeatureTable();

                    // can call getDeletedFeaturesCountAsync() to verify number of deletes to be applied before calling applyEditsAsync

                    serviceFeatureTable.applyEditsAsync().addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "deleteFeature(): feature deleted");
                            listener.onDeleteFeature(true);
                        }
                    });

                    // if required, can check the edits applied in this operation by using returned FeatureEditResult
                }
            } else {
                Log.i(TAG, "deleteFeature(): founded feature is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onDeleteFeature(false);
        }
    }

    public void deleteFeatureOffline(OnlineQueryResult result) {
        Log.i(TAG, "deleteFeature(): is called");

        // get selected features from the layer for this ArcGISFeatureTable
        // query feature layer to find element by id
        QueryParameters queryParameters = new QueryParameters();
        queryParameters.setWhereClause(String.format("OBJECTID = %s", result.getObjectID()));

        final FeatureLayer featureLayer = result.getFeatureLayer();
        final ListenableFuture<FeatureQueryResult> selected = featureLayer.getFeatureTable().queryFeaturesAsync(queryParameters);
        FeatureQueryResult features;
        Feature foundFeature = null;
        try {

            try {
                // check result has a feature
                if (selected.get().iterator().hasNext()) {
                    // attempt to get first feature from result as it should be the only feature
                    foundFeature = selected.get().iterator().next();
                    // delete found features
                    Log.i(TAG, "deleteFeature(): feature has been founded");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            features = selected.get();

            if (foundFeature != null) {
                // delete the selected features
                Log.i(TAG, "deleteFeature(): founded feature not null");
                result.getGeodatabaseFeatureTable().deleteFeatureAsync(foundFeature).addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            Log.i(TAG, "deleteFeature(): feature deleted");
                            listener.onDeleteFeature(true);
                        }
                    }
                });

            } else {
                Log.i(TAG, "deleteFeature(): founded feature is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onDeleteFeature(false);
        }
    }

    void downloadAndSaveDatabase(String downloadGeoDatabase, String localDatabaseTitle, Envelope extent, String featureServerUrl) {
        try {
            // create a geodatabase sync task
            final GeodatabaseSyncTask geodatabaseSyncTask = new GeodatabaseSyncTask(featureServerUrl);
            geodatabaseSyncTask.loadAsync();
            geodatabaseSyncTask.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {

                    // create generate geodatabase parameters for the current extent
                    final ListenableFuture<GenerateGeodatabaseParameters> defaultParameters = geodatabaseSyncTask
                            .createDefaultGenerateGeodatabaseParametersAsync(extent);

                    defaultParameters.addDoneListener(() -> {
                        try {
                            // set parameters and don't include attachments
                            GenerateGeodatabaseParameters parameters = defaultParameters.get();
                            parameters.setReturnAttachments(false);

                            createDatabaseFilePath(localDatabaseTitle);

                            // create and start the job
                            final GenerateGeodatabaseJob generateGeodatabaseJob = geodatabaseSyncTask
                                    .generateGeodatabaseAsync(parameters, localGeodatabasePath);
                            generateGeodatabaseJob.start();

                            // update progress
                            generateGeodatabaseJob.addProgressChangedListener(() -> {
//                          progressBar.setProgress(generateGeodatabaseJob.getProgress());
//                          mProgressTextView.setText(getString(R.string.progress_fetching));
                            });

                            // get geodatabase when done
                            generateGeodatabaseJob.addJobDoneListener(() -> handleOnDatabaseLoaded(generateGeodatabaseJob, localDatabaseTitle, localGeodatabasePath, geodatabaseFile, databaseFile, extent));
                        } catch (Exception e) {
                            Log.e(TAG, "Error generating geodatabase parameters : " + e.getMessage());
                            downloadAndSaveDatabase(downloadGeoDatabase, localDatabaseTitle, extent, featureServerUrl);
//                            Utilities.dismissLoadingDialog();
                        }
                    });

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDatabaseFilePath(String localDatabaseTitle) {
        try {
            // define the local path where the geodatabase will be stored
            Log.d(TAG, "createDatabaseFilePath: is called");
            File rootFolder = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), IMAGE_FOLDER_NAME);
            if (!rootFolder.exists())
                rootFolder.mkdirs();
            geodatabaseFile = new File(rootFolder.getPath(), "geodatabase");
            if (!geodatabaseFile.exists()) {
                geodatabaseFile.mkdirs();
            }

            databaseFile = new File(geodatabaseFile.getPath(), localDatabaseTitle + ".geodatabase");
            localGeodatabasePath = databaseFile.getPath();
            Log.d(TAG, "createDatabaseFilePath: localGeodatabasePath = " + localDatabaseTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleOnDatabaseLoaded(GenerateGeodatabaseJob generateGeodatabaseJob, String localDatabaseTitle, String localGeodatabasePath, File geodatabaseFile, File databaseFile, Envelope extent) {
        try {
//            mProgressLayout.setVisibility(View.INVISIBLE);
            if (generateGeodatabaseJob.getStatus() == Job.Status.SUCCEEDED) {
                final Geodatabase geodatabase = generateGeodatabaseJob.getResult();
                geodatabase.loadAsync();
                geodatabase.addDoneLoadingListener(new Runnable() {
                    @Override
                    public void run() {
                        if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                            for (GeodatabaseFeatureTable geodatabaseFeatureTable : geodatabase
                                    .getGeodatabaseFeatureTables()) {
                                geodatabaseFeatureTable.loadAsync();
                            }
                            int dbNum = DataCollectionApplication.getDatabaseNumber();
                            DataCollectionApplication.setLocalDatabaseTitle(localDatabaseTitle, dbNum);
                            DataCollectionApplication.incrementDatabaseNumber();
                            DataCollectionApplication.addBookMark(mCurrent.mapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).toJson(), localDatabaseTitle);

//                            Log.i(TAG, "Local geodatabase stored at: " + localGeodatabasePath);
                            listener.onDownloadGeoDatabaseSuccess(true, geodatabaseFile.getPath(), databaseFile.getPath(),null);
                            Utilities.dismissLoadingDialog();

//                            String plFeatureServerUrl = mCurrent.getString(R.string.gcs_feature_server_test_substation_pl);
//                            downloadPLDatabase(localDatabaseTitle, extent, plFeatureServerUrl, localGeodatabasePath, geodatabaseFile, databaseFile);

                        } else {
                            Log.e(TAG, "Error loading geodatabase: " + geodatabase.getLoadError().getMessage());
                        }
                    }
                });

            } else if (generateGeodatabaseJob.getError() != null) {
                Log.e(TAG, "Error generating geodatabase: " + generateGeodatabaseJob.getError().getMessage());
                generateGeodatabaseJob.getError().printStackTrace();
                Utilities.dismissLoadingDialog();
            } else {
                Log.e(TAG, "Unknown Error generating geodatabase");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadPLDatabase(String localDatabaseTitle, Envelope extent, String featureServerUrl, String localGeodatabasePath, File geodatabaseFile, File databaseFile) {
        try {

            // create a geodatabase sync task
            final GeodatabaseSyncTask geodatabaseSyncTask = new GeodatabaseSyncTask(featureServerUrl);
            geodatabaseSyncTask.loadAsync();
            geodatabaseSyncTask.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        createDatabaseFilePath_pl(localDatabaseTitle.concat("_pl"));

                        // create generate geodatabase parameters for the current extent
                        final ListenableFuture<GenerateGeodatabaseParameters> defaultParameters = geodatabaseSyncTask
                                .createDefaultGenerateGeodatabaseParametersAsync(extent);
                        defaultParameters.addDoneListener(() -> {
                            try {
                                // set parameters and don't include attachments
                                GenerateGeodatabaseParameters parameters = defaultParameters.get();
                                parameters.setReturnAttachments(false);

//                                createDatabaseFilePath(localDatabaseTitle);

                                // create and start the job
                                final GenerateGeodatabaseJob generateGeodatabaseJob = geodatabaseSyncTask
                                        .generateGeodatabaseAsync(parameters, localPLGeodatabasePath);
                                generateGeodatabaseJob.start();

                                // update progress
                                generateGeodatabaseJob.addProgressChangedListener(() -> {
//                          progressBar.setProgress(generateGeodatabaseJob.getProgress());
//                          mProgressTextView.setText(getString(R.string.progress_fetching));
                                });

                                // get geodatabase when done
                                generateGeodatabaseJob.addJobDoneListener(() -> handleOnPLDatabaseLoaded(generateGeodatabaseJob, localDatabaseTitle, localGeodatabasePath, geodatabaseFile, databaseFile, extent));
                            } catch (Exception e) {
                                Log.e(TAG, "Error generating geodatabase parameters : " + e.getMessage());
                                Utilities.dismissLoadingDialog();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDatabaseFilePath_pl(String localDatabaseTitle) {
        try {
            // define the local path where the geodatabase will be stored

            File rootFolder = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), IMAGE_FOLDER_NAME);
            if (!rootFolder.exists())
                rootFolder.mkdirs();
            pl_geodatabaseFile = new File(rootFolder.getPath(), "geodatabase");
            if (!pl_geodatabaseFile.exists()) {
                pl_geodatabaseFile.mkdirs();
            }

            pl_databaseFile = new File(pl_geodatabaseFile.getPath(), localDatabaseTitle + ".geodatabase");
            localPLGeodatabasePath = pl_databaseFile.getPath();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleOnPLDatabaseLoaded(GenerateGeodatabaseJob generateGeodatabaseJob, String localDatabaseTitle, String localGeodatabasePath, File geodatabaseFile, File databaseFile, Envelope extent) {
        try {
//            mProgressLayout.setVisibility(View.INVISIBLE);
            if (generateGeodatabaseJob.getStatus() == Job.Status.SUCCEEDED) {
                final Geodatabase geodatabase = generateGeodatabaseJob.getResult();
                geodatabase.loadAsync();
                geodatabase.addDoneLoadingListener(() -> {
                    if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                        for (GeodatabaseFeatureTable geodatabaseFeatureTable : geodatabase
                                .getGeodatabaseFeatureTables()) {
                            geodatabaseFeatureTable.loadAsync();
                        }
                        Log.d(TAG, "handleOnPLDatabaseLoaded: Local geodatabase stored at: " + localGeodatabasePath);
                        listener.onDownloadGeoDatabaseSuccess(true, geodatabaseFile.getPath(), databaseFile.getPath(), pl_databaseFile.getPath());
                        Utilities.dismissLoadingDialog();

                    } else {
                        Log.e(TAG, "handleOnPLDatabaseLoaded: Error loading geodatabase: " + geodatabase.getLoadError().getMessage());
                    }
                });

            } else if (generateGeodatabaseJob.getError() != null) {
                Log.e(TAG, "handleOnPLDatabaseLoaded: Error generating geodatabase: " + generateGeodatabaseJob.getError().getMessage());
                generateGeodatabaseJob.getError().printStackTrace();
                Utilities.dismissLoadingDialog();
            } else {
                Log.e(TAG, "handleOnPLDatabaseLoaded: Unknown Error generating geodatabase");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void loadMap(String folderPath, String geoDatabasePath, MapView mapView, ArcGISMap map) {
        try {
            // create path to local geodatabase

            // create a new geodatabase from local path
            final Geodatabase geodatabase = new Geodatabase(geoDatabasePath);

            // load the geodatabase
            geodatabase.loadAsync();

            // create feature layer from geodatabase and add to the map
            geodatabase.addDoneLoadingListener(() -> {
                if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                    Log.i(TAG, "loadMap():loading tables...");
                    loadTables(geodatabase, mapView, map);
                } else {

                    Utilities.showToast(mCurrent, "Geodatabase failed to load!");

                    Log.e(TAG, "Geodatabase failed to load!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void loadPLMap(String folderPath, String geoDatabasePath, MapView mapView, ArcGISMap map) {
        try {
            // create path to local geodatabase

            // create a new geodatabase from local path
            final Geodatabase geodatabase = new Geodatabase(geoDatabasePath);

            // load the geodatabase
            geodatabase.loadAsync();

            // create feature layer from geodatabase and add to the map
            geodatabase.addDoneLoadingListener(() -> {
                if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                    Log.i(TAG, "loadMap():loading tables...");
                    loadPLTables(geodatabase, mapView, map);
                } else {

                    Utilities.showToast(mCurrent, "Geodatabase failed to load!");

                    Log.e(TAG, "Geodatabase failed to load!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTables(Geodatabase geodatabase, MapView mapView, ArcGISMap map) {
        try {
            Log.i(TAG, "loadTables(): is called");

            if (geodatabase != null && geodatabase.getGeodatabaseFeatureTables() != null) {
                Log.i(TAG, "loadTables(): tables count = " + geodatabase.getGeodatabaseFeatureTables().size());
                if (Utilities.isNetworkAvailable(mCurrent)) {
                    ArcGISMapImageLayer plImageLayer = new ArcGISMapImageLayer(mCurrent.getString(R.string.substation_pl));
                    map.getOperationalLayers().add(plImageLayer);
                }
            }

            for (GeodatabaseFeatureTable geodatabaseFeatureTables : geodatabase.getGeodatabaseFeatureTables()) {
                Log.i(TAG, "loadTables(): table name = " + geodatabaseFeatureTables.getTableName());
                // access the geodatabase's feature table name

                geodatabaseFeatureTables.loadAsync();
                // create a layer from the geodatabase feature table and add to map
                final FeatureLayer featureLayer = new FeatureLayer(geodatabaseFeatureTables);
                map.getOperationalLayers().add(featureLayer);
                mapView.setMap(map);
                loadLayerAsyncOffline(featureLayer);
                loadTableAsyncOffline(geodatabaseFeatureTables);
                if (OConstants.LAYER_DISTRIBUTION_BOX.contains(geodatabaseFeatureTables.getTableName())) {
                    mCurrent.FCL_DistributionBoxTableOffline = geodatabaseFeatureTables;
                    mCurrent.FCL_DistributionBoxLayer = featureLayer;
                } else if (OConstants.LAYER_POLES.contains(geodatabaseFeatureTables.getTableName())) {
                    mCurrent.FCL_POLESTableOffline = geodatabaseFeatureTables;
                    mCurrent.FCL_POLES_Layer = featureLayer;
                } else if (OConstants.LAYER_RMU.contains(geodatabaseFeatureTables.getTableName())) {
                    mCurrent.FCL_RMUTableOffline = geodatabaseFeatureTables;
                    mCurrent.FCL_RMU_Layer = featureLayer;
                } else if (OConstants.LAYER_SUB_STATION.contains(geodatabaseFeatureTables.getTableName())) {
                    mCurrent.FCL_SubstationTableOffline = geodatabaseFeatureTables;
                    mCurrent.FCL_Substation_Layer = featureLayer;
                } else if (OConstants.LAYER_OCL_METER.contains(geodatabaseFeatureTables.getTableName())) {
                    mCurrent.OCL_METERTableOffline = geodatabaseFeatureTables;
                    mCurrent.OCL_METER_Layer = featureLayer;
                } /*else if (OConstants.LAYER_SERVICE_POINT.contains(geodatabaseFeatureTables.getTableName())) {
                    mCurrent.ServicePointTableOffline = geodatabaseFeatureTables;
                    mCurrent.ServicePoint_Layer = featureLayer;
                }*/ else if (OConstants.LAYER_SURVEYOR.contains(geodatabaseFeatureTables.getTableName())) {
                    mCurrent.SurveyorTableOffline = geodatabaseFeatureTables;
                    mCurrent.Surveyor_Layer = featureLayer;
                } else if (OConstants.LAYER_OTHERS.contains(geodatabaseFeatureTables.getTableName())) {
                    mCurrent.Others_Layer = featureLayer;
                    mCurrent.OthersTableOffline = geodatabaseFeatureTables;
                } else if (OConstants.LAYER_PL.contains(geodatabaseFeatureTables.getTableName())) {
                    mCurrent.FCL_PL_Layer = featureLayer;
                    mCurrent.FCL_PL_TableOffline = geodatabaseFeatureTables;
                }

                mCurrent.onlineData = false;
            }

            listener.onShowOfflineViews(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPLTables(Geodatabase geodatabase, MapView mapView, ArcGISMap map) {
        try {
            Log.i(TAG, "loadPLTables(): is called");

            if (geodatabase != null && geodatabase.getGeodatabaseFeatureTables() != null) {
                Log.i(TAG, "loadPLTables(): tables count = " + geodatabase.getGeodatabaseFeatureTables().size());
                if (Utilities.isNetworkAvailable(mCurrent)) {
                    ArcGISMapImageLayer plImageLayer = new ArcGISMapImageLayer(mCurrent.getString(R.string.substation_pl));
                    map.getOperationalLayers().add(plImageLayer);
                }
            }

            for (GeodatabaseFeatureTable geodatabaseFeatureTables : geodatabase.getGeodatabaseFeatureTables()) {
                Log.i(TAG, "loadPLTables(): table name = " + geodatabaseFeatureTables.getTableName());
                // access the geodatabase's feature table name

                geodatabaseFeatureTables.loadAsync();
                // create a layer from the geodatabase feature table and add to map
                final FeatureLayer featureLayer = new FeatureLayer(geodatabaseFeatureTables);
                map.getOperationalLayers().add(featureLayer);
                mapView.setMap(map);
                loadLayerAsyncOffline(featureLayer);
                loadTableAsyncOffline(geodatabaseFeatureTables);
                if (OConstants.LAYER_SUB_STATION.contains(geodatabaseFeatureTables.getTableName())) {
                    mCurrent.FCL_PL_Layer = featureLayer;
                    mCurrent.FCL_PL_TableOffline = geodatabaseFeatureTables;
                }
            }

            listener.onShowOfflineViews(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTableAsyncOffline(GeodatabaseFeatureTable geodatabaseFeatureTables) {
        try {
            Log.d(TAG, "loadTableAsyncOffline: is called with table " + geodatabaseFeatureTables.getTableName());
            geodatabaseFeatureTables.loadAsync();
            geodatabaseFeatureTables.addDoneLoadingListener(() -> {
                if (geodatabaseFeatureTables.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                    Log.e(TAG, "loadTableAsyncOffline: Geodatabase Feature Tables " + geodatabaseFeatureTables.getTableName() + " failed to load!");
                    loadTableAsyncOffline(geodatabaseFeatureTables);
                } else if (geodatabaseFeatureTables.getLoadStatus() == LoadStatus.LOADED) {
                    Log.d(TAG, "loadTableAsyncOffline: Geodatabase Feature Tables " + geodatabaseFeatureTables.getTableName() + " is loaded!");

                    if (geodatabaseFeatureTables.getTableName().equals("Surveyor")) {
                        if (mCurrent.Surveyor_Layer.getLoadStatus() == LoadStatus.LOADED) {
                            String surveyorCode = getSurveyorCode(mCurrent.prefManager);
                            querySurveyorAreaOffline(surveyorCode, mCurrent.mapView.getSpatialReference(), mCurrent.SurveyorTableOffline, mCurrent.Surveyor_Layer);
                        }
                    }
                } else if (geodatabaseFeatureTables.getLoadStatus() == LoadStatus.LOADING) {
                    Log.d(TAG, "loadTableAsyncOffline: Geodatabase Feature Tables " + geodatabaseFeatureTables.getTableName() + "is loaded!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLayerAsyncOffline(FeatureLayer featureLayer) {
        try {
            Log.d(TAG, "loadLayerAsyncOffline: is called with layer " + featureLayer.getName());
            featureLayer.loadAsync();
            featureLayer.addDoneLoadingListener(() -> {
                if (featureLayer.getLoadStatus() == LoadStatus.LOADED) {
                    // set viewpoint to the feature layer's extent
                    Log.d(TAG, "loadLayerAsyncOffline: Feature Layer " + featureLayer.getName() + " is loaded");
                    if (featureLayer.getName().equals("Surveyor")) {
                        if (mCurrent.SurveyorTableOffline.getLoadStatus() == LoadStatus.LOADED) {
                            String surveyorCode = getSurveyorCode(mCurrent.prefManager);
                            querySurveyorAreaOffline(surveyorCode, mCurrent.mapView.getSpatialReference(), mCurrent.SurveyorTableOffline, mCurrent.Surveyor_Layer);
                        }
                    }
                } else if (featureLayer.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
//                    Utilities.showToast(mCurrent, "Feature Layer failed to load!");
                    Log.e(TAG, "loadLayerAsyncOffline: Feature Layer " + featureLayer.getName() + "failed to load!");
                    loadLayerAsyncOffline(featureLayer);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addLocalLayers(final MapView mapView, ArcGISMap map, final int databaseNumber, String dbTitle) {

        mCurrent.onlineData = false;

        Log.i(TAG, "Removing all the features layers from map");

        map.getOperationalLayers().clear();

        Log.i(TAG, "addLocalLayers(): Add features layers from Local Geo Database");

        Geodatabase geodatabase;

        try {

            String databasePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + IMAGE_FOLDER_NAME + File.separator + ROOT_GEO_DATABASE_PATH + File.separator + dbTitle + ".geodatabase";
            Log.i(TAG, "addLocalLayers(): database path = " + databasePath);

            geodatabase = new Geodatabase(databasePath);
            geodatabase.loadAsync();
            geodatabase.addDoneLoadingListener(() -> mCurrent.runOnUiThread(() -> {
                try {
                    Log.i(TAG, "addLocalLayers(): tables count = " + geodatabase.getGeodatabaseFeatureTables().size());
                    if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                        Log.i(TAG, "addLocalLayers(): tables count = " + geodatabase.getGeodatabaseFeatureTables().size());
//                        if (Utilities.isNetworkAvailable(mCurrent)) {
//                            ArcGISMapImageLayer plImageLayer = new ArcGISMapImageLayer(mCurrent.getString(R.string.substation_pl));
//                            map.getOperationalLayers().add(plImageLayer);
//                        }

                        for (GeodatabaseFeatureTable gdbFeatureTable : geodatabase.getGeodatabaseFeatureTables()) {

                            Log.i(TAG, "addLocalLayers(): gdb Feature Table has geometry");

                            if (OConstants.LAYER_DISTRIBUTION_BOX.contains(gdbFeatureTable.getTableName())) {
                                mCurrent.FCL_DistributionBoxLayer = new FeatureLayer(gdbFeatureTable);
                                mCurrent.FCL_DistributionBoxTableOffline = ((GeodatabaseFeatureTable) mCurrent.FCL_DistributionBoxLayer.getFeatureTable());
                                map.getOperationalLayers().add(mCurrent.FCL_DistributionBoxLayer);
                                Log.i(TAG, "addLocalLayers(): LayerName is " + mCurrent.FCL_DistributionBoxTableOffline.getTableName());

                                loadLayerAsyncOffline(mCurrent.FCL_DistributionBoxLayer);
                                loadTableAsyncOffline(mCurrent.FCL_DistributionBoxTableOffline);

                            } else if (OConstants.LAYER_POLES.contains(gdbFeatureTable.getTableName())) {
                                mCurrent.FCL_POLES_Layer = new FeatureLayer(gdbFeatureTable);
                                mCurrent.FCL_POLESTableOffline = ((GeodatabaseFeatureTable) mCurrent.FCL_POLES_Layer.getFeatureTable());
                                map.getOperationalLayers().add(mCurrent.FCL_POLES_Layer);

                                Log.i(TAG, "addLocalLayers(): LayerName is " + mCurrent.FCL_POLES_ServiceTable.getTableName());

                                loadLayerAsyncOffline(mCurrent.FCL_POLES_Layer);
                                loadTableAsyncOffline(mCurrent.FCL_POLESTableOffline);

                            } else if (OConstants.LAYER_RMU.contains(gdbFeatureTable.getTableName())) {
                                mCurrent.FCL_RMU_Layer = new FeatureLayer(gdbFeatureTable);
                                mCurrent.FCL_RMUTableOffline = ((GeodatabaseFeatureTable) mCurrent.FCL_RMU_Layer.getFeatureTable());
                                map.getOperationalLayers().add(mCurrent.FCL_RMU_Layer);

                                Log.i(TAG, "addLocalLayers(): LayerName is " + mCurrent.FCL_RMUTableOffline.getTableName());

                                loadLayerAsyncOffline(mCurrent.FCL_RMU_Layer);
                                loadTableAsyncOffline(mCurrent.FCL_RMUTableOffline);

                            } else if (OConstants.LAYER_SUB_STATION.contains(gdbFeatureTable.getTableName())) {
                                mCurrent.FCL_Substation_Layer = new FeatureLayer(gdbFeatureTable);
                                mCurrent.FCL_SubstationTableOffline = ((GeodatabaseFeatureTable) mCurrent.FCL_Substation_Layer.getFeatureTable());
                                map.getOperationalLayers().add(mCurrent.FCL_Substation_Layer);

                                Log.i(TAG, "addLocalLayers(): LayerName is " + mCurrent.FCL_SubstationTableOffline.getTableName());

                                loadLayerAsyncOffline(mCurrent.FCL_Substation_Layer);
                                loadTableAsyncOffline(mCurrent.FCL_SubstationTableOffline);

                            } else if (OConstants.LAYER_OCL_METER.contains(gdbFeatureTable.getTableName())) {
                                mCurrent.OCL_METER_Layer = new FeatureLayer(gdbFeatureTable);
                                mCurrent.OCL_METERTableOffline = ((GeodatabaseFeatureTable) mCurrent.OCL_METER_Layer.getFeatureTable());
                                map.getOperationalLayers().add(mCurrent.OCL_METER_Layer);

                                Log.i(TAG, "addLocalLayers(): LayerName is " + mCurrent.OCL_METERTableOffline.getTableName());

                                loadLayerAsyncOffline(mCurrent.OCL_METER_Layer);
                                loadTableAsyncOffline(mCurrent.OCL_METERTableOffline);

                            } else if (OConstants.LAYER_SURVEYOR.contains(gdbFeatureTable.getTableName())) {
                                mCurrent.Surveyor_Layer = new FeatureLayer(gdbFeatureTable);
                                mCurrent.SurveyorTableOffline = ((GeodatabaseFeatureTable) mCurrent.Surveyor_Layer.getFeatureTable());
                                map.getOperationalLayers().add(mCurrent.Surveyor_Layer);

                                Log.i(TAG, "addLocalLayers(): LayerName is " + mCurrent.SurveyorTableOffline.getTableName());

                                loadLayerAsyncOffline(mCurrent.Surveyor_Layer);
                                loadTableAsyncOffline(mCurrent.SurveyorTableOffline);

                            } else if (OConstants.LAYER_OTHERS.contains(gdbFeatureTable.getTableName())) {
                                mCurrent.Others_Layer = new FeatureLayer(gdbFeatureTable);
                                mCurrent.OthersTableOffline = ((GeodatabaseFeatureTable) mCurrent.Others_Layer.getFeatureTable());

                                map.getOperationalLayers().add(mCurrent.Others_Layer);

                                Log.i(TAG, "addLocalLayers(): LayerName is " + mCurrent.OthersTableOffline.getTableName());

                                loadLayerAsyncOffline(mCurrent.Others_Layer);
                                loadTableAsyncOffline(mCurrent.OthersTableOffline);
                            }else if (OConstants.LAYER_PL.contains(gdbFeatureTable.getTableName())) {
                                mCurrent.FCL_PL_Layer = new FeatureLayer(gdbFeatureTable);
                                mCurrent.FCL_PL_TableOffline = ((GeodatabaseFeatureTable) mCurrent.FCL_PL_Layer.getFeatureTable());

                                map.getOperationalLayers().add(mCurrent.FCL_PL_Layer);

                                Log.i(TAG, "addLocalLayers(): LayerName is " + mCurrent.FCL_PL_TableOffline.getTableName());

                                loadLayerAsyncOffline(mCurrent.FCL_PL_Layer);
                                loadTableAsyncOffline(mCurrent.FCL_PL_TableOffline);
                            }

                            mapView.setMap(map);

                        }


                        zoomToArea(dbTitle, null);
                        mCurrent.runOnUiThread(() -> {
                            try {
                                String surveyorCode = getSurveyorCode(mCurrent.prefManager);
                                querySurveyorAreaOffline(surveyorCode, mapView.getSpatialReference(), mCurrent.SurveyorTableOffline, mCurrent.Surveyor_Layer);

                                mCurrent.currentOfflineVersion = databaseNumber;
                                mCurrent.item_load_previous_offline.setVisible(true);
                                mCurrent.menuItemGoOfflineMode.setVisible(false);
                                mCurrent.menuItemGoOnlineMode.setVisible(true);
                                mCurrent.menuItemOffline.setVisible(false);
                                mCurrent.menuItemSync.setVisible(true);
//                              mCurrent.menuItemOnline.setVisible(true);
                                if (isLocalGeoDatabase()) {
                                    mCurrent.menuItemLoad.setVisible(false);
                                } else {
                                    mCurrent.menuItemLoad.setVisible(true);
                                }

                                Utilities.dismissLoadingDialog();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });
                    } else if (geodatabase.getLoadStatus() == LoadStatus.FAILED_TO_LOAD || geodatabase.getLoadStatus() == LoadStatus.NOT_LOADED) {
//                        Utilities.showToast(mCurrent, "Geodatabase failed to load!");

                        Log.e(TAG, "Geodatabase failed to load!  " + geodatabase.getLoadError().getMessage());
                        addLocalLayers(mapView, map, databaseNumber, dbTitle);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));


        } catch (Exception e) {
            Log.i(TAG, "Error in adding feature layers from Local Geo Database");
            e.printStackTrace();
        }
    }

    void addLocalPLLayer(final MapView mapView, ArcGISMap map, final int databaseNumber, String dbTitle) {

        mCurrent.onlineData = false;

        Log.i(TAG, "Removing all the features layers from map");

        map.getOperationalLayers().clear();

        Log.i(TAG, "addLocalLayers(): Add features layers from Local Geo Database");

        Geodatabase geodatabase;

        try {

            String databasePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + IMAGE_FOLDER_NAME + File.separator + ROOT_GEO_DATABASE_PATH + File.separator + dbTitle + "_pl" + ".geodatabase";
            Log.i(TAG, "addLocalLayers(): database path = " + databasePath);

            geodatabase = new Geodatabase(databasePath);
            geodatabase.loadAsync();
            geodatabase.addDoneLoadingListener(() -> mCurrent.runOnUiThread(() -> {
                try {
                    Log.i(TAG, "addLocalLayers(): tables count = " + geodatabase.getGeodatabaseFeatureTables().size());
                    if (geodatabase.getLoadStatus() == LoadStatus.LOADED) {
                        Log.i(TAG, "addLocalLayers(): tables count = " + geodatabase.getGeodatabaseFeatureTables().size());

                        for (GeodatabaseFeatureTable gdbFeatureTable : geodatabase.getGeodatabaseFeatureTables()) {

                            Log.i(TAG, "addLocalLayers(): gdb Feature Table has geometry");

                            if (OConstants.LAYER_SUB_STATION.contains(gdbFeatureTable.getTableName())) {
                                mCurrent.FCL_PL_Layer = new FeatureLayer(gdbFeatureTable);
                                mCurrent.FCL_PL_TableOffline = ((GeodatabaseFeatureTable) mCurrent.FCL_PL_Layer.getFeatureTable());

                                map.getOperationalLayers().add(mCurrent.FCL_PL_Layer);

                                Log.i(TAG, "addLocalLayers(): LayerName is " + mCurrent.FCL_PL_TableOffline.getTableName());

                                loadLayerAsyncOffline(mCurrent.FCL_PL_Layer);
                                loadTableAsyncOffline(mCurrent.FCL_PL_TableOffline);
                            }
                            mapView.setMap(map);

                        }


//                        if (mCurrent.FCL_PL_Layer != null) {
//                            zoomToArea(dbTitle, new Viewpoint(mCurrent.FCL_PL_Layer.getFullExtent().getExtent()));
//                        }
                        mCurrent.runOnUiThread(() -> {
                            try {
                                String surveyorCode = getSurveyorCode(mCurrent.prefManager);
//                                querySurveyorAreaOffline(surveyorCode, mapView.getSpatialReference(), mCurrent.SurveyorTableOffline, mCurrent.Surveyor_Layer);

                                mCurrent.currentOfflineVersion = databaseNumber;
                                mCurrent.item_load_previous_offline.setVisible(true);
                                mCurrent.menuItemGoOfflineMode.setVisible(false);
                                mCurrent.menuItemGoOnlineMode.setVisible(true);
                                mCurrent.menuItemOffline.setVisible(false);
                                mCurrent.menuItemSync.setVisible(true);
//                              mCurrent.menuItemOnline.setVisible(true);
                                if (isLocalGeoDatabase()) {
                                    mCurrent.menuItemLoad.setVisible(false);
                                } else {
                                    mCurrent.menuItemLoad.setVisible(true);
                                }

//                                Utilities.dismissLoadingDialog();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });
                    } else if (geodatabase.getLoadStatus() == LoadStatus.FAILED_TO_LOAD || geodatabase.getLoadStatus() == LoadStatus.NOT_LOADED) {
//                        Utilities.showToast(mCurrent, "Geodatabase failed to load!");

                        Log.e(TAG, "Geodatabase failed to load!  " + geodatabase.getLoadError().getMessage());
//                        addLocalPLLayer(mapView, map, databaseNumber, dbTitle);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));


        } catch (Exception e) {
            Log.i(TAG, "Error in adding feature layers from Local Geo Database");
            e.printStackTrace();
        }
    }

    private void zoomToArea(String title, Viewpoint mViewpoint) {
        try {
            boolean founded = false;
            ArrayList<BookMark> bookMarks = DataCollectionApplication.getAllBookMarks();
            for (BookMark bookMark : bookMarks) {
                if (bookMark.getTitle().matches(title)) {
                    Viewpoint viewpoint = Viewpoint.fromJson(bookMark.getJson());
                    mCurrent.mapView.setViewpoint(viewpoint);
                    founded = true;
                    Log.e(TAG, "zoomToArea: zoom found");
                    break;
                }
            }
            if (!founded) {
                Log.e(TAG, "zoomToArea: no zoom found");
                Viewpoint viewpoint = new Viewpoint(new Point(4655017.6699, 2818095.9426, mCurrent.mapView.getSpatialReference()), 12500000);
                mCurrent.mapView.setViewpoint(viewpoint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isLocalGeoDatabase() {
        ArrayList<String> databaseTitles = DataCollectionApplication.getOfflineDatabasesTitle();
        for (String title : databaseTitles) {
            if (title != null)
                return false;
        }
        DataCollectionApplication.resetDatabaseNumber();
        return true;
    }

    void syncData(String dbTitle) {
        try {
            GeodatabaseSyncTask mGeodatabaseSyncTask = new GeodatabaseSyncTask(mCurrent.getString(R.string.gcs_feature_server_test));
            Geodatabase mGeodatabase;

            String databasePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + IMAGE_FOLDER_NAME + File.separator + ROOT_GEO_DATABASE_PATH + File.separator + dbTitle + ".geodatabase";
            Log.i(TAG, "addLocalLayers(): database path = " + databasePath);

            mGeodatabase = new Geodatabase(databasePath);
            mGeodatabase.loadAsync();

            mGeodatabase.addLoadStatusChangedListener(new LoadStatusChangedListener() {
                @Override
                public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                    if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.LOADED) {
                        Log.d(TAG, "loadStatusChanged: geoDatabase LOADED!");

                        try {
                            // create parameters for the sync task
                            SyncGeodatabaseParameters syncGeodatabaseParameters = new SyncGeodatabaseParameters();
                            syncGeodatabaseParameters.setSyncDirection(SyncGeodatabaseParameters.SyncDirection.BIDIRECTIONAL);
//                            syncGeodatabaseParameters.setRollbackOnFailure(false);

                            // get the layer ID for each feature table in the geodatabase, then add to the sync job
                            for (GeodatabaseFeatureTable geodatabaseFeatureTable : mGeodatabase.getGeodatabaseFeatureTables()) {
                                long serviceLayerId = geodatabaseFeatureTable.getServiceLayerId();
                                SyncLayerOption syncLayerOption = new SyncLayerOption(serviceLayerId);
                                syncGeodatabaseParameters.getLayerOptions().add(syncLayerOption);
                            }


                            final SyncGeodatabaseJob syncGeodatabaseJob = mGeodatabaseSyncTask.syncGeodatabase(syncGeodatabaseParameters, mGeodatabase);

                            createProgressDialog(syncGeodatabaseJob);

                            syncGeodatabaseJob.addJobDoneListener(() -> {
                                if (syncGeodatabaseJob.getStatus() == Job.Status.SUCCEEDED) {
                                    Log.d(TAG, "Job sync SUCCEEDED!");
                                    try {
                                        Log.d(TAG, "loadStatusChanged: syncLayerResult size = " + syncGeodatabaseJob.getResult().size());
                                        for (SyncLayerResult syncLayerResult : syncGeodatabaseJob.getResult()) {
                                            Log.d(TAG, "loadStatusChanged: Layer " + syncLayerResult.getTableName()
                                                    + "synced size " + syncLayerResult.getEditResults().size());
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    listener.onSyncSuccess(true);
                                } else if (syncGeodatabaseJob.getStatus() == Job.Status.FAILED || syncGeodatabaseJob.getStatus() != Job.Status.SUCCEEDED) {
                                    Log.e(TAG, "Job sync FAILED! " + syncGeodatabaseJob.getError());
                                    listener.onSyncSuccess(false);
                                } else if (syncGeodatabaseJob.getStatus() == Job.Status.NOT_STARTED) {
                                    Log.e(TAG, "Job did NOT_STARTED!");
                                } else if (syncGeodatabaseJob.getStatus() == Job.Status.PAUSED) {
                                    Log.e(TAG, "Job PAUSED!");
                                }
                            });

                            syncGeodatabaseJob.start();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.LOADING) {
                        Log.d(TAG, "loadStatusChanged: LOADING");
                    } else if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                        Log.d(TAG, "loadStatusChanged: FAILED_TO_LOAD");
                        listener.onSyncSuccess(false);
                    } else if (loadStatusChangedEvent.getNewLoadStatus() == LoadStatus.NOT_LOADED) {
                        Log.d(TAG, "loadStatusChanged: NOT_LOADED");
                        listener.onSyncSuccess(false);
                    }
                }
            });
            mGeodatabase.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
//                    try {
//                        // create parameters for the sync task
//                        SyncGeodatabaseParameters syncGeodatabaseParameters = new SyncGeodatabaseParameters();
//                        syncGeodatabaseParameters.setSyncDirection(SyncGeodatabaseParameters.SyncDirection.BIDIRECTIONAL);
//                        syncGeodatabaseParameters.setRollbackOnFailure(true);
//                        // get the layer ID for each feature table in the geodatabase, then add to the sync job
//                        for (GeodatabaseFeatureTable geodatabaseFeatureTable : mGeodatabase.getGeodatabaseFeatureTables()) {
//                            long serviceLayerId = geodatabaseFeatureTable.getServiceLayerId();
//                            SyncLayerOption syncLayerOption = new SyncLayerOption(serviceLayerId);
//                            syncGeodatabaseParameters.getLayerOptions().add(syncLayerOption);
//                        }
//
//                        final SyncGeodatabaseJob syncGeodatabaseJob = mGeodatabaseSyncTask.syncGeodatabase(syncGeodatabaseParameters, mGeodatabase);
//
//                        syncGeodatabaseJob.start();
//
//                        createProgressDialog(syncGeodatabaseJob);
//
//                        syncGeodatabaseJob.addJobDoneListener(() -> {
//                            if (syncGeodatabaseJob.getStatus() == Job.Status.SUCCEEDED) {
//                                listener.onSyncSuccess(true);
////                                mGeodatabaseButton.setVisibility(View.INVISIBLE);
//                            } else {
//                                Log.e(TAG, "Database did not sync correctly!");
//                                listener.onSyncSuccess(false);
//                            }
//                        });
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createProgressDialog(Job job) {
        try {
            ProgressDialog syncProgressDialog = new ProgressDialog(mCurrent);
            syncProgressDialog.setTitle("Sync Geodatabase Job");
            syncProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            syncProgressDialog.setCanceledOnTouchOutside(false);
            syncProgressDialog.show();

            job.addProgressChangedListener(() -> syncProgressDialog.setProgress(job.getProgress()));

            job.addJobDoneListener(syncProgressDialog::dismiss);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void querySurveyorAreaOnline(String code, SpatialReference sp, FeatureLayer layer) {
        try {
            QueryParameters query = QueryConfig.getSurveyorAreaQuery(sp, code, true);

            final ListenableFuture<FeatureQueryResult> future = layer.getFeatureTable().queryFeaturesAsync(query);
            // add done loading listener to fire when the selection returns

            future.addDoneListener(() -> {
                try {
                    ArrayList<Polygon> polygonList = new ArrayList<>();
                    // call get on the future to get the result
                    FeatureQueryResult result = future.get();
                    // check there are some results
                    Iterator<Feature> resultIterator = result.iterator();
                    if (resultIterator.hasNext()) {
                        while (resultIterator.hasNext()) {
                            // get the extent of the first feature in the result to zoom to

                            ArcGISFeature feature = (ArcGISFeature) resultIterator.next();
                            feature.loadAsync();
                            Polygon polygon = (Polygon) feature.getGeometry();
                            polygonList.add(polygon);

                            // select the feature
//                        mFeatureLayer.selectFeature(feature);
                            Log.i(TAG, "queryOnline(): Feature founded with id = " + feature.getAttributes().get(Columns.ObjectID));

                        }
                        if (listener != null) {
                            listener.onQuerySurveyorAreaOnline(polygonList);
                        }
                    } else {
                        Log.e(TAG, "queryOnline(): No states found ");
                        listener.onQuerySurveyorAreaOnline(polygonList);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onQuerySurveyorAreaOnline(null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            listener.onQuerySurveyorAreaOnline(null);
        }
    }

    private void querySurveyorAreaOffline(String code, SpatialReference sp, GeodatabaseFeatureTable mGeodatabaseFeatureTable, FeatureLayer layer) {
        try {
            QueryParameters query = QueryConfig.getSurveyorAreaQuery(sp, code, true);

            mGeodatabaseFeatureTable.loadAsync();
            mGeodatabaseFeatureTable.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ListenableFuture<FeatureQueryResult> future = mGeodatabaseFeatureTable.queryFeaturesAsync(query);
                        // add done loading listener to fire when the selection returns

                        future.addDoneListener(() -> {
                            try {
                                ArrayList<Polygon> polygonList = new ArrayList<>();
                                // call get on the future to get the result
                                FeatureQueryResult result = future.get();
                                // check there are some results
                                Iterator<Feature> resultIterator = result.iterator();
                                if (resultIterator.hasNext()) {
                                    while (resultIterator.hasNext()) {
                                        // get the extent of the first feature in the result to zoom to

                                        ArcGISFeature feature = (ArcGISFeature) resultIterator.next();
                                        feature.loadAsync();
                                        Polygon polygon = (Polygon) feature.getGeometry();
                                        polygonList.add(polygon);

                                        // select the feature
//                                      mFeatureLayer.selectFeature(feature);

                                        Log.i(TAG, "queryOnline(): Feature founded with id = " + feature.getAttributes().get(Columns.ObjectID));
                                    }

                                    if (listener != null) {
                                        listener.onQuerySurveyorAreaOnline(polygonList);
                                    }
                                } else {
                                    Log.e(TAG, "queryOnline(): No states found ");
                                    listener.onQuerySurveyorAreaOnline(polygonList);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                listener.onQuerySurveyorAreaOnline(null);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            listener.onQuerySurveyorAreaOnline(null);
        }
    }

    String getSurveyorCode(PrefManager prefManager) {
        return prefManager.readString(PrefManager.KEY_SURVEYOR_CODE);
    }

    void handleUploadImages() {
        try {
            mCurrent.startActivity(new Intent(mCurrent, UploadImageActivity.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void loadImagesIntoDBOnce() {
        try {
            imageUtils.handleLoadImages();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnImageLoadedIntoDb(boolean status, Throwable t, String date) {
        if (!status) {
            t.printStackTrace();
        }
    }

    boolean isValidDatabaseName(String dbTitle) {
        String folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + IMAGE_FOLDER_NAME + File.separator + ROOT_GEO_DATABASE_PATH + File.separator;
        File file = new File(folder);
        if (file != null && file.exists()) {
            File[] result = imageUtils.getFiles(file, null);
            if (result != null && result.length != 0) {
                for (File db : result) {
                    Log.d(TAG, "isValidDatabaseName: db name = " + db.getName());
                    if (db.getName().equals(dbTitle.concat(".").concat(ROOT_GEO_DATABASE_PATH))) {
                        Log.d(TAG, "isValidDatabaseName: db name founded ");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    File[] getFiles(File folder, String filter) {
        return folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
                        || (filter != null && name.contains(filter)) || (filter == null));
            }
        });
    }

    public Map<String, String> loadDBTitles() {
        String geoDatabasesRootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + IMAGE_FOLDER_NAME + File.separator + ROOT_GEO_DATABASE_PATH + File.separator;
        String extension = ".geodatabase";
        File geoDatabasesRoot = new File(geoDatabasesRootPath);

        File[] dbFiles = getFiles(geoDatabasesRoot, extension);

        if (dbFiles != null && dbFiles.length > 0) {
            Map<String, String> result = new HashMap<>();
            for (File dbFile : dbFiles) {
                String name = dbFile.getName().split("\\.")[0];
                result.put(name, dbFile.getPath());
            }
            return result;
        }
        return null;
    }
}
