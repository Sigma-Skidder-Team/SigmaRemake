package io.github.sst.remake.event.impl.client;

import io.github.sst.remake.event.Event;

public class InputEvent extends Event {
    public float forward, strafe;
    public boolean jumping, sneaking;
    public float sneakFactor;

    public InputEvent(float forward, float strafe, boolean jumping, boolean sneaking, float sneakFactor) {
        this.forward = forward;
        this.strafe = strafe;
        this.jumping = jumping;
        this.sneaking = sneaking;
        this.sneakFactor = sneakFactor;
    }
}