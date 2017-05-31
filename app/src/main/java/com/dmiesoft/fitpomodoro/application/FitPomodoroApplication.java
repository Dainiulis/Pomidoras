package com.dmiesoft.fitpomodoro.application;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dmiesoft.fitpomodoro.ui.fragments.nested.NestedSaveExerciseFragment;

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

    @Override
    public void onCreate() {
        super.onCreate();
//        appInitialization();
    }

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

    public int getPagerPage() {
        return pagerPage;
    }

    public void setPagerPage(int pagerPage) {
        this.pagerPage = pagerPage;
    }

}
