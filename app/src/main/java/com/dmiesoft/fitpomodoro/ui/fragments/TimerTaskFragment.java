package com.dmiesoft.fitpomodoro.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.dmiesoft.fitpomodoro.application.FitPomodoroApplication;
import com.dmiesoft.fitpomodoro.events.exercises.RequestForNewExerciseEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.CircleProgressEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.ChangeExerciseEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerSendTimeEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerHandlerEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerUpdateRequestEvent;
import com.dmiesoft.fitpomodoro.services.TimerService;
import com.dmiesoft.fitpomodoro.utils.LogToFile;
import com.dmiesoft.fitpomodoro.utils.helpers.TimerHelper;
import com.dmiesoft.fitpomodoro.utils.preferences.TimerPreferenceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class TimerTaskFragment extends Fragment {

    public static final int STATE_STOPPED = 90;
    public static final int STATE_RUNNING = 91;
    public static final int STATE_PAUSED = 92;
    public static final int STATE_FINISHED = 93;
    public static final int TYPE_WORK = 200;
    public static final int TYPE_SHORT_BREAK = 201;
    public static final int TYPE_LONG_BREAK = 202;

    private static final String TAG = "TTF";
    private long millisecs, mInitializedMillisecs;
    private TimerSendTimeEvent sendTimeEvent;
    private TimerTaskFragmentListener mListener;

    ////////// Circle variables /////////////////
    public static final int CIRCLE_STOP = 2864;
    private CircleProgressEvent mCircleProgressEvent;
    private float mCircleProgressValue;

    private float mAnimatedVal, mAnimationFraction;
    private int mNextSecond, mCurrentSecond;
    /////////////////////////////////////////////

    private FitPomodoroApplication appContext;

    //for txt logging
    private LogToFile logToFile;

    public TimerTaskFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        TimerPreferenceManager.initPreferences(getContext());
        appContext = (FitPomodoroApplication) getActivity().getApplicationContext();

        // init events
        mCircleProgressEvent = new CircleProgressEvent();
        sendTimeEvent = new TimerSendTimeEvent();

        //set starting state, type, variables...
        setTimer();

        registerBroadcastReceiver();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof TimerTaskFragmentListener) {
//            mListener = (TimerTaskFragmentListener) context;
//        } else {
//            throw new RuntimeException(context.toString() + " must implement TimerTaskFragmentListener");
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if (appContext.getCurrentState() == STATE_STOPPED) {
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
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mLocalBroadcastReceiver);
    }

    //  **Handle millisecs and mTimer**
    private void setTimer() {
        millisecs = TimerPreferenceManager.getDefaultMillisecs(appContext.getCurrentType());
        sendTimeEvent.setMillisecs(millisecs);
        EventBus.getDefault().post(sendTimeEvent);
        mInitializedMillisecs = millisecs;
        initStartingTimerAnimVars();
    }

    /**
     * mAnimatedVal - the animated value, it is variable;
     * mAnimationFraction - sort of interpolation. Constant value for every mTimer
     * mNextSecond - the next decreasing second. It is required for drawing mTimer. See initTimer()
     */
    private void initStartingTimerAnimVars() {
        mAnimatedVal = 0;
        mAnimationFraction = (float) 1000 / (float) millisecs;
        mNextSecond = (int) (millisecs / 1000) - 1;
    }

    public void onTimerTick(long millisUntilFinished) {
    /*
     * How mTimer animation works...
     *
     * It gets mCurrentSecond - the current second as Integer
     * then it evaluates if current second and next second difference is 0
     * and only then the value for mTimer animation drawing is sent
     */
        millisecs = millisUntilFinished;
        mCurrentSecond = (int) (millisecs / 1000);

        int difference = mCurrentSecond - mNextSecond;

        if (Math.abs(difference) > 1) {
            difference = 0;
        }
        if (difference == 0) {
            mNextSecond = mCurrentSecond - 1;
            mAnimatedVal = mAnimationFraction * (mInitializedMillisecs / 1000 - mCurrentSecond);
            sendAnimatedTimerValue();

            if (EventBus.getDefault().hasSubscriberForEvent(sendTimeEvent.getClass())) {
                sendTimeEvent.setMillisecs(millisecs);
                EventBus.getDefault().post(sendTimeEvent);
            }
        }
    }

    public void onTimerFinish() {
        //this part is required to clear mTimer animation
        mAnimatedVal = 0;
        sendAnimatedTimerValue();

        int longBreakCounter = TimerPreferenceManager.getLongBreakCounter();
        setTimer();

//        if (TimerPreferenceManager.isContinuous() && appContext.getPreviousType() != TYPE_LONG_BREAK) {
//            startTimerService();
//        }
        boolean shouldAnimBGColor = false;
        if (appContext.getPreviousType() == TYPE_LONG_BREAK) {
            shouldAnimBGColor = true;
        }
        // the second param shouldAnimBGColor is because if timer should animate BG
        // color from onTimerFinish, then it automatically is stopped, so the timer is always
        // stopped in this case
        if (!TimerPreferenceManager.isContinuous())
            postTimerHandlerEvent(shouldAnimBGColor, shouldAnimBGColor);
    }

    /**
     * Sends animated value for mTimer through EventBus
     */
    private void sendAnimatedTimerValue() {
        if (EventBus.getDefault().hasSubscriberForEvent(mCircleProgressEvent.getClass())) {
            mCircleProgressEvent.setCircleProgress(mAnimatedVal);
            EventBus.getDefault().post(mCircleProgressEvent);
        }
    }

    private void postTimerHandlerEvent(boolean shouldAnimateBGColor, boolean timerStopped) {
        TimerHandlerEvent timerHandlerEvent = new TimerHandlerEvent();
        timerHandlerEvent.setShouldAnimateBackgroundColor(shouldAnimateBGColor);
        timerHandlerEvent.setStopTimer(timerStopped);
        timerHandlerEvent.setPublisher(TimerHandlerEvent.PUBLISHER_TIMER_TASK_FRAGMENT);
        EventBus.getDefault().post(timerHandlerEvent);
    }

    /**
     * Send random exercise id through eventbus
     */
    private void setOrChangeRandomExercise() {
        if (EventBus.getDefault().hasSubscriberForEvent(ChangeExerciseEvent.class)) {
            EventBus.getDefault().post(new ChangeExerciseEvent(true));
        }
    }

    @Subscribe
    public void onTimerButtonClicked(TimerHandlerEvent event) {
        // negerai, kai tas pats fragment siuncia ir kitam ir sau ta pati event.
        if (event.getPublisher() == TimerHandlerEvent.PUBLISHER_TIMER_TASK_FRAGMENT) {
            return;
        }
        if (!event.shouldStopTimer()) {
            startTimerService();
        } else {
            TimerService.stopTimerService(getContext());
//            stopTimer(true);
        }
    }

    private void stopTimer(boolean shouldAnimBGColor, boolean shouldStopTimer) {
        handleTimerStates(CIRCLE_STOP);
        postTimerHandlerEvent(shouldAnimBGColor, shouldStopTimer);
        setTimer();
    }

    @Subscribe
    public void onTimerUIUpdateRequest(TimerUpdateRequestEvent event) {
        if (event.isAskingForCurrentState()) {
            sendTimeEvent.setMillisecs(millisecs);
            EventBus.getDefault().post(sendTimeEvent);
            mCircleProgressEvent.setCircleProgress(mAnimatedVal);
            EventBus.getDefault().post(mCircleProgressEvent);
            Log.i(TAG, "onTimerUIUpdateRequest: ");
            setOrChangeRandomExercise();
            postTimerHandlerEvent(false, false);
        }
    }

    @Subscribe
    public void onRequestForNewExercise(RequestForNewExerciseEvent event) {
        if (event.isNeedNewExercise()) {
            Log.i(TAG, "onRequestForNewExercise: ");
            appContext.setRandExerciseId();
            setOrChangeRandomExercise();
        }
    }

    /*
     * Circle handlers
     */

    /**
     * @param time   time for which the circle animation will happen
     * @param values the animated value will be calculating between these values
     * @return ValueAnimator, which later can be manipulated
     */
    private ValueAnimator getCircleAnimator(long time, float... values) {
        final ValueAnimator circleAnimator = ValueAnimator.ofFloat(values);
        circleAnimator.setDuration(time);
        circleAnimator.setInterpolator(new LinearInterpolator());
        circleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setTimer();
            }
        });
        circleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCircleProgressValue = (float) animation.getAnimatedValue();
                if (mCircleProgressValue == 1f) {
                    mCircleProgressValue = 0;
                }
                if (EventBus.getDefault().hasSubscriberForEvent(mCircleProgressEvent.getClass())) {
                    mCircleProgressEvent.setCircleProgress(mCircleProgressValue);
                    EventBus.getDefault().post(mCircleProgressEvent);
                }
            }

        });
        return circleAnimator;
    }

    /**
     * Handles mTimer circle states
     *
     * @param state the state of mTimer circle
     */
    private void handleTimerStates(int state) {
        switch (state) {
            case CIRCLE_STOP:
                getCircleAnimator((long) (mAnimatedVal * 1000), mAnimatedVal, 0f).start();
                break;
        }
    }

    public interface TimerTaskFragmentListener {
        void onExercisesIdsRequested();
    }

    private void logToFileMillisAndSecs(String identifyingText) {
        if (logToFile == null) {
            logToFile = new LogToFile(getContext(), "log.txt");
        }
        logToFile.appendLog(identifyingText + " millisecs (" + millisecs + ")   currSec (" + mCurrentSecond + ")     nextSec (" + mNextSecond + ")");
    }


    public void log() {
        Log.i(TAG, "service running : " + TimerService.serviceRunning);

//        int mCurrentType = TimerPreferenceManager.getCurrentType();
//        int mCurrentState = TimerPreferenceManager.getCurrentState();
//        int mPreviousType = TimerPreferenceManager.getPreviousType();
//        int mPreviousState = TimerPreferenceManager.getPreviousState();
//        Log.i(TAG, "TIMER TASK FROM PREFS: currType " + TimerHelper.getTimerStateOrTypeString(mCurrentType) + "  currState " + TimerHelper.getTimerStateOrTypeString(mCurrentState) +
//                "\n prevType " + TimerHelper.getTimerStateOrTypeString(mPreviousType) + "   prevState " + TimerHelper.getTimerStateOrTypeString(mPreviousState));

        int CurrentType = appContext.getCurrentType();
        int CurrentState = appContext.getCurrentState();
        int PreviousType = appContext.getPreviousType();
        int PreviousState = appContext.getPreviousState();

        Log.i(TAG, "TIMER TASK from context: currType " + TimerHelper.getTimerStateOrTypeString(CurrentType) + "  currState " + TimerHelper.getTimerStateOrTypeString(CurrentState) +
                "\n prevType " + TimerHelper.getTimerStateOrTypeString(PreviousType) + "   prevState " + TimerHelper.getTimerStateOrTypeString(PreviousState));
    }

    private void startTimerService() {
        Intent timerIntent = new Intent(getActivity(), TimerService.class);
        getContext().startService(timerIntent);
    }

    //Broadcast receiver

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TimerService.INTENT_TIMER_TICK);
        intentFilter.addAction(TimerService.INTENT_TIMER_FINISH);
        intentFilter.addAction(TimerService.INTENT_TIMER_STOP);
        intentFilter.addAction(TimerService.INTENT_TIMER_START_PAUSE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mLocalBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(TimerService.INTENT_TIMER_TICK)) {
                long time = intent.getLongExtra(TimerService.TIMER_TIME, 0);
                onTimerTick(time);
            } else if (action.equalsIgnoreCase(TimerService.INTENT_TIMER_FINISH)) {
                onTimerFinish();
            } else if (action.equalsIgnoreCase(TimerService.INTENT_TIMER_STOP)) {
                appContext.setAnimateViewPager(true);
                stopTimer(true, true);
            } else if (action.equalsIgnoreCase(TimerService.INTENT_TIMER_START_PAUSE)) {
                boolean shouldAnimBGColor = false;
                if (appContext.getCurrentState() == STATE_RUNNING && appContext.getPreviousState() == STATE_STOPPED) {
                    shouldAnimBGColor = true;
                }
                if (appContext.getPreviousState() == TimerTaskFragment.STATE_FINISHED) {
                    appContext.setAnimateViewPager(true);
                }
                if (appContext.isSessionFinished()) {
                    appContext.setSessionFinished(false);
                    appContext.setAnimateViewPager(true);
                }
                postTimerHandlerEvent(shouldAnimBGColor, false);
            }
        }
    };

    /*
     *   TimerService conncetion
     */
    // TimerService vars
//    private TimerService mTimerService;
//    private boolean mBound = false;
//
//    private ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            TimerService.LocalTimerBinder binder = (TimerService.LocalTimerBinder) service;
//            mTimerService = binder.getTimerService();
//            mBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            mBound = false;
//        }
//    };
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        Intent intent = new Intent(getActivity(), TimerService.class);
//        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        getActivity().unbindService(mConnection);
//    }

}
