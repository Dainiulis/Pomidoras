package com.dmiesoft.fitpomodoro.ui.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerSendTimeEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerTypeStateHandlerEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerUpdateRequestEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class TimerUIFragment extends Fragment implements View.OnClickListener {

    private static final int BTN_START = 1001;
    private static final int BTN_PAUSE = 1002;
    private static final int BTN_STOP = 1003;
    public static final String TAG = "TIMER";

    private int mCurrentState, mCurrentType;
    private long millisecs;
    private TextView timerText, timerTypeText;
    private FloatingActionButton btnStartPauseTimer, btnStopTimer, btnSkipTimer;

    public TimerUIFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: TIMER");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        initializeViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        TimerUpdateRequestEvent timerUpdate = new TimerUpdateRequestEvent();
        timerUpdate.askForCurrentState(true);
        EventBus.getDefault().post(timerUpdate);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    private void setTimer() {
        timerText.setText(getTimerString(millisecs));
    }

    private void initializeViews(View view) {

        timerText = (TextView) view.findViewById(R.id.timerText);
        timerTypeText = (TextView) view.findViewById(R.id.timerType);

        btnStartPauseTimer = (FloatingActionButton) view.findViewById(R.id.btnStartPauseTimer);
        btnStartPauseTimer.setOnClickListener(this);

        btnStopTimer = (FloatingActionButton) view.findViewById(R.id.btnStopTimer);
        btnStopTimer.setOnClickListener(this);

        btnSkipTimer = (FloatingActionButton) view.findViewById(R.id.btnSkipTimer);
        btnSkipTimer.setOnClickListener(this);
        btnSkipTimer.setVisibility(View.GONE);

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
        setBtnTypes(BTN_STOP);
        TimerTypeStateHandlerEvent timerHandler = new TimerTypeStateHandlerEvent();
        timerHandler.setCurrentState(TimerTaskFragment.STATE_STOPPED);
        EventBus.getDefault().post(timerHandler);
    }

    private void handleBtnStartPause() {
        if (btnStartPauseTimer.getTag().equals(BTN_START)) {
            setBtnTypes(BTN_START);
            TimerTypeStateHandlerEvent timerHandler = new TimerTypeStateHandlerEvent();
            timerHandler.setCurrentState(TimerTaskFragment.STATE_RUNNING);
            EventBus.getDefault().post(timerHandler);
        } else if (btnStartPauseTimer.getTag().equals(BTN_PAUSE)) {
            TimerTypeStateHandlerEvent timerHandler = new TimerTypeStateHandlerEvent();
            timerHandler.setCurrentState(TimerTaskFragment.STATE_PAUSED);
            setBtnTypes(BTN_PAUSE);
            EventBus.getDefault().post(timerHandler);
        }
    }
//  *********************************************

    //  Change buttons and timer textview according to btn tag
    private void setBtnTypes(int clickedButtonTag) {
        if (clickedButtonTag == BTN_START) {
            btnStartPauseTimer.setTag(BTN_PAUSE);
            btnStartPauseTimer.setImageResource(android.R.drawable.ic_media_pause);
            btnStopTimer.setVisibility(View.VISIBLE);
        } else if (clickedButtonTag == BTN_PAUSE) {
            btnStartPauseTimer.setTag(BTN_START);
            btnStartPauseTimer.setImageResource(android.R.drawable.ic_media_play);
            btnStopTimer.setVisibility(View.VISIBLE);
        } else {
            btnStopTimer.setVisibility(View.GONE);
            btnStartPauseTimer.setImageResource(android.R.drawable.ic_media_play);
            btnStartPauseTimer.setTag(BTN_START);
        }
    }

    @Subscribe
    public void onTimerChange(TimerSendTimeEvent event) {
        millisecs = event.getMillisecs();
        setTimer();
    }

    @Subscribe
    public void onTimerTypeStateRequest(TimerTypeStateHandlerEvent event) {
        int currentState = event.getCurrentState();
        int currentType = event.getCurrentType();
        Log.i(TAG, "onTimerTypeStateRequest: " + currentState);
        if (currentState == TimerTaskFragment.STATE_RUNNING) {
            setBtnTypes(BTN_START);
        } else if (currentState == TimerTaskFragment.STATE_PAUSED) {
            setBtnTypes(BTN_PAUSE);
        } else {
            setBtnTypes(BTN_STOP);
        }
        if (currentType == TimerTaskFragment.TYPE_WORK) {
            timerTypeText.setText("Work");
        } else if (currentType == TimerTaskFragment.TYPE_SHORT_BREAK) {
            timerTypeText.setText("Short break");
        } else if (currentType == TimerTaskFragment.TYPE_LONG_BREAK){
            timerTypeText.setText("Long break");
        }
    }

}
