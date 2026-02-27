package io.github.sst.remake.event.impl.game.player;

import io.github.sst.remake.event.Statable;
import lombok.AllArgsConstructor;

public class MotionEvent extends Statable {
    public double x, y, z;
    public float yaw, pitch;
    public boolean onGround;
    public boolean moving;

    public MotionEvent(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }
}
