package com.ph7.foodscan.application;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

/**
 * Created by sony on 13-07-2016.
 */


public class PermissionRequestHandler {

    private static final int PERMISSIONS_REQUEST_CAMERA = 0 ;
    private static final int PERMISSIONS_REQUEST_GALLARY = 1 ;

    public static boolean requestPermissionToCamera(final Context context)
    {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,
                        Manifest.permission.CAMERA)) {

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                        public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSIONS_REQUEST_CAMERA);

                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_CAMERA);

                }
                return false ;
            }
            else
            {
                return true  ;
            }
        }
        else
        {
            return true  ;
        }

    }

    public static boolean requestPermissionToGallary(final Context context)
    {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions((Activity)context,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        PERMISSIONS_REQUEST_GALLARY);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions((Activity)context,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_GALLARY);
                }
                return false ;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return true ;
        }

    }


}
