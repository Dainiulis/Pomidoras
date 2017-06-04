package com.dmiesoft.fitpomodoro.application;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dmiesoft.fitpomodoro.services.TimerService;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerTaskFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.nested.NestedSaveExerciseFragment;
import com.dmiesoft.fitpomodoro.utils.preferences.TimerPreferenceManager;

public class FitPomodoroApplication extends Application {

    private static final String TAG = "FPAPP";
    private Thread.UncaughtExceptionHandler defaultUEH;
    private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
            Log.i(TAG, "uncaughtException: " + t.getName());
            if (isUIThread()) {
                Log.i(TAG, "uncaughtException: " + e.getMessage());
            } else {

                //not from UI thread
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });
            }
        }
    };

    private void appInitialization() {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(mUncaughtExceptionHandler);
    }

    private boolean isUIThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
    /**
     * For saving how many reps was selected in {@link NestedSaveExerciseFragment}
     */
    private int reps;
    /**
     * saved pager page of the mViewPager
     */
    private int pagerPage;
    /**
     * How many times the exercise was done
     */
    private int howManyTimesDone;
    /**
     * For notifying {@link com.dmiesoft.fitpomodoro.ui.fragments.TimerUIFragment} to animate ViewPager
     */
    private boolean animateViewPager;

    private int currentState;
    private int currentType;
    private int previousState;
    private int previousType;
    private boolean sessionFinished;

    @Override
    public void onCreate() {
        super.onCreate();
        TimerPreferenceManager.initPreferences(getApplicationContext());
//        if (TimerService.serviceRunning) {
//            setCurrentState(TimerPreferenceManager.getCurrentState());
//            setCurrentType(TimerPreferenceManager.getCurrentType());
//            setPreviousType(TimerPreferenceManager.getPreviousType());
//            setPreviousState(TimerPreferenceManager.getPreviousState());
//        } else {
//            setCurrentType(TimerTaskFragment.TYPE_WORK);
//            setCurrentState(TimerTaskFragment.STATE_STOPPED);
//            setPreviousType(TimerTaskFragment.TYPE_WORK);
//            setPreviousState(TimerTaskFragment.STATE_STOPPED);
//        }
        setCurrentType(TimerTaskFragment.TYPE_WORK);
        setCurrentState(TimerTaskFragment.STATE_STOPPED);
        setPreviousType(TimerTaskFragment.TYPE_WORK);
        setPreviousState(TimerTaskFragment.STATE_STOPPED);
        setSessionFinished(false);
//        appInitialization();
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        setPreviousState(this.currentState);
        this.currentState = currentState;
    }

    public int getCurrentType() {
        return currentType;
    }

    public void setCurrentType(int currentType) {
        setPreviousType(this.currentType);
        this.currentType = currentType;
    }

    public int getPreviousState() {
        return previousState;
    }

    public void setPreviousState(int previousState) {
        this.previousState = previousState;
    }

    public int getPreviousType() {
        return previousType;
    }

    public void setPreviousType(int previousType) {
        this.previousType = previousType;
    }

    public int getHowManyTimesDone() {
        return howManyTimesDone;
    }

    public void setHowManyTimesDone(int howManyTimesDone) {
        this.howManyTimesDone = howManyTimesDone;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public boolean shouldAnimateViewPager() {
        return animateViewPager;
    }

    public void setAnimateViewPager(boolean animateViewPager) {
        this.animateViewPager = animateViewPager;
    }

    public boolean isSessionFinished() {
        return sessionFinished;
    }

    public void setSessionFinished(boolean sessionFinished) {
        this.sessionFinished = sessionFinished;
    }

    public int getPagerPage() {
        return pagerPage;
    }

    public void setPagerPage(int pagerPage) {
        this.pagerPage = pagerPage;
    }

}
