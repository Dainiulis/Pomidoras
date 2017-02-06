package com.dmiesoft.fitpomodoro.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.utils.adapters.ExercisesListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ExercisesFragment extends ListFragment {

    private static final String PACKAGE_EXERCISES = "com.dmiesoft.fitpomodoro.model.Exercise";
    private List<Exercise> exercises;
    private ExercisesListAdapter adapter;

    public ExercisesFragment() {}

    public static ExercisesFragment newInstance(List<Exercise> exercises) {

        Bundle args = new Bundle();
        ExercisesFragment fragment = new ExercisesFragment();

        args.putParcelableArrayList(PACKAGE_EXERCISES, (ArrayList) exercises);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            exercises = getArguments().getParcelableArrayList(PACKAGE_EXERCISES);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_exercises, container, false);
        adapter = new ExercisesListAdapter(getContext(), R.layout.list_exercises, exercises);
        setListAdapter(adapter);

        return rootView;
    }
}
