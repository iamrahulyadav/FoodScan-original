package com.ph7.analyserforscio.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.provider.MediaStore;
import android.view.View;

/**
 * Created by craigtweedy on 26/01/2016.
 */
public class ViewToImage {

    public static String convert(Context context, View view) {
        Bitmap image = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(image));
        return MediaStore.Images.Media.insertImage(context.getContentResolver(), image, "Sharing", null);
    }

}
