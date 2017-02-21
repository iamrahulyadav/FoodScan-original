package com.ph7.foodscan.models.scio;

import com.consumerphysics.android.sdk.model.ScioModel;
import com.consumerphysics.android.sdk.model.attribute.ScioAttribute;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by craigtweedy on 19/02/2016.
 */
public class Model {

    @SerializedName("uuid")
    private String id;
    private String name;

    @SerializedName("collection_name")
    private String collectionName;

    private List<ScioModelAttribute> attributes;
    private Double confidence; // Old Version
  // private boolean confidence;
    private ScioModel.Type type;

    private String source ;

    public Model(ScioModel scioModel) {
        this.id = scioModel.getId();
        this.name = scioModel.getName();
        this.collectionName = scioModel.getCollectionName();
        this.type = scioModel.getType();

        if (this.type.equals(ScioModel.Type.CLASSIFICATION)) {
           // this.confidence = scioModel.getConfidence(); // old Version
            //
           if(scioModel.isLowConfidence())
           {
               this.confidence =  .3333333 ;
           }
            else
           {
               this.confidence =  .9999999 ;
           }
           // scioModel.getConfidence()

        } else {
            this.confidence = null;
        }

        this.setModelAttributes(scioModel.getAttributes());

    }

    public Model() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<ScioModelAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ScioModelAttribute> attributes) {
        this.attributes = attributes;
    }

    private void setModelAttributes(List<ScioAttribute> attributes) {
        this.attributes = new ArrayList<ScioModelAttribute>();
        for (ScioAttribute attribute : attributes) {
            this.attributes.add(new ScioModelAttribute(this.getName(), this.getId(), attribute));
        }
    }

    public void addAttributes(List<ScioModelAttribute> attributes) {
        this.attributes.addAll(attributes);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
