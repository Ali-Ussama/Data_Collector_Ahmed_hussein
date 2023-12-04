package com.ekc.ekccollector.collector.view.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import com.ekc.ekccollector.collector.DataCollectionApplication;
import com.ekc.ekccollector.collector.model.PrefManager;
import com.ekc.ekccollector.collector.model.models.ImageBodyRealm;
import com.ekc.ekccollector.collector.model.singleton.ThreadSingleton;
import com.ekc.ekccollector.collector.view.activities.map.MapActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;

public class EditFragPresenter {

    private final String IMAGE_FOLDER_NAME = "AJC_Collector";
    private final String IMAGE_FOLDER_NAME_BACKUP = "AJC_Collector_Backup";
    private final String threadName = "EditFragThread";
    private static final String TAG = "EditFragPresenter";
    private MapActivity mCurrent;
    private EditFragListener listener;
    private PrefManager mPrefManager;
    private Date date;
    public Realm realm;
    private String surveyorName;
    private HandlerThread handlerThread;
    private Handler handler;
    private ThreadSingleton mThreadSingleton;

    public EditFragPresenter(MapActivity mCurrent, EditFragListener listener, ThreadSingleton threadSingleton) {
        this.mCurrent = mCurrent;
        this.listener = listener;
        this.mPrefManager = new PrefManager(this.mCurrent);
        surveyorName = mPrefManager.readString(PrefManager.KEY_SURVEYOR_NAME);
        mThreadSingleton = DataCollectionApplication.getThreadSingleton();
        realm = Realm.getDefaultInstance();
    }

    public void initBgThread() {
        try {
            Log.d(TAG, "initBgThread: is called");
//            handlerThread = new HandlerThread(threadName);
//            handlerThread.start();
//            handler = new Handler(handlerThread.getLooper());
            mThreadSingleton.initBgThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void dispatchBgThread() {
        try {
//            handlerThread.quit();
//            handlerThread = null;
//            handler = null;
            mThreadSingleton.dispatchBgThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteImage(File Image, String ObjectID, String layerName, String deviceNo, int position) {
        try {
            boolean deleted = true;
            boolean adapterImageDeleted = Image.delete();
            Log.i(TAG, "deleteImage: adapterImageDeleted = " + adapterImageDeleted);
            listener.onImageDeletedSuccess(deleted, position, null);
        } catch (Exception e) {
            listener.onImageDeletedSuccess(false, 0, e);
        }

    }

    public File[] getFiles(File folder, String filter) {
        return folder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
                        || (filter != null && name.contains(filter)) || (filter == null));
            }
        });
    }

    public String getSurveyorName() {
        return mPrefManager.readString(PrefManager.KEY_SURVEYOR_NAME);
    }

    public void createBackupImage(File source, String layerFolderName, String deviceNo) {
        Log.d(TAG, "createBackupImage: is called");

        try {
            Log.d(TAG, "createBackupImage: run: is called");
            int imageIndex = 0;
            File destination = null;
            Date d = new Date();
            String date = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.ENGLISH).format(d);

            File rootFolder = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), IMAGE_FOLDER_NAME_BACKUP);

            if (!rootFolder.exists()) {
                if (rootFolder.mkdir()) {
                    Log.i(TAG, "createBackupImage(): rootFolder created");
                } else {
                    Log.i(TAG, "createBackupImage(): rootFolder director not created");
                }
            }

            File dateFolderName = new File(rootFolder, new SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH).format(d));

            if (!dateFolderName.exists()) {

                if (dateFolderName.mkdir()) {
                    Log.i(TAG, "createBackupImage(): dateFolderName directory created");

                } else {
                    Log.i(TAG, "createBackupImage(): dateFolderName directory not created");
                }
            }

            File layerFolder = new File(dateFolderName.getPath(), layerFolderName);

            if (!layerFolder.exists()) {
                if (layerFolder.mkdir()) {
                    Log.i(TAG, "createBackupImage(): layerFolder directory is created = " + layerFolder.toString());
                } else {
                    Log.i(TAG, "createBackupImage(): layerFolder directory not created");
                }
            }

            File pointFolder = new File(layerFolder.getPath(), (deviceNo));
            if (!pointFolder.exists()) {
                if (pointFolder.mkdir()) {
                    Log.i(TAG, "createBackupImage(): pointFolder directory is created = " + pointFolder.toString());
                } else {
                    Log.i(TAG, "createBackupImage(): pointFolder director not created");
                }
            }
            File[] existsImages = getFiles(pointFolder, null);

            imageIndex = getImagesIndexFromOriginalFolder(existsImages);

            destination = new File(pointFolder.getPath() + File.separator +
                    "IMG_" + date + "_" + deviceNo + "_" + layerFolderName + "_(" + imageIndex + ")_.png");

            FileInputStream in = new FileInputStream(source);
            FileOutputStream out = new FileOutputStream(destination);

            byte[] buffer = new byte[(int) source.length()];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

            Log.i(TAG, "createBackupImage: copy done");

            if (listener != null) {
                listener.onBackupImageCreated(imageIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createOriginalImage(File source, String layerFolderName, String deviceNo) {
        Log.d(TAG, "createOriginalImage: is called");
        if (mThreadSingleton != null && mThreadSingleton.getHandler() != null) {
            Log.e(TAG, "createOriginalImage: handler NOT NULL");
            mThreadSingleton.getHandler().post(() -> {
                try {
                    File destination = null;
                    int imageIndex = 0;
                    Date d = new Date();
                    String date = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.ENGLISH).format(d);

                    File rootFolder = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DCIM), IMAGE_FOLDER_NAME);

                    if (!rootFolder.exists()) {
                        if (rootFolder.mkdir()) {
                            Log.i(TAG, "createBackupImage(): rootFolder created");
                        } else {
                            Log.i(TAG, "createBackupImage(): rootFolder director not created");
                        }
                    }

                    File dateFolderName = new File(rootFolder, new SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH).format(d));

                    if (!dateFolderName.exists()) {

                        if (dateFolderName.mkdir()) {
                            Log.i(TAG, "createBackupImage(): dateFolderName directory created");

                        } else {
                            Log.i(TAG, "createBackupImage(): dateFolderName directory not created");
                        }
                    }

                    File layerFolder = new File(dateFolderName.getPath(), layerFolderName);

                    if (!layerFolder.exists()) {
                        if (layerFolder.mkdir()) {
                            Log.i(TAG, "createBackupImage(): layerFolder directory is created = " + layerFolder.toString());
                        } else {
                            Log.i(TAG, "createBackupImage(): layerFolder directory not created");
                        }
                    }

                    File pointFolder = new File(layerFolder.getPath(), (deviceNo));
                    if (!pointFolder.exists()) {
                        if (pointFolder.mkdir()) {
                            Log.i(TAG, "createBackupImage(): pointFolder directory is created = " + pointFolder.toString());
                        } else {
                            Log.i(TAG, "createBackupImage(): pointFolder director not created");
                        }
                    }
                    File[] existsImages = getFiles(pointFolder, null);

                    imageIndex = getImagesIndexFromOriginalFolder(existsImages);

                    destination = new File(pointFolder.getPath() + File.separator +
                            "IMG_" + date + "_" + deviceNo + "_" + layerFolderName + "_(" + imageIndex + ")_.png");

                    FileInputStream in = new FileInputStream(source);
                    FileOutputStream out = new FileOutputStream(destination);

                    byte[] buffer = new byte[(int) source.length()];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }

                    in.close();
                    in = null;

                    // write the output file (You have now copied the file)
                    out.flush();
                    out.close();
                    out = null;

                    Log.i(TAG, "createBackupImage: copy done");

                    source.delete();

                    if (listener != null) {
                        listener.onOriginalImageCreated(destination);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            Log.e(TAG, "createOriginalImage: handler is NULL");
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

    public void createImageBody(File image, File compressed, String layerName, String deviceNo, String imageName) {
        try {
            Log.d(TAG, "createImageBody: is called");
            Date d = new Date();
            String dateFolder = new SimpleDateFormat("dd_MM_yyyy", Locale.ENGLISH).format(d);
            realm.executeTransactionAsync(realm -> {
                try {
                    Log.d(TAG, "createImageBody: execute: is called");
                    String[] splitDate = dateFolder.split("_");
                    ImageBodyRealm imageBodyRealm = realm.createObject(ImageBodyRealm.class, imageName);
                    imageBodyRealm.setDay(splitDate[0]);
                    imageBodyRealm.setMonth(splitDate[1]);
                    imageBodyRealm.setYear(splitDate[2]);
                    imageBodyRealm.setDeviceNo(deviceNo);
                    imageBodyRealm.setSurvayor(surveyorName);
                    imageBodyRealm.setLayerName(layerName);
                    imageBodyRealm.setImageFile(image.getPath());
                    imageBodyRealm.setCompressedFile(compressed.getPath());
                    imageBodyRealm.setSentIndicator(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
//            realm.beginTransaction();
//            String[] splitDate = dateFolder.split("_");
//            ImageBodyRealm imageBodyRealm = realm.createObject(ImageBodyRealm.class, imageName);
//            imageBodyRealm.setDay(splitDate[0]);
//            imageBodyRealm.setMonth(splitDate[1]);
//            imageBodyRealm.setYear(splitDate[2]);
//            imageBodyRealm.setDeviceNo(deviceNo);
//            imageBodyRealm.setSurvayor(surveyorName);
//            imageBodyRealm.setLayerName(layerName);
//            imageBodyRealm.setImageFile(image.getPath());
//            imageBodyRealm.setCompressedFile(compressed.getPath());
//            imageBodyRealm.setSentIndicator(0);
//            realm.commitTransaction();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void compressImage(String filePath, Context context, int imageIndex, String deviceNo) {
        Log.d(TAG, "compressImage: is called");
        try {
            Bitmap scaledBitmap = null;

            BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612
            float maxHeight = 816.0f;
            float maxWidth = 612.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image
            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;
                }
            }

//      setting inSampleSize value allows to load a scaled down version of the original image
            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
//          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(filePath, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
            try {
                Canvas canvas = new Canvas(Objects.requireNonNull(scaledBitmap));
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
            } catch (Exception e) {
                e.printStackTrace();
            }
//      check the rotation of the image and display it properly
            ExifInterface exif;
            try {
                exif = new ExifInterface(filePath);

                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                    Log.d("EXIF", "Exif: " + orientation);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                        true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream out = null;

            File file = getImageFile();
            String filename = getFilename(imageIndex, deviceNo);

            File mImageFile = new File(file.getPath(), filename);

            try {
                out = new FileOutputStream(mImageFile);

//          write the compressed bitmap at the destination specified by filename.
                Objects.requireNonNull(scaledBitmap).compress(Bitmap.CompressFormat.PNG, 50, out);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (listener != null) {
                listener.onImageCompressedSuccess(mImageFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getImageFile() {
        final String IMAGES_FOLDER_NAME = "AJC_Collector_COMPRESSED_Images";
        File mediaStorageDir = null;

        try {

            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), IMAGES_FOLDER_NAME);

            if (!mediaStorageDir.exists())
                mediaStorageDir.mkdir();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaStorageDir;
    }

    private String getFilename(int existsImagesIndex, String deviceNo) {
        String uriSting = null;


        try {
            Date d = new Date();
            uriSting = "Image_" + new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.ENGLISH).format(d) + "_(" + existsImagesIndex + ")_" + deviceNo + ".png";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return uriSting;
    }


    public Bitmap decodeScaledBitmapFromSdCard(String filePath, int reqWidth,
                                               int reqHeight) {

// First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                      int reqHeight) {
// Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

// Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public void closeRealmInstance() {
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    public void loadImages(String mDeviceNoET, List<File> imagesAdapter) {
        if (realm != null) {

            realm.beginTransaction();
            RealmResults<ImageBodyRealm> result = realm.where(ImageBodyRealm.class).equalTo("DeviceNo", mDeviceNoET).findAll();
            for (ImageBodyRealm imageBodyRealm : result) {
                imagesAdapter.add(new File(imageBodyRealm.getCompressedFile()));
            }
            realm.commitTransaction();
        }
    }
}
