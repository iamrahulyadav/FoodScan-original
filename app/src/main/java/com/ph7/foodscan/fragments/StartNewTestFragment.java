package com.ph7.foodscan.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.consumerphysics.android.sdk.callback.device.ScioDeviceBatteryHandler;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceCalibrateHandler;
import com.consumerphysics.android.sdk.model.ScioBattery;
import com.ph7.foodscan.R;
import com.ph7.foodscan.activities.AppActivity;
import com.ph7.foodscan.activities.main.DashboardActivity;
import com.ph7.foodscan.activities.main.DiscoverDevicesActivity;
import com.ph7.foodscan.activities.main.MyTestsActivity;
import com.ph7.foodscan.application.FoodScanApplication;
import com.ph7.foodscan.callbacks.DeviceConnectHandler;
import com.ph7.foodscan.callbacks.FoodScanHandler;
import com.ph7.foodscan.callbacks.StartNewTestHandler;
import com.ph7.foodscan.device.interfaces.DeviceHandlerInterface;
import com.ph7.foodscan.models.scio.SCIOResultDataModel;
import com.ph7.foodscan.services.FCDBService;
import com.ph7.foodscan.services.FoodScanService;
import com.ph7.foodscan.services.SessionService;
import com.ph7.foodscan.utils.Validation;
import com.ph7.foodscan.views.BatteryView;
import com.ph7.foodscan.views.ShadowedButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The StartNewTestFragment allows the user to start new tests if the device is connected and also
 * ready to use. If the device is not connected, the fragment will prompt the user to connect a
 * device. If the device needs calibration, the fragment will prompt the user to calibrate the
 * device.
 *
 * @author  Craig Tweedy
 * @version 0.7
 * @since   2016-07-05
 */
public class StartNewTestFragment extends Fragment {

    private SessionService sessionService = new SessionService();
    private FoodScanService foodScanService = new FoodScanService();
    public StartNewTestHandler newTestHandler;
    private DeviceHandlerInterface deviceHandler;
    private BatteryView ivBatteryStatus ;
    private ArrayList<SCIOResultDataModel> listNotAnalysedData = null;
    private ArrayList<SCIOResultDataModel> listAnalysedData = null;

    private boolean isConnecting = false;
    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String actionName  = intent.getAction() ;
            if (BluetoothDevice.ACTION_FOUND.equals(actionName)) {
                Log.d("FoodScan","Action : "+actionName);
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("FoodScan_Bluetooth","Device Founded : Device Name => "+device.getName()+" Address => "+device.getAddress());
                if (device.getName() != null && device.getName().startsWith("SCiO")) {
                    final String deviceName = device.getName().substring(4);
                    Log.d("FoodScan_Bluetooth", "deviceId : "+device.getAddress());
                    if(device.getAddress().equals(sessionService.getScioDeviceId()))
                    {
                        isConnecting = true  ; // Change 09-02-17
                        Log.d("FoodScan_Bluetooth","Connecting... with "+device.getName());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FoodScanApplication.getDeviceHandler().setDevice(false ,new com.ph7.foodscan.device.BluetoothDevice(device.getAddress(),deviceName), new DeviceConnectHandler() {
                                    @Override
                                    public void onConnect() {
                                        Log.d("FoodScan_Bluetooth","Connected...");
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                refresh();
                                            }
                                        },1000);}
                                    @Override
                                    public void onFailed(String msg) {
                                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                        ((AppActivity) getActivity()).checkBluetoothPermissions();
                                    }
                                });
                            }
                        });

                    }
                    else {
                        Log.d("FoodScan_Bluetooth","Bluetooth timeout start (10 sec).");
                        // Change 09-02-17
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!isConnecting)
                                {
                                    Log.d("FoodScan_Bluetooth","Saved Device not found Yet !");
                                    Snackbar.make(ivBatteryStatus,sessionService.getScioDeviceName()+
                                            " is not found yet.\nPress Ok ,for searching another device.",Snackbar.LENGTH_INDEFINITE)
                                            .setAction("Ok", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    isConnecting = false ;
                                                    newTestHandler.onNeedsDevice();
                                                }
                                            }).show();
//                                    AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
//                                    builder.setMessage(sessionService.getScioDeviceName()+" is not found yet.\nPress Ok ,for searching another device.");
//                                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            newTestHandler.onNeedsDevice();
//                                            dialog.dismiss();
//                                        }
//                                    });
//
//                                    builder.create() ;
//                                    builder.show() ;
                                }
                                else{
                                    isConnecting = false ;
                                    Log.d("FoodScan_Bluetooth","Already Connected");
                                }
                            }
                        },10000);
                        // Change 09-02-17
                    }

                }
                else {
                    Log.d("MyTag", "device.getName() is null");
                }
            }
            else{
                Log.d("FoodScan_Bluetooth","Action : "+actionName);
            }


            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(actionName)) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isConnecting = false ;
                        refresh();
                    }
                },1000);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_new_test, container, false);
        this.configureView(view);
        return view;
    }

    private void configureView(View view) {
        this.deviceHandler = FoodScanApplication.getDeviceHandler();
        this.configureText(view);
        this.configureButton(view);
        setSavedScanResult(view);
        LinearLayout myTestActivity = (LinearLayout) view.findViewById(R.id.myTestActivity);
        LinearLayout notAnalysedListView = (LinearLayout) myTestActivity.findViewById(R.id.notAnalysedListView);
        LinearLayout analysedListView = (LinearLayout) myTestActivity.findViewById(R.id.analysedListView);

        analysedListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyTestsActivity.class);
                intent.putExtra("index",0);
                startActivity(intent);
            }
        });
        notAnalysedListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyTestsActivity.class);
                intent.putExtra("index",1);
                startActivity(intent);
            }
        });

    }

    private void configureText(View view) {
        TextView textView = (TextView) view.findViewById(R.id.textView);
        RelativeLayout startNewTestContainer =  (RelativeLayout) view.findViewById(R.id.startNewTestContainer);
        startNewTestContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newTestHandler != null) {
                    if (deviceHandler.isDeviceConnected()) {
                        if(!deviceHandler.isCalibrationNeeded()) newTestHandler.onShouldStartNewTest();
                        else  Snackbar.make(view,"Calibration required first!",Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(view,"No device connected!",Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
        ImageView ivScanIcon = (ImageView) view.findViewById(R.id.ivScanIcon) ;
        View separator = view.findViewById(R.id.separator);
        if (deviceHandler.isDeviceConnected()) {
            /***************************
             *
             * This is Styling For Header
             *
             * ***************************/
            if(deviceHandler.isCalibrationNeeded()) {
                textView.setText("Calibration required first");
                textView.setCompoundDrawablePadding(10);
                textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.warning, 0, 0, 0);
                startNewTestContainer.setBackgroundColor(Color.parseColor("#f0b130"));
                ivScanIcon.setImageDrawable(getResources().getDrawable(R.drawable.graphic_radar_yellow));
                separator.setBackgroundColor(Color.parseColor("#cb9528"));
            }
            else {
                textView.setText(deviceHandler.getDeviceName()+" ready to scan.");
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                startNewTestContainer.setBackgroundColor(Color.parseColor("#3daf64"));
                ivScanIcon.setImageDrawable(getResources().getDrawable(R.drawable.graphic_radar_black));
                separator.setBackgroundColor(Color.parseColor("#339454"));
            }
        } else {
            textView.setText("No device connected!");
            textView.setCompoundDrawablePadding(10);
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.warning, 0, 0, 0);
            startNewTestContainer.setBackgroundColor(Color.parseColor("#bfbfb4"));
            separator.setBackgroundColor(Color.parseColor("#a1a198"));
            ivScanIcon.setImageDrawable(getResources().getDrawable(R.drawable.graphic_radar_grey));
        }
    }

    private void configureButton(View view) {

        RelativeLayout button = (RelativeLayout)view.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newTestHandler != null) {
                    if (!deviceHandler.isDeviceConnected()) {
                        TextView tvDeviceName = (TextView)view.findViewById(R.id.tvDeviceName);
                        tvDeviceName.setText("Connecting...");
                        if (sessionService.getScioDeviceId()==null || sessionService.getScioDeviceId().isEmpty()) newTestHandler.onNeedsDevice();
                        else ((DashboardActivity)getActivity()).checkBluetoothPermissions();

                    }
                }
            }
        });

        RelativeLayout buttonRecalibrate = (RelativeLayout)view.findViewById(R.id.buttonRecalibrate);
        FrameLayout buttonCalibrateCont = (FrameLayout)view.findViewById(R.id.buttonCalibrateCont);
        buttonRecalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newTestHandler != null) {
                    if (deviceHandler.isDeviceConnected()) calibrate(view);
                }
            }
        });

        TextView tvDeviceName =(TextView) button.findViewById(R.id.tvDeviceName);
        ImageView ivBluetoothStatus =(ImageView) button.findViewById(R.id.ivBluetoothStatus);
        ivBatteryStatus  =(BatteryView) button.findViewById(R.id.ivBatteryStatus);

        if(deviceHandler.isDeviceConnected())
        {
            ivBatteryStatus.setVisibility(View.VISIBLE);
            buttonCalibrateCont.setVisibility(View.VISIBLE);
            TextView tvCalibrationStatus =  (TextView) buttonRecalibrate.findViewById(R.id.tvCalibrationStatus);
            ImageView ivCalibrateGraphic =  (ImageView) buttonRecalibrate.findViewById(R.id.ivCalibrateGraphic);
            ShadowedButton calibrateButtonBG = (ShadowedButton) buttonCalibrateCont.findViewById(R.id.calibrateButtonBG);

            deviceHandler.readBattery(new ScioDeviceBatteryHandler() {
                @Override
                public void onSuccess(final ScioBattery scioBattery) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int batteryPercent = scioBattery.getChargePercentage(); // Battery Percentage
                            showBatteryStatus(batteryPercent);
                        }});
                }

                @Override
                public void onError() {}

                @Override
                public void onTimeout() {}
            });

            if(deviceHandler.isCalibrationNeeded()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) calibrateButtonBG.setTextAppearance(R.style.OrangeButtonStyle);
                else calibrateButtonBG.setTextAppearance(getActivity(),R.style.OrangeButtonStyle);
                tvCalibrationStatus.setText("Calibrate");
                ivCalibrateGraphic.setBackground(getResources().getDrawable(R.drawable.graphic_refresh_yellow));
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) calibrateButtonBG.setTextAppearance(R.style.PurpleButtonStyle);
                else calibrateButtonBG.setTextAppearance(getActivity(),R.style.PurpleButtonStyle);
                tvCalibrationStatus.setText("Recalibrate");
                ivCalibrateGraphic.setBackground(getResources().getDrawable(R.drawable.graphic_refresh_purple));
            }

            tvDeviceName.setText(deviceHandler.getDeviceName());

            if(deviceHandler.isCalibrationNeeded()) ivBluetoothStatus.setBackground(getResources().getDrawable(R.drawable.icon_bluetooth_status_yellow));
            else ivBluetoothStatus.setBackground(getResources().getDrawable(R.drawable.icon_bluetooth_status_green));

        }
        else
        {
            ivBatteryStatus.setVisibility(View.GONE);
            buttonCalibrateCont.setVisibility(View.GONE);
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

        // Network Connectivity
        FrameLayout networkConnectCont = (FrameLayout) view.findViewById(R.id.networkConnectCont) ;
        RelativeLayout buttonNetworkConnect = (RelativeLayout) networkConnectCont.findViewById(R.id.buttonNetworkConnect) ;
        buttonNetworkConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reconnectToFoodScanDB();
            }
        });
        ImageView ivNWConnectivityStatus =  (ImageView)networkConnectCont.findViewById(R.id.ivNWConnectivityStatus) ;
        TextView tvNWConnectivityStatus =  (TextView)networkConnectCont.findViewById(R.id.tvNWConnectivityStatus) ;

        /*
        *******************************************************************
        * UPDATE BACKEND DATABASE CONNECTIVITY STATUS ON REFRESH GUI
        * *****************************************************************
        * ***/

        if(!sessionService.isExpireAccessToken())
        {
            networkConnectCont.setVisibility(View.VISIBLE);
            tvNWConnectivityStatus.setText("Connected");
            ivNWConnectivityStatus.setBackground(getResources().getDrawable(R.drawable.icon_bluetooth_status_green));
        }
        else{
            networkConnectCont.setVisibility(View.VISIBLE);
            tvNWConnectivityStatus.setText("Reconnect");
            ivNWConnectivityStatus.setBackground(getResources().getDrawable(R.drawable.icon_bluetooth_status_red));
            reconnectToFoodScanDB();
        }

        /*********************/

    }


    private void setSavedScanResult(View view) {

        FCDBService fcdbService = new FCDBService(getActivity());
        Cursor testsCursor =  fcdbService.getAllTests() ;
        int analysedCount =  0 ;
        int notAnalysedCount = 0 ;
        listAnalysedData = new ArrayList<>() ;
        listNotAnalysedData= new ArrayList<>() ;
        try
        {
            testsCursor.moveToFirst();
            while (!testsCursor.isAfterLast()) {
                int testStatus = Integer.parseInt(testsCursor.getString(9)) ;
                if(testStatus==0){
                    ++notAnalysedCount ;
                }
                else{
                    ++analysedCount;
                }
                testsCursor.moveToNext();
            }

        }catch(Exception x)
        {
            x.printStackTrace();
        }
        finally {
            if(testsCursor!=null) testsCursor.close();
            if(fcdbService!=null) fcdbService.close();
        }


        TextView tvNotAnalysedTestCount  =  (TextView)  view.findViewById(R.id.tvNotAnalysedTestCount);
        TextView tvAnalysedTestCount  =  (TextView)  view.findViewById(R.id.tvAnalysedTestCount);

        tvNotAnalysedTestCount.setText(notAnalysedCount+"");

        tvAnalysedTestCount.setText(analysedCount+"");
    }


    private void reconnectToFoodScanDB() {
        if(!Validation.isOnline(getActivity())) {
            showSettingsPopup();
        }
        else
        {
            if(sessionService.getUsername()!=null && sessionService.getPassword()!=null)
                regenerateTokenRequest();
        }
    }


    private void regenerateTokenRequest() {
        Log.d("Token","Regenerate token request");
        Map<String, String> params = new HashMap<>();
        params.put("email",sessionService.getUsername());
        params.put("password", sessionService.getPassword());
        foodScanService.login(params, new FoodScanHandler() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    Log.d("Token","Regenerated successfully!");
                    sessionService.setUserToken(jsonObject.getString("token"));
                    refresh();
                } catch (JSONException e) { }
            }
            @Override
            public void onError() {
                reconnectToFoodScanDB();
            }
        });
    }
    private void showSettingsPopup() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("No Internet connection.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WifiManager wifiManager = (WifiManager)getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
    }

    // Calibrate Functioning

    private void calibrate(final View view) {
        final ProgressBar progressBar =(ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        FoodScanApplication.getDeviceHandler().calibrate(new ScioDeviceCalibrateHandler() {
            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(view,"Device calibrated successfully!",Snackbar.LENGTH_SHORT).show();
                        //  Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                        Log.d("Calibrate","Device Calibrated Successfully!");
                        refresh();
                    }
                });
            }

            @Override
            public void onError() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Log.d("Calibrate","Something wrong!");
                        Snackbar.make(view,"Something wrong!",Snackbar.LENGTH_SHORT).show();
                        //Toast.makeText(getActivity(), "Something wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onTimeout() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(view,"Calibration request timeout!",Snackbar.LENGTH_SHORT).show();
                        Log.d("Calibrate","Calibration request timeout!");
                    }
                });
            }

        });
    }


    private void showBatteryStatus(int batteryPercent) {
        ivBatteryStatus.setBatteryStatus(batteryPercent);
        Log.d("FoodScan","Device Status : "+deviceHandler.isDeviceConnected().toString()+ " Battery Status : "+batteryPercent) ;
    }

    public void refresh() {

        final View view =  this.getView() ;
        if(view!=null) this.configureView(view);

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(networkReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        isConnecting = false ;// Change 09-02-17
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        getActivity().registerReceiver(networkReceiver, filter);
    }


}
