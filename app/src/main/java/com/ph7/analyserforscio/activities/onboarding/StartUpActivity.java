package com.ph7.analyserforscio.activities.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.consumerphysics.android.sdk.sciosdk.ScioCloud;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.ph7.analyserforscio.activities.AppActivity;
import com.ph7.analyserforscio.activities.main.DashboardActivity;
import com.ph7.analyserforscio.services.SessionService;

/**
 * The StartUpActivity is responsible for managing whether the user is logged in and should go to
 * the DashboardActivity, or if they should be pushed through the setup & login process.
 *
 * @author  Craig Tweedy
 * @version 0.7
 * @since   2016-07-04
 */
public class StartUpActivity extends AppActivity {

    private static final String TAG = StartUpActivity.class.getSimpleName();

    private ScioCloud scioCloud;
    private SessionService sessionService = new SessionService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // initScioCloud(); // Before
        licenseCheckModule(); // After on Adding Licensing in App
    }

    private void initScioCloud() {
        scioCloud = new ScioCloud(this);
        checkLogin();
    }

    private void checkLogin() {
        Log.d(TAG, "doLogin");

        if (!scioCloud.hasAccessToken()) {
            //Starting the full process, so connect a scanner first
            startActivity(new Intent(this, ConnectScannerActivity.class));
            finish();
        }
        else {
            Log.d(TAG, "Already have token");
            if (this.sessionService.isUserLoggedInToFoodScan()) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, FoodScanLoginActivity.class));
                finish();
            }
        }
    }


    private FoodScanLicenseCallback mLicenseCheckerCallback ;
    private LicenseChecker mChecker;
    // Generate 20 random bytes, and put them here.
    private static final byte[] SALT = new byte[] {
            -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95,
            -45, 77, -117, -36, -113, -11, 32, -64, 89
    };

    private void licenseCheckModule() {

        // Check Licensing here

        mLicenseCheckerCallback = new FoodScanLicenseCallback();

        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        // Construct the LicenseChecker with a Policy.
        mChecker = new LicenseChecker(
                this, new ServerManagedPolicy(this,
                new AESObfuscator(SALT, getPackageName(), deviceId)),
                base64EncodedPublicKey  // Your public licensing key.
        );

        mChecker.checkAccess(mLicenseCheckerCallback);
    }

    private class FoodScanLicenseCallback implements LicenseCheckerCallback {
        public void allow(int reason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                ///initScioCloud();
                return;
            }
            // Should allow user access.
            //displayResult(getString(R.string.allow));
            initScioCloud();
        }

        public void dontAllow(int reason) {
            if (isFinishing()) {
                // Don't update UI if Activity is finishing.
                return;
            }
            // displayResult(getString(R.string.dont_allow));

            if (reason == Policy.RETRY) {
                // If the reason received from the policy is RETRY, it was probably
                // due to a loss of connection with the service, so we should give the
                // user a chance to retry. So show a dialog to retry.
                //  showDialog(DIALOG_RETRY);
            } else {
                // Otherwise, the user is not licensed to use this app.
                // Your response should always inform the user that the application
                // is not licensed, but your behavior at that point can vary. You might
                // provide the user a limited access version of your app or you can
                // take them to Google Play to purchase the app.
                // showDialog(DIALOG_GOTOMARKET);
            }
        }

        @Override
        public void applicationError(int errorCode) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChecker.onDestroy();
    }




}
