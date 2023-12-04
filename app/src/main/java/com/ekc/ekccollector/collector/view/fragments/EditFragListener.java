package com.ekc.ekccollector.collector.view.fragments;

import java.io.File;

public interface EditFragListener {

    void onDeleteImage(File image,int position);

    void onImageDeletedSuccess(boolean Status,int position, Throwable t);


    void onImageCompressedSuccess(File mImageFile);

    void onOriginalImageCreated(File originalImage);

    void onBackupImageCreated(int imageIndex);
}
