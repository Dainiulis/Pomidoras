package com.dmiesoft.fitpomodoro.utils.helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;

public class NotificationHelper {

    public static final int RESULT_PENDING_INTENT_REQUEST_CODE = 8001;
    public static final int FINISH_PENDING_INTENT_REQUEST_CODE = 8002;
    public static final int STOP_BROADCAST_REQUEST_CODE = 1001;
    public static final int TIMER_TIME_NOTIFICATION = 7007;
    public static final int TIMER_FINISHED_NOTIFICATION = 8008;
    public static final String ACTION_STOP = "com.dmiesoft.fitpomodoro.ACTION_STOP";
    public static final String ACTION_OPEN_TIMER_FRAG = "com.dmiesoft.fitpomodoro.ACTION_OPEN_TIMER_FRAG";
    public static final String ACTION_OPEN_TIMER_FRAG_FROM_FINISH = "com.dmiesoft.fitpomodoro.ACTION_OPEN_TIMER_FRAG_FROM_FINISH";
    /**
     * used for getting to package, if raw data is needed (sounds, images, etc.)
     */
    public static final String URI_TO_PACKAGE = "android.resource://com.dmiesoft.fitpomodoro/";
    private static final String TAG = "NH";

    /**
     * Gets timer time notification builder (used for showing which timer is going and it's time)
     *
     * @param context
     * @param timerType
     * @return
     */
    public static NotificationCompat.Builder getTimerTimeNotificationBuilder(
            Context context, int timerType, PendingIntent resultPendingIntent) {

        Intent stopIntent = new Intent(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, STOP_BROADCAST_REQUEST_CODE, stopIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(TimerHelper.getFromResources(timerType, TimerHelper.DRAWABLE_ICONS))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(resultPendingIntent)
                .addAction(R.drawable.ic_stop_for_notif, "Stop", stopPendingIntent);
        return builder;
    }

    public static PendingIntent getPendingIntentForFinishingNotification(Context context) {

//        Intent resultIntent = new Intent(context, MainActivity.class);
//        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, RESULT_PENDING_INTENT_REQUEST_CODE, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent resultIntent = new Intent(ACTION_OPEN_TIMER_FRAG);
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context, RESULT_PENDING_INTENT_REQUEST_CODE, resultIntent, 0);
        return resultPendingIntent;
    }

    public static NotificationCompat.Builder getFinishedNotificationBuilder(
            Context context, int timerType) {


        Intent resultIntent = new Intent(ACTION_OPEN_TIMER_FRAG_FROM_FINISH);
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context, FINISH_PENDING_INTENT_REQUEST_CODE, resultIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(TimerHelper.getFromResources(timerType, TimerHelper.DRAWABLE_ICONS))
                .setContentTitle(TimerHelper.getTimerTypeName(timerType) + " time has finished...")
                .setOngoing(true)
                .setContentText("Tap to continue your session")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(resultPendingIntent);

        return builder;

    }
}
