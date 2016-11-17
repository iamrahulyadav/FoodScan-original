package com.ph7.analyserforscio.callbacks;

import com.consumerphysics.android.sdk.model.ScioReading;
import com.ph7.analyserforscio.models.scio.Model;

import java.util.HashSet;
import java.util.Map;

/**
 * Created by craigtweedy on 19/02/2016.
 */
public interface ScioCloudAnalyzeManyModelCallback {


    void onSuccess(final Map<ScioReading, HashSet<Model>> models);
    void onError(final int code, final String msg);
}

