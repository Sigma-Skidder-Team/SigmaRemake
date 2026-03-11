package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.util.game.player.MovementUtils;

public class SlowHopSpeed extends SubModule {
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", "Automatically jumps for you.", true);
    private int onGroundTicks = 2;
    private double speed;
    private boolean initialized = false;

    public SlowHopSpeed() {
        super("SlowHop");
    }

    @Override
    public void onEnable() {
        this.onGroundTicks = 2;
        this.speed = MovementUtils.getSpeed();
        this.initialized = false;
    }

    @Override
    public void onDisable() {
        this.initialized = false;
        this.onGroundTicks = 2;
        this.speed = MovementUtils.getSpeed();
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (client.player == null) return;
        if (!initialized) {
            speed = MovementUtils.getSpeed();
            initialized = true;
        }
        if (!client.player.isOnGround()) {
            onGroundTicks++;
            speed = 0.36 - (double) onGroundTicks / 250.0;
            if (speed < MovementUtils.getSpeed()) speed = MovementUtils.getSpeed();
            MovementUtils.setMotion(event, speed);
        } else {
            onGroundTicks = 0;
            if (autoJump.value) {
                client.player.jump();
                event.setY(MovementUtils.getJumpValue());
            }
        }
    }

    @Subscribe
    public void onJump(io.github.sst.remake.event.impl.game.player.JumpEvent event) {
        // ensure consistent hop behavior
    }
}
