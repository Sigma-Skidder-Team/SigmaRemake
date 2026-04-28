package io.github.sst.remake.module.impl.movement.blockfly;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.ActionEvent;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.event.impl.game.player.*;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.module.impl.movement.BlockFlyModule;
import io.github.sst.remake.module.impl.movement.SafeWalkModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.util.game.player.MovementUtils;
import io.github.sst.remake.util.game.combat.data.Rotation;
import io.github.sst.remake.util.game.combat.RotationUtils;
import io.github.sst.remake.util.game.world.BlockUtils;
import io.github.sst.remake.util.game.world.data.PositionFacing;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

@SuppressWarnings({"DataFlowIssue", "unused"})
public class OldAACBlockFly extends SubModule{
    private static final float NO_ROTATION_SENTINEL = 999.0f;

    private final BooleanSetting haphe = new BooleanSetting("Haphe (AACAP)", "Never lets you touch the ground", false);

    private float targetYaw;
    private float targetPitch;

    private int scaffoldYLevel;
    private int originalHotbarSlot = 0;
    private PositionFacing pendingPlace;

    private int hopTicks;
    private int speedStage;

    public OldAACBlockFly() {
        super("Old AAC");
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

        scaffoldYLevel = (int) client.player.getY();
        speedStage = -1;
        pendingPlace = null;

        getParent().lastSpoofedSlot = -1;
    }

    @Override
    public void onDisable() {
        if (client.player == null) return;

        getParent().handleDisableSlotSpoof(originalHotbarSlot);
        originalHotbarSlot = -1;
        targetYaw = NO_ROTATION_SENTINEL;
        targetPitch = NO_ROTATION_SENTINEL;
        pendingPlace = null;

        setTimer(1.0f);
    }

    @Subscribe
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            speedStage = 0;
        }
    }

    @Subscribe
    public void onSafeWalk(SafeWalkEvent event) {
        if (client.player == null) return;

        if (client.player.isOnGround() && Client.INSTANCE.moduleManager.getModule(SafeWalkModule.class).enabled) {
            event.setSafe(true);
        }
    }

    @Subscribe
    public void onJump(JumpEvent event) {
        if (event.entity != client.player) return;

        if (getParent().towerMode.value.equals("Vanilla") && (!MovementUtils.isMoving() || getParent().moveAndTower.value)) {
            event.cancel();
        }
    }

    @Subscribe
    public void onFov(MovementFovEvent event) {
        if (haphe.value && MovementUtils.isMoving() && !client.player.isSprinting()) {
            event.speed *= 1.14f;
        }
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (!haphe.value) {
            client.options.keySprint.setPressed(false);
            client.player.setSprinting(false);
        }

        getParent().performTowering(event);

        if (haphe.value) {
            if (!client.player.isOnGround() || client.player.forwardSpeed == 0.0f && client.player.sidewaysSpeed == 0.0f) {
                if (hopTicks >= 0) {
                    hopTicks++;
                }
            } else {
                hopTicks = 0;
                client.player.jump();
                event.setY(0.419998 + (double) MovementUtils.getJumpBoost() * 0.1);
                if (speedStage < 3) {
                    speedStage++;
                }
            }

            if (client.player.forwardSpeed == 0.0f && client.player.sidewaysSpeed == 0.0f || client.player.horizontalCollision) {
                speedStage = 0;
            }

            double currentSpeed = MovementUtils.getAacHopSpeed(hopTicks, speedStage, () -> speedStage = 0);
            if (hopTicks >= 0) {
                MovementUtils.setMotion(event, currentSpeed);
            }
        }
    }

    @Subscribe(priority = Priority.LOWEST)
    public void onMotion(MotionEvent event) {
        if (event.isPre()) {
            getParent().refillHotbarWithBlocks();
            if (getParent().countPlaceableBlocks() == 0) {
                pendingPlace = null;
                targetYaw = NO_ROTATION_SENTINEL;
                targetPitch = NO_ROTATION_SENTINEL;
            }
            return;
        }

        if (MovementUtils.isMoving()
                && client.player.isOnGround()
                && haphe.value
                && !client.player.jumping) {
            client.player.jump();
        }
    }

    @Subscribe(priority = Priority.LOWEST)
    public void onAction(ActionEvent event) {
        if (client.player == null) return;
        if (pendingPlace == null) return;
        if (targetYaw == NO_ROTATION_SENTINEL) return;
        if (getParent().countPlaceableBlocks() == 0) return;

        tryPlaceBlock();
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

        double y = client.player.getY();
        if (!client.player.jumping && haphe.value) {
            y = scaffoldYLevel;
        }

        BlockPos below = new BlockPos(client.player.getX(), (double) Math.round(y - 1.0), client.player.getZ());
        List<PositionFacing> targets = getPlacementPath(below);
        pendingPlace = null;

        if (!targets.isEmpty()) {
            PositionFacing target = targets.get(targets.size() - 1);
            pendingPlace = target;
            Rotation rots = RotationUtils.getBlockPlacementRotations(target.blockPos, target.direction);
            targetYaw = rots.yaw;
            targetPitch = rots.pitch;
        }

        if (targetYaw == NO_ROTATION_SENTINEL) {
            return;
        }

        event.yaw = targetYaw;
        event.pitch = targetPitch;
    }

    private static List<PositionFacing> getPlacementPath(BlockPos startPos) {
        return BlockUtils.findPlacementPath(
                Blocks.STONE,
                startPos,
                (int) client.interactionManager.getReachDistance()
        );
    }

    private void tryPlaceBlock() {
        if (getParent().itemSpoofMode.value.equals("None")) {
            if (!BlockUtils.isPlacableBlockItem(client.player.getStackInHand(Hand.MAIN_HAND).getItem())) {
                return;
            }
        }

        BlockHitResult hit = new BlockHitResult(
                BlockUtils.getRandomizedHitVec(pendingPlace.blockPos, pendingPlace.direction),
                pendingPlace.direction,
                pendingPlace.blockPos,
                false
        );

        if (haphe.value && !client.player.jumping && !client.player.isOnGround()) {
            if (hit.getSide() == Direction.UP) {
                return;
            }
            if (hit.getBlockPos().getY() != scaffoldYLevel - 1) {
                return;
            }
        }

        if (hit.getSide() == Direction.UP
                && (hit.getBlockPos().getY() + 2) > client.player.getY()
                && BlockUtils.isValidBlockPosition(hit.getBlockPos())) {
            return;
        }

        if (hit.getBlockPos().getY() == client.player.getY()) {
            return;
        }

        getParent().spoofAndPlace(hit);

        if (hit.getSide() == Direction.UP) {
            scaffoldYLevel = hit.getBlockPos().getY() + 2;
        }
    }
}