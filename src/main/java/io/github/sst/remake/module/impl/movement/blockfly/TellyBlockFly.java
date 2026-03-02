package io.github.sst.remake.module.impl.movement.blockfly;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.data.rotation.Rotatable;
import io.github.sst.remake.data.rotation.Rotation;
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
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

// TODO: why does it not stay on the same Y position?
// TODO: all block fly modes seem to flag air place when towering? and movement correction is broken sometimes ig.
public class TellyBlockFly extends SubModule implements Rotatable {
    private static final float NO_ROTATION_SENTINEL = 999.0f;

    private float targetYaw;
    private float targetPitch;
    private PositionFacing pendingPlace;
    private int originalHotbarSlot = -1;
    private int groundTicksSinceLeave;
    private Hand placeHand;
    private double lockedY;

    public TellyBlockFly() {
        super("(Broken) Telly");
        registerRotatable();
    }

    @Override
    public BlockFlyModule getParent() {
        return (BlockFlyModule) super.getParent();
    }

    @Override
    public void onEnable() {
        if (client.player == null) return;

        originalHotbarSlot = client.player.inventory.selectedSlot;

        targetYaw = NO_ROTATION_SENTINEL;
        targetPitch = NO_ROTATION_SENTINEL;

        getParent().lastSpoofedSlot = -1;

        lockedY = -1;

        if (client.player.isOnGround()) {
            lockedY = client.player.getBlockPos().getY();
        }

        groundTicksSinceLeave = -1;
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
    public void onJump(JumpEvent event) {
        if (event.entity != client.player) return;

        if (getParent().towerMode.value.equals("Vanilla") && (!MovementUtils.isMoving() || getParent().moveAndTower.value)) {
            event.cancel();
        }
    }

    @Subscribe
    public void onSafeWalk(SafeWalkEvent event) {
        assert client.player != null;
        if (client.player.isOnGround() && Client.INSTANCE.moduleManager.getModule(SafeWalkModule.class).isEnabled()) {
            event.setSafe(true);
        }
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (getParent().countPlaceableBlocks() == 0) return;

        assert client.player != null;
        if (client.player.isOnGround() && MovementUtils.isMoving() && !client.player.isSneaking()) {
            client.player.jump();

            event.setY(client.player.getVelocity().y);
            event.setX(client.player.getVelocity().x);
            event.setZ(client.player.getVelocity().z);
        }

        if (client.player.isOnGround()) {
            lockedY = client.player.getBlockPos().getY();
        }

        if (client.player.isOnGround()) {
            groundTicksSinceLeave = 0;
        } else if (groundTicksSinceLeave >= 0) {
            groundTicksSinceLeave++;
        }

        getParent().performTowering(event);
    }

    @Subscribe
    public void onMotion(MotionEvent event) {
        if (!getParent().isEnabled()) return;
        if (getParent().countPlaceableBlocks() == 0) return;

        if (event.isPre()) {
            handlePlace(event);
        }
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
        if (!getParent().isEnabled()) {
            pendingPlace = null;
            targetYaw = NO_ROTATION_SENTINEL;
            targetPitch = NO_ROTATION_SENTINEL;
            return null;
        }

        if (!needsToRotate()) {
            return null;
        }

        if (getParent().countPlaceableBlocks() == 0) {
            pendingPlace = null;
            targetYaw = NO_ROTATION_SENTINEL;
            targetPitch = NO_ROTATION_SENTINEL;
            return null;
        }

        updateTarget();

        if (pendingPlace != null) {
            Rotation rotations = RotationUtils.getMovementDirectionBlockRotations();
            if (rotations != null) {
                targetYaw = rotations.yaw;
                targetPitch = rotations.pitch + 0.067f;
            }
        }

        if (targetYaw == NO_ROTATION_SENTINEL) return null;

        assert client.player != null;

        return new Rotation(targetYaw, targetPitch);
    }

    private void handlePlace(MotionEvent event) {
        if (!canPerform()) return;
        if (targetYaw == NO_ROTATION_SENTINEL) return;

        getParent().refillHotbarWithBlocks();

        if (pendingPlace == null) return;

        BlockHitResult hit = RaytraceUtils.rayTraceBlocksFromRotations(targetYaw, targetPitch, 5.0f, event);
        if (hit.getType() == HitResult.Type.MISS) {
            return;
        }

        if (hit.getSide() == Direction.UP) {
            assert client.player != null;
            if (hit.getBlockPos().getY() <= client.player.getBlockPos().getY() - 1.0
                    && client.player.isOnGround()) {
                return;
            }
        }

        assert client.player != null;

        getParent().interactBlockWithSpoofing(placeHand, hit);

        pendingPlace = null;

        client.player.swingHand(Hand.MAIN_HAND);
    }

    private void updateTarget() {

        placeHand = Hand.MAIN_HAND;
        assert client.player != null;
        if (BlockUtils.isPlacableBlockItem(client.player.getStackInHand(Hand.OFF_HAND).getItem())
                && (client.player.getStackInHand(placeHand).isEmpty()
                || !BlockUtils.isPlacableBlockItem(client.player.getStackInHand(placeHand).getItem()))) {
            placeHand = Hand.OFF_HAND;
        }

        final BlockPos bp = client.player.getBlockPos();

        double targetX = bp.getX();
        double targetZ = bp.getZ();
        double targetY = bp.getY();

        if (!client.player.horizontalCollision && !client.options.keyJump.isPressed()) {
            double[] extended = BlockUtils.getSafeExtendedXZ(getParent().extend.value);
            targetX = extended[0];
            targetZ = extended[1];
        }

        if (!client.options.keyJump.isPressed()) {
            targetY = lockedY;
        }

        BlockPos belowTarget = new BlockPos(targetX, targetY, targetZ);
        if (!BlockUtils.isValidBlockPosition(belowTarget)
                && getParent().canPlaceWithHand(placeHand)) {

            pendingPlace = BlockUtils.findPlaceableNeighbor(belowTarget, false);
        } else {
            pendingPlace = null;
        }
    }
}
