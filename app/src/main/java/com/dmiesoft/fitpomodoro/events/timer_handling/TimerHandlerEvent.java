package com.dmiesoft.fitpomodoro.events.timer_handling;

public class TimerHandlerEvent {

    public static final int PUBLISHER_TIMER_UI_FRAGMENT = 5589;
    public static final int PUBLISHER_TIMER_TASK_FRAGMENT = 5570;

    private boolean shouldAnimateBackgroundColor, stopTimer;
    private int publisher;

    public TimerHandlerEvent() {
    }

    public int getPublisher() {
        return publisher;
    }

    public void setPublisher(int publisher) {
        this.publisher = publisher;
    }

    public boolean isShouldAnimateBackgroundColor() {
        return shouldAnimateBackgroundColor;
    }

    public void setShouldAnimateBackgroundColor(boolean shouldAnimateBackgroundColor) {
        this.shouldAnimateBackgroundColor = shouldAnimateBackgroundColor;
    }

    public boolean shouldStopTimer() {
        return stopTimer;
    }

    public void setStopTimer(boolean stopTimer) {
        this.stopTimer = stopTimer;
    }
}
