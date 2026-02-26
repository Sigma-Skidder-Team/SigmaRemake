package io.github.sst.remake.util.game;

import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.game.world.BlockUtils;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;

import java.util.stream.Stream;

public class WorldUtils implements IMinecraft {

    static {
        BlockUtils.INVALID_BLOCKS.add(Blocks.AIR);
        BlockUtils.INVALID_BLOCKS.add(Blocks.WATER);
        BlockUtils.INVALID_BLOCKS.add(Blocks.LAVA);
        BlockUtils.INVALID_BLOCKS.add(Blocks.ENCHANTING_TABLE);
        BlockUtils.INVALID_BLOCKS.add(Blocks.BLACK_CARPET);
        BlockUtils.INVALID_BLOCKS.add(Blocks.GLASS_PANE);
        BlockUtils.INVALID_BLOCKS.add(Blocks.IRON_BARS);
        BlockUtils.INVALID_BLOCKS.add(Blocks.ICE);
        BlockUtils.INVALID_BLOCKS.add(Blocks.PACKED_ICE);
        BlockUtils.INVALID_BLOCKS.add(Blocks.CHEST);
        BlockUtils.INVALID_BLOCKS.add(Blocks.TRAPPED_CHEST);
        BlockUtils.INVALID_BLOCKS.add(Blocks.TORCH);
        BlockUtils.INVALID_BLOCKS.add(Blocks.ANVIL);
        BlockUtils.INVALID_BLOCKS.add(Blocks.NOTE_BLOCK);
        BlockUtils.INVALID_BLOCKS.add(Blocks.JUKEBOX);
        BlockUtils.INVALID_BLOCKS.add(Blocks.TNT);
        BlockUtils.INVALID_BLOCKS.add(Blocks.REDSTONE_WIRE);
        BlockUtils.INVALID_BLOCKS.add(Blocks.LEVER);
        BlockUtils.INVALID_BLOCKS.add(Blocks.COBBLESTONE_WALL);
        BlockUtils.INVALID_BLOCKS.add(Blocks.OAK_FENCE);
        BlockUtils.INVALID_BLOCKS.add(Blocks.TALL_GRASS);
        BlockUtils.INVALID_BLOCKS.add(Blocks.TRIPWIRE);
        BlockUtils.INVALID_BLOCKS.add(Blocks.TRIPWIRE_HOOK);
        BlockUtils.INVALID_BLOCKS.add(Blocks.RAIL);
        BlockUtils.INVALID_BLOCKS.add(Blocks.LILY_PAD);
        BlockUtils.INVALID_BLOCKS.add(Blocks.RED_MUSHROOM);
        BlockUtils.INVALID_BLOCKS.add(Blocks.BROWN_MUSHROOM);
        BlockUtils.INVALID_BLOCKS.add(Blocks.VINE);
        BlockUtils.INVALID_BLOCKS.add(Blocks.ACACIA_TRAPDOOR);
        BlockUtils.INVALID_BLOCKS.add(Blocks.LADDER);
        BlockUtils.INVALID_BLOCKS.add(Blocks.FURNACE);
        BlockUtils.INVALID_BLOCKS.add(Blocks.SAND);
        BlockUtils.INVALID_BLOCKS.add(Blocks.CACTUS);
        BlockUtils.INVALID_BLOCKS.add(Blocks.DISPENSER);
        BlockUtils.INVALID_BLOCKS.add(Blocks.DROPPER);
        BlockUtils.INVALID_BLOCKS.add(Blocks.CRAFTING_TABLE);
        BlockUtils.INVALID_BLOCKS.add(Blocks.COBWEB);
        BlockUtils.INVALID_BLOCKS.add(Blocks.PUMPKIN);
        BlockUtils.INVALID_BLOCKS.add(Blocks.ACACIA_SAPLING);
    }

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

    public static boolean isAboveBounds(Entity entity, float yBounds) {
        Box bounds = new Box(
                entity.getBoundingBox().minX,
                entity.getBoundingBox().minY - (double) yBounds,
                entity.getBoundingBox().minZ,
                entity.getBoundingBox().maxX,
                entity.getBoundingBox().maxY,
                entity.getBoundingBox().maxZ
        );
        Stream<VoxelShape> shapes = client.world.getBlockCollisions(client.player, bounds);

        return shapes.findAny().isPresent();
    }
}