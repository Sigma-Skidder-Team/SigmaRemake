package io.github.sst.remake.util.game.world;

import io.github.sst.remake.util.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("ALL")
public class EntityUtils implements IMinecraft {
    public static double calculateDistanceSquared(Entity entity) {
        Vec3d playerPos = getEntityPosition(client.player);
        Vec3d entityPos = getEntityPosition(entity);

        return distanceSq(
                playerPos.x, playerPos.y, playerPos.z,
                entityPos.x, entityPos.y, entityPos.z
        );
    }

    public static double calculateDistanceSquared(BlockPos blockPos) {
        Vec3d playerPos = getEntityPosition(client.player);

        return distanceSq(
                playerPos.x, playerPos.y, playerPos.z,
                blockPos.getX(), blockPos.getY(), blockPos.getZ()
        );
    }

    public static Vec3d getEntityPosition(Entity entity) {
        double partial = (double) client.getTickDelta();

        double x = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * partial;
        double y = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * partial;
        double z = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * partial;

        return new Vec3d(x, y, z);
    }

    public static Vec3d getRelativePosition(Entity entity) {
        Vec3d entityPos = getEntityPosition(entity);
        Vec3d camera = client.gameRenderer.getCamera().getPos();

        return new Vec3d(
                entityPos.x - camera.x,
                entityPos.y - camera.y,
                entityPos.z - camera.z
        );
    }

    public static Vec3d getRelativePosition(BlockPos blockPos) {
        Vec3d camera = client.gameRenderer.getCamera().getPos();

        return new Vec3d(
                blockPos.getX() - camera.x,
                blockPos.getY() - camera.y,
                blockPos.getZ() - camera.z
        );
    }

    private static double distanceSq(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;

        return dx * dx + dy * dy + dz * dz;
    }
}
