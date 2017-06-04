package com.dmiesoft.fitpomodoro.events.timer_handling;

public class ChangeExerciseEvent {

    long exerciseId;
    boolean isSessionFinished;

    public ChangeExerciseEvent(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public boolean isSessionFinished() {
        return isSessionFinished;
    }

    public void setSessionFinished(boolean sessionFinished) {
        isSessionFinished = sessionFinished;
    }
}
