package com.skidders.sigma.events.impl;

import com.skidders.sigma.events.Event;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.util.math.MatrixStack;

@RequiredArgsConstructor
public class Render3DEvent extends Event {
    public final MatrixStack matrixStack;
    public final float tickDelta;
    public final long limitTime;
}
