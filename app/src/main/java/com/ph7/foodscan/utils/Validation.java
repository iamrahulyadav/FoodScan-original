package com.ph7.foodscan.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ph7.foodscan.callbacks.FoodScanHandler;
import com.ph7.foodscan.services.FoodScanService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sony on 01-08-2016.
 */
public class Validation {

    static Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
    private static boolean isTesting;

    public static boolean isValidEmail(String email) {
        Matcher m = emailPattern.matcher(email);
        return m.matches();
    }


    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            return true  ;
        else {
            return false;
        }
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = false  ;
        if(networkInfo!=null)
        {
            isWifiConn= networkInfo.isConnected();
        }
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = false ;
        if(networkInfo!=null)
        {
            isMobileConn = networkInfo.isConnected();
        }

        if(isWifiConn || isMobileConn) return true  ;
        else
        {
           // showSettingsPopup(context) ;
            return false ;
        }
    }

    public static void showSettingsPopup(final Context activity) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("No Internet connection.");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
    }


    public static String generateRandomString(int length) {
        String keyCode = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        char[] chars = keyCode.toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;
    }


    public static void sendReport(final Context context,final String moduleName , final String error, final String input,final String output) {
        isTesting = true;
        if (isTesting) {
//            AlertDialog.Builder builder  = new AlertDialog.Builder(context) ;
//            builder.setMessage(error);
//            builder.setPositiveButton("Send Report", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    String[] to  ={"atul@askonlinesolutions.com"};
//                    Intent intent = new Intent(Intent.ACTION_SEND);
//                    intent.setData(Uri.parse("mailto:"));
//                    intent.setType("*/*");
//                    intent.putExtra(Intent.EXTRA_EMAIL, to);
//                    intent.putExtra(Intent.EXTRA_SUBJECT, "Error Report");
//                    intent.putExtra(Intent.EXTRA_TEXT,"Method : \n\n\n"+moduleName +
//                            "\n\n\nRequest :\n\n\n"+input+
//                            " \n\n\nResponse :\n\n\n"+output+
//                            " \n\n\nError :\n\n\n"+error) ;
//                    if (intent.resolveActivity(context.getPackageManager()) != null) {
//                        context.startActivity(Intent.createChooser(intent, "Send mail"));
//                    }
//                }
//            });
//            builder.create() ;
//            builder.show() ;
            sendLog(context, moduleName, error, input, output);
        }
    }

    private static void sendLog(final Context context,final String moduleName ,final String error, final String input,final String output)
    {
        isTesting =  true  ;
        if(isTesting){
            String message  = "<b>Context Name : </b><br/>"+context.getClass().getName()+
                    " <br/<br/><b>Method : </b><br/>"+moduleName +
                    " <br/<br/><b>Request : </b><br/>"+input+
                    " <br/<br/><b>Response : </b><br/>"+output+
                    " <br/<br/><b>Status : </b><br/>"+error ;
            JSONObject logJsonObj  = new JSONObject();
            try {
                logJsonObj.put("to","atul@askonlinesolutions.com");
                logJsonObj.put("message",message);
                logJsonObj.put("subject","Log Report -"+ Calendar.getInstance().getTime().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new FoodScanService().logError(logJsonObj, new FoodScanHandler() {
                @Override
                public void onSuccess(JSONObject object) {

                }

                @Override
                public void onError() {

                }
            });

        }

    }


    
    

}
