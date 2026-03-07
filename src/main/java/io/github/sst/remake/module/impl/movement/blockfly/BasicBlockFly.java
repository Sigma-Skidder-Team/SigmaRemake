package io.github.sst.remake.module.impl.movement.blockfly;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.ActionEvent;
import io.github.sst.remake.event.impl.game.player.*;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.module.impl.movement.BlockFlyModule;
import io.github.sst.remake.module.impl.movement.SafeWalkModule;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.util.game.player.MovementUtils;
import io.github.sst.remake.util.game.combat.data.Rotation;
import io.github.sst.remake.util.game.combat.RotationUtils;
import io.github.sst.remake.util.game.world.WorldUtils;
import io.github.sst.remake.util.game.world.BlockUtils;
import io.github.sst.remake.util.game.world.RaytraceUtils;
import io.github.sst.remake.util.game.world.data.PositionFacing;
import io.github.sst.remake.util.math.timer.BasicTimer;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings({"DataFlowIssue", "unused"})
public class BasicBlockFly extends SubModule  {
    private static final float NO_ROTATION_SENTINEL = 999.0f;

    public final ModeSetting movementMode = new ModeSetting("Movement mode", "Basic blockfly movement mode", 0, "None", "Jump", "Sneak", "Slow", "Eagle", "Vulcan");
    public final SliderSetting eagleSneakDelay = new SliderSetting("Eagle sneak delay", "Sneak delay while using eagle mode", 30.0f, 0.0f, 300.0f, 1.0f)
            .hide(() -> !movementMode.value.equals("Eagle"));

    private float targetYaw;
    private float targetPitch;

    private PositionFacing pendingPlace;
    private int originalHotbarSlot = -1;
    private boolean allowJumpCancel;
    private double lockedY;
    private final BasicTimer eagleTimer = new BasicTimer();
    private boolean eagleLastShouldSneak;
    private int vulcanBlocksWalked;
    private int vulcanSneakTicks;
    private int vulcanLastBlockX;
    private int vulcanLastBlockZ;
    private boolean vulcanHasLastPos;

    public BasicBlockFly() {
        super("Basic");
    }

    @Override
    public BlockFlyModule getParent() {
        return (BlockFlyModule) super.getParent();
    }

    @Override
    public void onEnable() {
        if (client.player == null) return;

        originalHotbarSlot = client.player.inventory.selectedSlot;
        targetYaw = targetPitch = NO_ROTATION_SENTINEL;
        getParent().lastSpoofedSlot = -1;

        lockedY = -1.0;
        allowJumpCancel = false;
        if (client.player.isOnGround()) {
            lockedY = client.player.getY();
        }
        eagleTimer.reset();
        eagleLastShouldSneak = false;
        resetVulcanState();
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
        eagleTimer.reset();
        eagleLastShouldSneak = false;
        resetVulcanState();
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
        if (client.player == null) return;
        if (getParent().countPlaceableBlocks() == 0) return;

        if (client.player.isOnGround() || WorldUtils.isAboveBounds(client.player, 0.01f)) {
            lockedY = client.player.getY();
        }

        if (getParent().noSprint.value) {
            client.player.setSprinting(false);
        }

        boolean autoSneak = false;
        boolean handledSneak = false;

        switch (movementMode.value) {
            case "Jump":
                if (client.player.isOnGround() && MovementUtils.isMoving() && !client.player.isSneaking()) {
                    allowJumpCancel = false;
                    client.player.jump();
                    allowJumpCancel = true;
                    event.setY(client.player.getVelocity().y);
                    event.setX(client.player.getVelocity().x);
                    event.setZ(client.player.getVelocity().z);
                }
                break;

            case "Slow":
                if (client.player.isOnGround()) {
                    event.setX(event.getX() * 0.75);
                    event.setZ(event.getZ() * 0.75);
                } else {
                    event.setX(event.getX() * 0.93);
                    event.setZ(event.getZ() * 0.93);
                }
                break;

            case "Sneak":
                autoSneak = true;
                break;

            case "Eagle":
                handledSneak = true;
                handleEagleSneak();
                break;

            case "Vulcan":
                handledSneak = true;
                handleVulcanSneak();
                break;
        }

        if (!handledSneak) {
            client.options.keySneak.setPressed(autoSneak);
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
        if (client.player == null) return;
        if (pendingPlace == null) return;
        if (targetYaw == NO_ROTATION_SENTINEL) return;
        if (getParent().countPlaceableBlocks() == 0) return;

        BlockHitResult hit = new BlockHitResult(
                BlockUtils.getRandomizedHitVec(pendingPlace.blockPos, pendingPlace.direction),
                pendingPlace.direction,
                pendingPlace.blockPos,
                false
        );

        getParent().interactBlockWithSpoofing(Hand.MAIN_HAND, hit);
        client.player.swingHand(Hand.MAIN_HAND);

        pendingPlace = null;
    }

    @Subscribe
    public void onRotate(RotateEvent event) {
        if (getParent().countPlaceableBlocks() == 0) {
            pendingPlace = null;
            targetYaw = NO_ROTATION_SENTINEL;
            targetPitch = NO_ROTATION_SENTINEL;
            return;
        }

        updateTarget();

        if (pendingPlace != null) {
            Rotation rotations = RotationUtils.getBlockPlacementRotations(pendingPlace.blockPos, pendingPlace.direction);
            targetYaw = rotations.yaw;
            targetPitch = rotations.pitch;
        }

        if (targetYaw == NO_ROTATION_SENTINEL) {
            return;
        }

        event.yaw = targetYaw;
        event.pitch = targetPitch;
    }

    private void updateTarget() {
        double targetX = client.player.getX();
        double targetZ = client.player.getZ();
        double targetY = client.player.getY();

        if (client.player.getVelocity().y < 0.0
                && client.player.fallDistance > 1.0f
                && RaytraceUtils.rayTrace(0.0f, 90.0f, 3.0f).getType() == HitResult.Type.MISS) {
            targetY += Math.min(client.player.getVelocity().y * 2.0, 4.0);
        } else if (movementMode.value.equals("Jump") && !client.options.keyJump.isPressed()) {
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

    private void handleEagleSneak() {
        boolean shouldSneak = client.player.isOnGround() && isOnEdge();
        if (shouldSneak != eagleLastShouldSneak) {
            eagleLastShouldSneak = shouldSneak;
            eagleTimer.reset();
        }

        if (shouldSneak) {
            client.options.keySneak.setPressed(true);
            return;
        }

        long delay = Math.max(0L, Math.round(eagleSneakDelay.value));
        if (eagleTimer.hasElapsed(delay)) {
            client.options.keySneak.setPressed(false);
        }
    }

    private void handleVulcanSneak() {
        int currentBlockX = (int) Math.floor(client.player.getX());
        int currentBlockZ = (int) Math.floor(client.player.getZ());

        if (!vulcanHasLastPos) {
            vulcanLastBlockX = currentBlockX;
            vulcanLastBlockZ = currentBlockZ;
            vulcanHasLastPos = true;
        }

        if (client.player.isOnGround()) {
            int deltaX = Math.abs(currentBlockX - vulcanLastBlockX);
            int deltaZ = Math.abs(currentBlockZ - vulcanLastBlockZ);
            int traversedBlocks = deltaX + deltaZ;

            if (traversedBlocks > 0) {
                vulcanBlocksWalked += traversedBlocks;
                vulcanLastBlockX = currentBlockX;
                vulcanLastBlockZ = currentBlockZ;

                if (vulcanBlocksWalked >= 7) {
                    vulcanBlocksWalked %= 7;
                    vulcanSneakTicks = 3;
                }
            }
        } else {
            vulcanLastBlockX = currentBlockX;
            vulcanLastBlockZ = currentBlockZ;
        }

        boolean shouldSneak = client.player.isOnGround() && isOnEdge() && vulcanSneakTicks > 0;
        client.options.keySneak.setPressed(shouldSneak);
        if (shouldSneak) {
            vulcanSneakTicks--;
        }
    }

    private void resetVulcanState() {
        vulcanBlocksWalked = 0;
        vulcanSneakTicks = 0;
        vulcanHasLastPos = false;
        vulcanLastBlockX = 0;
        vulcanLastBlockZ = 0;
    }
}