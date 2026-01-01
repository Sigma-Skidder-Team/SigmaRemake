package io.github.sst.remake.util.game;

import io.github.sst.remake.util.IMinecraft;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings("ALL")
public class EntityUtils implements IMinecraft {

    public static double calculateDistanceSquared(BlockPos blockPos) {
        double deltaX = getEntityPosition(client.player).x - (double) blockPos.getX();
        double deltaY = getEntityPosition(client.player).y - (double) blockPos.getY();
        double deltaZ = getEntityPosition(client.player).z - (double) blockPos.getZ();
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    public static Vector3d getEntityPosition(Entity entity) {
        return new Vector3d(
                entity.lastRenderX + (entity.getX() - entity.lastRenderX) * (double) client.renderTickCounter.tickDelta,
                entity.lastRenderY + (entity.getY() - entity.lastRenderY) * (double) client.renderTickCounter.tickDelta,
                entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * (double) client.renderTickCounter.tickDelta
        );
    }

}
