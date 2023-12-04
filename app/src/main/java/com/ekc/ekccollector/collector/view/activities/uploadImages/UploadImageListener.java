package com.ekc.ekccollector.collector.view.activities.uploadImages;

import com.ekc.ekccollector.collector.model.models.ImageBody;

import java.io.File;
import java.util.List;

public interface UploadImageListener {

    void onDatePickedUp(String date, String dayOfMonth, String month, String year);

    void onImageUploaded(int index);

    void onImageFailed(Throwable t, int index);

    void onImageLoadedFromStorage(List<ImageBody> imageBodies, List<File> mImageFiles);
}
