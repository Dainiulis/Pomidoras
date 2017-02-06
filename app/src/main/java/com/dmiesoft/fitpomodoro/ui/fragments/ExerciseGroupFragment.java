package com.dmiesoft.fitpomodoro.ui.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.utils.InitialDatabasePopulation;
import com.dmiesoft.fitpomodoro.utils.adapters.ExercisesGroupListAdapter;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ExerciseGroupFragment extends ListFragment {

    private static final String TAG = "EXERCISEGROUP";
    private static final String PACKAGE_EXERCISES_GROUP = "com.dmiesoft.fitpomodoro.model.ExercisesGroup";
    private List<ExercisesGroup> exercisesGroups;
    private ExercisesGroupListAdapter adapter;
    private ExerciseGroupListFragmentListener mListener;

    public ExerciseGroupFragment() {}

    public static ExerciseGroupFragment newInstance(List<ExercisesGroup> exercisesGroups) {
        Bundle args = new Bundle();
        ExerciseGroupFragment fragment = new ExerciseGroupFragment();

        args.putParcelableArrayList(PACKAGE_EXERCISES_GROUP, (ArrayList) exercisesGroups);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ExerciseGroupListFragmentListener) {
            mListener = (ExerciseGroupListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ExerciseGroupListFragmentListener");
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

        View rootView = inflater.inflate(R.layout.fragment_exercise_group, container, false);

        adapter = new ExercisesGroupListAdapter(getContext(), R.layout.list_exercise_groups, exercisesGroups);
        setListAdapter(adapter);

        return rootView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        long exercisesGroupId = exercisesGroups.get(position).getId();
        mListener.onExerciseGroupItemClicked(exercisesGroupId);

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

    public interface ExerciseGroupListFragmentListener {
        void onExerciseGroupItemClicked(long exercisesGroupId);
    }

}
