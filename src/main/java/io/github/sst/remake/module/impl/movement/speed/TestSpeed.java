package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.util.game.player.MovementUtils;

public class TestSpeed extends SubModule {
    public final BooleanSetting sprint = new BooleanSetting("Sprint", "Sprints when walking", true);
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", "Automatically jumps for you.", true);

    private int tickCounter = 0;
    private float strafeYaw = 1.0F;
    private boolean wasOnGround = false;

    public TestSpeed() {
        super("TestSpeed");
    }

    @Override
    public void onEnable() {
        this.tickCounter = 0;
        this.wasOnGround = false;
        this.strafeYaw = 1.0f;
    }

    @Override
    public void onDisable() {
        // restore key states
        client.options.keySprint.setPressed(false);
        client.options.keyJump.setPressed(false);
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (client.player == null) return;
        if (client.player.isOnGround() && MovementUtils.isMoving() && autoJump.value) {
            client.player.jump();
            event.setY(MovementUtils.getJumpValue());
        }
        // Keep basic behaviour but allow optional sprint boosting
        double base = MovementUtils.getSpeed();
        if (sprint.value && client.player.isSprinting()) base *= 1.05;
        MovementUtils.setMotion(event, base);
        tickCounter++;
    }

    @Subscribe
    public void onMotion(io.github.sst.remake.event.impl.game.player.MotionEvent event) {
        // Keep input-state consistent each tick like the legacy module did
        if (MovementUtils.isMoving()) {
            if (sprint.value) client.options.keySprint.setPressed(true);
            else client.options.keySprint.setPressed(false);

            if (autoJump.value) client.options.keyJump.setPressed(true);
            else client.options.keyJump.setPressed(false);
        } else {
            client.options.keySprint.setPressed(false);
            client.options.keyJump.setPressed(false);
        }
    }

    @Subscribe
    public void onJump(io.github.sst.remake.event.impl.game.player.JumpEvent event) {
        // Mirror legacy: record jump origin and ensure auto-jump key state
        this.wasOnGround = false;
        this.tickCounter = 0;
        if (client.player != null) {
            // Use the exposed yaw field
            this.strafeYaw = client.player.yaw;
        }
        if (autoJump.value) client.options.keyJump.setPressed(true);
    }
}
