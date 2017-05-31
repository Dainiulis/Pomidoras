package com.dmiesoft.fitpomodoro.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.dmiesoft.fitpomodoro.application.FitPomodoroApplication;
import com.dmiesoft.fitpomodoro.events.exercises.RequestForNewExerciseEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.CircleProgressEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.ExerciseIdSendEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerAnimationStatusEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerSendTimeEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerTypeStateHandlerEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerUpdateRequestEvent;
import com.dmiesoft.fitpomodoro.services.TimerService;
import com.dmiesoft.fitpomodoro.ui.activities.SettingsActivity;
import com.dmiesoft.fitpomodoro.utils.LogToFile;
import com.dmiesoft.fitpomodoro.utils.helpers.NotificationHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.TimerHelper;
import com.dmiesoft.fitpomodoro.utils.preferences.TimerPreferenceHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Random;

public class TimerTaskFragment extends Fragment {

    public static final int STATE_STOPPED = 90;
    public static final int STATE_RUNNING = 91;
    public static final int STATE_PAUSED = 92;
    public static final int STATE_FINISHED = 93;
    public static final int TYPE_WORK = 200;
    public static final int TYPE_SHORT_BREAK = 201;
    public static final int TYPE_LONG_BREAK = 202;

    private int mCurrentState, mCurrentType, mPreviousState, mPreviousType;
    private static final String TAG = "TTF";
    private CountDownTimer mTimer;
    private long millisecs, mExerciseId, mInitializedMillisecs;
    private int mLongBreakCounter;
    private SharedPreferences sharedPref, timerPrefs;
    private TimerSendTimeEvent sendTimeEvent;
    private TimerTaskFragmentListener mListener;
    private boolean mShouldAnimate;

    ////////// Circle variables /////////////////
    static final int CIRCLE_RESUME = 7235;
    static final int CIRCLE_STOP = 2864;
    static final int CIRCLE_PAUSE = 9898;
    private ValueAnimator mTimerAnimator;
    private CircleProgressEvent mCircleProgressEvent;
    private float mCircleProgressValue;

    private float mAnimatedVal, mAnimationFraction;
    private int mNextSecond, mCurrentSecond;
    /////////////////////////////////////////////

    //Notifications
//    private NotificationCompat.Builder mTimeNotificationBuilder;
//    private NotificationManager mNotificationManager;
//    private PendingIntent mPendingIntentForFinishingNotification;

    private List<Long> mExercisesIds;
    private boolean misSessionFinished;
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

        // init notification stuff
//        mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        mPendingIntentForFinishingNotification = NotificationHelper.getPendingIntentForFinishingNotification(getContext());
        // init events
        mCircleProgressEvent = new CircleProgressEvent();
        sendTimeEvent = new TimerSendTimeEvent();
        timerPrefs = getContext().getSharedPreferences(TimerPreferenceHelper.TIMER_PARAMS_PREF, Context.MODE_PRIVATE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //set starting state, type, variables...
        mCurrentState = STATE_STOPPED;
        mCurrentType = TYPE_WORK;
        setmCurrentState(STATE_STOPPED);
        setmCurrentType(TYPE_WORK);
        setmLongBreakCounter(0);
        mExerciseId = -1;
        setTimer();
        appContext = (FitPomodoroApplication) getActivity().getApplicationContext();

        registerBroadcastReceiver();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TimerTaskFragmentListener) {
            mListener = (TimerTaskFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement TimerTaskFragmentListener");
        }
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
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    //  **Handle millisecs and mTimer**
    private void setTimer() {
        millisecs = getMillisecs(getDefaultMins());
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

    private int getWhenLongBreak() {
        return sharedPref.getInt(SettingsActivity.PREF_KEY_WHEN_LONG_BREAK, 4);
    }

    /**
     * @return true if sharedPref is set for continuous mode
     */
    private boolean isContinuous() {
        return sharedPref.getBoolean(SettingsActivity.PREF_CONTINUOUS_MODE, false);
    }

    /**
     * @return true if app is set to auto open when finished
     */
    private boolean isAutoOpen() {
        return sharedPref.getBoolean(SettingsActivity.PREF_AUTO_OPEN_WHEN_TIMER_FINISH, false);
    }

    private long getMillisecs(long minutes) {

        //while developing, when released, leave multiplier = 60000;
        long multiplier = 1000;
        if (!sharedPref.getBoolean(SettingsActivity.PREF_TIMER_TIME_FOR_TESTING, true)) {
            multiplier = 60000;
        }
        return minutes * multiplier;
    }

    private long getDefaultMins() {
        int defMinutes = 0;
        if (mCurrentType == TYPE_WORK) {
            defMinutes = sharedPref.getInt(SettingsActivity.PREF_KEY_WORK_TIME, 25);
        } else {
//            if (mLongBreakCounter == getWhenLongBreak())
            if (mLongBreakCounter == getWhenLongBreak()) {
                defMinutes = sharedPref.getInt(SettingsActivity.PREF_KEY_LONG_BREAK_TIME, 15);
                setmLongBreakCounter(0);
            } else {
                defMinutes = sharedPref.getInt(SettingsActivity.PREF_KEY_REST_TIME, 5);
            }
        }
        return (long) defMinutes;
    }
//  *********************************

    private void initTimer() {
//        initTimeNotifBuilder();
        mTimer = new CountDownTimer(millisecs, 300) {
            @Override
            public void onTick(long millisUntilFinished) {
                onTimerTick(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                mTimer.cancel();
                if (mCurrentSecond != 0) {
                    initTimer();
                } else {
                    onTimerFinish();
                }
            }
        };
        mTimer.start();
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
                /*
                 *skaiciuoja kada bus ilga pertrauka
                 */
        if (mCurrentType == TYPE_WORK) {
            setmLongBreakCounter(++mLongBreakCounter);
        }
                /*
                 * jei naudotojas pakeicia ilgos pertraukos kintamaji i mazesni nei yra suskaiciuotas
                 * tuomet pradedamas ilgosios pertraukos laikmatis
                 */
        if (mLongBreakCounter > getWhenLongBreak()) {
            setmLongBreakCounter(getWhenLongBreak());
        }
                /*
                 * jei ilgoji pertrauka, tuomet pradedama
                 * kitaip pradedama trumpoji pertrauka
                 */
        if (mLongBreakCounter == getWhenLongBreak()) {
            setmCurrentType(TYPE_LONG_BREAK);
        } else if (mLongBreakCounter != 0) {
            setmCurrentType(TYPE_SHORT_BREAK);
        }
                /*
                 * patikrinama kokio tipo laikmatis buvo
                 * jei trumpos ar ilgos pertraukos, tuomet nustatomas darbinis
                 */
        if (mPreviousType == TYPE_SHORT_BREAK || mPreviousType == TYPE_LONG_BREAK || mLongBreakCounter == 0) {
            setmCurrentType(TYPE_WORK);
            if (isContinuous()) {
                mExerciseId = -1;
            }
        }
        setTimer();

        // misSessionFinished is used to determine whether
        // the whole work session has finished or not
        misSessionFinished = false;
        boolean shouldAnimateBackgroundColor = false;
        if (isContinuous() && mPreviousType != TYPE_LONG_BREAK) {
            setmCurrentState(STATE_RUNNING);
            startTimerService();
        } else if (mPreviousType == TYPE_LONG_BREAK) {
            setmCurrentState(STATE_STOPPED);
            misSessionFinished = true;
            shouldAnimateBackgroundColor = true;
        } else {
            setmCurrentState(STATE_FINISHED);
        }
        mShouldAnimate = true;
        postCurrentStateAndType(mShouldAnimate, misSessionFinished, shouldAnimateBackgroundColor);
        if (mCurrentType == TYPE_SHORT_BREAK || mCurrentType == TYPE_LONG_BREAK) {
            sendRandomExerciseId();
        }
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

    private void postCurrentStateAndType(boolean shouldAnimate, boolean sessionFinished, boolean shouldAnimateBackgoundColor) {
        TimerTypeStateHandlerEvent timerHandlerEvent = new TimerTypeStateHandlerEvent();
        timerHandlerEvent.setSessionFinished(sessionFinished);
        timerHandlerEvent.setShouldAnimate(shouldAnimate);
        timerHandlerEvent.setPreviousState(mPreviousState);
        timerHandlerEvent.setPreviousType(mPreviousType);
        timerHandlerEvent.setCurrentState(mCurrentState);
        timerHandlerEvent.setCurrentType(mCurrentType);
        timerHandlerEvent.setShouldAnimateBackgroundColor(shouldAnimateBackgoundColor);
        EventBus.getDefault().post(timerHandlerEvent);
    }

    public void setmLongBreakCounter(int mLongBreakCounter) {
        this.mLongBreakCounter = mLongBreakCounter;
        timerPrefs.edit()
                .putInt(TimerPreferenceHelper.LONG_BREAK_COUNTER, mLongBreakCounter)
                .apply();
    }

    public void setmCurrentState(int currentState) {
        this.mPreviousState = this.mCurrentState;
        this.mCurrentState = currentState;
        timerPrefs.edit()
                .putInt(TimerPreferenceHelper.CURRENT_TIMER_STATE, mCurrentState)
                .putInt(TimerPreferenceHelper.PREVIOUS_TIMER_STATE, mPreviousState)
                .apply();
    }

    public void setmCurrentType(int currentType) {
        this.mPreviousType = mCurrentType;
        this.mCurrentType = currentType;
        timerPrefs.edit()
                .putInt(TimerPreferenceHelper.CURRENT_TIMER_TYPE, mCurrentType)
                .putInt(TimerPreferenceHelper.PREVIOUS_TIMER_TYPE, mPreviousType)
                .apply();
    }

    public void setmExercisesIds(List<Long> mExercisesIds) {
        this.mExercisesIds = mExercisesIds;
    }

    /**
     * Request mainActivity for all exercises id's
     * (Later update to request for favorite exercises)
     */
    public void requestExercisesIds() {
        mListener.onExercisesIdsRequested();
    }

    /**
     * Send random exercise id through eventbus
     */
    private void sendRandomExerciseId() {
        if (mExercisesIds != null) {
            if (mExerciseId == -1 && mExercisesIds.size() > 0) {
                Random randomGenerator = new Random();
                int index = randomGenerator.nextInt(mExercisesIds.size());
                mExerciseId = mExercisesIds.get(index);
            }
        }
        if (EventBus.getDefault().hasSubscriberForEvent(ExerciseIdSendEvent.class)) {
            EventBus.getDefault().post(new ExerciseIdSendEvent(mExerciseId));
        }
    }

    @Subscribe
    public void onTimerButtonClicked(TimerTypeStateHandlerEvent event) {
        if (event.getPublisher() != TimerTypeStateHandlerEvent.PUBLISHER_TIMER_UI_FRAGMENT) {
            return;
        }
        if (event.getCurrentType() == TYPE_WORK) {
            mExerciseId = -1;
        }
        setmCurrentState(event.getCurrentState());
        if (event.getCurrentState() == STATE_RUNNING) {
            misSessionFinished = false;
            if (mPreviousState == STATE_STOPPED || mPreviousState == STATE_FINISHED) {
                requestExercisesIds();
            }
            startTimerService();
//            initTimer();
        } else if (event.getCurrentState() == STATE_PAUSED) {
            startTimerService();
//            mTimer.cancel();
        } else if (event.getCurrentState() == STATE_STOPPED) {
            if (!event.isSessionFinished()) {
                misSessionFinished = false;
            }
            boolean shouldAnimate = event.isShouldAnimate();
            TimerService.stopTimerService(getContext());
            stopTimer(shouldAnimate, event.isShouldAnimateBackgroundColor());
        }
    }

    private void stopTimer(boolean shouldAnimate, boolean shouldAnimateBackGround) {
        handleTimerStates(CIRCLE_STOP);
        setmLongBreakCounter(0);
        mExerciseId = -1;
        setmCurrentType(TYPE_WORK);
        if (mCurrentState != STATE_STOPPED) {
            setmCurrentState(STATE_STOPPED);
        }
        postCurrentStateAndType(shouldAnimate, misSessionFinished, shouldAnimateBackGround);
        setTimer();
    }

    @Subscribe
    public void onTimerUIUpdateRequest(TimerUpdateRequestEvent event) {
        if (event.isAskingForCurrentState()) {
            sendTimeEvent.setMillisecs(millisecs);
            EventBus.getDefault().post(sendTimeEvent);
            mCircleProgressEvent.setCircleProgress(mAnimatedVal);
            EventBus.getDefault().post(mCircleProgressEvent);
            sendRandomExerciseId();
            postCurrentStateAndType(mShouldAnimate, misSessionFinished, false);
        }
    }

    @Subscribe
    public void onAnimationEnded(TimerAnimationStatusEvent event) {
        if (event.isAnimated()) {
            mShouldAnimate = false;
        }
    }

    @Subscribe
    public void onRequestForNewExercise(RequestForNewExerciseEvent event) {
        if (event.isNeedNewExercise()) {
            mExerciseId = -1;
            sendRandomExerciseId();
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
//                mTimerAnimator.cancel();
                getCircleAnimator((long) (mAnimatedVal * 1000), mAnimatedVal, 0f).start();
                break;
            //not used
            case CIRCLE_PAUSE: //not used
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mTimerAnimator.pause();
                } else {
                    mTimerAnimator.cancel();
                }
                break;
            //not used
            case CIRCLE_RESUME: // not used
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mTimerAnimator.resume();
                } else {
                    mTimerAnimator = getCircleAnimator(millisecs, mCircleProgressValue, 1f);
                    mTimerAnimator.start();
                }
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
        Log.i(TAG, "log: " + Thread.getAllStackTraces().keySet());
    }

    private void startTimerService() {
        Intent timerIntent = new Intent(getActivity(), TimerService.class);
        timerIntent.putExtra(TimerService.STARTING_TIME, getMillisecs(getDefaultMins()));
        getContext().startService(timerIntent);
    }

    //Broadcast receiver

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TimerService.INTENT_TIMER_TICK);
        intentFilter.addAction(TimerService.INTENT_TIMER_FINISH);
        intentFilter.addAction(TimerService.INTENT_TIMER_STOP);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(TimerService.INTENT_TIMER_TICK)) {
                long time = intent.getLongExtra(TimerService.TIMER_TIME, 0);
                onTimerTick(time);
            } else if (action.equalsIgnoreCase(TimerService.INTENT_TIMER_FINISH)) {
                onTimerFinish();
            } else if (action.equalsIgnoreCase(TimerService.INTENT_TIMER_STOP)) {
                stopTimer(true, false);
            }
        }
    };

    /*
        TimerService conncetion
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
