package com.ph7.foodscan.activities.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.consumerphysics.android.sdk.callback.cloud.ScioCloudModelsCallback;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceCalibrateHandler;
import com.consumerphysics.android.sdk.model.ScioModel;
import com.ph7.foodscan.R;
import com.ph7.foodscan.activities.AppActivity;
import com.ph7.foodscan.adapters.SpinnerSCIOCollectionAdapter;
import com.ph7.foodscan.adapters.SpinnerSCIOModelAdapter;
import com.ph7.foodscan.application.FoodScanApplication;
import com.ph7.foodscan.callbacks.FoodScanHandler;
import com.ph7.foodscan.callbacks.ScanHandler;
import com.ph7.foodscan.callbacks.ScanUpdateHandler;
import com.ph7.foodscan.device.interfaces.DeviceHandlerInterface;
import com.ph7.foodscan.models.ph7.ScioCollection;
import com.ph7.foodscan.models.ph7.ScioCollectionModel;
import com.ph7.foodscan.models.ph7.ScioReadingWrapper;
import com.ph7.foodscan.services.FoodScanService;
import com.ph7.foodscan.services.ScanningService;
import com.ph7.foodscan.services.SessionService;
import com.ph7.foodscan.views.ShadowedButton;
import com.ph7.foodscan.views.StatusView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The NewTestActivity is the entry point into starting a new test using the SCiO device.
 * It is responsible for configuring the test parameters before running the test.
 *
 * @author  Craig Tweedy
 * @version 0.7
 * @since   2016-07-04
 */
public class NewTestActivity extends AppActivity {

    private FoodScanService foodScanService = new FoodScanService();
    private ScanningService scanningService = new ScanningService();
    private SessionService sessionService = new SessionService();

    private List<ScioCollection> collections = new ArrayList<>();
    private List<ScioCollectionModel> models = new ArrayList<>();
    private SpinnerSCIOCollectionAdapter collectionAdapter;
    private SpinnerSCIOModelAdapter modelAdapter;
    private DeviceHandlerInterface deviceHandler;
    private Spinner collectionList;
    private Spinner modelList;
    private LinearLayout selectedModelsContainer ;
    private ScioCollectionModel model;
    private ScioCollection collection;
    private int scansCount=1 ;
    private ArrayList<ScioCollectionModel> multipleModels = new ArrayList<>();
    private boolean isValidationSuccess = true ;
    private ProgressBar progressBarCollection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_test);
        setActionBarOverlayZero();

        this.progressBarCollection = (ProgressBar) findViewById(R.id.progressBarCollection);
        this.collectionList = (Spinner) findViewById(R.id.collectionList); // Collection Spinner
        this.modelList = (Spinner) findViewById(R.id.modelList);  // model Spinner
        this.selectedModelsContainer =  (LinearLayout) findViewById(R.id.selectedModelsContainer);  // Selected Models Container
        this.collectionAdapter = new SpinnerSCIOCollectionAdapter(this.getApplicationContext(), R.layout.simple_item, new ArrayList<ScioCollection>());
        this.collectionList.setAdapter(this.collectionAdapter);
        this.modelAdapter = new SpinnerSCIOModelAdapter(this.getApplicationContext(), R.layout.simple_item, new ArrayList<ScioCollectionModel>());
        this.modelList.setAdapter(this.modelAdapter);

        this.collectionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                modelAdapter.clear();
                ScioCollectionModel scioCollectionModel = new ScioCollectionModel("Choose a model");
                modelAdapter.add(scioCollectionModel);
                selectedModelsContainer.removeAllViews();
                multipleModels.clear();
                if(i>0) {
                    checkBlankFieldValidation();
                    modelAdapter.addAll(collections.get(i-1).getModels());
                    modelAdapter.notifyDataSetChanged();
                    collection = collections.get(i-1);
                    models = collections.get(i-1).getModels();
                    if (models.size() > 0) {
                        model = models.get(0);
                    } else {
                        model = null;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        this.modelList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i>0)
                {
                    model = models.get(i-1);
                   // multipleModels.add(model);
                    addAnotherModelCollection();
                    checkBlankFieldValidation();
                }
                else
                {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        this.setupSoundPool();

        this.getCollections();
        this.setupScanSeekBaar();
        this.setupScanButton();
        this.setupCalibrateButton();
        // checkBlankFieldValidation();
    }



    private void addAnotherModelCollection() {
        // Spinner spinner =  new Spinner();

        Spinner spinner  = new Spinner(getApplicationContext());
        spinner.setBackground( modelList.getBackground());
        spinner.setPopupBackgroundDrawable(modelList.getPopupBackground());
        ArrayList<ScioCollectionModel> modelsColl  = new ArrayList<>();
        SpinnerSCIOModelAdapter adapter = new SpinnerSCIOModelAdapter(this.getApplicationContext(), R.layout.simple_item, modelsColl);
        spinner.setAdapter(adapter);
        adapter.clear();
        ScioCollectionModel collectionModel = new ScioCollectionModel("Add Another");
        adapter.add(collectionModel);
        // modelsColl.addAll(models);
        // modelsColl.remove(i);
        adapter.addAll(models);
        selectedModelsContainer.addView(spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i>0)
                {
                    model = models.get(i-1);
                   // multipleModels.add(model);
                    addAnotherModelCollection();
                }
                else
                {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    DiscreteSeekBar seekBarScansCount ;
    private void setupScanSeekBaar() {
        seekBarScansCount = (DiscreteSeekBar) findViewById(R.id.seekBarScansCount);
        final NewTestActivity _this = this;



        seekBarScansCount.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                scansCount =  value ;
                checkBlankFieldValidation();
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView startTestButton = (TextView) findViewById(R.id.startTest);
        ShadowedButton calibrateButton = (ShadowedButton) findViewById(R.id.calibrate);

        startTestButton.setText("Start Test");
        calibrateButton.setText("Calibrate");
        checkBluetoothPermissions();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(networkReceiver, filter);

    }

    private void setupScanButton() {
        final TextView button = (TextView) findViewById(R.id.startTest);
        final NewTestActivity _this = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isValidationSuccess)
                {
                    return ;
                }
                if(!FoodScanApplication.getDeviceHandler().isDeviceConnected()) {
                    Snackbar.make(view,"No device connected!",Snackbar.LENGTH_SHORT).show();
                    return ;
                }

                getSelectedModels();

                _this.scan();
            }
        });
    }
    private void getSelectedModels() {
        multipleModels.clear();
        if(modelList.getSelectedItemPosition()>0)
        {
            multipleModels.add((ScioCollectionModel) modelList.getSelectedItem());
        }
        int countModelDD =  selectedModelsContainer.getChildCount() ;
        for (int indexdd = 0; indexdd < countModelDD; indexdd++) {
            Spinner spinner = (Spinner) selectedModelsContainer.getChildAt(indexdd);
            int indexSelectedModel  =  spinner.getSelectedItemPosition() ;
            if(indexSelectedModel>0)
            {
                multipleModels.add((ScioCollectionModel) spinner.getSelectedItem());
            }

        }

    }

    private void setupCalibrateButton() {
        final Button button = (Button) findViewById(R.id.calibrate);
        final NewTestActivity _this = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _this.calibrate();
            }
        });
    }

    private void calibrate() {
        final Button button = (Button) findViewById(R.id.calibrate);
        button.setText("Calibrating");
        FoodScanApplication.getDeviceHandler().calibrate(new ScioDeviceCalibrateHandler() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.setText("Calibrate");
                    }
                });
            }

            @Override
            public void onError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.setText("Calibrate");
                    }
                });
            }

            @Override
            public void onTimeout() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.setText("Calibrate");
                    }
                });
            }
        });
    }

    private void scan() {
        final NewTestActivity _this = this;
        final int _soundId =  this.soundId ;
        final TextView button = (TextView) findViewById(R.id.startTest);
        final  StatusView statusView = new StatusView(NewTestActivity.this);
        statusView.setStatusCode(2);
        statusView.setStatusMessage("Scanning in progress...");
        statusView.show();

        final int streamId =  (new SessionService().isSoundTurnedOn()) ?soundPool.play(_soundId,1.0f,1.0f,1,-1,1):0;
        this.scanningService = new ScanningService();
        scanningService.execute(new ScanHandler() {
            @Override
            public void onComplete(final List<ScioReadingWrapper> scioReadings) {
                if(new SessionService().isSoundTurnedOn())
                    soundPool.stop(streamId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        statusView.setStatusCode(1);
                        statusView.setBGColor();
                        statusView.setStatusMessage("Test complete");
                        statusView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                statusView.hide();

                                Intent intent = new Intent(_this, TestDetailsActivity.class);
                                intent.putParcelableArrayListExtra("readings", (ArrayList<ScioReadingWrapper>)scioReadings);
                                intent.putExtra("model", model);
                                intent.putParcelableArrayListExtra("models", multipleModels);
                                intent.putExtra("collection", collection);
                                intent.putExtra("scans", scansCount);
                                startActivity(intent);
                            }
                        });

                    }
                });
            }

            @Override
            public void onFailed(final Error exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(new SessionService().isSoundTurnedOn())
                            soundPool.stop(streamId);
                        button.setText("Start Test");
                        Log.d("Reading", exception.getLocalizedMessage());
                        if (exception instanceof ScanningService.DeviceNeedsCalibrationError) {
                            statusView.hide();
                            Toast.makeText(_this, "SCiO needs calibration", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        },this.scansCount, new ScanUpdateHandler() {
            @Override
            public void updateScanCount(final int scanCount) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusView.setStatusMessage("Scan "+scanCount+" in progress...");
                    }
                });

            }
        });
    }

    private void getCollections() {
        if(sessionService ==null || sessionService.getUserToken() ==null|| sessionService.getUserToken().trim().isEmpty())
        {
            return ;
        }


        progressBarCollection.setVisibility(View.VISIBLE);

        //get Collection n=and model from CP server for Specific User login
       // getCollectionFromCPForSpecificUser();

        // get Collection From Food Scan Server
        this.foodScanService.getCollections(new FoodScanHandler() {
            @Override
            public void onSuccess(JSONObject object) {
                progressBarCollection.setVisibility(View.GONE);
                try {
                    collections = ScioCollection.fromJSON(object.getJSONArray("collections"));
                } catch (JSONException e) {
                    collections = new ArrayList<>();
                }
                collectionAdapter.clear();
                ScioCollection scioCollection =  new ScioCollection("Choose a collection");
                collectionAdapter.add(scioCollection);
                collectionAdapter.addAll(collections);
                collectionAdapter.notifyDataSetChanged();
                collectionList.setSelection(0);
            }

            @Override
            public void onError() {
                progressBarCollection.setVisibility(View.GONE);
                getCollections();
            }


        });
    }

//    private void getCollectionFromCPForSpecificUser() {
//
//        this.scioCloud.getModels(new ScioCloudModelsCallback() {
//            @Override
//            public void onSuccess(List<ScioModel> list) {
//                progressBarCollection.setVisibility(View.GONE);
//                HashMap<String,ScioCollection> modelHashMap = new HashMap<>();
//                for (ScioModel scioModel : list) {
//                    String collName  = scioModel.getCollectionName() ;
//                    if(modelHashMap.containsKey(collName)) {
//                        ScioCollectionModel scioCollectionModel = new ScioCollectionModel(scioModel.getName(),scioModel.getId(),scioModel.getType().toString().toLowerCase());
//                        modelHashMap.get(collName).addModel(scioCollectionModel);
//                    }
//                    else {
//                        ScioCollection scioCollection = new ScioCollection(collName);
//                        String uuid  = UUID.randomUUID().toString() ;
//                        scioCollection.setUuid(uuid);
//                        ScioCollectionModel scioCollectionModel = new ScioCollectionModel(scioModel.getName(),scioModel.getId(),scioModel.getType().toString().toLowerCase());
//                        scioCollection.addModel(scioCollectionModel);
//                        modelHashMap.put(collName,scioCollection);
//                    }
//                }
//
//                collections = new ArrayList<>();
//                collectionAdapter.clear();
//                ScioCollection scioCollection =  new ScioCollection("Choose a collection");
//                collectionAdapter.add(scioCollection);
//                Iterator it = modelHashMap.entrySet().iterator();
//                while (it.hasNext()) {
//                    Map.Entry pair = (Map.Entry)it.next();
//                    collections.add((ScioCollection) pair.getValue()) ;
//                    it.remove(); // avoids a ConcurrentModificationException
//                }
//                collectionAdapter.addAll(collections);
//                collectionAdapter.notifyDataSetChanged();
//                collectionList.setSelection(0);
//            }
//
//            @Override
//            public void onError(int i, String s) {
//                progressBarCollection.setVisibility(View.GONE);
//            }
//        });
//    }


    private void checkBlankFieldValidation() {

        final TextView startTestButton = (TextView) findViewById(R.id.startTest);

        if(scansCount>0) // && this.modelList.getSelectedItemPosition()>0 && collectionList.getSelectedItemPosition()>0
        {
            startTestButton.setEnabled(true);
            isValidationSuccess =  true  ;
        }
        else
        {
            startTestButton.setEnabled(false);
            isValidationSuccess =  false  ;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }



}
