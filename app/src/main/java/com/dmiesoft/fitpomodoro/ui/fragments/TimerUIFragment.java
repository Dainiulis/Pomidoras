package com.dmiesoft.fitpomodoro.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.application.GlobalVariables;
import com.dmiesoft.fitpomodoro.events.exercises.RequestForNewExerciseEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.CircleProgressEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.ExerciseIdSendEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerAnimationStatusEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerSendTimeEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerTypeStateHandlerEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerUpdateRequestEvent;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.Favorite;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.nested.ExerciseInTimerUIFragment;
import com.dmiesoft.fitpomodoro.utils.customViews.CustomTimerView;
import com.dmiesoft.fitpomodoro.utils.helpers.DisplayHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.TimerHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Vector;


public class TimerUIFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private static final int BTN_START = 1001;
    private static final int BTN_PAUSE = 1002;
    private static final int BTN_STOP = 1003;
    public static final String TAG = "TIMER";
    public static final String SELECTED_FAVORITE = "selected_favorite";

    private int mCurrentState, mCurrentType, mPreviousState, mPreviousType;
    private long mMillisecs;
    private CustomTimerView mCustomTimerView;
    private FloatingActionButton mMainFab;
    private TimerUIFragmentListener mListener;
    private ViewPager mViewPager;
    private View mFakeView;
    private ObjectAnimator mCustomTimerAnimator, mCustomViewPagerAnimator;
    private Property<View, Float> mPropertyName;
    private boolean mShouldAnimate;
    private LinearLayout container;
    private List<Favorite> favorites;
    private ArrayAdapter<Favorite> mSpinnerAdapter;
    private SharedPreferences sharedPrefs;
    private boolean misSessionFinished;
    private ExercisePagerAdapter mPagerAdapter;
    private View view;
    private AlertDialog alertDialog;
    private GlobalVariables appContext;

    public TimerUIFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShouldAnimate = false;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        appContext = (GlobalVariables) getActivity().getApplicationContext();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainFab = ((MainActivity) getActivity()).getMainFab();
        if (mMainFab != null) {
            mMainFab.hide();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TimerUIFragmentListener) {
            mListener = (TimerUIFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement TimerUIFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mListener.onFavoritesListRequested();
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_timer, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timer_menu, menu);

        MenuItem item = menu.findItem(R.id.fav_spinner);
        final Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        int selectionPosition = 0;
        long selectedId = sharedPrefs.getLong(SELECTED_FAVORITE, -1);
        mSpinnerAdapter = new ArrayAdapter<Favorite>(getActivity(),
                R.layout.favorite_spinner_layout, favorites);
        spinner.setAdapter(mSpinnerAdapter);
        for (Favorite fav : favorites) {
            if (selectedId == fav.getId()) {
                selectionPosition = mSpinnerAdapter.getPosition(fav);
            }
        }
        spinner.setSelection(selectionPosition);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long favId = ((Favorite) parent.getItemAtPosition(position)).getId();
                sharedPrefs.edit().putLong(SELECTED_FAVORITE, favId).apply();
                mListener.onFavoriteSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        requestForTimerUpdate();
    }

    private void requestForTimerUpdate() {
        TimerUpdateRequestEvent timerUpdate = new TimerUpdateRequestEvent();
        timerUpdate.askForCurrentState(true);
        EventBus.getDefault().post(timerUpdate);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void setTimer() {
        mCustomTimerView.setmTimerText(TimerHelper.getTimerString(mMillisecs));
    }

    private void initViews(View view) {
        container = (LinearLayout) view.findViewById(R.id.content_fragment_timer);
        mFakeView = view.findViewById(R.id.fake_view);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setVisibility(View.GONE);

        mCustomTimerView = (CustomTimerView) view.findViewById(R.id.customTimer);
        mCustomTimerView.setOnClickListener(this);
        mCustomTimerView.setOnLongClickListener(this);
    }

//    private String getTimerString(long millisUntilFinished) {
//        long sec = (millisUntilFinished / 1000) % 60;
//        long min = (millisUntilFinished / 60000);
//        return String.format("%02d:%02d", min, sec);
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.customTimer:
                handleBtnStartPause();
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.customTimer:
                if (mCurrentState != TimerTaskFragment.STATE_STOPPED) {
                    initAlertDialog("Are you sure?", "Would you like to end session?", BTN_STOP);
                    return true;
                }
        }
        return false;
    }

    //  *********Handle buttons**********
    private void handleBtnStop() {
        mShouldAnimate = (mViewPager.getVisibility() == View.VISIBLE);
        setBtnTypes(BTN_STOP);
        timerHandler(mShouldAnimate, TimerTaskFragment.STATE_STOPPED);
    }

    private void handleBtnStartPause() {
        if (mCustomTimerView.getTag().equals(BTN_START)) {
            setBtnTypes(BTN_START);
            boolean shouldAnimate;
            if ((mCurrentState == TimerTaskFragment.STATE_FINISHED || mViewPager.getVisibility() == View.VISIBLE)
                    && mCurrentState != TimerTaskFragment.STATE_PAUSED) {
                shouldAnimate = true;
            } else {
                shouldAnimate = false;
            }
            timerHandler(shouldAnimate, TimerTaskFragment.STATE_RUNNING);
        } else if (mCustomTimerView.getTag().equals(BTN_PAUSE)) {
            setBtnTypes(BTN_PAUSE);
            timerHandler(false, TimerTaskFragment.STATE_PAUSED);
        }
    }

    private void timerHandler(boolean shouldAnimate, int currentState) {
        TimerTypeStateHandlerEvent timerHandler = new TimerTypeStateHandlerEvent();
        timerHandler.setCurrentState(currentState);
        timerHandler.setPreviousType(mPreviousType);
        timerHandler.setPreviousState(mCurrentState);
        timerHandler.setCurrentType(mCurrentType);
        timerHandler.setShouldAnimate(shouldAnimate);
        timerHandler.setPublisher(TimerTypeStateHandlerEvent.PUBLISHER_TIMER_UI_FRAGMENT);
        EventBus.getDefault().post(timerHandler);
    }
//  *********************************************

    //      Change buttons and timer textview according to btn tag
    private void setBtnTypes(int clickedButtonTag) {
        if (clickedButtonTag == BTN_START) {
            mCustomTimerView.setTag(BTN_PAUSE);
        } else if (clickedButtonTag == BTN_PAUSE) {
            mCustomTimerView.setTag(BTN_START);
        } else {
            mCustomTimerView.setTag(BTN_START);
        }
    }

    /**
     * This method is used to animate custom timer position on screen
     */
    private void animateCustomTimer(final boolean hideViewPager, float... values) {
        setPropertyName();
        if (hideViewPager) {
            hideViewPager();
        }
        int duration = 1000;
        extraTimerAnimation(duration);
        mCustomTimerAnimator = ObjectAnimator.ofFloat(mCustomTimerView, mPropertyName, values);
        mCustomTimerAnimator.setDuration(duration);
        mCustomTimerAnimator.setInterpolator(new AnticipateOvershootInterpolator());
        mCustomTimerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mCustomTimerView.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCustomTimerView.setClickable(true);
                // next 3 lines are used to reset the position of timer
                mCustomTimerAnimator.setDuration(0);
                mCustomTimerAnimator.setFloatValues((float) mCustomTimerAnimator.getAnimatedValue(), 0);
                mCustomTimerAnimator.start();
                if (hideViewPager) {
                    mFakeView.setVisibility(View.VISIBLE);
                } else {
                    mFakeView.setVisibility(View.GONE);
                    mViewPager.setVisibility(View.INVISIBLE);
                    animateViewPager(false, 0f, 1f);
                }
                mShouldAnimate = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mCustomTimerAnimator.start();
    }

    private void extraTimerAnimation(int duration) {
        // Little extra animation, not necessary
        // maybe remove later
        mCustomTimerView.animate()
                .setDuration(duration / 2)
                .alpha(.2f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCustomTimerView.animate()
                                .setDuration(animation.getDuration())
                                .alpha(1f)
                                // required because listener is continuing to run
                                // idk why
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        animation.removeAllListeners();
                                    }
                                });
                    }
                });
    }

    private void animateViewPager(final boolean hideViewPager, float... values) {
        if (!hideViewPager) {
            mViewPager.setVisibility(View.VISIBLE);
        }
        mCustomViewPagerAnimator = ObjectAnimator.ofFloat(mViewPager, View.ALPHA, values);
        mCustomViewPagerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (hideViewPager) {
                    animateCustomTimer(hideViewPager, -getAnimationDistance(.25f), 0f);
                }
                EventBus.getDefault().post(new TimerAnimationStatusEvent(true));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mCustomViewPagerAnimator.setInterpolator(new LinearInterpolator());
        mCustomViewPagerAnimator.start();
    }

    private void manageViewsAnimation() {
        Log.i(TAG, TimerHelper.getTimerStateOrTypeString(mCurrentState) +
                TimerHelper.getTimerStateOrTypeString(mCurrentType));
        if (!mShouldAnimate) {
            manageFVandVPVisibility();
        } else {
            if (mCurrentState == TimerTaskFragment.STATE_RUNNING) {
                if (mCurrentType == TimerTaskFragment.TYPE_WORK || mViewPager.getVisibility() == View.VISIBLE) {
                    animateViewPager(true, 1f, 0f);
                } else if (mCurrentType != TimerTaskFragment.TYPE_WORK && mViewPager.getVisibility() != View.VISIBLE) {
                    animateCustomTimer(false, 0f, -getAnimationDistance(.25f));
                }
            } else if (mCurrentState == TimerTaskFragment.STATE_STOPPED && mViewPager.getVisibility() == View.VISIBLE) {
                animateViewPager(true, 1f, 0f);
            } else if (mCurrentState == TimerTaskFragment.STATE_FINISHED && mCurrentType == TimerTaskFragment.TYPE_WORK) {
                showViewPager();
            }
        }
    }

    private void manageFVandVPVisibility() {
        if (mCurrentState == TimerTaskFragment.STATE_FINISHED) {
            if (mCurrentType == TimerTaskFragment.TYPE_WORK) {
                showViewPager();
            } else {
                hideViewPager();
            }
        } else {
            if (mCurrentType == TimerTaskFragment.TYPE_WORK) {
                hideViewPager();
            } else {
                showViewPager();
            }
        }
    }

    private void showViewPager() {
        mFakeView.setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);
    }

    private void hideViewPager() {
        mViewPager.setVisibility(View.GONE);
        mFakeView.setVisibility(View.VISIBLE);
    }

    /**
     * @param multiplier the number from which to multiply container size to calculate animation distance
     *                   currently 0.25 is sweet spot for customTimerView
     * @return
     */
    private float getAnimationDistance(float multiplier) {
        float animationDistance = 0;
        try {
            if (new DisplayHelper(getActivity()).getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
                animationDistance = container.getHeight() * multiplier;
            } else {
                animationDistance = container.getWidth() * multiplier;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return animationDistance;
    }

    private void setPropertyName() {
        if (getActivity() != null) {
            if (new DisplayHelper(getActivity()).getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
                mPropertyName = View.TRANSLATION_Y;
            } else {
                mPropertyName = View.TRANSLATION_X;
            }
        }
    }

    public void setExercise(Exercise exercise) {
        mPagerAdapter = new ExercisePagerAdapter(getChildFragmentManager(), exercise);
        mViewPager.setAdapter(mPagerAdapter);
    }

    @Subscribe
    public void onTimerChange(TimerSendTimeEvent event) {
        mMillisecs = event.getMillisecs();
        setTimer();
    }

    @Subscribe
    public void onTimerTypeStateRequest(TimerTypeStateHandlerEvent event) {
        mPreviousState = event.getPreviousState();
        mPreviousType = event.getPreviousType();
        mCurrentState = event.getCurrentState();
        mCurrentType = event.getCurrentType();
        mShouldAnimate = event.isShouldAnimate();
        mCustomTimerView.setmTimerStateAndType(mCurrentState, mCurrentType);
        misSessionFinished = event.isSessionFinished();
        if (mCurrentState == TimerTaskFragment.STATE_RUNNING) {
            setBtnTypes(BTN_START);
        } else if (mCurrentState == TimerTaskFragment.STATE_PAUSED || mCurrentState == TimerTaskFragment.STATE_FINISHED) {
            setBtnTypes(BTN_PAUSE);
        } else {
            setBtnTypes(BTN_STOP);
            if (misSessionFinished) {
                if (mViewPager.getVisibility() != View.VISIBLE) {
                    mViewPager.setVisibility(View.VISIBLE);
                }
                initAlertDialog("Session ended", "Did you finish your exercise?", BTN_START);
            }
        }
        if (!misSessionFinished) {
            managePagerAdapter();
            manageViewsAnimation();
        } else {
            mFakeView.setVisibility(View.GONE);
        }
    }

    /**
     * This method is used to determine whether to set adapter to null (required to call it's on destroy method) and reset GlobalVariables.
     * Basically it determines if exercises should be reset.
     */
    private void managePagerAdapter() {
        if (mCurrentState == TimerTaskFragment.STATE_RUNNING && mCurrentType == TimerTaskFragment.TYPE_WORK) {
            mViewPager.setAdapter(null);
            appContext.setReps(-1);
        }
    }

    @Subscribe
    public void onCircleProgressChanged(CircleProgressEvent event) {
        mCustomTimerView.drawProgress(event.getCircleProgress());
    }

    @Subscribe
    public void onRandExerciseIdReceived(ExerciseIdSendEvent event) {
        if (event.getExerciseId() != -1) {
            mListener.onRandomExerciseRequested(event.getExerciseId());
        }
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
        Favorite all = new Favorite();
        all.setId(-1);
        all.setName("All");
        favorites.add(0, all);
//        favorites.addAll(favorites);
    }

    /**
     * @param title
     * @param message
     * @param buttonHandler pass BTN_START for suggesting to start new session or BTN_STOP for stopping without suggestion
     */
    private void initAlertDialog(String title, String message, final int buttonHandler) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (buttonHandler == BTN_START) {
                    timerHandler(true, mCurrentState);
                } else {
                    handleBtnStop();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    public interface TimerUIFragmentListener {
        void onRandomExerciseRequested(long randExerciseId);

        void onFavoritesListRequested();

        void onFavoriteSelected();
    }

    private class ExercisePagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> fragments;

        public ExercisePagerAdapter(FragmentManager fm, Exercise exercise) {
            super(fm);
            fragments = new Vector<>();
            fragments.add(ExerciseInTimerUIFragment.newInstance(exercise, ExerciseInTimerUIFragment.PAGE_SAVE_PROGRESS));
            fragments.add(ExerciseInTimerUIFragment.newInstance(exercise, ExerciseInTimerUIFragment.PAGE_IMAGE));
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public void log() {

    }

}
