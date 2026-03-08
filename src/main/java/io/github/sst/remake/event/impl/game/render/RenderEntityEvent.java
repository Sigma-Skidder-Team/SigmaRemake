package io.github.sst.remake.event.impl.game.render;

import io.github.sst.remake.event.Event;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.LivingEntity;

@RequiredArgsConstructor
public class RenderEntityEvent extends Event {
    public final LivingEntity entity;
    public boolean render = true;
}