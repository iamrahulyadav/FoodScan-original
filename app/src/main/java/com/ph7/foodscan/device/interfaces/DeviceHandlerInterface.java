package com.ph7.foodscan.device.interfaces;

import com.consumerphysics.android.sdk.callback.device.ScioDeviceBatteryHandler;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceCalibrateHandler;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceCallback;
import com.consumerphysics.android.sdk.sciosdk.ScioDevice;
import com.ph7.foodscan.callbacks.DeviceConnectHandler;
import com.ph7.foodscan.callbacks.ScioDeviceScanHandler;
import com.ph7.foodscan.device.BluetoothDevice;

/**
 * Created by craigtweedy on 19/02/2016.
 */
public interface DeviceHandlerInterface {

    BluetoothDevice BLUETOOTH_DEVICE = null;
    ScioDevice scioDevice = null;

    void disconnectFromDevice(ScioDeviceCallback onDisconnect);

    void clearScioDevice();

    boolean hasDevice();

    void setDevice(BluetoothDevice bluetoothDevice, final DeviceConnectHandler handler);
    void setDevice(boolean isAutoConnect ,BluetoothDevice bluetoothDevice, final DeviceConnectHandler handler);
    String getDeviceAddress();

    String getDeviceName();

    Boolean isDeviceConnected();

    boolean isCalibrationNeeded();

    void connectToDevice(final DeviceConnectHandler handler);

    void scan(ScioDeviceScanHandler handler);

    void calibrate(ScioDeviceCalibrateHandler handler);

    void readBattery(ScioDeviceBatteryHandler handler);
}
