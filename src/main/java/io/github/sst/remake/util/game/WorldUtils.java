package io.github.sst.remake.util.game;

import io.github.sst.remake.util.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class WorldUtils implements IMinecraft {
    public static Vec3d getBestVisiblePoint(Entity target) {
        return getBestVisiblePoint(target.getBoundingBox());
    }

    public static Vec3d getBestVisiblePoint(Box bounds) {
        double centerX = bounds.getCenter().x;
        double minY = bounds.minY;
        double centerZ = bounds.getCenter().z;

        double usableHeight = (bounds.maxY - minY) * 0.95;
        double usableWidthX = (bounds.maxX - bounds.minX) * 0.95;
        double usableWidthZ = (bounds.maxZ - bounds.minZ) * 0.95;

        double eyeY = client.player.getY() + (double) client.player.getEyeHeight(client.player.getPose());

        double x = Math.max(centerX - usableWidthX / 2.0, Math.min(centerX + usableWidthX / 2.0, client.player.getX()));
        double y = Math.max(minY, Math.min(minY + usableHeight, eyeY));
        double z = Math.max(centerZ - usableWidthZ / 2.0, Math.min(centerZ + usableWidthZ / 2.0, client.player.getZ()));

        return new Vec3d(x, y, z);
    }

    public static boolean canSeeVector(Vec3d target) {
        Vec3d from = new Vec3d(
                client.player.getX(),
                client.player.getY() + (double) client.player.getEyeHeight(client.player.getPose()),
                client.player.getZ()
        );

        RaycastContext context = new RaycastContext(from, target, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, client.player);
        BlockHitResult result = client.world.raycast(context);

        return result.getType() == HitResult.Type.MISS || result.getType() == HitResult.Type.ENTITY;
    }
}
