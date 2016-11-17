package com.ph7.analyserforscio.activities.main;

import android.content.Intent;
import android.os.Bundle;

import com.ph7.analyserforscio.R;
import com.ph7.analyserforscio.activities.AppActivity;
import com.ph7.analyserforscio.activities.purchase.PurchaseProductScreen;
import com.ph7.analyserforscio.activities.scan.NewTestActivity;
import com.ph7.analyserforscio.callbacks.StartNewTestHandler;
import com.ph7.analyserforscio.fragments.StartNewTestFragment;

/**
 * The DashboardActivity is the users "home screen" and allows the user to start new tests,
 * connect to devices, calibrate devices, see a list of previous tests, and
 * see tests which are not yet analysed.
 *
 * @author  Craig Tweedy
 * @version 0.7
 * @since   2016-07-04
 */
public class DashboardActivity extends AppActivity {

    StartNewTestFragment newTestFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        setActionBarOverlayZero();
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.graphic_foodscan_logo_small);
        final DashboardActivity _this = this;

        newTestFragment = (StartNewTestFragment) getFragmentManager().findFragmentById(R.id.newTestFragment);
        newTestFragment.newTestHandler = new StartNewTestHandler() {
            @Override
            public void onShouldStartNewTest() {
                startActivity(new Intent(_this, NewTestActivity.class));
            }

            @Override
            public void onNeedsDevice() {
                startActivityForResult(new Intent(_this, DiscoverDevicesActivity.class), DiscoverDevicesActivity.PICK_DEVICE_REQUEST);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        newTestFragment.refresh();
    }

    private static int SKU_PRODUCT_LIST_REQUEST =  10000 ;

    public void onBuySKUProduct()
    {
        Intent intent  = new Intent(DashboardActivity.this,PurchaseProductScreen.class) ;
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DiscoverDevicesActivity.PICK_DEVICE_REQUEST) {
            if (resultCode == RESULT_OK) {
                newTestFragment.refresh();
            }
        }
        if(requestCode==SKU_PRODUCT_LIST_REQUEST)
        {
            if(resultCode== SKU_PRODUCT_LIST_RESPONSE)
            {

            }
        }
    }


}
