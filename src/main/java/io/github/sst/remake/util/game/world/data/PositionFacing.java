package io.github.sst.remake.util.game.world.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PositionFacing {
    public BlockPos blockPos;
    public Direction direction;

    public PositionFacing(BlockPos blockPos, Direction direction) {
        this.blockPos = blockPos;
        this.direction = direction;
    }
}
