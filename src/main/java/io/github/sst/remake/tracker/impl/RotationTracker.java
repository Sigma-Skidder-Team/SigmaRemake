package io.github.sst.remake.tracker.impl;

import io.github.sst.remake.tracker.Tracker;
import io.github.sst.remake.util.IMinecraft;

public final class RotationTracker extends Tracker implements IMinecraft {
    public static float yaw, pitch;
    public static float lastYaw, lastPitch;
}