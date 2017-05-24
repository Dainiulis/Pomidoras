package com.dmiesoft.fitpomodoro.events.timer_handling;

public class TimerTypeStateHandlerEvent {

    public static final int PUBLISHER_TIMER_UI_FRAGMENT = 5589;
    public static final int PUBLISHER_TIMER_TASK_FRAGMENT = 5570;

    private int currentState;
    private int currentType;
    private int previousState;
    private int previousType;
    private boolean shouldAnimate, sessionFinished, shouldAnimateBackgroundColor;
    private int publisher;

    public TimerTypeStateHandlerEvent() {}

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public int getCurrentType() {
        return currentType;
    }

    public void setCurrentType(int currentType) {
        this.currentType = currentType;
    }

    public int getPublisher() {
        return publisher;
    }

    public void setPublisher(int publisher) {
        this.publisher = publisher;
    }

    public boolean isShouldAnimate() {
        return shouldAnimate;
    }

    public void setShouldAnimate(boolean shouldAnimate) {
        this.shouldAnimate = shouldAnimate;
    }

    public boolean isSessionFinished() {
        return sessionFinished;
    }

    public void setSessionFinished(boolean sessionFinished) {
        this.sessionFinished = sessionFinished;
    }

    public int getPreviousState() {
        return previousState;
    }

    public void setPreviousState(int previousState) {
        this.previousState = previousState;
    }

    public int getPreviousType() {
        return previousType;
    }

    public void setPreviousType(int previousType) {
        this.previousType = previousType;
    }

    public boolean isShouldAnimateBackgroundColor() {
        return shouldAnimateBackgroundColor;
    }

    public void setShouldAnimateBackgroundColor(boolean shouldAnimateBackgroundColor) {
        this.shouldAnimateBackgroundColor = shouldAnimateBackgroundColor;
    }
}
