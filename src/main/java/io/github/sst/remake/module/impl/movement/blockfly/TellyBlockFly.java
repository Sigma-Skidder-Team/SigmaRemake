package io.github.sst.remake.module.impl.movement.blockfly;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.ActionEvent;
import io.github.sst.remake.event.impl.game.player.*;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.module.impl.movement.BlockFlyModule;
import io.github.sst.remake.module.impl.movement.SafeWalkModule;
import io.github.sst.remake.util.game.player.MovementUtils;
import io.github.sst.remake.util.game.combat.data.Rotation;
import io.github.sst.remake.util.game.combat.RotationUtils;
import io.github.sst.remake.util.game.world.BlockUtils;
import io.github.sst.remake.util.game.world.RaytraceUtils;
import io.github.sst.remake.util.game.world.data.PositionFacing;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class TellyBlockFly extends SubModule {
    private static final float NO_ROTATION_SENTINEL = 999.0f;

    private float targetYaw;
    private float targetPitch;
    private PositionFacing pendingPlace;
    private int originalHotbarSlot = -1;
    private int groundTicksSinceLeave;
    private double lockedY;

    public TellyBlockFly() {
        super("Telly");
    }

    @Override
    public BlockFlyModule getParent() {
        return (BlockFlyModule) super.getParent();
    }

    @Override
    public void onEnable() {
        if (client.player == null) return;

        originalHotbarSlot = client.player.getInventory().selectedSlot;

        targetYaw = NO_ROTATION_SENTINEL;
        targetPitch = NO_ROTATION_SENTINEL;

        getParent().lastSpoofedSlot = -1;

        lockedY = -1.0;

        if (client.player.isOnGround()) {
            lockedY = client.player.getY();
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

        if (client.player.isOnGround()) lockedY = client.player.getY();

        if (client.player.isOnGround()) {
            groundTicksSinceLeave = 0;
        } else if (groundTicksSinceLeave >= 0) {
            groundTicksSinceLeave++;
        }

        getParent().performTowering(event);
    }

    @Subscribe
    public void onAction(ActionEvent event) {
        if (getParent().countPlaceableBlocks() == 0) return;

        handlePlace();
    }

    private boolean needsToRotate() {
        return client.player != null && !client.player.isOnGround();
    }

    @Subscribe
    public void onRotate(RotateEvent event) {
        if (!needsToRotate()) {
            return;
        }

        if (getParent().countPlaceableBlocks() == 0) {
            pendingPlace = null;
            targetYaw = NO_ROTATION_SENTINEL;
            targetPitch = NO_ROTATION_SENTINEL;
            return;
        }

        updateTarget();

        if (pendingPlace != null) {
            Rotation rotations = RotationUtils.getMovementDirectionBlockRotations();
            if (rotations != null) {
                targetYaw = rotations.yaw;
                targetPitch = rotations.pitch + 0.067f;
            }
        }

        if (targetYaw == NO_ROTATION_SENTINEL) return;

        event.yaw = targetYaw;
        event.pitch = targetPitch;
    }

    private void handlePlace() {
        if (targetYaw == NO_ROTATION_SENTINEL) return;

        getParent().refillHotbarWithBlocks();

        if (pendingPlace == null) return;

        BlockHitResult hit = RaytraceUtils.rayTrace(targetYaw, targetPitch, client.interactionManager.getReachDistance());
        if (hit == null
                || hit.getType() != HitResult.Type.BLOCK
                || !hit.getBlockPos().equals(pendingPlace.blockPos)
                || hit.getSide() != pendingPlace.direction) {
            hit = new BlockHitResult(
                    BlockUtils.getRandomizedHitVec(pendingPlace.blockPos, pendingPlace.direction),
                    pendingPlace.direction,
                    pendingPlace.blockPos,
                    false
            );
        }

        getParent().spoofAndPlace(hit);

        pendingPlace = null;
    }

    private void updateTarget() {
        Hand placeHand = Hand.MAIN_HAND;
        assert client.player != null;
        if (BlockUtils.isPlacableBlockItem(client.player.getStackInHand(Hand.OFF_HAND).getItem())
                && (client.player.getStackInHand(placeHand).isEmpty()
                || !BlockUtils.isPlacableBlockItem(client.player.getStackInHand(placeHand).getItem()))) {
            placeHand = Hand.OFF_HAND;
        }

        double targetX = client.player.getX();
        double targetZ = client.player.getZ();
        double targetY = client.player.getY();

        if (!client.options.keyJump.isPressed()) {
            targetY = lockedY;
        }

        BlockPos belowTarget = new BlockPos(targetX, targetY - 1.0, targetZ);
        if (!BlockUtils.isValidBlockPosition(belowTarget)
                && getParent().canPlaceWithHand(placeHand)) {

            pendingPlace = BlockUtils.findPlaceableNeighbor(belowTarget, false);
        } else {
            pendingPlace = null;
        }
    }
}