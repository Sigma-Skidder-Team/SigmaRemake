package io.github.sst.remake.event;

public class Cancellable extends Event {
    public boolean cancelled = false;

    public void cancel() {
        this.cancelled = true;
    }
}
