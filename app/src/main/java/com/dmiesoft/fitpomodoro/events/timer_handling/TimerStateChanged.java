package com.dmiesoft.fitpomodoro.events.timer_handling;

/**
 * Created by daini on 2017-06-04.
 */

public class TimerStateChanged {

    private boolean stateChanged;

    public TimerStateChanged(boolean stateChanged) {
        this.stateChanged = stateChanged;
    }

    public boolean isStateChanged() {
        return stateChanged;
    }

    public void setStateChanged(boolean stateChanged) {
        this.stateChanged = stateChanged;
    }
}
