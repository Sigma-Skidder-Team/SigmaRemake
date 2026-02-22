package io.github.sst.remake.event;

import io.github.sst.remake.data.bus.State;

public class Statable extends Event {
    public State state = State.PRE;

    public boolean isPre() {
        return state == State.PRE;
    }

    public boolean isPost() {
        return state == State.POST;
    }
}
