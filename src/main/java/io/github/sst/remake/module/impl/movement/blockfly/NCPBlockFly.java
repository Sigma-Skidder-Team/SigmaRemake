package io.github.sst.remake.module.impl.movement.blockfly;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.data.rotation.Rotatable;
import io.github.sst.remake.data.rotation.Rotation;
import io.github.sst.remake.event.impl.client.KeyPressEvent;
import io.github.sst.remake.event.impl.client.MouseHoverEvent;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.player.JumpEvent;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.event.impl.game.player.SafeWalkEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.module.impl.movement.BlockFlyModule;
import io.github.sst.remake.module.impl.movement.SafeWalkModule;
import io.github.sst.remake.util.game.MovementUtils;
import io.github.sst.remake.util.game.RotationUtils;
import io.github.sst.remake.util.game.WorldUtils;
import io.github.sst.remake.util.game.world.BlockUtils;
import io.github.sst.remake.util.game.world.RaytraceUtils;
import io.github.sst.remake.util.game.world.data.PositionFacing;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings({"DataFlowIssue", "unused"})
public class NCPBlockFly extends SubModule implements Rotatable {
    private static final float NO_ROTATION_SENTINEL = 999.0f;

    private float targetYaw;
    private float targetPitch;

    private PositionFacing pendingPlace;
    private int originalHotbarSlot = -1;
    private int rotationChangeTicks;
    private int groundTicksSinceLeave;
    private Hand placeHand = Hand.MAIN_HAND;
    private boolean allowJumpCancel;
    private double lockedY;
    private boolean isSneakDownwards;

    public NCPBlockFly() {
        super("NCP");
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
        targetYaw = targetPitch = NO_ROTATION_SENTINEL;
        getParent().lastSpoofedSlot = -1;
        if (client.options.keySneak.isPressed() && getParent().downwards.value) {
            client.options.keySneak.setPressed(false);
            isSneakDownwards = true;
        }

        if (!client.options.keySneak.isPressed()) {
            isSneakDownwards = false;
        }

        lockedY = -1.0;
        allowJumpCancel = false;
        if (client.player.isOnGround()) {
            lockedY = client.player.getY();
        }

        groundTicksSinceLeave = -1;
    }

    @Override
    public void onDisable() {
        if (client.player == null) return;

        if (originalHotbarSlot != -1 && getParent().itemSpoofMode.value.equals("Switch")) {
            client.player.inventory.selectedSlot = originalHotbarSlot;
        }
        originalHotbarSlot = -1;

        if (getParent().lastSpoofedSlot >= 0) {
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(client.player.inventory.selectedSlot));
            getParent().lastSpoofedSlot = -1;
        }

        MovementUtils.strafe(MovementUtils.getSpeed() * 0.9);
        setTimer(1.0f);
        if (getParent().speedMode.value.equals("Cubecraft") && groundTicksSinceLeave == 0) {
            MovementUtils.setPlayerYMotion(-0.0789);
        }
        client.options.keySneak.setPressed(false);
    }

    @Subscribe
    public void onSafeWalk(SafeWalkEvent event) {
        if (client.player == null) return;

        if (getParent().speedMode.value.equals("Cubecraft")
            /*&& !Client.getInstance().moduleManager.getModuleByClass(Fly.class).isEnabled()*/) {

            if (!client.world.getBlockCollisions(client.player,
                    client.player.getBoundingBox()
                            .stretch(0.0, -1.5, 0.0)
                            .shrink(0.05, 0.0, 0.05)
                            .shrink(-0.05, 0.0, -0.05)
            ).findAny().isPresent() && client.player.fallDistance < 1.0f) {
                event.setSafe(true);
            }
            return;
        }

        if (client.player.isOnGround()
                && Client.INSTANCE.moduleManager.getModule(SafeWalkModule.class).isEnabled()
                && (!isSneakDownwards || !getParent().downwards.value)) {
            event.setSafe(true);
        }
    }

    @Subscribe
    public void onKey(KeyPressEvent event) {
        if (client.player == null) return;

        if (getParent().downwards.value && event.key == client.options.keySneak.boundKey.getCode()) {
            event.cancel();
            isSneakDownwards = true;
        }
    }

    @Subscribe
    public void onHover(MouseHoverEvent event) {
        if (client.player == null) return;

        if (getParent().downwards.value && event.button == client.options.keySneak.boundKey.getCode()) {
            event.cancel();
            isSneakDownwards = false;
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

        if (client.player.isOnGround()) {
            groundTicksSinceLeave = 0;
        } else if (groundTicksSinceLeave >= 0) {
            groundTicksSinceLeave++;
        }

        switch (getParent().speedMode.value) {
            case "Jump":
                if (client.player.isOnGround() && MovementUtils.isMoving() && !client.player.isSneaking() && !isSneakDownwards) {
                    allowJumpCancel = false;
                    client.player.jump();
                    // Client.INSTANCE.moduleManager.getModule(SpeedModule.class).resetHopStage();
                    allowJumpCancel = true;
                    event.setY(client.player.getVelocity().y);
                    event.setX(client.player.getVelocity().x);
                    event.setZ(client.player.getVelocity().z);
                }
                break;

            case "AAC":
                if (rotationChangeTicks == 0 && client.player.isOnGround()) {
                    MovementUtils.setMotion(event, MovementUtils.getSpeed() * 0.82);
                }
                break;
            case "Cubecraft":
                double speed = 0.2;
                float yaw = RotationUtils.getDirection(RotationUtils.normalizeYaw(client.player.yaw));

                if (client.options.keyJump.isPressed()) {
                    setTimer(1.0f);
                } else if (client.player.isOnGround()) {
                    if (MovementUtils.isMoving() && !client.player.isSneaking() && !isSneakDownwards) {
                        event.setY(1.01);
                    }
                } else if (groundTicksSinceLeave == 1) {
                    if (event.getY() <= 0.9) {
                        groundTicksSinceLeave = -1;
                    } else {
                        event.setY(0.122);
                        setTimer(0.7f);
                        speed = 2.4;
                    }
                } else if (groundTicksSinceLeave == 2) {
                    if (event.getY() > 0.05) {
                        groundTicksSinceLeave = -1;
                    } else {
                        setTimer(0.7f);
                        speed = 0.28;
                    }
                } else if (groundTicksSinceLeave == 3) {
                    setTimer(0.3f);
                    speed = 2.4;
                } else if (groundTicksSinceLeave == 4) {
                    speed = 0.28;
                    setTimer(1.0f);
                } else if (groundTicksSinceLeave == 6) {
                    event.setY(-1.023456987345906);
                }

                if (!MovementUtils.isMoving()) {
                    speed = 0.0;
                }

                if (client.player.fallDistance < 1.0F) {
                    MovementUtils.setMotionWithTurnLimit(event, speed, yaw, yaw, 360.0F);
                }

                MovementUtils.setPlayerYMotion(event.getY());
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
                client.options.keySneak.setPressed(true);
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

        if (!event.isPre()) {
            getParent().refillHotbarWithBlocks();
            handlePlace();
        } else {
            if (getParent().countPlaceableBlocks() == 0) {
                pendingPlace = null;
                targetYaw = NO_ROTATION_SENTINEL;
                targetPitch = NO_ROTATION_SENTINEL;
            }
            event.moving = true;
        }
    }

    @Subscribe
    public void onRender(RenderClient2DEvent event) {
        if (!getParent().speedMode.value.equals("Cubecraft") || groundTicksSinceLeave < 0) return;

        if (client.player.fallDistance > 1.2f) return;
        if (client.player.capeY < lockedY) return;
        if (client.player.jumping) return;

        client.player.getPos().y = lockedY;
        client.player.lastRenderY = lockedY;
        client.player.capeY = lockedY;
        client.player.prevY = lockedY;

        if (MovementUtils.isMoving()) {
            client.player.strideDistance = 0.099999994f;
        }
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public Rotation getRotations() {
        if (client.player == null) return null;
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
        } else if (!getParent().keepRotations.value) {
            targetYaw = NO_ROTATION_SENTINEL;
            targetPitch = NO_ROTATION_SENTINEL;
        }

        if (targetYaw == NO_ROTATION_SENTINEL) {
            return null;
        }

        if (client.player.yaw != targetYaw || client.player.pitch != targetPitch) {
            rotationChangeTicks = 0;
        }

        return new Rotation(targetYaw, targetPitch);
    }

    private void updateTarget() {
        rotationChangeTicks++;

        placeHand = Hand.MAIN_HAND;

        double targetX = client.player.getX();
        double targetZ = client.player.getZ();
        double targetY = client.player.getY();

        if (!client.player.horizontalCollision && !client.options.keyJump.isPressed()) {
            double[] extended = BlockUtils.getSafeExtendedXZ(getParent().extend.value);
            targetX = extended[0];
            targetZ = extended[1];
        }

        if (client.player.getVelocity().y < 0.0
                && client.player.fallDistance > 1.0f
                && RaytraceUtils.rayTrace(0.0f, 90.0f, 3.0f).getType() == HitResult.Type.MISS) {
            targetY += Math.min(client.player.getVelocity().y * 2.0, 4.0);
        } else if (isSneakDownwards && getParent().downwards.value) {
            targetY -= 1.0;
        } else if ((getParent().speedMode.value.equals("Jump") || getParent().speedMode.value.equals("Cubecraft"))
                && !client.options.keyJump.isPressed()) {
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

        if (!BlockUtils.isValidBlockPosition(belowTarget) && getParent().canPlaceWithHand(placeHand)) {
            pendingPlace = BlockUtils.findPlaceableNeighbor(belowTarget, !isSneakDownwards && getParent().downwards.value);
        } else {
            pendingPlace = null;
        }
    }

    private void handlePlace() {
        if (pendingPlace == null) return;

        BlockHitResult hit = new BlockHitResult(
                BlockUtils.getRandomizedHitVec(pendingPlace.blockPos, pendingPlace.direction),
                pendingPlace.direction,
                pendingPlace.blockPos,
                false
        );

        int prevSlot = client.player.inventory.selectedSlot;

        if (!getParent().itemSpoofMode.value.equals("None")) {
            getParent().selectPlaceableHotbarSlot();
        }

        client.interactionManager.interactBlock(client.player, client.world, placeHand, hit);
        client.player.swingHand(placeHand);

        pendingPlace = null;

        String spoofMode = getParent().itemSpoofMode.value;
        if (spoofMode.equals("Spoof") || spoofMode.equals("LiteSpoof")) {
            client.player.inventory.selectedSlot = prevSlot;
        }
    }
}
