package io.github.sst.remake.tracker;

import io.github.sst.remake.Client;

public abstract class Tracker {
    public void enable() {
        Client.BUS.register(this);
    }

    public void disable() {
        Client.BUS.unregister(this);
    }
}
