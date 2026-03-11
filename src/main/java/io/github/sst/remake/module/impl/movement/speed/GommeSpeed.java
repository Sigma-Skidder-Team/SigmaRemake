package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.util.game.player.MovementUtils;
import io.github.sst.remake.util.game.combat.RotationUtils;

public class GommeSpeed extends SubModule {
    // Internal variables for boost/decay stages
    private int stage = 0;
    private int groundTicks = 0;
    private double currentSpeed = 0.0;
    private float currentYaw = 0f;

    public GommeSpeed() {
        super("Gomme");
    }

    @Override
    public void onEnable() {
        stage = 0;
        groundTicks = 0;
        currentYaw = RotationUtils.getDirectionArray()[0];
        currentSpeed = MovementUtils.getSpeed();
    }

    @Override
    public void onDisable() {
        MovementUtils.strafe(0.27);
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (!client.player.isOnGround()) {
            if (currentSpeed > 0.0 && groundTicks > 0) {
                stage++;
                if (stage == 1) {
                    double[] boost = {0.3686, 0.3688};
                    currentSpeed = groundTicks - 1 >= boost.length ? 0.28 : boost[groundTicks - 1];
                } else if (stage != 1 && stage != 11) {
                    double[] decay = {0.98, 0.98};
                    currentSpeed *= groundTicks - 1 < decay.length ? decay[groundTicks - 1] : 0.98;
                } else if (stage == 11) {
                    double[] decay = {0.999, 0.999};
                    currentSpeed *= groundTicks - 1 < decay.length ? decay[groundTicks - 1] : 0.98;
                }
                // Use turn-limited motion to better mirror legacy behavior
                currentYaw = MovementUtils.setMotionWithTurnLimit(event, Math.max(currentSpeed, 0.23), RotationUtils.getDirectionArray()[0], currentYaw, 45.0F);
            } else {
                currentSpeed = 0.1;
                MovementUtils.setMotion(event, 0.0);
            }
        } else if (MovementUtils.isMoving()) {
            stage = 0;
            event.setY(MovementUtils.getJumpValue());
            double[] startBoost = {0.549, 0.625};
            currentSpeed = startBoost[Math.min(groundTicks, startBoost.length - 1)];
            if (groundTicks < startBoost.length) groundTicks++;
            MovementUtils.setMotion(event, currentSpeed);
        }
    }

    @Subscribe
    public void onReceivePacket(io.github.sst.remake.event.impl.game.net.ReceivePacketEvent event) {
        if (event.packet instanceof net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket) {
            // reset ground tick counter like legacy
            groundTicks = 0;
            stage = 0;
        }
    }
}
