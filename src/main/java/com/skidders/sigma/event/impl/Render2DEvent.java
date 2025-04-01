package com.skidders.sigma.event.impl;

import com.skidders.sigma.event.Event;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.util.math.MatrixStack;

@RequiredArgsConstructor
public class Render2DEvent extends Event {
    public final MatrixStack matrixStack;
}
