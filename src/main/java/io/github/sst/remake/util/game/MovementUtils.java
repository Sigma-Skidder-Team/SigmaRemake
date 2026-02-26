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
        float[] strafe = getDirectionArray();
        float forward = strafe[1];
        float side = strafe[2];
        float yaw = strafe[0];

        if (forward == 0.0F && side == 0.0F) {
            setPlayerXMotion(0.0);
            setPlayerZMotion(0.0);
        }

        double cos = Math.cos(Math.toRadians(yaw));
        double sin = Math.sin(Math.toRadians(yaw));
        double x = (forward * cos + side * sin) * speed;
        double z = (forward * sin - side * cos) * speed;

        setPlayerXMotion(x);
        setPlayerZMotion(z);
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

        setPlayerXMotion(moveEvent.getX());
        setPlayerZMotion(moveEvent.getZ());
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

    public static double getAacHopSpeed(int hopTicks, int speedStage, Runnable resetStage) {
        double speed = 0.29;

        // Base speed tables by hop tick
        double base3019 = 0.3019;
        double dynamicAdd = 0.0286 - (double) hopTicks / 1000.0;

        double[] baseSpeedByTick = new double[]{
                0.497, 0.3031, 0.302, base3019, base3019, base3019, base3019, base3019, base3019, base3019, base3019,
                0.3, 0.301, 0.298, 0.297
        };
        double[] stage2AddByTick = new double[]{
                0.1069, 0.0642, 0.0629, 0.0607, 0.0584, 0.0561, 0.0539, 0.0517, 0.0496, 0.0475, 0.0455,
                0.045, 0.042, 0.042, 0.042
        };
        double[] stage3AddByTick = new double[]{
                0.046, dynamicAdd, dynamicAdd, dynamicAdd, dynamicAdd, dynamicAdd, dynamicAdd, dynamicAdd, dynamicAdd,
                dynamicAdd, dynamicAdd, 0.018, dynamicAdd + 0.001, dynamicAdd + 0.001, dynamicAdd + 0.001
        };

        if (hopTicks < 0) {
            return speed;
        }

        if (hopTicks < baseSpeedByTick.length) {
            speed = baseSpeedByTick[hopTicks];
        }

        if (speedStage >= 2 && hopTicks < stage2AddByTick.length) {
            speed += stage2AddByTick[hopTicks];
        }

        if (speedStage >= 3 && hopTicks < stage3AddByTick.length) {
            speed += stage3AddByTick[hopTicks];
        }

        if (hopTicks == 12 && speedStage <= 2) {
            resetStage.run();
        }

        if (client.player.forwardSpeed <= 0.0F) {
            speed -= 0.06;
        }

        if (client.player.horizontalCollision) {
            speed -= 0.1;
            resetStage.run();
        }

        return speed;
    }

    public static double setPlayerXMotion(double x) {
        client.player.setVelocity(x, client.player.getVelocity().y, client.player.getVelocity().z);
        return x;
    }

    public static double setPlayerYMotion(double y) {
        client.player.setVelocity(client.player.getVelocity().x, y, client.player.getVelocity().z);
        return y;
    }

    public static double setPlayerZMotion(double z) {
        client.player.setVelocity(client.player.getVelocity().x, client.player.getVelocity().y, z);
        return z;
    }

}