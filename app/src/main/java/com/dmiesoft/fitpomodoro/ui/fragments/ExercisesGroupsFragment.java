package com.dmiesoft.fitpomodoro.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.utils.adapters.ExercisesGroupListAdapter;

import java.util.ArrayList;
import java.util.List;


public class ExercisesGroupsFragment extends ListFragment implements View.OnClickListener {

    private static final String TAG = "EXERCISEGROUP";
    private static final String PACKAGE_EXERCISES_GROUP = "com.dmiesoft.fitpomodoro.model.ExercisesGroup";
    private List<ExercisesGroup> exercisesGroups;
    private ExercisesGroupListAdapter adapter;
    private ExercisesGroupsListFragmentListener mListener;
    private boolean isFabOpen = false;
    private FloatingActionButton mainFab, addExGrFab, addExFab, deleteFab, addFavFab;
    private Animation fabOpen, fabClose, fabRotateFroward, fabRotateBackward;

    public ExercisesGroupsFragment() {}

    public static ExercisesGroupsFragment newInstance(List<ExercisesGroup> exercisesGroups) {
        Bundle args = new Bundle();
        ExercisesGroupsFragment fragment = new ExercisesGroupsFragment();

        args.putParcelableArrayList(PACKAGE_EXERCISES_GROUP, (ArrayList) exercisesGroups);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ExercisesGroupsListFragmentListener) {
            mListener = (ExercisesGroupsListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ExercisesGroupsListFragmentListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercisesGroups = getArguments().getParcelableArrayList(PACKAGE_EXERCISES_GROUP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_exercises_groups, container, false);

        initViews(rootView);
        adapter = new ExercisesGroupListAdapter(getContext(), R.layout.list_exercises_groups, exercisesGroups);
        setListAdapter(adapter);


        return rootView;
    }

    private void initViews(View rootView) {
        mainFab = (FloatingActionButton) rootView.findViewById(R.id.fab_main);
        addExFab = (FloatingActionButton) rootView.findViewById(R.id.fab_add_exercise);
        addExGrFab = (FloatingActionButton) rootView.findViewById(R.id.fab_add_exercise_group);
        addFavFab = (FloatingActionButton) rootView.findViewById(R.id.fab_add_favorites);
        deleteFab = (FloatingActionButton) rootView.findViewById(R.id.fab_delete);
        /*
         * Load animations
         */
        fabOpen = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.fab_close);
        fabRotateFroward = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.fab_rotate_forward);
        fabRotateBackward = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.fab_rotate_backward);
        /*
         * Set onClickListeners
         */
        mainFab.setOnClickListener(this);
        addExFab.setOnClickListener(this);
        addExGrFab.setOnClickListener(this);
        addFavFab.setOnClickListener(this);
        deleteFab.setOnClickListener(this);
    }

    private void animateFab() {
        if (isFabOpen) {
            mainFab.startAnimation(fabRotateBackward);
            addExGrFab.startAnimation(fabClose);
            addExFab.startAnimation(fabClose);
            addFavFab.startAnimation(fabClose);
            deleteFab.startAnimation(fabClose);
            addExGrFab.setClickable(false);
            addExFab.setClickable(false);
            addFavFab.setClickable(false);
            deleteFab.setClickable(false);
            isFabOpen = false;
        } else {
            mainFab.startAnimation(fabRotateFroward);
            addExGrFab.startAnimation(fabOpen);
            addExFab.startAnimation(fabOpen);
            addFavFab.startAnimation(fabOpen);
            deleteFab.startAnimation(fabOpen);
            addExGrFab.setClickable(true);
            addExFab.setClickable(true);
            addFavFab.setClickable(true);
            deleteFab.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        isFabOpen=false;
        long exercisesGroupId = exercisesGroups.get(position).getId();
        mListener.onExercisesGroupItemClicked(exercisesGroupId);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach: Exercise");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab_main:
                animateFab();
                break;
        }
    }

    public interface ExercisesGroupsListFragmentListener {
        void onExercisesGroupItemClicked(long exercisesGroupId);
    }

}
