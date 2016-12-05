package com.ph7.foodscan.activities.analyse;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ph7.foodscan.R;
import com.ph7.foodscan.activities.AppActivity;
import com.ph7.foodscan.activities.scan.ReTestDetailsActivity;
import com.ph7.foodscan.application.FoodScanApplication;
import com.ph7.foodscan.callbacks.DeviceConnectHandler;
import com.ph7.foodscan.callbacks.ScanHandler;
import com.ph7.foodscan.callbacks.ScanUpdateHandler;
import com.ph7.foodscan.device.interfaces.DeviceHandlerInterface;
import com.ph7.foodscan.models.ph7.ScioReadingWrapper;
import com.ph7.foodscan.models.scio.SCIOResultDataModel;
import com.ph7.foodscan.services.FCDBService;
import com.ph7.foodscan.services.ScanningService;
import com.ph7.foodscan.services.SessionService;
import com.ph7.foodscan.views.ClassificationModelDetailsView;
import com.ph7.foodscan.views.EstimationModelDetailsView;
import com.ph7.foodscan.views.StatusView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TestResultsActivity extends AppActivity {

    String test_id  = "";
    HashMap<String ,String> organisedData;//HashMap<String ,ArrayList<String>> organisedData;
    LinearLayout modelsDescriptionCont ;
    private boolean isEnableRetest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_results);
        setActionBarOverlayZero();
        Intent intent = getIntent();
        test_id = intent.getStringExtra("test_id");
        isEnableRetest = intent.getBooleanExtra("isEnableRetest",false);
        setAnalysedData();
        setupHeader();
        this.setupSoundPool();
        modelsDescriptionCont = (LinearLayout)findViewById(R.id.modelsDescriptionCont) ;
        // TextView responseString = (TextView)findViewById(R.id.responseString) ;
        try {

            organisedData = organizeDataIntoModels();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        parseCollectionId();
        setupModels();

        //
        ActionBar actionBar =  getSupportActionBar() ;
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.custom_text_toolbar);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater  =  getMenuInflater() ;
        if(isEnableRetest)
            menuInflater.inflate(R.menu.test_result_menu,menu);
        else
            menuInflater.inflate(R.menu.menu,menu);
        return true;
    }

    private String collectionId =  "";
    private void parseCollectionId() {
        try {
            JSONObject jObj  = new JSONObject(analysedDataModel.collection_id);
            collectionId = jObj.getString("uuid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupModels() {

        try {
            JSONArray modelsArr  =  new JSONArray(analysedDataModel.model_ids) ;
            for (int indexModel = 0; indexModel < modelsArr.length(); indexModel++) {
                JSONObject jObj = modelsArr.getJSONObject(indexModel);
                String modelName =  jObj.getString("name") ;
                String modelType = "";
                //String modelID ;
                if(organisedData!=null)
                {
                    if(organisedData.containsKey(modelName.trim()))
                    {
                        String list = organisedData.get(modelName.trim());
                        if(!list.isEmpty())
                        {
                            JSONObject obj =  new JSONObject(list);
                            modelType = obj.getString("type");
                        }
                        // modelID = jObj.getString("uuid");
                        setupModelDescription(list,modelType);
                    }
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void setupModelDescription(String list, String type) {

        switch(type.toLowerCase()) {
            case "classification" :
                ClassificationModelDetailsView modelDetailsView = new ClassificationModelDetailsView(getApplicationContext());
                modelDetailsView.setModelRecords(list,collectionId);
                modelsDescriptionCont.addView(modelDetailsView);
                break ;

            case "estimation" :
                EstimationModelDetailsView estimationModelView  = new EstimationModelDetailsView(getApplicationContext());
                estimationModelView.setModelRecords(list,collectionId);
                modelsDescriptionCont.addView(estimationModelView);
                break ;
        }
    }



    private HashMap<String, String> organizeDataIntoModels() throws JSONException {

        HashMap<String,String>  data  =  new HashMap<>();

        String testAnalysedResult =  analysedDataModel.test_scan_result ;

        JSONObject rootJsonObj =  new JSONObject(testAnalysedResult);
        JSONArray aggregationsJArr  =  rootJsonObj.getJSONArray("aggregations");
        boolean success  =  rootJsonObj.getBoolean("success");
        if(success)
        {
            for (int indexAggregation = 0; indexAggregation < aggregationsJArr.length(); indexAggregation++) {
                JSONObject  jObjAggregation  = aggregationsJArr.getJSONObject(indexAggregation) ;
                JSONObject jModelObj  =  jObjAggregation.getJSONObject("model");
                String  modelName  =  jModelObj.getString("name").trim() ;
                data.put(modelName,jObjAggregation.toString());
            }
        }
        return data ;
    }


    private void setupHeader() {
        ImageView ivScanImages = (ImageView) findViewById(R.id.ivScanImages);
        TextView tvTestName = (TextView) findViewById(R.id.tvTestName);
        TextView testCreatedDate = (TextView) findViewById(R.id.testCreatedDate);
        tvTestName.setText(analysedDataModel.test_name);
        testCreatedDate.setText("Tested "+analysedDataModel.create_datetime);
        String imgs_path  =  analysedDataModel.imgs_path;
        imgs_path= imgs_path!=null?imgs_path:"";

        String[] imgs= imgs_path.split(",");
        for (String img :imgs) {
            if(showImage(ivScanImages,img))
            {
                break ;
            }
        }

    }

    private boolean showImage(ImageView iv,String path) {
        if(!path.isEmpty()) {
            File imgFile = new File(path);

            if (imgFile.exists()) {
                // Decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, o);

                // The new size we want to scale to
                final int REQUIRED_SIZE = 1024;

                // Find the correct scale value. It should be the power of 2.
                int width_tmp = o.outWidth, height_tmp = o.outHeight;
                int scale = 1;
                while (true) {
                    if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                        break;
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale *= 2;
                }
                // Decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                Bitmap bitmap = BitmapFactory.decodeFile(path, o2);
                iv.setImageBitmap(bitmap);
                return true;
            }
        }
        return false ;
    }

    private void setAnalysedData() {
        FCDBService fcdbService = new FCDBService(TestResultsActivity.this);
        Cursor cursor =  fcdbService.getAnalysedTestsById(test_id);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                getAnalysedData(cursor);
                cursor.moveToNext();
            }
        }catch(Exception x)
        {
            x.printStackTrace();
        }
        finally {
            if(cursor!=null) cursor.close();
            if(fcdbService!=null) fcdbService.close();
        }

    }

    private SCIOResultDataModel analysedDataModel ;
    private void getAnalysedData(Cursor testsCursor) {
        analysedDataModel = new SCIOResultDataModel();
        testsCursor.getString(0); // id
        analysedDataModel.test_id = testsCursor.getString(1); // test_id
        analysedDataModel.test_name = testsCursor.getString(2); // test_name
        analysedDataModel.test_note = testsCursor.getString(3); // test_note
        analysedDataModel.test_location = testsCursor.getString(4); // test_location
        analysedDataModel.model_ids = testsCursor.getString(5); // model_ids
        analysedDataModel.collection_id = testsCursor.getString(6); // collection_id
        analysedDataModel.test_scan_result = testsCursor.getString(7); // test_scan_result
        analysedDataModel.scan_count = testsCursor.getInt(8); // scan_count
        analysedDataModel.test_status = testsCursor.getString(9); // test_status
        analysedDataModel.create_datetime = testsCursor.getString(10); // create datetime
        analysedDataModel.expire = testsCursor.getString(11); // expire
        analysedDataModel.imgs_path = testsCursor.getString(12); // imgs_path
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId())
        {
            case R.id.action_rescan:
                rescan("Please connect scanner!") ;

                break;
        }


        return true ;
    }

    private void rescan(final String msg) {
        final TextView view = (TextView) findViewById(R.id.tvTestName);
        DeviceHandlerInterface deviceHandler = FoodScanApplication.getDeviceHandler();
        if(deviceHandler.isDeviceConnected())
        {
            if(!deviceHandler.isCalibrationNeeded()) rescanWithLastScanValues();
            else  Toast.makeText(getApplicationContext(),"Calibration required first!",Toast.LENGTH_SHORT).show();
        }
        else Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE).setAction("Connect", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = ProgressDialog.show(TestResultsActivity.this,"","Searching device...",true) ;
                checkBluetoothPermissions();
            }
        }).show();
    }

    private void rescanWithLastScanValues() {

        // Get Recent Scan Input Data

        // Get Scan Data and Open Re Analyze activity
        scan(analysedDataModel.scan_count);

    }

    private void scan(final int scansCount) {
        final TestResultsActivity _this = this;
        final int _soundId =  this.soundId ;
        final StatusView statusView = new StatusView(_this);
        statusView.setStatusCode(2);
        statusView.setStatusMessage("Scanning in progress...");
        statusView.show();
        final int streamId =  (new SessionService().isSoundTurnedOn()) ?soundPool.play(_soundId,1.0f,1.0f,1,-1,1):0;
        ScanningService scanningService = new ScanningService();
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
                                Intent intent = new Intent(_this, ReTestDetailsActivity.class);
                                intent.putParcelableArrayListExtra("readings", (ArrayList<ScioReadingWrapper>)scioReadings);
                                intent.putExtra("data",analysedDataModel) ;
                                startActivity(intent);
                                finish();
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
                        Log.d("Reading", exception.getLocalizedMessage());
                        if (exception instanceof ScanningService.DeviceNeedsCalibrationError) {
                            statusView.hide();
                            Toast.makeText(_this, "SCiO needs calibration", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        },scansCount,new ScanUpdateHandler() {
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

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(networkReceiver, filter);

    }
    ProgressDialog progressDialog  ;
    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =  intent.getAction() !=null ? intent.getAction():"" ;
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null && device.getName().startsWith("SCiO")) {
                    final String deviceName = device.getName().substring(4);
                    Log.d("AppActivity : deviceId",device.getAddress());
                    if(device.getAddress().equals(new SessionService().getScioDeviceId()))
                    {
                        progressDialog.setMessage("Connecting to "+deviceName);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FoodScanApplication.getDeviceHandler().setDevice(false,new com.ph7.foodscan.device.BluetoothDevice(device.getAddress(),deviceName), new DeviceConnectHandler() {
                                    @Override
                                    public void onConnect() {
                                        progressDialog.setMessage("Connected to "+deviceName);
                                        progressDialog.dismiss();
                                        rescan("Please Reconnect scanner!");
                                    }
                                    @Override
                                    public void onFailed(String msg) {
                                        progressDialog.dismiss();
                                        rescan(msg);
                                    }
                                });
                            }
                        });

                    }

                }
                else { Log.d("MyTag", "device.getName() is null");}
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }
}
