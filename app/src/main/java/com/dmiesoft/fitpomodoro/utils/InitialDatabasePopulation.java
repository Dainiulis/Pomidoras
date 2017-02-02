package com.dmiesoft.fitpomodoro.utils;


import android.content.Context;
import android.util.Log;

import com.dmiesoft.fitpomodoro.R;
import com.dmiesoft.fitpomodoro.database.ExercisesDataSource;
import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class InitialDatabasePopulation {

    private static final String TAG = "IDP";
    private Context context;
    private ExercisesDataSource dataSource;

    public InitialDatabasePopulation(Context context, ExercisesDataSource dataSource){
        this.context = context;
        this.dataSource = dataSource;
    }

    public List<ExercisesGroup> readJson() throws IOException, JSONException {

        InputStream stream = context.getResources().openRawResource(R.raw.json_data);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
        StringBuffer stringBuffer = new StringBuffer();
        while (bufferedInputStream.available() != 0) {
            char c = (char) bufferedInputStream.read();
            stringBuffer.append(c);
        }
        bufferedInputStream.close();
        stream.close();

        JSONArray allData = new JSONArray(stringBuffer.toString());
        List<ExercisesGroup> exercisesGroups = new ArrayList<>();
        for (int i = 0; i < allData.length(); i++) {

            ExercisesGroup exercisesGroup = new ExercisesGroup();
            String exerciseGroupName = allData.getJSONObject(i).getString("exercise_group");
            exercisesGroup.setName(exerciseGroupName);
            exercisesGroup = dataSource.createExercisesGroup(exercisesGroup);

            List<Exercise> exercises = new ArrayList<>();
            JSONArray exercisesData = allData.getJSONObject(i).getJSONArray("exercises");
            for (int j = 0; j < exercisesData.length(); j++) {
                Exercise exercise = new Exercise();
                exercise.setName(exercisesData.getJSONObject(j).getString("name"));
                exercise.setType(exercisesData.getJSONObject(j).getString("type"));
                exercise.setExercise_group_id(exercisesGroup.getId());
                exercise = dataSource.createExercise(exercise);
                exercises.add(exercise);
            }
            exercisesGroup.setExercises(exercises);
            exercisesGroups.add(exercisesGroup);
        }
        return exercisesGroups;
    }

}
