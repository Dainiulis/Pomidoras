package com.dmiesoft.fitpomodoro.events.timer_handling;

/**
 * Created by Dainius on 2017-02-07.
 */

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
