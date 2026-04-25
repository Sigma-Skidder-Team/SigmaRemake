package io.github.sst.remake.module.impl.render.esp;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.module.impl.render.ESPModule;
import net.minecraft.entity.LivingEntity;

@SuppressWarnings("unused")
public class VanillaESP extends SubModule {
    public VanillaESP() {
        super("Vanilla");
    }

    @Override
    public ESPModule getParent() {
        return (ESPModule) super.parent;
    }

    @Override
    public void onDisable() {
        if (client.world == null) return;

        client.world.getEntities().forEach(entity -> {
            entity.setGlowing(false);
        });
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        for (LivingEntity entity : getParent().getTargets()) {
            entity.setGlowing(true);
        }
    }
}