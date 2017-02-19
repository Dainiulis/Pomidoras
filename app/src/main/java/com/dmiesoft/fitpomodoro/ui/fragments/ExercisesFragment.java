package com.dmiesoft.fitpomodoro.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.utils.adapters.ExercisesListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ExercisesFragment extends ListFragment implements View.OnClickListener {

    private static final String PACKAGE_EXERCISES_ARRAY = "com.dmiesoft.fitpomodoro.model.Exercise.Array";
    private static final String EXERCISE_GROUP_ID_KEY = "EXERCISE_GROUP_ID_KEY";
    private List<Exercise> exercises;
    private ExercisesListAdapter adapter;
    private ExercisesListFragmentListener mListener;
    private boolean isFabOpen = false;
    private FloatingActionButton mainFab, addExFab, deleteFab, addFavFab;
    private Animation fabOpen, fabClose, fabRotateFroward, fabRotateBackward;
    private long exerciseGroupId;

    public ExercisesFragment() {
    }

    public static ExercisesFragment newInstance(List<Exercise> exercises, long exerciseGroupId) {

        Bundle args = new Bundle();
        ExercisesFragment fragment = new ExercisesFragment();

        args.putParcelableArrayList(PACKAGE_EXERCISES_ARRAY, (ArrayList) exercises);
        args.putLong(EXERCISE_GROUP_ID_KEY, exerciseGroupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            exercises = getArguments().getParcelableArrayList(PACKAGE_EXERCISES_ARRAY);
            exerciseGroupId = getArguments().getLong(EXERCISE_GROUP_ID_KEY);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ExercisesListFragmentListener) {
            mListener = (ExercisesListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ExercisesListFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_exercises, container, false);
        initViews(rootView);
        adapter = new ExercisesListAdapter(getContext(), R.layout.list_exercises, exercises);
        setListAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Exercise exercise = (Exercise) parent.getItemAtPosition(position);
                mListener.onExerciseLongClicked(exercise);
                return true;
            }
        });
    }

    private void initViews(View rootView) {
        mainFab = (FloatingActionButton) ((MainActivity) getActivity()).getMainFab();
        addExFab = (FloatingActionButton) ((MainActivity) getActivity()).getAddFab();
        addFavFab = (FloatingActionButton) ((MainActivity) getActivity()).getAddFavFab();
        deleteFab = (FloatingActionButton) ((MainActivity) getActivity()).getDeleteFab();
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
        addFavFab.setOnClickListener(this);
        deleteFab.setOnClickListener(this);
    }

    private void animateFab() {
        if (isFabOpen) {
            mainFab.startAnimation(fabRotateBackward);
            addExFab.startAnimation(fabClose);
            addFavFab.startAnimation(fabClose);
            deleteFab.startAnimation(fabClose);
            addExFab.setClickable(false);
            addFavFab.setClickable(false);
            deleteFab.setClickable(false);
            isFabOpen = false;
        } else {
            mainFab.startAnimation(fabRotateFroward);
            addExFab.startAnimation(fabOpen);
            addFavFab.startAnimation(fabOpen);
            deleteFab.startAnimation(fabOpen);
            addExFab.setClickable(true);
            addFavFab.setClickable(true);
            deleteFab.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab_main:
                animateFab();
                break;
            case R.id.fab_add:
                mListener.onAddExerciseBtnClicked(exerciseGroupId);
                break;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Exercise exercise = exercises.get(position);
        mListener.onExerciseClicked(exercise);
    }

    @Override
    public void onResume() {
        super.onResume();
        mainFab.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isFabOpen) {
            animateFab();
        }
        mainFab.hide();
    }

    public interface ExercisesListFragmentListener {
        void onExerciseClicked(Exercise exercise);
        void onExerciseLongClicked(Exercise exercise);
        void onAddExerciseBtnClicked(long exerciseGroupId);
    }

    public void updateListView(Exercise exercise) {
        int index = 0;
        boolean found = false;
        for (Exercise e : exercises) {
            if (e.getId() == exercise.getId()) {
                index = exercises.indexOf(e);
                found = true;
                break;
            }
        }
        if (found) {
            exercises.remove(index);
            exercises.add(index, exercise);
        } else {
            exercises.add(exercise);
        }
        adapter.notifyDataSetChanged();
    }

}
