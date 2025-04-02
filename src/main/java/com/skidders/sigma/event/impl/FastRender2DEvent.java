package com.skidders.sigma.event.impl;

import com.skidders.sigma.event.Event;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FastRender2DEvent extends Event {
    public final float tickDelta;
    public final long startTime;
    public final boolean tick;
}
