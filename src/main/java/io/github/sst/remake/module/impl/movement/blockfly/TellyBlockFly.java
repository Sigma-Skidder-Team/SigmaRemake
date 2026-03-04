package io.github.sst.remake.module.impl.movement.blockfly;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.data.rotation.Rotatable;
import io.github.sst.remake.data.rotation.Rotation;
import io.github.sst.remake.event.impl.client.ActionEvent;
import io.github.sst.remake.event.impl.game.player.JumpEvent;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.event.impl.game.player.SafeWalkEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.module.impl.movement.BlockFlyModule;
import io.github.sst.remake.module.impl.movement.SafeWalkModule;
import io.github.sst.remake.util.game.MovementUtils;
import io.github.sst.remake.util.game.RotationUtils;
import io.github.sst.remake.util.game.world.BlockUtils;
import io.github.sst.remake.util.game.world.RaytraceUtils;
import io.github.sst.remake.util.game.world.data.PositionFacing;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings({"DataFlowIssue", "unused"})
public class TellyBlockFly extends SubModule implements Rotatable {
    private static final float NO_ROTATION_SENTINEL = 999.0f;

    private float targetYaw;
    private float targetPitch;

    private PositionFacing pendingPlace;
    private int originalHotbarSlot = -1;
    private boolean allowJumpCancel;
    private double lockedY;

    public TellyBlockFly() {
        super("Telly");
        registerRotatable();
    }

    @Override
    public BlockFlyModule getParent() {
        return (BlockFlyModule) super.getParent();
    }

    @Override
    public void onEnable() {
        if (client.player == null) return;

        originalHotbarSlot = client.player.getInventory().selectedSlot;
        targetYaw = targetPitch = NO_ROTATION_SENTINEL;
        getParent().lastSpoofedSlot = -1;

        lockedY = -1.0;
        allowJumpCancel = false;
        if (client.player.isOnGround()) {
            lockedY = client.player.getY();
        }
    }

    @Override
    public void onDisable() {
        if (client.player == null) return;

        getParent().handleDisableSlotSpoof(originalHotbarSlot);
        originalHotbarSlot = -1;
        pendingPlace = null;
        targetYaw = NO_ROTATION_SENTINEL;
        targetPitch = NO_ROTATION_SENTINEL;

        MovementUtils.strafe(MovementUtils.getSpeed() * 0.9);
        setTimer(1.0f);
        client.options.keySneak.setPressed(false);
    }

    @Subscribe
    public void onSafeWalk(SafeWalkEvent event) {
        if (client.player == null) return;

        if (client.player.isOnGround()
                && Client.INSTANCE.moduleManager.getModule(SafeWalkModule.class).isEnabled()) {
            event.setSafe(true);
        }
    }

    @Subscribe(priority = Priority.HIGH)
    public void onMove(MoveEvent event) {
        if (client.player.isOnGround() && MovementUtils.isMoving() && !client.player.isSneaking()) {
            allowJumpCancel = false;
            client.player.jump();
            allowJumpCancel = true;
            event.setY(client.player.getVelocity().y);
            event.setX(client.player.getVelocity().x);
            event.setZ(client.player.getVelocity().z);
        }

        getParent().performTowering(event);
    }

    @Subscribe
    public void onJump(JumpEvent event) {
        if (event.entity != client.player) return;
        if (!allowJumpCancel) return;

        if (getParent().towerMode.value.equals("Vanilla")
                && (!MovementUtils.isMoving() || getParent().moveAndTower.value)) {
            event.cancel();
        }
    }

    @Subscribe(priority = Priority.LOW)
    public void onMotion(MotionEvent event) {
        if (client.player == null) return;
        if (getParent().countPlaceableBlocks() == 0) return;

        if (event.isPre()) {
            getParent().refillHotbarWithBlocks();
            if (getParent().countPlaceableBlocks() == 0) {
                pendingPlace = null;
                targetYaw = NO_ROTATION_SENTINEL;
                targetPitch = NO_ROTATION_SENTINEL;
            }
        }
    }

    @Subscribe
    public void onAction(ActionEvent event) {
        if (!getParent().isEnabled()) return;
        if (!canPerform()) return;
        if (client.player == null) return;
        if (pendingPlace == null) return;
        if (targetYaw == NO_ROTATION_SENTINEL) return;
        if (getParent().countPlaceableBlocks() == 0) return;

        placeWithDoItemUse();
        pendingPlace = null;
    }

    @Override
    public int getPriority() {
        return 80;
    }

    public boolean needsToRotate() {
        return client.player != null && !client.player.isOnGround();
    }

    @Override
    public Rotation getRotations() {
        if (client.player == null) return null;
        if (!needsToRotate()) return null;
        if (getParent().countPlaceableBlocks() == 0) {
            pendingPlace = null;
            targetYaw = NO_ROTATION_SENTINEL;
            targetPitch = NO_ROTATION_SENTINEL;
            return null;
        }

        updateTarget();

        if (pendingPlace != null) {
            Rotation rotations = RotationUtils.getBlockPlacementRotations(pendingPlace.blockPos, pendingPlace.direction);
            targetYaw = rotations.yaw;
            targetPitch = rotations.pitch;
        }

        if (targetYaw == NO_ROTATION_SENTINEL) {
            return null;
        }

        return new Rotation(targetYaw, targetPitch);
    }

    private void updateTarget() {
        double targetX = client.player.getX();
        double targetZ = client.player.getZ();
        double targetY = client.player.getY();

        if (client.player.getVelocity().y < 0.0
                && client.player.fallDistance > 1.0f
                && RaytraceUtils.rayTrace(0.0f, 90.0f, 3.0f).getType() == HitResult.Type.MISS) {
            targetY += Math.min(client.player.getVelocity().y * 2.0, 4.0);
        } else if (!client.options.keyJump.isPressed()) {
            targetY = lockedY;
        }

        if (!BlockUtils.isValidBlockPosition(
                new BlockPos(
                        client.player.getPos().getX(),
                        client.player.getPos().getY() - 1.0,
                        client.player.getPos().getZ()
                )
        )) {
            targetX = client.player.getPos().getX();
            targetZ = client.player.getPos().getZ();
        }

        BlockPos belowTarget = new BlockPos(targetX, targetY - 1.0, targetZ);

        if (!BlockUtils.isValidBlockPosition(belowTarget) && getParent().canPlaceWithHand(Hand.MAIN_HAND)) {
            pendingPlace = BlockUtils.findPlaceableNeighbor(belowTarget, false);
        } else {
            pendingPlace = null;
        }
    }

    private boolean isOnEdge() {
        double x = client.player.getX();
        double y = client.player.getY() - 0.1;
        double z = client.player.getZ();

        double[][] offsets = {
                {0.3, 0.3},
                {-0.3, 0.3},
                {0.3, -0.3},
                {-0.3, -0.3}
        };

        for (double[] offset : offsets) {
            int blockX = (int) Math.floor(x + offset[0]);
            int blockY = (int) Math.floor(y);
            int blockZ = (int) Math.floor(z + offset[1]);

            BlockPos pos = new BlockPos(blockX, blockY, blockZ);
            if (client.world.getBlockState(pos).getBlock() == Blocks.AIR) {
                return true;
            }
        }

        return false;
    }

    private void placeWithDoItemUse() {
        int previousSlot = client.player.getInventory().selectedSlot;

        if (!getParent().itemSpoofMode.value.equals("None")) {
            getParent().selectPlaceableHotbarSlot();
        }

        client.doItemUse();

        String spoofMode = getParent().itemSpoofMode.value;
        if (spoofMode.equals("Spoof") || spoofMode.equals("LiteSpoof")) {
            client.player.getInventory().selectedSlot = previousSlot;
        }
    }
}
