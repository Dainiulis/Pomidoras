package com.dmiesoft.fitpomodoro.events;


import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.ExerciseGroupFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.ExercisesFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.HistoryFragment;

import java.util.List;

public class DrawerItemClickedEvent {

    private static final String TAG = "DICE";
    private FragmentManager fragmentManager;
    private Fragment fragment = null;
    private List<Fragment> fragments;
    private String fragTag;
    private List<?> objects;
    private boolean addToBackStack;

    private static final String TIMER_FRAGMENT_TAG = "timer_fragment_tag";
    private static final String EXERCISE_GROUP_FRAGMENT_TAG = "exercise_group_fragment_tag";
    private static final String HISTORY_FRAGMENT_TAG = "history_fragment_tag";
    private static final String EXERCISES_FRAGMENT_TAG = "exercises_fragment";

    public DrawerItemClickedEvent(FragmentManager fragmentManager, String fragTag, List<?> objects, boolean addToBackStack) {
        this.fragmentManager = fragmentManager;
        this.fragTag = fragTag;
        this.fragments = fragmentManager.getFragments();
        this.objects = objects;
        this.addToBackStack = addToBackStack;
    }

    private void initFragment(String fragTag) {
        switch (fragTag) {
            case TIMER_FRAGMENT_TAG:
                if (isFragmentCreated(TIMER_FRAGMENT_TAG)) {
                    fragment = fragmentManager
                            .findFragmentByTag(TIMER_FRAGMENT_TAG);
                }
                break;

            case EXERCISE_GROUP_FRAGMENT_TAG:
                /*
                if fragment is created then it finds it by tag
                but the problem is that it can find it by tag but
                fragment could be null so if it's null it creates new fragment
                 */
                if (isFragmentCreated(EXERCISE_GROUP_FRAGMENT_TAG)) {
                    fragment = fragmentManager
                            .findFragmentByTag(EXERCISE_GROUP_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = ExerciseGroupFragment.newInstance((List<ExercisesGroup>) objects);
                    }
                } else {
                    fragment = ExerciseGroupFragment.newInstance((List<ExercisesGroup>) objects);
                }
                break;

            case HISTORY_FRAGMENT_TAG:
                if (isFragmentCreated(HISTORY_FRAGMENT_TAG)) {
                    fragment = fragmentManager
                            .findFragmentByTag(HISTORY_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = new HistoryFragment();
                    }
                } else {
                    fragment = new HistoryFragment();
                }
                break;
            case EXERCISES_FRAGMENT_TAG:
                if (isFragmentCreated(EXERCISES_FRAGMENT_TAG)) {
                    fragment = fragmentManager
                            .findFragmentByTag(EXERCISES_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = ExercisesFragment.newInstance((List<Exercise>) objects);
                    }
                } else {
                    fragment = ExercisesFragment.newInstance((List<Exercise>) objects);
                }
                break;
        }
    }

    public FragmentTransaction getFragmentTransaction() {
        initFragment(fragTag);
        if (fragment != null && !fragment.isVisible()) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                    fragmentManager.popBackStack();
                }
            }
            if (fragment.getTag() != null) {
                /*
                if fragment is TimerFragment then it
                gets all fragments, removes all but not the TimerFragment
                */
                if (fragment.getTag().equals(TIMER_FRAGMENT_TAG)) {
                    removeAllFragmentsExceptTimer(fragmentTransaction);
                    // then it shows the TimerFragment
                    fragmentTransaction.show(fragment);
                }
            /*
            if it's not TimeFragment then it hides TimerFragment and adds
            other passed fragment
            */
            } else {
                if (fragmentManager.findFragmentByTag(TIMER_FRAGMENT_TAG).isVisible()) {
                    fragmentTransaction.hide(fragmentManager.findFragmentByTag(TIMER_FRAGMENT_TAG));
                    fragmentTransaction.add(R.id.main_fragment_container, fragment, fragTag);
                } else if (fragmentManager.findFragmentByTag(TIMER_FRAGMENT_TAG).isHidden()) {
                    removeAllFragmentsExceptTimer(fragmentTransaction);
                    fragmentTransaction.add(R.id.main_fragment_container, fragment, fragTag);
                    if (addToBackStack) {
                        fragmentTransaction.addToBackStack(null);
                    }
                }
            }
            return fragmentTransaction;
        }
        return null;
    }

    private void removeAllFragmentsExceptTimer(FragmentTransaction fragmentTransaction) {
        for (Fragment frag : fragments) {
            if (frag != null && frag != fragmentManager.findFragmentByTag(TIMER_FRAGMENT_TAG)) {
                fragmentTransaction.remove(frag);
            }
        }
    }

    private boolean isFragmentCreated(String tag) {
        return fragments.contains(fragmentManager.findFragmentByTag(tag));
    }

}
