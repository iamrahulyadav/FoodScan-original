package com.ph7.foodscan.models.ph7;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ph7.foodscan.models.scio.ScioModelAttribute;
import com.ph7.foodscan.utils.AttributeTypeDeserializer;
import com.ph7.foodscan.utils.AttributeTypeSerializer;
import com.ph7.foodscan.utils.ListOfJson;
import com.ph7.foodscan.utils.UTCDateAdapter;
import com.ph7.foodscan.utils.serializing.AnnotationExclusionStrategy;

import org.json.JSONArray;

import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * Created by craigtweedy on 17/05/2016.
 */
public class ScioModelImage {

    private Integer width;
    private Integer height;
    private String type;
    private URL url;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public static List<ScioModelImage> fromJSON(JSONArray json) {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ScioModelAttribute.AttributeType.class, new AttributeTypeDeserializer())
                .registerTypeAdapter(ScioModelAttribute.AttributeType.class, new AttributeTypeSerializer())
                .registerTypeAdapter(Date.class, new UTCDateAdapter())
                .setExclusionStrategies(new AnnotationExclusionStrategy())
                .create();
        return gson.fromJson(json.toString(), new ListOfJson<ScioModelImage>(ScioModelImage.class));
    }
}
