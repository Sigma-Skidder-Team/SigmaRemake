package io.github.sst.remake.module.impl.movement;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.SafeWalkEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;

public class SafeWalkModule extends Module {
    public SafeWalkModule() {
        super("SafeWalk", "Doesn't let you run off edges", Category.MOVEMENT);
    }

    @Subscribe
    public void onSafeWalk(SafeWalkEvent event) {
        if (client.player == null) return;
        
        if (client.player.isOnGround() && !Client.INSTANCE.moduleManager.getModule(BlockFlyModule.class).enabled) {
            event.setSafe(true);
        }
    }
}