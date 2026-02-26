package io.github.sst.remake.util.game;

import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.util.IMinecraft;
import net.minecraft.client.input.Input;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;

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

    public static boolean isMoving() {
        return client.player.forwardSpeed != 0 || client.player.sidewaysSpeed != 0;
    }

    public static int getJumpBoost() {
        return !client.player.hasStatusEffect(StatusEffects.JUMP_BOOST) ? 0 : client.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1;
    }

    public static double getJumpValue() {
        return 0.42f + (double) getJumpBoost() * 0.1;
    }

    public static void strafe(double speed) {
        float[] adjusted = getDirectionArray();
        float forward = adjusted[1];
        float side = adjusted[2];
        float yaw = adjusted[0];

        if (forward == 0.0F && side == 0.0F) {
            stop();
        }

        double cos = Math.cos(Math.toRadians(yaw));
        double sin = Math.sin(Math.toRadians(yaw));
        double x = (forward * cos + side * sin) * speed;
        double z = (forward * sin - side * cos) * speed;

        client.player.setVelocity(x, client.player.getVelocity().y, z);
    }

    public static void setMotion(MoveEvent moveEvent, double motionSpeed) {
        float[] strafe = getDirectionArray();
        float forward = strafe[1];
        float side = strafe[2];
        float yaw = strafe[0];

        if (forward == 0.0F && side == 0.0F) {
            moveEvent.setX(0.0);
            moveEvent.setZ(0.0);
        }

        double cos = Math.cos(Math.toRadians(yaw));
        double sin = Math.sin(Math.toRadians(yaw));
        double x = (forward * cos + side * sin) * motionSpeed;
        double z = (forward * sin - side * cos) * motionSpeed;

        moveEvent.setX(x);
        moveEvent.setZ(z);

        client.player.setVelocity(moveEvent.getX(), client.player.getVelocity().y, moveEvent.getZ());
    }

    private static float[] getDirectionArray() {
        Input input = client.player.input;
        return getDirectionArray(input.movementForward, input.movementSideways);
    }

    private static float[] getDirectionArray(float forward, float strafe) {
        float yaw = client.player.yaw + 90.0f;

        if (forward != 0.0f) {
            if (!(strafe >= 1.0f)) {
                if (strafe <= -1.0f) {
                    yaw += (float) (!(forward > 0.0f) ? -45 : 45);
                    strafe = 0.0f;
                }
            } else {
                yaw += (float) (!(forward > 0.0f) ? 45 : -45);
                strafe = 0.0f;
            }

            if (!(forward > 0.0f)) {
                if (forward < 0.0f) {
                    forward = -1.0f;
                }
            } else {
                forward = 1.0f;
            }
        }

        return new float[]{yaw, forward, strafe};
    }

    public static void stop() {
        stop(false);
    }

    public static void stop(boolean stopY) {
        client.player.setVelocity(0, stopY ? 0.0 : client.player.getVelocity().y, 0);
    }

    public static double getSmartSpeed() {
        double speed = 0.2873;
        float multiplier = 1.0F;
        EntityAttributeInstance attribute = client.player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        multiplier = (float) ((double) multiplier * ((attribute.getValue() / (double) client.player.abilities.getWalkSpeed() + 1.0) / 2.0));
        if (client.player.isSprinting()) {
            multiplier = (float) ((double) multiplier - 0.15);
        }

        if (client.player.hasStatusEffect(StatusEffects.SPEED) && client.player.isSprinting()) {
            multiplier = (float) ((double) multiplier - 0.03000002 * (double) (client.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1));
        }

        if (client.player.isSneaking()) {
            speed *= 0.25;
        }

        if (client.player.isTouchingWater()) {
            speed *= 0.3;
        }

        return speed * (double) multiplier;
    }
}