package io.github.sst.remake.module.impl.misc;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.EndTickEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import net.minecraft.entity.player.PlayerEntity;

public class NickDetectorModule extends Module {
    public NickDetectorModule() {
        super("NickDetector", "Detect if a player has a custom name", Category.MISC);
    }

    @Subscribe
    public void onTick(EndTickEvent event) {
        if (client.world == null || client.player == null) return;

        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;
            //    if (Client.getInstance().botManager.isBot(player)) continue; - fix when this is added
            if (player.age <= 30) continue;
            if (player.hasCustomName()) {
                client.player.sendMessage(
                        new net.minecraft.text.LiteralText(
                                player.getEntityName() + " might have a custom nametag"
                        ),
                        false
                );
            }
        }
    }
}