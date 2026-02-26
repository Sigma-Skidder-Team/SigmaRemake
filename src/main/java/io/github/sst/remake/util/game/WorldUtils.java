package io.github.sst.remake.util.game;

import io.github.sst.remake.util.IMinecraft;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WorldUtils implements IMinecraft {
    public static final List<Block> INVALID_BLOCKS = new ArrayList<>();

    static {
        INVALID_BLOCKS.add(Blocks.AIR);
        INVALID_BLOCKS.add(Blocks.WATER);
        INVALID_BLOCKS.add(Blocks.LAVA);
        INVALID_BLOCKS.add(Blocks.ENCHANTING_TABLE);
        INVALID_BLOCKS.add(Blocks.BLACK_CARPET);
        INVALID_BLOCKS.add(Blocks.GLASS_PANE);
        INVALID_BLOCKS.add(Blocks.IRON_BARS);
        INVALID_BLOCKS.add(Blocks.ICE);
        INVALID_BLOCKS.add(Blocks.PACKED_ICE);
        INVALID_BLOCKS.add(Blocks.CHEST);
        INVALID_BLOCKS.add(Blocks.TRAPPED_CHEST);
        INVALID_BLOCKS.add(Blocks.TORCH);
        INVALID_BLOCKS.add(Blocks.ANVIL);
        INVALID_BLOCKS.add(Blocks.NOTE_BLOCK);
        INVALID_BLOCKS.add(Blocks.JUKEBOX);
        INVALID_BLOCKS.add(Blocks.TNT);
        INVALID_BLOCKS.add(Blocks.REDSTONE_WIRE);
        INVALID_BLOCKS.add(Blocks.LEVER);
        INVALID_BLOCKS.add(Blocks.COBBLESTONE_WALL);
        INVALID_BLOCKS.add(Blocks.OAK_FENCE);
        INVALID_BLOCKS.add(Blocks.TALL_GRASS);
        INVALID_BLOCKS.add(Blocks.TRIPWIRE);
        INVALID_BLOCKS.add(Blocks.TRIPWIRE_HOOK);
        INVALID_BLOCKS.add(Blocks.RAIL);
        INVALID_BLOCKS.add(Blocks.LILY_PAD);
        INVALID_BLOCKS.add(Blocks.RED_MUSHROOM);
        INVALID_BLOCKS.add(Blocks.BROWN_MUSHROOM);
        INVALID_BLOCKS.add(Blocks.VINE);
        INVALID_BLOCKS.add(Blocks.ACACIA_TRAPDOOR);
        INVALID_BLOCKS.add(Blocks.LADDER);
        INVALID_BLOCKS.add(Blocks.FURNACE);
        INVALID_BLOCKS.add(Blocks.SAND);
        INVALID_BLOCKS.add(Blocks.CACTUS);
        INVALID_BLOCKS.add(Blocks.DISPENSER);
        INVALID_BLOCKS.add(Blocks.DROPPER);
        INVALID_BLOCKS.add(Blocks.CRAFTING_TABLE);
        INVALID_BLOCKS.add(Blocks.COBWEB);
        INVALID_BLOCKS.add(Blocks.PUMPKIN);
        INVALID_BLOCKS.add(Blocks.ACACIA_SAPLING);
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

    public static boolean isPlacableBlockItem(Item item) {
        if (!(item instanceof BlockItem)) {
            return false;
        }

        Block block = ((BlockItem) item).getBlock();
        if (INVALID_BLOCKS.contains(block)) {
            return false;
        }

        // Exclude block types that usually should not be used for automated placement.
        return !(block instanceof AbstractButtonBlock)
                && !(block instanceof PlantBlock)
                && !(block instanceof TrapdoorBlock)
                && !(block instanceof AbstractPressurePlateBlock)
                && !(block instanceof SandBlock)
                && !(block instanceof OreBlock)
                && !(block instanceof SkullBlock)
                && !(block instanceof BedBlock)
                && !(block instanceof BannerBlock)
                && !(block instanceof ChestBlock)
                && !(block instanceof DoorBlock);
    }
}