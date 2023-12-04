package com.ekc.ekccollector.collector.view.activities.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.esri.arcgisruntime.geometry.Polygon;
import com.ekc.ekccollector.R;
import com.ekc.ekccollector.collector.view.activities.map.MapActivity;
import com.ekc.ekccollector.collector.view.utils.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements LoginListener, View.OnClickListener {

    @BindView(R.id.login_code_edit_text)
    EditText mCodeET;

    @BindView(R.id.login_password_edit_text)
    EditText mPassword;

    @BindView(R.id.login_surveyor_name_edit_text)
    EditText mSurveyorName;

    @BindView(R.id.login_button)
    Button mLoginBtn;

    @BindView(R.id.login_version_no)
    TextView mVersionNumber;

    private LoginPresenter presenter;
    private LoginActivity mCurrent;
    private String code;
    private String pass;
    private String surveyorName;

    final int MY_LOCATION_REQUEST_CODE = 2;
    private static final int REQUEST_CODE_GALLERY = 1;
    private static final int REQUEST_CODE_TAKE_PICTURE = 2;
    private static final int WRITE_EXTERNAL_STORAGE = 3;
    private static final int READ_EXTERNAL_STORAGE = 4;
    private static final int REQUEST_CODE_VIDEO = 5;
    private static final int REQUEST_CODE_AUDIO = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_login);

            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            mCurrent = LoginActivity.this;
            ButterKnife.bind(this);
            presenter = new LoginPresenter(this, mCurrent);
            mLoginBtn.setOnClickListener(this);

            setVersion();

            checkPermissions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPermissions() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(mCurrent,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                        MY_LOCATION_REQUEST_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setVersion() {
        try {
            String mVersionName = Utilities.getVersionInfo(mCurrent)[1];
            mVersionNumber.setText(getString(R.string.version).concat(" ").concat(mVersionName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLogin(Polygon polygon, boolean status) {
        try {
            if (status && polygon != null) {

                presenter.cacheSurveyorData(code, pass, surveyorName);

                Utilities.dismissLoadingDialog();
                Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                startActivity(intent);
            } else {
                Utilities.dismissLoadingDialog();
                Utilities.showToast(mCurrent, getString(R.string.network_connection_failed));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if (mSurveyorName.getText() == null || mSurveyorName.getText().toString().trim().isEmpty() || TextUtils.isDigitsOnly(mSurveyorName.getText().toString())) {
                mSurveyorName.setError(getString(R.string.required));
            } else if (mPassword.getText() == null || mPassword.getText().toString().isEmpty()) {
                mPassword.setError(getString(R.string.required));
            } else if (mCodeET.getText() == null || mCodeET.getText().toString().isEmpty()) {
                mCodeET.setError(getString(R.string.required));
            } else {
                pass = mPassword.getText().toString();
                code = mCodeET.getText().toString();
                surveyorName = mSurveyorName.getText().toString();

                if (!pass.toLowerCase().matches("123456")) {
                    Utilities.showToast(mCurrent, getString(R.string.wrong_username_or_password));
                } else {
                    if (code.equals("54722") || code.equals("1")) {
                        supervisorLogin(code, pass, surveyorName);
                    } else {
                        if (presenter != null) {
                            Utilities.showLoadingDialog(mCurrent);
                            presenter.login(pass, code);
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void supervisorLogin(String code, String pass, String surveyorName) {
        try {
            if (presenter != null) {
                presenter.cacheSurveyorData(code, pass, surveyorName);
            }

            Intent intent = new Intent(LoginActivity.this, MapActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
