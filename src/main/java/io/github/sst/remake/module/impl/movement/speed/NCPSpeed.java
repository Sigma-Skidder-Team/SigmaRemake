package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.util.game.player.MovementUtils;

public class NCPSpeed extends SubModule {
    public final BooleanSetting autoJump = new BooleanSetting("Auto Jump", "Automatically jumps for you.", true);

    private int jumpState = 1;
    private int airTicks = 0;
    private double lastSpeed = 0.2873;

    public NCPSpeed() {
        super("NCP");
    }

    @Override
    public void onEnable() {
        jumpState = 1;
        if (client.player != null) {
            double mX = client.player.getVelocity().x;
            double mZ = client.player.getVelocity().z;
            lastSpeed = Math.sqrt(mX * mX + mZ * mZ);
        } else {
            lastSpeed = MovementUtils.getSpeed();
        }
    }

    @Override
    public void onDisable() {
        // restore sensible defaults
        jumpState = 1;
        airTicks = 0;
        lastSpeed = MovementUtils.getSpeed();
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        // Ported behavior from legacy: maintain lastSpeed when airborne, optionally auto-jump
        if (!client.player.isTouchingWater() && !client.player.isOnGround()) {
            airTicks++;
            double speedValue = lastSpeed;
            if (airTicks > 1) {
                speedValue = Math.max(MovementUtils.getSpeed(), lastSpeed - (0.004 - MovementUtils.getSpeed() * 0.003));
            }
            MovementUtils.setMotion(event, speedValue);
            // subtle Y corrections from legacy are omitted (fabric event differences)
        } else if (autoJump.value && MovementUtils.isMoving()) {
            airTicks = 0;
            client.player.jump();
            // ensure event reflects player's new motion
            event.setX(client.player.getVelocity().x);
            event.setY(client.player.getVelocity().y);
            event.setZ(client.player.getVelocity().z);
        }
    }

    @Subscribe
    public void onJump(io.github.sst.remake.event.impl.game.player.JumpEvent event) {
        // Legacy NCP cancels default jump vector and applies strafe-based boost
        if (!client.player.isTouchingWater() && !client.player.isOnGround()) return;

        if (this.jumpState != 0) {
            // cancel default jump handling so we can set custom values in move
            event.cancel();
        }

        if (!client.options.keyJump.isPressed() || io.github.sst.remake.Client.INSTANCE.moduleManager == null) {
            double strafeSpeed = 0.56 + (double) MovementUtils.getSpeedBoost() * 0.1;
            event.velocity = new net.minecraft.util.math.Vec3d(event.velocity.x, 0.407 + (double) MovementUtils.getJumpBoost() * 0.1 + Math.random() * 1.0E-5, event.velocity.z);
            if (io.github.sst.remake.module.impl.movement.SpeedModule.tickCounter < 2) {
                strafeSpeed /= 2.5;
            }

            strafeSpeed = Math.max(MovementUtils.getSpeed(), strafeSpeed);
            // No direct event.setStrafeSpeed; approximate by storing lastSpeed
            this.lastSpeed = strafeSpeed;
        }
    }

    @Subscribe
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket) {
            // reset NCP state on lagback/teleport
            this.jumpState = 1;
            this.airTicks = 0;
            this.lastSpeed = MovementUtils.getSpeed();
        }
    }
}
