package com.ph7.foodscan.device;

import android.util.Log;
import android.widget.Toast;

import com.consumerphysics.android.sdk.callback.device.ScioDeviceBatteryHandler;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceCalibrateHandler;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceCallback;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceConnectHandler;
import com.consumerphysics.android.sdk.model.ScioReading;
import com.consumerphysics.android.sdk.sciosdk.ScioDevice;
import com.ph7.foodscan.application.FoodScanApplication;
import com.ph7.foodscan.callbacks.DeviceConnectHandler;
import com.ph7.foodscan.callbacks.ScioDeviceScanHandler;
import com.ph7.foodscan.device.interfaces.DeviceHandlerInterface;

/**
 * Created by craigtweedy on 25/01/2016.
 */
public class DeviceHandler implements DeviceHandlerInterface {

    private static final String TAG = DeviceHandler.class.getSimpleName();

    private BluetoothDevice bluetoothDevice = null;
    private ScioDevice scioDevice = null;

    public void disconnectFromDevice(ScioDeviceCallback onDisconnect) {
        this.scioDevice.setScioDisconnectCallback(onDisconnect);
        this.scioDevice.disconnect();
    }

    public void clearScioDevice() {
        this.scioDevice = null;
    }

    public boolean hasDevice() {
        return this.bluetoothDevice != null;
    }

    public void setDevice(BluetoothDevice bluetoothDevice, final DeviceConnectHandler handler) {
        this.bluetoothDevice = bluetoothDevice;
        this.connectToDevice(handler);
    }
    public void setDevice(boolean isAutoConnect ,BluetoothDevice bluetoothDevice, final DeviceConnectHandler handler) {
        this.bluetoothDevice = bluetoothDevice;
        this.connectToDevice(isAutoConnect,handler);
    }


    @Override
    public String getDeviceAddress() {
        return this.scioDevice.getAddress();
    }

    @Override
    public String getDeviceName() {
        return this.bluetoothDevice.getName();
    }


    public Boolean isDeviceConnected() {
        return this.scioDevice != null && this.scioDevice.isConnected();
    }

    public void connectToDevice(final DeviceConnectHandler handler) {
        this.scioDevice = new ScioDevice(FoodScanApplication.getAppContext(), this.bluetoothDevice.getAddress());
        this.scioDevice.connect(new ScioDeviceConnectHandler() {
            @Override
            public void onConnected() {
                handler.onConnect();
            }

            @Override
            public void onConnectFailed() {
                Log.d(TAG, "Connection failed");
                handler.onFailed("Connection failed!");
            }

            @Override
            public void onTimeout() {
                Log.d(TAG, "Connection timed out");
                handler.onFailed("Connection timed out");
            }
        });
    }

    public void connectToDevice(boolean isAutoConnect ,final DeviceConnectHandler handler) {
        this.scioDevice = new ScioDevice(FoodScanApplication.getAppContext(), this.bluetoothDevice.getAddress());
        this.scioDevice.connect(isAutoConnect,new ScioDeviceConnectHandler() {
            @Override
            public void onConnected() {
                handler.onConnect();
            }

            @Override
            public void onConnectFailed() {
                Log.d(TAG, "Connection failed");
                handler.onFailed("Connection failed!");
                //
            }

            @Override
            public void onTimeout() {
                Log.d(TAG, "Connection timed out");
                handler.onFailed("Connection timed out");
            }
        });
    }


    public void scan(final ScioDeviceScanHandler handler) {
        this.scioDevice.scan(new com.consumerphysics.android.sdk.callback.device.ScioDeviceScanHandler() {
            @Override
            public void onSuccess(ScioReading scioReading) {
                handler.onSuccess(scioReading);
            }

            @Override
            public void onNeedCalibrate() {
                handler.onNeedCalibrate();
            }

            @Override
            public void onError() {
                handler.onError();
            }

            @Override
            public void onTimeout() {
                handler.onTimeout();
            }
        });
    }

    public boolean isCalibrationNeeded()
    {
        if (this.scioDevice == null || !this.scioDevice.isConnected()) {
            Log.d(TAG, "Can not calibrate. SCiO is not connected");
            Toast.makeText(FoodScanApplication.getAppContext(), "Can not calibrate. SCiO is not connected", Toast.LENGTH_SHORT).show();
            return false;
        }

        return this.scioDevice.isCalibrationNeeded() ;
    }

    public void calibrate(ScioDeviceCalibrateHandler handler) {
        if (this.scioDevice == null || !this.scioDevice.isConnected()) {
            Log.d(TAG, "Can not calibrate. SCiO is not connected");
            Toast.makeText(FoodScanApplication.getAppContext(), "Can not calibrate. SCiO is not connected", Toast.LENGTH_SHORT).show();
            handler.onError();
            return;
        }
        this.scioDevice.calibrate(handler);
    }

    @Override
    public void readBattery(ScioDeviceBatteryHandler handler) {
        this.scioDevice.readBattery(handler);
    }
}
