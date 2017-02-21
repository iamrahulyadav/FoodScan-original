package com.ph7.foodscan.models.ph7;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by craigtweedy on 25/01/2016.
 */
public class ScioCollectionModel implements Parcelable {

    private String uuid;
    private String name;
    private String type;
    private String source;
    private Float fitness= 0.0f ;
    private List<String> attributes;

    public ScioCollectionModel(Parcel in) {
        uuid = in.readString();
        name = in.readString();
        type = in.readString();
        source =  in.readString() ;
        fitness = in.readFloat();
        attributes = new ArrayList<>();
        in.readStringList(attributes);
    }
    public ScioCollectionModel(String modelName)
    {
        this.name =  modelName ;
    }

    public ScioCollectionModel(String modelName,String uuid)
    {
        this.name = modelName ;
        this.uuid =  uuid ;
    }
    public ScioCollectionModel(String modelName,String uuid,String type)
    {
        this.name = modelName ;
        this.uuid =  uuid ;
        this.type =  type;
        this.attributes =  new ArrayList<>();
    }

    public ScioCollectionModel(String modelName,String uuid,String type,String src)
    {
        this.name = modelName ;
        this.uuid =  uuid ;
        this.type =  type;
        this.attributes =  new ArrayList<>();
        this.source =  src ;
    }


    public static final Creator<ScioCollectionModel> CREATOR = new Creator<ScioCollectionModel>() {
        @Override
        public ScioCollectionModel createFromParcel(Parcel in) {
            return new ScioCollectionModel(in);
        }

        @Override
        public ScioCollectionModel[] newArray(int size) {
            return new ScioCollectionModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeString(this.name);
        dest.writeString(this.type);
        dest.writeString(this.source);
        dest.writeFloat(this.fitness);
        dest.writeStringList(this.attributes);
    }

    public String getSrc() {
        return this.source;
    }

    public String getName() {
        return this.name;
    }

    public String getUuid() {
        return this.uuid;
    }

    public List<String> getAttributes() { return this.attributes; }
}
