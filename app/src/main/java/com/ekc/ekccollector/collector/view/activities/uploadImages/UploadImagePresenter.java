package com.ekc.ekccollector.collector.view.activities.uploadImages;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;

import com.ekc.ekccollector.collector.model.PrefManager;
import com.ekc.ekccollector.collector.model.api.ApiClient;
import com.ekc.ekccollector.collector.model.models.ImageBody;
import com.ekc.ekccollector.collector.model.models.ImageBodyRealm;
import com.ekc.ekccollector.collector.view.activities.map.ImageUtilsListener;
import com.ekc.ekccollector.collector.view.utils.ImageUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

class UploadImagePresenter implements ImageUtilsListener {
    private static final String TAG = "UploadImagePresenter";
    private static final String threadName = "ConvertImageThread";
    private final String IMAGE_FOLDER_NAME_BACKUP = "AJC_Collector_Backup";
    private final String IMAGE_FOLDER_NAME_COMPRESSED = "AJC_Collector_COMPRESSED_Images";

    private UploadImageActivity mContext;
    private UploadImageListener listener;
    private ApiClient apiClient;
    private List<File> imagesFiles;
    private List<File> compressedImages;
    private List<ImageBody> imageBodies;
    private HandlerThread handlerThread;
    private Handler handler;
    private PrefManager prefManager;
    private String username;
    private Realm realm;
    private RealmResults<ImageBodyRealm> imagesDBList;
    private ImageUtils imageUtils;

    UploadImagePresenter(UploadImageActivity mContext, UploadImageListener listener) {
        try {
            this.mContext = mContext;
            this.listener = listener;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void init(String dayOfMonth, String month, String year) {
        try {
            apiClient = new ApiClient();
            imagesFiles = new ArrayList<>();
            imageBodies = new ArrayList<>();
            compressedImages = new ArrayList<>();
            prefManager = new PrefManager(mContext);
            username = prefManager.readString(PrefManager.KEY_SURVEYOR_NAME);
            realm = Realm.getDefaultInstance();
            imageUtils = new ImageUtils(this, prefManager);
            initBgThread();
            readImagesFromDb(dayOfMonth, month, year);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readImagesFromDb(String dayOfMonth, String month, String year) {
        try {
//            String dayStr = String.valueOf(dayOfMonth);
//            String monthStr = String.valueOf(month);
//            String yearStr = String.valueOf(year);
//
//            if (dayOfMonth <= 9) {
//                dayStr = "0".concat(dayStr);
//            }
//            if (month <= 9) {
//                monthStr = "0".concat(monthStr);
//            }

            realm.beginTransaction();
            imagesDBList = realm.where(ImageBodyRealm.class)
                    .equalTo("sentIndicator", 0)
                    .and()
                    .equalTo("day", dayOfMonth)
                    .and()
                    .equalTo("month", month)
                    .and()
                    .equalTo("year", year)
                    .findAll();
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initBgThread() {
        try {
            handlerThread = new HandlerThread(threadName);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void uploadNextImage(int index) {
        try {
            if (handler != null) {
                handler.post(() -> {
                    try {
//                        String imageContent = convertImgFileToBase64(imageBodies.get(index).getImageFile());
//                        imageBodies.get(index).setImageContent(imageContent);
                        uploadImage(imageBodies.get(index), index);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertImgFileToBase64(File imgFile) {
        String imgBase64 = "";
        try {
            byte[] bytes = new byte[(int) imgFile.length()];
            FileInputStream fis = new FileInputStream(imgFile);
            BufferedInputStream buf = new BufferedInputStream(fis);
            boolean isRead = buf.read(bytes, 0, bytes.length) > 0;
            buf.close();
            if (isRead)
                imgBase64 = Base64.encodeToString(bytes, Base64.DEFAULT);
            //imgBase64 = imgBase64.replaceAll(System.getProperty("line.separator"), "");// to remove\n
        } catch (Exception e) {
            //
        }
        return "data:image/jpg;base64," + imgBase64;
    }

    private void uploadImage(ImageBody imageBody, int imageIndex) {
        try {
            apiClient.uploadImage(imageBody, new ApiClient.CommonCallback<Object>() {
                @Override
                public void onSuccess(Object response) {
                    try {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.d(TAG, "onSuccess: is called");
                                    Log.d(TAG, "onSuccess: response -> " + response.toString());
                                    Log.d(TAG, "onSuccess: index = " + imageIndex + " - imagesDbList size = " + imagesDBList.size());
                                    if (imagesDBList != null && !imagesDBList.isEmpty()) {
                                        ImageBodyRealm imageBodyRealm = imagesDBList.first();
                                        imageUtils.updateSentImage(imageBodyRealm, 1, realm);
                                    }
                                    if (listener != null) {
                                        listener.onImageUploaded(imageIndex);
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

                @Override
                public void onFailure(Throwable throwable) {
                    Log.d(TAG, "onFailure: is called");
                    Log.d(TAG, "onFailure: imageIndex = " + imageIndex);
                    throwable.printStackTrace();
                    if (listener != null) {
                        listener.onImageFailed(throwable, imageIndex);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void handleLoadImages() {
        handler.post(() -> {
            try {
                loadCompressedImages();
                loadImages();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    void loadImages2(String date) {
        try {
            Log.d(TAG, "loadImages2: is called with date = " + date);
            if (date.contains("/")) {
                date = date.replace("/", "_");
            }

            if (imagesDBList != null && imagesDBList.isLoaded() && !imagesDBList.isEmpty()) {
                for (ImageBodyRealm imageBodyRealm : imagesDBList) {
                    Log.d(TAG, "loadImages2: DeviceNo = " + imageBodyRealm.getDeviceNo() + " - imageName = " + imageBodyRealm.getImageName() + " - indicator = " + imageBodyRealm.getSentIndicator());
                    createImageBody(imageBodyRealm.getImageFile(), imageBodyRealm.getCompressedFile(), imageBodyRealm.getSurvayor(),
                            imageBodyRealm.getDay(), imageBodyRealm.getMonth(), imageBodyRealm.getYear(), imageBodyRealm.getLayerName(),
                            imageBodyRealm.getDeviceNo(), imageBodyRealm.getImageName());
                }
            }
            if (imageBodies != null && !imageBodies.isEmpty()) {
                if (listener != null) {
                    listener.onImageLoadedFromStorage(imageBodies, null);
                }
            } else {
                Log.d(TAG, "loadImages2: calling loadImagesByDate");
                imageUtils.handleLoadImagesByDate(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCompressedImages() {
        try {
            Log.d(TAG, "loadCompressedImages: is called");
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + IMAGE_FOLDER_NAME_COMPRESSED + File.separator;
            Log.i(TAG, "loadImages2(): path = " + path);
            File root = new File(path);
            //root path = ~ / DCIM / AJC_COLLECTOR_Backup /
            if (root.exists()) {
                File[] allFiles = getFiles(root, null);
                compressedImages.addAll(Arrays.asList(allFiles));
                Log.d(TAG, "loadCompressedImages: compressedImages size = " + compressedImages.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadImages() {
        try {
            Log.d(TAG, "loadImages: is called");
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + IMAGE_FOLDER_NAME_BACKUP + File.separator;
            Log.i(TAG, "loadImages(): path = " + path);
            File root = new File(path);
            //root path = ~ / DCIM / AJC_COLLECTOR_Backup /
            if (root.exists()) {
                File[] allFiles = getFiles(root, null);
                Log.i(TAG, "loadImages: layers date folders count = " + allFiles.length);
                for (File dateFolder : allFiles) {//Date Folders ex: 3/3/2020
                    File[] layers = getFiles(dateFolder, null);

                    Log.i(TAG, "loadImages: layers folder count = " + layers.length);

                    for (File layer : layers) {//Layers Folders
                        File[] points = getFiles(layer, null);
                        Log.i(TAG, "loadImages: layer folder name = " + layer.getName());
                        Log.i(TAG, "loadImages: points folder count = " + points.length);

                        for (File point : points) {//Points Folders ex: HG23@123
                            File[] images = getFiles(point, null);
                            Log.i(TAG, "loadImages: point folder name = " + point.getName());
                            Log.i(TAG, "loadImages: images files count = " + images.length);

                            for (File image : images) {//Images Files
                                Log.i(TAG, "loadImages(): image path = " + image.getName());
                                imagesFiles.add(image);

                                for (File file : compressedImages) {
                                    if (file.getPath().contains("_(") && file.getPath().contains(")_")) {
                                        String[] split_1 = file.getPath().split("(\\)_)");
                                        String[] split_2 = split_1[1].split("\\.");
                                        Log.d(TAG, "loadImages: pointName = " + point.getName() + " - compressedImgName = " + split_2[0]);
                                        if (split_2[0].equals(point.getName())) {
                                            Log.i(TAG, "loadImages(): image path = " + file.getPath() + " contains DeviceNo = " + (point.getName()));
                                            if (getImageIndex(file).matches(getImageIndex(image))) {

                                                if (!isUploadedImage(image, dateFolder.getName(), layer.getName(), point.getName())) {
//                                                    createImageBody(image, file, username, dateFolder.getName(), layer.getName(), point.getName(), image.getName());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Log.d(TAG, "loadImages: \n\n");
                        }
                    }
                }

                if (listener != null) {
                    listener.onImageLoadedFromStorage(imageBodies, imagesFiles);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isUploadedImage(File image, String date, String layer, String deviceNo) {
        if (imagesDBList != null && !imagesDBList.isEmpty()) {
            for (ImageBodyRealm imageBodyRealm : imagesDBList) {
                if (imageBodyRealm.getImageName().equals(image.getName()) &&
                        imageBodyRealm.getLayerName().equals(layer) &&
                        imageBodyRealm.getDeviceNo().equals(deviceNo) &&
                        imageBodyRealm.getSentIndicator() == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getImageIndex(File file) {
        String result = "";
        try {
            String[] split = file.getName().split("(\\()|(\\))");
            result = split[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void createImageBody(String image, String compressed, String surveyorName, String day, String month, String year, String layerName, String deviceNo, String imageName) {
        try {
            File imageFile = new File(image);
//            File compressedImageFile = new File(compressed);
            //Date 22_04_2020
            ImageBody.DateInfo dateInfo = new ImageBody.DateInfo();
            dateInfo.setDay(day);
            dateInfo.setMonth(month);
            dateInfo.setYear(year);

            ImageBody imageBody = new ImageBody();
            imageBody.setDateInfo(dateInfo);
            imageBody.setDeviceNo(deviceNo);
            imageBody.setSurvayor(surveyorName);
            imageBody.setLayerName(layerName);
            imageBody.setImageName(imageName);
            imageBody.setImageFile(imageFile);
//            imageBody.setCompressedFile(compressedImageFile);

            imageBodies.add(imageBody);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File[] getFiles(File folder, String filter) {
        return folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
                        || (filter != null && name.contains(filter)) || (filter == null));
            }
        });
    }

    void dispatchBgThread() {
        try {
            handlerThread.interrupt();
            handlerThread = null;
            handler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnImageLoadedIntoDb(boolean status, Throwable t, String date) {
        Log.d(TAG, "OnImageLoadedIntoDb: is called with status = " + status);
        if (status) {

            if (mContext != null) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d(TAG, "OnImageLoadedIntoDb: date = " + date);
                            String[] dateSplit = date.split("_");

                            readImagesFromDb(dateSplit[0], dateSplit[1], dateSplit[2]);
                            loadImages2(date);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        } else {
            if (listener != null) {
                listener.onImageLoadedFromStorage(null, null);
            }
            if (t != null)
                t.printStackTrace();
        }
    }
}
