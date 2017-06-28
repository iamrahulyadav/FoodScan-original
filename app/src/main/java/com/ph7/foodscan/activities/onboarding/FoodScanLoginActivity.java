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

    private SessionService sessionService = new SessionService();

    private final static int FS_LOGIN_RESULT = 2000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFoodScan();

    }

    private void initFoodScan() {
        loginFoodScan() ;
    }

    private void loginFoodScan() {
        startActivityForResult(new Intent(FoodScanLoginActivity.this, AuthDialog.class),FS_LOGIN_RESULT); //AuthDialog.class
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final FoodScanLoginActivity _this = this;
        if (requestCode == FS_LOGIN_RESULT) {
            if(resultCode == RESULT_OK) {
                String token  = data.getStringExtra("token");
                _this.sessionService.setUserToken(token);
                startActivity(new Intent(_this, DashboardActivity.class));
                finish();
            }else if (resultCode == RESULT_CANCELED)
            {
                finish();
            }
        }
    }


}
