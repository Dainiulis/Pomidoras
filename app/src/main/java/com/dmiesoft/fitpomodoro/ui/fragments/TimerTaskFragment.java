package com.dmiesoft.fitpomodoro.ui.fragments;

import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.dmiesoft.fitpomodoro.application.GlobalVariables;
import com.dmiesoft.fitpomodoro.events.exercises.RequestForNewExerciseEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.CircleProgressEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.ExerciseIdSendEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerAnimationStatusEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerSendTimeEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerTypeStateHandlerEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerUpdateRequestEvent;
import com.dmiesoft.fitpomodoro.ui.activities.SettingsActivity;
import com.dmiesoft.fitpomodoro.utils.helpers.TimerHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.NotificationHelper;

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
    private CountDownTimer timer;
    private long millisecs, mExerciseId;
    private int longBreakCounter;
    private SharedPreferences sharedPref;
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
    private NotificationCompat.Builder mTimeNotificationBuilder, mTimerFinishNotificationBuilder;
    private NotificationManager mNotificationManager;

    private List<Long> mExercisesIds;
    private boolean misSessionFinished;
    private PendingIntent mPendingIntentForFinishingNotification;
    private GlobalVariables appContext;

    public TimerTaskFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // init notification stuff
        mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mPendingIntentForFinishingNotification = NotificationHelper.getPendingIntentForFinishingNotification(getContext());
        // init events
        mCircleProgressEvent = new CircleProgressEvent();
        sendTimeEvent = new TimerSendTimeEvent();

        //set starting state, type, variables...
        setmCurrentState(STATE_STOPPED);
        setmCurrentType(TYPE_WORK);
        longBreakCounter = 0;
        mExerciseId = -1;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setTimer();
        appContext = (GlobalVariables) getActivity().getApplicationContext();
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
        manualNotificationsClear();
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
        mTimeNotificationBuilder = NotificationHelper.getTimerTimeNotificationBuilder(
                getContext(), mCurrentType, mPendingIntentForFinishingNotification);
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
                    if (mCurrentSecond == 0) {
                        cancelNotification();
                    }
                }

                millisecs = millisUntilFinished;
                if (EventBus.getDefault().hasSubscriberForEvent(sendTimeEvent.getClass())) {
                    sendTimeEvent.setMillisecs(millisecs);
                    EventBus.getDefault().post(sendTimeEvent);
                }
                updateNotificationTimer();
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
                } else if (longBreakCounter != 0) {
                    setmCurrentType(TYPE_SHORT_BREAK);
                }
                /*
                 * patikrinama kokio tipo laikmatis buvo
                 * jei trumpos ar ilgos pertraukos, tuomet nustatomas darbinis
                 */
                if (mPreviousType == TYPE_SHORT_BREAK || mPreviousType == TYPE_LONG_BREAK || longBreakCounter == 0) {
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
                    initTimer();
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
        };
        timer.start();
    }

    private void cancelNotification() {
        if (mCurrentState != STATE_STOPPED) {
            manageNotificationWithSound();
        }
        mNotificationManager.cancel(NotificationHelper.TIMER_TIME_NOTIFICATION);
        if (isAutoOpen()) {
            try {
                mPendingIntentForFinishingNotification.send(NotificationHelper.RESULT_PENDING_INTENT_REQUEST_CODE);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        mTimeNotificationBuilder = null;
    }

    public void manualNotificationsClear() {
        mNotificationManager.cancel(NotificationHelper.TIMER_TIME_NOTIFICATION);
        mNotificationManager.cancel(NotificationHelper.TIMER_FINISHED_NOTIFICATION);
    }

    /**
     * if notification needs to play sound, it should cancel itself after the alarm sound
     */
    private void manageNotificationWithSound() {
        Uri uri = Uri.parse(NotificationHelper.URI_TO_PACKAGE +
                TimerHelper.getFromResources(mCurrentType, TimerHelper.RAW_SOUNDS));
        mTimerFinishNotificationBuilder = NotificationHelper.getFinishedNotificationBuilder(
                getContext(), mCurrentType);
        mTimerFinishNotificationBuilder.setSound(uri);
        mNotificationManager.notify(NotificationHelper.TIMER_FINISHED_NOTIFICATION, mTimerFinishNotificationBuilder.build());
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mNotificationManager.cancel(NotificationHelper.TIMER_FINISHED_NOTIFICATION);
            }
        };
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(getContext(), uri);
        long duration = Long.parseLong(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        if (isContinuous()) {
            handler.postDelayed(runnable, duration);
        }
    }

    /**
     * Updates the timer in notification
     */
    private void updateNotificationTimer() {
        if (mTimeNotificationBuilder != null) {
            try {
                mTimeNotificationBuilder.setContentTitle(TimerHelper.getTimerTypeName(mCurrentType) +
                        " time remaining - " +
                        TimerHelper.getTimerString(millisecs));
                Notification notification = mTimeNotificationBuilder.build();
                mNotificationManager.notify(NotificationHelper.TIMER_TIME_NOTIFICATION, notification);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
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

    public void setmCurrentState(int currentState) {
        this.mPreviousState = this.mCurrentState;
        this.mCurrentState = currentState;
    }

    public void setmCurrentType(int currentType) {
        this.mPreviousType = mCurrentType;
        this.mCurrentType = currentType;
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
            initTimer();
            manualNotificationsClear();
        } else if (event.getCurrentState() == STATE_PAUSED) {
            timer.cancel();
        } else if (event.getCurrentState() == STATE_STOPPED) {
            if (!event.isSessionFinished()) {
                misSessionFinished = false;
            }
            boolean shouldAnimate = event.isShouldAnimate();
            stopTimer(shouldAnimate, event.isShouldAnimateBackgroundColor());
            manualNotificationsClear();
        }
    }

    private void stopTimer(boolean shouldAnimate, boolean shouldAnimateBackGround) {
        Log.i(TAG, "stopTimer: ");
        handleTimerStates(CIRCLE_STOP);
        timer.cancel();
        longBreakCounter = 0;
        mExerciseId = -1;
        setmCurrentType(TYPE_WORK);
        postCurrentStateAndType(shouldAnimate, misSessionFinished, shouldAnimateBackGround);
        setTimer();
    }

    public void stopTimerFromNotification() {
        setmCurrentState(STATE_STOPPED);
        stopTimer(true, false);
        mNotificationManager.cancel(NotificationHelper.TIMER_TIME_NOTIFICATION);
    }

    @Subscribe
    public void onTimerUIUpdateRequest(TimerUpdateRequestEvent event) {
        if (event.isAskingForCurrentState()) {
//            nesuprantu kam as cia buvau i if'us sukises
//            if (mCurrentState != STATE_RUNNING) {
//                sendTimeEvent.setMillisecs(millisecs);
//                EventBus.getDefault().post(sendTimeEvent);
//            }
//            if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_FINISHED) {
//                mCircleProgressEvent.setCircleProgress(mAnimatedVal);
//                EventBus.getDefault().post(mCircleProgressEvent);
//            }
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
            Log.i(TAG, "onAnimationEnded: " + event.isAnimated());
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

    public void log() {

    }
}
