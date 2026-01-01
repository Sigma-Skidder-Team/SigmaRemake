package io.github.sst.remake.event.impl.game;

import io.github.sst.remake.event.Event;

public class RunLoopEvent extends Event {
    public boolean pre = true;

    public RunLoopEvent(boolean pre) {
        this.pre = pre;
    }
}
