package com.dmiesoft.fitpomodoro.utils.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.model.Exercise;

import java.util.List;

public class ExercisesListAdapter extends ArrayAdapter<Exercise> {

    private List<Exercise> exercises;

    public ExercisesListAdapter(Context context, int resource, List<Exercise> exercises) {
        super(context, resource, exercises);
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_exercises, parent, false);
        }

        Exercise exercise = exercises.get(position);

        TextView nameText = (TextView) convertView.findViewById(R.id.nameExercise);
        nameText.setText(exercise.getName());

        return convertView;
    }
}
