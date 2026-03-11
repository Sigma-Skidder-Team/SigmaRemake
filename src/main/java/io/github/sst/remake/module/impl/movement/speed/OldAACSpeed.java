package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.event.impl.game.player.JumpEvent;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.util.game.player.MovementUtils;
import io.github.sst.remake.util.game.combat.RotationUtils;

public class OldAACSpeed extends SubModule {
    public final BooleanSetting autoJump = new BooleanSetting("Auto Jump", "Automatically jumps for you.", true);

    private double speedVal = 0.2873;
    private int airTicks = 0;
    private int groundState = 0;
    private float lastYaw = 0;
    private int localAirCounter = 0; 

    public OldAACSpeed() {
        super("OldAAC");
    }

    @Override
    public void onEnable() {
        // initialize states similar to Rebase; best-effort when player tracker isn't present
        if (client.player != null) {
            this.groundState = client.player.isOnGround() ? 0 : 1;
            this.speedVal = MovementUtils.getSpeed();
            this.lastYaw = RotationUtils.getDirectionArray()[0];
            this.localAirCounter = 0;
        }
    }

    @Override
    public void onDisable() {
        // reset motion when disabled
        this.speedVal = MovementUtils.getSpeed();
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (client.player == null) return;
        if (!client.player.isOnGround()) {
            airTicks++;
            if (airTicks == 1) {
                // choose initial airborne speed based on previous groundState
                speedVal = (groundState != 1) ? (groundState == 2 ? 0.362 : 0.306) : 0.306;
            }
            if (client.player.horizontalCollision) {
                speedVal = MovementUtils.getSpeed();
            }
            // MovementUtil.setMotion(event, speedVal, MovementUtil.getDirectionArray()[0], this.lastYaw, 45.0F)
            // Remake: use setMotionWithTurnLimit to preserve turning behavior
            this.lastYaw = MovementUtils.setMotionWithTurnLimit(event, speedVal, RotationUtils.getDirectionArray()[0], this.lastYaw, 45.0F);
        } else {
            airTicks = 0;
            if (autoJump.value && MovementUtils.isMoving()) {
                client.player.jump();
                event.setY(MovementUtils.getJumpValue());
                // copy player's motion into the event to match legacy behavior
                event.setX(client.player.getVelocity().x);
                event.setZ(client.player.getVelocity().z);
            }
            // legacy jump vertical was 0.4 + jumpBoost*0.1; match that check
            double legacyJump = 0.4 + MovementUtils.getJumpBoost() * 0.1;
            if (event.getY() != legacyJump) {
                groundState = 0;
            } else {
                MovementUtils.setMotion(event, speedVal);
            }
        }
    }

    @Subscribe
    public void onJump(JumpEvent event) {
        // emulate legacy jump state transitions
        if (this.groundState < 2) this.groundState++;

        if (this.groundState != 1) {
            if (this.groundState == 2) {
                this.speedVal = 0.6;
            }
        } else {
            this.speedVal = 0.5;
        }

        this.lastYaw = RotationUtils.getDirectionArray()[0];
        // set jump vertical and reset air counter
        event.velocity = new net.minecraft.util.math.Vec3d(event.velocity.x, 0.4 + MovementUtils.getJumpBoost() * 0.1, event.velocity.z);
        this.localAirCounter = 0;
    }

    @Subscribe
    public void onReceivePacket(io.github.sst.remake.event.impl.game.net.ReceivePacketEvent event) {
        if (event.packet instanceof net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket) {
            this.groundState = 0;
            this.speedVal = MovementUtils.getSpeed();
        }
    }
}
