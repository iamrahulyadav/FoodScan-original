package com.ph7.foodscan.models.ph7;

import java.util.UUID;

/**
 * Created by craigtweedy on 16/06/2016.
 */
public class Sample {

    String uuid;

    public void setTestName(String testName) {
        this.name = testName;
    }

    public void setTestNote(String testNote) {
        this.notes = testNote;
    }

    private String  name ;
    private String notes ;
    public Business business;

    public Sample() {
        this.uuid = UUID.randomUUID().toString();
    }

    public void setBusiness(Business business) {
        this.business = business;
    }



}
