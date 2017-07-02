package com.dmiesoft.fitpomodoro.events.timer_handling;

public class ChangeExerciseEvent {

    boolean isChangeExercise;

    public ChangeExerciseEvent(boolean isChangeExercise) {
        this.isChangeExercise = isChangeExercise;
    }

    public boolean isChangeExercise() {
        return isChangeExercise;
    }
}
