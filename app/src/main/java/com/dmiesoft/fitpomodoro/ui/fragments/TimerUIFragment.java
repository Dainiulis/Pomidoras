package com.dmiesoft.fitpomodoro.ui.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerSendTimeEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerTypeStateHandlerEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerUpdateRequestEvent;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.utils.helpers.DisplayWidthHeight;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class TimerUIFragment extends Fragment implements View.OnClickListener {

    private static final int BTN_START = 1001;
    private static final int BTN_PAUSE = 1002;
    private static final int BTN_STOP = 1003;
    public static final String TAG = "TIMER";
    private static final long LONG_DURATION = 500;
    private static final long NO_DURATION = 0;

    private int mCurrentState, mCurrentType;
    private long millisecs;
    private TextView timerText, timerTypeText;
    private FloatingActionButton btnStartPauseTimer, btnStopTimer;
    private ObjectAnimator oA;
    private ObjectAnimator oA1;
    private String propertyName;
    private long animLength;
    private FloatingActionButton mainFab;

    public TimerUIFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainFab = ((MainActivity) getActivity()).getMainFab();
        if(mainFab != null) {
            mainFab.hide();
        }
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

        // tik bandymams
        timerTypeText.setOnClickListener(this);

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

            case R.id.btnStopTimer:
                handleBtnStop();
                break;

            //bandymams
            case R.id.timerType:
                getOrientation();
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

//      Change buttons and timer textview according to btn tag
    private void setBtnTypes(int clickedButtonTag) {
        if (getOrientation() == Configuration.ORIENTATION_PORTRAIT){
            propertyName = "translationX";
        } else {
            propertyName = "translationY";
        }
        if (clickedButtonTag == BTN_START) {

            try {
                if (oA == null)
                    initObjectAnimators();
            } catch (NullPointerException ignored) {}


            btnStartPauseTimer.setTag(BTN_PAUSE);
            btnStartPauseTimer.setImageResource(android.R.drawable.ic_media_pause);
            btnStopTimer.setVisibility(View.VISIBLE);
            btnStopTimer.setClickable(true);
        } else if (clickedButtonTag == BTN_PAUSE) {
            animLength = NO_DURATION;
            initObjectAnimators();
            btnStartPauseTimer.setTag(BTN_START);
            btnStartPauseTimer.setImageResource(android.R.drawable.ic_media_play);
            btnStopTimer.setVisibility(View.VISIBLE);
        } else {

            try {
                endObjectAnimators();
            } catch (NullPointerException ignored) {}

            btnStartPauseTimer.setImageResource(android.R.drawable.ic_media_play);
            btnStartPauseTimer.setTag(BTN_START);
        }
    }

    private void endObjectAnimators() {
        oA.setDuration(LONG_DURATION);
        oA1.setDuration(LONG_DURATION);
        oA.end();
        oA1.end();
        oA.reverse();
        oA1.reverse();
        oA = null;
        oA1 = null;
    }

    private void initObjectAnimators() {
        oA = ObjectAnimator.ofFloat(btnStartPauseTimer, propertyName, 0f, getPixels(-40));
        oA.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                btnStartPauseTimer.setClickable(false);
                btnStopTimer.setClickable(false);
                if (btnStopTimer.getVisibility() == View.INVISIBLE) {
                    btnStopTimer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                btnStartPauseTimer.setClickable(true);
                if (mCurrentState == BTN_STOP) {
                    btnStopTimer.setVisibility(View.INVISIBLE);
                    btnStopTimer.setClickable(false);
                } else {
                    btnStopTimer.setClickable(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        oA.setDuration(animLength);
        oA1 = ObjectAnimator.ofFloat(btnStopTimer, propertyName, 0f, getPixels(40));
        oA1.setDuration(animLength);

        oA.start();
        oA1.start();

    }

    private float getPixels(float dp) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float pixels = dp * outMetrics.density;
        return pixels;
    }

    private int getOrientation() {
        DisplayWidthHeight display = new DisplayWidthHeight(getActivity());
        float width = display.getWidth();
        float height = display.getHeight();
        int orientation;
        if (width<height) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        } else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }

    @Subscribe
    public void onTimerChange(TimerSendTimeEvent event) {
        millisecs = event.getMillisecs();
        setTimer();
    }

    @Subscribe
    public void onTimerTypeStateRequest(TimerTypeStateHandlerEvent event) {
        mCurrentState = event.getCurrentState();
        int currentType = event.getCurrentType();
        if (mCurrentState == TimerTaskFragment.STATE_RUNNING) {
            animLength = NO_DURATION;
            setBtnTypes(BTN_START);
        } else if (mCurrentState == TimerTaskFragment.STATE_PAUSED) {
            setBtnTypes(BTN_PAUSE);
        } else {
            animLength = LONG_DURATION;
            setBtnTypes(BTN_STOP);
        }
        if (currentType == TimerTaskFragment.TYPE_WORK) {
            timerTypeText.setText("Work");
        } else if (currentType == TimerTaskFragment.TYPE_SHORT_BREAK) {
            timerTypeText.setText("Short break");
        } else if (currentType == TimerTaskFragment.TYPE_LONG_BREAK) {
            timerTypeText.setText("Long break");
        }
    }

}
