package com.dmiesoft.fitpomodoro.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.ui.activities.SettingsActivity;


public class TimerFragment extends Fragment implements View.OnClickListener{

    private static final int BTN_START = 1001;
    private static final int BTN_PAUSE = 1002;
    public static final String TAG = "TAG";
    private CountDownTimer timer;
    private TextView timerText;
    private static boolean timerRunning = false, timerPaused = false, workTimer = true;
    private long millisecs;
    private int longBreakCounter;
    private SharedPreferences sharedPref;
    private FloatingActionButton btnStartPauseTimer, btnStopTimer, btnSkipTimer;
    private View view;

    public TimerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        longBreakCounter = 0;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_timer, container, false);
            initializeViews(view);
            setTimer();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!timerRunning && !timerPaused) {
            setTimer();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach: TIMER");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach: TIMER");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: TIMER");
    }

    //  **Handle millisecs and timer**
    private void setTimer() {
        if (workTimer) {
            timerText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            millisecs = getMillisecs(getDefaultMins(true));
        } else {
            timerText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            millisecs = getMillisecs(getDefaultMins(false));
        }
        timerText.setText(getTimerString(millisecs));
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
        if (workTimer) {

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

    private void initializeViews(View view) {

        timerText = (TextView) view.findViewById(R.id.timerText);
        timerText.setText(getTimerString(millisecs));

        btnStartPauseTimer = (FloatingActionButton) view.findViewById(R.id.btnStartPauseTimer);
        btnStartPauseTimer.setOnClickListener(this);
        btnStartPauseTimer.setTag(BTN_START);

        btnStopTimer = (FloatingActionButton) view.findViewById(R.id.btnStopTimer);
        btnStopTimer.setOnClickListener(this);

        btnSkipTimer = (FloatingActionButton) view.findViewById(R.id.btnSkipTimer);
        btnSkipTimer.setOnClickListener(this);
        btnSkipTimer.setVisibility(View.GONE);

        btnStopTimer.setVisibility(View.GONE);
    }

    private void initTimer(long timeMilli) {
        timer = new CountDownTimer(timeMilli, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                String time = getTimerString(millisUntilFinished);
                millisecs = millisUntilFinished;
                timerText.setText(time);
            }

            @Override
            public void onFinish() {
                timer.cancel();
                if (workTimer) {
                    longBreakCounter++;
                }
                workTimer = !workTimer;
                setTimer();
                if (isContinuous()) {
                    initTimer(millisecs);
                } else {
                    timerRunning = false;
                    timerPaused = false;
                    setStartPauseBtn(BTN_START);
                }
            }
        };
        timer.start();
    }

    private String getTimerString(long millisUntilFinished) {
        long sec = (millisUntilFinished / 1000) % 60;
        long min = (millisUntilFinished / 60000);
        return String.format("%02d:%02d", min, sec);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartPauseTimer:
                handleBtnStartPause();
                break;

            case R.id.btnSkipTimer:

                break;

            case R.id.btnStopTimer:
                handleBtnStop();
                break;
        }
    }

    //  ***Handle buttons***
    private void handleBtnStop() {
        if (timer != null) {
            timer.cancel();
        }
        longBreakCounter = 0;
        workTimer = true;
        setTimer();
        btnStopTimer.setVisibility(View.GONE);
        setStartPauseBtn(BTN_START);
        timerPaused = false;
        timerRunning = false;
    }

    private void handleBtnStartPause() {
        if (btnStartPauseTimer.getTag().equals(BTN_START)) {
            initTimer(millisecs);
            timerRunning = true;
            timerPaused = false;
            btnStopTimer.setVisibility(View.VISIBLE);
            setStartPauseBtn(BTN_PAUSE);
        } else if (btnStartPauseTimer.getTag().equals(BTN_PAUSE)) {
            if (timer != null) {
                timer.cancel();
            }
            timerPaused = true;
            timerRunning = false;
            setStartPauseBtn(BTN_START);
        }
    }
//  *********************************************

    //  Change buttons and timer textview according to btn tag
    private void setStartPauseBtn(int tag) {
        if (tag == BTN_START) {
            btnStartPauseTimer.setTag(tag);
            btnStartPauseTimer.setImageResource(android.R.drawable.ic_media_play);
        } else {
            btnStartPauseTimer.setTag(tag);
            btnStartPauseTimer.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    public interface TimerFragmentListener {
        // enter methods to implement in MainActivity.class
    }

}
