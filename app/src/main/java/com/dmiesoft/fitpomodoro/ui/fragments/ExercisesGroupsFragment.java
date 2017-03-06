package com.dmiesoft.fitpomodoro.ui.fragments;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.utils.adapters.ExercisesGroupListAdapter;

import java.util.ArrayList;
import java.util.List;


public class ExercisesGroupsFragment extends ListFragment implements View.OnClickListener {

    private static final String TAG = "EGF";
    private static final String PACKAGE_EXERCISES_GROUP = "com.dmiesoft.fitpomodoro.model.ExercisesGroup";
    private List<ExercisesGroup> exercisesGroups;
    private ExercisesGroupListAdapter adapter;
    private ExercisesGroupsListFragmentListener mListener;
    private FloatingActionButton mainFab;
    private Handler handlerFab;
    private Runnable runnableFab;

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                long exercisesGroupId = exercisesGroups.get(position).getId();
                mListener.onExercisesGroupItemLongClicked(exercisesGroupId);
                return true;
            }
        });
    }

    private void initViews(View rootView) {
        mainFab = (FloatingActionButton) ((MainActivity) getActivity()).getMainFab();
        mainFab.setOnClickListener(this);
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
        handlerFab.removeCallbacks(runnableFab);
        hideFab();
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
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab_main:
                mListener.onAddExerciseGroupBtnClicked();
                break;
        }
    }

    public interface ExercisesGroupsListFragmentListener {
        void onExercisesGroupItemClicked(long exercisesGroupId);
        void onAddExerciseGroupBtnClicked();
        void onExercisesGroupItemLongClicked(long exercisesGroupId);
    }

    public void updateListView(ExercisesGroup exercisesGroup, boolean animate) {
        if (exercisesGroup == null) {
            if (!animate) {
                adapter.clearViewsToAnimate();
            }
            adapter.notifyDataSetChanged();
            return;
        }
        int index = 0;
        boolean found = false;
        for (ExercisesGroup e : exercisesGroups) {
            if (e.getId() == exercisesGroup.getId()) {
                index = exercisesGroups.indexOf(e);
                found = true;
                break;
            }
        }
        if (found) {
            exercisesGroups.remove(index);
            exercisesGroups.add(index, exercisesGroup);
        } else {
            exercisesGroups.add(exercisesGroup);
        }
        adapter.notifyDataSetChanged();
    }

}
