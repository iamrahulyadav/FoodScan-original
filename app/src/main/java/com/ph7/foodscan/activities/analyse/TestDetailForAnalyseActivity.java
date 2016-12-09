package com.ph7.foodscan.activities.analyse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.consumerphysics.android.sdk.model.ScioReading;
import com.ph7.foodscan.R;
import com.ph7.foodscan.activities.AppActivity;
import com.ph7.foodscan.activities.scan.CollectionModelSelectionActivity;
import com.ph7.foodscan.application.FoodScanApplication;
import com.ph7.foodscan.callbacks.FoodScanHandler;
import com.ph7.foodscan.callbacks.ScioCloudAnalyzeManyModelCallback;
import com.ph7.foodscan.models.ph7.Business;
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
import com.ph7.foodscan.services.SessionService;
import com.ph7.foodscan.services.interfaces.AnalyzerInterface;
import com.ph7.foodscan.utils.Validation;
import com.ph7.foodscan.views.StatusView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TestDetailForAnalyseActivity extends AppActivity {

    SCIOResultDataModel scanResults ;

    List<ScioReadingWrapper> scioReadingWrappers =  new ArrayList<>();
    private FoodScanService foodScanService = new FoodScanService();
    private ArrayList<ScioCollectionModel> models ;
    private AnalyzerInterface analyzerService = FoodScanApplication.getAnalyzerService();
    List<ScioReading> scioReadings = new ArrayList<>();
    private String collectionID  =  "";
    private String collectionName ="";
    private ScanBundle scanBundle;

    String testId =  "" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_detail_for_analyse);

        Intent intent = getIntent();
        scanResults = (SCIOResultDataModel) intent.getSerializableExtra("scan_result");
        testId = scanResults.test_id ;
        setupHeader() ;
        setupModels();
        setupCollection();
        setupScioReadings();
        setupBundle();
        setupAnalyseNowButton();
        setActionBarOverlayZero();

    }
    private void setupBundle()  {
        this.scanBundle = new ScanBundle();
        this.scanBundle.sample = new Sample();
        this.scanBundle.setCollectionUuid(collectionID);
        Business business ;
        try {
            business = setBusinessData();
        } catch (JSONException e) {
            e.printStackTrace();
            business = new Business();
        }
        // business.setName(scanResults.test_location);
        this.scanBundle.sample.setBusiness(business);
        for (ScioReadingWrapper scioReading: this.scioReadingWrappers) {
            Scan scan = new Scan(scioReading);
            this.scanBundle.addScan(scan);
        }
    }

    private Business setBusinessData() throws JSONException {
        Business business =  new Business();
        String test_location_data  =  scanResults.test_location ;
        if(!test_location_data.isEmpty())
        {
            JSONObject rootObj  =  new JSONObject(test_location_data);
            String name  =  rootObj.getString("name");
            String address  =  rootObj.getString("address");
            business.setName(name);
            business.setAddress(address);
            int loc_id  =  rootObj.getInt("id");
            if(loc_id != -1)
            {
                business.setId(loc_id);
            }
            JSONObject locObj  =  rootObj.getJSONObject("location");

            if(locObj!=null)
            {

                double lat =  locObj.getDouble("lat");
                double lng  = locObj.getDouble("lng");
                if(lat!=0.0 || lng!=0.0)
                {
                    Location location = new Location(lat,lng);
                    business.setLocation(location);
                }
            }
            final TextView tvTestBusinessLocation =  (TextView) findViewById(R.id.tvTestBusinessLocation) ;
            StringBuilder testLocation  = new StringBuilder();
            if(name!=null && !name.isEmpty())
                testLocation.append(name+"\n");
            testLocation.append(address);
            tvTestBusinessLocation.setText(testLocation.toString());
        }
        return business ;
    }

    private void setupScioReadings() {
        String path  = scanResults.test_scan_result ;
        ScanStorageService scanStorageService =  new ScanStorageService();
        scanStorageService.loadScansFromStorage(path) ;
        scioReadings = scanStorageService.getScioReadings() ;
        // SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm", Locale.US);
        Date create_datetime = null ;
        for (ScioReading scioReading:scioReadings) {

            try {
                create_datetime = dateFormat.parse(scanResults.create_datetime);
            } catch (ParseException e) {
                create_datetime = new Date();
                e.printStackTrace();
            }
            ScioReadingWrapper scioReadingWrapper = new ScioReadingWrapper(create_datetime,scioReading);
            scioReadingWrappers.add(scioReadingWrapper);
        }
    }

    private void setupAnalyseNowButton() {
        final Button buttonAnalyseNow = (Button) findViewById(R.id.analyseNow);
        final TestDetailForAnalyseActivity _this = this;
        // Set Test Notes
        final TextView tvTestNotes =  (TextView)findViewById(R.id.tvTestNote);
        tvTestNotes.setText(scanResults.test_note);
        buttonAnalyseNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!Validation.isNetworkConnected(TestDetailForAnalyseActivity.this))
                {
                    Snackbar.make(view, "No internet connection!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(!isLogin())
                {
                    return ;
                }
                if(checkValidation()) {
                    final StatusView statusView = new StatusView(TestDetailForAnalyseActivity.this);
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
                                    buttonAnalyseNow.setText("Analyse Now");
                                    updateScan(models);
                                    statusView.setStatusMessage("Sending to FoodScan");
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
                            Toast.makeText(_this, "Analyse failed", Toast.LENGTH_SHORT);
                            // showError(code, msg);
                        }
                    });
                }
            }
        });

    }
    private static final int REQUEST_COLLECTION_MODEL = 109 ;
    private boolean checkValidation() {

        if(models.size()<=0 && collectionID.isEmpty()) {
            Intent intent = new Intent(TestDetailForAnalyseActivity.this, CollectionModelSelectionActivity.class);
            intent.putExtra("test_note",scanResults.test_note);
            startActivityForResult(intent,REQUEST_COLLECTION_MODEL);
            return false ;
        }


        return true  ;
    }
    // New
    private void updateScan(Map<ScioReading, HashSet<Model>> models) {
        for (Map.Entry<ScioReading,HashSet<Model>> entry : models.entrySet()) {
            HashSet<Model> modelsList = entry.getValue();
            for (Model model :modelsList) {
                model.setCollectionName(collectionName);
                this.scanBundle.updateScan(entry.getKey(), model);
            }
        }
        this.scanBundle.sample.setTestName(scanResults.test_name);
        this.scanBundle.sample.setTestNote(scanResults.test_note);

        // Set Test Notes
        final TextView tvTestNotes =  (TextView)findViewById(R.id.tvTestNote);
        tvTestNotes.setText(scanResults.test_note);

    }

    private void setupCollection() {
        TextView tvCollectionName = (TextView)  findViewById(R.id.tvCollectionName);
        final String collection_id =  scanResults.collection_id ;
        if(!collection_id.isEmpty()) {
            try {
                JSONObject jObj = new JSONObject(collection_id);
                collectionName = jObj.getString("name");
                collectionID = jObj.getString("uuid");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        tvCollectionName.setText(collectionName);
    }

    private void setupModels() {
        LinearLayout selectedModelsContainer = (LinearLayout)findViewById(R.id.selectedModelsContainer);
        selectedModelsContainer.removeAllViews();
        models = new ArrayList<>();
        try {
            JSONArray modelsArr  =  new JSONArray(scanResults.model_ids) ;
            int modelArrLen  =  modelsArr.length() ;
            if(modelArrLen>0)
            {
                for (int indexModel = 0; indexModel < modelArrLen; indexModel++) {
                    JSONObject jObj = modelsArr.getJSONObject(indexModel);
                    String modelName =  jObj.getString("name") ;
                    String modelId =  jObj.getString("uuid") ;
                    TextView modelView =  new TextView(getApplicationContext());
                    modelView.setText(modelName);
                    modelView.setTextSize(20f);
                    modelView.setTextColor(Color.parseColor("#484843"));
                    selectedModelsContainer.addView(modelView);
                    ScioCollectionModel scioCollectionModel =  new ScioCollectionModel(modelName,modelId);
                    models.add(scioCollectionModel);
                }
            }
            else
            {

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupHeader() {
        ImageView ivScanImages = (ImageView) findViewById(R.id.ivScanImages);
        TextView tvTestName = (TextView) findViewById(R.id.tvTestName);
        TextView testCreatedDate = (TextView) findViewById(R.id.testCreatedDate);
        tvTestName.setText(scanResults.test_name);
        testCreatedDate.setText("Tested "+scanResults.create_datetime);

        String imgs_path  =  scanResults.imgs_path;
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

    private void logScan(final StatusView statusView) {
        final TestDetailForAnalyseActivity _this = this;
        Map<String,String> paths =  new HashMap<>();

        String[] imgs = scanResults.imgs_path.split(",");
        int index = 0 ;
        for (int indexPath = 0; indexPath < imgs.length; indexPath++) {
            String path = imgs[indexPath];
            if(!path.trim().isEmpty()) {
                String imageBMS  =  getBitMapString(path);
                paths.put("images["+index+"]",imageBMS);
                index++ ;
            }
        }

        foodScanService.logScan(this.scanBundle,paths, new FoodScanHandler() {
            @Override
            public void onSuccess(JSONObject object) {
                final FCDBService fcdbService = new FCDBService(TestDetailForAnalyseActivity.this);
                String create_datetime = dateFormat.format(Calendar.getInstance().getTime());
                String timeStamp  = String.valueOf(Calendar.getInstance().getTime().getTime());
                if (fcdbService.updateTestScanToAnalyse(testId, object.toString(),scanResults.model_ids,scanResults.collection_id,create_datetime,timeStamp)) {
                    ScanStorageService storageService = new ScanStorageService();
                    storageService.deleteScanStorage(scanResults.test_scan_result);
                }

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
                Log.d("Food Scan", object.toString());
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
                        showResults();
                    }
                });
                Log.d("FoodScan", "Error");
                Toast.makeText(_this, "FoodScan Error", Toast.LENGTH_LONG).show();
            }
        });
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

    private void showResults() {
        if (testId != null && !testId.isEmpty()) {

         //   int totalScans = scanResults.scan_count*models.size() ;
            // SessionService sessionService = new SessionService();
            ///  sessionService.setScanCount(sessionService.getScanCount() - totalScans) ;

            Intent intent = new Intent(TestDetailForAnalyseActivity.this,TestResultsActivity.class);
            intent.putExtra("isEnableRetest",false) ;
            intent.putExtra("test_id",testId);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== REQUEST_COLLECTION_MODEL)
        {
            if(data!=null)
            {
                final ArrayList<ScioCollectionModel> models = data.getParcelableArrayListExtra("models");
                ScioCollection collection = data.getParcelableExtra("collection");
                try {
                    scanResults.collection_id  = createCollectionJson(collection);
                    scanResults.model_ids = createModelsJson(models);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setupModels();
                setupCollection();
                this.scanBundle.setCollectionUuid(collection.getUuid());
            }
        }
    }

    private String createCollectionJson(ScioCollection collection) throws JSONException {
        String jsonStr = "" ;
        JSONObject root = new JSONObject();

        ScioCollection modelCollection  =  collection ;
        if(modelCollection!=null) {
            root.put("name", modelCollection.getName());
            root.put("uuid", modelCollection.getUuid());
            jsonStr =  root.toString() ;
        }

        return jsonStr ;
    }

    private String createModelsJson(final ArrayList<ScioCollectionModel> models) throws JSONException {
        JSONArray root =  new JSONArray();

        for (ScioCollectionModel scioCollectionModel : models) {
            JSONObject modelJsonObj  =  new JSONObject();
            modelJsonObj.put("name",scioCollectionModel.getName());
            modelJsonObj.put("uuid", scioCollectionModel.getUuid());

            root.put(modelJsonObj);
        }
        return root.toString() ;
    }
}
