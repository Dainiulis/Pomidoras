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

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * This abstraact class helps to perform certain operations with bitmap files
 */
public abstract class BitmapHelper {

    public static final int REQUIRED_WIDTH = 600;
    public static final int REQUIRED_HEIGTH = 600;
    public static final float MAX_SIZE = 600f;
    public static final float BORDER_SIZE = 1;
    private static final String TAG = "BH";

    /*
     * For getting bitmap from files
     */
    public static Bitmap getBitmapFromFiles(Context context, String image, boolean scaled, int resourceDimen) {
        File f = getFileFromImages(image, context);
        if (!scaled) {
            return BitmapFactory.decodeFile(f.getAbsolutePath());
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), options);
            int srcWidth = options.outWidth;
            int srcHeight = options.outHeight;
            int srcDensity = 0;
            if (srcWidth > srcHeight) {
                srcDensity = srcWidth;
            } else {
                srcDensity = srcHeight;
            }
            options.inSampleSize = calculateInSampleSize(options, resourceDimen, resourceDimen);
            options.inJustDecodeBounds = false;
            options.inScaled = true;
            options.inDensity = srcDensity;
            options.inTargetDensity = resourceDimen * options.inSampleSize;
            return BitmapFactory.decodeFile(f.getAbsolutePath(), options);
        }
    }


    /**
     * Calculate inSampleSize
     *
     * @param options        BitmapFactory.Options
     * @param requiredWidth  int
     * @param requiredHeight int
     * @return InSample value which fits for both parameters
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int requiredWidth, int requiredHeight) {
        final int imageHeight = options.outHeight;
        final int imageWidth = options.outWidth;
        int inSampleWidth = Math.round((float) imageWidth / (float) requiredWidth);
        int inSampleHeight = Math.round((float) imageHeight / (float) requiredHeight);
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
     * Makes bitmap circle and adds border
     *
     * @param bitmap
     * @param borderWidth pass 0 if border is not required
     * @return returns cropped bitmap in circle
     */
    public static Bitmap getCroppedBitmap(Bitmap bitmap, float borderWidth) {
        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        int xO = 0;
        int yO = 0;
        if (x > y) {
            xO = x;
            yO = x;
        } else if (x < y) {
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
        output.setDensity(bitmap.getDensity());
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(xO / 2, yO / 2, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, (xO - x) / 2, (yO - y) / 2, paint);

        if (borderWidth != 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(borderWidth);
            paint.setColor(colorBorder);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
            canvas.drawCircle(xO / 2, yO / 2, radius - (borderWidth / 2), paint);
        }
        return output;
    }

    public static void saveImage(String fileName, Bitmap bitmap, Context context) {
        File newFile = getFileFromImages(fileName, context);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get file from images directory
     *
     * @param fileName
     * @param context
     * @return
     */
    @NonNull
    public static File getFileFromImages(String fileName, Context context) {
        File internalFolder = new File(context.getFilesDir(), "images");
        if (!internalFolder.exists()) {
            internalFolder.mkdir();
        }
        return new File(internalFolder, fileName);
    }

    /**
     * Helps to decode bitmap from specified path
     *
     * @param path           the path to bitmap
     * @param requiredWidth  required width of bitmap
     * @param requiredHeight required height of bitmap
     * @return decoded bitmap
     */
    public static Bitmap decodeBitmapFromPath(String path, int requiredWidth, int requiredHeight) {
        Bitmap bitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int inSampleSize = BitmapHelper.calculateInSampleSize(options, requiredWidth, requiredHeight);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        bitmap = BitmapFactory.decodeFile(path, options);

        return bitmap;
    }

    /**
     * Scales then crops bitmap to circle and adds border if needed.
     *
     * @param bitmap
     * @param maxSize maximum size of bitmap image. For example if image is 400px width and 200px height,
     *                the width will be set to maxSize and height will be scaled accordingly.
     * @return scaled bitmap
     */
    public static Bitmap getScaledBitmap(Bitmap bitmap, float maxSize) {

        float width = (float) bitmap.getWidth();
        float heigth = (float) bitmap.getHeight();
        int[] widthHeight = BitmapHelper.calculateWidthAndHeighth(width, heigth, maxSize);
        bitmap = Bitmap.createScaledBitmap(bitmap, widthHeight[0], widthHeight[1], false);
        return bitmap;
    }

    /**
     * Helper for TextDrawable api
     *
     * @param name
     * @return
     */
    public static TextDrawable getTextDrawable(String name) {
        String firstChar = name.substring(0, 1).toUpperCase();
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(name);
        return TextDrawable.builder().buildRound(firstChar, color);
    }

}
