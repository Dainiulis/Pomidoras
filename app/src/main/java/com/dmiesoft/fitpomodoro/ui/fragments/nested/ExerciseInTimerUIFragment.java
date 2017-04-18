package com.dmiesoft.fitpomodoro.ui.fragments.nested;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;

public class ExerciseInTimerUIFragment extends Fragment {

    private static final String EXERCISE_MODEL = ".model.Exercise";
    private static final String IS_IMAGE = "IS_IMAGE";
    private Exercise exercise;
    private boolean isImage;
    private TextView textView;
    private ImageView imageView;
    private View view;

    public ExerciseInTimerUIFragment() {
        // Required empty public constructor
    }

    public static ExerciseInTimerUIFragment newInstance(Exercise exercise, boolean isImage) {
        ExerciseInTimerUIFragment fragment = new ExerciseInTimerUIFragment();
        Bundle args = new Bundle();

        args.putParcelable(EXERCISE_MODEL, exercise);
        args.putBoolean(IS_IMAGE, isImage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercise = getArguments().getParcelable(EXERCISE_MODEL);
            isImage = getArguments().getBoolean(IS_IMAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_exercise_in_timer_ui, container, false);
        initViews();
        return view;
    }

    private void initViews() {
        textView = (TextView) view.findViewById(R.id.exerciseDetails);
        imageView = (ImageView) view.findViewById(R.id.exerciseImage);
        if (isImage) {
            textView.setVisibility(View.GONE);
            loadImage();
        } else {
            imageView.setVisibility(View.GONE);
            loadText();
        }
    }

    private void loadText() {
        textView.setText(exercise.getName());
    }

    private void loadImage() {
        Bitmap bitmap = BitmapHelper.getBitmapFromFiles(getContext(), exercise.getImage(), false, 0);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(getContext(), "No image for this exercise", Toast.LENGTH_SHORT).show();
        }
    }

}
