package com.dmiesoft.fitpomodoro.utils.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dmiesoft.fitpomodoro.ui.activities.SettingsActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerTaskFragment;

import java.util.List;
import java.util.Random;

public class TimerPreferenceManager {

    public static final String TIMER_PARAMS_PREF = "timer_params_pref";
    public static final String INITIALIZED_MILLISECS = "initialized_millisecs";
    public static final String SHOULD_ANIMATE = "should_animate";
    public static final String SHOULD_ANIMATE_BG = "should_animate_background";
    public static final String LONG_BREAK_COUNTER = "long_break_counter";
    public static final String CURRENT_TIMER_STATE = "current_timer_state";
    public static final String CURRENT_TIMER_TYPE = "current_timer_type";
    public static final String PREVIOUS_TIMER_STATE = "previous_timer_state";
    public static final String PREVIOUS_TIMER_TYPE = "previous_timer_type";
    public static final String EXERCISE_ID = "exercise_id";
    public static final String SELECTED_FAVORITE = "selected_favorite";
    public static final String CURRENT_RAND_EXERCISE = "current_random_exercise";

    private static final String TAG = "TPM";

    private static SharedPreferences mTimerPrefs;
    private static SharedPreferences mDefaultSharedPrefs;

    private TimerPreferenceManager() {
    }

    public static void initPreferences(Context context) {
        if (mTimerPrefs == null) {
            Log.i(TAG, "initPreferences: timerPrefInIT");
            mTimerPrefs = context.getSharedPreferences(TIMER_PARAMS_PREF, Context.MODE_PRIVATE);
        }
        if (mDefaultSharedPrefs == null) {
            Log.i(TAG, "initPreferences: defShPrefInit");
            mDefaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }


    public static void setLongBreakCounter(int mLongBreakCounter) {
        Log.i(TAG, "setLongBreakCounter: " + mLongBreakCounter);
        mTimerPrefs.edit()
                .putInt(LONG_BREAK_COUNTER, mLongBreakCounter)
                .apply();
    }

    public static void setCurrentState(int currentState) {
        int previousState = getCurrentState();
        mTimerPrefs.edit()
                .putInt(CURRENT_TIMER_STATE, currentState)
                .putInt(PREVIOUS_TIMER_STATE, previousState)
                .apply();
    }

    public static void setCurrentType(int currentType) {
        int previousType = getCurrentType();
        mTimerPrefs.edit()
                .putInt(CURRENT_TIMER_TYPE, currentType)
                .putInt(PREVIOUS_TIMER_TYPE, previousType)
                .apply();
    }

    public static int getLongBreakCounter() {
        return mTimerPrefs.getInt(LONG_BREAK_COUNTER, 0);
    }

    public static int getCurrentType() {
        return mTimerPrefs.getInt(CURRENT_TIMER_TYPE, 0);
    }

    public static int getCurrentState() {
        return mTimerPrefs.getInt(CURRENT_TIMER_STATE, 0);
    }

    public static int getPreviousState() {
        return mTimerPrefs.getInt(PREVIOUS_TIMER_STATE, 0);
    }

    public static int getPreviousType() {
        return mTimerPrefs.getInt(PREVIOUS_TIMER_TYPE, 0);
    }

    public static long getCurrentRandomExercise() {
        return mTimerPrefs.getLong(CURRENT_RAND_EXERCISE, -1);
    }

    public static void setCurrentRandExercise(List<Long> exercisesIds) {
        if (exercisesIds != null) {
            if (exercisesIds.size() > 0) {
                Random randomGenerator = new Random();
                int index = randomGenerator.nextInt(exercisesIds.size());
                long randExerciseID = exercisesIds.get(index);
                mTimerPrefs.edit().putLong(CURRENT_RAND_EXERCISE, randExerciseID).apply();
            }
        }
    }


    //default shared prefs

    public static boolean isAutoOpen() {
        return mDefaultSharedPrefs.getBoolean(SettingsActivity.PREF_AUTO_OPEN_WHEN_TIMER_FINISH, false);
    }

    public static boolean isContinuous() {
        return mDefaultSharedPrefs.getBoolean(SettingsActivity.PREF_CONTINUOUS_MODE, false);
    }

    public static int getWhenLongBreak() {
        return mDefaultSharedPrefs.getInt(SettingsActivity.PREF_KEY_WHEN_LONG_BREAK, 4);
    }

    public static boolean isTimerForTesting() {
        return mDefaultSharedPrefs.getBoolean(SettingsActivity.PREF_TIMER_TIME_FOR_TESTING, true);
    }

    public static int getWorkTime() {
        return mDefaultSharedPrefs.getInt(SettingsActivity.PREF_KEY_WORK_TIME, 25);
    }

    public static int getLongBreakTime() {
        return mDefaultSharedPrefs.getInt(SettingsActivity.PREF_KEY_LONG_BREAK_TIME, 15);
    }

    public static int getShortBreakTime() {
        return mDefaultSharedPrefs.getInt(SettingsActivity.PREF_KEY_REST_TIME, 5);
    }

    public static boolean showTimerSuggestions() {
        return mDefaultSharedPrefs.getBoolean(SettingsActivity.PREF_KEY_SHOW_TIMER_TEXT_SUGGESTIONS, true);
    }

    public static long getSelectedFavorite() {
        return mDefaultSharedPrefs.getLong(SELECTED_FAVORITE, -1);
    }

    public static void saveSelectedFavorite(long favId) {
        mDefaultSharedPrefs.edit().putLong(TimerPreferenceManager.SELECTED_FAVORITE, favId).apply();
    }

    public static boolean isFirstTimeLoad() {
        return mDefaultSharedPrefs.getBoolean(SettingsActivity.FIRST_TIME_LOAD, true);
    }

    public static void setiSFirstTimeLoad(boolean iSFirstTimeLoad) {
        mDefaultSharedPrefs.edit().putBoolean(SettingsActivity.FIRST_TIME_LOAD, iSFirstTimeLoad).apply();
    }


    public static long getDefaultMillisecs(int currentType) {

        //while developing, when released, leave multiplier = 60000;
        long multiplier = 1000;
        if (!TimerPreferenceManager.isTimerForTesting()) {
            multiplier = 60000;
        }
        return getDefaultMins(currentType) * multiplier;
    }

    public static boolean isVibrate() {
        return mDefaultSharedPrefs.getBoolean(SettingsActivity.PREF_KEY_VIBRATE, true);
    }

    public static boolean isSilence() {
        return mDefaultSharedPrefs.getBoolean(SettingsActivity.PREF_KEY_SILENCE, false);
    }

    private static long getDefaultMins(int currentType) {
        int defMinutes = 0;
        if (currentType == TimerTaskFragment.TYPE_WORK) {
            defMinutes = TimerPreferenceManager.getWorkTime();
        } else if (currentType == TimerTaskFragment.TYPE_LONG_BREAK) {
            defMinutes = TimerPreferenceManager.getLongBreakTime();
        } else {
            defMinutes = TimerPreferenceManager.getShortBreakTime();

        }
        return (long) defMinutes;
    }

}
