package com.ph7.foodscan.models.ph7;

import com.consumerphysics.android.sdk.model.ScioReading;
import com.google.gson.annotations.SerializedName;
import com.ph7.foodscan.models.scio.Model;
import com.ph7.foodscan.utils.serializing.Exclude;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by craigtweedy on 16/06/2016.
 */
public class Scan  {

    @SerializedName("datetime")
    Date scannedAt;

    Location location;
    Set<Model> models = new HashSet<>();

    @Exclude
    ScioReading scioReading;

    public Scan(ScioReadingWrapper scioReadingWrapper) {
        this.scioReading = scioReadingWrapper.scioReading;
        this.scannedAt = scioReadingWrapper.scannedAt;
    }

    public void updateModel(Model value) {
        Model foundModel = null;
        for (Model model : models) {
            if (model.getId().equals(value.getId())) {
                foundModel = model;
                break;
            }
        }

        if (foundModel != null) {
            this.models.remove(foundModel);
        }

        this.models.add(value);
    }

    public void addModel(Model value) {
        this.models.add(value);
    }
}
