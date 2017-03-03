package com.dmiesoft.fitpomodoro.events.navigation;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.ExerciseDetailFragment;
import com.dmiesoft.fitpomodoro.ui.fragments.ExercisesGroupsFragment;
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
    private Exercise exercise;
    private long exerciseGroupId;

    /*
     * ~~~~~~~~~~ Constructor methods ~~~~~~~~~~~
     */

    /*
     * Constructor for instatiating DrawerItemClickedEvent object if not passing info to fragment
     */
    public DrawerItemClickedEvent(FragmentManager fragmentManager, String fragTag, boolean addToBackStack) {
        this.fragmentManager = fragmentManager;
        this.fragTag = fragTag;
        this.fragments = fragmentManager.getFragments();
        this.addToBackStack = addToBackStack;
    }

    /*
     * Constructor for instantiating DrawerItemClickedEvent object if navigating to ExercisesGroups or Exercises list
     */
    public DrawerItemClickedEvent(FragmentManager fragmentManager, String fragTag, List<?> objects, boolean addToBackStack, long exerciseGroupId) {
        this.fragmentManager = fragmentManager;
        this.fragTag = fragTag;
        this.fragments = fragmentManager.getFragments();
        this.objects = objects;
        this.addToBackStack = addToBackStack;
        if (exerciseGroupId != -1) {
            this.exerciseGroupId = exerciseGroupId;
        }
    }

    /*
     * Constructor for instatiating DrawerItemClickedEvent object if navigating to Exercises details
     */
    public DrawerItemClickedEvent(FragmentManager fragmentManager, String fragTag, Exercise exercise, boolean addToBackStack) {
        this.fragmentManager = fragmentManager;
        this.fragTag = fragTag;
        this.fragments = fragmentManager.getFragments();
        this.exercise = exercise;
        this.addToBackStack = addToBackStack;
    }
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private void initFragment(String fragTag) {
        switch (fragTag) {
            case MainActivity.TIMER_UI_FRAGMENT_TAG:
                if (isFragmentCreated(MainActivity.TIMER_UI_FRAGMENT_TAG)) {
                    fragment = fragmentManager.findFragmentByTag(MainActivity.TIMER_UI_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = new TimerUIFragment();
                    }
                } else {
                    fragment = new TimerUIFragment();
                }
                break;

            case MainActivity.EXERCISE_GROUP_FRAGMENT_TAG:
                /*
                 * jeigu fragment yra sukurtas, tuomet ji randa pagal fragTag
                 * bet gali rasti pagal fragTag net jei fragment == null
                 * todel reikia patikrinti salyga fragment == null
                 * jei fragment nebuvo sukurtas tuomet sukuriamas naujas fragment
                 *
                 */
                if (isFragmentCreated(MainActivity.EXERCISE_GROUP_FRAGMENT_TAG)) {
                    fragment = fragmentManager.findFragmentByTag(MainActivity.EXERCISE_GROUP_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = ExercisesGroupsFragment.newInstance((List<ExercisesGroup>) objects);
                    }
                } else {
                    fragment = ExercisesGroupsFragment.newInstance((List<ExercisesGroup>) objects);
                }
                break;

            case MainActivity.HISTORY_FRAGMENT_TAG:
                if (isFragmentCreated(MainActivity.HISTORY_FRAGMENT_TAG)) {
                    fragment = fragmentManager.findFragmentByTag(MainActivity.HISTORY_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = new HistoryFragment();
                    }
                } else {
                    fragment = new HistoryFragment();
                }
                break;
            case MainActivity.EXERCISES_FRAGMENT_TAG:
                if (isFragmentCreated(MainActivity.EXERCISES_FRAGMENT_TAG)) {
                    fragment = fragmentManager.findFragmentByTag(MainActivity.EXERCISES_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = ExercisesFragment.newInstance((List<Exercise>) objects, exerciseGroupId);
                    }
                } else {
                    fragment = ExercisesFragment.newInstance((List<Exercise>) objects, exerciseGroupId);
                }
                break;
            case MainActivity.EXERCISE_DETAIL_FRAGMENT_TAG:
                if (isFragmentCreated(MainActivity.EXERCISE_DETAIL_FRAGMENT_TAG)) {
                    fragment = fragmentManager.findFragmentByTag(MainActivity.EXERCISE_DETAIL_FRAGMENT_TAG);
                    if (fragment == null) {
                        fragment = ExerciseDetailFragment.newInstance(exercise);
                    }
                } else {
                    fragment = ExerciseDetailFragment.newInstance(exercise);
                }
                break;
        }
    }

    public FragmentTransaction getFragmentTransaction() {
        initFragment(fragTag); // getting fragment object
        if (fragment != null && !fragment.isVisible()) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (fragmentManager.getBackStackEntryCount() > 0 && exercise == null) {
//                if (fragTag.equals(MainActivity.TIMER_UI_FRAGMENT_TAG)) {
//                    removeAllFragmentsExceptTimer(fragmentTransaction);
//                }
                for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                    fragmentManager.popBackStackImmediate();
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
