package com.ph7.foodscan.activities.onboarding;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ph7.foodscan.R;
import com.ph7.foodscan.activities.AppActivity;
import com.ph7.foodscan.activities.main.DashboardActivity;
import com.ph7.foodscan.activities.main.DiscoverDevicesActivity;
import com.ph7.foodscan.utils.Validation;

/**
 * The ConnectScannerActivity is responsible for allowing the user to connect to a scanner
 * immediately or skip the process and log in
 *
 * @author  Craig Tweedy
 * @version 0.7
 * @since   2016-07-04
 */
public class ConnectScannerActivity extends AppActivity {

    int countFoundDevices = 0 ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_scanner);
        setActionBarHidden(true);

        //throw new NullPointerException();

    }
    public void onConnectScannerClicked(View view) {
       // if(!FoodScanApplication.getDeviceHandler().isDeviceConnected())
            startActivityForResult(new Intent(this, DiscoverDevicesActivity.class), DiscoverDevicesActivity.PICK_DEVICE_REQUEST);
      //  else
      //      startActivity(new Intent(this, DashboardActivity.class));
    }

    public void onSkipClicked(View view) {
        Log.d("ConnectScanner", "Skip Clicked");
        if(Validation.isNetworkConnected(this))  {
            startActivity(new Intent(this, CPLoginActivity.class));
        }
        else Toast.makeText(ConnectScannerActivity.this, "No network connectivity!!", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DiscoverDevicesActivity.PICK_DEVICE_REQUEST) {
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            }
        }
    }


    private final BroadcastReceiver mReceiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =  intent.getAction() ;
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null && device.getName().startsWith("SCiO")) {
                    ++countFoundDevices ;
                    TextView tvCountFoundDevices = (TextView)  findViewById(R.id.tvCountFoundDevices);
                    tvCountFoundDevices.setText(countFoundDevices+" scanner found nearby!");
                    tvCountFoundDevices.setVisibility(View.VISIBLE);
                }
                else {
                    Log.d("MyTag", "device.getName() is null");
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        countFoundDevices=0 ;
        TextView tvCountFoundDevices = (TextView)  findViewById(R.id.tvCountFoundDevices);
        tvCountFoundDevices.setVisibility(View.GONE);
        checkBluetoothPermissions();
        IntentFilter intentFilter  =  new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

}
