package io.github.sst.remake.util.game.world;

import com.google.common.collect.ImmutableList;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.game.world.data.PlacementPattern;
import io.github.sst.remake.util.game.world.data.PositionFacing;
import io.github.sst.remake.util.java.ListUtils;
import io.github.sst.remake.util.java.RandomUtils;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        StateManager<Block, BlockState> stateManager = block.getStateManager();
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
        if (placePath.isEmpty()) {
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

    public static @NotNull List<PositionFacing> findPlacementPath(Block expectedBlock, BlockPos startPos, int maxDepth) {
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

                if (hasAnySolidNeighbor(ListUtils.reverse(childPath))) {
                    path.addAll(childPath);
                    return path.size() <= 1 ? path : ListUtils.reverse(path);
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

    public static double[] getSafeExtendedXZ(double extend) {
        double startX = client.player.getX();
        double startZ = client.player.getZ();

        double forward = client.player.input.movementForward;
        double strafe = client.player.input.movementSideways;
        float yawDegrees = client.player.yaw;

        BlockPos belowCandidate = new BlockPos(startX, client.player.getY() - 1.0, startZ);

        double candidateX = startX;
        double candidateZ = startZ;

        double stepIndex = 0.0;
        double maxSteps = extend * 2.0F;

        for (; isValidBlockPosition(belowCandidate);
             belowCandidate = new BlockPos(candidateX, client.player.getY() - 1.0, candidateZ)) {

            if (++stepIndex > maxSteps) {
                stepIndex = maxSteps;
            }

            double yawRad = Math.toRadians(yawDegrees + 90.0F);

            candidateX = startX + (forward * 0.45 * Math.cos(yawRad) + strafe * 0.45 * Math.sin(yawRad)) * stepIndex;
            candidateZ = startZ + (forward * 0.45 * Math.sin(yawRad) - strafe * 0.45 * Math.cos(yawRad)) * stepIndex;

            if (stepIndex == maxSteps) {
                break;
            }
        }

        return new double[]{candidateX, candidateZ};
    }

    public static PositionFacing findPlaceableNeighbor(BlockPos pos, boolean disallowDownFace) {
        Vec3i[] baseOffsets = new Vec3i[]{
                new Vec3i(0, 0, 0), new Vec3i(-1, 0, 0),
                new Vec3i(1, 0, 0), new Vec3i(0, 0, 1),
                new Vec3i(0, 0, -1)
        };
        PlacementPattern[] searchPatterns = new PlacementPattern[]{
                new PlacementPattern(1, 1, 1, false),
                new PlacementPattern(2, 1, 2, false),
                new PlacementPattern(3, 1, 3, false),
                new PlacementPattern(4, 1, 4, false),
                new PlacementPattern(0, -1, 0, true)
        };

        for (PlacementPattern pattern : searchPatterns) {
            for (Vec3i baseOffset : baseOffsets) {
                Vec3i candidateOffset = !pattern.isAdditive
                        ? new Vec3i(baseOffset.getX() * pattern.offsetX, baseOffset.getY() * pattern.offsetY, baseOffset.getZ() * pattern.offsetZ)
                        : new Vec3i(baseOffset.getX() + pattern.offsetX, baseOffset.getY() + pattern.offsetY, baseOffset.getZ() + pattern.offsetZ);

                for (Direction face : Direction.values()) {
                    if ((face != Direction.DOWN || !disallowDownFace) && isValidBlockPosition(pos.add(candidateOffset).offset(face, -1))) {
                        return new PositionFacing(pos.add(candidateOffset).offset(face, -1), face);
                    }
                }
            }
        }

        return null;
    }

    public static Vec3d getRandomizedHitVec(BlockPos blockPos, Direction side) {
        double x = (double) blockPos.getX() + 0.5;
        double y = (double) blockPos.getY() + 0.5;
        double z = (double) blockPos.getZ() + 0.5;

        x += (double) side.getOffsetX() / 2.0;
        y += (double) side.getOffsetY() / 2.0;
        z += (double) side.getOffsetZ() / 2.0;

        double jitter = 0.2;

        if (side != Direction.UP && side != Direction.DOWN) {
            y += RandomUtils.randomInRange(jitter, -jitter);
        } else {
            x += RandomUtils.randomInRange(jitter, -jitter);
            z += RandomUtils.randomInRange(jitter, -jitter);
        }

        if (side == Direction.WEST || side == Direction.EAST) {
            z += RandomUtils.randomInRange(jitter, -jitter);
        }

        if (side == Direction.SOUTH || side == Direction.NORTH) {
            x += RandomUtils.randomInRange(jitter, -jitter);
        }

        return new Vec3d(x, y, z);
    }
}