package com.ph7.analyserforscio.activities.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.consumerphysics.android.sdk.sciosdk.ScioLoginActivity;
import com.ph7.analyserforscio.activities.AppActivity;

/**
 * The CPLoginActivity is responsible for logging the user into ConsumerPhysics and keeping track
 * of their access token through the SessionService.
 *
 * @author  Craig Tweedy
 * @version 0.7
 * @since   2016-07-04
 */
public class CPLoginActivity  extends AppActivity {
    private static final String TAG = CPLoginActivity.class.getSimpleName();

    private final static int LOGIN_ACTIVITY_RESULT = 1000;

    private static final String REDIRECT_URL = "http://www.consumerphysics.com";
    private static final String APPLICATION_KEY = "8a8095f6-39cd-4ec5-b03f-545e606a5b26";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initScioCloud();
    }

    private void initScioCloud() {
        login();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LOGIN_ACTIVITY_RESULT:
                if (resultCode == RESULT_OK && this.scioCloud.hasAccessToken()) {
                    Log.d(TAG, "We are logged in.");
                    loggedIn();
                }
                else {
                    if(data!=null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final String description = data.getStringExtra(ScioLoginActivity.ERROR_DESCRIPTION);
                                final int errorCode = data.getIntExtra(ScioLoginActivity.ERROR_CODE, -1);
                                Log.d("CP Error" ,"Error Code : "+errorCode+" Description : "+description);
                                if(errorCode == -2)
                                    Toast.makeText(CPLoginActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                                else {
                                    login();
                                }
                                //Toast.makeText(CPLoginActivity.this, "An error has occurred.\nError code: " + errorCode + "\nDescription: " + description, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else finish();
                }

                break;
            default:
                break;
        }
    }

    private void login() {
        final Intent intent = new Intent(this, ScioLoginActivity.class);
        intent.putExtra(ScioLoginActivity.INTENT_REDIRECT_URI, REDIRECT_URL);
        intent.putExtra(ScioLoginActivity.INTENT_APPLICATION_ID, APPLICATION_KEY);
        startActivityForResult(intent, LOGIN_ACTIVITY_RESULT);
    }

    private void loggedIn() {
        startActivity(new Intent(this, FoodScanLoginActivity.class));
        finish();
    }


}
