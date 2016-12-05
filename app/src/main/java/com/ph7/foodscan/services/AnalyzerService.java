package com.ph7.foodscan.services;

import android.util.Log;

import com.consumerphysics.android.sdk.model.ScioModel;
import com.consumerphysics.android.sdk.model.ScioReading;
import com.consumerphysics.android.sdk.sciosdk.ScioCloud;
import com.ph7.foodscan.callbacks.ScioCloudAnalyzeManyModelCallback;
import com.ph7.foodscan.services.interfaces.AnalyzerInterface;
import com.ph7.foodscan.application.FoodScanApplication;
import com.ph7.foodscan.callbacks.ScioCloudAnalyzeCallback;
import com.ph7.foodscan.callbacks.ScioCloudAnalyzeManyCallback;
import com.ph7.foodscan.models.ph7.ScioCollectionModel;
import com.ph7.foodscan.models.scio.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by craigtweedy on 19/02/2016.
 */
public class AnalyzerService implements AnalyzerInterface {

    private final ScioCloud scioCloud;
    private  int analyzeCount = 0;
    private ScioCloudAnalyzeManyCallback callBackLookup;
    private ScioCloudAnalyzeManyModelCallback callBackModelSetLookup;

    public AnalyzerService() {
        this.scioCloud = new ScioCloud(FoodScanApplication.getAppContext());
    }

    public void analyze(ScioReading scioReading, final ScioCollectionModel model, final ScioCloudAnalyzeCallback callback) {
        this.scioCloud.analyze(scioReading, model.getUuid(), new com.consumerphysics.android.sdk.callback.cloud.ScioCloudAnalyzeCallback() {
            @Override
            public void onSuccess(ScioModel scioModel) {
                Model returnModel = new Model(scioModel);
                callback.onSuccess(returnModel);
            }

            @Override
            public void onError(int i, String s) {
                callback.onError(i, s);
            }
        });
    }

    HashMap<String,String> modelMapping  = new HashMap<>();
    public void analyze(final ScioReading scioReading, final List<ScioCollectionModel> models, final ScioCloudAnalyzeCallback callback) {

        List<String> modelIds = new ArrayList<>();
        for (ScioCollectionModel model : models) {
            modelIds.add(model.getUuid());
            modelMapping.put(model.getUuid(),model.getName());
        }

        this.scioCloud.analyze(scioReading, modelIds, new com.consumerphysics.android.sdk.callback.cloud.ScioCloudAnalyzeManyCallback() {
            @Override
            public void onSuccess(List<ScioModel> list) {

                for (ScioModel model : list) {
                    Model returnModel = new Model(model);
                    // Check n Replace Model Name Value
                    if(modelMapping.containsKey(returnModel.getId()))
                    {
                        returnModel.setName(modelMapping.get(returnModel.getId())) ;
                    }
                    callback.onSuccess(returnModel);
                }
                Log.d("Analyze","Reading "+analyzeCount);
                --analyzeCount;
                if (analyzeCount<=0) {
                    Log.d("Analyze","*** Complete ***");
                   // callBackLookup.onSuccess(modelsLookups);
                    callBackModelSetLookup.onSuccess(modelSetLookups);
                }

            }


            @Override
            public void onError(int i, String s) {
                callback.onError(i, s);
            }
        });
    }
// not Used
    @Override
    public void analyze(final List<ScioReading> scioReadings, final ScioCollectionModel model, final ScioCloudAnalyzeManyCallback callback) {

        final Map<ScioReading, Model> lookup = new HashMap<>();
        analyzeCount = 0;


        for (final ScioReading scioReading : scioReadings) {
            this.analyze(scioReading, model, new ScioCloudAnalyzeCallback() {
                @Override
                public void onSuccess(Model scioModel) {
                    Model returnModel;
                    if (!lookup.containsKey(scioReading)) {
                        returnModel = scioModel;
                    } else {
                        returnModel = lookup.get(scioReading);
                        returnModel.addAttributes(scioModel.getAttributes());
                    }

                    lookup.put(scioReading, returnModel);
                    analyzeCount++;
                    if (analyzeCount == scioReadings.size()) {
                        callback.onSuccess(lookup);
                    }
                }

                @Override
                public void onError(int code, String msg) {
                    callback.onError(code, msg);
                }
            });
        }
    }

    final Map<ScioReading, Model> modelsLookups = new HashMap<>();
    // Not Using

    @Override
    public void analyze(final List<ScioReading> scioReadings, final List<ScioCollectionModel> models, ScioCloudAnalyzeManyCallback callback) {
        modelsLookups.clear();
        analyzeCount = scioReadings.size() ;
        this.callBackLookup  =  callback ;
        Log.d("Analyze","*** Start ***");
        for (final ScioReading scioReading : scioReadings) {
            this.analyze(scioReading, models, new ScioCloudAnalyzeCallback() {
                @Override
                public void onSuccess(Model scioModel) {
                     modelsLookups.put(scioReading, scioModel);
                }

                @Override
                public void onError(int code, String msg) {
                    callBackLookup.onError(code, msg);
                }
            });
        }
    }
    // Latest that is using

    final Map<ScioReading, HashSet<Model>> modelSetLookups = new HashMap<>();
    @Override
    public void analyze(List<ScioReading> scioReadings, List<ScioCollectionModel> models, ScioCloudAnalyzeManyModelCallback callback) {
        modelSetLookups.clear();
        analyzeCount = scioReadings.size() ;
        this.callBackModelSetLookup  =  callback ;
        Log.d("Analyze","*** Start ***");
        for (final ScioReading scioReading : scioReadings) {
            this.analyze(scioReading, models, new ScioCloudAnalyzeCallback() {
                @Override
                public void onSuccess(Model scioModel) {
                    HashSet<Model> modelHashSet  ;
                    if (!modelSetLookups.containsKey(scioReading)) {
                        modelHashSet = new HashSet<Model>();
                        modelHashSet.add(scioModel);
                    } else {
                        modelHashSet = modelSetLookups.get(scioReading);
                        modelHashSet.add(scioModel);
                    }
                    Log.d("Analyze","*** Model ***");
                    modelSetLookups.put(scioReading,modelHashSet);
                }

                @Override
                public void onError(int code, String msg) {
                    callBackModelSetLookup.onError(code, msg);
                }
            });
        }
    }
}
