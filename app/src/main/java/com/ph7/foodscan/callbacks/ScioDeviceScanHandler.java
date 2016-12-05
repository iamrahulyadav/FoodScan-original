package com.ph7.foodscan.callbacks;

import com.consumerphysics.android.sdk.model.ScioReading;

/**
 * Created by craigtweedy on 19/02/2016.
 */
public interface ScioDeviceScanHandler {
    void onSuccess(ScioReading var1);

    void onNeedCalibrate();

    void onError();

    void onTimeout();
}
