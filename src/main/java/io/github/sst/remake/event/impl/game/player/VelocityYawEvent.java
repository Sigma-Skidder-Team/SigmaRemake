package io.github.sst.remake.event.impl.game.player;

import io.github.sst.remake.event.Event;
import lombok.AllArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@AllArgsConstructor
public class VelocityYawEvent extends Event {
    public final Entity entity;
    public Vec3d movementInput;
    public float yaw, speed;
}
