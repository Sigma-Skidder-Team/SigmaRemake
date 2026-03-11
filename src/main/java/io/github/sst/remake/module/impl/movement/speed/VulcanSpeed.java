package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.util.game.player.MovementUtils;

public class VulcanSpeed extends SubModule {
    private int offGroundTicks;
    private int jumpCount = 0;

    public VulcanSpeed() {
        super("Vulcan");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        offGroundTicks = 0;
        jumpCount = 0;
    }

    @Subscribe
    public void onMotion(MotionEvent event) {
        if (client.player == null) return;

        if (client.player.isOnGround()) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        // Force sprint
        client.options.keySprint.setPressed(true);

        if (client.player.isOnGround() && MovementUtils.isMoving() && this.parent != null && this.parent.enabled) {
            client.player.jump();
            jumpCount++;
            MovementUtils.strafe(0.48);
            if (jumpCount % 6 == 0) {
                setTimer(1.1f);
            } else {
                setTimer(1.05f);
            }
        }

        if (offGroundTicks < 4) {
            MovementUtils.strafe(MovementUtils.getSpeed());
        }
    }

    @Override
    public void onDisable() {
        setTimer(1.0f);
        jumpCount = 0;
        // stop forcing sprint on disable
        try {
            client.options.keySprint.setPressed(false);
        } catch (Throwable ignored) {}
    }
}
