package io.github.sst.remake.tracker.impl;

import io.github.sst.remake.tracker.Tracker;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class BotTracker extends Tracker {
    public static final List<PlayerEntity> BOTS = new ArrayList<>();

    public static void clear() {
        BOTS.clear();
    }

    public static boolean isBot(PlayerEntity entity) {
        return BOTS.contains(entity);
    }
}