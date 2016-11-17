package com.ph7.analyserforscio.application;

import android.content.Context;
import android.content.res.Configuration;
import android.support.multidex.MultiDexApplication;

import com.ph7.analyserforscio.device.DeviceHandler;
import com.ph7.analyserforscio.device.interfaces.DeviceHandlerInterface;
import com.ph7.analyserforscio.services.AnalyzerService;
import com.ph7.analyserforscio.services.interfaces.AnalyzerInterface;

import java.util.Locale;

/**
 * Created by craigtweedy on 04/07/2016.
 */
public class FoodScanApplication extends MultiDexApplication {

    private static Context context;

    private static DeviceHandlerInterface deviceHandler;
    private static AnalyzerInterface analyzerService;

    private static Locale locale;

    public void onCreate() {
        super.onCreate();
        FoodScanApplication.context = getApplicationContext();
        FoodScanApplication.setDeviceHandler(new DeviceHandler());
        FoodScanApplication.setAnalyzerService(new AnalyzerService());
        FoodScanApplication.setLocale(Locale.getDefault());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FoodScanApplication.setLocale(Locale.getDefault());
    }

    public static Context getAppContext() {
        return FoodScanApplication.context;
    }

    public static DeviceHandlerInterface getDeviceHandler() {
        return FoodScanApplication.deviceHandler;
    }

    public static void setDeviceHandler(DeviceHandlerInterface deviceHandler) {
        FoodScanApplication.deviceHandler = deviceHandler;
    }

    public static AnalyzerInterface getAnalyzerService() {
        return analyzerService;
    }

    public static void setAnalyzerService(AnalyzerInterface analyzerService) {
        FoodScanApplication.analyzerService = analyzerService;
    }

    public static Locale getLocale() {
        return locale;
    }

    public static void setLocale(Locale locale) {
        FoodScanApplication.locale = locale;
    }
}
