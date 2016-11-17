package com.ph7.analyserforscio.callbacks;

import com.consumerphysics.android.sdk.model.ScioReading;
import com.ph7.analyserforscio.models.scio.Model;

import java.util.Map;

public interface ScioCloudAnalyzeManyCallback {

    void onSuccess(final Map<ScioReading, Model> models);
    void onError(final int code, final String msg);
}
