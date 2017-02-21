package com.dmiesoft.fitpomodoro.events.timer_handling;

public class TimerSendTimeEvent {

    private long millisecs;

    public TimerSendTimeEvent() {}

    public long getMillisecs() {
        return millisecs;
    }

    public void setMillisecs(long millisecs) {
        this.millisecs = millisecs;
    }
}
