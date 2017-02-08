package com.dmiesoft.fitpomodoro.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.utils.adapters.ExercisesGroupListAdapter;

import java.util.ArrayList;
import java.util.List;


public class ExercisesGroupsFragment extends ListFragment {

    private static final String TAG = "EXERCISEGROUP";
    private static final String PACKAGE_EXERCISES_GROUP = "com.dmiesoft.fitpomodoro.model.ExercisesGroup";
    private List<ExercisesGroup> exercisesGroups;
    private ExercisesGroupListAdapter adapter;
    private ExercisesGroupsListFragmentListener mListener;

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

        adapter = new ExercisesGroupListAdapter(getContext(), R.layout.list_exercises_groups, exercisesGroups);
        setListAdapter(adapter);

        return rootView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
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

    public interface ExercisesGroupsListFragmentListener {
        void onExercisesGroupItemClicked(long exercisesGroupId);
    }

}
