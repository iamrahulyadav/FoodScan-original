package com.ph7.foodscan.activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.consumerphysics.android.sdk.callback.device.ScioDeviceBatteryHandler;
import com.consumerphysics.android.sdk.model.ScioBattery;
import com.consumerphysics.android.sdk.sciosdk.ScioCloud;
import com.ph7.foodscan.R;
import com.ph7.foodscan.activities.main.DiscoverDevicesActivity;
import com.ph7.foodscan.activities.main.Help;
import com.ph7.foodscan.activities.main.Settings;
import com.ph7.foodscan.activities.onboarding.CPLoginActivity;
import com.ph7.foodscan.activities.onboarding.FoodScanLoginActivity;
import com.ph7.foodscan.activities.onboarding.StartUpActivity;
import com.ph7.foodscan.application.FoodScanApplication;
import com.ph7.foodscan.callbacks.DeviceConnectHandler;
import com.ph7.foodscan.callbacks.ExceptionHandler;
import com.ph7.foodscan.device.interfaces.DeviceHandlerInterface;
import com.ph7.foodscan.services.SessionService;
import com.ph7.foodscan.views.BatteryView;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by craigtweedy on 26/05/2016.
 */
public class AppActivity extends AppCompatActivity  {

    public static int SKU_PRODUCT_LIST_RESPONSE =  10001 ;
    private static final String TAG = "FoodScan";
    public static String base64EncodedPublicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgru98m52/mDdtOrSgrwTPafEqqGbMeEh/ECO8I9QsxQg0yk/33FDDg6P1QB1yc3I4Yn71VCR9CEXhh9jc41gkfKzXZ7o5aGwHIi7y1Bw4FvexIch4audVmsZ5iju7CnYeDGW3rfiZbW17Nc3CXC8+vuSxTwqRPOku5b4rtbxZU7QvWKXWKUqfuXIsfg2hzu3DzC2dBnxv0yhHE4D61fEJu2dM63tKkcZNVzK5my2Qu2giDcTpC97iqp+pix8vAKP1dAcD+FeR41PRiac3PIaNcO81sKpt61PkJamreYz0vjjUkKGW4ABKloGXJPiibXsYqcCMJvGs6sZK4G3Z/XwBQIDAQAB";
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy 'at' HH:mm", Locale.US);
    private SessionService sessionService = new SessionService();
    protected ScioCloud scioCloud;
    private DeviceHandlerInterface deviceHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(AppActivity.this));
        this.scioCloud = new ScioCloud(this);

        //inAppBillingModule();

    }


    protected void setActionBarHidden(boolean hidden) {
        if (hidden) {
            getSupportActionBar().hide();
        } else {
            getSupportActionBar().show();
        }

    }
    protected void setActionBarOverlayZero() {
        getSupportActionBar().setElevation(0f);
    }
    protected void setActionBarOptionHidden(boolean hidden) {
    }protected void setActionBarTitleHidden(boolean hidden) {
        getSupportActionBar().setDisplayShowTitleEnabled(hidden);
        if(hidden) setTitle("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (!scioCloud.hasAccessToken() && !this.sessionService.isUserLoggedInToFoodScan())
            inflater.inflate(R.menu.menu_not_loggedin, menu);
        else
            inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean isLogin() {
        Log.d(TAG, "doLogin");
        final AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity.this) ;
        builder.setTitle("Login Required");
        if (!scioCloud.hasAccessToken()) {
            builder.setMessage("Go to CP Login");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Starting the full process, so connect a scanner first
                    startActivity(new Intent(AppActivity.this, CPLoginActivity.class));
                    dialogInterface.dismiss();
                }
            });
            builder.create() ;
            builder.show();
            return false ;
        }
        else {
            Log.d(TAG, "Already have token");
            builder.setMessage("Go to FoodScan Login");
            if (!this.sessionService.isUserLoggedInToFoodScan()) {
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Starting the full process, so connect a scanner first
                        startActivity(new Intent(AppActivity.this, FoodScanLoginActivity.class));
                        dialogInterface.dismiss();
                    }
                });
                builder.create();
                builder.show();
                return false ;
            }
        }
        return true ;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.settings:
                startActivity(new Intent(this, Settings.class));
                break;
            case R.id.action_logout:
                this.scioCloud.deleteAccessToken();
                this.sessionService.logout();
                startActivity(new Intent(this, StartUpActivity.class));
                finish();
                break;
            case R.id.action_help:
                startActivity(new Intent(this, Help.class));
                break;
            case R.id.action_version :
                try {
                    String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                    Toast.makeText(this, "Version: "+versionName, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {}
                break ;
            case android.R.id.home:
                finish();
                break;
            case R.id.action_login:
                startActivity(new Intent(AppActivity.this,CPLoginActivity.class)) ;
                finish();
                break;

            case R.id.action_privacy_policy :
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ph-7.co.uk/privacy/"));
                startActivity(browserIntent);
                break;

            default:
                break;
        }

        return true;
    }


    public void configureView(){
        final AppActivity _this = this;
        this.deviceHandler = FoodScanApplication.getDeviceHandler();
        RelativeLayout button = (RelativeLayout)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!deviceHandler.isDeviceConnected())
                {
                    TextView tvDeviceName = (TextView)view.findViewById(R.id.tvDeviceName);
                    tvDeviceName.setText("Connecting...");
                    if(sessionService.getScioDeviceId()!=null && !sessionService.getScioDeviceId().isEmpty()) checkBluetoothPermissions();
                    else startActivityForResult(new Intent(_this, DiscoverDevicesActivity.class), DiscoverDevicesActivity.PICK_DEVICE_REQUEST);
                }
            }
        });
        TextView tvDeviceName =(TextView) button.findViewById(R.id.tvDeviceName);
        ImageView ivBluetoothStatus =(ImageView) button.findViewById(R.id.ivBluetoothStatus);
        final BatteryView ivBatteryStatus  =(BatteryView) button.findViewById(R.id.ivBatteryStatus);
        if(deviceHandler.isDeviceConnected())
        {
            ivBatteryStatus.setVisibility(View.VISIBLE);

            deviceHandler.readBattery(new ScioDeviceBatteryHandler() {

                @Override
                public void onSuccess(final ScioBattery scioBattery) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int batteryPercent = scioBattery.getChargePercentage(); // Battery Percentage
                            scioBattery.isCharging(); //  is charging
                            ivBatteryStatus.setBatteryStatus(batteryPercent);
                        }});
                }

                @Override
                public void onError() {

                }

                @Override
                public void onTimeout() {

                }
            });


            tvDeviceName.setText(deviceHandler.getDeviceName());

            if(deviceHandler.isCalibrationNeeded())
                ivBluetoothStatus.setBackground(getResources().getDrawable(R.drawable.icon_bluetooth_status_yellow));
            else
                ivBluetoothStatus.setBackground(getResources().getDrawable(R.drawable.icon_bluetooth_status_green));


        }
        else
        {
            ivBatteryStatus.setVisibility(View.GONE);
            // Check Bluetooth is on or Off

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {

            } else {
                if (!mBluetoothAdapter.isEnabled()) {
                    tvDeviceName.setText("Bluetooth is switched off");
                    ivBluetoothStatus.setBackground(getResources().getDrawable(R.drawable.icon_bluetooth_status_red));
                }
                else
                {
                    tvDeviceName.setText("Bluetooth is switched off");
                    ivBluetoothStatus.setBackground(getResources().getDrawable(R.drawable.icon_bluetooth_status_red));
                }
            }

        }
    }

    public BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =  intent.getAction() !=null ? intent.getAction():"" ;
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null && device.getName().startsWith("SCiO")) {
                    final String deviceName = device.getName().substring(4);
                    Log.d("AppActivity : deviceId",device.getAddress());
                    if(device.getAddress().equals(sessionService.getScioDeviceId()))
                    {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FoodScanApplication.getDeviceHandler().setDevice(false,new com.ph7.foodscan.device.BluetoothDevice(device.getAddress(),deviceName), new DeviceConnectHandler() {
                                    @Override
                                    public void onConnect() { configureView();}
                                    @Override
                                    public void onFailed(String msg) {

                                        Toast.makeText(AppActivity.this, msg, Toast.LENGTH_SHORT).show();
                                        scanningDevices();
                                    }
                                });
                            }
                        });

                    }

                }
                else { Log.d("MyTag", "device.getName() is null");}
            }
            if(intent.getExtras()!=null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() { configureView();}
                },1000);
            }
        }
    };



    // Setup Sound Pool
    protected int soundId ;
    protected SoundPool soundPool = null ;
    public void setupSoundPool() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder builder =  new SoundPool.Builder();
            soundPool = builder.build() ;
        }
        else
        {
            soundPool = new SoundPool(1,1,1);
        }
        soundId = soundPool.load(getApplicationContext(),R.raw.tos_tricorder_scan,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                return;
            }
            else if(resultCode == Activity.RESULT_OK)
            {
                scanningDevices();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void checkBluetoothPermissions() {
        // Check if the Bluetooth permission has been granted
        if (    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
            // Permission is already available, start bluetooth
            scanningDevices();
        } else {
            // Permission is missing and must be requested.
            requestBluetoothPermission();
        }
    }

    private void requestBluetoothPermission() {
        ActivityCompat.requestPermissions(AppActivity.this,
                new String[]{
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                },
                1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 1000) {
            // Request for bluetooth permission.
            boolean allPermissionsGranted = true;
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                }
            }
            if (allPermissionsGranted) {
                // Permission has been granted. Start camera preview Activity.

                scanningDevices();
            } else {
                // Permission request was denied.
                Toast.makeText(this, "Permissions were denied.",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private int REQUEST_ENABLE_BT = 1000;
    private void scanningDevices() {
        BluetoothAdapter  bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            //Bluetooth is OFF, so turn it on
            if (!bluetoothAdapter.isEnabled()) {
                //bluetoothAdapter.enable();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else {
                bluetoothAdapter.startDiscovery();
            }
        }

    }



}


