package com.dmiesoft.fitpomodoro.ui.fragments.nested;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;

public class ExerciseInTimerUIFragment extends Fragment implements View.OnClickListener {

    private static final String EXERCISE_MODEL = ".model.Exercise";
    private static final String IS_IMAGE = "IS_IMAGE";
    private static final String TAG = "ETUIF";
    private Exercise exercise;
    private boolean isImage;
    private LinearLayout suggestionLayout;
    private TextView exerciseNameTV, repsTimeTV;
    private ImageButton subsBtn, addBtn;
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
        suggestionLayout = (LinearLayout) view.findViewById(R.id.suggestionLayout);
        exerciseNameTV = (TextView) view.findViewById(R.id.exerciseName);

        repsTimeTV = (TextView) view.findViewById(R.id.repsTime);
        repsTimeTV.setText(String.valueOf(getIntValue(repsTimeTV.getText().toString())));
        subsBtn = (ImageButton) view.findViewById(R.id.substBtn);
        subsBtn.setOnClickListener(this);
        addBtn = (ImageButton) view.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(this);

        imageView = (ImageView) view.findViewById(R.id.exerciseImage);
        if (isImage) {
            suggestionLayout.setVisibility(View.GONE);
            loadImage();
        } else {
            imageView.setVisibility(View.GONE);
            loadExerciseSuggestion();
        }
    }

    private void loadExerciseSuggestion() {
        exerciseNameTV.setText(exercise.getName());
    }

    private void loadImage() {
        Bitmap bitmap = BitmapHelper.getBitmapFromFiles(getContext(), exercise.getImage(), false, 0);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(getContext(), "No image for this exercise", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.substBtn:
                int val = getIntValue(repsTimeTV.getText().toString());
                if (val > 0) {
                    val--;
                }
                repsTimeTV.setText(String.valueOf(val));
                break;
            case R.id.addBtn:
                int val2 = getIntValue(repsTimeTV.getText().toString());
                val2++;
                repsTimeTV.setText(String.valueOf(val2));
                break;
        }
    }

    private int getIntValue(String stringVal) {
        if (stringVal.equals("")) {
            stringVal = "0";
        }
        int val = Integer.parseInt(stringVal);
        return val;
    }
}
