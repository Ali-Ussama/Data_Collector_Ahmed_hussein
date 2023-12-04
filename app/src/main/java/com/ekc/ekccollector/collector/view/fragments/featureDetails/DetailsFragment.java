package com.ekc.ekccollector.collector.view.fragments.featureDetails;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ekc.ekccollector.R;
import com.ekc.ekccollector.collector.model.models.Columns;
import com.ekc.ekccollector.collector.model.models.OnlineQueryResult;
import com.ekc.ekccollector.collector.model.singleton.ThreadSingleton;
import com.ekc.ekccollector.collector.view.activities.map.MapActivity;
import com.ekc.ekccollector.collector.view.activities.map.MapPresenter;
import com.ekc.ekccollector.collector.view.adapter.AttachmentRVAdapter;
import com.ekc.ekccollector.collector.view.fragments.EditFeatureFragment;
import com.ekc.ekccollector.collector.view.fragments.EditFragListener;
import com.ekc.ekccollector.collector.view.fragments.EditFragPresenter;
import com.ekc.ekccollector.collector.view.utils.Utilities;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsFragment extends BottomSheetDialogFragment implements View.OnClickListener, EditFragListener, AdapterView.OnItemSelectedListener {

    private final String TAG = "DetailsFragment";
    @BindView(R.id.offline_attachments_recycler_view)
    RecyclerView mOfflineAttachmentsRV;

    private List<File> adapterData;
    private AttachmentRVAdapter mOfflineAttachmentRVAdapter;

    @BindView(R.id.object_id)
    TextView mObjectIDTV;

    @BindView(R.id.type_spinner)
    Spinner typesSpinner;

    @BindView(R.id.comment_spinner)
    Spinner commentsSpinner;

    @BindView(R.id.mCodeEt)
    TextInputEditText mCodeET;

    @BindView(R.id.device_num)
    TextInputEditText mDeviceNoET;

    @BindView(R.id.take_picture_fab)
    FloatingActionButton mTakePictureFAB;

    private static String TEMP_PHOTO_FILE_NAME;
    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_TAKE_PICTURE = 2;
    private static final int WRITE_EXTERNAL_STORAGE = 3;
    private static final int READ_EXTERNAL_STORAGE = 4;
    private static final int REQUEST_CODE_VIDEO = 5;
    private static final int REQUEST_CODE_AUDIO = 6;
    private final String IMAGE_FOLDER_NAME = "AJC_Collector";

    private static final String JPG = "jpg";
    private static final String MP4 = "mp4";

    private static MapActivity mCurrent;
    private static MapPresenter mPresenter;
    private EditFragPresenter presenter;

    private static OnlineQueryResult mSelectedResult;

    private Feature selectedFeature;
    private FeatureLayer selectedLayer;
    private FeatureTable selectedTable;
    private GeodatabaseFeatureTable selectedOfflineFeatureTable;
    private String objectID;
    private static boolean mOnlineData;

    private Map<String, String> types = null, comments = null;
    private ArrayList<String> typesList = null, commentsList = null;
    private List<CodedValue> codedValues, commentCodedValue;
    private CodedValueDomain typeDomain, commentsDomain;
    private HashMap<String, String> codeValue;
    private ArrayList<String> codeList, CommentsCodeList;
    private File mFileTemp;
    private int imageIndex;
    private static boolean mDraw_shape;
    public static int mStatus; //1: default, 2: edit: 3: created

    private String pointFolderName = "";
    private String deviceNo;
    private String code;
    private File compressedImageFile;
    private static ThreadSingleton mThreadSingleton;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(MapActivity current, MapPresenter presenter, OnlineQueryResult selectedResult,
                                              boolean onlineData, boolean draw_shape, int status, ThreadSingleton threadSingleton) {
        mCurrent = current;
        mPresenter = presenter;
        mSelectedResult = selectedResult;
        mOnlineData = onlineData;
        mDraw_shape = draw_shape;
        mStatus = status;
        mThreadSingleton = threadSingleton;
        return new DetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        try {
            // Inflate the layout for this fragment
            view = inflater.inflate(R.layout.fragment_edit_feature, container, false);

            if (view == null) {
                Log.i(TAG, "onCreateView: view == null");
            } else {
                Log.i(TAG, "onCreateView: view != null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            setHasOptionsMenu(true);

            init(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        try {
            super.onCreateOptionsMenu(menu, inflater);
            Log.i(TAG, "onCreateOptionsMenu(): is called");
            inflater.inflate(R.menu.menu_fragment_edit, menu);

            ActionBar actionBar = mCurrent.getSupportActionBar();

            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                if (mSelectedResult.getFeatureLayer() != null && mSelectedResult.getFeatureLayer().getName() != null && !mSelectedResult.getFeatureLayer().getName().isEmpty()) {
                    actionBar.setTitle("Update " + mSelectedResult.getFeatureLayer().getName());
                }
            }

            mCurrent.menuItemOverflow.setVisible(false);

            mCurrent.item_load_previous_offline.setVisible(false);
            mCurrent.menuItemOffline.setVisible(false);
            mCurrent.menuItemSync.setVisible(false);
//            mCurrent.menuItemOnline.setVisible(false);
            mCurrent.menuItemGoOfflineMode.setVisible(false);
            mCurrent.menuItemGoOnlineMode.setVisible(false);
            mCurrent.menuItemOverflow.setVisible(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {

            Log.i(TAG, "onOptionsItemSelected(): is called");
            switch (item.getItemId()) {
                case android.R.id.home: {
                    mCurrent.onBackPressed();
                    return true;
                }
                case R.id.menu_save:
                    Log.i(TAG, "onOptionsItemSelected(): menu_save has been hit");
                    saveChanges();
                    break;
                case R.id.menu_delete:
                    onDelete();
                    break;
                case R.id.menu_camera:

//                    if (ActivityCompat.checkSelfPermission(mCurrent, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                        Log.i(TAG, "onOptionsItemSelected: READ_EXTERNAL_STORAGE is granted");
//                        this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
//                    } else {
//                        Log.i(TAG, "onOptionsItemSelected: READ_EXTERNAL_STORAGE is granted");
                    takePicture();
//                    }
                    break;

                case R.id.menu_audio:
//                    if (ActivityCompat.checkSelfPermission(mCurrent, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                        this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_AUDIO);
//                    } else {
////                        showAudioDialog();
//                    }
                    break;

                case R.id.menu_gallery:
//                    if (ActivityCompat.checkSelfPermission(mCurrent, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                        Log.i(TAG, "onOptionsItemSelected: requesting READ_EXTERNAL_STORAGE");
//                        this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
//                    } else {
                    Log.i(TAG, "onOptionsItemSelected: READ_EXTERNAL_STORAGE is granted");
                    openGallery();
//                    }
                    break;
                case R.id.menu_video:
//                    if (ActivityCompat.checkSelfPermission(mCurrent, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                        this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
//                    } else {
//                        Log.i(TAG, "record video");
////                        recordVideo();
//                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onDelete() {
        try {
            if (mOnlineData) {
                mPresenter.deleteFeatureOnline(mSelectedResult);
            } else {
                mPresenter.deleteFeatureOffline(mSelectedResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveChanges() {
        try {
            Log.i(TAG, "saveChanges(): is called");

            if (mCodeET.getText() != null && mCodeET.getText().toString().isEmpty()) {
                mCodeET.setError(mCurrent.getString(R.string.required));
            } else if (mDeviceNoET.getText() != null && (mDeviceNoET.getText().toString().isEmpty() || mDeviceNoET.getText().toString().contains("null"))) {
                mDeviceNoET.setError(mCurrent.getString(R.string.required));
            } else if (mDeviceNoET.getText() != null && mDeviceNoET.getText().toString().contains(".")) {
                mDeviceNoET.setError(mCurrent.getString(R.string.characters_only));
            } else {
                Utilities.showLoadingDialog(mCurrent);
                Utilities.hideKeyBoard(mCurrent);

                String code = mCodeET.getText().toString();
                String deviceNo = mDeviceNoET.getText().toString();
                String typeCode = codeList.get(typesSpinner.getSelectedItemPosition());
                String surveyorName = presenter.getSurveyorName();
                int commentCode = Integer.parseInt(CommentsCodeList.get((commentsSpinner.getSelectedItemPosition())));
                if (mOnlineData) {
                    Log.i(TAG, "saveChanges(): calling update feature online");
                    mPresenter.updateFeatureOnline(mSelectedResult, code, deviceNo, typeCode, surveyorName, commentCode, mStatus);
                } else {
                    Log.i(TAG, "saveChanges(): calling update feature offline");
                    mPresenter.updateFeatureOffline(mSelectedResult, code, deviceNo, typeCode, surveyorName, commentCode, mStatus);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(View v) {
        try {

//            requestPermission();
            ButterKnife.bind(this, v);
            presenter = new EditFragPresenter(mCurrent, this, mThreadSingleton);
            if (mOnlineData) {
                selectedFeature = mSelectedResult.getFeature();
                selectedLayer = mSelectedResult.getFeatureLayer();
                selectedTable = selectedLayer.getFeatureTable();
                objectID = mSelectedResult.getObjectID();
            } else {
                selectedFeature = mSelectedResult.getFeatureOffline();
                selectedLayer = mSelectedResult.getFeatureLayer();
                selectedOfflineFeatureTable = mSelectedResult.getGeodatabaseFeatureTable();
                objectID = mSelectedResult.getObjectID();
            }

            setViewsWithData();
            pointFolderName = selectedLayer.getName();

            initTypeSpinner();

            loadCommentsSpinner();

            try {
                adapterData = new ArrayList<>();
                loadImages();
                GridLayoutManager mGridLayoutManager = new GridLayoutManager(mCurrent, 2);
                mOfflineAttachmentRVAdapter = new AttachmentRVAdapter(adapterData, mCurrent, this);
                mOfflineAttachmentsRV.setLayoutManager(mGridLayoutManager);
                mOfflineAttachmentsRV.setAdapter(mOfflineAttachmentRVAdapter);
                mOfflineAttachmentsRV.setNestedScrollingEnabled(false);

                mTakePictureFAB.setOnClickListener(this);

                Log.i(TAG, "init: adapter is attached");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i(TAG, "init: mStatus = " + mStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCommentsSpinner() {
        try {
            FeatureTable mSelectedTable = mSelectedResult.getFeatureLayer().getFeatureTable();
            commentsDomain = (CodedValueDomain) mSelectedTable.getField(Columns.Comments).getDomain();
            commentCodedValue = commentsDomain.getCodedValues();

            commentsList = new ArrayList<>();
            CommentsCodeList = new ArrayList<>();

            for (CodedValue codedValue : commentCodedValue) {
                commentsList.add(codedValue.getName());
                CommentsCodeList.add(codedValue.getCode().toString());
            }
            ArrayAdapter adapter = new ArrayAdapter<String>(mCurrent, android.R.layout.simple_dropdown_item_1line, commentsList);
            commentsSpinner.setAdapter(adapter);

            setSelectedDomainItem(Columns.Comments, CommentsCodeList, commentsSpinner);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadImages() {
        try {
            String pointFolderName = "";
            if (mCurrent.onlineData) {
                pointFolderName = (selectedLayer.getName());/*.split("\\.")[2]);*/
                Log.i(TAG, "loadImages(): layer name = " + pointFolderName);
            } else {
                pointFolderName = (selectedLayer.getName());
                Log.i(TAG, "loadImages(): layer name = " + pointFolderName);
            }


            Date d = new Date();
            String date = new SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH).format(d);

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + "AJC_Collector_COMPRESSED_Images" + File.separator;
//            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + IMAGE_FOLDER_NAME + File.separator;
            Log.i(TAG, "loadImages(): path = " + path);
            File folder = new File(path);
            if (folder.exists()) {
//                File[] allFiles = folder.listFiles(new FilenameFilter() {
//                    public boolean accept(File dir, String name) {
//                        return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"));
//                    }
//                });
//                Log.i(TAG, "loadImages(): images count = " + allFiles.length);
                if (/*file.getPath().contains("_" + mSelectedResult.getObjectID().concat(".jpg")) || file.getPath().contains("_" + mSelectedResult.getObjectID().concat(".png"))
                            || */(mDeviceNoET.getText() != null && mDeviceNoET.getText().toString() != null &&
                        !mDeviceNoET.getText().toString().toString().isEmpty() &&
                        !mDeviceNoET.getText().toString().equals("null"))) {
                    /*for (File file : allFiles) {
                        if (file.getPath().contains("_(") && file.getPath().contains(")_")) {
                            String[] split_1 = file.getPath().split("(\\)_)");
                            String[] split_2 = split_1[1].split("\\.");

                            if (split_2[0].equals(mDeviceNoET.getText().toString())) {
                                Log.i(TAG, "loadImages(): image path = " + file.getPath() + " contains DeviceNo = " + (mDeviceNoET.getText().toString()));
                                adapterData.add(file);

                            } else {
                                Log.i(TAG, "loadImages(): image path = " + file.getPath() + " doesn't contains DeviceNo = " + mDeviceNoET.getText().toString());
                            }
                        }
                    }*/

                    presenter.loadImages(mDeviceNoET.getText().toString(), adapterData);

                }

                for (File file : adapterData) {
                    Log.i(TAG, "loadImages(): image path = " + file.getPath());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File[] getFolders(File path) {
        return path.listFiles();
    }

    private void requestPermission() {
        try {
            if (ActivityCompat.checkSelfPermission(mCurrent, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mCurrent, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
            }

            if (ActivityCompat.checkSelfPermission(mCurrent, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mCurrent, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_TAKE_PICTURE);
            }

            if (ActivityCompat.checkSelfPermission(mCurrent, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mCurrent, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setViewsWithData() {
        try {

            if (mDraw_shape) {
                mDeviceNoET.setEnabled(true);
            } else {
                mDeviceNoET.setEnabled(false);
            }
            mCodeET.setEnabled(false);

            if (mCurrent.drawShape) {
                mObjectIDTV.setText("");
                mDeviceNoET.setText("");
                mCodeET.setText("");
            } else {
                if (mOnlineData) {
                    ArcGISFeature feature = mSelectedResult.getFeature();
                    feature.loadAsync();
                    feature.addDoneLoadingListener(() -> mCurrent.runOnUiThread(() -> {
                        int status = 0;

                        try {
                            status = (Integer) selectedFeature.getAttributes().get(Columns.Status);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Log.i(TAG, "setViewsWithData: status = " + status);

                        if (status == 0) {
                            status = 1;
                        }

                        try {
                            CodedValueDomain codedValueDomain = (CodedValueDomain) mSelectedResult.getFeatureLayer().getFeatureTable().getField(Columns.Status).getDomain();
                            for (CodedValue codedValue : codedValueDomain.getCodedValues()) {
                                Log.i(TAG, "setViewsWithData: coded = " + codedValue.getCode());
                                if ((int) codedValue.getCode() == status) {
                                    mObjectIDTV.setText(codedValue.getName());
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                        mObjectIDTV.setText(String.valueOf(feature.getAttributes().get(Columns.ObjectID)));
                        if (feature.getAttributes().get(Columns.Device_No) == null || String.valueOf(feature.getAttributes().get(Columns.Device_No)).equals("null") || String.valueOf(feature.getAttributes().get(Columns.Device_No)).isEmpty()) {
                            mDeviceNoET.setEnabled(true);
                        } else {
                            mDeviceNoET.setEnabled(false);
                        }
                        mDeviceNoET.setText(String.valueOf(feature.getAttributes().get(Columns.Device_No)));
                        mCodeET.setText(String.valueOf(feature.getAttributes().get(Columns.Code)));

                    }));
                } else {
                    Feature feature = mSelectedResult.getFeatureOffline();
                    int status = 0;

                    try {
                        status = (Integer) selectedFeature.getAttributes().get(Columns.Status);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (status == 0) {
                        status = 1;
                    }

                    try {
                        CodedValueDomain codedValueDomain = (CodedValueDomain) mSelectedResult.getFeatureLayer().getFeatureTable().getField(Columns.Status).getDomain();
                        for (CodedValue codedValue : codedValueDomain.getCodedValues()) {
                            if ((int) codedValue.getCode() == status) {
                                mObjectIDTV.setText(codedValue.getName());
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    mObjectIDTV.setText(String.valueOf(feature.getAttributes().get(Columns.ObjectID)));
                    if (feature.getAttributes().get(Columns.Device_No) == null || String.valueOf(feature.getAttributes().get(Columns.Device_No)).equals("null") || String.valueOf(feature.getAttributes().get(Columns.Device_No)).isEmpty()) {
                        mDeviceNoET.setEnabled(true);
                    } else {
                        mDeviceNoET.setEnabled(false);
                    }
                    mDeviceNoET.setText(String.valueOf(feature.getAttributes().get(Columns.Device_No)));
                    mCodeET.setText(String.valueOf(feature.getAttributes().get(Columns.Code)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTypeSpinner() {
//        if (!mOnlineData) {
//            loadSpinnerOnAddFeatureOffline();
//        } else {
        loadSpinnerOnEditFeature();
//        }

    }

    private void loadSpinnerOnAddFeatureOffline() {
        try {
            ServiceFeatureTable mSelectedTable = mSelectedResult.getServiceFeatureTable();

            mSelectedTable.loadAsync();
            mSelectedTable.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    mCurrent.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                typeDomain = (CodedValueDomain) mSelectedTable.getField(Columns.Type).getDomain();
                                codedValues = typeDomain.getCodedValues();

                                typesList = new ArrayList<>();
                                codeList = new ArrayList<>();

                                for (CodedValue codedValue : codedValues) {
                                    typesList.add(codedValue.getName());
                                    codeList.add(codedValue.getCode().toString());
                                }
                                ArrayAdapter adapter = new ArrayAdapter<String>(mCurrent, android.R.layout.simple_dropdown_item_1line, typesList);
                                typesSpinner.setAdapter(adapter);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSpinnerOnEditFeature() {
        try {
            FeatureTable mSelectedTable = mSelectedResult.getFeatureLayer().getFeatureTable();
            typeDomain = (CodedValueDomain) mSelectedTable.getField(Columns.Type).getDomain();
            codedValues = typeDomain.getCodedValues();

            typesList = new ArrayList<>();
            codeList = new ArrayList<>();

            for (CodedValue codedValue : codedValues) {
                typesList.add(codedValue.getName());
                codeList.add(codedValue.getCode().toString());
            }
            ArrayAdapter adapter = new ArrayAdapter<String>(mCurrent, android.R.layout.simple_dropdown_item_1line, typesList);
            typesSpinner.setAdapter(adapter);

            typesSpinner.setOnItemSelectedListener(this);
            setSelectedDomainItem(Columns.Type, codeList, typesSpinner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSelectedDomainItem(String columnName, ArrayList<String> codeList, Spinner spinner) {
        try {
            Log.i(TAG, "setSelectedDomainItem(): is called");
            String type = String.valueOf(selectedFeature.getAttributes().get(columnName));
            Log.i(TAG, "setSelectedDomainItem(): type = " + type);

            for (int i = 0; i < codeList.size(); i++) {
                if (type != null) {
                    Log.i(TAG, "setSelectedDomainItem: codeList[i] = " + codeList.get(i));
                    if (codeList.get(i).equals(type)) {
                        spinner.setSelection(i, true);
                        Log.i(TAG, "setSelectedDomainItem: index = " + i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void takePicture() {
        try {

            if (ActivityCompat.checkSelfPermission(mCurrent, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mCurrent, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);

            } else if (ActivityCompat.checkSelfPermission(mCurrent, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mCurrent, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            } else {

                String pointName;
                try {
                    pointName = String.valueOf(objectID);
                } catch (Exception e) {
                    e.printStackTrace();
                    pointName = "1"; // TODO Remove
                }

                String OriginalObjectId = "";

                try {
                    OriginalObjectId = String.valueOf(selectedFeature.getAttributes().get(Columns.ID));
                } catch (Exception e) {
                    e.printStackTrace();
                    OriginalObjectId = "NULL";
                }


                String deviceNo = "";
                if (mCurrent.onlineData)
                    pointFolderName = (selectedLayer.getName());/*.split("\\.")[2]);*/
                else {
                    Log.i(TAG, "layer name = " + selectedLayer.getName());
                    pointFolderName = (selectedLayer.getName());
                }

                if (mDeviceNoET.getText().toString().contains(".")) {
                    mDeviceNoET.setError(mCurrent.getString(R.string.characters_only));
                    return;
                } else if (mDeviceNoET.getText() != null && !mDeviceNoET.getText().toString().isEmpty()
                        && !mDeviceNoET.getText().toString().toLowerCase().equals("null") && !mDeviceNoET.getText().toString().equals("0")) {
                    deviceNo = mDeviceNoET.getText().toString();
                    this.deviceNo = deviceNo;
                } else {
                    mDeviceNoET.setError(mCurrent.getString(R.string.required));
                    Utilities.showToast(mCurrent, "من فضلك ادخل رقم المساح اولأا");
                    return;
                }

                createCompressedTempFile(OriginalObjectId, pointName, pointFolderName, JPG, "IMG", deviceNo);
//                createFile(OriginalObjectId, pointName, pointFolderName, JPG, "IMG", deviceNo);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                Uri photoURI = FileProvider.getUriForFile(mCurrent, getString(R.string.app_package_name), mFileTemp);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    List<ResolveInfo> resInfoList = mCurrent.getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        mCurrent.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
                startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PICTURE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createFile(String originalObjectID, String name, String layerFolderName, String extension, String type, String deviceNo) {
        try {

            Date d = new Date();
            String date = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.ENGLISH).format(d);
            TEMP_PHOTO_FILE_NAME = "Image_" + new SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH).format(d) + layerFolderName + "_" + name + "." + extension;

            File rootFolder = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), IMAGE_FOLDER_NAME);

            if (!rootFolder.exists()) {
                if (rootFolder.mkdir()) {
                    Log.i(TAG, "createFile(): rootFolder created");
                } else {
                    Log.i(TAG, "createFile(): rootFolder director not created");
                }
            }

            File dateFolderName = new File(rootFolder, new SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH).format(d));

            if (!dateFolderName.exists()) {

                if (dateFolderName.mkdir()) {
                    Log.i(TAG, "createFile(): dateFolderName directory created");

                } else {
                    Log.i(TAG, "createFile(): dateFolderName directory not created");
                }
            }

            File layerFolder = new File(dateFolderName.getPath(), layerFolderName);

            if (!layerFolder.exists()) {
                if (layerFolder.mkdir()) {
                    Log.i(TAG, "createFile(): layerFolder directory is created = " + layerFolder.toString());
                } else {
                    Log.i(TAG, "createFile(): layerFolder directory not created");
                }
            }

            this.deviceNo = deviceNo;
            File pointFolder = new File(layerFolder.getPath(), (deviceNo));
            if (!pointFolder.exists()) {
                if (pointFolder.mkdir()) {
                    Log.i(TAG, "createFile(): pointFolder directory is created = " + pointFolder.toString());
                } else {
                    Log.i(TAG, "createFile(): pointFolder director not created");
                }
            }
            File[] existsImages = presenter.getFiles(pointFolder, null);
            imageIndex = getImagesIndexFromOriginalFolder(existsImages);

            mFileTemp = new File(pointFolder.getPath() + File.separator +
                    type + "_" + date + "_" + deviceNo + "_" + layerFolderName + "_(" + imageIndex + ")_" + name + "." + extension.trim());

            Log.i(TAG, "createFile(): pointFolder directory is created = " + pointFolder.toString());

            // rename image...
            Log.i(TAG, "file createFile " + mFileTemp.getPath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createCompressedTempFile(String originalObjectID, String name, String layerFolderName, String extension, String type, String deviceNo) {
        try {
            Date d = new Date();
            String date = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.ENGLISH).format(d);


            mFileTemp = new File(presenter.getImageFile().getPath() + File.separator +
                    type + "_" + date + "_" + deviceNo + "_" + layerFolderName + "_(" + imageIndex + ")_" + name + "." + extension.trim());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getImagesIndexFromOriginalFolder(File[] existsImages) {
        int max = 0;
        boolean foundImages = false;
        try {
            for (File image : existsImages) {
                String[] split_1 = image.getPath().split("(\\()|(\\))");
                for (int i = 0; i < split_1.length; i++) {
                    Log.i(TAG, "getImagesIndex: split_1[" + i + "] = " + split_1[i]);
                }
                int index = Integer.parseInt(split_1[1]);

                if (index >= max) {
                    max = index;
                    foundImages = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (foundImages) {
            max++;
        }
        return max;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult(): is called");
        Log.i(TAG, "onActivityResult(): requestCode = " + requestCode);
        Log.i(TAG, "onActivityResult(): resultCode = " + resultCode);

        if ((resultCode == Activity.RESULT_OK)) {
            switch (requestCode) {
                case REQUEST_CODE_TAKE_PICTURE:
                    if (mFileTemp != null && mFileTemp.exists() && mFileTemp.getPath() != null) {
                        presenter.createBackupImage(mFileTemp, pointFolderName, deviceNo);
                    }

                    break;
                case REQUEST_CODE_GALLERY:
                    if (data != null) {
                        try {
                            if (data != null && data.getData() != null) {
                                Uri uri = data.getData();
                                try {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(mCurrent.getContentResolver(), uri);
                                    Log.d(TAG, String.valueOf(bitmap));
                                    writeBitmapInFile(bitmap);
                                    if (mFileTemp != null && mFileTemp.exists() && mFileTemp.getPath() != null) {
                                        presenter.createOriginalImage(mFileTemp, pointFolderName, deviceNo);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case REQUEST_CODE_VIDEO:
                    try {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;


            }
        }
    }

    private void addAttachmentToRecyclerView(int imageIndex, String deviceNo) {
        try {
            Log.i(TAG, "addAttachmentToRecyclerView: mStatus before = " + mStatus);
            if (mStatus != Columns.CreatePoint) {
                mStatus = Columns.EditPoint;
            }
            Log.i(TAG, "addAttachmentToRecyclerView: mStatus after = " + mStatus);
            if (mFileTemp != null && mFileTemp.getPath() != null) {
                Log.i(TAG, "addAttachmentToRecyclerView(): mFile Temp != null");
                presenter.compressImage(mFileTemp.getPath(), mCurrent, imageIndex, deviceNo);
            } else {
                Log.i(TAG, "addAttachmentToRecyclerView(): mFile Temp = null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeBitmapInFile(Bitmap bmp) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mFileTemp);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void openGallery() {

        String pointName = objectID;
        String pointFolderName = "";
        String deviceNo = "";
        if (mCurrent.onlineData) {
            pointFolderName = (selectedLayer.getName());/*.split("\\.")[2]);*/

            if (mDeviceNoET.getText() != null && !mDeviceNoET.getText().toString().isEmpty() && !mDeviceNoET.getText().toString().toLowerCase().equals("null")) {
                deviceNo = mDeviceNoET.getText().toString();
                this.deviceNo = deviceNo;
            } else {
                mDeviceNoET.setError(mCurrent.getString(R.string.required));
                Utilities.showToast(mCurrent, "من فضلك ادخل رقم المساح اولأا");
                return;
            }

            Log.i(TAG, "openGallery(): layer folder name = " + pointFolderName);
        } else {
            Log.i(TAG, "layer name = " + selectedLayer.getName());
            pointFolderName = (selectedLayer.getName());
        }
        String OriginalObjectId = "";

        try {
            OriginalObjectId = String.valueOf(selectedFeature.getAttributes().get(Columns.ID));
        } catch (Exception e) {
            e.printStackTrace();
            OriginalObjectId = "NULL";
        }

        createCompressedTempFile(OriginalObjectId, pointName, pointFolderName, JPG, "IMG", deviceNo);

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mTakePictureFAB)) {
            takePicture();
        }
    }

    @Override
    public void onDeleteImage(File image, int position) {
        String deviceNo = "";
        if (mDeviceNoET.getText() != null && !mDeviceNoET.getText().toString().isEmpty()) {
            deviceNo = mDeviceNoET.getText().toString();
        }
        adapterData.remove(position);
        presenter.deleteImage(image, objectID, pointFolderName, deviceNo, position);
    }

    @Override
    public void onImageDeletedSuccess(boolean status, int position, Throwable t) {
        if (status) {
            mOfflineAttachmentRVAdapter.refreshList(adapterData, position);
        } else {
            if (t != null) {
                t.printStackTrace();
            }
        }
    }

    @Override
    public void onImageCompressedSuccess(File mImageFile) {
        Log.e(TAG, "onImageCompressedSuccess: is called");
        if (mCurrent != null) {
            Log.e(TAG, "onImageCompressedSuccess: mCurrent not null");
            mCurrent.runOnUiThread(() -> {
                try {
                    compressedImageFile = mImageFile;
                    mOfflineAttachmentRVAdapter.addImageBitmap(compressedImageFile);
                    Log.d(TAG, "onImageCompressedSuccess: notifying offlineAttachmentRVAdapter");
                    mOfflineAttachmentsRV.setVisibility(View.VISIBLE);
                    mOfflineAttachmentRVAdapter.notifyDataSetChanged();
                    Log.d(TAG, "onImageCompressedSuccess: run: calling createImageBody");
                    presenter.createImageBody(mFileTemp, compressedImageFile, pointFolderName, deviceNo, mFileTemp.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            Log.e(TAG, "onImageCompressedSuccess: mCurrent is NULL");
        }
        presenter.createOriginalImage(mFileTemp, pointFolderName, deviceNo);
    }

    @Override
    public void onOriginalImageCreated(File originalImage) {
        try {
            mFileTemp = originalImage;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackupImageCreated(int imageIndex) {
        try {
            this.imageIndex = imageIndex;
            addAttachmentToRecyclerView(imageIndex, deviceNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.equals(typesSpinner)) {
            String typeCode = codeList.get(typesSpinner.getSelectedItemPosition());
            mCodeET.setText(typeCode);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
//            presenter.dispatchBgThread();
            presenter.closeRealmInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
