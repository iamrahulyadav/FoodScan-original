package com.ph7.foodscan.activities.onboarding;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ph7.foodscan.activities.AppActivity;
import com.ph7.foodscan.activities.main.DashboardActivity;
import com.ph7.foodscan.callbacks.FoodScanHandler;
import com.ph7.foodscan.services.FoodScanService;
import com.ph7.foodscan.services.SessionService;
import com.ph7.foodscan.utils.Validation;
import com.ph7.foodscan.views.StatusView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * The FoodScanLoginActivity is responsible for logging the user into the FoodScan API and keeping track
 * of their access token through the SessionService.
 *
 * @author  Craig Tweedy
 * @version 0.7
 * @since   2016-07-04
 */
public class FoodScanLoginActivity extends AppActivity {
    private static final String TAG = FoodScanLoginActivity.class.getSimpleName();

    private FoodScanService foodScanService = new FoodScanService();
    private SessionService sessionService = new SessionService();

    private final static int FS_LOGIN_RESULT = 2000;

    EditText email;
    EditText password;
    TextView email_err, pass_err ;
    TextView loginButton ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFoodScan();

//        setContentView(R.layout.activity_food_scan_login);
//
//        email = (EditText) findViewById(R.id.email);
//        email.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                checkBlankFieldValidation();
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//        password = (EditText) findViewById(R.id.password);
//        password.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                checkBlankFieldValidation();
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//        email_err = (TextView) findViewById(R.id.email_err);
//        pass_err = (TextView) findViewById(R.id.pass_err);
//        loginButton  =  (TextView) findViewById(R.id.login) ;
//        email_err.setVisibility(View.INVISIBLE);
//        pass_err.setVisibility(View.INVISIBLE);
//        setActionBarOverlayZero();
//        setActionBarOptionHidden(true);
//        setActionBarTitleHidden(true);
//        setActionBarHidden(false);
    }

    private void initFoodScan() {
        loginFoodScan() ;
    }

    private void loginFoodScan() {
        startActivityForResult(new Intent(FoodScanLoginActivity.this, AuthDialog.class),FS_LOGIN_RESULT); //AuthDialog.class
    }

//    private void checkBlankFieldValidation() {
//
//        if(!email.getText().toString().trim().isEmpty() && !password.getText().toString().trim().isEmpty()){
//            loginButton.setEnabled(true);
//            loginButton.setFocusable(true);
//        }
//        else{
//            loginButton.setEnabled(false);
//            loginButton.setFocusable(false);
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final FoodScanLoginActivity _this = this;
        if (requestCode == FS_LOGIN_RESULT) {
            if(resultCode == RESULT_OK) {
                String token  = data.getStringExtra("token");
                _this.sessionService.setUserToken(token);
              //  _this.sessionService.setScanCount(1000);
                startActivity(new Intent(_this, DashboardActivity.class));
                finish();
            }else if (resultCode == RESULT_CANCELED)
            {
                finish();
            }
        }
    }

    public void onLoginButtonClicked(View view) {

        if(!isValidationSuccess()) return ;
        else {
            email_err.setVisibility(View.INVISIBLE);
            pass_err.setVisibility(View.INVISIBLE);
        }

        final FoodScanLoginActivity _this = this;
        Map<String, String> params = new HashMap<>();
        final String username = email.getText().toString().trim() ;
        final String pass = password.getText().toString().trim() ;
        params.put("email", username);
        params.put("password", pass);
        final  ProgressDialog dialog = ProgressDialog.show(FoodScanLoginActivity.this,"","Connecting...",true);
        this.foodScanService.login(params, new FoodScanHandler() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                dialog.dismiss();
                try {

                    _this.sessionService.setUserToken(jsonObject.getString("token"));
                    _this.sessionService.setUsername(username);
                    _this.sessionService.setPassword(pass);

                    /*
                    *************************************************************************
                        On Food Scan login We get this scan Quota value
                        On Each time of login we updated it from foorScan server to local
                    *************************************************************************
                    */

//                    int scans =  1000 ;  //  For Testing Purpose
//                    _this.sessionService.setScanCount(scans);
//                    Log.d(TAG, "Login to PH7 worked.");

                    final StatusView statusView = new StatusView(FoodScanLoginActivity.this);
                    statusView.setStatusCode(1);
                    statusView.setStatusMessage("Success");
                    statusView.show();
                    statusView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            statusView.hide();
                            startActivity(new Intent(_this, DashboardActivity.class));
                            _this.finish();
                        }
                    });



                } catch (JSONException e) {

                    Log.d(TAG, "Setting user token failed");
                    //Toast.makeText(getApplicationContext(), "Could not login", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError() {
                dialog.dismiss();
                final StatusView statusView = new StatusView(FoodScanLoginActivity.this);
                statusView.setStatusCode(0);
                statusView.setStatusMessage("Oops! something went wrong");
                statusView.show();
                statusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        statusView.hide();
                    }
                });

            }
        });
    }

    private boolean isValidationSuccess() {
        boolean isValid = true ;

        if(!Validation.isValidEmail(email.getText().toString().trim()))
        {
            isValid = false ;
            email_err.setVisibility(View.VISIBLE);
            email_err.setText("Incorrect username");

        }
        else
            email_err.setVisibility(View.INVISIBLE);


        return  isValid ;
    }
}
