package com.dmiesoft.fitpomodoro.events.timer_handling;

public class TimerTypeStateHandlerEvent {

    private int currentState;
    private int currentType;

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

}
