package com.skidders.sigma.events.impl;

import com.skidders.sigma.events.Event;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.util.math.MatrixStack;

@RequiredArgsConstructor
data class Render3DEvent(val matrixStack: MatrixStack, val tickDelta: Float, val limitTime: Long) : Event() {
}
