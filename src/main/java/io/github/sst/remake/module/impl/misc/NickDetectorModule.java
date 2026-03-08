package io.github.sst.remake.module.impl.misc;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.EndTickEvent;
import io.github.sst.remake.gui.screen.notifications.Notification;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.tracker.impl.BotTracker;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class NickDetectorModule extends Module {
    private final List<PlayerEntity> knownNickedPlayers = new ArrayList<>();

    public NickDetectorModule() {
        super("NickDetector", "Detects if players have custom names.", Category.MISC);
    }

    @Override
    public void onDisable() {
        knownNickedPlayers.clear();
    }

    @Subscribe
    public void onTick(EndTickEvent event) {
        if (client.world == null || client.player == null) return;

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;
            if (BotTracker.isBot(player)) continue;
            if (player.age <= 30) continue;
            if (player.hasCustomName() && !knownNickedPlayers.contains(player)) {
                int distance = (int) client.player.distanceTo(player);
                Client.INSTANCE.notificationManager.send(new Notification(
                        "NickDetector",
                        player.getEntityName() + " is probably a nick! (" + distance + "m)"
                ));
                knownNickedPlayers.add(player);
            }
        }
    }
}