package com.dmiesoft.fitpomodoro.utils.helpers;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * This class is used for storing general constants and methods
 */
public class UniversalAppHelper {
    public static final String PACKAGE_NAME_FIT_POMODORO = "com.dmiesoft.fitpomodoro";

    public static boolean isAppOnForeground(Context context, String appPackageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(appPackageName)) {
                return true;
            }
        }
        return false;
    }
}
