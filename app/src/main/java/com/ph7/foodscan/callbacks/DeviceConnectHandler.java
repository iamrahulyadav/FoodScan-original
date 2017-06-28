package com.ph7.foodscan.callbacks;

/**
 * Created by craigtweedy on 19/02/2016.
 */
public interface DeviceConnectHandler {
    void onConnect();

    void onFailed(String msg) ;
}
