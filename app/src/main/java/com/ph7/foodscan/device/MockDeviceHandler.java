package com.ph7.foodscan.device;

import com.consumerphysics.android.sdk.callback.device.ScioDeviceBatteryHandler;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceCalibrateHandler;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceCallback;
import com.ph7.foodscan.callbacks.DeviceConnectHandler;
import com.ph7.foodscan.callbacks.ScioDeviceScanHandler;
import com.ph7.foodscan.device.interfaces.DeviceHandlerInterface;

/**
 * Created by craigtweedy on 19/02/2016.
 */
public class MockDeviceHandler implements DeviceHandlerInterface {

    private boolean hasDevice = false;
    private boolean isConnected = false;

    @Override
    public void disconnectFromDevice(ScioDeviceCallback onDisconnect) {
        this.hasDevice = false;
        this.isConnected = false;
        onDisconnect.execute();
    }

    @Override
    public void clearScioDevice() {
        this.isConnected = false;
    }

    @Override
    public boolean hasDevice() {
        return this.hasDevice;
    }

    @Override
    public void setDevice(BluetoothDevice bluetoothDevice, DeviceConnectHandler handler) {
        this.hasDevice = true;
        this.connectToDevice(handler);
    }

    @Override
    public void setDevice(boolean isAutoConnect, BluetoothDevice bluetoothDevice, DeviceConnectHandler handler) {
        this.hasDevice = true;
        this.connectToDevice(handler);
    }

    @Override
    public String getDeviceAddress() {
        return "0000-0000-0000-0000";
    }

    @Override
    public String getDeviceName() {
        return null;
    }

    @Override
    public Boolean isDeviceConnected() {
        return this.isConnected;
    }

    @Override
    public boolean isCalibrationNeeded() {
        return false;
    }

    @Override
    public void connectToDevice(DeviceConnectHandler handler) {
        this.hasDevice = true;
        this.isConnected = true;
        handler.onConnect();
    }

    @Override
    public void scan(ScioDeviceScanHandler handler) {
        handler.onSuccess(null);
    }

    @Override
    public void calibrate(ScioDeviceCalibrateHandler handler) {
        handler.onSuccess();
    }

    @Override
    public void readBattery(ScioDeviceBatteryHandler handler) {
        handler.onError();
    }
}
