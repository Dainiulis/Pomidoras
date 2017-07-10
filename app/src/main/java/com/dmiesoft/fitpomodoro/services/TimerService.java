package com.dmiesoft.fitpomodoro.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.application.FitPomodoroApplication;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerTaskFragment;
import com.dmiesoft.fitpomodoro.utils.LogToFile;
import com.dmiesoft.fitpomodoro.utils.handlers.timer.TimerServiceHandler;
import com.dmiesoft.fitpomodoro.utils.helpers.NotificationHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.TimerHelper;
import com.dmiesoft.fitpomodoro.utils.preferences.TimerPreferenceManager;

import java.util.List;

public class TimerService extends Service {

    public static final String INTENT_TIMER_TICK = "com.dmiesoft.fitpomodoro.TIME_FILTER";
    public static final String INTENT_TIMER_FINISH = "com.dmiesoft.fitpomodoro.TIMER_FINISH";
    public static final String INTENT_TIMER_STOP = "com.dmiesoft.fitpomodoro.TIMER_STOP";
    public static final String INTENT_TIMER_START_PAUSE = "com.dmiesoft.fitpomodoro.TIMER_START_PAUSE";
    public static final String TIMER_TIME = "timer_time";

    public static final String TIMER_ACTION_MSG = "timer_action_msg";
    public static final int TIMER_ACTION_STOP = 1442;
    public static final int TIMER_ACTION_START = 1440;

    public static final String STARTING_TIME = "starting_time";
    private static final String TAG = "SERVISAS";
    private static final String TIMER_THREAD = "timer";
    private static final String PROCESSING_TIME_THREAD = "processing_time";

    public static final int MSG_UPDATE_TIMER = 133;
    public static final int MSG_FINISH_TIMER = 111;
    public static final int MSG_TIMER_STATUS = 122;
    public static final int ARG_TIMER_STATUS_RUNNING = 411;
    public static final int ARG_TIMER_STATUS_PAUSED = 511;

    private Intent intentTimerTick;
    public static boolean serviceRunning = false;

    private NotificationCompat.Builder mTimeNotificationBuilder;
    private PendingIntent mPendingIntentForFinishingNotification;

    private LogToFile logToFile;
    private Handler mHandler;

    private Looper mCountDownTimerServiceLooper;
    private TimerServiceHandler mTimerServiceHandler;
    private final IBinder mBinder = new LocalTimerBinder();
    private boolean mNotifSoundOngoing = false;
    private FitPomodoroApplication appContext;

    /**
     * this is used for notification update.
     * It's required because if notification is updating too often,
     * then the buttos become almost unresponsive
     */
    boolean canUpdateNotif = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        TimerPreferenceManager.initPreferences(this);
        appContext = (FitPomodoroApplication) getApplicationContext();
        mPendingIntentForFinishingNotification = NotificationHelper.getPendingIntentForFinishingNotification(this);
        HandlerThread countDownTimerThread = new HandlerThread(TIMER_THREAD, Process.THREAD_PRIORITY_BACKGROUND);
        countDownTimerThread.start();
        mCountDownTimerServiceLooper = countDownTimerThread.getLooper();

        mHandler = new LocalHandler();

        mTimerServiceHandler = new TimerServiceHandler(mCountDownTimerServiceLooper, mHandler);

        serviceRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mTimerServiceHandler.obtainMessage();
//        Log.i(TAG + "S", "onStartCommand: " +
//                "   currType " + TimerHelper.getTimerStateOrTypeString(appContext.getCurrentType()) +
//                "  currState " + TimerHelper.getTimerStateOrTypeString(appContext.getCurrentState()) +
//                "\n prevType " + TimerHelper.getTimerStateOrTypeString(appContext.getPreviousType()) +
//                "   prevState " + TimerHelper.getTimerStateOrTypeString(appContext.getPreviousState()));

        if (appContext.getCurrentState() == TimerTaskFragment.STATE_STOPPED ||
                appContext.getCurrentState() == TimerTaskFragment.STATE_FINISHED) {
            startLogging();
        }

        msg.arg1 = appContext.getCurrentState();
        msg.arg2 = appContext.getPreviousState();
        msg.obj = appContext.getCurrentType();
        mTimerServiceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        TimerPreferenceManager.setLongBreakCounter(0);
        manageTimerInUI(INTENT_TIMER_STOP);
        appContext.setCurrentState(TimerTaskFragment.STATE_STOPPED);
        appContext.setCurrentType(TimerTaskFragment.TYPE_WORK);
        mCountDownTimerServiceLooper.quit();
        serviceRunning = false;
        super.onDestroy();
    }

    public class LocalTimerBinder extends Binder {
        public TimerService getTimerService() {
            return TimerService.this;
        }
    }

    private void finishTimer(long millisecs) {
        boolean lastSession = false;
        if (appContext.getCurrentType() == TimerTaskFragment.TYPE_LONG_BREAK) {
            lastSession = true;
        }
        finishingNotification(lastSession);

        int longBreakCounter = TimerPreferenceManager.getLongBreakCounter();

        if (appContext.getCurrentType() == TimerTaskFragment.TYPE_WORK) {
            ++longBreakCounter;
            TimerPreferenceManager.setLongBreakCounter(longBreakCounter);
            if (longBreakCounter == TimerPreferenceManager.getWhenLongBreak()) {
                appContext.setCurrentType(TimerTaskFragment.TYPE_LONG_BREAK);
            } else if (longBreakCounter != 0) {
                appContext.setCurrentType(TimerTaskFragment.TYPE_SHORT_BREAK);
            }
            appContext.setRandExerciseId();
        } else {
            appContext.setCurrentType(TimerTaskFragment.TYPE_WORK);
        }
                /*
                 * jei naudotojas pakeicia ilgos pertraukos kintamaji i mazesni nei yra suskaiciuotas
                 * tuomet pradedamas ilgosios pertraukos laikmatis
                 */
        if (longBreakCounter > TimerPreferenceManager.getWhenLongBreak()) {
            longBreakCounter = TimerPreferenceManager.getWhenLongBreak();
            TimerPreferenceManager.setLongBreakCounter(longBreakCounter);
        }
        // misSessionFinished is used to determine whether
        // the whole work session has finished or not
        if (appContext.getPreviousType() == TimerTaskFragment.TYPE_LONG_BREAK) {
            appContext.setCurrentState(TimerTaskFragment.STATE_STOPPED);
        } else {
            appContext.setCurrentState(TimerTaskFragment.STATE_FINISHED);
        }

        if (appContext.getPreviousType() == TimerTaskFragment.TYPE_LONG_BREAK) {
            appContext.setSessionFinished(true);
        }
        appContext.setAnimateViewPager(true);

//        Log.i(TAG, "onFinishTImer: currType " + TimerHelper.getTimerStateOrTypeString(appContext.getCurrentType()) + "  currState " + TimerHelper.getTimerStateOrTypeString(appContext.getCurrentState()) +
//                "\n prevType " + TimerHelper.getTimerStateOrTypeString(appContext.getPreviousType()) + "   prevState " + TimerHelper.getTimerStateOrTypeString(appContext.getPreviousState()));


        Intent intent = new Intent(INTENT_TIMER_FINISH);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        if (TimerPreferenceManager.isContinuous() && appContext.getPreviousType() != TimerTaskFragment.TYPE_LONG_BREAK) {

            //not perfect solution, but works...
            //basically the problem is that as soon as onFinish calls animations
            //the newly started timer also calls animations and the whole thing looks like shit
            //and timer acts very buggy
            //Better solution would be to learn Threads more deeply and run animation code in it and/or synchronize
            //methods running animations...
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Intent intentc = new Intent(TimerService.this, TimerService.class);
                    startService(intentc);
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable, 500);

//            Intent intentc = new Intent(TimerService.this, TimerService.class);
//            startService(intentc);
        }

    }

    //notify timer in UI
    private void manageTimerInUI(String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendTimeToUI(long millisecs) {
        if (intentTimerTick == null) {
            intentTimerTick = new Intent(INTENT_TIMER_TICK);
        }
        intentTimerTick.putExtra(TIMER_TIME, millisecs);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentTimerTick);
    }

    private void sendTime(long millisecs) {
        if (canUpdateNotif) {
            updateNotificationTimer(millisecs);
            canUpdateNotif = false;
        }
        Handler h = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                canUpdateNotif = true;
            }
        };
        h.postDelayed(r, 1000);
        sendTimeToUI(millisecs);
    }

    private void startLogging() {
        logToFileMillisAndSecs("---------------------------------------------------");
        logToFileMillisAndSecs("Starting new timer");
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
            initTimeNotifBuilder(millisecs, getString(R.string.pause));
        }
        if (mTimeNotificationBuilder != null && !mNotifSoundOngoing) {
            try {
                mTimeNotificationBuilder.setContentTitle(TimerHelper.getTimerTypeName(appContext.getCurrentType()) +
                        " time remaining - " +
                        TimerHelper.getTimerString(millisecs));
                Notification notification = mTimeNotificationBuilder.build();
                startForeground(NotificationHelper.TIMER_TIME_NOTIFICATION, notification);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void initTimeNotifBuilder(long millisecs, String firstBtnText) {
        if (mPendingIntentForFinishingNotification == null) {
            mPendingIntentForFinishingNotification = NotificationHelper.getPendingIntentForFinishingNotification(this);
        }
        mTimeNotificationBuilder = NotificationHelper.getTimerTimeNotificationBuilder(
                this, appContext.getCurrentType(), false, false, mPendingIntentForFinishingNotification, firstBtnText)
                .setContentTitle(TimerHelper.getTimerTypeName(appContext.getCurrentType()) +
                        " time remaining - " +
                        TimerHelper.getTimerString(millisecs));
        if (!mNotifSoundOngoing && !TimerPreferenceManager.isContinuous()) {
            startForeground(NotificationHelper.TIMER_TIME_NOTIFICATION, mTimeNotificationBuilder.build());
        }
    }

    private void finishingNotification(boolean lastSessionTimer) {
        if (appContext.getCurrentState() != TimerTaskFragment.STATE_STOPPED) {
            manageNotificationWithSound(lastSessionTimer);
        }
        if (TimerPreferenceManager.isAutoOpen()) {
            try {
                mPendingIntentForFinishingNotification.send(NotificationHelper.RESULT_PENDING_INTENT_REQUEST_CODE);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        mTimeNotificationBuilder = null;
    }

    private void manageNotificationWithSound(boolean lastSessionTimer) {
        Uri uri = Uri.parse(NotificationHelper.URI_TO_PACKAGE +
                TimerHelper.getFromResources(appContext.getCurrentType(), TimerHelper.RAW_SOUNDS));
        mTimeNotificationBuilder = NotificationHelper
                .getTimerTimeNotificationBuilder(this, appContext.getCurrentType(), lastSessionTimer, true, mPendingIntentForFinishingNotification, "Start")
                .setContentTitle(TimerHelper.getTimerTypeName(appContext.getCurrentType()) + " time has finished...");
        if (!TimerPreferenceManager.isSilence()) {
            mTimeNotificationBuilder.setSound(uri);
        }
        startForeground(NotificationHelper.TIMER_TIME_NOTIFICATION, mTimeNotificationBuilder.build());
        mNotifSoundOngoing = true;
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mNotifSoundOngoing = false;
            }
        };
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(this, uri);
        long duration = Long.parseLong(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        handler.postDelayed(runnable, duration);
    }

    private void setmTimerRunning(int state) {
        switch (state) {
            case ARG_TIMER_STATUS_PAUSED:
                appContext.setCurrentState(TimerTaskFragment.STATE_PAUSED);
                manageTimerInUI(INTENT_TIMER_START_PAUSE);
                break;
            case ARG_TIMER_STATUS_RUNNING:
                appContext.setCurrentState(TimerTaskFragment.STATE_RUNNING);
                manageTimerInUI(INTENT_TIMER_START_PAUSE);
                break;
        }
    }

    private class LocalHandler extends Handler {
        LocalHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_TIMER:
                    sendTime((Long) msg.obj);
                    break;
                case MSG_FINISH_TIMER:
                    finishTimer((Long) msg.obj);
                    setmTimerRunning(MSG_FINISH_TIMER);
                    break;
                case MSG_TIMER_STATUS:
                    String btnName = "";
                    if (msg.arg1 == ARG_TIMER_STATUS_RUNNING) {
                        setmTimerRunning(ARG_TIMER_STATUS_RUNNING);
                        btnName = "Pause";
                    } else if (msg.arg1 == ARG_TIMER_STATUS_PAUSED) {
                        setmTimerRunning(ARG_TIMER_STATUS_PAUSED);
                        btnName = "Resume";
                    }
                    initTimeNotifBuilder((Long) msg.obj, btnName);
                    break;
            }
        }
    }

    public static void stopTimerService(Context context) {
        Intent timerIntent = new Intent(context, TimerService.class);
        context.stopService(timerIntent);
    }

}
