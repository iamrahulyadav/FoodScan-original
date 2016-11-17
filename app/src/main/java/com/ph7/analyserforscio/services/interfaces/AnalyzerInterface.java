package com.ph7.analyserforscio.services.interfaces;

import com.consumerphysics.android.sdk.model.ScioReading;
import com.ph7.analyserforscio.callbacks.ScioCloudAnalyzeCallback;
import com.ph7.analyserforscio.callbacks.ScioCloudAnalyzeManyCallback;
import com.ph7.analyserforscio.callbacks.ScioCloudAnalyzeManyModelCallback;
import com.ph7.analyserforscio.models.ph7.ScioCollectionModel;

import java.util.List;

/**
 * Created by craigtweedy on 19/02/2016.
 */
public interface AnalyzerInterface {

    void analyze(ScioReading scioReading, ScioCollectionModel model, ScioCloudAnalyzeCallback callback);
    void analyze(ScioReading scioReading, List<ScioCollectionModel> model, ScioCloudAnalyzeCallback callback);

    void analyze(List<ScioReading> scioReadings, ScioCollectionModel model, ScioCloudAnalyzeManyCallback callback);
    void analyze(List<ScioReading> scioReadings, List<ScioCollectionModel> model, ScioCloudAnalyzeManyCallback callback);
    void analyze(List<ScioReading> scioReadings, List<ScioCollectionModel> model, ScioCloudAnalyzeManyModelCallback callback);
}
