package com.dmiesoft.fitpomodoro.ui.activities;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import com.dmiesoft.fitpomodoro.R;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREF_TIMER_TIME_FOR_TESTING = "pref_timer_time_for_testing";

    public static final String PREF_KEY_WORK_TIME = "pref_key_work_time";
    public static final String PREF_KEY_REST_TIME = "pref_key_rest_time";
    public static final String PREF_KEY_WHEN_LONG_BREAK = "pref_key_when_long_break";
    public static final String PREF_KEY_LONG_BREAK_TIME = "pref_key_long_break_time";
    public static final String PREF_CONTINUOUS_MODE = "pref_key_continuous_mode";
    public static final String PREF_AUTO_OPEN_WHEN_TIMER_FINISH = "pref_key_auto_open_when_timer_finish";
    public static final String PREF_KEY_AUTO_START_TIMED_EXERCISES = "pref_key_auto_start_timed_exercises";
    public static final String PREF_KEY_AUTO_SAVE_TIMED_EXERCISES = "pref_key_auto_save_timed_exercises";
    public static final String PREF_KEY_SHOW_TIMER_TEXT_SUGGESTIONS = "pref_key_show_timer_text_suggestions";
    public static final String PREF_KEY_SETS_BEFORE_CHANGING_EXERCISE = "pref_key_sets_before_changing_exercise";
    public static final String PREF_KEY_VIBRATE = "pref_key_vibrate";
    public static final String FIRST_TIME_LOAD = "FIRST_TIME_LOAD";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    public static class SettingsFragment extends PreferenceFragment {
        private static final String TAG = "PREF";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
