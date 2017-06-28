package com.ph7.foodscan.services.interfaces;

import android.location.Location;

import com.ph7.foodscan.callbacks.FoodScanHandler;
import com.ph7.foodscan.models.ph7.ScanBundle;
import com.ph7.foodscan.models.ph7.ScioTestResults;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by craigtweedy on 18/04/2016.
 */
public interface FoodScanServiceInterface {
    void login(Map<String, String> params, FoodScanHandler handler);
    void logScan(ScioTestResults testResults, FoodScanHandler handler);
    void logScan(ScanBundle scanBundle, FoodScanHandler handler);
    void logSCIO(JSONObject jsonObject, FoodScanHandler handler);
    void getBusinesses(Location location, FoodScanHandler handler);
    void getCollections(FoodScanHandler handler);
    void searchForImages(String collectionId, String modelId, String value, FoodScanHandler handler);
}
