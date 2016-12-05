package com.ph7.foodscan.models.ph7;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ph7.foodscan.models.scio.ScioModelAttribute;
import com.ph7.foodscan.utils.AttributeTypeDeserializer;
import com.ph7.foodscan.utils.AttributeTypeSerializer;
import com.ph7.foodscan.utils.ListOfJson;
import com.ph7.foodscan.utils.UTCDateAdapter;
import com.ph7.foodscan.utils.serializing.AnnotationExclusionStrategy;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by craigtweedy on 25/01/2016.
 */
public class ScioCollection implements Parcelable {

    private String uuid;
    private String name;
    private List<ScioCollectionModel> models = new ArrayList<>();

    public ScioCollection(String collectionName) {
        this.name = collectionName;
    }

    private ScioCollection(Parcel in) {
        uuid = in.readString();
        name = in.readString();
        in.readList(this.models, ScioCollectionModel.class.getClassLoader());
    }

    public static final Creator<ScioCollection> CREATOR = new Creator<ScioCollection>() {
        @Override
        public ScioCollection createFromParcel(Parcel in) {
            return new ScioCollection(in);
        }

        @Override
        public ScioCollection[] newArray(int size) {
            return new ScioCollection[size];
        }
    };

    public void addModel(ScioCollectionModel model) {
        if (!this.models.contains(model)) {
            this.models.add(model);
        }
    }

    public String getName() {
        return this.name;
    }

    public List<ScioCollectionModel> getModels() {
        return this.models;
    }

    public List<String> getModelIds() {
        List<String> ids = new ArrayList<>();
        for (ScioCollectionModel model : this.models) {
            ids.add(model.getUuid());
        }
        return ids;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.getUuid());
        dest.writeString(this.getName());
        dest.writeList(this.getModels());
    }

    public static List<ScioCollection> fromJSON(JSONArray json) {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ScioModelAttribute.AttributeType.class, new AttributeTypeDeserializer())
                .registerTypeAdapter(ScioModelAttribute.AttributeType.class, new AttributeTypeSerializer())
                .registerTypeAdapter(Date.class, new UTCDateAdapter())
                .setExclusionStrategies(new AnnotationExclusionStrategy())
                .create();
        return gson.fromJson(json.toString(), new ListOfJson<ScioCollection>(ScioCollection.class));
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
