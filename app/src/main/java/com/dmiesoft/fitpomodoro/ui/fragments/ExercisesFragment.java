package com.dmiesoft.fitpomodoro.ui.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.ui.fragments.dialogs.AddExerciseDialog;
import com.dmiesoft.fitpomodoro.utils.adapters.ExercisesListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ExercisesFragment extends ListFragment implements View.OnClickListener {

    private static final String PACKAGE_EXERCISES_ARRAY = "com.dmiesoft.fitpomodoro.model.Exercise.Array";
    private static final String EXERCISE_GROUP_ID_KEY = "EXERCISE_GROUP_ID_KEY";
    private List<Exercise> exercises;
    private ExercisesListAdapter adapter;
    private ExercisesListFragmentListener mListener;
    private FloatingActionButton mainFab;
    private long exerciseGroupId;
    private Handler handlerFab;
    private Runnable runnableFab;

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

                List<Exercise> exercises = ExercisesDataSource.findExercises(getContext(), null, null);
                int editCode = AddExerciseDialog.EDIT_IMAGE_LAYOUT;
                AddExerciseDialog dialog = AddExerciseDialog.newInstance(exercises, exercise, exercise.getExerciseGroupId(), editCode);
                dialog.setCancelable(false);
                dialog.show(getChildFragmentManager(), MainActivity.ADD_EXERCISE_DIALOG);

                return true;
            }
        });
    }

    private void initViews(View rootView) {
        mainFab = (FloatingActionButton) ((MainActivity) getActivity()).getMainFab();
        mainFab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab_main:

                List<Exercise> exercises = ExercisesDataSource.findExercises(getContext(), null, null);
                AddExerciseDialog dialog = AddExerciseDialog.newInstance(exercises, null, exerciseGroupId, AddExerciseDialog.NO_EDIT);
                dialog.setCancelable(false);
                dialog.show(getChildFragmentManager(), MainActivity.ADD_EXERCISE_DIALOG);

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
        startShowFabHandler();
    }

    private void startShowFabHandler() {
        handlerFab = new Handler();
        runnableFab = new Runnable() {
            @Override
            public void run() {
                showFab();
            }
        };
        handlerFab.postDelayed(runnableFab, 400);
    }

    private void showFab() {
        mainFab.show();
        mainFab.setClickable(true);
    }

    private void hideFab() {
        mainFab.hide();
        mainFab.setClickable(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        handlerFab.removeCallbacks(runnableFab);
        hideFab();
    }

    public interface ExercisesListFragmentListener {
        void onExerciseClicked(Exercise exercise);
    }

    public void updateListView(Exercise exercise, boolean animate) {
        if (exercise == null) {
            if (!animate) {
                adapter.clearViewsToAnimate();
            }
            adapter.notifyDataSetChanged();
            return;
        }
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
