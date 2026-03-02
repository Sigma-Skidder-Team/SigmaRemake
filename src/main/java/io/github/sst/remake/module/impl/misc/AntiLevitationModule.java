package io.github.sst.remake.module.impl.misc;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;


public class AntiLevitationModule extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public AntiLevitationModule() {
        super("AntiLevitation", "Removes levitation effects.", Category.MISC);
    }


    @Override
    public void onEnable() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                client.player.removeStatusEffect(StatusEffects.LEVITATION);
            }
        });
    }
}




