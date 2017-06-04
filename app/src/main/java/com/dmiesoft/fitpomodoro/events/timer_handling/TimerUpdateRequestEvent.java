package com.dmiesoft.fitpomodoro.events.timer_handling;

public class TimerUpdateRequestEvent {

    private boolean isAsking;

    public TimerUpdateRequestEvent() {}

    public TimerUpdateRequestEvent(boolean isAsking) {
        this.isAsking = isAsking;
    }

    public void askForCurrentState(boolean isAsking) {
        this.isAsking = isAsking;
    }

    public boolean isAskingForCurrentState() {
        return isAsking;
    }


}
