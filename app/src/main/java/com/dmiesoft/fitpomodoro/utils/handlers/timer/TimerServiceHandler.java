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
        Log.i(TAG, "currState: " + TimerHelper.getTimerStateOrTypeString(msg.arg1) +
                "  prevState " + TimerHelper.getTimerStateOrTypeString(msg.arg2));
        if (msg.arg1 == TimerTaskFragment.STATE_RUNNING && (msg.arg2 != TimerTaskFragment.STATE_PAUSED)) {
            mMillisecs = msg.getData().getLong(TimerService.STARTING_TIME);
        }
        if (!mTimerRunning) {
            initTimer();
            mTimerRunning = true;
        } else {
            mCountDownTimer.cancel();
            mTimerRunning = false;
        }
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
