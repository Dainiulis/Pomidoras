package com.dmiesoft.fitpomodoro.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
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
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.application.FitPomodoroApplication;
import com.dmiesoft.fitpomodoro.events.timer_handling.CircleProgressEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.ChangeExerciseEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerAnimationStatusEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerSendTimeEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerHandlerEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerStateChanged;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerUpdateRequestEvent;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExerciseHistory;
import com.dmiesoft.fitpomodoro.model.Favorite;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.nested.NestedExerciseHistoryListFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.nested.NestedSaveExerciseFragment;
import com.dmiesoft.fitpomodoro.utils.customViews.CustomTimerView;
import com.dmiesoft.fitpomodoro.utils.helpers.DisplayHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.TimerHelper;
import com.dmiesoft.fitpomodoro.utils.preferences.TimerPreferenceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Vector;


public class TimerUIFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private static final int BTN_START = 1001;
    private static final int BTN_PAUSE = 1002;
    private static final int BTN_STOP = 1003;
    public static final String TAG = "TIMER";

    private CustomTimerView mCustomTimerView;
    private TimerUIFragmentListener mListener;
    private ViewPager mViewPager;
    private View mFakeView;
    private ObjectAnimator mCustomTimerAnimator, mCustomViewPagerAnimator;
    private Property<View, Float> mPropertyName;
    private LinearLayout container;
    private List<Favorite> favorites;
    private ExercisePagerAdapter mPagerAdapter;
    private View view;
    private AlertDialog alertDialog;
    private FitPomodoroApplication appContext;

    public TimerUIFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TimerPreferenceManager.initPreferences(getContext());
        appContext = (FitPomodoroApplication) getActivity().getApplicationContext();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton mMainFab = ((MainActivity) getActivity()).getMainFab();
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
        long selectedId = TimerPreferenceManager.getSelectedFavorite();
        ArrayAdapter<Favorite> mSpinnerAdapter = new ArrayAdapter<Favorite>(getActivity(),
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
                TimerPreferenceManager.saveSelectedFavorite(favId);
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

    private void setTimer(long millisecs) {
        mCustomTimerView.setmTimerText(TimerHelper.getTimerString(millisecs));
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
                if (appContext.getCurrentState() != TimerTaskFragment.STATE_STOPPED) {
                    initAlertDialog("Are you sure?", "Would you like to end session?", BTN_STOP);
                    return true;
                }
        }
        return false;
    }

    //  *********Handle buttons**********
    private void handleBtnStop() {
        appContext.setAnimateViewPager((mViewPager.getVisibility() == View.VISIBLE));
        setBtnTypes(BTN_STOP);
        timerHandler(true);
    }

    private void handleBtnStartPause() {
        if (mCustomTimerView.getTag().equals(BTN_START)) {
            setBtnTypes(BTN_START);
            if ((appContext.getCurrentState() == TimerTaskFragment.STATE_FINISHED || mViewPager.getVisibility() == View.VISIBLE)
                    && appContext.getCurrentState() != TimerTaskFragment.STATE_PAUSED) {
                appContext.setAnimateViewPager(true);
            } else {
                appContext.setAnimateViewPager(false);
            }
            timerHandler(false);
        } else if (mCustomTimerView.getTag().equals(BTN_PAUSE)) {
            setBtnTypes(BTN_PAUSE);
            timerHandler(false);
        }
    }

    private void timerHandler(boolean shouldStopTimer) {
        TimerHandlerEvent timerHandler = new TimerHandlerEvent();
        timerHandler.setPublisher(TimerHandlerEvent.PUBLISHER_TIMER_UI_FRAGMENT);
        timerHandler.setStopTimer(shouldStopTimer);
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
        final int duration = 1000;
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
                appContext.setAnimateViewPager(false);
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
                    clearViewPager();
                    animateCustomTimer(hideViewPager, -getAnimationDistance(.25f), 0f);
                }
                appContext.setAnimateViewPager(false);
//                EventBus.getDefault().post(new TimerAnimationStatusEvent(true));
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
        if (!appContext.shouldAnimateViewPager()) {
            manageFVandVPVisibility();
        } else {
            if (appContext.getCurrentState() == TimerTaskFragment.STATE_RUNNING) {
                if (appContext.getCurrentType() == TimerTaskFragment.TYPE_WORK || mViewPager.getVisibility() == View.VISIBLE) {
                    animateViewPager(true, 1f, 0f);
                } else if (appContext.getCurrentType() != TimerTaskFragment.TYPE_WORK && mViewPager.getVisibility() != View.VISIBLE) {
                    animateCustomTimer(false, 0f, -getAnimationDistance(.25f));
                }
            } else if (appContext.getCurrentState() == TimerTaskFragment.STATE_STOPPED && mViewPager.getVisibility() == View.VISIBLE) {
                animateViewPager(true, 1f, 0f);
            } else if (appContext.getCurrentState() == TimerTaskFragment.STATE_FINISHED && appContext.getCurrentType() == TimerTaskFragment.TYPE_WORK) {
                showViewPager();
            }
        }
    }

    private void manageFVandVPVisibility() {
        if (appContext.getCurrentState() == TimerTaskFragment.STATE_FINISHED) {
            if (appContext.getCurrentType() == TimerTaskFragment.TYPE_WORK) {
                showViewPager();
            } else {
                hideViewPager();
            }
        } else {
            if (appContext.getCurrentType() == TimerTaskFragment.TYPE_WORK) {
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

    public void setExercise(Exercise exercise, List<ExerciseHistory> exerciseHistoryList) {
        mPagerAdapter = new ExercisePagerAdapter(getChildFragmentManager(), exercise, exerciseHistoryList);
//        mViewPager.setAdapter(mPagerAdapter);

        if (appContext.shouldAnimateViewPager()) {
//            appContext.setAnimateViewPager(false); // if not set to false here it then animation continues on screen orientation change until DONE button is pressed
            mViewPager.animate()
                    .setDuration(300)
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(.2f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mViewPager.setAdapter(mPagerAdapter);
                            appContext.setAnimateViewPager(false); // if not set to false here it then animation continues on screen orientation change until DONE button is pressed
                            mViewPager.animate()
                                    .setDuration(300)
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .alpha(1f)
                                    .setListener(null)
                                    .start();
                        }
                    })
                    .start();
//            animViewPagerWhenSettingExercise();
        } else {
            mViewPager.setAdapter(mPagerAdapter);
        }

    }

    private void animViewPagerWhenSettingExercise() {
        ObjectAnimator animScaleX = ObjectAnimator.ofFloat(mViewPager, "scaleX", 1f, 0f);
        animScaleX.setRepeatCount(1);
        animScaleX.setRepeatMode(ValueAnimator.REVERSE);
        ObjectAnimator animScaleY = ObjectAnimator.ofFloat(mViewPager, "scaleY", 1f, 0f);
        animScaleY.setRepeatCount(1);
        animScaleY.setRepeatMode(ValueAnimator.REVERSE);
        ObjectAnimator animAlpha = ObjectAnimator.ofFloat(mViewPager, "alpha", 1f, .2f);
        animAlpha.setRepeatCount(1);
        animAlpha.setRepeatMode(ValueAnimator.REVERSE);
        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(animScaleX, animScaleY, animAlpha);
        animSet.setDuration(500);
        animSet.start();
    }

//    @Subscribe
//    public void onTimerStateChanged(TimerStateChanged event) {
//        if (event.isStateChanged()) {
//        }
//    }

    @Subscribe
    public void onTimerChange(TimerSendTimeEvent event) {
        setTimer(event.getMillisecs());
    }

    @Subscribe
    public void onTimerHandlerRequest(TimerHandlerEvent event) {
        if (event.getPublisher() == TimerHandlerEvent.PUBLISHER_TIMER_UI_FRAGMENT) {
            return;
        }
        mCustomTimerView.setmTimerStateAndType(appContext.getCurrentState(), appContext.getCurrentType(), TimerPreferenceManager.showTimerSuggestions());
        if (appContext.getCurrentState() == TimerTaskFragment.STATE_RUNNING) {
            setBtnTypes(BTN_START);
        } else if (appContext.getCurrentState() == TimerTaskFragment.STATE_PAUSED || appContext.getCurrentState() == TimerTaskFragment.STATE_FINISHED) {
            setBtnTypes(BTN_PAUSE);
        } else {
            setBtnTypes(BTN_STOP);
            if (appContext.isSessionFinished()) {
                if (mViewPager.getVisibility() != View.VISIBLE) {
                    mViewPager.setVisibility(View.VISIBLE);
                }
                initAlertDialog("Session ended", "Did you finish your exercise?", BTN_START);
            }
        }
        if (!appContext.isSessionFinished()) {
            managePagerAdapter();
            manageViewsAnimation();
        } else {
            mFakeView.setVisibility(View.GONE);
        }
        manageBackgroundColor(event.isShouldAnimateBackgroundColor(), event.shouldStopTimer());
    }

    /**
     * This method is used to determine whether to set adapter to null (required to call it's on destroy method) and reset FitPomodoroApplication.
     * Basically it determines if exercises should be reset.
     */
    private void managePagerAdapter() {
        if (appContext.getCurrentType() == TimerTaskFragment.TYPE_WORK && !appContext.shouldAnimateViewPager()
                && (appContext.getCurrentState() == TimerTaskFragment.STATE_RUNNING || appContext.getCurrentState() == TimerTaskFragment.STATE_PAUSED)) {
            clearViewPager();
        }
    }

    private void clearViewPager() {
        mViewPager.setAdapter(null);
        appContext.setReps(-1);
        appContext.setHowManyTimesDone(0);
    }

    @Subscribe
    public void onCircleProgressChanged(CircleProgressEvent event) {
        mCustomTimerView.drawProgress(event.getCircleProgress());
    }

    @Subscribe
    public void onRandExerciseIdReceived(ChangeExerciseEvent event) {
        if (event.getExerciseId() != -1) {
            mListener.onRandomExerciseRequested(event.getExerciseId());
        }
    }

    private void manageBackgroundColor(boolean shouldAnimateBackgroundColor, boolean timerStopped) {
        int color = getResources().getColor(R.color.timer_running);
        if (shouldAnimateBackgroundColor) {
            if (!timerStopped) {
                animateBackgroundColor(true, color);
            } else {
                animateBackgroundColor(false, color);
            }
        } else {
            if (appContext.getCurrentState() == TimerTaskFragment.STATE_STOPPED) {
                container.setBackgroundColor(Color.WHITE);
            } else {
                container.setBackgroundColor(color);
            }
        }

    }

    private void animateBackgroundColor(boolean whiteToColor, int color) {
        ValueAnimator backgroundAnimation = null;
        if (whiteToColor) {
            backgroundAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.WHITE, color)
                    .setDuration(400);
        } else {
            backgroundAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), color, Color.WHITE)
                    .setDuration(400);
        }
        backgroundAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                container.setBackgroundColor((int) animation.getAnimatedValue());
            }
        });
        backgroundAnimation.start();
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
        Favorite random_sequence = new Favorite();
        random_sequence.setId(-2);
        random_sequence.setName(getString(R.string.rand_sequence));
        Favorite all_rand = new Favorite();
        all_rand.setId(-1);
        all_rand.setName(getString(R.string.random_exercise));
        favorites.add(0, all_rand);
        favorites.add(1, random_sequence);
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
                    appContext.setSessionFinished(false);
                    manageViewsAnimation();
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

        public ExercisePagerAdapter(FragmentManager fm, Exercise exercise, List<ExerciseHistory> exerciseHistoryList) {
            super(fm);
            fragments = new Vector<>();
            fragments.add(NestedSaveExerciseFragment.newInstance(exercise));
            fragments.add(NestedExerciseHistoryListFragment.newInstance(exerciseHistoryList, 1));
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
