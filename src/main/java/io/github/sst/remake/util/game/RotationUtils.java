package io.github.sst.remake.util.game;

import io.github.sst.remake.data.rotation.Rotation;
import io.github.sst.remake.util.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils implements IMinecraft {
    public static Rotation getRotationsRaw(Entity target) {
        if (target == null || client.player == null) return null;

        double dX = target.getX() - client.player.getX();
        double dY = target.getY() - client.player.getY();
        double dZ = target.getZ() - client.player.getZ();

        double dist = Math.hypot(dX, dZ);

        float yaw = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dY, dist));

        return new Rotation(yaw, pitch);
    }

    public static Rotation getRotationsInterpolated(Entity target) {
        if (target == null || client.player == null) return null;

        double targetX = target.getX() + (target.getX() - target.prevX) * client.getTickDelta();
        double targetY = target.getY() + (target.getY() - target.prevY) * client.getTickDelta();
        double targetZ = target.getZ() + (target.getZ() - target.prevZ) * client.getTickDelta();

        double dX = targetX - client.player.getX();
        double dY = targetY - client.player.getEyeHeight(client.player.getPose()) - 0.02F + target.getEyeHeight(target.getPose()) - client.player.getY();
        double dZ = targetZ - client.player.getZ();

        double dist = Math.hypot(dX, dZ);

        float yaw = wrapDegrees(client.player.yaw, (float) (Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0F);
        float pitch = wrapDegrees(client.player.pitch, (float) (-(Math.atan2(dY, dist) * 180.0 / Math.PI)));

        return new Rotation(yaw, pitch);
    }

    public static Rotation getRotationsSmart(Entity target, boolean raytrace) {
        Vec3d preferredAimPoint = WorldUtils.getBestVisiblePoint(target);

        if (raytrace && !WorldUtils.canSeeVector(preferredAimPoint)) {
            for (int verticalIndex = -1; verticalIndex < 2; verticalIndex++) {
                double yOffset = verticalIndex;

                if (verticalIndex != -1) {
                    yOffset *= target.getBoundingBox().getYLength();
                } else {
                    yOffset = target.getEyeHeight(target.getPose()) - 0.02F;
                }

                double targetX = target.getX();
                double targetZ = target.getZ();
                double targetY = target.getY() + yOffset + 0.05;

                double dX = targetX - client.player.getX();
                double dZ = targetZ - client.player.getZ();
                double dY = targetY - (double) client.player.getEyeHeight(client.player.getPose()) - 0.02F - client.player.getY();

                double dist = Math.hypot(dX, dZ);

                float yaw = clamp(
                        client.player.yaw,
                        (float) (Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0F,
                        360f
                );
                float pitch = clamp(
                        client.player.pitch,
                        (float) (-(Math.atan2(dY, dist) * 180.0 / Math.PI)),
                        360.0F
                );

                if (WorldUtils.canSeeVector(new Vec3d(targetX, targetY, targetZ))) {
                    return new Rotation(yaw, pitch);
                }

                for (int side = -1; side < 2; side += 2) {
                    targetX = target.getX() + (target.getX() - target.prevX) * (double) client.getTickDelta();
                    targetZ = target.getZ() + (target.getZ() - target.prevZ) * (double) client.getTickDelta();
                    targetY = target.getY() + 0.05 + (target.getY() - target.prevY) * (double) client.getTickDelta() + yOffset;

                    double halfX = target.getBoundingBox().getXLength() / 2.5 * (double) side;
                    double halfZ = target.getBoundingBox().getZLength() / 2.5 * (double) side;

                    if (client.player.getX() < targetX + halfX) {
                        if (client.player.getZ() > targetZ + halfZ) {
                            targetX += halfX;
                        } else {
                            targetX -= halfX;
                        }

                        if (client.player.getX() < targetX - halfX) {
                            targetZ += halfZ;
                        } else {
                            targetZ -= halfZ;
                        }
                    } else if (client.player.getX() > targetX + halfX) {
                        if (client.player.getZ() < targetZ - halfZ) {
                            targetX += halfX;
                        } else {
                            targetX -= halfX;
                        }

                        if (client.player.getX() > targetX + halfX) {
                            targetZ -= halfZ;
                        } else {
                            targetZ += halfZ;
                        }
                    } else {
                        if (client.player.getZ() > targetZ + halfZ) {
                            targetX += halfX;
                        } else {
                            targetX -= halfX;
                        }

                        if (client.player.getX() < targetX - halfX) {
                            targetZ -= halfZ;
                        } else {
                            targetZ += halfZ;
                        }
                    }

                    dX = targetX - client.player.getX();
                    dZ = targetZ - client.player.getZ();
                    dY = targetY - (double) client.player.getEyeHeight(client.player.getPose()) - 0.02 - client.player.getY();
                    dist = MathHelper.sqrt(dX * dX + dZ * dZ);

                    yaw = clamp(
                            client.player.yaw,
                            (float) (Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0F,
                            360.0F
                    );
                    pitch = clamp(
                            client.player.pitch,
                            (float) (-(Math.atan2(dY, dist) * 180.0 / Math.PI)),
                            360.0F
                    );

                    if (WorldUtils.canSeeVector(new Vec3d(targetX, targetY, targetZ))) {
                        return new Rotation(yaw, pitch);
                    }
                }
            }

            return null;
        }

        return getRotationsToVector(preferredAimPoint);
    }

    public static Rotation getRotationsToVector(Vec3d target) {
        Vec3d from = client.player.getPos().add(0.0, client.player.getEyeHeight(client.player.getPose()), 0.0);
        return getRotationsBetween(from, target);
    }

    public static Rotation getRotationsBetween(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double dy = to.y - from.y;

        double horizontalDistance = MathHelper.sqrt(dx * dx + dz * dz);

        float yaw = clamp(
                0.0F,
                (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F,
                360.0f
        );
        float pitch = clamp(
                client.player.pitch,
                (float) (-(Math.atan2(dy, horizontalDistance) * 180.0 / Math.PI)),
                360.0f
        );

        return new Rotation(yaw, pitch);
    }

    public static float getYawToEntityInterpolated(Entity target) {
        if (target == null || client.player == null) return 0f;

        double targetX = target.prevX + (target.getX() - target.prevX) * client.getTickDelta();
        double targetZ = target.prevZ + (target.getZ() - target.prevZ) * client.getTickDelta();

        double dX = targetX - client.player.getX();
        double dZ = targetZ - client.player.getZ();

        return (float) (Math.toDegrees(Math.atan2(dZ, dX)) - 90.0);
    }

    public static float getAngleMetricToEntity(Entity target, float playerYaw) {
        float targetYaw = getYawToEntityInterpolated(target);
        return Math.abs(MathHelper.wrapDegrees(targetYaw - playerYaw));
    }

    public static Rotation applyGcdFix(float currentYaw, float currentPitch, float targetYaw, float targetPitch) {
        float f = (float) (client.options.mouseSensitivity * 0.6f + 0.2f);
        float gcd = f * f * f * 1.2f;

        float deltaYaw = targetYaw - currentYaw;
        float deltaPitch = targetPitch - currentPitch;

        int stepsYaw = Math.round(deltaYaw / gcd);
        int stepsPitch = Math.round(deltaPitch / gcd);

        float fixedYaw = currentYaw + (stepsYaw * gcd);
        float fixedPitch = currentPitch + (stepsPitch * gcd);

        return new Rotation(normalizeYaw(fixedYaw), normalizePitch(fixedPitch));
    }

    public static float normalizeYaw(float yaw) {
        return MathHelper.wrapDegrees(yaw);
    }

    public static float normalizePitch(float pitch) {
        return MathHelper.clamp(pitch, -90f, 90f);
    }

    public static float clamp(float start, float end, float step) {
        float delta = MathHelper.wrapDegrees(end - start);

        if (delta > step) {
            delta = step;
        }

        if (delta < -step) {
            delta = -step;
        }

        return start + delta;
    }

    public static float wrapDegrees(float start, float end) {
        float wrappedDelta = MathHelper.wrapDegrees(end - start);
        return start + wrappedDelta;
    }

    public static float wrapDegrees2(float start, float end) {
        return MathHelper.wrapDegrees(-(start - end));
    }
}