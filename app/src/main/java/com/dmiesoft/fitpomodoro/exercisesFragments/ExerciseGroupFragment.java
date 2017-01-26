package com.dmiesoft.fitpomodoro.exercisesFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dmiesoft.fitpomodoro.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseGroupFragment extends Fragment {

    private static final String TAG = "TAGAS";


    public ExerciseGroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercise_group, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach: ");
    }
}
