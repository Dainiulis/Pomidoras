package com.dmiesoft.fitpomodoro.ui.fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.dmiesoft.fitpomodoro.events.timer_handling.CircleProgressEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.ExerciseIdSendEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerSendTimeEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerTypeStateHandlerEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerUpdateRequestEvent;
import com.dmiesoft.fitpomodoro.model.Favorite;
import com.dmiesoft.fitpomodoro.ui.activities.SettingsActivity;

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

    private int mCurrentState, mCurrentType, mPreviousState;
    private static final String TAG = "TTF";
    private CountDownTimer timer;
    private long millisecs, mExerciseId;
    private int longBreakCounter;
    private SharedPreferences sharedPref;
//    private TimerTypeStateHandlerEvent timerHandlerEvent;
    private TimerSendTimeEvent sendTimeEvent;
    private TimerTaskFragmentListener mListener;
    private Favorite favorite;

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

    private List<Long> mExercisesIds;

    public TimerTaskFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

//        timerHandlerEvent = new TimerTypeStateHandlerEvent();
        mCircleProgressEvent = new CircleProgressEvent();
        sendTimeEvent = new TimerSendTimeEvent();
        setmCurrentState(STATE_STOPPED);
        setmCurrentType(TYPE_WORK);
        longBreakCounter = 0;
        mExerciseId = -1;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setTimer();
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

        initStartingTimerAnimVars();
    }

    /**
     * mAnimatedVal - the animated value, it is variable;
     * mAnimationFraction - sort of interpolation. Constant value for every timer
     * mNextSecond - the next decreasing second. It is required for drawing timer. See initTimer()
     */
    private void initStartingTimerAnimVars() {
        mAnimatedVal = 0;
        mAnimationFraction = (float) 1000 / (float) millisecs;
        mNextSecond = (int) (millisecs / 1000) - 1;
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
        timer = new CountDownTimer(millisecs, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                /*
                 * How timer animation works...
                 *
                 * It gets mCurrentSecond - the current second as Integer
                 * then it evaluates if current second is equal to next second
                 * and only then the value for timer animation drawing is send
                 * the animated value gets added the animated fraction...
                 */
                mCurrentSecond = (int) (millisUntilFinished / 1000);
                if (mNextSecond == mCurrentSecond) {
                    mNextSecond = mCurrentSecond - 1;
                    mAnimatedVal += mAnimationFraction;
                    sendAnimatedTimerValue();
                }

                millisecs = millisUntilFinished;
                if (EventBus.getDefault().hasSubscriberForEvent(sendTimeEvent.getClass())) {
                    sendTimeEvent.setMillisecs(millisecs);
                    EventBus.getDefault().post(sendTimeEvent);
                }
            }

            @Override
            public void onFinish() {
                timer.cancel();
                //this part is required to clear timer animation
                mAnimatedVal = 0;
                sendAnimatedTimerValue();
                /*
                 *skaiciuoja kada bus ilga pertrauka
                 */
                if (mCurrentType == TYPE_WORK) {
                    longBreakCounter++;
                }
                /*
                 * isimena koks buvo laikmacio tipas
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
                    if (isContinuous()) {
                        mExerciseId = -1;
                    }
                }
                setTimer();
                mPreviousState = mCurrentState;
                boolean sessionFinished = false;
                if (isContinuous() && previousType != TYPE_LONG_BREAK) {
                    initTimer();
                } else if (previousType == TYPE_LONG_BREAK) {
                    setmCurrentState(STATE_STOPPED);
                    sessionFinished = true;
                } else {
                    setmCurrentState(STATE_FINISHED);
                }
                postCurrentStateAndType(true, sessionFinished);
                if (mCurrentType == TYPE_SHORT_BREAK || mCurrentType == TYPE_LONG_BREAK) {
                    sendRandomExerciseId();
                }
            }
        };
        timer.start();
    }

    /**
     * Sends animated value for timer through EventBus
     */
    private void sendAnimatedTimerValue() {
        if (EventBus.getDefault().hasSubscriberForEvent(mCircleProgressEvent.getClass())) {
            mCircleProgressEvent.setCircleProgress(mAnimatedVal);
            EventBus.getDefault().post(mCircleProgressEvent);
        }
    }

    private void postCurrentStateAndType(boolean shouldAnimate, boolean sessionFinished) {
        TimerTypeStateHandlerEvent timerHandlerEvent = new TimerTypeStateHandlerEvent();
        timerHandlerEvent.setSessionFinished(sessionFinished);
        timerHandlerEvent.setShouldAnimate(shouldAnimate);
        timerHandlerEvent.setCurrentState(mCurrentState);
        timerHandlerEvent.setCurrentType(mCurrentType);
        EventBus.getDefault().post(timerHandlerEvent);
    }

    public void setmCurrentState(int mCurrentState) {
        this.mCurrentState = mCurrentState;
    }

    public void setmCurrentType(int mCurrentType) {
        this.mCurrentType = mCurrentType;
    }


    public void setmExercisesIds(List<Long> mExercisesIds) {
        this.mExercisesIds = mExercisesIds;
    }

    /**
     * Request mainActivity for all exercises id's
     * (Later update to request for favorite exercises)
     */
    private void requestExercisesIds() {
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
        EventBus.getDefault().post(new ExerciseIdSendEvent(mExerciseId));
    }

    @Subscribe
    public void onTimerButtonClicked(TimerTypeStateHandlerEvent event) {
        if (event.getPublisher() != TimerTypeStateHandlerEvent.PUBLISHER_TIMER_UI_FRAGMENT) {
            return;
        }
        if (event.getCurrentType() == TYPE_WORK) {
            mExerciseId = -1;
        }
        mPreviousState = mCurrentState;
        if (event.getCurrentState() == STATE_RUNNING) {
            //butinai reikejo patikrinti sita salyga, kitaip buginosi laikmatis
            if (mCurrentState != STATE_RUNNING) {
                if (mPreviousState == STATE_STOPPED || mPreviousState == STATE_FINISHED) {
                    requestExercisesIds();
                }
                initTimer();
            }
        } else if (event.getCurrentState() == STATE_PAUSED) {
            timer.cancel();
//            handleTimerStates(CIRCLE_PAUSE);
            mCurrentState = STATE_PAUSED;
        } else if (event.getCurrentState() == STATE_STOPPED) {
//            if (mTimerAnimator != null) {
//            }
            handleTimerStates(CIRCLE_STOP);
            timer.cancel();
            longBreakCounter = 0;
            mExerciseId = -1;
            mCurrentType = TYPE_WORK;
            mCurrentState = STATE_STOPPED;
            postCurrentStateAndType(true, false);
            setTimer();
        }
    }

    @Subscribe
    public void onTimerUIUpdateRequest(TimerUpdateRequestEvent event) {
        if (event.isAskingForCurrentState()) {
            if (mCurrentState != STATE_RUNNING) {
                sendTimeEvent.setMillisecs(millisecs);
                EventBus.getDefault().post(sendTimeEvent);
            }
            if (mCurrentState == STATE_PAUSED) {
                mCircleProgressEvent.setCircleProgress(mAnimatedVal);
                EventBus.getDefault().post(mCircleProgressEvent);
            }
            sendRandomExerciseId();
            postCurrentStateAndType(false, false);
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
     * Handles timer circle states
     *
     * @param state the state of timer circle
     */
    private void handleTimerStates(int state) {
        switch (state) {
            case CIRCLE_STOP:
//                mTimerAnimator.cancel();
                getCircleAnimator((long) (mAnimatedVal * 1000), mAnimatedVal, 0f).start();
                break;
            //not used
            case CIRCLE_PAUSE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mTimerAnimator.pause();
                } else {
                    mTimerAnimator.cancel();
                }
                break;
            //not used
            case CIRCLE_RESUME:
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
}
