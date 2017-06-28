package com.ph7.foodscan.callbacks;

import com.ph7.foodscan.models.ph7.ScioReadingWrapper;

import java.util.List;

/**
 * Created by craigtweedy on 17/06/2016.
 */
public interface ScanHandler {
    void onComplete(List<ScioReadingWrapper> scioReadings);
    void onFailed(Error exception);
}
