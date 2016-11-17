package com.ph7.analyserforscio.callbacks;

import com.ph7.analyserforscio.models.scio.Model;

/**
 * Created by craigtweedy on 19/02/2016.
 */
public interface ScioCloudAnalyzeCallback {

    void onSuccess(final Model model);
    void onError(final int code, final String msg);
}

