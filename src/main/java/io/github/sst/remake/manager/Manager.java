package io.github.sst.remake.manager;

import io.github.sst.remake.Client;

public abstract class Manager {
    public void init() {
        Client.BUS.register(this);
    }

    public void shutdown() {
        Client.BUS.unregister(this);
    }
}