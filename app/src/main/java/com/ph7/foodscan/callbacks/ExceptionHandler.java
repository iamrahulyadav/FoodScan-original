package com.ph7.foodscan.callbacks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by sony on 21-11-2016.
 */

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Activity myContext;
    private final String LINE_SEPARATOR = "\n";

    public ExceptionHandler(Activity context) {
        myContext = context;
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        final StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ CAUSE OF ERROR ************\n\n");
        errorReport.append(stackTrace.toString());

        errorReport.append("\n\n************ Device Information ************\n\n");
        String PhoneModel = android.os.Build.MODEL;
        errorReport.append("Phone Model  : ").append(PhoneModel).append("\n");
        String AndroidVersion = android.os.Build.VERSION.RELEASE;
        errorReport.append("Android Version  : ").append(AndroidVersion).append("\n");
        String incremental = Build.VERSION.INCREMENTAL;
        errorReport.append("Android Incremental Version  : ").append(incremental).append("\n");
        String release = Build.VERSION.RELEASE;
        errorReport.append("Android release Version  : ").append(release).append("\n");

        String[] to  ={"atul@askonlinesolutions.com"};
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, to);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Error Report");
        intent.putExtra(Intent.EXTRA_TEXT,errorReport.toString()) ;
        if (intent.resolveActivity(myContext.getPackageManager()) != null) {
            myContext.startActivity(Intent.createChooser(intent, "Send error mail"));
        }

        System.exit(0);
    }
}
