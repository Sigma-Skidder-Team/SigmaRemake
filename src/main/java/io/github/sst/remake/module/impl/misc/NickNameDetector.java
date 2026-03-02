package io.github.sst.remake.module.impl.misc;

import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class NickNameDetector extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public NickNameDetector() {
        super("NickNameDetector", "Detect if a player has a custom name", Category.MISC);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!this.isEnabled()) return;
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
        });
    }
}