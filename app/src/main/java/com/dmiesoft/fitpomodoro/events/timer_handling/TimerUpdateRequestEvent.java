package com.dmiesoft.fitpomodoro.events.timer_handling;

/**
 * Created by Dainius on 2017-02-07.
 */

public class TimerUpdateRequestEvent {

    private boolean isAsking;

    public TimerUpdateRequestEvent() {}

    public void askForCurrentState(boolean isAsking) {
        this.isAsking = isAsking;
    }

    public boolean isAskingForCurrentState() {
        return isAsking;
    }


}
