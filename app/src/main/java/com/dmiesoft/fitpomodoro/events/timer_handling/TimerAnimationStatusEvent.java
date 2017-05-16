package com.dmiesoft.fitpomodoro.events.timer_handling;

/**
 * Created by daini on 2017-05-04.
 */

public class TimerAnimationStatusEvent {

    private boolean animated;

    public TimerAnimationStatusEvent(boolean animated) {
        this.animated = animated;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }
}
