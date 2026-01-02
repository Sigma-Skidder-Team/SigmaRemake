package io.github.sst.remake.util.game;

import io.github.sst.remake.util.IMinecraft;

public class MovementUtils implements IMinecraft {

    public static float getDirection(float forward, float strafing, float yaw) {
        if (forward == 0.0f && strafing == 0.0f) {
            return yaw;
        }

        boolean isReversed = forward < 0.0f;
        float strafingAdjustment = 90.0f * (isReversed ? -0.5f : (forward > 0.0f ? 0.5f : 1.0f));

        if (isReversed) {
            yaw += 180.0f;
        }

        if (strafing != 0.0f) {
            yaw += (strafing > 0.0f) ? -strafingAdjustment : strafingAdjustment;
        }

        return yaw;
    }

    public static float getDirection() {
        return getDirection(client.player.input.movementForward, client.player.input.movementSideways, client.player.yaw);
    }

}
