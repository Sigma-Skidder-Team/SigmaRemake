package io.github.sst.remake.util.game.world;

import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.game.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class RaytraceUtils implements IMinecraft {
    public static BlockHitResult rayTraceBlocksFromLastTick(float yawDeg, float pitchDeg, float reach) {
        Vec3d start = new Vec3d(
                client.player.lastX,
                client.player.lastBaseY + (double) client.player.getEyeHeight(client.player.getPose()),
                client.player.lastZ
        );

        float yawRad = (float) Math.toRadians(yawDeg);
        float pitchRad = (float) Math.toRadians(pitchDeg);

        float dirX = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad);
        float dirY = -MathHelper.sin(pitchRad);
        float dirZ = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad);

        if (reach == 0.0F) {
            reach = client.interactionManager.getReachDistance();
        }

        Vec3d end = new Vec3d(
                client.player.lastX + (double) (dirX * reach),
                client.player.lastBaseY + (double) (dirY * reach) + (double) client.player.getEyeHeight(client.player.getPose()),
                client.player.lastZ + (double) (dirZ * reach)
        );

        Entity cameraEntity = client.getCameraEntity();
        return client.world.raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                cameraEntity
        ));
    }

    public static BlockHitResult rayTraceBlocksFromEye(float yawDeg, float pitchDeg, float reach, float forwardOffset) {
        double offsetX = Math.cos((double) RotationUtils.getMovementDirectionYaw() * Math.PI / 180.0) * (double) forwardOffset;
        double offsetZ = Math.sin((double) RotationUtils.getMovementDirectionYaw() * Math.PI / 180.0) * (double) forwardOffset;

        Vec3d start = new Vec3d(
                client.player.getX() + offsetX,
                client.player.getY() + (double) client.player.getEyeHeight(client.player.getPose()),
                client.player.getZ() + offsetZ
        );

        float yawRad = (float) Math.toRadians(yawDeg);
        float pitchRad = (float) Math.toRadians(pitchDeg);

        float dirX = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad);
        float dirY = -MathHelper.sin(pitchRad);
        float dirZ = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad);

        if (reach == 0.0f) {
            reach = client.interactionManager.getReachDistance();
        }

        Vec3d end = new Vec3d(
                client.player.lastX + (double) (dirX * reach),
                client.player.lastBaseY + (double) (dirY * reach) + (double) client.player.getEyeHeight(client.player.getPose()),
                client.player.lastZ + (double) (dirZ * reach)
        );

        Entity cameraEntity = client.getCameraEntity();
        return client.world.raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                cameraEntity
        ));
    }

}
