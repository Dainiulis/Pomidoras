package com.dmiesoft.fitpomodoro.utils;

import com.squareup.otto.Bus;

public class EventBus extends Bus{
    private static final EventBus bus = new EventBus();

    public static final Bus getInstance() {
        return bus;
    }

    private EventBus() {}
}
