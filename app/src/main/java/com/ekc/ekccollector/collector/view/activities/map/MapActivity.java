package com.ekc.ekccollector.collector.view.activities.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ekc.ekccollector.R;
import com.ekc.ekccollector.collector.DataCollectionApplication;
import com.ekc.ekccollector.collector.model.PrefManager;
import com.ekc.ekccollector.collector.model.models.BookMark;
import com.ekc.ekccollector.collector.model.models.Columns;
import com.ekc.ekccollector.collector.model.models.DeviceNoRealm;
import com.ekc.ekccollector.collector.model.models.OnlineQueryResult;
import com.ekc.ekccollector.collector.model.singleton.ThreadSingleton;
import com.ekc.ekccollector.collector.view.activities.MapSingleTapListener;
import com.ekc.ekccollector.collector.view.activities.login.LoginActivity;
import com.ekc.ekccollector.collector.view.adapter.BookMarkAdapter;
import com.ekc.ekccollector.collector.view.adapter.DBTitleAdapter;
import com.ekc.ekccollector.collector.view.adapter.MultiResultRecAdapter;
import com.ekc.ekccollector.collector.view.callbacks.mapCallbacks.SingleTapListener;
import com.ekc.ekccollector.collector.view.fragments.EditFeatureFragment;
import com.ekc.ekccollector.collector.view.fragments.featureDetails.DetailsFragment;
import com.ekc.ekccollector.collector.view.utils.DateUtils;
import com.ekc.ekccollector.collector.view.utils.Utilities;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.WrapAroundMode;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class MapActivity extends AppCompatActivity implements SingleTapListener, View.OnClickListener, MapActivityListener, MultiResultRecAdapter.MultiResultListener {


    private static final String TAG = "MapActivity";
    public static final String DOWNLOAD_GEO_DATABASE = "DOWNLOAD_GEO_DATABASE";
    public static final String POLYLINE = "PolyLine";
    public static final String POLYGON = "Polygon";
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 2;

    @BindView(R.id.mapView)
    public MapView mapView;

    @BindView(R.id.rlFragment)
    public RelativeLayout rlFragment;

    @BindView(R.id.fab_general)
    public FloatingActionMenu fabGeneral;

    @BindView(R.id.fab_add_distribution_box)
    public com.github.clans.fab.FloatingActionButton fabDistributionBox;

    @BindView(R.id.fab_add_poles)
    public com.github.clans.fab.FloatingActionButton fabPoles;

    @BindView(R.id.fab_add_rmu)
    public com.github.clans.fab.FloatingActionButton fabRMU;

    @BindView(R.id.fab_add_sub_station)
    public com.github.clans.fab.FloatingActionButton fabSubStation;

    @BindView(R.id.fab_add_ocl_meter)
    public com.github.clans.fab.FloatingActionButton fabOCLMeter;

    @BindView(R.id.fab_add_service_point)
    public com.github.clans.fab.FloatingActionButton fabServicePoint;

    @BindView(R.id.fab_add_other)
    public com.github.clans.fab.FloatingActionButton fabAddOther;

    @BindView(R.id.fab_add_pl)
    public com.github.clans.fab.FloatingActionButton fabAddPL;
    @BindView(R.id.fabLocation)
    public FloatingActionButton fabLocation;

    @BindView(R.id.fabFullScreen)
    public FloatingActionButton fabFullScreen;

    @BindView(R.id.fab_measure)
    FloatingActionMenu mFabMeasureMenu;

    @BindView(R.id.fab_measure_distance)
    public com.github.clans.fab.FloatingActionButton fabMeasureDistance;

    @BindView(R.id.fab_measure_area)
    public com.github.clans.fab.FloatingActionButton fabMeasureArea;

    @BindView(R.id.linear_layers_info)
    LinearLayout mapLegend;

    @BindView(R.id.tvLatLong)
    TextView tvLatLong;

    @BindView(R.id.tv_more_layer_info)
    TextView tvMoreLayerInfo;

    @BindView(R.id.linear_layers_details)
    LinearLayout mapLegendContainer;

    @BindView(R.id.compass)
    public ImageView mCompass;

    @BindView(R.id.map_layout)
    ConstraintLayout mConstraintLayout;

    @BindView(R.id.bottom_sheet)
    LinearLayout mBottomSheet;

    @BindView(R.id.update_btn)
    Button mUpdateBtn;

    @BindView(R.id.cancel_btn)
    Button mCancelBtn;

    @BindView(R.id.add_btn)
    Button mCreatePointBtn;

    @BindView(R.id.cancel_button)
    Button mCancelCreatePointBtn;

    @BindView(R.id.edit_point_bottom_sheet_container)
    LinearLayout mEditPointLayout;

    @BindView(R.id.add_point_bottom_sheet_container)
    LinearLayout mAddPointLayout;

    Point pointToAdd;

    @BindView(R.id.measure_info)
    LinearLayout mMeasureLayerInfo;

    @BindView(R.id.measure_function_in_meter_lbl)
    TextView mMeasureInMeterLbl;

    @BindView(R.id.measure_function_value_in_meter_lbl)
    TextView mMeasureValueInMeterLbl;

    @BindView(R.id.measure_function_in_km_lbl)
    TextView mMeasureInKMLbl;

    @BindView(R.id.measure_function_value_in_km_lbl)
    TextView mMeasureValueInKMLbl;


    //Multi Result Container
    @BindView(R.id.map_select_bottom_sheet_edit_multi_result_container)
    CardView mMultiResultContainer;

    @BindView(R.id.map_select_bottom_sheet_multi_result_close_iv)
    ImageView mMultiResultCloseIV;

    @BindView(R.id.map_select_bottom_sheet_multi_result_recyclerview)
    RecyclerView mMultiResultRecyclerView;

    BottomSheetBehavior sheetBehavior;
    public MenuItem menuItemOnline;
    public MenuItem menuItemLoad;
    public MenuItem menuItemSync;
    public MenuItem menuItemOffline;
    public MenuItem item_load_previous_offline;
    public MenuItem menuItemGoOfflineMode;
    public MenuItem menuItemGoOnlineMode;
    public MenuItem menuItemOverflow;

    //EditInFeatureFragment editInFeatureFragment;
    View dialogView;
    private boolean isShowingLayerInfo;
    FloatingActionButton fabMeasure;
    Polygon poly;
    ActionMode drawToolsActionMode;
    public Matrix mMatrix;
    public Bitmap mBitmap;
    LocationManager manager;
    Geometry workingAreaGeometry;
    MapActivity mCurrent;
    MapSingleTapListener mapSingleTapListener;

    final int MY_LOCATION_REQUEST_CODE = 2;

    Point mCurrentLocation;
    FeatureLayer FCL_DistributionBoxLayer, FCL_POLES_Layer, FCL_RMU_Layer, FCL_Substation_Layer, OCL_METER_Layer, FCL_PL_Layer,/*ServicePoint_Layer,*/
            Surveyor_Layer, Others_Layer;
    ServiceFeatureTable FCL_DistributionBox_ServiceTable, FCL_POLES_ServiceTable, FCL_RMU_ServiceTable, FCL_Substation_ServiceTable, OCL_METER_ServiceTable, FCL_PL_ServiceTable, /*ServicePoint_ServiceTable,*/
            SurveyorTable, Others_ServiceTable;
    GeodatabaseFeatureTable FCL_DistributionBoxTableOffline, FCL_POLESTableOffline, FCL_RMUTableOffline, FCL_SubstationTableOffline, OCL_METERTableOffline, FCL_PL_TableOffline, /*ServicePointTableOffline,*/
            SurveyorTableOffline, OthersTableOffline;
    GraphicsOverlay graphicsOverlay;
    public boolean drawShape = false, frag_drawShape = false;
    public String shapeType;
    PictureMarkerSymbol pictureMarkerSymbol;
    ArcGISMap baseMap;
    MapPresenter presenter;
    OnlineQueryResult selectedResult;
    EditFeatureFragment editFeatureFragment;
    DetailsFragment detailsFragment;

    public boolean onlineData = true;
    private boolean queryStatus = false;
    private boolean isFragmentShown = false;
    private FragmentManager fragmentManager;
    private boolean isFullScreenMode = false;
    private boolean isInDrawMood;
    private GraphicsOverlay drawGraphicLayer;
    private String localDatabaseTitle;
    public int currentOfflineVersion;
    public String currentOfflineVersionTitle;
    private boolean syncAndGoOnline;
    private int status = 2;

    PointCollection pointCollection;
    Point lastPointStep;
    private boolean drawMeasure = false;

    ArrayList<Polygon> mSurveyorAreaPolygon;
    PrefManager prefManager;
    String surveyorCode;
    private MultiResultRecAdapter mMultiResultRecAdapter;
    private Realm realm;
    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_TAKE_PICTURE = 2;
    private static final int WRITE_EXTERNAL_STORAGE = 3;
    private static final int READ_EXTERNAL_STORAGE = 4;
    private static final int REQUEST_CODE_VIDEO = 5;
    private static final int REQUEST_CODE_AUDIO = 6;
    private String surveyorName;
    private ThreadSingleton threadSingleton;
    private DBTitleAdapter adapter;
    private MaterialDialog dbDialog;

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.d(TAG, "onResume: is called");
            if (mapView != null && !isFragmentShown) {
                mapView.resume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (rlFragment.getVisibility() == View.VISIBLE) {

                if (editFeatureFragment != null && !editFeatureFragment.isZoomageDisplayed()) {
                    Utilities.showConfirmDialog(mCurrent, "", "هل انت متأكد من الرجوع و عدم الحفظ؟", (dialog, which) -> {
                        if (which == BUTTON_POSITIVE) {
                            hideFragment();//TODO UNCOMMENT
//                        dismissDetailsFragment();
                            showViews();
                        }
                        dialog.dismiss();
                    });
                } else if (editFeatureFragment != null && editFeatureFragment.isZoomageDisplayed()) {
                    editFeatureFragment.deleteTempFile();
                    editFeatureFragment.hideZoomgePreview();
                    editFeatureFragment.takePicture();
                }
            } else {
                Utilities.showConfirmDialog(mCurrent, "", "هل تريد الخروج؟", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == BUTTON_POSITIVE) {
                            finish();
                        }
                        dialog.dismiss();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

            init();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            mCurrent = MapActivity.this;

            ButterKnife.bind(mCurrent);

            threadSingleton = DataCollectionApplication.getThreadSingleton();
//            threadSingleton.initBgThread();//TODO Delete,Not Needed anymore 2/6/2020

            presenter = new MapPresenter(this, mCurrent, threadSingleton);

            fragmentManager = getSupportFragmentManager();

            prefManager = new PrefManager(mCurrent);
            surveyorName = prefManager.readString(PrefManager.KEY_SURVEYOR_NAME);


            sheetBehavior = BottomSheetBehavior.from(mBottomSheet);
            sheetBehavior.setPeekHeight(150, true);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//            mBottomSheet.setVisibility(View.GONE);

            mCompass.setScaleType(ImageView.ScaleType.MATRIX);
            mMatrix = new Matrix();
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_compass);

            initMapFabMenu();

            initSheetBehavior();

            initMap();

            intFabLocation();

//            requestPermission();
            tvMoreLayerInfo.setOnClickListener(this);
            fabFullScreen.setOnClickListener(this);
            //Compass rotation
            mCompass.setOnClickListener(this);
            //AddButton
            mCreatePointBtn.setOnClickListener(this);
            mCancelCreatePointBtn.setOnClickListener(this);
            realm = Realm.getDefaultInstance();

        } catch (Exception e) {
            Utilities.showToast(mCurrent, e.getMessage());
        }
    }

    private void requestPermission() {
        try {
            Log.i(TAG, "requestPermission: is called");
            Log.i(TAG, "requestPermission: check external storage permission");
            if (ActivityCompat.checkSelfPermission(mCurrent, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "requestPermission: external storage permission not granted");
                ActivityCompat.requestPermissions(mCurrent, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
            }
            Log.i(TAG, "requestPermission: CAMERA permission");

            if (ActivityCompat.checkSelfPermission(mCurrent, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "requestPermission: CAMERA permission not granted");
                ActivityCompat.requestPermissions(mCurrent, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_TAKE_PICTURE);
            }
            Log.i(TAG, "requestPermission: READ_EXTERNAL_STORAGE permission");

            if (ActivityCompat.checkSelfPermission(mCurrent, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "requestPermission: CAMERA READ_EXTERNAL_STORAGE not granted");
                ActivityCompat.requestPermissions(mCurrent, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMapFabMenu() {
        try {

            fabGeneral.setClosedOnTouchOutside(true);

            fabDistributionBox.setOnClickListener(this);
            fabPoles.setOnClickListener(this);
            fabRMU.setOnClickListener(this);
            fabSubStation.setOnClickListener(this);
            fabOCLMeter.setOnClickListener(this);
            fabServicePoint.setOnClickListener(this);
            fabAddOther.setOnClickListener(this);
            fabAddPL.setOnClickListener(this);
            mFabMeasureMenu.setClosedOnTouchOutside(true);

            fabMeasureArea.setOnClickListener(this);
            fabMeasureDistance.setOnClickListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSheetBehavior() {
        try {
            mCancelBtn.setOnClickListener(this);
            mUpdateBtn.setOnClickListener(this);
            /**
             * bottom sheet state change listener
             * we are changing button text when sheet changed state
             * */
            sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        graphicsOverlay.getGraphics().clear();
                        selectedResult = null;
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void intFabLocation() {
        try {
            fabLocation.setOnClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMap() {
        try {

            //Declaring baseMap
            baseMap = new ArcGISMap(Basemap.createOpenStreetMap());
            mapView.setMap(baseMap);

            pointCollection = new PointCollection(mapView.getSpatialReference());

            // wraparound is enabled if layers within map support it
            mapView.setWrapAroundMode(WrapAroundMode.ENABLE_WHEN_SUPPORTED);

            graphicsOverlay = new GraphicsOverlay();
            pictureMarkerSymbol = new PictureMarkerSymbol((BitmapDrawable) getResources().getDrawable(R.drawable.ic_marker_64));
            pictureMarkerSymbol.setOffsetY(10f);

            mapView.getGraphicsOverlays().add(graphicsOverlay);

            if (checkLocationPermissions()) {

                // displaying user location on map
                showDeviceLocation();
            }


            initOnlineLayers(baseMap, mapView);

            surveyorCode = presenter.getSurveyorCode(prefManager);


            if (!surveyorCode.equals("100") && Utilities.isNetworkAvailable(mCurrent)) {

                presenter.querySurveyorAreaOnline(surveyorCode, mapView.getSpatialReference(), Surveyor_Layer);

            } else {
//                presenter.querySurveyorAreaOffline(surveyorCode, mapView.getSpatialReference(), SurveyorTableOffline,Surveyor_Layer);
            }

            // init map rotation
//            initMapRotation();

            //initSingleTap
            initSingleTap();
        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showToast(mCurrent, e.getMessage());
        }
    }

    private void initOnlineLayers(ArcGISMap baseMap, MapView mapView) {
        try {

            // create feature layer with its service feature table
            // create the service feature table
//            ArcGISMapImageLayer plImageLayer = new ArcGISMapImageLayer(getString(R.string.substation_pl));

            FCL_DistributionBox_ServiceTable = new ServiceFeatureTable(getString(R.string.FCL_DISTRIBUTIONBOX));
            FCL_POLES_ServiceTable = new ServiceFeatureTable(getString(R.string.FCL_POLES));
            FCL_RMU_ServiceTable = new ServiceFeatureTable(getString(R.string.FCL_RMU));
            FCL_Substation_ServiceTable = new ServiceFeatureTable(getString(R.string.FCL_Substation));
            OCL_METER_ServiceTable = new ServiceFeatureTable(getString(R.string.OCL_METER));
            SurveyorTable = new ServiceFeatureTable(getString(R.string.Surveyor));
            Others_ServiceTable = new ServiceFeatureTable(getString(R.string.othersLayer));
            FCL_PL_ServiceTable = new ServiceFeatureTable(getString(R.string.substation_pl));

            // create the feature layer using the service feature table
            FCL_DistributionBoxLayer = new FeatureLayer(FCL_DistributionBox_ServiceTable);
            FCL_POLES_Layer = new FeatureLayer(FCL_POLES_ServiceTable);
            FCL_RMU_Layer = new FeatureLayer(FCL_RMU_ServiceTable);
            FCL_Substation_Layer = new FeatureLayer(FCL_Substation_ServiceTable);
            OCL_METER_Layer = new FeatureLayer(OCL_METER_ServiceTable);
            Surveyor_Layer = new FeatureLayer(SurveyorTable);
            Others_Layer = new FeatureLayer(Others_ServiceTable);
            FCL_PL_Layer = new FeatureLayer(FCL_PL_ServiceTable);

            // add the layer to the map
//            baseMap.getOperationalLayers().add(plImageLayer);
            baseMap.getOperationalLayers().add(FCL_DistributionBoxLayer);
            baseMap.getOperationalLayers().add(FCL_POLES_Layer);
            baseMap.getOperationalLayers().add(FCL_RMU_Layer);
            baseMap.getOperationalLayers().add(FCL_Substation_Layer);
            baseMap.getOperationalLayers().add(OCL_METER_Layer);
            baseMap.getOperationalLayers().add(Surveyor_Layer);
            baseMap.getOperationalLayers().add(Others_Layer);
            baseMap.getOperationalLayers().add(FCL_PL_Layer);

            // set the map to be displayed in the mapView
            baseMap.setMinScale(40000000);
            baseMap.setMaxScale(100);
            mapView.setMap(baseMap);

            status = Columns.EditPoint;

            loadSurveyorArea();
        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showToast(mCurrent, e.getMessage());
        }
    }

    private void loadSurveyorArea() {
        try {
            Log.i(TAG, "loadSurveyorArea(): is called");
            Surveyor_Layer.loadAsync();
            Surveyor_Layer.addLoadStatusChangedListener(new LoadStatusChangedListener() {
                @Override
                public void loadStatusChanged(LoadStatusChangedEvent loadStatusChangedEvent) {
                    try {
                        if (loadStatusChangedEvent.getNewLoadStatus().ordinal() == LoadStatus.FAILED_TO_LOAD.ordinal()) {
                            Log.i(TAG, "loadSurveyorArea(): surveyor area is FAILED_TO_LOAD");
                            loadSurveyorArea();
                        } else if (loadStatusChangedEvent.getNewLoadStatus().ordinal() == LoadStatus.NOT_LOADED.ordinal()) {
                            Log.i(TAG, "loadSurveyorArea(): surveyor area is NOT_LOADED");
                            loadSurveyorArea();

                        } else if (loadStatusChangedEvent.getNewLoadStatus().ordinal() == LoadStatus.LOADED.ordinal()) {
                            Log.i(TAG, "loadSurveyorArea(): surveyor area is LOADED");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void zoomToCurrentLocation() {
        try {
            mapView.setViewpoint(new Viewpoint(mCurrentLocation, 16.0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkLocationPermissions() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

//                ActivityCompat.requestPermissions(mCurrent,
//                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
//                        MY_LOCATION_REQUEST_CODE);
                ActivityCompat.requestPermissions(mCurrent,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_LOCATION_REQUEST_CODE);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSingleTap() {
        try {
            mapSingleTapListener = new MapSingleTapListener(mCurrent, mapView, this);
            mapView.setOnTouchListener(mapSingleTapListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDeviceLocation() {
        try {
            Log.i(TAG, "showDeviceLocation(): is called");

            LocationDisplay locationDisplay = mapView.getLocationDisplay();
            locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.NAVIGATION);

            locationDisplay.addLocationChangedListener(new LocationDisplay.LocationChangedListener() {
                @Override
                public void onLocationChanged(LocationDisplay.LocationChangedEvent locationChangedEvent) {
                    //reading location changing
                    mCurrentLocation = locationChangedEvent.getLocation().getPosition();

                    if (mCurrentLocation != null) {
                        double accuracy = locationChangedEvent.getLocation().getHorizontalAccuracy();
                        int mAccuracy = (int) (accuracy * 100);
                        accuracy = (double) mAccuracy / 100;
                        String latLang = "Lat: " + Utilities.round(mCurrentLocation.getX(), 5) + " Lan: " + Utilities.round(mCurrentLocation.getY(), 5) + " Accuracy = " + accuracy;
                        tvLatLong.setText(latLang);
                    }

                    mMatrix.reset();
                    mMatrix.postRotate(-(float) mapView.getRotation(), mBitmap.getHeight() / 2, mBitmap.getWidth() / 2);
                    mCompass.setImageMatrix(mMatrix);
                }
            });

            locationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {

                if (dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError() == null) {
                    Log.i(TAG, "Location Display Started=" + dataSourceStatusChangedEvent.isStarted());
                } else {
                    dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().printStackTrace();
                }
            });
            locationDisplay.startAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMapRotation() {
        try {
            Log.i(TAG, "initMapRotation(): is called");

            mMatrix.reset();
            final ListenableFuture<Boolean> viewpointSetFuture = mapView.setViewpointRotationAsync(0.0);
            viewpointSetFuture.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean completed = viewpointSetFuture.get();
                        if (completed) {
                            Log.i(TAG, "Rotation completed successfully");
                            mMatrix.postRotate(-(float) mapView.getRotation(), mBitmap.getHeight() / 2, mBitmap.getWidth() / 2);
                            mCompass.setImageMatrix(mMatrix);
                        }
                    } catch (InterruptedException e) {
                        Log.i(TAG, "Rotation interrupted");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        // Deal with exception during animation...
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            Log.d(TAG, "onPause: is called");
            if (mapView != null && !isFragmentShown) {
                mapView.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Log.d(TAG, "onDestroy: is called");
            if (mapView != null) {
                mapView.dispose();
            }

//            if (threadSingleton != null) {//TODO Delete,Not Needed anymore 2/6/2020
//                threadSingleton.dispatchBgThread();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            if (requestCode == MY_LOCATION_REQUEST_CODE) {
                if ((permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) || (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    showDeviceLocation();
                }
            } else if (((permissions[0].equals(Manifest.permission.BLUETOOTH_ADMIN) || (permissions[0].equals(Manifest.permission.BLUETOOTH))) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                Log.i(TAG, "onRequestPermissionsResult(): BLUETOOTH_ADMIN is granted");

            } /*else {
                    Utilities.showToast(mCurrent, getString(R.string.please_open_gps_location));
                }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.equals(fabLocation)) {
            if (mCurrentLocation != null) {
                zoomToCurrentLocation();
            }
        } else if (v.equals(tvMoreLayerInfo)) {
            if (mapLegendContainer.getVisibility() == View.GONE) {
                mapLegendContainer.setVisibility(View.VISIBLE);
            } else {
                mapLegendContainer.setVisibility(View.GONE);
            }
        } else if (v.equals(fabFullScreen)) {
            if (isFullScreenMode) {
                exitFullScreenMode();
            } else {
                fullScreenMode();
            }
        } else if (v.equals(mUpdateBtn)) {
            updatePoint();
        } else if (v.equals(mCancelBtn)) {
            cancelUpdateBtn();

        } else if (v.equals(mCompass)) {

            initMapRotation();

        } else if (v.equals(fabDistributionBox)) {
            fabGeneral.close(true);

            drawShape = true;
            frag_drawShape = true;
            selectedResult = new OnlineQueryResult();
            selectedResult.setGeodatabaseFeatureTable(FCL_DistributionBoxTableOffline);
            selectedResult.setServiceFeatureTable(FCL_DistributionBox_ServiceTable);
            selectedResult.setFeatureLayer(FCL_DistributionBoxLayer);
        } else if (v.equals(fabPoles)) {
            fabGeneral.close(true);

            drawShape = true;
            frag_drawShape = true;
            selectedResult = new OnlineQueryResult();
            selectedResult.setGeodatabaseFeatureTable(FCL_POLESTableOffline);
            selectedResult.setServiceFeatureTable(FCL_POLES_ServiceTable);
            selectedResult.setFeatureLayer(FCL_POLES_Layer);

        } else if (v.equals(fabRMU)) {
            fabGeneral.close(true);

            drawShape = true;
            frag_drawShape = true;
            selectedResult = new OnlineQueryResult();
            selectedResult.setGeodatabaseFeatureTable(FCL_RMUTableOffline);
            selectedResult.setServiceFeatureTable(FCL_RMU_ServiceTable);
            selectedResult.setFeatureLayer(FCL_RMU_Layer);

        } else if (v.equals(fabSubStation)) {
            fabGeneral.close(true);

            drawShape = true;
            frag_drawShape = true;
            selectedResult = new OnlineQueryResult();
            selectedResult.setGeodatabaseFeatureTable(FCL_SubstationTableOffline);
            selectedResult.setServiceFeatureTable(FCL_Substation_ServiceTable);
            selectedResult.setFeatureLayer(FCL_Substation_Layer);

        } else if (v.equals(fabOCLMeter)) {
            fabGeneral.close(true);

            drawShape = true;
            frag_drawShape = true;
            selectedResult = new OnlineQueryResult();
            selectedResult.setGeodatabaseFeatureTable(OCL_METERTableOffline);
            selectedResult.setServiceFeatureTable(OCL_METER_ServiceTable);
            selectedResult.setFeatureLayer(OCL_METER_Layer);

        } else if (v.equals(fabServicePoint)) {
//            fabGeneral.close(true);
//
//            drawShape = true;
//            frag_drawShape = true;
//            selectedResult = new OnlineQueryResult();
//            selectedResult.setGeodatabaseFeatureTable(ServicePointTableOffline);
//            selectedResult.setServiceFeatureTable(ServicePoint_ServiceTable);
//            selectedResult.setFeatureLayer(ServicePoint_Layer);

        } else if (v.equals(fabAddOther)) {
            fabGeneral.close(true);

            drawShape = true;
            frag_drawShape = true;
            selectedResult = new OnlineQueryResult();
            selectedResult.setGeodatabaseFeatureTable(OthersTableOffline);
            selectedResult.setServiceFeatureTable(Others_ServiceTable);
            selectedResult.setFeatureLayer(Others_Layer);

        }  else if (v.equals(fabAddPL)) {
            fabGeneral.close(true);

            drawShape = true;
            frag_drawShape = true;
            selectedResult = new OnlineQueryResult();
            selectedResult.setGeodatabaseFeatureTable(FCL_PL_TableOffline);
            selectedResult.setServiceFeatureTable(FCL_PL_ServiceTable);
            selectedResult.setFeatureLayer(FCL_PL_Layer);

        } else if (v.equals(mCreatePointBtn)) {
            createPoint();
        } else if (v.equals(mCancelCreatePointBtn)) {
            cancelCreatePoint();
        } else if (v.equals(fabMeasureDistance)) {
            handleFabMeasureAction();
            mFabMeasureMenu.close(true);

            drawMeasure = true;
            shapeType = POLYLINE;
        }
    }

    private void updatePoint() {
        try {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            if (graphicsOverlay != null && graphicsOverlay.getGraphics() != null) {
                graphicsOverlay.getGraphics().clear();

                Log.i(TAG, "updatePoint: status = " + status);
                showEditFragment(selectedResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelUpdateBtn() {
        try {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            if (graphicsOverlay != null && graphicsOverlay.getGraphics() != null) {
                graphicsOverlay.getGraphics().clear();
                graphicsOverlay = null;
                graphicsOverlay = new GraphicsOverlay();
                mapView.getGraphicsOverlays().add(graphicsOverlay);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelCreatePoint() {
        try {
            graphicsOverlay.getGraphics().clear();
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            mAddPointLayout.setVisibility(View.GONE);
            mEditPointLayout.setVisibility(View.GONE);
            drawShape = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createPoint() {
        try {
            Utilities.showLoadingDialog(mCurrent);

            graphicsOverlay.getGraphics().clear();
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            mAddPointLayout.setVisibility(View.GONE);
            mEditPointLayout.setVisibility(View.GONE);

            drawShape = false;

            if (onlineData) {
                drawOnlineMode();
            } else {
                drawOfflineMode();
            }


        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showToast(mCurrent, e.getMessage());
            Utilities.dismissLoadingDialog();
        }
    }

    private void drawOnlineMode() {
        try {
            selectedResult.getServiceFeatureTable().loadAsync();
            selectedResult.getServiceFeatureTable().addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "drawOnlineMode(): add Done listener is called");
                        Log.i(TAG, "drawOnlineMode(): loading service feature table is finished");
                        realm.executeTransactionAsync(realm -> {
                            RealmResults<DeviceNoRealm> results = realm.where(DeviceNoRealm.class).findAll();
                            if (results != null && !results.isEmpty()) {
                                Log.d(TAG, "drawOnlineMode: run: has a deviceNo");
                                DeviceNoRealm deviceNoRealm = results.last();
                                deviceNoRealm.setDeviceNo(deviceNoRealm.getDeviceNo() + 1);
                                String date = DateUtils.getCurrentFormattedDate(DateUtils.FORMAT_DATE_DASHED_INVERSE);
                                String deviceNo = getSurveyorAcronym(surveyorName).concat(date).concat("@").concat(String.valueOf(deviceNoRealm.getDeviceNo()));
                                Log.d(TAG, "drawOnlineMode: run: deviceNo = " + deviceNo);
                                continueCreatePointOnline(deviceNo);
                            } else {
                                Log.d(TAG, "drawOnlineMode: run: has a deviceNo");
                                DeviceNoRealm deviceNoRealm = new DeviceNoRealm();
                                deviceNoRealm.setDeviceNo(1);
                                deviceNoRealm.setSurveyorName(surveyorName);
                                realm.copyToRealm(deviceNoRealm);
                                String date = DateUtils.getCurrentFormattedDate(DateUtils.FORMAT_DATE_DASHED_INVERSE);
                                String deviceNo = getSurveyorAcronym(surveyorName).concat(date).concat("@").concat(String.valueOf(deviceNoRealm.getDeviceNo()));
                                Log.d(TAG, "drawOnlineMode: run: deviceNo = " + deviceNo);
                                continueCreatePointOnline(deviceNo);
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

    private void continueCreatePointOnline(String deviceNo) {
        try {
            Log.d(TAG, "continueCreatePointOnline: is called");
            Map<String, Object> attributes = new HashMap<>();

            attributes.put(Columns.Device_No, deviceNo);
            attributes.put(Columns.Code, "null");
            attributes.put(Columns.Status, Columns.CreatePoint);//TODO Uncomment
            attributes.put(Columns.SurveyorName, surveyorName);//TODO Uncomment
            attributes.put(Columns.Comments, 7);
            // creates a new feature using default attributes and point
            Feature feature = selectedResult.getServiceFeatureTable().createFeature(attributes, pointToAdd);

            // check if feature can be added to feature table
            if (selectedResult.getServiceFeatureTable().canAdd()) {
                Log.i(TAG, "continueCreatePoint(): can add feature");

                // add the new feature to the feature table and to server
                selectedResult.getServiceFeatureTable().addFeatureAsync(feature).addDoneListener(() -> applyEditsOnline(selectedResult.getServiceFeatureTable(), selectedResult));
            } else {
                Log.e(TAG, "continueCreatePoint(): cannot add feature");
                status = Columns.EditPoint;
                runOnUiThread(() -> {
                    Log.e(TAG, getString(R.string.error_cannot_add_to_feature_table));
                    Utilities.showToast(mCurrent, getString(R.string.error_cannot_add_to_feature_table));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawOfflineMode() {
        try {
            selectedResult.getGeodatabaseFeatureTable().loadAsync();
            selectedResult.getGeodatabaseFeatureTable().addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "drawOfflineMode(): add Done listener is called");
                        Log.i(TAG, "drawOfflineMode(): loading service feature table is finished");
                        realm.executeTransactionAsync(realm -> {
                            RealmResults<DeviceNoRealm> results = realm.where(DeviceNoRealm.class).findAll();
                            if (results != null && !results.isEmpty()) {
                                Log.d(TAG, "drawOfflineMode: run: has a deviceNo");
                                DeviceNoRealm deviceNoRealm = results.get(results.size() - 1);
                                deviceNoRealm.setDeviceNo(deviceNoRealm.getDeviceNo() + 1);
                                String date = DateUtils.getCurrentFormattedDate(DateUtils.FORMAT_DATE_DASHED_INVERSE);
                                String deviceNo = getSurveyorAcronym(surveyorName).concat(date).concat("@").concat(String.valueOf(deviceNoRealm.getDeviceNo()));
                                Log.d(TAG, "drawOfflineMode: run: deviceNo = " + deviceNo);
                                continueCreatePointOffline(deviceNo);
                            } else {
                                Log.d(TAG, "drawOfflineMode: run: not has a deviceNo");
                                DeviceNoRealm deviceNoRealm = new DeviceNoRealm();
                                deviceNoRealm.setDeviceNo(1);
                                deviceNoRealm.setSurveyorName(surveyorName);
                                realm.copyToRealm(deviceNoRealm);
                                String date = DateUtils.getCurrentFormattedDate(DateUtils.FORMAT_DATE_DASHED_INVERSE);
                                String deviceNo = getSurveyorAcronym(surveyorName).concat(date).concat("@").concat(String.valueOf(deviceNoRealm.getDeviceNo()));
                                Log.d(TAG, "drawOfflineMode: run: deviceNo = " + deviceNo);
                                continueCreatePointOffline(deviceNo);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utilities.showToast(mCurrent, getString(R.string.error_cannot_add_to_feature_table));
                        Utilities.dismissLoadingDialog();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showToast(mCurrent, getString(R.string.error_cannot_add_to_feature_table));
            Utilities.dismissLoadingDialog();
        }
    }

    private String getSurveyorAcronym(String surveyorName) {
        String result = "";

        if (surveyorName.split(" ").length == 2) {
            String[] split = surveyorName.trim().split(" ");
            result = split[0].toUpperCase().charAt(0) + "" + split[1].toUpperCase().charAt(0);
            Log.d(TAG, "getSurveyorAcronym: resulr = " + result);
            return result;
        }
        return surveyorName.toUpperCase();
    }

    private void continueCreatePointOffline(String deviceNo) {
        try {
            Log.d(TAG, "continueCreatePointOffline: is called");
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(Columns.Device_No, deviceNo);
            attributes.put(Columns.Code, "null");
            attributes.put(Columns.Status, Columns.CreatePoint);//TODO Uncomment
            attributes.put(Columns.SurveyorName, prefManager.readString(PrefManager.KEY_SURVEYOR_NAME));//TODO Uncomment
            attributes.put(Columns.Comments, 7);

            // creates a new feature using default attributes and point
            Feature feature = selectedResult.getGeodatabaseFeatureTable().createFeature(attributes, pointToAdd);

            // check if feature can be added to feature table
            if (selectedResult.getGeodatabaseFeatureTable().canAdd()) {
                Log.i(TAG, "continueCreatePointOffline(): can add feature");

                // add the new feature to the feature table and to server
                selectedResult.getGeodatabaseFeatureTable().addFeatureAsync(feature).addDoneListener(() -> applyEditsOffline(selectedResult.getGeodatabaseFeatureTable(), selectedResult));
            } else {
                Log.i(TAG, "continueCreatePointOffline(): cannot add feature");

                runOnUiThread(() -> {
                    Log.i(TAG, getString(R.string.error_cannot_add_to_feature_table));
                    Utilities.showToast(mCurrent, getString(R.string.error_cannot_add_to_feature_table));
                    Utilities.dismissLoadingDialog();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyEditsOnline(ServiceFeatureTable featureTable, OnlineQueryResult selectedResult) {
        Log.i(TAG, "applyEdits(): is called");

        featureTable.loadAsync();
        featureTable.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // apply the changes to the server
                    final ListenableFuture<List<FeatureEditResult>> editResult = featureTable.applyEditsAsync();
                    editResult.addDoneListener(() -> {
                        try {
                            Log.i(TAG, "applyEdits(): add Done listener is called");

                            List<FeatureEditResult> editResults = editResult.get();
                            // check if the server edit was successful
                            if (editResults != null && !editResults.isEmpty()) {
                                if (!editResults.get(0).hasCompletedWithErrors()) {
                                    Log.i(TAG, getString(R.string.feature_added));
                                    status = Columns.CreatePoint;
                                    Log.i(TAG, "applyEditsOnline: mStatus = " + status);
//                            selectedResult.setObjectID(String.valueOf(editResults.get(0).getObjectId()));
                                    queryOnAddedFeatureOnline(featureTable, selectedResult);

                                } else {
                                    throw editResults.get(0).getError();
                                }
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            runOnUiThread(() -> {
                                Log.i(TAG, getString(R.string.error_cannot_add_to_feature_table));
                                Utilities.showToast(mCurrent, e.getMessage());
                                Utilities.dismissLoadingDialog();
                            });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void applyEditsOffline(GeodatabaseFeatureTable featureTable, OnlineQueryResult selectedResult) {
        Log.i(TAG, "applyEdits(): is called");


        try {
            status = Columns.CreatePoint;
            Log.i(TAG, "applyEditsOffline: mStatus = " + status);

            queryOnAddedFeatureOffline(featureTable, selectedResult);


        } catch (Exception e) {
            runOnUiThread(() -> {
                Log.i(TAG, getString(R.string.error_cannot_add_to_feature_table));
                Utilities.showToast(mCurrent, e.getMessage());
                Utilities.dismissLoadingDialog();
            });
        }

    }

    private void queryOnAddedFeatureOnline(ServiceFeatureTable featureTable, OnlineQueryResult selectedResult) {
        try {

            QueryParameters queryParameters = new QueryParameters();
            queryParameters.setWhereClause("1 = 1");
            queryParameters.setGeometry(pointToAdd);
            queryParameters.setReturnGeometry(false);

            ListenableFuture<FeatureQueryResult> queryResult = featureTable.queryFeaturesAsync(queryParameters);
            queryResult.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        FeatureQueryResult result = queryResult.get();
                        Iterator<Feature> resultIterator = result.iterator();
                        if (resultIterator.hasNext()) {
                            while (resultIterator.hasNext()) {
                                // get the extent of the first feature in the result to zoom to

                                ArcGISFeature feature = (ArcGISFeature) resultIterator.next();
                                feature.loadAsync();

                                if (feature == null) {
                                    Log.i(TAG, "queryOnAddedFeature(): feature result = null");
                                }

                                if (selectedResult == null) {
                                    Log.i(TAG, "queryOnAddedFeature(): selected Query Online = null");
                                }
                                selectedResult.setFeature(feature);
                                selectedResult.setObjectID(String.valueOf(feature.getAttributes().get(Columns.ObjectID)));

                                Log.i(TAG, "queryOnAddedFeature(): Feature founded with id = " + feature.getAttributes().get(Columns.ObjectID));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utilities.dismissLoadingDialog();
                                        showEditFragment(selectedResult);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utilities.showToast(mCurrent, e.getMessage());
                        Utilities.dismissLoadingDialog();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showToast(mCurrent, e.getMessage());
            Utilities.dismissLoadingDialog();
        }
    }

    private void queryOnAddedFeatureOffline(GeodatabaseFeatureTable featureTable, OnlineQueryResult selectedResult) {
        try {

            QueryParameters queryParameters = new QueryParameters();
            queryParameters.setWhereClause("1 = 1");
            queryParameters.setGeometry(pointToAdd);
            queryParameters.setReturnGeometry(false);

            ListenableFuture<FeatureQueryResult> queryResult = featureTable.queryFeaturesAsync(queryParameters);
            queryResult.addDoneListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        FeatureQueryResult result = queryResult.get();
                        Iterator<Feature> resultIterator = result.iterator();
                        if (resultIterator.hasNext()) {
                            while (resultIterator.hasNext()) {
                                // get the extent of the first feature in the result to zoom to

                                Feature feature = resultIterator.next();

                                if (feature == null) {
                                    Log.i(TAG, "queryOnAddedFeature(): feature result = null");
                                }

                                if (selectedResult == null) {
                                    Log.i(TAG, "queryOnAddedFeature(): selected Query Online = null");
                                }
                                selectedResult.setGeodatabaseFeatureTable(featureTable);
                                selectedResult.setFeatureOffline(feature);
                                selectedResult.setObjectID(String.valueOf(feature.getAttributes().get(Columns.ObjectID)));

                                Log.i(TAG, "queryOnAddedFeature(): Feature founded with id = " + feature.getAttributes().get(Columns.ObjectID));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utilities.dismissLoadingDialog();
                                        showEditFragment(selectedResult);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utilities.showToast(mCurrent, e.getMessage());
                        Utilities.dismissLoadingDialog();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Utilities.showToast(mCurrent, e.getMessage());
            Utilities.dismissLoadingDialog();
        }
    }

    private void startDrawShape(Point point) {
        try {

            mAddPointLayout.setVisibility(View.VISIBLE);
            mEditPointLayout.setVisibility(View.GONE);
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            graphicsOverlay.getGraphics().clear();
            graphicsOverlay.getGraphics().add(new Graphic(point, pictureMarkerSymbol));
            pointToAdd = point;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exitFullScreenMode() {
        try {
            isFullScreenMode = false;
            fabFullScreen.setImageResource(R.drawable.ic_fullscreen_white_24dp);
            getSupportActionBar().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fabLocation.setVisibility(View.VISIBLE);
        fabGeneral.setVisibility(View.VISIBLE);
        tvLatLong.setVisibility(View.VISIBLE);

    }

    public void fullScreenMode() {
        try {
            isFullScreenMode = true;
            fabFullScreen.setImageResource(R.drawable.ic_fullscreen_exit_white_24dp);
            getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fabLocation.setVisibility(View.GONE);
        fabGeneral.setVisibility(View.GONE);
        tvLatLong.setVisibility(View.GONE);
    }

    public void showToolbar() {
        try {
            getSupportActionBar().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideToolbar() {
        try {
            getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.main_menu, menu);

            menuItemOffline = menu.findItem(R.id.item_go_offline);
            item_load_previous_offline = menu.findItem(R.id.item_load_previous_offline);
//            menuItemOnline = menu.findItem(R.id.item_go_online);
            menuItemSync = menu.findItem(R.id.item_sync);
            menuItemLoad = menu.findItem(R.id.item_load_previous);
            menuItemGoOfflineMode = menu.findItem(R.id.item_go_offline_mode);
            menuItemGoOnlineMode = menu.findItem(R.id.item_go_online_mode);
            menuItemOverflow = menu.findItem(R.id.overflow);


            if (presenter.isLocalGeoDatabase() && Utilities.isNetworkAvailable(this)) {
                menuItemLoad.setVisible(false);
            } else {
                menuItemLoad.setVisible(true);
            }

            if (!onlineData) {
                item_load_previous_offline.setVisible(true);
                menuItemOffline.setVisible(false);
                menuItemSync.setVisible(true);
//                menuItemOnline.setVisible(true);
                menuItemGoOfflineMode.setVisible(false);
                menuItemGoOnlineMode.setVisible(true);
            } else {
                item_load_previous_offline.setVisible(true);
                menuItemGoOfflineMode.setVisible(true);
                menuItemGoOnlineMode.setVisible(false);
            }
            Log.d("Test", "In OnCreate Options Menu");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_upload_images:
                handleUploadImages();
                break;
            case R.id.item_draw_polygon:

                break;
            case R.id.item_current_extent:
                // define permission to request
                String[] reqPermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

                if (ContextCompat.checkSelfPermission(mCurrent, reqPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                    // request permission
                    ActivityCompat.requestPermissions(mCurrent, reqPermission, WRITE_EXTERNAL_STORAGE_REQUEST);
                } else {
                    goOffline();
                }
                try {
                    menuItemGoOfflineMode.setVisible(false);
                    menuItemGoOnlineMode.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.item_load_previous:
                showOfflineMapsList(mCurrent, mapView);
                return true;
            case R.id.item_load_previous_offline:
                showOfflineMapsList(mCurrent, mapView);
                return true;
            /*case R.id.item_go_online:
                syncAndGoOnline();
                return true;*/
            case R.id.item_sync:
                syncData();
                return true;
            case R.id.item_Add_Bookmark:
                showAddNewBookmarkDialog();
                break;
            case R.id.item_Show_Bookmarks:
                showBookmarksDialog();
                break;
            case R.id.item_Logout:
                logout();
                break;
            /**------------------------------Ali Ussama Update------------------------------------*/
            case R.id.item_go_offline_mode:
                try {
                    goOffline();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.item_go_online_mode:
                try {
                    menuItemGoOfflineMode.setVisible(true);
                    menuItemGoOnlineMode.setVisible(false);
                    if (Utilities.isNetworkAvailable(mCurrent)) {
                        goOnline();
                    } else {
                        Utilities.showInfoDialog(mCurrent, getString(R.string.network_connection_failed), getString(R.string.please_your_network_connect));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            /**------------------------------Ali Ussama Update------------------------------------*/

        }
        return super.onOptionsItemSelected(item);
    }

    private void handleUploadImages() {
        try {
            presenter.handleUploadImages();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        try {
            Utilities.showConfirmationDialog(mCurrent, "Do you want to Logout?", "Yes", "No", new Utilities.CallBack() {
                @Override
                public void OnPositiveClicked(MaterialDialog dlg) {
                    dlg.dismiss();
                    prefManager.saveString(PrefManager.KEY_SURVEYOR_CODE, "");
                    prefManager.saveString(PrefManager.KEY_SURVEYOR_PASSWORD, "");

                    Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                    startActivity(intent);

                }

                @Override
                public void OnNegativeClicked(MaterialDialog dlg) {
                    dlg.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showBookmarksDialog() {
        ArrayList<BookMark> bookMarks = DataCollectionApplication.getAllBookMarks();

        if (bookMarks.size() > 0) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_show_bookmarks_title));
            dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_show_bookmarks, null, false);

            ListView listView = (ListView) dialogView.findViewById(R.id.lvBookmarks);


            builder.setView(dialogView);
            final android.app.AlertDialog alertDialog = builder.create();

            final BookMarkAdapter bookMarkAdapter = new BookMarkAdapter(this, bookMarks, alertDialog);
            listView.setAdapter(bookMarkAdapter);

            alertDialog.show();

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {

                        BookMark bookMark = (BookMark) bookMarkAdapter.getItem(position);

                        Viewpoint viewpoint = Viewpoint.fromJson(bookMark.getJson());
                        mapView.setViewpoint(viewpoint);
                        alertDialog.dismiss();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

        } else {
            Utilities.showToast(this, getString(R.string.no_bookmarks));
        }
    }

    private void showAddNewBookmarkDialog() {

        MaterialDialog mBookMarkDlg = Utilities.showAlertDialogWithCustomView(mCurrent, R.layout.book_mark_layout, getString(R.string.dialog_bookmark_cancel));

        EditText bookmarkET = mBookMarkDlg.getView().findViewById(R.id.book_mark_name_edit_text);
        Button bookmarkBtn = mBookMarkDlg.getView().findViewById(R.id.book_mark_save_btn);
        bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookmarkET.getText() == null || bookmarkET.getText().toString().isEmpty()) {
                    bookmarkET.setError(getString(R.string.required));
                } else {
                    String title = bookmarkET.getText().toString();
                    saveBookMark(title, mapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY));
                    mBookMarkDlg.dismiss();
                }
            }
        });
    }

    private void saveBookMark(String title, Viewpoint currentViewpoint) {
        try {

            DataCollectionApplication.addBookMark(currentViewpoint.toJson(), title);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncAndGoOnline() {
        try {
            syncData();
            syncAndGoOnline = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goOnline() {
        if (Utilities.isNetworkAvailable(mCurrent)) {
            initMap();
            onlineData = true;
            showViews();
        } else {
            Utilities.showInfoDialog(mCurrent, getString(R.string.network_connection_failed), getString(R.string.please_your_network_connect));
        }
    }

    private void syncData() {
        try {
            if (Utilities.isNetworkAvailable(mCurrent)) {
                presenter.syncData(currentOfflineVersionTitle);
            } else {
                Utilities.showInfoDialog(mCurrent, getString(R.string.network_connection_failed), getString(R.string.please_your_network_connect));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showOfflineMapsList(final MapActivity context, final MapView mapView) {
        //Declaring List to hold NonNull offline database titles

        HashMap<String, String> dbTitles = (HashMap<String, String>) presenter.loadDBTitles();
        if (dbTitles != null && !dbTitles.isEmpty()) {
            ArrayList<String> databaseTitlesWithoutNull = new ArrayList<>();
            for (String title : dbTitles.keySet()) {
                if (!title.endsWith("_pl")) {
                    databaseTitlesWithoutNull.add(title);
                }
            }
            displayTitlesOfflineMapDialog2(mCurrent, mapView, null, databaseTitlesWithoutNull, dbTitles);
        } else {
            Utilities.showToast(mCurrent, getString(R.string.no_offline_version));
        }
/**----------------------------------------------------------------------------------------------*/
        //OLD Version
        //Filtering NonNull database titles
       /* for (String title : databaseTitles) {
            if (title != null) {
                Log.i(TAG, "showOfflineMapsList(): title " + title + " is not null");
                databaseTitlesWithoutNull.add(title);
            } else {
                Log.i(TAG, "showOfflineMapsList(): title  is null");
            }
        }
        //Handle database's titles not null
        if (databaseTitlesWithoutNull.size() > 0) {
            Log.i(TAG, "showOfflineMapsList() : database titles without null size = " + databaseTitlesWithoutNull.size());
            //declaring array to hold database titles
            String[] titles = new String[databaseTitlesWithoutNull.size()];
            //Converting ArrayList to array
            titles = databaseTitlesWithoutNull.toArray(titles);
            //calling method to display dialog with available offline database titles
            displayTitlesOfflineMapDialog(mCurrent, mapView, titles, databaseTitles);
        } else {
            Log.i(TAG, "showOfflineMapsList() : displaying No Offline Map Dialog");

//            mCurrent.showNoOfflineMapDialog(context, mapView);
        }*/
    }

    private void displayTitlesOfflineMapDialog2(final MapActivity context, final MapView mapView, final String[] titles, final ArrayList<String> databaseTitles, HashMap<String, String> dbTitles) {
        try {
            dbDialog = Utilities.showAlertDialogWithCustomView(mCurrent, R.layout.db_titles_dialog, getString(R.string.cancel));
            dbDialog.setCancelable(true);
            View view = dbDialog.getView();
            RecyclerView rec = view.findViewById(R.id.db_titles_dialog_rec);
            adapter = new DBTitleAdapter(databaseTitles, dbTitles, this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            rec.setLayoutManager(layoutManager);
            rec.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayTitlesOfflineMapDialog(final MapActivity context, final MapView mapView, final String[] titles, final ArrayList<String> databaseTitles) {
        new AlertDialog.Builder(context)
                .setItems(titles, (dialog, which) -> {
                    try {
                        String selectedTitle = titles[which];
                        int selectedVersion = 0;
                        for (int i = 0; i < databaseTitles.size(); i++) {
                            if (selectedTitle.equals(databaseTitles.get(i))) {
                                selectedVersion = i + 1;
                                break;
                            }
                        }

                        Log.i(TAG, "Selected Version: " + selectedVersion);
                        onlineData = false;
                        currentOfflineVersion = selectedVersion; // TODO un comment
                        currentOfflineVersionTitle = selectedTitle;
//                        presenter.addLocalPLLayer(mapView, baseMap, selectedVersion, selectedTitle);
                        presenter.addLocalLayers(mapView, baseMap, selectedVersion, selectedTitle);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).setCancelable(true)
                .setPositiveButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void goOffline() {
        try {
            MaterialDialog materialDialog = Utilities.showAlertDialogWithCustomView(mCurrent, R.layout.dialog_local_db_name, getString(R.string.cancel));

            EditText databaseName = (EditText) materialDialog.findViewById(R.id.local_db_name_edit_text);
            Button dialogCloseButton = (Button) materialDialog.findViewById(R.id.local_db_download_btn);

            dialogCloseButton.setOnClickListener(v -> {

                localDatabaseTitle = databaseName.getText().toString();
                if (localDatabaseTitle.equals("")) {
                    databaseName.setError(getString(R.string.name_validation));
                } else if (!isValidDatabaseName(localDatabaseTitle)) {
                    databaseName.setError(getString(R.string.area_name_exists));
                } else if(localDatabaseTitle.toLowerCase().contains("pl")){
                    databaseName.setError(getString(R.string.pl_name_restriction));
                }else {
                    try {

                        menuItemGoOfflineMode.setVisible(false);
                        menuItemGoOnlineMode.setVisible(true);

                        materialDialog.dismiss();
                        Log.d(TAG, "Going offline ....");
                        //Async task
                        baseMap.getOperationalLayers().clear();
                        graphicsOverlay.getGraphics().clear();

                        Utilities.showLoadingDialog(mCurrent);
                        String dbUrl = mCurrent.getString(R.string.gcs_feature_server_test);
                        presenter.downloadAndSaveDatabase(DOWNLOAD_GEO_DATABASE, localDatabaseTitle, mapView.getVisibleArea().getExtent(), dbUrl);

                    } catch (Exception e) {
                        Log.d(TAG, "Error in Going offline");
                        e.printStackTrace();
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidDatabaseName(String localDatabaseTitle) {
        return presenter.isValidDatabaseName(localDatabaseTitle);
    }

    /**
     * --------------------------------------callbacks-------------------------------------------
     */

    @Override
    public void onSingleTap(Point point) {
        try {
            Log.i(TAG, "onSingleTap: sheetBehavior state = " + sheetBehavior.getState());
            if (drawMeasure) {
                if (shapeType.matches(POLYLINE)) {
                    if (pointCollection.size() < 2) {
                        pointCollection.add(point);
                        drawLine(pointCollection);
                    } else {
                        Utilities.showToast(mCurrent, "Please undo, or press Done and remeasure again");
                    }
                }

            } else if (drawShape) {
                if (surveyorCode.equals("1") || surveyorCode.equals("54722") || isValidTapArea(point, mSurveyorAreaPolygon)) {
                    startDrawShape(point);
                } else {

                    Utilities.showToast(mCurrent, getString(R.string.not_valid_area));
                }
            } else if (!queryStatus) {

                if (surveyorCode.equals("1") || surveyorCode.equals("54722") || isValidTapArea(point, mSurveyorAreaPolygon)) {
                    if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || sheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                        Utilities.showToast(mCurrent, getString(R.string.please_press_cancel_first));
                    } else {

                        graphicsOverlay.getGraphics().clear();
                        selectedResult = null;
                        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        queryStatus = true;

//                        status = Columns.EditPoint;

                        Log.i(TAG, "onSingleTap(): callback is Called ");
                        Log.i(TAG, "onSingleTap(): taped point lat = " + point.getX() + " Lang = " + point.getY());

                        presenter.prepareQueryResult();
                        Utilities.showLoadingDialog(mCurrent);
                        if (onlineData) {
                            presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_DistributionBox_ServiceTable, FCL_DistributionBoxLayer);
                        } else {
                            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_DistributionBoxTableOffline, FCL_DistributionBoxLayer);
                        }
                    }
                } else {

                    Utilities.showToast(mCurrent, getString(R.string.not_valid_area));
                }
            } else {
                Utilities.showToast(mCurrent, getString(R.string.please_wait));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isValidTapArea(Point point, ArrayList<Polygon> mSurveyorAreaPolygon) {
        try {

            if (mSurveyorAreaPolygon != null && !mSurveyorAreaPolygon.isEmpty()) {
                for (Polygon area : mSurveyorAreaPolygon) {

                    Polygon polygon = (Polygon) GeometryEngine.project(area, mapView.getSpatialReference());
                    Point selectedPoint = (Point) GeometryEngine.project(point, mapView.getSpatialReference());

                    if (GeometryEngine.intersects(selectedPoint, polygon)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onQueryOnline(ArrayList<OnlineQueryResult> results, FeatureLayer featureLayer, Point point) {
        try {
            if (featureLayer.equals(FCL_DistributionBoxLayer)) {
                presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_POLES_ServiceTable, FCL_POLES_Layer);
            } else if (featureLayer.equals(FCL_POLES_Layer)) {
                presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_RMU_ServiceTable, FCL_RMU_Layer);
            } else if (featureLayer.equals(FCL_RMU_Layer)) {
                presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_Substation_ServiceTable, FCL_Substation_Layer);
            } else if (featureLayer.equals(FCL_Substation_Layer)) {
                presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), OCL_METER_ServiceTable, OCL_METER_Layer);
            } else if (featureLayer.equals(OCL_METER_Layer)) {
                presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), Others_ServiceTable, Others_Layer);
            } else if (featureLayer.equals(Others_Layer)) {
                presenter.queryOnline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_PL_ServiceTable, FCL_PL_Layer);
            } else if (featureLayer.equals(FCL_PL_Layer)) {
                showQueryResult(results, point);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onQueryOffline(ArrayList<OnlineQueryResult> results, FeatureLayer featureLayer, Point point) {
        if (featureLayer.equals(FCL_DistributionBoxLayer)) {
            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_POLESTableOffline, FCL_POLES_Layer);
        } else if (featureLayer.equals(FCL_POLES_Layer)) {
            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_RMUTableOffline, FCL_RMU_Layer);
        } else if (featureLayer.equals(FCL_RMU_Layer)) {
            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_SubstationTableOffline, FCL_Substation_Layer);
        } else if (featureLayer.equals(FCL_Substation_Layer)) {
            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), OCL_METERTableOffline, OCL_METER_Layer);
        } else if (featureLayer.equals(OCL_METER_Layer)) {
//            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), ServicePointTableOffline, ServicePoint_Layer);
            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), OthersTableOffline, Others_Layer);
        } else if (featureLayer.equals(Others_Layer)) {
            presenter.queryOffline(presenter.getOnlineQueryResults(), point, mapView.getSpatialReference(), FCL_PL_TableOffline, FCL_PL_Layer);
        } else if (featureLayer.equals(FCL_PL_Layer)) {
            showOfflineQueryResult(results, point);
        }
    }

    @Override
    public void onSyncSuccess(boolean status) {
        if (status) {
            Utilities.showToast(mCurrent, "تم ارسال البيانات بنجاح");
            if (syncAndGoOnline) {
                goOnline();
            }
        } else {
            Utilities.showToast(mCurrent, "من فضلك اعد تحميل منطقة العمل و حاول مجددا!");
        }
    }

    @Override
    public void onShowOfflineViews(boolean status) {
        try {
            if (status) {
                item_load_previous_offline.setVisible(true);
                menuItemOffline.setVisible(false);
                menuItemSync.setVisible(true);
//                menuItemOnline.setVisible(true);
                if (presenter.isLocalGeoDatabase()) {
                    menuItemLoad.setVisible(false);
                } else {
                    menuItemLoad.setVisible(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeleteFeature(boolean status) {
        try {
            hideFragment();//TODO UNCOMMENT
//            dismissDetailsFragment();
            showViews();

            if (status) {
                Utilities.showToast(mCurrent, getString(R.string.deleted_successfully));
            } else {
                Utilities.showToast(mCurrent, getString(R.string.failed_to_deleted));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onQuerySurveyorAreaOnline(ArrayList<Polygon> polygon) {
        try {
            mSurveyorAreaPolygon = polygon;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeleteDatabase(String path, String title) {
        try {
            File file = new File(path);
            file.delete();
            adapter.updateDbList(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDatabaseTitleSelected(String dbTitle) {
        try {
            if (dbDialog.isShowing()) {
                dbDialog.dismiss();
            }
            onlineData = false;
            currentOfflineVersionTitle = dbTitle;
//            presenter.addLocalPLLayer(mapView, baseMap, 0, dbTitle);
            presenter.addLocalLayers(mapView, baseMap, 0, dbTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hideFragmentFromActivity() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        hideFragment();//TODO UNCOMMENT
//                        dismissDetailsFragment();
                        showViews();
                        Utilities.dismissLoadingDialog();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDownloadGeoDatabaseSuccess(boolean status, String folderPath, String geoDatabasePath, String plGeoDatabasePath) {
        try {
            if (status) {
                onlineData = false;
                presenter.loadMap(folderPath, geoDatabasePath, mapView, baseMap);
//                presenter.loadPLMap(folderPath, plGeoDatabasePath, mapView, baseMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOfflineQueryResult(ArrayList<OnlineQueryResult> results, Point pointOnMap) {
        try {
            queryStatus = false;
            if (results != null && !results.isEmpty()) {
                Log.i(TAG, "showOfflineQueryResult(): there is feature");
                Log.i(TAG, "showOfflineQueryResult(): results size = " + results.size());

                if (results.size() == 1) {
                    runOnUiThread(() -> {
                        try {
                            for (OnlineQueryResult result : results) {
                                handleSelectPoint(result, pointOnMap);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    handleMultiOnlineQueryResult(results, pointOnMap);
                }

//                runOnUiThread((Runnable) () -> {
//                    try {
//                        double shortestDistance = 1000000000;
//                        for (OnlineQueryResult result : results) {
//
//                            if (result.getFeatureOffline() != null) {
//                                Log.i(TAG, "showOfflineQueryResult: result.getFeatureOffline() != null");
//
//                                Point endPoint = (Point) result.getFeatureOffline().getGeometry();
//
//                                if (endPoint != null) {
//                                    Log.i(TAG, "showOnlineQueryResult(): endPoint != null");
//                                    Point edinburghGeographic = new Point(pointOnMap.getX(), pointOnMap.getY(), mapView.getSpatialReference());
//                                    Point darEsSalaamGeographic = new Point(endPoint.getX(), endPoint.getY(), mapView.getSpatialReference());
//
//                                    // Create a world equidistant cylindrical spatial reference for measuring planar distance.
////                                    SpatialReference equidistantSpatialRef = SpatialReference.create(54002);
//                                    Log.i(TAG, "showQueryResult: spatial reference wkid = " + mapView.getSpatialReference().getWkid());
//
//                                    // Project the points from geographic to the projected coordinate system.
//                                    Point startP = (Point) GeometryEngine.project(edinburghGeographic, mapView.getSpatialReference());
//                                    Point endP = (Point) GeometryEngine.project(darEsSalaamGeographic, mapView.getSpatialReference());
//
//                                    // Get the planar distance between the points in the spatial reference unit (meters).
//                                    double planarDistanceMeters = GeometryEngine.distanceBetween(startP, endP);
//                                    // Result = 7,372,671.29511302 (around 7,372.67 kilometers)
//
//                                    Log.i(TAG, "showOfflineQueryResult(): Start Point X = " + startP.getX() + " Start Point Y = " + startP.getY());
//                                    Log.i(TAG, "showOfflineQueryResult(): End Point X = " + endP.getX() + " Enf Point Y = " + endP.getY());
//                                    Log.i(TAG, "showOfflineQueryResult(): point id = " + result.getObjectID() + " distance = " + planarDistanceMeters);
//
//                                    if (planarDistanceMeters <= shortestDistance) {
//                                        shortestDistance = planarDistanceMeters;
//                                        selectedResult = result;
//                                    }
//                                } else {
//                                    Log.i(TAG, "showOfflineQueryResult(): endPoint = null");
//
//                                }
//                            } else {
//                                Log.i(TAG, "showOfflineQueryResult: result.getFeatureOffline() == null");
//
//                            }
//                        }
//                        if (selectedResult != null && selectedResult.getFeatureOffline() != null) {
//                            Log.i(TAG, "showOfflineQueryResult(): selectedResult != null && selectedResult.getFeatureOffline() != null");
//                            Log.i(TAG, "showOfflineQueryResult(): graphics old size = " + graphicsOverlay.getGraphics().size());
//
//                            boolean graphicsAdded = graphicsOverlay.getGraphics().add(new Graphic((Point) selectedResult.getFeatureOffline().getGeometry(), pictureMarkerSymbol));
//                            if (graphicsAdded) {
//                                Log.i(TAG, "showOfflineQueryResult: graphics added");
//                            } else {
//                                Log.i(TAG, "showOfflineQueryResult: graphics not added");
//                            }
//                            Log.i(TAG, "showOfflineQueryResult(): graphics new size = " + graphicsOverlay.getGraphics().size());
//
//
//                            Utilities.dismissLoadingDialog();
//                            mAddPointLayout.setVisibility(View.GONE);
//                            mEditPointLayout.setVisibility(View.VISIBLE);
//                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//
//                        } else {
//                            if (selectedResult == null) {
//                                Log.i(TAG, "showOfflineQueryResult(): selectedResult == null ");
//
//                            } else if (selectedResult.getFeatureOffline() == null) {
//                                Log.i(TAG, "showOfflineQueryResult(): selectedResult.getFeatureOffline() == null");
//                            }
//                            Log.i(TAG, "showOfflineQueryResult(): graphics size = " + graphicsOverlay.getGraphics().size());
//
//                            Utilities.dismissLoadingDialog();
//                            mEditPointLayout.setVisibility(View.GONE);
//                            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Utilities.dismissLoadingDialog();
//                    }
//                });

            } else {
                runOnUiThread(() -> {
                    Utilities.dismissLoadingDialog();
                    Toast.makeText(mCurrent, getString(R.string.zoom_more), Toast.LENGTH_SHORT).show();
                });
            }
        } catch (
                Exception e) {
            e.printStackTrace();
            Utilities.dismissLoadingDialog();
        }
    }

    private void showQueryResult(ArrayList<OnlineQueryResult> results, Point pointOnMap) {
        try {
            queryStatus = false;
            if (results != null && !results.isEmpty()) {
                Log.i(TAG, "showQueryResult(): there is feature");
                Log.i(TAG, "showQueryResult(): listQueryResults size = " + results.size());

                if (results.size() == 1) {
                    runOnUiThread(() -> {
                        try {
                            for (OnlineQueryResult result : results) {
                                handleSelectPoint(result, pointOnMap);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    handleMultiOnlineQueryResult(results, pointOnMap);
                }
//                runOnUiThread((Runnable) () -> {
//                    try {
//                        double shortestDistance = 1000000000;
//                        for (OnlineQueryResult result : results) {
//
//                            Point endPoint = (Point) result.getFeature().getGeometry();
//
//                            if (endPoint != null) {
//
//                                Point edinburghGeographic = new Point(pointOnMap.getX(), pointOnMap.getY(), mapView.getSpatialReference());
//                                Point darEsSalaamGeographic = new Point(endPoint.getX(), endPoint.getY(), mapView.getSpatialReference());
//
//                                // Create a world equidistant cylindrical spatial reference for measuring planar distance.
////                                SpatialReference equidistantSpatialRef = SpatialReference.create(54002);
//
//                                Log.i(TAG, "showQueryResult: spatial reference wkid = " + mapView.getSpatialReference().getWkid());
//
//                                // Project the points from geographic to the projected coordinate system.
//                                Point startP = (Point) GeometryEngine.project(edinburghGeographic, mapView.getSpatialReference());
//                                Point endP = (Point) GeometryEngine.project(darEsSalaamGeographic, mapView.getSpatialReference());
//
//                                // Get the planar distance between the points in the spatial reference unit (meters).
//                                double planarDistanceMeters = GeometryEngine.distanceBetween(startP, endP);
//                                // Result = 7,372,671.29511302 (around 7,372.67 kilometers)
//
//                                Log.i(TAG, "showOnlineQueryResult(): Start Point X = " + startP.getX() + " Start Point Y = " + startP.getY());
//                                Log.i(TAG, "showOnlineQueryResult(): End Point X = " + endP.getX() + " Enf Point Y = " + endP.getY());
//                                Log.i(TAG, "showOnlineQueryResult(): point id = " + result.getObjectID() + " distance = " + planarDistanceMeters);
//
//                                if (planarDistanceMeters <= shortestDistance) {
//                                    shortestDistance = planarDistanceMeters;
//                                    if (selectedResult != null) {
//                                        selectedResult.setFeature(null);
//                                        selectedResult.setFeatureLayer(null);
//                                        selectedResult.setServiceFeatureTable(null);
//                                    }
//                                    selectedResult = null;
//
//                                    selectedResult = result;
//                                }
//                            }
//                        }
//
//                        if (selectedResult != null && selectedResult.getFeature() != null) {
//                            Log.i(TAG, "showOnlineQueryResult(): selectedResult != null && selectedResult.getFeature() != null");
//                            Log.i(TAG, "showOnlineQueryResult(): graphics old size = " + graphicsOverlay.getGraphics().size() + "");
//                            Graphic graphic = new Graphic(selectedResult.getFeature().getGeometry(), pictureMarkerSymbol);
//                            graphicsOverlay.getGraphics().add(graphic);
//                            Log.i(TAG, "showOnlineQueryResult(): graphics new size = " + graphicsOverlay.getGraphics().size());
//
//
//                            Utilities.dismissLoadingDialog();
//                            mAddPointLayout.setVisibility(View.GONE);
//                            mEditPointLayout.setVisibility(View.VISIBLE);
//                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//
//                        } else {
//                            if (selectedResult == null) {
//                                Log.i(TAG, "showOnlineQueryResult(): selectedResult == null ");
//
//                            } else if (selectedResult.getFeature() == null) {
//                                Log.i(TAG, "showOnlineQueryResult(): selectedResult.getFeature() == null");
//                            }
//                            Log.i(TAG, "showOnlineQueryResult(): graphics size = " + graphicsOverlay.getGraphics().size());
//
//                            Utilities.dismissLoadingDialog();
//                            mAddPointLayout.setVisibility(View.GONE);
//                            mEditPointLayout.setVisibility(View.GONE);
//                            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });

            } else {
                runOnUiThread(() -> {
                    Utilities.dismissLoadingDialog();
                    Toast.makeText(mCurrent, getString(R.string.zoom_more), Toast.LENGTH_SHORT).show();
                });
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSelectPoint(OnlineQueryResult result, Point pointOnMap) {
        Log.i(TAG, "handleSelectPoint: is called");
        Point endPoint;
        if (onlineData) {
            endPoint = (Point) result.getFeature().getGeometry();
        } else {
            endPoint = (Point) result.getFeatureOffline().getGeometry();
        }
        double shortestDistance = 1000000000;

        if (endPoint != null) {
            Point edinburghGeographic = new Point(pointOnMap.getX(), pointOnMap.getY(), result.getFeatureLayer().getSpatialReference());
            Point darEsSalaamGeographic = new Point(endPoint.getX(), endPoint.getY(), result.getFeatureLayer().getSpatialReference());

            // Create a world equidistant cylindrical spatial reference for measuring planar distance.

            // Project the points from geographic to the projected coordinate system.
            Point startP = (Point) GeometryEngine.project(edinburghGeographic, result.getFeatureLayer().getSpatialReference());
            Point endP = (Point) GeometryEngine.project(darEsSalaamGeographic, result.getFeatureLayer().getSpatialReference());

            // Get the planar distance between the points in the spatial reference unit (meters).
            double planarDistanceMeters = GeometryEngine.distanceBetween(startP, endP);
            // Result = 7,372,671.29511302 (around 7,372.67 kilometers)

            Log.i(TAG, "showOnlineQueryResult(): Start Point X = " + startP.getX() + " Start Point Y = " + startP.getY());
            Log.i(TAG, "showOnlineQueryResult(): End Point X = " + endP.getX() + " Enf Point Y = " + endP.getY());
            Log.i(TAG, "showOnlineQueryResult(): point id = " + result.getObjectID() + " distance = " + planarDistanceMeters);

            if (planarDistanceMeters <= shortestDistance) {
                shortestDistance = planarDistanceMeters;
                selectedResult = result;
            }
        }
        if (selectedResult != null) {
            if (onlineData) {
                graphicsOverlay.getGraphics().add(new Graphic(selectedResult.getFeature().getGeometry(), pictureMarkerSymbol));
                selectedResult.getFeature().loadAsync();
                selectedResult.getFeature().addDoneLoadingListener(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.i(TAG, "showQueryResult(): feature is loaded");
                            Map<String, Object> attr = selectedResult.getFeature().getAttributes();
                            for (String key : attr.keySet()) {
                                try {
                                    Log.i(TAG, "showQueryResult(): " + key + " = " + attr.get(key));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                graphicsOverlay.getGraphics().add(new Graphic(selectedResult.getFeatureOffline().getGeometry(), pictureMarkerSymbol));
            }


            Utilities.dismissLoadingDialog();
            mAddPointLayout.setVisibility(View.GONE);
            mEditPointLayout.setVisibility(View.VISIBLE);
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        } else {
            Utilities.dismissLoadingDialog();
        }
    }

    private void handleMultiOnlineQueryResult(ArrayList<OnlineQueryResult> results, Point pointOnMap) {
        try {
            Log.i(TAG, "handleMultiOnlineQueryResult: results size = " + results.size());
            hide(mAddPointLayout);
            hide(mEditPointLayout);
            show(mMultiResultContainer);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

            mMultiResultCloseIV.setOnClickListener(v -> {
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                if (selectedResult != null) {
                    selectedResult.getFeatureLayer().clearSelection();
                }
                hide(mMultiResultContainer);

            });

            Utilities.dismissLoadingDialog();
            showMultiResultRecyclerView(results);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMultiResultRecyclerView(ArrayList<OnlineQueryResult> results) {
        try {
            Log.i(TAG, "showMultiResultRecyclerView: is called");
            mMultiResultRecAdapter = new MultiResultRecAdapter(results, mCurrent, onlineData, this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mCurrent);
            mMultiResultRecyclerView.setNestedScrollingEnabled(true);
            mMultiResultRecyclerView.setLayoutManager(layoutManager);
            mMultiResultRecyclerView.setAdapter(mMultiResultRecAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideFragment() {
        try {
            ActionBar actionBar = mCurrent.getSupportActionBar();
            frag_drawShape = false;
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setTitle(getString(R.string.app_name));
            }
            fragmentManager.beginTransaction().remove(Objects.requireNonNull(fragmentManager.findFragmentById(R.id.rlFragment))).commit();
            rlFragment.setVisibility(View.INVISIBLE);
            isFragmentShown = false;
            status = Columns.EditPoint;
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            showMapView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissDetailsFragment() {
        try {
            ActionBar actionBar = mCurrent.getSupportActionBar();
            frag_drawShape = false;
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setDisplayShowHomeEnabled(false);
                actionBar.setTitle(getString(R.string.app_name));
            }
            isFragmentShown = false;
            status = Columns.EditPoint;
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            if (detailsFragment != null && detailsFragment.isVisible()) {
                detailsFragment.dismiss();
            }
            detailsFragment.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEditFragment(OnlineQueryResult selectedResult) {
        try {
            Log.i(TAG, "showEditingFragment(): is called");
            exitFullScreenMode();
            Log.i(TAG, "showEditingFragment: sheetBehavior state = " + sheetBehavior.getState());
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            Log.i(TAG, "showEditingFragment: sheetBehavior state = " + sheetBehavior.getState());

//            showBottomDialogFragment(selectedResult);
            rlFragment.setVisibility(View.VISIBLE);
            editFeatureFragment = EditFeatureFragment.newInstance(mCurrent, presenter, selectedResult, onlineData, frag_drawShape, status, threadSingleton);
            getSupportFragmentManager().popBackStack();

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .add(R.id.rlFragment, editFeatureFragment, "EditFragment")
                    .addToBackStack("EditFragment")
                    .commit();
//
            isFragmentShown = true;

            hideActivityViews();

            hideMapView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideMapView() {
        if (mapView != null) {
            mapView.pause();
        }
    }

    private void showMapView() {
        if (mapView != null) {
            mapView.resume();
        }
    }

    private void showBottomDialogFragment(OnlineQueryResult selectedResult) {
        try {
            detailsFragment = DetailsFragment.newInstance(mCurrent, presenter, selectedResult, onlineData, frag_drawShape, status, threadSingleton);//NEW
            detailsFragment.show(getSupportFragmentManager(), detailsFragment.getTag());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideActivityViews() {
        try {

            mCompass.setVisibility(View.GONE);
            fabFullScreen.setVisibility(View.GONE);
            mFabMeasureMenu.setVisibility(View.GONE);
            fabGeneral.setVisibility(View.GONE);
            fabLocation.setVisibility(View.GONE);
            mapLegend.setVisibility(View.GONE);
            tvLatLong.setVisibility(View.GONE);

            item_load_previous_offline.setVisible(false);
            menuItemOffline.setVisible(false);
            menuItemSync.setVisible(false);
//            menuItemOnline.setVisible(false);
            menuItemGoOfflineMode.setVisible(false);
            menuItemGoOnlineMode.setVisible(false);
            menuItemOverflow.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showViews() {
        try {
            Log.i(TAG, "");

            mCompass.setVisibility(View.VISIBLE);
            mFabMeasureMenu.setVisibility(View.VISIBLE);
            fabFullScreen.setVisibility(View.VISIBLE);
            fabGeneral.setVisibility(View.VISIBLE);
            fabLocation.setVisibility(View.VISIBLE);
            tvLatLong.setVisibility(View.VISIBLE);
            mapLegend.setVisibility(View.VISIBLE);

            if (onlineData) {
                Log.i(TAG, "showViews(): working online");
                item_load_previous_offline.setVisible(true);
                menuItemGoOfflineMode.setVisible(true);
                menuItemGoOnlineMode.setVisible(false);
                menuItemSync.setVisible(false);
//                menuItemOnline.setVisible(false);
                menuItemOffline.setVisible(true);
            } else {
                Log.i(TAG, "showViews(): working offline");


                item_load_previous_offline.setVisible(true);
                menuItemOffline.setVisible(false);
                menuItemSync.setVisible(true);
//                menuItemOnline.setVisible(true);
                menuItemGoOfflineMode.setVisible(false);
                menuItemGoOnlineMode.setVisible(true);

            }

//            if (!onlineData) {
//                item_load_previous_offline.setVisible(true);
//                menuItemOffline.setVisible(false);
//                menuItemSync.setVisible(true);
//                menuItemOnline.setVisible(true);
//                menuItemGoOfflineMode.setVisible(false);
//                menuItemGoOnlineMode.setVisible(true);
//            } else {
//                item_load_previous_offline.setVisible(true);
//                menuItemGoOfflineMode.setVisible(true);
//                menuItemGoOnlineMode.setVisible(false);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ---------------------------------------Draw------------------------------------------------
     */

    private void handleFabMeasureAction() {
        try {
            startDrawMode();
            startSupportActionMode(new androidx.appcompat.view.ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                    MenuInflater inflater = getMenuInflater();
                    inflater.inflate(R.menu.menu_action_add_shape, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
                    try {
                        switch (item.getItemId()) {
                            case R.id.item_Done:
                                Done();
                                mode.finish();
                                return true;
                            case R.id.item_undo:
                                undo();
                                return true;
                            case R.id.item_redo:
                                redo();
                                return true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
                    Done();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void redo() {
        try {
            if (pointCollection != null && lastPointStep != null) {
                if (pointCollection.size() < 2) {
                    pointCollection.add(lastPointStep);
                    drawLine(pointCollection);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void undo() {
        try {
            Log.i(TAG, "undo(): is called");
            if (pointCollection.size() > 0) {
                Log.i(TAG, "undo(): pointCollection size = " + pointCollection.size());
                int lastIndex = pointCollection.size() - 1;
                lastPointStep = pointCollection.get(lastIndex);
                pointCollection.remove(lastIndex);
                if (pointCollection.size() >= 0) {
                    Log.i(TAG, "undo(): pointCollection size = " + pointCollection.size());
                    if (shapeType.matches(POLYLINE)) {
                        Log.i(TAG, "undo(): shapeType = POLYLINE");

                        drawLine(pointCollection);

                    } else if (shapeType.matches(POLYGON)) {

                    }
                    //                    drawShape();
                } else
                    graphicsOverlay.getGraphics().clear();
            } else {
                Toast.makeText(mCurrent, "No Steps", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Done() {
        try {
            endDrawMode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawLine(PointCollection points) {
        try {
            Log.i(TAG, "drawLine(): is called");
            drawGraphicLayer.getGraphics().clear();

            // create a new point collection for polyline
            Polyline polyline = new Polyline(points);

            //define a line symbol
            SimpleLineSymbol lineSymbol =
                    new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.argb(255, 255, 40, 0), 4.0f);
            Graphic line = new Graphic(polyline, lineSymbol);
            drawGraphicLayer.getGraphics().add(line);

            for (Point point : pointCollection) {
                drawGraphicLayer.getGraphics().add(new Graphic((Point) point, pictureMarkerSymbol));
            }

            if (pointCollection.size() == 2) {
                show(mMeasureLayerInfo);
                show(mMeasureInMeterLbl);
                show(mMeasureValueInMeterLbl);
                show(mMeasureInKMLbl);
                show(mMeasureValueInKMLbl);
                calculateDistanceBetweenTwoPoints(pointCollection.get(0), pointCollection.get(1), mapView.getSpatialReference());
            } else {
                hide(mMeasureLayerInfo);
                hide(mMeasureInMeterLbl);
                hide(mMeasureValueInMeterLbl);
                hide(mMeasureInKMLbl);
                hide(mMeasureValueInKMLbl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Polygon createPolygon() {
        //[DocRef: Name=Create Polygon, Category=Fundamentals, Topic=Geometries]
        // create a Polygon from a PointCollection
        PointCollection coloradoCorners = new PointCollection(SpatialReferences.getWgs84());

        coloradoCorners.add(-109.048, 40.998);
        coloradoCorners.add(-102.047, 40.998);
        coloradoCorners.add(-102.037, 36.989);
        coloradoCorners.add(-109.048, 36.998);

        Polygon polygon = new Polygon(coloradoCorners);

        //[DocRef: END]
        Polygon projectedPolygon = (Polygon) GeometryEngine.project(polygon, mapView.getSpatialReference());

        double area = GeometryEngine.area(projectedPolygon);

        return polygon;
    }

    private void startDrawMode() {
        try {
            drawShape = true;

            drawGraphicLayer = new GraphicsOverlay();
            mapView.getGraphicsOverlays().add(drawGraphicLayer);

            hide(mFabMeasureMenu);
            hide(fabLocation);
            hide(fabGeneral);
            hide(fabFullScreen);
            hide(mCompass);
            hide(mapLegend);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void endDrawMode() {
        try {
            drawGraphicLayer.getGraphics().clear();
            pointCollection.clear();
            drawMeasure = false;

            hide(mMeasureLayerInfo);
            show(mFabMeasureMenu);
            show(fabLocation);
            show(fabGeneral);
            show(fabFullScreen);
            show(mCompass);
            show(mapLegend);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hide(View v) {
        try {
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void show(View v) {
        try {
            v.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateDistanceBetweenTwoPoints(Point edinburghGeographic, Point darEsSalaamGeographic, SpatialReference equidistantSpatialRef) {
        try {

            // Project the points from geographic to the projected coordinate system.
            Point edinburghProjected = (Point) GeometryEngine.project(edinburghGeographic, equidistantSpatialRef);
            Point darEsSalaamProjected = (Point) GeometryEngine.project(darEsSalaamGeographic, equidistantSpatialRef);

            // Get the planar distance between the points in the spatial reference unit (meters).
            double planarDistanceMeters = GeometryEngine.distanceBetween(edinburghProjected, darEsSalaamProjected);

            // Result = 7,372,671.29511302 (around 7,372.67 kilometers)

            Log.i(TAG, "calculateDistanceBetweenTwoPoints(): distance in Meter = " + planarDistanceMeters);
            Log.i(TAG, "calculateDistanceBetweenTwoPoints(): distance in KM = " + (planarDistanceMeters / 1000));

            mMeasureValueInMeterLbl.setText(String.valueOf(Utilities.round(planarDistanceMeters, 2)));

            mMeasureValueInKMLbl.setText(String.valueOf(Utilities.round((planarDistanceMeters / 1000), 2)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(OnlineQueryResult onlineQueryResult, int position) {
        try {
            if (selectedResult != null) {
                selectedResult.getFeatureLayer().clearSelection();
            }
            selectedResult = onlineQueryResult;
            if (onlineData) {
                selectedResult.getFeatureLayer().selectFeature(selectedResult.getFeature());
            } else {
                selectedResult.getFeatureLayer().selectFeature(selectedResult.getFeatureOffline());
            }

            mMultiResultRecAdapter.setSelectedResult(onlineQueryResult, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEditItemSelected(OnlineQueryResult onlineQueryResult) {
        try {

            selectedResult = onlineQueryResult;
            hide(mMultiResultContainer);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            selectedResult.getFeatureLayer().clearSelection();
            showEditFragment(onlineQueryResult);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
