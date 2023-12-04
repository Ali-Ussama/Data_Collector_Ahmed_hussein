package com.ekc.ekccollector.collector.model;

import android.util.Log;

import com.ekc.ekccollector.collector.model.models.User;
import com.google.firebase.firestore.FirebaseFirestore;


public class SaveUserOnline {

    private static final String TAG = "SaveUserOnline";
    private FirebaseFirestore db;

    private String users_root = "users";

    private String version;

    private String imei;

    public SaveUserOnline() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void saveUserIntoFireStore(User user) {
        Log.d(TAG, "saveUserIntoFireStore: is called");
        db.collection(users_root).document(user.getVersion()).collection(user.getImei()).add(user).addOnCompleteListener(task -> {
            Log.i(TAG, "onComplete: user is add");
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            Log.i(TAG, "onFailure: is called");
        });
    }
}
