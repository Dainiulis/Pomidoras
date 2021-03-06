package com.dmiesoft.fitpomodoro.ui.fragments.nested;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.application.FitPomodoroApplication;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.ui.activities.SettingsActivity;

public class NestedSaveExerciseFragment extends Fragment implements View.OnClickListener {

    private static final String EXERCISE_MODEL = ".model.Exercise";
    private static final String TAG = "NSEF";

    private Exercise exercise;
    private LinearLayout suggestionLayout;
    private TextView exerciseNameTV, repsTimeTV;
    private ImageButton subsBtn, addBtn;
    private Button doneBtn;
    private ImageView imageView;
    private View view;
    private FitPomodoroApplication appContext;
    private int repsValue, howManyTimesDone;
    private NestedExerciseFragListener mListener;
    private SharedPreferences prefs;

    public NestedSaveExerciseFragment() {
        // Required empty public constructor
    }

    public static NestedSaveExerciseFragment newInstance(Exercise exercise) {
        NestedSaveExerciseFragment fragment = new NestedSaveExerciseFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXERCISE_MODEL, exercise);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NestedExerciseFragListener) {
            mListener = (NestedExerciseFragListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement NestedExerciseFragListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercise = getArguments().getParcelable(EXERCISE_MODEL);
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        appContext = (FitPomodoroApplication) getActivity().getApplicationContext();
        repsValue = appContext.getReps();
        howManyTimesDone = appContext.getHowManyTimesDone();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_exercise_in_timer_ui, container, false);
        initViews();
        return view;
    }

    private void initViews() {
        exerciseNameTV = (TextView) view.findViewById(R.id.exerciseName);
        repsTimeTV = (TextView) view.findViewById(R.id.repsTime);
        subsBtn = (ImageButton) view.findViewById(R.id.substBtn);
        addBtn = (ImageButton) view.findViewById(R.id.addBtn);
        doneBtn = (Button) view.findViewById(R.id.doneBtn);
        loadExerciseSuggestion();
    }

    /**
     * If fragment is for saving what user have done during suggested exercise
     */
    private void loadExerciseSuggestion() {
        exerciseNameTV.setText(exercise.getName());
        if (repsValue >= 0) {
            repsTimeTV.setText(String.valueOf(appContext.getReps()));
        } else if (getAverageReps() > 0) {
            repsTimeTV.setText(String.valueOf(getAverageReps()));
        } else {
            repsTimeTV.setText(String.valueOf(getIntValue(repsTimeTV.getText().toString())));
        }
        subsBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        doneBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        repsValue = getIntValue(repsTimeTV.getText().toString());
        switch (v.getId()) {
            case R.id.substBtn:
                if (repsValue > 0) {
                    repsValue--;
                }
                break;
            case R.id.addBtn:
                repsValue++;
                break;
            case R.id.doneBtn:
                saveExerciseToHistory();
                break;
        }
        repsTimeTV.setText(String.valueOf(repsValue));
    }

    private void saveExerciseToHistory() {
        howManyTimesDone++;
        boolean needNewExercise = false;
        if (prefs.getInt(SettingsActivity.PREF_KEY_SETS_BEFORE_CHANGING_EXERCISE, 3) <= howManyTimesDone) {
            howManyTimesDone = 0;
            needNewExercise = true;
        }
        appContext.setAnimateViewPager(needNewExercise);
        mListener.onExerciseDonePressed(repsValue, exercise.getId(), exercise.getName(), exercise.getType(), needNewExercise);
    }

    private int getIntValue(String stringVal) {
        if (stringVal.equals("")) {
            stringVal = "0";
        }
        int val = Integer.parseInt(stringVal);
        return val;
    }

    private int getAverageReps() {
        if (exercise.getHowManyTimesDone() > 0) {
            return exercise.getTotalRepsDone() / exercise.getHowManyTimesDone();
        } else {
            return 0;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (appContext.shouldAnimateViewPager()) {
            repsValue = -1;
        }
        appContext.setReps(repsValue);
        appContext.setHowManyTimesDone(howManyTimesDone);
    }

    public interface NestedExerciseFragListener {
        void onExerciseDonePressed(int howMany, long exerciseId, String exerciseName, String exerciseType, boolean needNewExercise);
    }
}
