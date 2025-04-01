package com.skidders.sigma.event.impl;

import com.skidders.sigma.event.Event;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WindowSizeChangeEvent extends Event {
    public final long window;
    public final int width, height;
}
