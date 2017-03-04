package com.ph7.foodscan.activities.scan;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.consumerphysics.android.sdk.model.ScioReading;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.ph7.foodscan.R;
import com.ph7.foodscan.activities.AppActivity;
import com.ph7.foodscan.activities.analyse.TestResultsActivity;
import com.ph7.foodscan.activities.main.DashboardActivity;
import com.ph7.foodscan.activities.main.LocationActivity;
import com.ph7.foodscan.application.FoodScanApplication;
import com.ph7.foodscan.application.PermissionRequestHandler;
import com.ph7.foodscan.callbacks.FoodScanHandler;
import com.ph7.foodscan.callbacks.ScanHandler;
import com.ph7.foodscan.callbacks.ScanUpdateHandler;
import com.ph7.foodscan.callbacks.ScioCloudAnalyzeManyModelCallback;
import com.ph7.foodscan.models.ph7.Business;
import com.ph7.foodscan.models.ph7.Device;
import com.ph7.foodscan.models.ph7.Location;
import com.ph7.foodscan.models.ph7.Sample;
import com.ph7.foodscan.models.ph7.Scan;
import com.ph7.foodscan.models.ph7.ScanBundle;
import com.ph7.foodscan.models.ph7.ScioCollection;
import com.ph7.foodscan.models.ph7.ScioCollectionModel;
import com.ph7.foodscan.models.ph7.ScioReadingWrapper;
import com.ph7.foodscan.models.scio.Model;
import com.ph7.foodscan.models.scio.SCIOResultDataModel;
import com.ph7.foodscan.services.FCDBService;
import com.ph7.foodscan.services.FoodScanService;
import com.ph7.foodscan.services.ScanStorageService;
import com.ph7.foodscan.services.ScanningService;
import com.ph7.foodscan.services.SessionService;
import com.ph7.foodscan.services.interfaces.AnalyzerInterface;
import com.ph7.foodscan.utils.Validation;
import com.ph7.foodscan.views.StatusView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * The TestDetailsActivity is responsible for allowing users to add additional information
 * to their test results, before deciding whether or not to analyse the test results. The
 * user can save the results for later or analyse them now.
 *
 * @author  Craig Tweedy
 * @version 0.7
 * @since   2016-07-04
 */
public class ReTestDetailsActivity extends AppActivity  implements GoogleApiClient.ConnectionCallbacks , GoogleApiClient.OnConnectionFailedListener{

    int addImagesLimit = 0 ;
    private static final int PICK_IMAGE = 100;
    private static final int PICK_Camera_IMAGE = 101;

    private Uri imageUri;
    private ScanBundle scanBundle;
    ScioCollection collection;
    private Business selectedBusiness = null;

    private AnalyzerInterface analyzerService = FoodScanApplication.getAnalyzerService();
    private FoodScanService foodScanService = new FoodScanService();
    private ArrayList<ScioCollectionModel> models ;
    private ArrayList<String> listImages =  new ArrayList<>();
    List<ScioReadingWrapper> readings = new ArrayList<>();

    private GridLayout addedPhotosContainer ;
    EditText etBusinessLocation;
    private SCIOResultDataModel analysedData;
    private int scans;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retest_details);
        // Fields Ids
        addedPhotosContainer = (GridLayout) findViewById(R.id.addedPhotosContainer);
        final EditText etTestName  =  (EditText) findViewById(R.id.etTestName );
        final EditText etTestNote = (EditText)findViewById(R.id.etTestNote);
        etBusinessLocation  =  (EditText) findViewById(R.id.etBusinessLocation);
        final TextView lblTestBusinessName =  (TextView) findViewById(R.id.lblTestBusinessName);
        final FrameLayout containerBizLocation =  (FrameLayout) findViewById(R.id.containerBizLocation);
        final ImageView ivClearTestName = (ImageView) findViewById(R.id.ivClearTestName);
        ivClearTestName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etTestName.setText("");
            }
        });
        final ImageView ivClearTestNote = (ImageView) findViewById(R.id.ivClearTestNote);
        ivClearTestNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etTestNote.setText("");
            }
        });
        final ImageView ivClearTestBusinessName = (ImageView) findViewById(R.id.ivClearTestBusinessName);
        ivClearTestBusinessName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etBusinessLocation.setText("");
                lblTestBusinessName.setText("");
                containerBizLocation.setVisibility(View.GONE);
                selectedBusiness = null ;
            }
        });
        final ImageView ivClearTestBusinessLocation = (ImageView) findViewById(R.id.ivClearTestBusinessLocation);
        ivClearTestBusinessLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etBusinessLocation.setText("");
                lblTestBusinessName.setText("");
                containerBizLocation.setVisibility(View.GONE);
                selectedBusiness = null ;
            }
        });
        etBusinessLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                final ImageView buttonCurrentLocation =  (ImageView) findViewById(R.id.buttonCurrentLocation) ;
                if(charSequence.toString().trim().isEmpty())
                {
                    buttonCurrentLocation.setEnabled(true);
                }
                else
                {
                    buttonCurrentLocation.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ImageView buttonCurrentLocation =  (ImageView) findViewById(R.id.buttonCurrentLocation) ;
        buttonCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // setupLocation();
                if(!Validation.isNetworkConnected(ReTestDetailsActivity.this))
                {
                    Snackbar.make(view, "No internet connection!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                Intent intent  = new Intent(ReTestDetailsActivity.this, LocationActivity.class);
                startActivityForResult(intent,102);
            }
        });

        this.readings = (List<ScioReadingWrapper>) getIntent().getExtras().get("readings");
        analysedData  = (SCIOResultDataModel) getIntent().getSerializableExtra("data") ;
        String collection_id = analysedData.collection_id ;
        String model_ids = analysedData.model_ids ;
        String test_location = analysedData.test_location ;
        String img_locs  =  analysedData.imgs_path ;
        if(!test_location.trim().isEmpty())
        {
            try {
                JSONObject rootBObj = new JSONObject(test_location) ;
                int bId  =  rootBObj.getInt("id") ;
                if(bId != -1)
                {
                    String name  = null;
                    name = rootBObj.getString("name");
                    String address  =  rootBObj.getString("address") ;
                    etBusinessLocation.setText(address);
                    lblTestBusinessName.setText(name);
                    if(!name.trim().isEmpty()) containerBizLocation.setVisibility(View.VISIBLE);
                    selectedBusiness = new Business();
                    selectedBusiness.setName(name);
                    selectedBusiness.setAddress(address);
                    selectedBusiness.setId(bId);
                    JSONObject locJObj  = rootBObj.getJSONObject("location") ;
                    double lat = locJObj.getDouble("lat");
                    double lng = locJObj.getDouble("lng");
                    selectedBusiness.setLocation(new Location(lat,lng));
                }
                else
                {
                    String address  =  rootBObj.getString("address") ;
                    etBusinessLocation.setText(address);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
           // setCollection(collection_id) ;
            setCollectionModels(model_ids) ;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setImagePaths(img_locs) ;
        scans =  analysedData.scan_count ;

        // Bind data into fields

        etTestName.setText(analysedData.test_name);
        etTestName.setSelection(etTestName.getText().length());
        etTestNote.setText(analysedData.test_note);

        this.setupBundle();
        this.setupAnalyse();
        this.setupLocation();
        this.setupRescan();
        this.setupSaveForLater();
        this.setupAddPhotoView();
        setActionBarOverlayZero();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void setImagePaths(String img_locs) {
        String[] imgs  = img_locs.split(",");
        for (int indexImg = 0; indexImg < imgs.length; indexImg++) {
            String  imgPath  = imgs[indexImg] ;
            if(!imgPath.trim().isEmpty()) decodeFile(imgPath);
        }
    }

    private void setCollection(String collection_id) throws JSONException {
        JSONObject rootobj = new JSONObject(collection_id);
        String collName =  rootobj.getString("name") ;
        String collUUID  = rootobj.getString("uuid") ;
        collection = new ScioCollection(collName) ;
        collection.setUuid(collUUID);
    }
    private void setCollectionModels(String model_ids) throws JSONException {
        JSONArray modelJArr  =  new JSONArray(model_ids);
        models = new ArrayList<>();
        for (int indexModel = 0; indexModel < modelJArr.length(); indexModel++) {

            JSONObject modelJObj  = modelJArr.getJSONObject(indexModel) ;
            String modelName  =  modelJObj.getString("name") ;
            String modelUUID  =  modelJObj.getString("uuid") ;
            String modelSource  =  modelJObj.getString("source") ;
            ScioCollectionModel model = new ScioCollectionModel(modelName,modelUUID,"",modelSource) ;
            models.add(model);
        }
    }
    private void setupAddPhotoView() {
        RelativeLayout  addPhoto =  (RelativeLayout) findViewById(R.id.addPhoto);
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addImagesLimit>=3) {
                    Toast.makeText(ReTestDetailsActivity.this, "Add photos limit over!.", Toast.LENGTH_SHORT).show();
                    return;
                }
                final CharSequence[] items = {"Take Photo", "Choose from Gallery", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(ReTestDetailsActivity.this);
                builder.setTitle("Select Image");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Take Photo")) {
                            if(PermissionRequestHandler.requestPermissionToCamera(ReTestDetailsActivity.this)) camFunction();
                        } else if (items[item].equals("Choose from Gallery")) {
                            if(PermissionRequestHandler.requestPermissionToGallary(ReTestDetailsActivity.this)) gallaryFun();
                        } else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.create() ;
                builder.show();
            }
        });
    }

    private void setupSaveForLater() {
        final LinearLayout button = (LinearLayout) findViewById(R.id.saveForLater);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //saveForLaterAnalyse();
                saveNGoToNext("");
            }
        });
    }

    private void saveForLaterAnalyse() {
        final EditText etTestName =  (EditText) findViewById(R.id.etTestName) ;
        final EditText etTestNote =  (EditText) findViewById(R.id.etTestNote) ;

        String test_id=  Validation.generateRandomString(15);
        String test_name = etTestName.getText().toString().trim();
        if(test_name.isEmpty())
        {
            Snackbar.make(etTestName, "Please enter test name.", Snackbar.LENGTH_SHORT).show();
            return;
        }
        String test_note = etTestNote.getText().toString().trim() ;

        // business Location json
        String test_location = "";
        try {
            test_location = createBusinessJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String create_datetime = dateFormat.format(Calendar.getInstance().getTime());

        String expire= "" ;
        String imgs_path = "" ;

        for (String imagePath:listImages) {
            imgs_path = imgs_path+","+imagePath;
        }
        if(!imgs_path.isEmpty())
        {
            imgs_path = imgs_path.substring(1);
        }

        List<ScioReading> scioReadings = new ArrayList<>();
        for (ScioReadingWrapper scioReadingWrapper : readings) {
            scioReadings.add(scioReadingWrapper.getScioReading());
        }

        ScanStorageService scanStorageService =  new ScanStorageService();
        String scanDirPath  = scanStorageService.initScansStorage(getApplicationContext());
        scanStorageService.saveScanToStorage(scioReadings);

        FCDBService fcdbService = new FCDBService(getApplicationContext());
        try {
            String timeStamp  = String.valueOf(Calendar.getInstance().getTime().getTime());
            if (fcdbService.saveTestScan(test_id, test_name, test_note, test_location,this.analysedData.model_ids,
                    this.analysedData.collection_id,scanDirPath, this.scans,create_datetime, expire, imgs_path,timeStamp)) {
                final StatusView statusView = new StatusView(ReTestDetailsActivity.this);
                statusView.setStatusCode(1);
                statusView.setStatusMessage("Saved for later");
                statusView.show();
                statusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        statusView.hide();
                        //startActivity(new Intent(ReTestDetailsActivity.this, DashboardActivity.class));
                        //finish();
                    }
                });
            } else {
                final StatusView statusView = new StatusView(ReTestDetailsActivity.this);
                statusView.setStatusCode(0);
                statusView.setStatusMessage("Something Wrong");
                statusView.show();
                statusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        statusView.hide();
                    }
                });
            }

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally {
            fcdbService.close();
        }

    }

    private void saveNGoToNext(String where)
    {
        final EditText etTestName =  (EditText) findViewById(R.id.etTestName) ;
        final EditText etTestNote =  (EditText) findViewById(R.id.etTestNote) ;

        String test_id=  Validation.generateRandomString(15);
        String test_name = etTestName.getText().toString().trim();
        if(test_name.isEmpty())
        {
            Snackbar.make(etTestName, "Please enter test name.", Snackbar.LENGTH_SHORT).show();
            return;
        }
        String test_note = etTestNote.getText().toString().trim() ;

        // business Location json
        String test_location = "";
        try {
            test_location = createBusinessJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("FoodScan","Test Location : "+test_location);

        String create_datetime = dateFormat.format(Calendar.getInstance().getTime());
        //create_datetime = create_datetime.replace(" "," at ");
        String expire= "" ;
        String imgs_path = "" ;

        for (String imagePath:listImages) {
            imgs_path = imgs_path+","+imagePath;
        }
        if(!imgs_path.isEmpty())
        {
            imgs_path = imgs_path.substring(1);
        }

        List<ScioReading> scioReadings = new ArrayList<>();
        for (ScioReadingWrapper scioReadingWrapper : readings) {
            scioReadings.add(scioReadingWrapper.getScioReading());
        }

        ScanStorageService scanStorageService =  new ScanStorageService();
        String scanDirPath  = scanStorageService.initScansStorage(getApplicationContext());
        scanStorageService.saveScanToStorage(scioReadings);

        FCDBService fcdbService = new FCDBService(getApplicationContext());
        try {
            String timeStamp  = String.valueOf(Calendar.getInstance().getTime().getTime());
            if (fcdbService.saveTestScan(test_id, test_name, test_note, test_location,this.analysedData.model_ids,
                    this.analysedData.collection_id,scanDirPath, this.scans,create_datetime, expire, imgs_path,timeStamp)) {
                switch(where)
                {
                    case "scan" :
                        Toast.makeText(ReTestDetailsActivity.this, "Saved for later", Toast.LENGTH_SHORT).show();
                        rescan();
                        break ;

                    case "login":
                        Toast.makeText(ReTestDetailsActivity.this, "Saved for later", Toast.LENGTH_SHORT).show();
                        break ;

                    case "analyse"  :
                        this.test_id = test_id ;
                        Toast.makeText(ReTestDetailsActivity.this, "Saved for later", Toast.LENGTH_SHORT).show();  // [23-02-2017]
                        break ;

                    default :
                        final StatusView statusView = new StatusView(ReTestDetailsActivity.this);
                        statusView.setStatusCode(1);
                        statusView.setStatusMessage("Saved for later");
                        statusView.show();
                        statusView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                statusView.hide();
                                startActivity(new Intent(ReTestDetailsActivity.this, DashboardActivity.class));
                                finish();
                            }
                        });
                        break ;
                }
            } else {
                final StatusView statusView = new StatusView(ReTestDetailsActivity.this);
                statusView.setStatusCode(0);
                statusView.setStatusMessage("Something Wrong");
                statusView.show();
                statusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        statusView.hide();
                    }
                });

            }

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally {
            fcdbService.close();
        }
    }

    private String createBusinessJSON() throws JSONException {
        JSONObject businessJOBj = new JSONObject();
        String test_location= etBusinessLocation.getText().toString().trim() ;
        if(this.selectedBusiness == null)
        {
            businessJOBj.put("name","");
            businessJOBj.put("address",test_location);
            businessJOBj.put("id",-1);
            JSONObject locJObj  =  new JSONObject();
            locJObj.put("lat",0.0);
            locJObj.put("lng",0.0);
            businessJOBj.put("location",locJObj);

        }
        else
        {
            businessJOBj.put("name",this.selectedBusiness.getName());
            businessJOBj.put("address",test_location);
            businessJOBj.put("id", this.selectedBusiness.getId() );
            JSONObject locJObj  =  new JSONObject();
            locJObj.put("lat",this.selectedBusiness.getLocation().getLatitude());
            locJObj.put("lng",this.selectedBusiness.getLocation().getLongitude());
            businessJOBj.put("location",locJObj);
        }
        return businessJOBj.toString();
    }

    private void setupBundle() {
        this.scanBundle = new ScanBundle();
        this.scanBundle.sample = new Sample();
        if(this.collection!=null)  this.scanBundle.setCollectionUuid(this.collection.getUuid());
        this.scanBundle.sample.setBusiness(this.selectedBusiness);
        for (ScioReadingWrapper scioReading: this.readings) {
            Scan scan = new Scan(scioReading);

            // [04-03-2017]
            String deviceName = FoodScanApplication.getDeviceHandler().getDeviceName() ;
            String deviceAddress = FoodScanApplication.getDeviceHandler().getDeviceAddress() ;
            Device device = new Device(deviceName,deviceAddress);
            scan.addDevice(device);

            this.scanBundle.addScan(scan);
        }
    }


    private void setupRescan() {
        final LinearLayout rescanButton = (LinearLayout) findViewById(R.id.rescanButton);
        rescanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //saveAndScan(true);
                saveNGoToNext("scan");
            }
        });
    }

    private void setupAnalyse() {
        final LinearLayout button = (LinearLayout) findViewById(R.id.analyseNow);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!Validation.isNetworkConnected(ReTestDetailsActivity.this))
                {
                    Snackbar.make(view, "No internet connection!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(!isLogin())
                {
                    ///saveForLaterAnalyse();
                    saveNGoToNext("login");
                    return ;
                }
                //    if(checkValidation()) {
                final EditText etTestName = (EditText) findViewById(R.id.etTestName);
                String test_name = etTestName.getText().toString().trim();
                if (test_name.isEmpty()) {
                    Snackbar.make(view, "Please enter test name.", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                saveNGoToNext("analyse");
                analyseReadings();
                //    }
            }
        });
    }
    private static final int REQUEST_COLLECTION_MODEL = 109 ;

    private void analyseReadings() {
        final LinearLayout button = (LinearLayout) findViewById(R.id.analyseNow);
        final ReTestDetailsActivity _this = this;

        List<ScioReading> scioReadings = new ArrayList<>();
        for (ScioReadingWrapper scioReadingWrapper : readings) {
            scioReadings.add(scioReadingWrapper.getScioReading());
        }

        final StatusView statusView = new StatusView(ReTestDetailsActivity.this);
        statusView.setStatusCode(3);
        statusView.setStatusMessage("Analysing Results");
        statusView.show();
        // Change model --> models
        analyzerService.analyze(scioReadings, models, new ScioCloudAnalyzeManyModelCallback() {
            @Override
            public void onSuccess(final Map<ScioReading, HashSet<Model>> models) {
                Log.d("Analyse Result", models.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateScan(models);
                        Toast.makeText(_this, "Analyse complete, sending to FoodScan", Toast.LENGTH_LONG).show();
                        logScan(statusView);

                    }
                });
            }

            @Override
            public void onError(int code, String msg) {
                Log.d("Analyse Result", msg);
                statusView.setStatusCode(0);
                statusView.setBGColor();
                statusView.setStatusMessage("Analyse failed");
                statusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        statusView.hide();
                    }
                });
                //showError(code, msg);
            }
        });
    }
    String test_id ;
    private void saveAnalyseData(String analysedData) {
        final EditText etTestName =  (EditText) findViewById(R.id.etTestName) ;
        final EditText etTestNote =  (EditText) findViewById(R.id.etTestNote) ;
        test_id =  Validation.generateRandomString(15);
        String test_name = etTestName.getText().toString().trim();
        String test_note = etTestNote.getText().toString().trim() ;
        // business Location json
        String test_location = "";
        try {
            test_location = createBusinessJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("FoodScan","Test Location : "+test_location);

        String create_datetime = dateFormat.format(Calendar.getInstance().getTime());
        //create_datetime = create_datetime.replace(" "," at ");
        String expire= "" ;
        String imgs_path = "" ;

        for (String imagePath:listImages) {
            imgs_path = imgs_path+","+imagePath;
        }
        if(!imgs_path.isEmpty())
        {
            imgs_path = imgs_path.substring(1);
        }

        FCDBService fcdbService = new FCDBService(getApplicationContext());
        try {

            String timeStamp  = String.valueOf(Calendar.getInstance().getTime().getTime());
            if (fcdbService.saveTestAnalyse(test_id, test_name, test_note, test_location,this.analysedData.model_ids,
                    this.analysedData.collection_id,analysedData, this.scans,create_datetime, expire, imgs_path,timeStamp)) {

                Log.d("Success","Saved analysed data");
            } else {
                Log.d("Failed","Error in saving analysed data");
            }

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally {
            fcdbService.close();
        }

    }
    //New
    private void updateScan(Map<ScioReading, HashSet<Model>> models) {
        for (Map.Entry<ScioReading,HashSet<Model>> entry : models.entrySet()) {
            HashSet<Model> modelsList = entry.getValue();
            for (Model model :modelsList) {
                model.setCollectionName("");//this.collection.getName());  // 16-02-17
                this.scanBundle.updateScan(entry.getKey(), model);
            }
        }

        final EditText etTestName =  (EditText) findViewById(R.id.etTestName) ;
        final EditText etTestNote =  (EditText) findViewById(R.id.etTestNote) ;
        String loc = etBusinessLocation.getText().toString().trim();
        String test_name = etTestName.getText().toString().trim();
        String test_note = etTestNote.getText().toString().trim() ;


        this.scanBundle.sample.setTestName(test_name);
        this.scanBundle.sample.setTestNote(test_note);

        Business business  = this.scanBundle.sample.business ;
        if(business==null)
        {
            business = new Business();
        }
        business.setAddress(loc);
        this.scanBundle.sample.setBusiness(business) ;
    }

    private void logScan(final StatusView statusView) {
        final ReTestDetailsActivity _this = this;
        Log.d("data", this.scanBundle.toJSON().toString());

        Map<String,String> paths =  new HashMap<>();
        int index=0 ;
        for (String imagePath:listImages) {
            if(!imagePath.trim().isEmpty()) {
                String imageBMS  =  getBitMapString(imagePath);
                paths.put("images["+index+"]",imageBMS);
                index++;
            }
        }
        foodScanService.logScan(this.scanBundle,paths, new FoodScanHandler() {
            @Override
            public void onSuccess(JSONObject object) {
               // saveAnalyseData(object.toString());
                updateToAnalysedData(object.toString());
                statusView.setStatusCode(1);
                statusView.setBGColor();
                statusView.setStatusMessage("Task complete");
                statusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        statusView.hide();
                        showResults();
                    }
                });
                Log.d("FoodScan", object.toString());
                Toast.makeText(_this, "Cloud update complete", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError() {
                statusView.setStatusCode(0);
                statusView.setBGColor();
                statusView.setStatusMessage("FoodScan Error");
                statusView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        statusView.hide();
                        //showResults();
                    }
                });

                Log.d("FoodScan", "Error");
                Toast.makeText(_this, "FoodScan Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showResults() {
        if (test_id != null && !test_id.isEmpty()) {

            int totalScans = scans*models.size() ;
           /* SessionService sessionService = new SessionService();
            sessionService.setScanCount(sessionService.getScanCount() - totalScans) ;*/

            Intent intent = new Intent(ReTestDetailsActivity.this,TestResultsActivity.class);
            intent.putExtra("isEnableRetest",true) ;
            intent.putExtra("test_id",test_id);
            startActivity(intent);
            finish();
        }

    }

    private void setSelectedBusiness(Business business) {
        this.selectedBusiness = business;
        this.scanBundle.sample.business = business;
        etBusinessLocation.setText(business.getAddress()) ;
        final TextView lblTestBusinessName  =  (TextView) findViewById(R.id.lblTestBusinessName);
        final FrameLayout containerBizLocation =  (FrameLayout) findViewById(R.id.containerBizLocation);
        String businessName =  business.getName().trim() ;
        if(businessName !=null && !businessName.isEmpty())
        {
            lblTestBusinessName.setText(businessName);
            containerBizLocation.setVisibility(View.VISIBLE);
        }
        else
        {
            containerBizLocation.setVisibility(View.GONE);
        }
        hideSoftKeyboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(networkReceiver, filter);

    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    private final int PERMISSIONS_REQUEST_CAMERA = 0 ;
    private final int PERMISSIONS_REQUEST_GALLARY = 1 ;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    camFunction();
                } else {

                }
                return;
            }

            case PERMISSIONS_REQUEST_GALLARY: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gallaryFun();
                } else {

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    // [23-02-2017]
    private void updateToAnalysedData(String result) {

        final FCDBService fcdbService = new FCDBService(ReTestDetailsActivity.this);
        String create_datetime = dateFormat.format(Calendar.getInstance().getTime());
        String timeStamp  = String.valueOf(Calendar.getInstance().getTime().getTime());
        if (fcdbService.updateTestScanToAnalyse(this.test_id,result,"","",create_datetime,timeStamp)) {
            ScanStorageService storageService = new ScanStorageService();
            storageService.deleteScanStorage(result);
        }

    }

    private  void gallaryFun(){

        try {

            Intent i = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            Intent gintent = new Intent();
            gintent.setType("image/*");
            gintent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    i,
                    PICK_IMAGE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    e.getMessage(),
                    Toast.LENGTH_LONG).show();
            Log.e(e.getClass().getName(), e.getMessage(), e);
        }
    }
    private void camFunction() {
        // TODO Auto-generated method stub

        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //create parameters for Intent with filename
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image captured by camera");
        //imageUri is the current activity attribute, define and save it for later usage (also in onSaveInstanceState)
        imageUri = getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //create new Intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, PICK_Camera_IMAGE);


    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri selectedImageUri = null;
        String filePath = null;
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImageUri = data.getData();
                    //camScannerApiMethod(selectedImageUri);
                }
                break;
            case PICK_Camera_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    //use imageUri here to access the image
                    selectedImageUri = imageUri;
                    // camScannerApiMethod(selectedImageUri);
                    //*Bitmap mPic = (Bitmap) data.getExtras().get("data");
                    //selectedImageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), mPic, getResources().getString(R.string.app_name), Long.toString(System.currentTimeMillis())));*//*
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "Picture was not taken", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Picture was not taken", Toast.LENGTH_SHORT).show();
                }
                break;
            case 102 :
                if(data!=null) {
                    Business businessResult = (Business) data.getSerializableExtra("business");
                    setSelectedBusiness(businessResult);
                }
                break ;

            case REQUEST_COLLECTION_MODEL :
                if(data!=null) {
                    this.models = data.getParcelableArrayListExtra("models");
                    this.collection = data.getParcelableExtra("collection");
                    this.scanBundle.setCollectionUuid(this.collection.getUuid());
                }
                break ;


        }

        if(selectedImageUri != null){
            try {
                // OI FILE Manager
                String filemanagerstring = selectedImageUri.getPath();

                // MEDIA GALLERY
                String selectedImagePath = getPath(selectedImageUri);

                if (selectedImagePath != null) {
                    filePath = selectedImagePath;
                } else if (filemanagerstring != null) {
                    filePath = filemanagerstring;
                } else {
                    Toast.makeText(getApplicationContext(), "Unknown path",
                            Toast.LENGTH_LONG).show();
                    Log.e("Bitmap", "Unknown path");
                }

                decodeFile(filePath);


            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Internal error",
                        Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA };
        Cursor cursor = ReTestDetailsActivity.this.managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }
    public void decodeFile(String filePath) {
        //Bitmap bp=null;
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

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
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, o2);
        String document_name = String.valueOf(System.currentTimeMillis());
        addImageToGUI(bitmap,filePath);
    }
    private String getBitMapString(String path) {
        String encodedImage = "";
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
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
                byte[] b = baos.toByteArray();
                encodedImage =  Base64.encodeToString(b, Base64.DEFAULT);
            }
        }
        return encodedImage ;
    }
    private void addImageToGUI(Bitmap bitmap, final String filePath) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        final View upload_image_view = inflater.inflate(R.layout.upload_image_view,null) ;
        ImageView uploadImage  = (ImageView) upload_image_view.findViewById(R.id.uploadedImage) ;
//        final FrameLayout fl  = new FrameLayout(getApplicationContext());
//        //fl.setPadding(5,5,5,5);
//        ImageView imageView =  new ImageView(getApplicationContext());
        uploadImage.setImageBitmap(bitmap);
//        imageView.setPadding(5,5,5,5);
//        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(200,200);
//        layoutParams.setMargins(5,5,5,5);
//        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        uploadImage.setContentDescription(filePath);
       // uploadImage.setLayoutParams(layoutParams);
       // fl.addView(imageView);
        //ImageView closeImage  = new ImageView(getApplicationContext());
        ImageView closeImage  = (ImageView) upload_image_view.findViewById(R.id.closeImage) ;
        closeImage.setImageResource(R.drawable.button_remove_photo);
        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addedPhotosContainer.removeView(upload_image_view);
                listImages.remove(filePath);
                --addImagesLimit;
            }
        });
       // fl.addView(closeImage);
       // FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams)closeImage.getLayoutParams() ;
      //  layoutParams1.gravity = Gravity.RIGHT|Gravity.TOP |Gravity.END ;
       // closeImage.setLayoutParams(layoutParams1);
        addedPhotosContainer.addView(upload_image_view,0);
        listImages.add(filePath);
        ++addImagesLimit;
    }

    private void rescan() {
        this.setupSoundPool();
        final ReTestDetailsActivity _this = this;
        final int _soundId =  this.soundId ;
        final  StatusView statusView = new StatusView(ReTestDetailsActivity.this);
        statusView.setStatusCode(2);
        statusView.setStatusMessage("Scanning in progress...");
        statusView.show();
        final int streamId =  (new SessionService().isSoundTurnedOn()) ?soundPool.play(_soundId,1.0f,1.0f,1,-1,1):0;

        new ScanningService().execute(new ScanHandler() {
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
                                _this.readings = scioReadings ;
                                String collectionName = "";
                                EditText etTestName  =  (EditText) _this.findViewById(R.id.etTestName);
                                if(collection!= null)
                                    collectionName =  _this.collection.getName()+"_" ;
                                String test_name = collectionName + Calendar.getInstance().getTime().getTime()+"_"+scans;
                                etTestName.setText(test_name);
                                etTestName.setSelection(etTestName.getText().length());
                                _this.setupBundle();
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
        },this.scans, new ScanUpdateHandler() {
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




    /**********************************
     * Get Current Location and Log it
     * to food scan server
     * ********************************/

    private GoogleApiClient googleApiClient;
    private android.location.Location lastKnownLocation;
    private void setupLocation() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (lastKnownLocation != null) {
            JSONObject rootJsonObject = new JSONObject();

            double lat  = lastKnownLocation.getLatitude() ;
            double lng = lastKnownLocation.getLongitude() ;
            String scioDeviceId  =  FoodScanApplication.getDeviceHandler().getDeviceAddress() ;
            try {
                JSONObject locJObj  = new JSONObject();
                locJObj.put("lat",lat);
                locJObj.put("lng",lng);
                rootJsonObject.put("device",scioDeviceId);
                rootJsonObject.put("location",locJObj) ;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            foodScanService.logSCIO(rootJsonObject, new FoodScanHandler() {
                @Override
                public void onSuccess(JSONObject object) {
                    Log.d("log scan","success");
                }

                @Override
                public void onError() {
                    Log.d("log scan","fail");
                }
            });


        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }
}
