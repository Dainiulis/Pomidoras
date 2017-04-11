package com.dmiesoft.fitpomodoro.ui.fragments;

import android.animation.Animator;
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
import android.util.Property;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.events.timer_handling.CircleProgressEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.ExerciseIdSendEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerSendTimeEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerTypeStateHandlerEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerUpdateRequestEvent;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.Favorite;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.nested.ExerciseInTimerUIFragment;
import com.dmiesoft.fitpomodoro.utils.customViews.CustomTimerView;
import com.dmiesoft.fitpomodoro.utils.helpers.DisplayHelper;

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

    private int mCurrentState, mCurrentType;
    private long mMillisecs;
    private CustomTimerView mCustomTimerView;
    private FloatingActionButton mMainFab;
    private TimerUIFragmentListener mListener;
    private ViewPager mViewPager;
    private ExercisePagerAdapter mPagerAdapter;
    private View mFakeView;
    private ObjectAnimator mCustomTimerAnimator, mCustomViewPagerAnimator;
    private Property<View, Float> mPropertyName;
    private boolean mShouldAnimate;
    private LinearLayout container;
    private List<Favorite> favorites;
    private ArrayAdapter<Favorite> mSpinnerAdapter;
    private SharedPreferences sharedPrefs;
    private Favorite favorite;

    public TimerUIFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShouldAnimate = false;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.timer_menu, menu);

        MenuItem item = menu.findItem(R.id.fav_spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        long selectedId = sharedPrefs.getLong(SELECTED_FAVORITE, -1);
        int selectionPosition = 0;
        mSpinnerAdapter = new ArrayAdapter<Favorite>(getActivity(),
                R.layout.favorite_spinner_layout, favorites);
        spinner.setAdapter(mSpinnerAdapter);

        for (Favorite fav : favorites) {
            if (selectedId == fav.getId()) {
                selectionPosition = mSpinnerAdapter.getPosition(fav);
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long favId = ((Favorite) parent.getItemAtPosition(position)).getId();
                sharedPrefs.edit().putLong(SELECTED_FAVORITE, favId).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(selectionPosition);

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
    }

    private void setTimer() {
        mCustomTimerView.setmTimerText(getTimerString(mMillisecs));
    }

    private void initViews(View view) {

        container = (LinearLayout) view.findViewById(R.id.content_fragment_timer);
        mFakeView = view.findViewById(R.id.fake_view);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);

        mCustomTimerView = (CustomTimerView) view.findViewById(R.id.customTimer);
        mCustomTimerView.setOnClickListener(this);
        mCustomTimerView.setOnLongClickListener(this);
    }

    private String getTimerString(long millisUntilFinished) {
        long sec = (millisUntilFinished / 1000) % 60;
        long min = (millisUntilFinished / 60000);
        return String.format("%02d:%02d", min, sec);
    }

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
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Are you sure?");
                builder.setMessage("Would you like to end session?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handleBtnStop();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
        }
        return false;
    }

    //  *********Handle buttons**********
    private void handleBtnStop() {
        if (mCurrentType != TimerTaskFragment.TYPE_WORK) {
            mShouldAnimate = true;
        }
        setBtnTypes(BTN_STOP);
        TimerTypeStateHandlerEvent timerHandler = new TimerTypeStateHandlerEvent();
        timerHandler.setCurrentState(TimerTaskFragment.STATE_STOPPED);
        timerHandler.setCurrentType(mCurrentType);
        timerHandler.setPublisher(TimerTypeStateHandlerEvent.PUBLISHER_TIMER_UI_FRAGMENT);
        EventBus.getDefault().post(timerHandler);
    }

    private void handleBtnStartPause() {
        if (mCustomTimerView.getTag().equals(BTN_START)) {
            setBtnTypes(BTN_START);
            TimerTypeStateHandlerEvent timerHandler = new TimerTypeStateHandlerEvent();
            timerHandler.setCurrentState(TimerTaskFragment.STATE_RUNNING);
            timerHandler.setCurrentType(mCurrentType);
            timerHandler.setPublisher(TimerTypeStateHandlerEvent.PUBLISHER_TIMER_UI_FRAGMENT);
            EventBus.getDefault().post(timerHandler);

        } else if (mCustomTimerView.getTag().equals(BTN_PAUSE)) {
            TimerTypeStateHandlerEvent timerHandler = new TimerTypeStateHandlerEvent();
            timerHandler.setCurrentState(TimerTaskFragment.STATE_PAUSED);
            timerHandler.setCurrentType(mCurrentType);
            setBtnTypes(BTN_PAUSE);
            timerHandler.setPublisher(TimerTypeStateHandlerEvent.PUBLISHER_TIMER_UI_FRAGMENT);
            EventBus.getDefault().post(timerHandler);
        }
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
            mViewPager.setVisibility(View.GONE);
            mFakeView.setVisibility(View.VISIBLE);
        }
        mCustomTimerAnimator = ObjectAnimator.ofFloat(mCustomTimerView, mPropertyName, values);
        mCustomTimerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mCustomTimerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
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
        mCustomTimerAnimator.setDuration(500);
        mCustomTimerAnimator.start();
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
        if (mCurrentType == TimerTaskFragment.TYPE_WORK) {
            if (mShouldAnimate) {
                animateViewPager(true, 1f, 0f);
            } else {
                mFakeView.setVisibility(View.VISIBLE);
                mViewPager.setAdapter(null);
                mViewPager.setVisibility(View.GONE);
            }
        } else {
            if (mShouldAnimate) {
                if (mViewPager.getVisibility() == View.VISIBLE) {
                    animateViewPager(true, 1f, 0f);
                } else {
                    animateCustomTimer(false, 0f, -getAnimationDistance(.25f));
                }
            } else {
                mFakeView.setVisibility(View.GONE);
                mViewPager.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * @param multiplier the number from which to multiply container size to calculate animation distance
     *                currently 0.25 is sweet spot for customTimerView
     * @return
     */
    private float getAnimationDistance(float multiplier) {
        float animationDistance = 0;
        if (new DisplayHelper(getActivity()).getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
//            if (mCurrentType != TimerTaskFragment.TYPE_WORK) {
//                animationDistance = mCustomTimerView.getTop();
//            } else {
//                // found sweet spot by dividing by 4
//                // it works because the timer is not in exact middle position of the layout
//                animationDistance = container.getHeight() / divider;
//            }
            animationDistance = container.getHeight() * multiplier;
        } else {
//            if (mCurrentType != TimerTaskFragment.TYPE_WORK) {
//                animationDistance = mCustomTimerView.getLeft();
//            } else {
//                animationDistance = container.getWidth() / divider;
//            }
            animationDistance = container.getWidth() * multiplier;
        }
        return animationDistance;
    }

    private void setPropertyName() {
        if (new DisplayHelper(getActivity()).getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            mPropertyName = View.TRANSLATION_Y;
        } else {
            mPropertyName = View.TRANSLATION_X;
        }
    }

    public void setExercise(Exercise exercise) {
        mPagerAdapter = new ExercisePagerAdapter(getChildFragmentManager(), exercise);
        mViewPager.setAdapter(mPagerAdapter);
    }

    @Subscribe
    public void onTimerChange(TimerSendTimeEvent event) {
        mMillisecs = event.getMillisecs();
        if (mMillisecs < 1000) {
            mShouldAnimate = true;
        }
        setTimer();
    }

    @Subscribe
    public void onTimerTypeStateRequest(TimerTypeStateHandlerEvent event) {
        mCurrentState = event.getCurrentState();
        mCurrentType = event.getCurrentType();
        mCustomTimerView.setmTimerStateAndType(mCurrentState, mCurrentType);
        if (mCurrentState == TimerTaskFragment.STATE_RUNNING) {
            setBtnTypes(BTN_START);
        } else if (mCurrentState == TimerTaskFragment.STATE_PAUSED || mCurrentState == TimerTaskFragment.STATE_FINISHED) {
            setBtnTypes(BTN_PAUSE);
        } else {
            setBtnTypes(BTN_STOP);
        }

        manageViewsAnimation();
    }

    @Subscribe
    public void onCircleProgressChanged(CircleProgressEvent event) {
        mCustomTimerView.drawProgress(event.getCircleProgress());
    }

    @Subscribe
    public void onRandExerciseIdReceived(ExerciseIdSendEvent event) {
        if (event.getExerciseId() != -1) {
            mListener.onRandomExerciseRequested(event.getExerciseId());
        } else {
            Toast.makeText(getContext(), "No exercises found", Toast.LENGTH_LONG).show();
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

    public interface TimerUIFragmentListener {
        void onRandomExerciseRequested(long randExerciseId);
        void onFavoritesListRequested();
    }

    private class ExercisePagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> fragments;

        public ExercisePagerAdapter(FragmentManager fm, Exercise exercise) {
            super(fm);
            fragments = new Vector<>();
            fragments.add(ExerciseInTimerUIFragment.newInstance(exercise, true));
            fragments.add(ExerciseInTimerUIFragment.newInstance(exercise, false));
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

}
