package com.dmiesoft.fitpomodoro.events.timer_handling;

public class ExerciseIdSendEvent {

    long exerciseId;

    public ExerciseIdSendEvent(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }
}
