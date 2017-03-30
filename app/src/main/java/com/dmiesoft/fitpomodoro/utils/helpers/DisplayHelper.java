package com.dmiesoft.fitpomodoro.utils.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;

public class DisplayHelper {

    private float width, height;

    public DisplayHelper(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.width = size.x;
        this.height = size.y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    /**
     * Helper method to calculate pixels from density independent pixels
     *
     * @param activity
     * @param dp density independent pixels
     * @return result in pixels
     */
    public static float getPixels(Activity activity, float dp) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float pixels = dp * outMetrics.density;
        return pixels;
    }

    /**
     * This method is required for determining the orientation of the device.
     * Use Configuration class provided by Android for evaluating the orientation.
     *
     * @return Orientation of the device. Evaluate either to Configuration.ORIENTATION_PORTRAIT or Configuration.ORIENTATION_LANDSCAPE
     */
    public int getOrientation() {
//        DisplayHelper display = new DisplayHelper(activity);
//        float width = display.getWidth();
//        float height = display.getHeight();
        int orientation;
        if (width < height) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        } else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }


}
