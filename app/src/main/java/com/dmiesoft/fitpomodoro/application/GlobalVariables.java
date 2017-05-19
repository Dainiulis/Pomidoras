package com.dmiesoft.fitpomodoro.application;

import android.app.Application;

import com.dmiesoft.fitpomodoro.ui.fragments.nested.NestedSaveExerciseFragment;

public class GlobalVariables extends Application {

    /**
     * For saving how many reps was selected in {@link NestedSaveExerciseFragment}
     */
    private int reps;
    /**
     * saved pager page of the mViewPager
     */
    private int pagerPage;


    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getPagerPage() {
        return pagerPage;
    }

    public void setPagerPage(int pagerPage) {
        this.pagerPage = pagerPage;
    }

}
