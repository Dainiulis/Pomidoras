package com.dmiesoft.fitpomodoro.ui.fragments.nested;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;

import static com.dmiesoft.fitpomodoro.utils.BasicConstants.EXERCISE;

public class NestedExerciseStatsFragment extends Fragment {

    public NestedExerciseStatsFragment() {
        // Required empty public constructor
    }

    public static NestedExerciseStatsFragment newInstance(Exercise exercise) {
        NestedExerciseStatsFragment fragment = new NestedExerciseStatsFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXERCISE, exercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nested_exercise_stats, container, false);
    }

}
