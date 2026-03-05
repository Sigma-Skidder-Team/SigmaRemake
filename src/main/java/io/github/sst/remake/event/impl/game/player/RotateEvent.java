package io.github.sst.remake.event.impl.game.player;

import io.github.sst.remake.event.Event;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RotateEvent extends Event {
    public float yaw, pitch;
}