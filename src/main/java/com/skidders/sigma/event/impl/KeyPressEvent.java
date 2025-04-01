package com.skidders.sigma.event.impl;

import com.skidders.sigma.event.Event;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KeyPressEvent extends Event {
    public final int key, modifiers, action;
}
