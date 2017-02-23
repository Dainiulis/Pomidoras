package com.dmiesoft.fitpomodoro.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.dmiesoft.fitpomodoro.events.timer_handling.TimerSendTimeEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerTypeStateHandlerEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerUpdateRequestEvent;
import com.dmiesoft.fitpomodoro.ui.activities.SettingsActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class TimerTaskFragment extends Fragment {

    public static final int STATE_STOPPED = 141;
    public static final int STATE_RUNNING = 493;
    public static final int STATE_PAUSED = 94;
    public static final int TYPE_WORK = 203;
    public static final int TYPE_SHORT_BREAK = 784;
    public static final int TYPE_LONG_BREAK = 736;

    private int mCurrentState, mCurrentType;
    private static final String TAG = "TTF";
    private CountDownTimer timer;
    private long millisecs;
    private int longBreakCounter;
    private SharedPreferences sharedPref;
    private TimerTypeStateHandlerEvent timerHandlerEvent;
    private TimerSendTimeEvent sendTimeEvent;

    public TimerTaskFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        timerHandlerEvent = new TimerTypeStateHandlerEvent();
        sendTimeEvent = new TimerSendTimeEvent();
        setmCurrentState(STATE_STOPPED);
        setmCurrentType(TYPE_WORK);
        longBreakCounter = 0;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (mCurrentState == STATE_STOPPED) {
            setTimer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    //  **Handle millisecs and timer**
    private void setTimer() {
        if (mCurrentType == TYPE_WORK) {
            millisecs = getMillisecs(getDefaultMins(true));
        } else {
            millisecs = getMillisecs(getDefaultMins(false));
        }
        sendTimeEvent.setMillisecs(millisecs);
        EventBus.getDefault().post(sendTimeEvent);
    }

    private int getWhenLongBreak() {
        return sharedPref.getInt(SettingsActivity.PREF_KEY_WHEN_LONG_BREAK, 4);
    }

    private boolean isContinuous() {
        return sharedPref.getBoolean(SettingsActivity.PREF_CONTINUOUS_MODE, false);
    }

    private long getMillisecs(long minutes) {
        //Testavimui pasidaryti minutes * 1000(bus sekundes), naudojimui minutes * 60000(bus minutes)
        return minutes * 1000;
    }

    private long getDefaultMins(boolean workTimer) {
        int defMinutes = 0;
        if (mCurrentType == TYPE_WORK) {
            defMinutes = sharedPref.getInt(SettingsActivity.PREF_KEY_WORK_TIME, 25);
        } else {
            if (longBreakCounter == getWhenLongBreak()) {
                defMinutes = sharedPref.getInt(SettingsActivity.PREF_KEY_LONG_BREAK_TIME, 15);
                longBreakCounter = 0;
            } else {
                defMinutes = sharedPref.getInt(SettingsActivity.PREF_KEY_REST_TIME, 5);
            }
        }
        return (long) defMinutes;
    }
//  *********************************

    private void initTimer() {
        setmCurrentState(STATE_RUNNING);
        timer = new CountDownTimer(millisecs, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisecs = millisUntilFinished;
                if (EventBus.getDefault().hasSubscriberForEvent(sendTimeEvent.getClass())) {
                    sendTimeEvent.setMillisecs(millisecs);
                    EventBus.getDefault().post(sendTimeEvent);
                }
            }

            @Override
            public void onFinish() {
                timer.cancel();
                /*
                 *skaiciuoja kada bus ilga pertrauka
                 */
                if (mCurrentType == TYPE_WORK) {
                    longBreakCounter++;
                }
                /*
                 * isimena koks laikmacio tipas buvo
                 */
                int previousType = mCurrentType;
                /*
                 * jei naudotojas pakeicia ilgos pertraukos kintamaji i mazesni nei yra suskaiciuotas
                 * tuomet pradedamas ilgosios pertraukos laikmatis
                 */
                if (longBreakCounter > getWhenLongBreak()) {
                    longBreakCounter = getWhenLongBreak();
                }
                /*
                 * jei ilgoji pertrauka, tuomet pradedama
                 * kitaip pradedama trumpoji pertrauka
                 */
                if (longBreakCounter == getWhenLongBreak()) {
                    setmCurrentType(TYPE_LONG_BREAK);
                } else {
                    setmCurrentType(TYPE_SHORT_BREAK);
                }
                /*
                 * patikrinama kokio tipo laikmatis buvo
                 * jei trumpos ar ilgos pertraukos, tuomet nustatomas darbinis
                 */
                if (previousType == TYPE_SHORT_BREAK || previousType == TYPE_LONG_BREAK) {
                    setmCurrentType(TYPE_WORK);
                }
                setTimer();
                if (isContinuous()) {
                    initTimer();
                } else {
                    setmCurrentState(STATE_STOPPED);
                }
                timerHandlerEvent.setCurrentState(mCurrentState);
                timerHandlerEvent.setCurrentType(mCurrentType);
                EventBus.getDefault().post(timerHandlerEvent);
            }
        };
        timer.start();
    }

    public void setmCurrentState(int mCurrentState) {
        this.mCurrentState = mCurrentState;
    }

    public void setmCurrentType(int mCurrentType) {
        this.mCurrentType = mCurrentType;
    }

    @Subscribe
    public void onTimerButtonClicked(TimerTypeStateHandlerEvent event) {
        if (event.getCurrentState() == STATE_RUNNING) {
            //butinai reikejo patikrinti sita salyga, kitaip buginosi laikmatis
            if (mCurrentState != STATE_RUNNING) {
                initTimer();
            }
        } else if (event.getCurrentState() == STATE_PAUSED) {
            timer.cancel();
            mCurrentState = STATE_PAUSED;
        } else if (event.getCurrentState() == STATE_STOPPED) {
            try {
                timer.cancel();
            } catch (NullPointerException e) {
                Log.i(TAG, e.getMessage());
            }
            setTimer();
            mCurrentState = STATE_STOPPED;
        }
    }
    @Subscribe
    public void onTimerUIUpdateRequest(TimerUpdateRequestEvent event){
        if (event.isAskingForCurrentState()) {
            if (mCurrentState != STATE_RUNNING) {
                sendTimeEvent.setMillisecs(millisecs);
                EventBus.getDefault().post(sendTimeEvent);
            }
            timerHandlerEvent.setCurrentState(mCurrentState);
            timerHandlerEvent.setCurrentType(mCurrentType);
            EventBus.getDefault().post(timerHandlerEvent);
        }
    }
}
