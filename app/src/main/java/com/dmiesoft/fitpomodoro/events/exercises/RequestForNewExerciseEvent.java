package com.dmiesoft.fitpomodoro.events.exercises;


public class RequestForNewExerciseEvent {
    private boolean needNewExercise;

    public RequestForNewExerciseEvent(boolean needNewExercise) {
        this.needNewExercise = needNewExercise;
    }

    public boolean isNeedNewExercise() {
        return needNewExercise;
    }

    public void setNeedNewExercise(boolean needNewExercise) {
        this.needNewExercise = needNewExercise;
    }
}
