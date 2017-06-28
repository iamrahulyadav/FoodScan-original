package com.ph7.foodscan.activities.scan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.consumerphysics.android.sdk.callback.cloud.ScioCloudModelsCallback;
import com.consumerphysics.android.sdk.model.ScioModel;
import com.ph7.foodscan.R;
import com.ph7.foodscan.activities.AppActivity;
import com.ph7.foodscan.adapters.SpinnerSCIOCollectionAdapter;
import com.ph7.foodscan.adapters.SpinnerSCIOModelAdapter;
import com.ph7.foodscan.callbacks.FoodScanHandler;
import com.ph7.foodscan.models.ph7.ScioCollection;
import com.ph7.foodscan.models.ph7.ScioCollectionModel;
import com.ph7.foodscan.services.FoodScanService;
import com.ph7.foodscan.services.SessionService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CollectionModelSelectionActivity extends AppActivity {

    private LinearLayout selectedModelsContainer ;
    private ScioCollectionModel model;
    private ScioCollection collection;
    private SessionService sessionService = new SessionService();
    private FoodScanService foodScanService = new FoodScanService();
    private List<ScioCollection> collections = new ArrayList<>();
    private List<ScioCollectionModel> models = new ArrayList<>();
    private SpinnerSCIOCollectionAdapter collectionAdapter;
    private SpinnerSCIOModelAdapter modelAdapter;
    private Spinner collectionList;
    private Spinner modelList;
    private ArrayList<ScioCollectionModel> multipleModels = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_model_selection);

        setActionBarOverlayZero();

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
                   // checkBlankFieldValidation();
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
                    multipleModels.add(model);
                    addAnotherModelCollection();
                    checkBlankFieldValidation();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //this.getCollections();// [Latest]
        this.getModels();
        this.setupDoneButton();
    }

    private void setupDoneButton() {
        final TextView startTestButton = (TextView) findViewById(R.id.startTest);
        startTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent result = new Intent();
                result.putParcelableArrayListExtra("models", multipleModels);
                //result.putExtra("collection", collection);
                setResult(200,result);
                finish();
            }
        });
    }



    private void getModels() {
        if(sessionService ==null || sessionService.getUserToken() ==null|| sessionService.getUserToken().trim().isEmpty())
        {
            return ;
        }

        //get Collection n=and model from CP server for Specific User login
        // getCollectionFromCPForSpecificUser();

        // get Collection From Food Scan Server
        this.foodScanService.getModels(new FoodScanHandler() {
            @Override
            public void onSuccess(JSONObject object) {

                modelAdapter.clear();
                ScioCollectionModel scioCollectionModel = new ScioCollectionModel("Choose a model");
                modelAdapter.add(scioCollectionModel);
                selectedModelsContainer.removeAllViews();
                multipleModels.clear();
                checkBlankFieldValidation();

                try {
                    JSONArray modelsJsonArr  = object.getJSONArray("models");
                    for (int indexModelJsonObj = 0; indexModelJsonObj < modelsJsonArr.length(); indexModelJsonObj++) {
                        JSONObject modelJsonObj  =  modelsJsonArr.getJSONObject(indexModelJsonObj) ;
                        String uuid = modelJsonObj.getString("uuid");
                        String name  = modelJsonObj.getString("name");
                        String src = modelJsonObj.getString("source");
                        String type  =  modelJsonObj.getString("type");
                        ScioCollectionModel scioModel = new ScioCollectionModel(name,uuid,type,src);
                        modelAdapter.add(scioModel);
                        models.add(scioModel);
                    }
                } catch (JSONException e) {
                    models = new ArrayList<>();
                }
                modelAdapter.notifyDataSetChanged();

            }

            @Override
            public void onError() {
                getModels();
            }


        });

    }

    private void getCollectionFromCPForSpecificUser() {

        this.scioCloud.getModels(new ScioCloudModelsCallback() {
            @Override
            public void onSuccess(List<ScioModel> list) {

                HashMap<String,ScioCollection> modelHashMap = new HashMap<>();
                for (ScioModel scioModel : list) {
                    String collName  = scioModel.getCollectionName() ;
                    if(modelHashMap.containsKey(collName)) {
                        ScioCollectionModel scioCollectionModel = new ScioCollectionModel(scioModel.getName(),scioModel.getId(),scioModel.getType().toString().toLowerCase());
                        modelHashMap.get(collName).addModel(scioCollectionModel);
                    }
                    else {
                        ScioCollection scioCollection = new ScioCollection(collName);
                        String uuid  = UUID.randomUUID().toString() ;
                        scioCollection.setUuid(uuid);
                        ScioCollectionModel scioCollectionModel = new ScioCollectionModel(scioModel.getName(),scioModel.getId(),scioModel.getType().toString().toLowerCase());
                        scioCollection.addModel(scioCollectionModel);
                        modelHashMap.put(collName,scioCollection);
                    }
                }

                collections = new ArrayList<>();
                collectionAdapter.clear();
                ScioCollection scioCollection =  new ScioCollection("Choose a collection");
                collectionAdapter.add(scioCollection);
                Iterator it = modelHashMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    collections.add((ScioCollection) pair.getValue()) ;
                    it.remove(); // avoids a ConcurrentModificationException
                }
                collectionAdapter.addAll(collections);
                collectionAdapter.notifyDataSetChanged();
                collectionList.setSelection(0);
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }



    private void addAnotherModelCollection() {

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
                    multipleModels.add(model);
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
    }
    private void checkBlankFieldValidation() {

        final TextView startTestButton = (TextView) findViewById(R.id.startTest);

        if(this.modelList.getSelectedItemPosition()>0 )
        {
            startTestButton.setEnabled(true);
        }
        else
        {
            startTestButton.setEnabled(false);

        }

    }


}
