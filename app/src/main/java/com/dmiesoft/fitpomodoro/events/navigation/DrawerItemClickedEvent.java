package com.dmiesoft.fitpomodoro.events.navigation;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.ExerciseGroupFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.ExercisesFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.HistoryFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.TimerUIFragment;

import java.util.List;

public class DrawerItemClickedEvent {

    private static final String TAG = "DICE";
    private FragmentManager fragmentManager;
    private Fragment fragment = null;
    private List<Fragment> fragments;
    private String fragTag;
    private List<?> objects;
    private boolean addToBackStack;

    public DrawerItemClickedEvent(FragmentManager fragmentManager, String fragTag, List<?> objects, boolean addToBackStack) {
        this.fragmentManager = fragmentManager;
        this.fragTag = fragTag;
        this.fragments = fragmentManager.getFragments();
        this.objects = objects;
        this.addToBackStack = addToBackStack;
    }

    private void initFragment(String fragTag) {
        switch (fragTag) {
            case MainActivity.TIMER_FRAGMENT_TAG:
                if (isFragmentCreated(MainActivity.TIMER_FRAGMENT_TAG)) {
                    fragment = fragmentManager
                            .findFragmentByTag(MainActivity.TIMER_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = new TimerUIFragment();
                    }
                } else {
                    fragment = new TimerUIFragment();
                }
                break;

            case MainActivity.EXERCISE_GROUP_FRAGMENT_TAG:
                /*
                if fragment is created then it finds it by tag
                but the problem is that it can find it by tag but
                fragment could be null so if it's null it creates new fragment
                 */
                if (isFragmentCreated(MainActivity.EXERCISE_GROUP_FRAGMENT_TAG)) {
                    fragment = fragmentManager
                            .findFragmentByTag(MainActivity.EXERCISE_GROUP_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = ExerciseGroupFragment.newInstance((List<ExercisesGroup>) objects);
                    }
                } else {
                    fragment = ExerciseGroupFragment.newInstance((List<ExercisesGroup>) objects);
                }
                break;

            case MainActivity.HISTORY_FRAGMENT_TAG:
                if (isFragmentCreated(MainActivity.HISTORY_FRAGMENT_TAG)) {
                    fragment = fragmentManager
                            .findFragmentByTag(MainActivity.HISTORY_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = new HistoryFragment();
                    }
                } else {
                    fragment = new HistoryFragment();
                }
                break;
            case MainActivity.EXERCISES_FRAGMENT_TAG:
                if (isFragmentCreated(MainActivity.EXERCISES_FRAGMENT_TAG)) {
                    fragment = fragmentManager
                            .findFragmentByTag(MainActivity.EXERCISES_FRAGMENT_TAG);
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
                removeAllFragmentsExceptTimer(fragmentTransaction);
                for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                    fragmentManager.popBackStack();
                }
            }
            fragmentTransaction.replace(R.id.main_fragment_container, fragment, fragTag);
            if (addToBackStack) {
                fragmentTransaction.addToBackStack(null);
            }
            return fragmentTransaction;
        }
        return null;
    }

    private void removeAllFragmentsExceptTimer(FragmentTransaction fragmentTransaction) {
        for (Fragment frag : fragments) {
            if (frag != null && frag != fragmentManager.findFragmentByTag(MainActivity.TIMER_TASK_FRAGMENT_TAG)) {
                fragmentTransaction.remove(frag);
            }
        }
    }

    private boolean isFragmentCreated(String tag) {
        return fragments.contains(fragmentManager.findFragmentByTag(tag));
    }

}
