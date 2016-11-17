package com.ph7.analyserforscio.callbacks;

import org.json.JSONObject;

/**
 * Created by craigtweedy on 19/04/2016.
 */
public interface FoodScanHandler {
    void onSuccess(JSONObject object);
    void onError();
}
