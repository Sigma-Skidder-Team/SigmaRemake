package io.github.sst.remake.event.impl.game.render;

import io.github.sst.remake.event.Event;
import lombok.AllArgsConstructor;
import net.minecraft.entity.LivingEntity;

@AllArgsConstructor
public class RenderEntityRotationsEvent extends Event {
    public final LivingEntity livingEntity;
    public float yaw, pitch;
    public float bodyYaw, headYaw;
    public final float tickDelta;
}