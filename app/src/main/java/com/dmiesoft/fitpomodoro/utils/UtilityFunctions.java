package com.dmiesoft.fitpomodoro.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public abstract class UtilityFunctions {

    /*
     * For getting bitmap from files
     */
    public static Bitmap getBitmapFromFiles(Context context, String image) {
        File folder = new File(context.getFilesDir(), "images");
        File f = new File(folder, image);
        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

        return bitmap;
    }


    /**
     * Calculate inSampleSize
     * @param options BitmapFactory.Options
     * @param requiredWidth int
     * @param requiredHeight int
     * @return InSample value which fits for both parameters
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int requiredWidth, int requiredHeight) {
        final int imageHeight = options.outHeight;
        final int imageWidth = options.outWidth;
        Log.i("AEGD", "imageHeight: " + imageHeight);
        Log.i("AEGD", "imageWidth: " + imageWidth);
        int inSampleWidth = Math.round((float)imageWidth / (float) requiredWidth);
        int inSampleHeight = Math.round((float)imageHeight / (float) requiredHeight);
        if (inSampleWidth <= inSampleHeight) {
            return inSampleHeight;
        } else {
            return inSampleWidth;
        }
    }

    /**
     * @param width
     * @param height
     * @param maxSize
     * @return int[] with two values, first width, second height
     */
    public static int[] calculateWidthAndHeighth(float width, float height, float maxSize) {
        Log.i("AEGD", "imageHeight: " + height);
        Log.i("AEGD", "imageWidth: " + width);
        float ratio = 0;
        int[] widthHeight;
        if (width == height) {
            width = maxSize;
            height = maxSize;
        } else if (width > height) {
            ratio = width / maxSize;
            width = maxSize;
            height = height / ratio;
        } else {
            ratio = height / maxSize;
            height = maxSize;
            width = width / ratio;
        }
        widthHeight = new int[]{(int) width, (int) height};
        return widthHeight;
    }

    /**
     * @param bitmap
     * @return returns cropped bitmap in circle
     */
    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        int xO = 0;
        int yO = 0;
        if (x > y) {
            xO = x;
            yO = x;
        } else if (x < y){
            xO = y;
            yO = y;
        } else {
            xO = x;
            yO = y;
        }
        final int color = Color.WHITE;
        final int colorBorder = 0xff424242;
        int radius = xO / 2;

        Bitmap output = Bitmap.createBitmap(xO,
                yO, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
//        final Rect rect = new Rect(0, 0, x, y);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(xO / 2,  yO / 2,radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, (xO - x) / 2, (yO - y) / 2, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);
        paint.setColor(colorBorder);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawCircle(xO / 2,  yO / 2,radius, paint);
        return output;
    }

    public static void saveImage(String fileName, Bitmap bitmap, Context context) {
        File newFile = getFile(fileName, context);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public static File getFile(String fileName, Context context) {
        File internalFolder = new File(context.getFilesDir(), "images");
        if (!internalFolder.exists()) {
            internalFolder.mkdir();
        }
        return new File(internalFolder, fileName);
    }

}
