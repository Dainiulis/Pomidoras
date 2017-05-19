package com.dmiesoft.fitpomodoro.events.exercises;

import com.dmiesoft.fitpomodoro.model.ExerciseHistory;

public class UpdateNestedExerciseHistoryEvent {
    private ExerciseHistory exerciseHistory;

    public UpdateNestedExerciseHistoryEvent(ExerciseHistory exerciseHistory) {
        this.exerciseHistory = exerciseHistory;
    }

    public ExerciseHistory getExerciseHistory() {
        return exerciseHistory;
    }

    public void setExerciseHistory(ExerciseHistory exerciseHistory) {
        this.exerciseHistory = exerciseHistory;
    }
}
