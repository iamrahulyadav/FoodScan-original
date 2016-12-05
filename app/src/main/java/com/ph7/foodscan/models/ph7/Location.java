package com.ph7.foodscan.models.ph7;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by craigtweedy on 11/05/2016.
 */
public class Location implements Serializable {

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    public Location(double lat, double lng) {
        this.setLatitude(lat);
        this.setLongitude(lng);
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
