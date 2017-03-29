package com.dmiesoft.fitpomodoro.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.events.timer_handling.CircleProgressEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.ExerciseIdSendEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerSendTimeEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerTypeStateHandlerEvent;
import com.dmiesoft.fitpomodoro.events.timer_handling.TimerUpdateRequestEvent;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.nested.ExerciseInTimerUIFragment;
import com.dmiesoft.fitpomodoro.utils.customViews.CustomTimerView;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;
import com.dmiesoft.fitpomodoro.utils.helpers.DisplayWidthHeight;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;


public class TimerUIFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private static final int BTN_START = 1001;
    private static final int BTN_PAUSE = 1002;
    private static final int BTN_STOP = 1003;
    public static final String TAG = "TIMER";
    private static final String WORK_IMAGE_NAME = "@_@work.png";

    private int mCurrentState, mCurrentType;
    private long millisecs;
    private CustomTimerView customTimerView;
    private FloatingActionButton mainFab;
    private TimerUIFragmentListener mListener;
    private ViewPager mViewPager;
    private ExercisePagerAdapter pagerAdapter;

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
        if (mainFab != null) {
            mainFab.hide();
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
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        initViews(view);
        return view;
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
        customTimerView.setmTimerText(getTimerString(millisecs));
    }

    private void initViews(View view) {

        mViewPager = (ViewPager) view.findViewById(R.id.pager);

        customTimerView = (CustomTimerView) view.findViewById(R.id.customTimer);
        customTimerView.setOnClickListener(this);
        customTimerView.setOnLongClickListener(this);
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

    //  ***Handle buttons***
    private void handleBtnStop() {
        setBtnTypes(BTN_STOP);
        TimerTypeStateHandlerEvent timerHandler = new TimerTypeStateHandlerEvent();
        timerHandler.setCurrentState(TimerTaskFragment.STATE_STOPPED);
        timerHandler.setCurrentType(mCurrentType);
        timerHandler.setPublisher(TimerTypeStateHandlerEvent.PUBLISHER_TIMER_UI_FRAGMENT);
        EventBus.getDefault().post(timerHandler);
    }

    private void handleBtnStartPause() {
        if (customTimerView.getTag().equals(BTN_START)) {
            setBtnTypes(BTN_START);
            TimerTypeStateHandlerEvent timerHandler = new TimerTypeStateHandlerEvent();
            timerHandler.setCurrentState(TimerTaskFragment.STATE_RUNNING);
            timerHandler.setCurrentType(mCurrentType);
            timerHandler.setPublisher(TimerTypeStateHandlerEvent.PUBLISHER_TIMER_UI_FRAGMENT);
            EventBus.getDefault().post(timerHandler);

        } else if (customTimerView.getTag().equals(BTN_PAUSE)) {
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
            customTimerView.setTag(BTN_PAUSE);
        } else if (clickedButtonTag == BTN_PAUSE) {
            customTimerView.setTag(BTN_START);
        } else {
            customTimerView.setTag(BTN_START);
        }
    }

    /**
     * Helper method to calculate pixels from density independent pixels
     *
     * @param dp density independent pixels
     * @return result in pixels
     */
    private float getPixels(float dp) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float pixels = dp * outMetrics.density;
        return pixels;
    }

    /**
     * This method is required for determining fab button placement
     *
     * @return
     */
    private int getOrientation() {
        DisplayWidthHeight display = new DisplayWidthHeight(getActivity());
        float width = display.getWidth();
        float height = display.getHeight();
        int orientation;
        if (width < height) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        } else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }

    public void setExercise(Exercise exercise) {
        Log.i(TAG, "setExercise: " + exercise.getName());
        pagerAdapter = new ExercisePagerAdapter(getChildFragmentManager(), exercise);
        mViewPager.setAdapter(pagerAdapter);
    }

    @Subscribe
    public void onTimerChange(TimerSendTimeEvent event) {
        millisecs = event.getMillisecs();
        setTimer();
    }

    @Subscribe
    public void onTimerTypeStateRequest(TimerTypeStateHandlerEvent event) {
        mCurrentState = event.getCurrentState();
        mCurrentType = event.getCurrentType();
        customTimerView.setmTimerStateAndType(mCurrentState, mCurrentType);
        if (mCurrentState == TimerTaskFragment.STATE_RUNNING) {
            setBtnTypes(BTN_START);
        } else if (mCurrentState == TimerTaskFragment.STATE_PAUSED || mCurrentState == TimerTaskFragment.STATE_FINISHED) {
            setBtnTypes(BTN_PAUSE);
        } else {
            setBtnTypes(BTN_STOP);
        }
        if (mCurrentType == TimerTaskFragment.TYPE_WORK) {
            mViewPager.setAdapter(null);
        }
    }

    @Subscribe
    public void onCircleProgressChanged(CircleProgressEvent event) {
        customTimerView.drawProgress(event.getCircleProgress());
    }

    @Subscribe
    public void onRandExerciseIdReceived(ExerciseIdSendEvent event) {
        if (event.getExerciseId() != -1) {
            mListener.onRandomExerciseRequested(event.getExerciseId());
        } else {
            Toast.makeText(getContext(), "No exercises found", Toast.LENGTH_LONG).show();
        }
    }

    public interface TimerUIFragmentListener {
        void onRandomExerciseRequested(long randExerciseId);
    }

    private class ExercisePagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> fragments;

        public ExercisePagerAdapter(FragmentManager fm, Exercise exercise) {
            super(fm);
            fragments = new Vector<>();
            fragments.add(ExerciseInTimerUIFragment.newInstance(exercise, false));
            fragments.add(ExerciseInTimerUIFragment.newInstance(exercise, true));
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
