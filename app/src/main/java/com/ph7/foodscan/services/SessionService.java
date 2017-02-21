package com.ph7.foodscan.services;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ph7.foodscan.application.FoodScanApplication;

import java.util.Calendar;

/**
 * Created by craigtweedy on 19/04/2016.
 */
public class SessionService {

    public String getUsername() {
        return this.preferences.getString("username", null);
    }

    public void setUsername(String username) {
        this.preferences.edit().putString("username", username).commit();
    }

    public String getPassword() {
        return this.preferences.getString("password", null);
    }

    public void setPassword(String password) {
        this.preferences.edit().putString("password", password).commit();
    }

    public void setScanCount(int scans)
    {
        this.preferences.edit().putInt("scans",scans).commit() ;
    }
    public int getScanCount()
    {
        return this.preferences.getInt("scans",0) ;
    }

    private SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FoodScanApplication.getAppContext());

    public boolean isUserLoggedInToFoodScan() {
        return this.getUserToken() != null;
    }

    public String getUserToken() {
        return this.preferences.getString("AnalyserForSCIOToken", null);
    }

    public void setUserToken(String token) {
        this.preferences.edit().putString("AnalyserForSCIOToken", token).apply();
        long currMillis =  Calendar.getInstance().getTime().getTime();
        long expire =  currMillis + 2419200000L  ; // 28 days
        this.preferences.edit().putLong("expire",expire).apply();
    }

    public boolean isExpireAccessToken()
    {
        long currMillis =  Calendar.getInstance().getTime().getTime();
        long expire  =  this.preferences.getLong("expire",0);
        return currMillis >expire ;
    }

    public void setScioDevice(String deviceId,String deviceName)
    {
        this.preferences.edit().putString("device_id",deviceId).apply();
        this.preferences.edit().putString("device_name",deviceName).apply();
    }

    public  String getScioDeviceId()
    {
        return this.preferences.getString("device_id", null);
    }

    public  String getScioDeviceName()
    {
        return this.preferences.getString("device_name", null);
    }


    public void logout() {
        SharedPreferences.Editor editor =  this.preferences.edit() ;
        editor.remove("AnalyserForSCIOToken").apply();
        editor.remove("username").apply();
        editor.remove("password").apply();
        editor.remove("expire").apply();
    }

    public Boolean isSoundTurnedOn() {
        return this.preferences.getBoolean("soundOn", true);
    }
}
