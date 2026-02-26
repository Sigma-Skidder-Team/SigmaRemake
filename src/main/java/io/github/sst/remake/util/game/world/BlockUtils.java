package io.github.sst.remake.util.game.world;

import com.google.common.collect.ImmutableList;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.game.world.data.PositionFacing;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockUtils implements IMinecraft {
    public static final List<Block> INVALID_BLOCKS = new ArrayList<>();

    public static Block getBlockAt(BlockPos pos) {
        return client.world.getBlockState(pos).getBlock();
    }

    public static boolean isValidBlockPosition(BlockPos pos) {
        if (pos == null) {
            return false;
        }

        Block block = client.world.getBlockState(pos).getBlock();

        if (!block.getDefaultState().isOpaque() && block.getDefaultState().getMaterial().isReplaceable()) {
            return false;
        }

        return !(block instanceof SnowBlock) || getBlockStateIndex(client.world.getBlockState(pos)) != 0;
    }

    public static int getBlockStateIndex(BlockState state) {
        Block block = state.getBlock();
        StateManager stateManager = block.getStateManager();
        ImmutableList<?> states = stateManager.getStates();
        return states.indexOf(state);
    }

    public static boolean isBlockedPlacementPosition(Block block, BlockPos pos) {
        VoxelShape collisionShape = block.getDefaultState().getCollisionShape(client.world, pos);
        return !isValidBlockPosition(pos)
                && client.world.intersectsEntities(client.player, collisionShape)
                && pos.getY() <= client.player.getY();
    }

    public static boolean hasAnySolidNeighbor(List<PositionFacing> placePath) {
        if (placePath.size() <= 0) {
            return false;
        }

        BlockPos basePos = placePath.get(0).blockPos;

        PositionFacing[] neighbors = new PositionFacing[]{
                new PositionFacing(basePos.north(), Direction.SOUTH),
                new PositionFacing(basePos.east(), Direction.WEST),
                new PositionFacing(basePos.south(), Direction.NORTH),
                new PositionFacing(basePos.west(), Direction.EAST),
                new PositionFacing(basePos.down(), Direction.UP),
                new PositionFacing(basePos.up(), Direction.DOWN)
        };

        for (PositionFacing neighbor : neighbors) {
            if (getBlockAt(neighbor.blockPos) != Blocks.AIR) {
                return true;
            }
        }

        return false;
    }

    public static List<PositionFacing> reversePath(List<PositionFacing> path) {
        List<PositionFacing> reversed = new ArrayList<>();
        for (int i = path.size() - 1; i >= 0; i--) {
            reversed.add(path.get(i));
        }

        return reversed;
    }

    public static List<PositionFacing> findPlacementPath(Block expectedBlock, BlockPos startPos, int maxDepth) {
        List<PositionFacing> path = new ArrayList<>();

        if (startPos == null || maxDepth < 0) {
            return path;
        }

        if (!isBlockedPlacementPosition(expectedBlock, startPos)) {
            return path;
        }

        PositionFacing[] candidates = new PositionFacing[]{
                new PositionFacing(startPos.up(), Direction.DOWN),
                new PositionFacing(startPos.north(), Direction.SOUTH),
                new PositionFacing(startPos.east(), Direction.WEST),
                new PositionFacing(startPos.south(), Direction.NORTH),
                new PositionFacing(startPos.west(), Direction.EAST),
                new PositionFacing(startPos.down(), Direction.UP)
        };

        for (PositionFacing candidate : candidates) {
            if (!isBlockedPlacementPosition(expectedBlock, candidate.blockPos)) {
                path.add(candidate);
                return path;
            }
        }

        for (int depth = 1; depth < maxDepth; depth++) {
            for (PositionFacing candidate : candidates) {
                List<PositionFacing> childPath = findPlacementPath(expectedBlock, candidate.blockPos, depth);

                if (hasAnySolidNeighbor(reversePath(childPath))) {
                    path.addAll(childPath);
                    return path.size() <= 1 ? path : reversePath(path);
                }
            }
        }

        return path;
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