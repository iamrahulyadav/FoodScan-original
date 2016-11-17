package com.ph7.analyserforscio.device;

/**
 * Created by craigtweedy on 25/01/2016.
 */
public final class BluetoothDevice {
    private final String address;
    private final String name;

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public BluetoothDevice(final String address, final String name) {
        this.name = name;
        this.address = address;
    }
}
