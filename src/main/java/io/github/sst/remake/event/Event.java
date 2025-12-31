package io.github.sst.remake.event;

import io.github.sst.remake.Client;

public class Event {
    public void call() {
        Client.BUS.call(this);
    }
}
