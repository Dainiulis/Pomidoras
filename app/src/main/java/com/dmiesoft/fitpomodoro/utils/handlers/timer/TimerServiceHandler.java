package com.dmiesoft.fitpomodoro.utils.handlers.timer;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.dmiesoft.fitpomodoro.services.TimerService;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerTaskFragment;
import com.dmiesoft.fitpomodoro.utils.LogToFile;
import com.dmiesoft.fitpomodoro.utils.helpers.TimerHelper;
import com.dmiesoft.fitpomodoro.utils.preferences.TimerPreferenceManager;

/**
 * Handler for running timer in another Thread
 */
public class TimerServiceHandler extends Handler {

    private static final String TAG = "TSH";
    private Handler mHandler;
    private long mMillisecs;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private LogToFile logToFile;

    public TimerServiceHandler(Looper looper, Handler handler) {
        super(looper);
        this.mHandler = handler;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.arg1 == TimerTaskFragment.STATE_STOPPED || msg.arg1 == TimerTaskFragment.STATE_FINISHED) {
            mMillisecs = TimerPreferenceManager.getDefaultMillisecs((Integer) msg.obj);
            if ((Integer) msg.obj == TimerTaskFragment.TYPE_LONG_BREAK) {
                TimerPreferenceManager.setLongBreakCounter(0);
            }
        }
        msg.what = TimerService.MSG_TIMER_STATUS;
        if (!mTimerRunning) {
            initTimer();
            msg.arg1 = TimerService.ARG_TIMER_STATUS_RUNNING;
            mTimerRunning = true;
        } else {
            mCountDownTimer.cancel();
            msg.arg1 = TimerService.ARG_TIMER_STATUS_PAUSED;
            mTimerRunning = false;
        }
        msg.obj = mMillisecs;
        mHandler.handleMessage(msg);
    }

    private void initTimer() {
        mCountDownTimer = new CountDownTimer(mMillisecs, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
                mMillisecs = millisUntilFinished;
                Message msg = mHandler.obtainMessage();
                msg.what = TimerService.MSG_UPDATE_TIMER;
                msg.obj = mMillisecs;
                mHandler.handleMessage(msg);
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                Message msg = mHandler.obtainMessage();
                msg.what = TimerService.MSG_FINISH_TIMER;
                msg.obj = mMillisecs;
                mHandler.handleMessage(msg);
            }
        }.start();
    }
}
