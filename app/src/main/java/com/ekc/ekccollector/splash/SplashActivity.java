package com.ekc.ekccollector.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.ekc.ekccollector.R;
import com.ekc.ekccollector.collector.model.PrefManager;
import com.ekc.ekccollector.collector.view.activities.login.LoginActivity;
import com.ekc.ekccollector.collector.view.activities.map.MapActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //If user not logged in before open login activity
                Intent intent ;
                if (isLoggedInBefore()) {
                intent =new Intent(SplashActivity.this, MapActivity.class);
                }else{
                    intent =new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 3000);
    }


    boolean isLoggedInBefore() {
        try {
            PrefManager prefManager = new PrefManager(this);

            String code = prefManager.readString(PrefManager.KEY_SURVEYOR_CODE);
            if (code != null && !code.isEmpty()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
