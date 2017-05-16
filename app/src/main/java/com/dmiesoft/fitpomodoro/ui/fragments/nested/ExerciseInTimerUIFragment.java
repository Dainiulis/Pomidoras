package com.dmiesoft.fitpomodoro.ui.fragments.nested;


import android.app.Application;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.application.GlobalVariables;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.utils.helpers.BitmapHelper;

import java.util.Locale;

public class ExerciseInTimerUIFragment extends Fragment implements View.OnClickListener {

    public static final int PAGE_SAVE_PROGRESS = 4477;
    public static final int PAGE_ALL_HISTORY = 4488;
    public static final int PAGE_CURRENT_HISTORY = 4499;
    public static final int PAGE_IMAGE = 4411;
    private static final String EXERCISE_MODEL = ".model.Exercise";
    private static final String CURRENT_PAGE = "CURRENT_PAGE";

    private static final String TAG = "ETUIF";

    private Exercise exercise;
    private int mCurrentPage;
    private LinearLayout suggestionLayout;
    private TextView exerciseNameTV, repsTimeTV;
    private ImageButton subsBtn, addBtn;
    private Button doneBtn;
    private ImageView imageView;
    private View view;
    private GlobalVariables appContext;
    private int repsValue;
    private TextToSpeech textToSpeech;

    public ExerciseInTimerUIFragment() {
        // Required empty public constructor
    }

    public static ExerciseInTimerUIFragment newInstance(Exercise exercise, int currentPage) {
        ExerciseInTimerUIFragment fragment = new ExerciseInTimerUIFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXERCISE_MODEL, exercise);
        args.putInt(CURRENT_PAGE, currentPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exercise = getArguments().getParcelable(EXERCISE_MODEL);
            mCurrentPage = getArguments().getInt(CURRENT_PAGE);
        }
        appContext = (GlobalVariables) getActivity().getApplicationContext();
        repsValue = appContext.getReps();
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
        subsBtn = (ImageButton) view.findViewById(R.id.substBtn);
        addBtn = (ImageButton) view.findViewById(R.id.addBtn);
        doneBtn = (Button) view.findViewById(R.id.doneBtn);

        imageView = (ImageView) view.findViewById(R.id.exerciseImage);

        if (exercise != null) {
            if (mCurrentPage == PAGE_IMAGE) {
                suggestionLayout.setVisibility(View.GONE);
                loadImage();
            } else if (mCurrentPage == PAGE_SAVE_PROGRESS){
                imageView.setVisibility(View.GONE);
                loadExerciseSuggestion();
            }
        }
    }

    /**
     * If fragment is for saving what user have done during suggested exercise
     */
    private void loadExerciseSuggestion() {
//        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if (status != TextToSpeech.ERROR) {
//                    textToSpeech.setLanguage(Locale.US);
//                }
//            }
//        });
        exerciseNameTV.setText(exercise.getName());
        Log.i(TAG, "loadExerciseSuggestion: " + appContext.getReps());
        if (repsValue >= 0) {
            repsTimeTV.setText(String.valueOf(appContext.getReps()));
        } else {
            repsTimeTV.setText(String.valueOf(getIntValue(repsTimeTV.getText().toString())));
        }
        subsBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        doneBtn.setOnClickListener(this);
    }

    /**
     * For loading fragment image
     */
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
        repsValue = getIntValue(repsTimeTV.getText().toString());
        switch (v.getId()) {
            case R.id.substBtn:
//                int val = getIntValue(repsTimeTV.getText().toString());
//                if (val > 0) {
//                    val--;
//                }
//                repsTimeTV.setText(String.valueOf(val));
                if (repsValue > 0) {
                    repsValue--;
                }
                break;
            case R.id.addBtn:
//                int val2 = getIntValue(repsTimeTV.getText().toString());
//                val2++;
//                repsTimeTV.setText(String.valueOf(val2));
                repsValue++;
                break;
            case R.id.doneBtn:
                break;
        }
        repsTimeTV.setText(String.valueOf(repsValue));
    }

    private int getIntValue(String stringVal) {
        if (stringVal.equals("")) {
            stringVal = "0";
        }
        int val = Integer.parseInt(stringVal);
        return val;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCurrentPage == PAGE_SAVE_PROGRESS) {
            appContext.setReps(repsValue);
        }
    }
}
