package io.github.sst.remake.module.impl.misc;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.EndTickEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import net.minecraft.entity.effect.StatusEffects;

@SuppressWarnings("unused")
public class AntiLevitationModule extends Module {
    public AntiLevitationModule() {
        super("AntiLevitation", "Removes levitation effect.", Category.MISC);
    }

    @Subscribe
    public void onTick(EndTickEvent event) {
        if (client.player != null) {
            client.player.removeStatusEffect(StatusEffects.LEVITATION);
        }
    }
}