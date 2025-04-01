package com.skidders.sigma.event;

import com.skidders.sigma.util.client.events.State;
import com.skidders.sigma.util.client.interfaces.ISubscriber;

public abstract class Event implements ISubscriber {
    public State state = State.PRE;
    public boolean cancelled = false;

    public void post() {
        bus.post(this);
    }
}