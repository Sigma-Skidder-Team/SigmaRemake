package io.github.sst.remake.module.impl.combat;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.world.LoadWorldEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.tracker.impl.BotTracker;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Objects;

public class AntiBotModule extends Module {
    private final BooleanSetting customNameCheck = new BooleanSetting("Custom name", "Should we check for custom names?", true);

    public AntiBotModule() {
        super("AntiBot", "Detect bots.", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        BotTracker.clear();
    }

    @Override
    public void onDisable() {
        BotTracker.clear();
    }

    @Subscribe
    public void onLoadWorld(LoadWorldEvent event) {
        BotTracker.clear();
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        for (PlayerEntity player : client.world.getPlayers()) {
            if (BotTracker.isBot(player)) continue;
            if (player == client.player) continue;

            if (customNameCheck.value && nameCheck(player)) {
                BotTracker.BOTS.add(player);
            }
        }
    }

    private boolean nameCheck(PlayerEntity player) {
        String displayName = player.getDisplayName().getString();
        String entityName = player.getName().getString();

        String customName =
                player.getCustomName() != null
                        ? player.getDisplayName().getString()
                        : null;

        if (!displayName.startsWith("§") && displayName.endsWith("§r")) {
            return true;
        }

        if (player.isInvisible()
                && entityName.equals(displayName)
                && Objects.equals(customName, entityName + "§r")) {
            return true;
        }

        if (customName != null
                && !customName.equalsIgnoreCase("")
                && displayName.toLowerCase().contains("§c")
                && displayName.toLowerCase().contains("§r")) {
            return true;
        }

        return displayName.contains("§8[NPC]")
                || (!displayName.contains("§c")
                && customName != null
                && !customName.equalsIgnoreCase(""));
    }
}