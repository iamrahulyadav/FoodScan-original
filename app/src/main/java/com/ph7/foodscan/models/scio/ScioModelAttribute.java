package com.ph7.foodscan.models.scio;

import com.consumerphysics.android.sdk.model.attribute.ScioAttribute;
import com.google.gson.annotations.SerializedName;
import com.ph7.foodscan.utils.serializing.Exclude;

import java.io.Serializable;

/**
 * Created by craigtweedy on 26/01/2016.
 */

public class ScioModelAttribute implements Serializable {

    private static final long serialVersionUID = 7526472295622779147L;

    public ScioModelAttribute() {

    }

    public enum AttributeType implements Serializable {
        String(0),
        Numeric(1),
        DateTime(2);

        private final int key;

        AttributeType(int key) {
            this.key = key;
        }

        public int getKey() {
            return this.key;
        }

        public static AttributeType fromKey(int key) {
            for(AttributeType type : AttributeType.values()) {
                if(type.getKey() == key) {
                    return type;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            switch (this) {
                case String:
                    return "string";
                case Numeric:
                    return "numeric";
                case DateTime:
                    return "datetime";
            }

            return "N/A";
        }

        private static final long serialVersionUID = 234242515125124L;
    }

    @SerializedName("type")
    private AttributeType attributeType;
    @SerializedName("value")
    private Object attributeValue;
    @SerializedName("units")
    private Object attributeUnit;

    @SerializedName("confidence")
    private Double confidence;

    @Exclude
    private Object modelName;

    @Exclude
    private String modelID;

    public ScioModelAttribute(String model, String modelID, ScioAttribute attribute) {
        this.modelID = modelID;
        this.modelName = model;
        this.attributeType = this.convertAttributeType(attribute.getAttributeType());
        this.attributeValue = attribute.getValue();
        this.attributeUnit = attribute.getUnits();
        this.confidence = attribute.getConfidence();
    }

    private AttributeType convertAttributeType(ScioAttribute.AttributeType attributeType) {
        switch (attributeType) {
            case STRING:
                return AttributeType.String;
            case NUMERIC:
                return AttributeType.Numeric;
            case DATE_TIME:
                return AttributeType.DateTime;
            default:
                return AttributeType.String;
        }
    }

    @Override
    public String toString() {
        switch (this.attributeType) {
            case String:
                return this.attributeValue.toString();
            case Numeric:
                return this.attributeValue.toString() + " " + this.attributeUnit.toString();
            case DateTime:
                return this.attributeValue.toString();
            default:
                return "";
        }
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public String getModelID() {
        return modelID;
    }

    public void setModelID(String modelID) {
        this.modelID = modelID;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }


    public Object getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(Object attributeValue) {
        this.attributeValue = attributeValue;
    }

    public Object getAttributeUnit() {
        return attributeUnit;
    }

    public void setAttributeUnit(Object attributeUnit) {
        this.attributeUnit = attributeUnit;
    }

    public Object getModelName() {
        return modelName;
    }

    public void setModelName(Object modelName) {
        this.modelName = modelName;
    }
}
