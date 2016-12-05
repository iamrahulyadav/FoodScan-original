package com.ph7.foodscan.activities.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.consumerphysics.android.scioconnection.services.SCiOBLeService;
import com.ph7.foodscan.R;
import com.ph7.foodscan.activities.AppActivity;
import com.ph7.foodscan.application.FoodScanApplication;
import com.ph7.foodscan.callbacks.DeviceConnectHandler;
import com.ph7.foodscan.device.BluetoothDevice;
import com.ph7.foodscan.services.SessionService;
import com.ph7.foodscan.views.StatusView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The DiscoverDevicesActivity will scan locally for SCIO devices and present them to the user.
 * The user can then select these devices and connect to them.
 *
 * @author  Craig Tweedy
 * @version 0.7
 * @since   2016-07-04
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class DiscoverDevicesActivity extends AppActivity {
    public static final int PICK_DEVICE_REQUEST = 1;

    private static final String TAG = DiscoverDevicesActivity.class.getSimpleName();

    private Map<String, String> devices;
    private DevicesAdapter devicesAdapter;

    private BluetoothAdapter bluetoothAdapter;

    private int REQUEST_ENABLE_BT = 1;
    private int PERMISSION_REQUEST_BLUETOOTH = 2;
    private Handler handler;

    private View mLayout;
    private BluetoothLeScanner bluetoothLeScanner;
    private ArrayList<ScanFilter> filters;
    private ScanSettings settings;

    public class DevicesAdapter extends ArrayAdapter<BluetoothDevice> {
        public DevicesAdapter(final Context context, final List<BluetoothDevice> bluetoothDevices) {
            super(context, 0, bluetoothDevices);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final BluetoothDevice dev = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_item, parent, false);
            }

            final TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            final ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            tvName.setText(dev.getName());
            return convertView;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_devices);

        mLayout = findViewById(R.id.main_layout);

        this.checkIfSystemHasBluetooth();

        //Stop previous service
        stopService(new Intent(this, SCiOBLeService.class));



    }


    private void checkIfSystemHasBluetooth() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private boolean isStatusOpen = false ;
    private void setupDeviceList() {

        this.devices = new LinkedHashMap<>();

        final List<BluetoothDevice> arrayOfBluetoothDevices = new ArrayList<>();
        this.devicesAdapter = new DevicesAdapter(this, arrayOfBluetoothDevices);

        final ListView lv = (ListView) findViewById(R.id.listView);

        lv.setAdapter(this.devicesAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice dev = devicesAdapter.getItem(position);
                //m Toast.makeText(getApplicationContext(), "Connecting to " + dev.getName(), Toast.LENGTH_SHORT).show();
                progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
                connectedToDevice(dev);

            }
        });
    }
     ProgressBar progressBar ;
    private void connectedToDevice(final BluetoothDevice dev) {
        progressBar.setVisibility(View.VISIBLE);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
                FoodScanApplication.getDeviceHandler().setDevice(false ,dev, new DeviceConnectHandler() {
                    @Override
                    public void onConnect() {
                        Log.d("BluetoothResponse",dev.getName()+" :=>  "+dev.getAddress() +" Connected") ;
                        try {
                            setResult(RESULT_OK);
                            /// if(!isStatusOpen) {
                            //isStatusOpen = true ;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(!isFinishing()){
                                        final StatusView statusView = new StatusView(DiscoverDevicesActivity.this);
                                        statusView.setStatusCode(1);
                                        statusView.setStatusMessage(dev.getName() + " connected");
                                        new SessionService().setScioDevice(dev.getAddress(),dev.getName());
                                        statusView.show();
                                        statusView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                statusView.hide();
                                                finish();
                                            }
                                        });
                                        Log.d("BluetoothResponse","Connected") ;
                                        progressBar.setVisibility(View.GONE);
                                    }

                                }
                            });

                        }
                        catch (Exception ex)
                        {
                            Log.d("BluetoothResponse",ex.getMessage()+ "|||| "+ex.getCause().toString()) ;
                           // connectedToDevice(dev);
                        }
                        //  }
                        //Toast.makeText(DiscoverDevicesActivity.this, dev.getName()+" connected", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(String msg) {
                        Log.d("BluetoothResponse",msg) ;
                        try {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(DiscoverDevicesActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception ex) {
                           // Toast.makeText(DiscoverDevicesActivity.this, "OnFailed!", Toast.LENGTH_SHORT).show();
                            ex.printStackTrace();
                            connectedToDevice(dev);
                        }
                    }
                });
         //   }
      //  });
    }

    @Override
    public void checkBluetoothPermissions() {
        // Check if the Bluetooth permission has been granted
        if (    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
            // Permission is already available, start bluetooth
            startBluetooth();
        } else {
            // Permission is missing and must be requested.
            requestBluetoothPermission();
        }
    }

    private void startBluetooth() {

        this.handler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();

        if (this.bluetoothAdapter == null || !this.bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                Log.d("BluetoothConnect_Build",">=21");
                this.bluetoothLeScanner = this.bluetoothAdapter.getBluetoothLeScanner();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                            .setReportDelay(0)
                            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                            .build();
                } else {
                    this.settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                }
                this.filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
        }
    }

    private void requestBluetoothPermission() {
        if (
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_ADMIN) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                ) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(mLayout, "Permissions are required to connect to the BLUETOOTH_DEVICE.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(DiscoverDevicesActivity.this,
                            new String[]{
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                            },
                            PERMISSION_REQUEST_BLUETOOTH);
                }
            }).show();

        } else {
            Snackbar.make(mLayout,
                    "Permission is not available. Requesting permissions.",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                    },
                    PERMISSION_REQUEST_BLUETOOTH);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            // Request for bluetooth permission.
            boolean allPermissionsGranted = true;
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                }
            }
            if (allPermissionsGranted) {
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(mLayout, "Permissions were granted. Starting scan",
                        Snackbar.LENGTH_SHORT)
                        .show();
                startBluetooth();
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, "Permissions were denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.bluetoothAdapter != null && this.bluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {

        final ProgressBar progressBarBTSearch = (ProgressBar) findViewById(R.id.progressBarBTSearch);
        if (Build.VERSION.SDK_INT >= 21) {
            Log.d("BluetoothConnect_Build",">=21");
            if (enable) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(devicesAdapter.getCount()<=0)
                        {
                            //Toast.makeText(DiscoverDevicesActivity.this, "No device found!", Toast.LENGTH_SHORT).show();
                            Snackbar.make(progressBarBTSearch,"No device found!",Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    setupDeviceList();
                                    checkBluetoothPermissions();
                                }
                            }).show();
                            progressBarBTSearch.setVisibility(View.GONE);
                        }

                    }
                },90000);
                progressBarBTSearch.setVisibility(View.VISIBLE);
                this.bluetoothLeScanner.startScan(filters, settings, scanCallback);
            } else {
                progressBarBTSearch.setVisibility(View.GONE);
                this.bluetoothLeScanner.stopScan(scanCallback);
            }
        } else {
            if (enable) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(devicesAdapter.getCount()<=0)
                        {
                            //Toast.makeText(DiscoverDevicesActivity.this, "No device found!", Toast.LENGTH_SHORT).show();
                            Snackbar.make(progressBarBTSearch,"No device found!",Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    setupDeviceList();
                                    checkBluetoothPermissions();
                                }
                            }).show();
                            progressBarBTSearch.setVisibility(View.GONE);
                        }

                    }
                },90000);
                progressBarBTSearch.setVisibility(View.VISIBLE);
                this.bluetoothAdapter.startLeScan(oldScanCallback);
            } else {
                progressBarBTSearch.setVisibility(View.GONE);
                this.bluetoothAdapter.stopLeScan(oldScanCallback);
            }
        }
    }

    private void addDevice(final String name, final String address) {
        ProgressBar progressBarBTSearch = (ProgressBar) findViewById(R.id.progressBarBTSearch);
        progressBarBTSearch.setVisibility(View.GONE);
        this.devices.put(address, name);

        final BluetoothDevice dev = new BluetoothDevice(address, name);
        this.devicesAdapter.add(dev);
    }

    private void logDevice(android.bluetooth.BluetoothDevice bluetoothDevice) {
        String deviceName = bluetoothDevice.getName();

        // Show only SCiO devices
        if (deviceName != null && deviceName.startsWith("SCiO")) {
            deviceName = deviceName.substring(4);
            String scio = this.devices.get(bluetoothDevice.getAddress());

            if (scio == null) {
                Log.d(TAG, "found scio BLUETOOTH_DEVICE BLUETOOTH_DEVICE: " + deviceName + ", " + bluetoothDevice.getAddress());
                addDevice(deviceName, bluetoothDevice.getAddress());
            }
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            logDevice(result.getDevice());
            Log.d("BluetoothConnect_Build",">=21");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                logDevice(sr.getDevice());
                Log.d("BluetoothConnect_Build",">=21");
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
            Log.d("BluetoothConnect_Build",">=21");
        }
    };

    private BluetoothAdapter.LeScanCallback oldScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(android.bluetooth.BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            logDevice(bluetoothDevice);
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        this.setupDeviceList();
        this.checkBluetoothPermissions();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false ;
    }
}
