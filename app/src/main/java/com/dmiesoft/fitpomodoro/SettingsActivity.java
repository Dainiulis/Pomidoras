package com.dmiesoft.fitpomodoro;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static String PREF_KEY_WORK_TIME = "pref_key_work_time";
    public static String PREF_KEY_REST_TIME = "pref_key_rest_time";
    public static String PREF_KEY_WHEN_LONG_BREAK = "pref_key_when_long_break";
    public static String PREF_KEY_LONG_BREAK_TIME = "pref_key_long_break_time";
    public static String PREF_CONTINUOUS_MODE = "pref_continuous_mode";

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
