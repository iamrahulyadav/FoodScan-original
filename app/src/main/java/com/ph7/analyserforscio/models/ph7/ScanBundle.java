package com.ph7.analyserforscio.models.ph7;

import com.consumerphysics.android.sdk.model.ScioReading;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ph7.analyserforscio.models.scio.Model;
import com.ph7.analyserforscio.models.scio.ScioModelAttribute;
import com.ph7.analyserforscio.utils.AttributeTypeDeserializer;
import com.ph7.analyserforscio.utils.AttributeTypeSerializer;
import com.ph7.analyserforscio.utils.UTCDateAdapter;
import com.ph7.analyserforscio.utils.serializing.AnnotationExclusionStrategy;
import com.ph7.analyserforscio.utils.serializing.Exclude;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by craigtweedy on 16/06/2016.
 */
public class ScanBundle {

    List<Scan> scans = new ArrayList<>();
    public Sample sample;

    @Exclude
    private String collectionUuid;

    public void addScan(Scan scan) {
        this.scans.add(scan);
    }

    public void updateScan(ScioReading key, Model value) {
        Scan foundScan = null;
        for (Scan scan : this.scans) {
            if (scan.scioReading.equals(key)) {
                foundScan = scan;
                break;
            }
        }

        if (foundScan == null) {
            return;
        }

        //foundScan.updateModel(value); // old
        foundScan.addModel(value); // new
    }
    public void updateScan(ScioReading key, Set<Model> values) {
        Scan foundScan = null;
        for (Scan scan : this.scans) {
            if (scan.scioReading.equals(key)) {
                foundScan = scan;
                break;
            }
        }

        if (foundScan == null) {
            return;
        }

       foundScan.models =  values ;

    }

    public String displayAttributes() {

        String attributeString = "";
        for (int i = 0; i < this.scans.size(); i++) {
            Scan scan = this.scans.get(i);
            attributeString += String.format("Scan %d \n\n", i+1);

            List<Model> modelList = new ArrayList<>(scan.models);
            for (Model model : modelList) {
                List<ScioModelAttribute> attributes = model.getAttributes();
                if (attributes.size() > 0) {
                    attributeString += model.getName() + ": " + model.getAttributes().get(0).toString();
                }

                if (modelList.get(modelList.size()-1).equals(model)) {
                    attributeString += "\n\n";
                }
            }
        }

        return attributeString;
    }

    public void setCollectionUuid(String collectionUuid) {
        this.collectionUuid = collectionUuid;
    }

    public String getCollectionUuid() {
        return collectionUuid;
    }

    public JSONObject toJSON() {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ScioModelAttribute.AttributeType.class, new AttributeTypeDeserializer())
                .registerTypeAdapter(ScioModelAttribute.AttributeType.class, new AttributeTypeSerializer())
                .registerTypeAdapter(Date.class, new UTCDateAdapter())
                .setExclusionStrategies(new AnnotationExclusionStrategy())
                .create();
        JSONObject json = null;
        try {
            json = new JSONObject(gson.toJson(this));
        } catch (JSONException e) {
            return new JSONObject();
        }

        return json;
    }
}
