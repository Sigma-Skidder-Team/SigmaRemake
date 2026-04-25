package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.util.game.player.MovementUtils;

public class LegitSpeed extends SubModule {
    public LegitSpeed() {
        super("Legit");
    }

    @Override
    public void onDisable() {
        client.options.keySprint.setPressed(false);
        client.options.keyJump.setPressed(false);
    }

    @Subscribe
    public void onMotion(MotionEvent event) {
        client.options.keySprint.setPressed(MovementUtils.isMoving());
        client.options.keyJump.setPressed(MovementUtils.isMoving());
    }
}