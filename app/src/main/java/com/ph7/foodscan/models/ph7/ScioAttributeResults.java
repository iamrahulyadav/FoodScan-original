package com.ph7.foodscan.models.ph7;

import com.ph7.foodscan.models.scio.ScioModelAttribute;

/**
 * Created by craigtweedy on 03/06/2016.
 */
public class ScioAttributeResults {

    private ScioModelAttribute attribute;
    private double confidence;

    ScioAttributeResults(ScioModelAttribute attribute, double confidence) {
        this.attribute = attribute;
        this.confidence = confidence;
    }

    public ScioModelAttribute getAttribute() {
        return this.attribute;
    }

    public void setAttribute(ScioModelAttribute attribute) {
        this.attribute = attribute;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

}
