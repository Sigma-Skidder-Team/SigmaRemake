package io.github.sst.remake.module.impl.movement;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.InputEvent;
import io.github.sst.remake.event.impl.game.player.JumpEvent;
import io.github.sst.remake.event.impl.game.player.VelocityYawEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.tracker.impl.RotationTracker;
import io.github.sst.remake.util.game.RotationUtils;

/**
 * @see <a href="https://github.com/Sumandora/tarasande/blob/1.20.4/src/main/kotlin/su/mandora/tarasande/feature/rotation/component/correctmovement/impl/Silent.kt">Silent mode</a>
 * @see <a href="https://github.com/Sumandora/tarasande/blob/1.20.4/src/main/kotlin/su/mandora/tarasande/feature/rotation/component/correctmovement/impl/Direct.kt">Direct mode</a>
 */
public class CorrectMovementModule extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Movement correction mode", 0, "Silent", "Direct");

    public CorrectMovementModule() {
        super("CorrectMovement", "Corrects your movement in combat.", Category.MOVEMENT);
    }

    @Subscribe(priority = Priority.HIGHEST)
    public void onInput(InputEvent event) {
        if (!mode.value.equals("Silent")) return;

        correctMovement(event, RotationTracker.yaw);
    }

    @Subscribe(priority = Priority.HIGHEST)
    public void onJump(JumpEvent event) {
        if (event.entity != client.player) return;

        event.yaw = RotationTracker.yaw;
    }

    @Subscribe(priority = Priority.HIGHEST)
    public void onVelocity(VelocityYawEvent event) {
        if (event.entity != client.player) return;

        event.yaw = RotationTracker.yaw;
    }

    private static void correctMovement(InputEvent event, float yaw) {
        if (client.player == null) return;

        final float forward = event.forward;
        final float strafe = event.strafe;

        final double angle = RotationUtils.normalizeYaw(Math.toDegrees(RotationUtils.getDirectionRadians(client.player.yaw, forward, strafe)));

        if (forward == 0 && strafe == 0) {
            return;
        }

        float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

        for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
            for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;

                final double predictedAngle = RotationUtils.normalizeYaw(Math.toDegrees(RotationUtils.getDirectionRadians(yaw, predictedForward, predictedStrafe)));
                final double difference = Math.abs(angle - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }

            event.forward = closestForward;
            event.strafe = closestStrafe;
        }
    }
}