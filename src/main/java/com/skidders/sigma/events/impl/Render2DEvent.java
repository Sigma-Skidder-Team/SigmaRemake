package com.skidders.sigma.events.impl;

import com.skidders.sigma.events.Event;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.util.math.MatrixStack;

@RequiredArgsConstructor
public class Render2DEvent extends Event {
    public final MatrixStack matrixStack;
}