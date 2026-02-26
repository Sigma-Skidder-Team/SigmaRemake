package io.github.sst.remake.event.impl.game.player;

import io.github.sst.remake.event.Event;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MovementFovEvent extends Event {
    public float speed;
}