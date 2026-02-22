package io.github.sst.remake.event.impl.game.player;

import io.github.sst.remake.event.Statable;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MotionEvent extends Statable {
    public double x, y, z;
    public float yaw, pitch;
    public boolean onGround;
}
