package com.dmiesoft.fitpomodoro.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dmiesoft.fitpomodoro.ui.activities.SettingsActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerTaskFragment;
import com.dmiesoft.fitpomodoro.utils.LogToFile;
import com.dmiesoft.fitpomodoro.utils.handlers.timer.TimerServiceHandler;
import com.dmiesoft.fitpomodoro.utils.helpers.NotificationHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.TimerHelper;
import com.dmiesoft.fitpomodoro.utils.preferences.TimerPreferenceHelper;

import java.lang.ref.WeakReference;

public class TimerService extends Service {

    public static final String INTENT_TIMER_TICK = "com.dmiesoft.fitpomodoro.TIME_FILTER";
    public static final String INTENT_TIMER_FINISH = "com.dmiesoft.fitpomodoro.TIMER_FINISH";
    public static final String INTENT_TIMER_STOP = "com.dmiesoft.fitpomodoro.TIMER_STOP";
    public static final String TIMER_TIME = "timer_time";

    public static final String TIMER_ACTION_MSG = "timer_action_msg";
    public static final int TIMER_ACTION_STOP = 1442;
    public static final int TIMER_ACTION_START = 1440;

    public static final String STARTING_TIME = "starting_time";
    private static final String TAG = "SERVISAS";
    private static final String TIMER_THREAD = "timer";
    public static final int MSG_UPDATE_TIMER = 133;
    public static final int MSG_FINISH_TIMER = 111;

    private Intent intentTimerTick;
    private SharedPreferences mTimerPrefs;
    private SharedPreferences mPrefs;

    private NotificationCompat.Builder mTimeNotificationBuilder;
    private PendingIntent mPendingIntentForFinishingNotification;

    private LogToFile logToFile;
    Handler mHandler;

    private Looper mServiceLooper;
    private TimerServiceHandler mTimerServiceHandler;
    private final IBinder mBinder = new LocalTimerBinder();
    private boolean notifSoundOngoing = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        mTimerPrefs = this.getSharedPreferences(TimerPreferenceHelper.TIMER_PARAMS_PREF, Context.MODE_PRIVATE);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mPendingIntentForFinishingNotification = NotificationHelper.getPendingIntentForFinishingNotification(this);

        HandlerThread thread = new HandlerThread(TIMER_THREAD, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mHandler = new LocalHandler(this);
        mTimerServiceHandler = new TimerServiceHandler(mServiceLooper, mHandler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mTimerServiceHandler.obtainMessage();
        msg.arg1 = getCurrentState();
        msg.arg2 = getPreviousState();
        if (intent.getExtras() != null) {
            startLogging();
            msg.setData(intent.getExtras());
        }
        mTimerServiceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        stopTimerInUI();
        mServiceLooper.quit();
        super.onDestroy();
    }

    public class LocalTimerBinder extends Binder {
        public TimerService getTimerService() {
            return TimerService.this;
        }
    }

    private void finishTimer(long millisecs) {
        logToFileMillisAndSecs("Finishing timer " + millisecs);
        cancelNotification();
        Intent intent = new Intent(INTENT_TIMER_FINISH);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void stopTimerInUI() {
        Intent intent = new Intent(INTENT_TIMER_STOP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendTime(long millisecs) {
        updateNotificationTimer(millisecs);
        if (intentTimerTick == null) {
            intentTimerTick = new Intent(INTENT_TIMER_TICK);
        }
        intentTimerTick.putExtra(TIMER_TIME, millisecs);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentTimerTick);
    }

    private void startLogging() {
        if (getCurrentState() == TimerTaskFragment.STATE_RUNNING && (getPreviousState() != TimerTaskFragment.STATE_PAUSED)) {
            logToFileMillisAndSecs("---------------------------------------------------");
            logToFileMillisAndSecs("Starting new timer");
        }
    }

    /**
     * @param identifyingText - text which will be logged to txt file
     */
    private void logToFileMillisAndSecs(String identifyingText) {
        if (logToFile == null) {
            logToFile = new LogToFile(this, "log.txt");
        }
        logToFile.appendLog(identifyingText);
    }

    private void updateNotificationTimer(long millisecs) {
        if (mTimeNotificationBuilder == null) {
            initTimeNotifBuilder();
        }
        if (mTimeNotificationBuilder != null && !notifSoundOngoing) {
            try {
                mTimeNotificationBuilder.setContentTitle(TimerHelper.getTimerTypeName(getCurrentType()) +
                        " time remaining - " +
                        TimerHelper.getTimerString(millisecs));
                Notification notification = mTimeNotificationBuilder.build();
                startForeground(NotificationHelper.TIMER_TIME_NOTIFICATION, notification);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void initTimeNotifBuilder() {
        if (mPendingIntentForFinishingNotification == null) {
            mPendingIntentForFinishingNotification = NotificationHelper.getPendingIntentForFinishingNotification(this);
        }
        mTimeNotificationBuilder = NotificationHelper.getTimerTimeNotificationBuilder(
                this, getCurrentType(), mPendingIntentForFinishingNotification);
    }

    private void cancelNotification() {
        if (getCurrentState() != TimerTaskFragment.STATE_STOPPED) {
            manageNotificationWithSound();
        }
        if (isAutoOpen()) {
            try {
                mPendingIntentForFinishingNotification.send(NotificationHelper.RESULT_PENDING_INTENT_REQUEST_CODE);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        mTimeNotificationBuilder = null;
    }

    private void manageNotificationWithSound() {
        int currentType = getCurrentType();
        Uri uri = Uri.parse(NotificationHelper.URI_TO_PACKAGE +
                TimerHelper.getFromResources(currentType, TimerHelper.RAW_SOUNDS));
        mTimeNotificationBuilder
                .setSound(uri)
                .setContentTitle(TimerHelper.getTimerTypeName(currentType) + " time has finished...")
                .setContentText("Tap to continue your session");
        startForeground(NotificationHelper.TIMER_TIME_NOTIFICATION, mTimeNotificationBuilder.build());
        notifSoundOngoing = true;
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                notifSoundOngoing = false;
            }
        };
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(this, uri);
        long duration = Long.parseLong(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        handler.postDelayed(runnable, duration);
    }

    private boolean isAutoOpen() {
        return mPrefs.getBoolean(SettingsActivity.PREF_AUTO_OPEN_WHEN_TIMER_FINISH, false);
    }

    private boolean isContinuous() {
        return mPrefs.getBoolean(SettingsActivity.PREF_CONTINUOUS_MODE, false);
    }

    private int getCurrentType() {
        return mTimerPrefs.getInt(TimerPreferenceHelper.CURRENT_TIMER_TYPE, 0);
    }

    private int getCurrentState() {
        return mTimerPrefs.getInt(TimerPreferenceHelper.CURRENT_TIMER_STATE, 0);
    }

    private int getPreviousState() {
        return mTimerPrefs.getInt(TimerPreferenceHelper.PREVIOUS_TIMER_STATE, 0);
    }

    private static class LocalHandler extends Handler {
        private final WeakReference<TimerService> mService;

        LocalHandler(TimerService service) {
            mService = new WeakReference<TimerService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            TimerService service = mService.get();
            if (service != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIMER:
                        service.sendTime((Long) msg.obj);
                        break;
                    case MSG_FINISH_TIMER:
                        service.finishTimer((Long) msg.obj);
                        break;
                }
            }
        }
    }

    public static void stopTimerService(Context context) {
        Intent timerIntent = new Intent(context, TimerService.class);
        context.stopService(timerIntent);
    }

}
