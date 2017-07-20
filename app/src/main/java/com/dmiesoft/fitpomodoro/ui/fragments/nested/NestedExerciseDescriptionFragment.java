package com.dmiesoft.fitpomodoro.ui.fragments.nested;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;

import static com.dmiesoft.fitpomodoro.utils.BasicConstants.EXERCISE;

public class NestedExerciseDescriptionFragment extends Fragment {

    private Exercise mExercise;
    private TextView exerciseDescriptionTV;

    public NestedExerciseDescriptionFragment() {
        // Required empty public constructor
    }

    public static NestedExerciseDescriptionFragment newInstance(Exercise exercise) {
        NestedExerciseDescriptionFragment fragment = new NestedExerciseDescriptionFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXERCISE, exercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mExercise = getArguments().getParcelable(EXERCISE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nested_exercise_description, container, false);
        exerciseDescriptionTV = (TextView) view.findViewById(R.id.exerciseDesc);
        exerciseDescriptionTV.setText(mExercise.getDescription());
        return view;
    }

}
