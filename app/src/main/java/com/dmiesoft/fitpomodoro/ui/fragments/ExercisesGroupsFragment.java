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
import android.widget.AdapterView;
import android.widget.ListView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;
import com.dmiesoft.fitpomodoro.utils.ObjectsHelper;
import com.dmiesoft.fitpomodoro.utils.adapters.ExercisesGroupListAdapter;

import java.util.ArrayList;
import java.util.List;


public class ExercisesGroupsFragment extends ListFragment implements View.OnClickListener {

    private static final String TAG = "EGF";
    private static final String PACKAGE_EXERCISES_GROUP = "com.dmiesoft.fitpomodoro.model.ExercisesGroup";
    private List<ExercisesGroup> exercisesGroups;
    private ExercisesGroupListAdapter adapter;
    private ExercisesGroupsListFragmentListener mListener;
    private boolean isFabOpen = false;
    private FloatingActionButton mainFab, addExGrFab, deleteFab, addFavFab;
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
        addExGrFab = (FloatingActionButton) ((MainActivity) getActivity()).getAddFab();
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
        addExGrFab.setOnClickListener(this);
        addFavFab.setOnClickListener(this);
        deleteFab.setOnClickListener(this);
    }

    private void animateFab() {
        if (isFabOpen) {
            mainFab.startAnimation(fabRotateBackward);
            addExGrFab.startAnimation(fabClose);
            addFavFab.startAnimation(fabClose);
            deleteFab.startAnimation(fabClose);
            addExGrFab.setClickable(false);
            addFavFab.setClickable(false);
            deleteFab.setClickable(false);
            isFabOpen = false;
        } else {
            mainFab.startAnimation(fabRotateFroward);
            addExGrFab.startAnimation(fabOpen);
            addFavFab.startAnimation(fabOpen);
            deleteFab.startAnimation(fabOpen);
            addExGrFab.setClickable(true);
            addFavFab.setClickable(true);
            deleteFab.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
//        isFabOpen=false;
        long exercisesGroupId = exercisesGroups.get(position).getId();
        mListener.onExercisesGroupItemClicked(exercisesGroupId);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isFabOpen) {
            animateFab();
        }
        mainFab.hide();
    }

    @Override
    public void onResume() {
        super.onResume();
        mainFab.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab_main:
                animateFab();
                break;
            case R.id.fab_add:
                mListener.onAddExerciseGroupBtnClicked();
                break;
        }
    }

    public interface ExercisesGroupsListFragmentListener {
        void onExercisesGroupItemClicked(long exercisesGroupId);
        void onAddExerciseGroupBtnClicked();
        void onExercisesGroupItemLongClicked(long exercisesGroupId);
    }

    public void updateListView(ExercisesGroup exercisesGroup) {
        if (exercisesGroup == null) {
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
