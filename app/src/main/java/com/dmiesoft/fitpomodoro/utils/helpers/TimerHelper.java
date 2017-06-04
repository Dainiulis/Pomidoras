package com.dmiesoft.fitpomodoro.utils.helpers;

import android.util.Log;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerTaskFragment;

public class TimerHelper {

    public static final int RAW_SOUNDS = 10;
    public static final int DRAWABLE_ICONS = 11;
    private static final String TAG = "TIMERHELPER";

    public static String getTimerTypeName(int timerType) {
        String timerTypeName = null;
        switch (timerType) {
            case TimerTaskFragment.TYPE_WORK:
                timerTypeName = "Work";
                break;
            case TimerTaskFragment.TYPE_SHORT_BREAK:
                timerTypeName = "Short break";
                break;
            case TimerTaskFragment.TYPE_LONG_BREAK:
                timerTypeName = "Long break";
                break;
        }

        return timerTypeName;
    }

    /**
     * function for getting easier to understand values for logging state or type of the timer
     * @param timerLogConst Constant of timer type or state from {@link TimerTaskFragment}
     * @return value for logging
     */
    public static String getTimerStateOrTypeString(int timerLogConst) {
        String logVal = null;
        String ending = "(" + timerLogConst + ") ";
        switch (timerLogConst) {
            case TimerTaskFragment.STATE_RUNNING:
                logVal = "State_Running" + ending;
                break;
            case TimerTaskFragment.STATE_FINISHED:
                logVal = "State_Finished" + ending;
                break;
            case TimerTaskFragment.STATE_PAUSED:
                logVal = "State_Paused" + ending;
                break;
            case TimerTaskFragment.STATE_STOPPED:
                logVal = "State_Stopped" + ending;
                break;
            case TimerTaskFragment.TYPE_WORK:
                logVal = "Work" + ending;
                break;
            case TimerTaskFragment.TYPE_SHORT_BREAK:
                logVal = "Short break" + ending;
                break;
            case TimerTaskFragment.TYPE_LONG_BREAK:
                logVal = "Long break" + ending;
                break;
        }
        return logVal;
    }

    /**
     * @param timerType
     * @param whatToGet Pass {@link TimerHelper#RAW_SOUNDS} to get sounds from raw.
     *                  Pass {@link TimerHelper#DRAWABLE_ICONS} to get icons from drawable
     * @return
     */
    public static int getFromResources(int timerType, int whatToGet) {
        int result = 0;
        switch (timerType) {
            case TimerTaskFragment.TYPE_WORK:
                result = (whatToGet == DRAWABLE_ICONS ?
                        R.drawable.ic_hard_working_dude
                        : R.raw.beep4);
                break;
            case TimerTaskFragment.TYPE_SHORT_BREAK:
                result = (whatToGet == DRAWABLE_ICONS ?
                        R.drawable.ic_stretching_dude
                        : R.raw.tiriri);
                break;
            case TimerTaskFragment.TYPE_LONG_BREAK:
                result = (whatToGet == DRAWABLE_ICONS ?
                        R.drawable.ic_pull_up_dude :
                        R.raw.firealarm);
                break;
        }
        return result;
    }

    public static String getTimerString(long millisUntilFinished) {
        long sec = (millisUntilFinished / 1000) % 60;
        long min = (millisUntilFinished / 60000);
        return String.format("%02d:%02d", min, sec);
    }

}
