package com.dmiesoft.fitpomodoro.ui.fragments;


import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.ui.activities.MainActivity;

import java.io.IOException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseDetailFragment extends Fragment {

    private static final String PACKAGE_EXERCISES = "com.dmiesoft.fitpomodoro.model.Exercise";
    private Exercise exercise;
    private View view;
    private ImageView exerciseIV;
    private TextView exerciseNameTV, exerciseTypeTV, exerciseDescriptionTV;
    private FloatingActionButton mainFab;

    public ExerciseDetailFragment() {
        // Required empty public constructor
    }

    public static ExerciseDetailFragment newInstance(Exercise exercise) {
        
        Bundle args = new Bundle();
        ExerciseDetailFragment fragment = new ExerciseDetailFragment();
        args.putParcelable(PACKAGE_EXERCISES, exercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            exercise = getArguments().getParcelable(PACKAGE_EXERCISES);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        
        view = inflater.inflate(R.layout.fragment_exercise_detail, container, false);
        initializeViews();
        return view;
    }

    private void initializeViews() {
        exerciseNameTV = (TextView) view.findViewById(R.id.nameExercise);
        exerciseTypeTV = (TextView) view.findViewById(R.id.typeExercise);
        exerciseDescriptionTV = (TextView) view.findViewById(R.id.descriptionExercise);
        exerciseIV = (ImageView) view.findViewById(R.id.imageExercise);

        exerciseNameTV.setText(exercise.getName());
        exerciseTypeTV.setText(exercise.getType());
        exerciseDescriptionTV.setText(exercise.getDescription());
        exerciseIV.setImageDrawable(getDrawableFromAssets(exercise.getImage()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainFab = ((MainActivity) getActivity()).getMainFab();
        if(mainFab != null) {
            mainFab.hide();
        }
    }

    private Drawable getDrawableFromAssets(String image) {
        AssetManager assetManager = getContext().getAssets();
        InputStream stream = null;

        try {
            stream = assetManager.open(image + ".png");
            Drawable drawable = Drawable.createFromStream(stream, null);
            return drawable;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
