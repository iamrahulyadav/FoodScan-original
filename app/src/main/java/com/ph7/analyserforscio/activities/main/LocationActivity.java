package com.ph7.analyserforscio.activities.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.ph7.analyserforscio.R;
import com.ph7.analyserforscio.activities.AppActivity;
import com.ph7.analyserforscio.adapters.SpinnerBusinessAdapter;
import com.ph7.analyserforscio.callbacks.FoodScanHandler;
import com.ph7.analyserforscio.models.ph7.Business;
import com.ph7.analyserforscio.models.ph7.Location;
import com.ph7.analyserforscio.services.FoodScanService;
import com.ph7.analyserforscio.services.SessionService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationActivity extends AppActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    ListView listViewNearByLocations ;
    SpinnerBusinessAdapter businessAdapter ;
    List<Business> nearbyBusinesses ;
    private SessionService sessionService ;
    private GoogleApiClient googleApiClient;
    private android.location.Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        listViewNearByLocations = (ListView) findViewById(R.id.listViewNearByLocations);
        listViewNearByLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               Business business =  nearbyBusinesses.get(i);
                Intent result = new Intent();
                result.putExtra("business",business);
                setResult(200,result);
                finish();
            }
        });


        setupLocation();
        setupBusinessList();
    }
    private void setupLocation() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void setupBusinessList() {
        nearbyBusinesses = new ArrayList<>();
        this.businessAdapter =  new SpinnerBusinessAdapter(getApplicationContext(),R.layout.simple_details_item,nearbyBusinesses);
        listViewNearByLocations.setAdapter(businessAdapter);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (lastKnownLocation != null) {
            this.reloadBusinesses();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private FoodScanService foodScanService = new FoodScanService();
    private void reloadBusinesses() {

        if(sessionService ==null || sessionService.getUsername() == null || sessionService.getUserToken().trim().isEmpty())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
            builder.setMessage("Food Scan Login required.");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                    dialogInterface.dismiss();
                }
            });
            builder.create() ;
            builder.show() ;
            return ;
        }


        this.foodScanService.getBusinesses(this.lastKnownLocation, new FoodScanHandler() {
            @Override
            public void onSuccess(JSONObject object) {
                try {
                    JSONArray jsonBusinesses = object.getJSONArray("businesses");
                    List<Business> businesses = new ArrayList<>();

                    for (Integer i=0; i < jsonBusinesses.length(); i++) {
                        JSONObject jsonBusiness = jsonBusinesses.getJSONObject(i);

                        Business business = new Business();
                        business.setId(jsonBusiness.getInt("id"));
                        business.setName(jsonBusiness.getString("name"));
                        business.setAddress(jsonBusiness.getString("address"));
                        JSONObject jsonLocation = jsonBusiness.getJSONObject("location");
                        business.setLocation(new Location(jsonLocation.getDouble("lat"), jsonLocation.getDouble("lng")));
                        businesses.add(business);
                    }
                    nearbyBusinesses = businesses ;
                } catch (JSONException e) {
                    nearbyBusinesses = new ArrayList<Business>();
                }

                businessAdapter.addAll(nearbyBusinesses);
                //TODO: Change this so it's not defaulting the selected business to the first one returned
//                if (_this.nearbyBusinesses.size() > 0) {
//                    _this.setSelectedBusiness(_this.nearbyBusinesses.get(0));
//                }

            }

            @Override
            public void onError() {
                regenerateTokenRequest();
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
                            reloadBusinesses();
                        } catch (JSONException e) { }
                    }
                    @Override
                    public void onError() {}
                });
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
