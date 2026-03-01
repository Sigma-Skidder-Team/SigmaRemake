package io.github.sst.remake.util.game;

import io.github.sst.remake.data.rotation.Rotation;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.game.world.RaytraceUtils;
import net.minecraft.client.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils implements IMinecraft {
    public static Rotation getBlockRotations(BlockPos targetPos, Direction side) {
        float offsetX = 0.0f;
        float offsetZ = 0.0f;

        float offsetY = (float) (0.4f + Math.random() * 0.1f);

        switch (side) {
            case EAST:
                offsetX += 0.49f;
                break;
            case NORTH:
                offsetZ -= 0.49f;
                break;
            case SOUTH:
                offsetZ += 0.49f;
                break;
            case WEST:
                offsetX -= 0.49f;
                break;
            case UP:
                offsetY = 0.0f;
                offsetX = 0.26f - (float) (Math.random() * 0.2f);
                offsetZ = 0.26f - (float) (Math.random() * 0.2f);
                break;
            case DOWN:
                offsetY = 1.0f;
                offsetX = 0.26f - (float) (Math.random() * 0.2f);
                offsetZ = 0.26f - (float) (Math.random() * 0.2f);
        }

        if (offsetX == 0.0f) {
            offsetX = (float) (0.1f - Math.sin((double) (System.currentTimeMillis() - 500L) / 1200.0) * 0.2);
        }

        if (offsetZ == 0.0f) {
            offsetZ = (float) (0.1f - Math.sin((double) (System.currentTimeMillis() - 500L) / 1000.0) * 0.2);
        }

        if (offsetY == 0.0f) {
            offsetY = (float) (0.6f - Math.sin((double) (System.currentTimeMillis() - 500L) / 1600.0) * 0.2);
        }

        double dX = (double) targetPos.getX() + 0.5 - client.player.getX() + (double) offsetX;
        double dY = (double) targetPos.getY() - 0.02 - (client.player.getY() + (double) client.player.getEyeHeight(client.player.getPose())) + (double) offsetY;
        double dZ = (double) targetPos.getZ() + 0.5 - client.player.getZ() + (double) offsetZ;

        double dist = Math.hypot(dX, dZ);

        float targetYaw = (float) (Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0f;
        float targetPitch = (float) (-(Math.atan2(dY, dist) * 180.0 / Math.PI));

        return new Rotation(
                client.player.yaw + normalizeYaw(targetYaw - client.player.yaw),
                client.player.pitch + normalizePitch(targetPitch - client.player.pitch)
        );
    }

    public static Rotation getRotationsSmart(Entity target, boolean raytrace) {
        Vec3d preferredAimPoint = WorldUtils.getBestVisiblePoint(target);

        if (raytrace && !WorldUtils.canSeeVector(preferredAimPoint)) {
            for (int verticalIndex = -1; verticalIndex < 2; verticalIndex++) {
                double yOffset = verticalIndex;

                if (verticalIndex != -1) {
                    yOffset *= target.getBoundingBox().getYLength();
                } else {
                    yOffset = target.getEyeHeight(target.getPose()) - 0.02;
                }

                double targetX = target.getX();
                double targetZ = target.getZ();
                double targetY = target.getY() + yOffset + 0.05;

                double dX = targetX - client.player.getX();
                double dZ = targetZ - client.player.getZ();
                double dY = targetY - (double) client.player.getEyeHeight(client.player.getPose()) - 0.02 - client.player.getY();

                double dist = Math.hypot(dX, dZ);

                float yaw = clamp(
                        client.player.yaw,
                        (float) (Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0f,
                        360f
                );
                float pitch = clamp(
                        client.player.pitch,
                        (float) (-(Math.atan2(dY, dist) * 180.0 / Math.PI)),
                        360.0f
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
                    dist = Math.hypot(dX, dZ);

                    yaw = clamp(
                            client.player.yaw,
                            (float) (Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0f,
                            360.0f
                    );
                    pitch = clamp(
                            client.player.pitch,
                            (float) (-(Math.atan2(dY, dist) * 180.0 / Math.PI)),
                            360.0f
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
        double dX = to.x - from.x;
        double dZ = to.z - from.z;
        double dY = to.y - from.y;

        double horizontalDistance = Math.hypot(dX, dZ);

        float yaw = clamp(
                0.0f,
                (float) (Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0f,
                360.0f
        );
        float pitch = clamp(
                client.player.pitch,
                (float) (-(Math.atan2(dY, horizontalDistance) * 180.0 / Math.PI)),
                360.0f
        );

        return new Rotation(yaw, pitch);
    }

    public static float getYawToEntityInterpolated(Entity target) {
        if (target == null || client.player == null) return 0.0f;

        double targetX = target.prevX + (target.getX() - target.prevX) * client.getTickDelta();
        double targetZ = target.prevZ + (target.getZ() - target.prevZ) * client.getTickDelta();

        double dX = targetX - client.player.getX();
        double dZ = targetZ - client.player.getZ();

        return (float) (Math.toDegrees(Math.atan2(dZ, dX)) - 90.0f);
    }

    public static float getAngleMetricToEntity(Entity target, float playerYaw) {
        float targetYaw = getYawToEntityInterpolated(target);
        return Math.abs(normalizeYaw(targetYaw - playerYaw));
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
        return MathHelper.clamp(pitch, -90.0f, 90.0f);
    }

    public static float clamp(float start, float end, float step) {
        float delta = normalizeYaw(end - start);

        if (delta > step) {
            delta = step;
        }

        if (delta < -step) {
            delta = -step;
        }

        return start + delta;
    }

    public static float getMovementDirectionYaw() {
        float forward = client.player.forwardSpeed;
        float strafe = client.player.sidewaysSpeed;
        float yaw = client.player.yaw + 90.0f;

        if (forward > 0.0f && client.options.keyBack.isPressed()) {
            forward = -1.0f;
        }

        if (strafe != 0.0f && strafe > 0.0f) {
            yaw -= 90.0f;
        } else if (strafe != 0.0f && strafe < 0.0f) {
            yaw += 90.0f;
        }

        if (forward != 0.0f) {
            if (strafe != 0.0f && strafe > 0.0f) {
                yaw -= (float) (!(forward > 0.0f) ? 45 : -45);
            } else if (strafe != 0.0f && strafe < 0.0f) {
                yaw -= (float) (!(forward > 0.0f) ? -45 : 45);
            }
        }

        if (forward < 0.0F && strafe == 0.0F) {
            yaw -= 180.0F;
        }

        return yaw;
    }

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

    public static float getDirection(float yaw) {
        float directionDegrees = 0.0F;

        float strafe = client.player.sidewaysSpeed;
        float forward = client.player.forwardSpeed;

        if (!(strafe > 0.0F)) {
            if (strafe < 0.0F) {
                if (!(forward > 0.0F)) {
                    if (!(forward < 0.0F)) {
                        yaw += 90.0F;
                    } else {
                        yaw -= 45.0F;
                    }
                } else {
                    yaw += 45.0F;
                }
            }
        } else if (!(forward > 0.0F)) {
            if (!(forward < 0.0F)) {
                yaw -= 90.0F;
            } else {
                yaw += 45.0F;
            }
        } else {
            yaw -= 45.0F;
        }

        if (yaw >= 45.0F && yaw <= 135.0F) {
            directionDegrees = 90.0F;
        } else if (yaw >= 135.0F || yaw <= -135.0F) {
            directionDegrees = 180.0F;
        } else if (yaw <= -45.0F && yaw >= -135.0F) {
            directionDegrees = -90.0F;
        } else if (yaw >= -45.0F && yaw <= 45.0F) {
            directionDegrees = 0.0F;
        }

        if (forward < 0.0F) {
            directionDegrees -= 180.0F;
        }

        return directionDegrees + 90.0F;
    }

    public static float[] getDirectionArray() {
        Input input = client.player.input;
        return getDirectionArray(input.movementForward, input.movementSideways);
    }

    public static float[] getDirectionArray(float forward, float strafe) {
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

    public static float getWrappedAngleDifference(float angleA, float angleB) {
        float difference = Math.abs(angleA - angleB) % 360.0F;
        return difference > 180.0F ? 360.0F - difference : difference;
    }

    public static Rotation getMovementDirectionBlockRotations() {
        BlockHitResult result = RaytraceUtils.rayTraceBlocksFromFeetYaw(RotationUtils.getMovementDirectionYaw() - 270.0F);

        if (result.getType() == HitResult.Type.MISS) {
            return null;
        }

        double targetX = result.getPos().x - result.getPos().getX();
        double targetY = result.getPos().y - result.getPos().getY();
        double targetZ = result.getPos().z - result.getPos().getZ();

        double dX = result.getPos().getX() - client.player.getX() + targetX;
        double dY = result.getPos().getY() - (client.player.getY() + (double) client.player.getEyeHeight(client.player.getPose())) + targetY;
        double dZ = result.getPos().getZ() - client.player.getZ() + targetZ;

        double dist = Math.hypot(dX, dZ);

        float targetYaw =
                (float) (Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0F;

        float targetPitch =
                (float) (-(Math.atan2(dY, dist) * 180.0 / Math.PI));

        return new Rotation(client.player.yaw + normalizeYaw(targetYaw - client.player.yaw), client.player.pitch + normalizePitch(targetPitch - client.player.pitch));
    }

    public static Rotation getBlockPlacementRotations(BlockPos blockPos, Direction face) {
        float offsetX = 0.0F;
        float offsetZ = 0.0F;
        float offsetY = 0.0F;

        switch (face) {
            case EAST:
                offsetX += 0.49F;
                break;
            case NORTH:
                offsetZ -= 0.49F;
                break;
            case SOUTH:
                offsetZ += 0.49F;
                break;
            case WEST:
                offsetX -= 0.49F;
                break;
            case UP:
                offsetY += 0.0F;
                break;
            case DOWN:
                offsetY++;
                break;
        }

        double dX = (double) blockPos.getX() + 0.5 - client.player.getX() + (double) offsetX;
        double dY = (double) blockPos.getY() - 0.02 - (client.player.getY() + (double) client.player.getEyeHeight(client.player.getPose())) + (double) offsetY;
        double dZ = (double) blockPos.getZ() + 0.5 - client.player.getZ() + (double) offsetZ;

        double dist = Math.hypot(dX, dZ);

        float yaw = (float) (Math.atan2(dZ, dX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(dY, dist) * 180.0 / Math.PI));

        return new Rotation(
                client.player.yaw + normalizeYaw(yaw - client.player.yaw),
                client.player.pitch + normalizePitch(pitch - client.player.pitch)
        );
    }
}
