package io.github.sst.remake.event.impl.game.render;

import io.github.sst.remake.event.Cancellable;
import net.minecraft.entity.LivingEntity;

public class RenderEntityYawEvent extends Cancellable {
    public LivingEntity livingEntity;
    public float tickDelta;
    public float result;

    public RenderEntityYawEvent(LivingEntity livingEntity, float tickDelta) {
        this.livingEntity = livingEntity;
        this.tickDelta = tickDelta;
    }
}
