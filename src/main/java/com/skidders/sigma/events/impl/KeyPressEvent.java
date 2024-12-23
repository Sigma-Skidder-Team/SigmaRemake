package com.skidders.sigma.events.impl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KeyPressEvent {
    public final int key, modifiers, action;
}
