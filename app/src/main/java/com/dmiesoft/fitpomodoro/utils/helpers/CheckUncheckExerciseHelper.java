package com.dmiesoft.fitpomodoro.utils.helpers;


import com.dmiesoft.fitpomodoro.model.Exercise;
import com.dmiesoft.fitpomodoro.model.ExercisesGroup;

import java.util.List;
import java.util.UUID;

public abstract class CheckUncheckExerciseHelper {

    public static void uncheckExercisesGroups(List<ExercisesGroup> exercisesGroups) {
        for (ExercisesGroup exercisesGroup : exercisesGroups) {
            exercisesGroup.setChecked(false);
        }
    }

    public static void uncheckExercises(List<Exercise> exercises) {
        for (Exercise exercise : exercises) {
            exercise.setChecked(false);
        }
    }

}
