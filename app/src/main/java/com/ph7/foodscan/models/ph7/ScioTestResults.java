package com.ph7.foodscan.models.ph7;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.ph7.foodscan.models.scio.Model;
import com.ph7.foodscan.models.scio.ScioModelAttribute;
import com.ph7.foodscan.utils.AttributeTypeDeserializer;
import com.ph7.foodscan.utils.AttributeTypeSerializer;
import com.ph7.foodscan.utils.UTCDateAdapter;
import com.ph7.foodscan.utils.serializing.AnnotationExclusionStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by craigtweedy on 26/01/2016.
 */
public class ScioTestResults implements Serializable {

    private static final long serialVersionUID = 12487189247918247L;

    private Map<String, ScioAttributeResults> attributes = new TreeMap<>();
    private List<ScioModelImage> images = new ArrayList<>();

    @SerializedName("collection_uuid")
    private String collectionUuid;

    @SerializedName("collection_name")
    private String collectionName;

    @SerializedName("date_scanned")
    private Date scannedDate;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("longitude")
    private String longitude;

    @SerializedName("business")
    private Business business;

    public ScioTestResults() {

    }

    public String getCollectionName() {
        return collectionName;
    }

    public void addModelAttributes(Model model) {
        for (ScioModelAttribute attribute : model.getAttributes()) {
            this.attributes.put(model.getName(), new ScioAttributeResults(attribute, model.getConfidence()));
        }
    }

    public String displayAttributes() {
        String attributeString = "";

        for (Object o : this.attributes.entrySet()) {
            Map.Entry<String, ScioAttributeResults> pair = (Map.Entry<String, ScioAttributeResults>) o;
            String key = pair.getKey();
            String value = pair.getValue().getAttribute().toString();
            double confidence = new BigDecimal(pair.getValue().getConfidence()).setScale(2, RoundingMode.HALF_UP).doubleValue() * 100;
            attributeString = attributeString + key + " : " + value + " / Confidence: " + confidence + "%\n";
        }

        return attributeString;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setCollectionUuid(String collectionUuid) {
        this.collectionUuid = collectionUuid;
    }

    public String getCollectionUuid() {
        return this.collectionUuid;
    }

    public void setScannedDate(Date scannedDate) {
        this.scannedDate = scannedDate;
    }

    public Date getScannedDate() {
        return scannedDate;
    }

    public void setImages(List<ScioModelImage> images) {
        this.images = images;
    }

    public List<ScioModelImage> getImages() {
        return this.images;
    }

    public void addImage(ScioModelImage image) {
        this.images.add(image);
    }

    public void addImages(List<ScioModelImage> images) { this.images.addAll(images); }

    public void removeImage(ScioModelImage image) {
        this.images.remove(image);
    }

    public void removeImages(List<ScioModelImage> images) { this.images.removeAll(images); }

    private Map<String, ScioAttributeResults> getAttributes() {
        return attributes;
    }

    public List<ScioAttributeResults> getAttributesAsList() {
        Iterator it = this.getAttributes().entrySet().iterator();
        List<ScioAttributeResults> attributes = new ArrayList<ScioAttributeResults>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            attributes.add((ScioAttributeResults) pair.getValue());
        }
        return attributes;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public Business getBusiness() {
        return this.business;
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
            json.put("attributes", new JSONArray(gson.toJson(this.getAttributesAsList())));
        } catch (JSONException e) {
            return new JSONObject();
        }

        return json;
    }
}
