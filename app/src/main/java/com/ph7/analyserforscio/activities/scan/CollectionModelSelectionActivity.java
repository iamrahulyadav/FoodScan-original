package com.ph7.analyserforscio.activities.scan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.ph7.analyserforscio.R;
import com.ph7.analyserforscio.activities.AppActivity;
import com.ph7.analyserforscio.adapters.SpinnerSCIOCollectionAdapter;
import com.ph7.analyserforscio.adapters.SpinnerSCIOModelAdapter;
import com.ph7.analyserforscio.callbacks.FoodScanHandler;
import com.ph7.analyserforscio.models.ph7.ScioCollection;
import com.ph7.analyserforscio.models.ph7.ScioCollectionModel;
import com.ph7.analyserforscio.services.FoodScanService;
import com.ph7.analyserforscio.services.SessionService;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        this.getCollections();
        this.setupDoneButton();
    }

    private void setupDoneButton() {
        final TextView startTestButton = (TextView) findViewById(R.id.startTest);
        startTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent result = new Intent();
                result.putParcelableArrayListExtra("models", multipleModels);
                result.putExtra("collection", collection);
                setResult(200,result);
                finish();
            }
        });
    }


    private void getCollections() {

        this.foodScanService.getCollections(new FoodScanHandler() {
            @Override
            public void onSuccess(JSONObject object) {
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
                    regenerateTokenRequest() ;
            }
        });
    }

    private void regenerateTokenRequest() {
        Log.d("Token","Regenerated");
        Map<String, String> params = new HashMap<>();
        params.put("email",sessionService.getUsername());
        params.put("password", sessionService.getPassword());
        foodScanService.login(params, new FoodScanHandler() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    sessionService.setUserToken(jsonObject.getString("token"));
                    getCollections();
                } catch (JSONException e) { }
            }
            @Override
            public void onError() {}
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

        if(this.modelList.getSelectedItemPosition()>0 && collectionList.getSelectedItemPosition()>0)
        {
            startTestButton.setEnabled(true);
        }
        else
        {
            startTestButton.setEnabled(false);

        }

    }


}
