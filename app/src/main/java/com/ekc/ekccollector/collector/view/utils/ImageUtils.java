package com.ekc.ekccollector.collector.view.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.ekc.ekccollector.collector.model.PrefManager;
import com.ekc.ekccollector.collector.model.models.ImageBody;
import com.ekc.ekccollector.collector.model.models.ImageBodyRealm;
import com.ekc.ekccollector.collector.view.activities.map.ImageUtilsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final String threadName = "ConvertImageThread";
    private final String IMAGE_FOLDER_NAME_BACKUP = "AJC_Collector_Backup";
    private final String IMAGE_FOLDER_NAME_COMPRESSED = "AJC_Collector_COMPRESSED_Images";
    private HandlerThread handlerThread;
    private Handler handler;
    private List<File> imagesFiles;
    private List<File> compressedImages;
    private List<ImageBody> imageBodies;
    private ImageUtilsListener listener;
    private PrefManager prefManager;
    private String username;
    private Realm realm;
    private boolean isImagesLoadedIntoDb;

    public ImageUtils(ImageUtilsListener listener, PrefManager prefManager) {
        this.listener = listener;
        this.prefManager = prefManager;
        init();
    }

    private void init() {
        imagesFiles = new ArrayList<>();
        imageBodies = new ArrayList<>();
        compressedImages = new ArrayList<>();
        username = prefManager.readString(PrefManager.KEY_SURVEYOR_NAME);
        isImagesLoadedIntoDb = prefManager.readBoolean(PrefManager.KEY_IMAGES_LOADED_INTO_DB);
    }

    public void handleLoadImages() {
        try {
            if (!isImagesLoadedIntoDb) {
                Log.d(TAG, "handleLoadImages: isImagesLoadedIntoDb is false");
                handlerThread = new HandlerThread(threadName);
                handlerThread.start();
                handler = new Handler(handlerThread.getLooper());

                handler.post(() -> {
                    try {
                        realm = Realm.getDefaultInstance();
//                        loadCompressedImages();
                        loadImages();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                Log.d(TAG, "handleLoadImages: isImagesLoadedIntoDb is true");
                listener.OnImageLoadedIntoDb(true, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLoadImagesByDate(String date) {
        try {
            Log.d(TAG, "handleLoadImages: isImagesLoadedIntoDb is false");
            handlerThread = new HandlerThread(threadName);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());

            handler.post(() -> {
                try {
                    realm = Realm.getDefaultInstance();
//                        loadCompressedImages();
                    loadImagesByDate(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

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
//                                imagesFiles.add(image);

//                                for (File file : compressedImages) {
//                                    if (file.getPath().contains("_(") && file.getPath().contains(")_")) {
//                                        String[] split_1 = file.getPath().split("(\\)_)");
//                                        String[] split_2 = split_1[1].split("\\.");
//                                        Log.d(TAG, "loadImages: pointName = " + point.getName() + " - compressedImgName = " + split_2[0]);
//                                        if (split_2[0].equals(point.getName())) {
//                                            Log.i(TAG, "loadImages(): image path = " + file.getPath() + " contains DeviceNo = " + (point.getName()));
//                                            if (getImageIndex(file).matches(getImageIndex(image))) {
                                createImageBody(image, null, username, dateFolder.getName(), layer.getName(), point.getName(), image.getName());
//                                            }
//                                        }
//                                    }
                            }
                        }
                        Log.d(TAG, "loadImages: \n\n");
                    }
                }
            }

            prefManager.saveBoolean(PrefManager.KEY_IMAGES_LOADED_INTO_DB, true);
            if (listener != null) {
                listener.OnImageLoadedIntoDb(true, null, null);
            }
//            }
        } catch (Exception e) {
            listener.OnImageLoadedIntoDb(false, e, null);
        }
    }

    public void loadImagesByDate(String date) {
        try {
            boolean founded = false;
            Log.d(TAG, "loadImages: is called");
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + File.separator + IMAGE_FOLDER_NAME_BACKUP + File.separator;
            Log.i(TAG, "loadImages(): path = " + path);
            File root = new File(path);
            //root path = ~ / DCIM / AJC_COLLECTOR_Backup /
            if (root.exists()) {
                File[] allFiles = getFiles(root, null);
                Log.i(TAG, "loadImages: layers date folders count = " + allFiles.length);
                for (File dateFolder : allFiles) {//Date Folders ex: 3/3/2020
                    if (date.equals(dateFolder.getName())) {
                        founded = true;
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
//                                imagesFiles.add(image);

//                                for (File file : compressedImages) {
//                                    if (file.getPath().contains("_(") && file.getPath().contains(")_")) {
//                                        String[] split_1 = file.getPath().split("(\\)_)");
//                                        String[] split_2 = split_1[1].split("\\.");
//                                        Log.d(TAG, "loadImages: pointName = " + point.getName() + " - compressedImgName = " + split_2[0]);
//                                        if (split_2[0].equals(point.getName())) {
//                                            Log.i(TAG, "loadImages(): image path = " + file.getPath() + " contains DeviceNo = " + (point.getName()));
//                                            if (getImageIndex(file).matches(getImageIndex(image))) {
                                    createImageBody(image, null, username, dateFolder.getName(), layer.getName(), point.getName(), image.getName());
//                                            }
//                                        }
//                                    }
                                }
                            }
                            Log.d(TAG, "loadImages: \n\n");
                        }
                        break;
                    }
                }
            }

            if (listener != null && founded) {
                listener.OnImageLoadedIntoDb(true, null, date);
            } else {
                listener.OnImageLoadedIntoDb(false, null, date);
            }
//            }
        } catch (Exception e) {
//            if (listener != null)
            listener.OnImageLoadedIntoDb(false, e, date);
        }
    }

    public File[] getFiles(File folder, String filter) {
        return folder.listFiles((dir, name) ->
                (name.endsWith(".jpg") ||
                        name.endsWith(".jpeg") ||
                        name.endsWith(".png") ||
                        (filter != null && name.contains(filter)) ||
                        (filter == null)));
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

    private void createImageBody(File image, File compressed, String surveyorName, String dateFolder, String layerName, String deviceNo, String imageName) {
        try {

            realm.beginTransaction();
//            realm.executeTransactionAsync(realm -> {
            try {
                String[] splitDate = dateFolder.split("_");
                ImageBodyRealm imageBodyRealm = realm.createObject(ImageBodyRealm.class, imageName);
                imageBodyRealm.setDay(splitDate[0]);
                imageBodyRealm.setMonth(splitDate[1]);
                imageBodyRealm.setYear(splitDate[2]);
                imageBodyRealm.setDeviceNo(deviceNo);
                imageBodyRealm.setSurvayor(surveyorName);
                imageBodyRealm.setLayerName(layerName);
                imageBodyRealm.setImageFile(image.getPath());
                imageBodyRealm.setCompressedFile(null);
                imageBodyRealm.setSentIndicator(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            });
            realm.commitTransaction();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSentImage(ImageBodyRealm image, int indicator, Realm realm) {
        try {
            Log.d(TAG, "updateSentImage: is called");
            realm.beginTransaction();
            image.setSentIndicator(indicator);
            Log.d(TAG, "updateSentImage: indicator = " + image.getSentIndicator());
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
