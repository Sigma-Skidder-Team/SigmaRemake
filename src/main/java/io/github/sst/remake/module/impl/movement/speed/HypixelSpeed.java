package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import io.github.sst.remake.event.impl.game.player.JumpEvent;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.event.impl.game.world.LoadWorldEvent;
import io.github.sst.remake.event.impl.game.render.Render2DEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.util.game.player.MovementUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class HypixelSpeed extends SubModule {
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", "Automatically jumps for you.", true);
    public final BooleanSetting timer = new BooleanSetting("Timer", "Use timer", true);
    public final BooleanSetting groundSpeed = new BooleanSetting("GroundSpeed", "Move faster on ground", true);
    public final BooleanSetting borderJump = new BooleanSetting("BorderJump", "Jump off edges with speed", true);

    private int stateTick = 6;
    private double speedBoost = 0.2873;
    private double lastY = -1.0;
    private double legacyDownAccum = 0.0;

    public HypixelSpeed() {
        super("Hypixel");
    }

    @Override
    public void onEnable() {
        this.stateTick = 6;
        this.speedBoost = 0.2873;
        this.lastY = -1.0;
        this.legacyDownAccum = 0.0;
    }

    @Override
    public void onDisable() {
        // restore timers and downward motion like legacy
        try {
            if (client.player != null && client.player.getVelocity().y > 0.0 && this.stateTick == 0) {
                client.player.setVelocity(client.player.getVelocity().x, -MovementUtils.getJumpValue() - 1.0E-5 - 0.0625, client.player.getVelocity().z);
            }
        } catch (Throwable ignored) {}
        try { setTimer(1.0f); } catch (Throwable ignored) {}
    }

    @Subscribe
    public void onMotion(MotionEvent event) {
        // Maintain compatibility with legacy Motion handling: track stateTick and minor adjustments
        if (client.player == null) return;
        if (client.player.isOnGround()) {
            // reset airborne tracking
            // no-op for now; existing onMove handles primary behavior
        }
        // Legacy code sometimes tweaked onGround in pre-motion; preserve compatibility by ensuring the event
        // observes onGround when the player is on ground. MotionEvent exposes a setter named `setOnGround` via Lombok
        // in some mappings; to avoid mapping issues, write the field directly.
        try {
            if (event.isPre() && client.player.isOnGround()) {
                event.onGround = true;
            }
        } catch (Throwable ignored) {}
    }

    @Subscribe
    public void onJump(JumpEvent event) {
        // Preserve legacy auto-jump hook. No additional behavior required here;
        // movement logic is handled in onMove().
        if (this.parent == null) return;
        if (this.parent.enabled) return;
    }

    @Subscribe
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            // mirror legacy: reset internal state
            this.stateTick = -2;
            this.legacyDownAccum = 0.0;
        }
    }

    @Subscribe
    public void onLoadWorld(LoadWorldEvent event) {
        this.lastY = -1.0;
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        // legacy had HUD adjustments; not required for core movement. noop.
    }

    // Called by SpeedModule.callHypixelSpeedMethod()
    public void method16044() {
        this.stateTick = 0;
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        // If on ground, jump and boost speed
        if (client.player.isOnGround() && stateTick >= 0) {
            if ((event.getY() > 0.0 || autoJump.value && MovementUtils.isMoving()) && !client.player.isTouchingWater()) {
                client.player.jump();
                event.setY(MovementUtils.getJumpValue());
                MovementUtils.setMotion(event, 0.644 + MovementUtils.getSpeedBoost() * 0.13);
                if (timer.value) {
                    setTimer(1.4123f);
                }
                stateTick = 0;
            } else if (MovementUtils.isMoving() && groundSpeed.value && !client.player.isTouchingWater()) {
                client.player.jump();
                event.setY(0.399 + MovementUtils.getJumpBoost() * 0.1);
                MovementUtils.setMotion(event, 0.51 + MovementUtils.getSpeedBoost() * 0.098);
                if (timer.value) {
                    setTimer(1.1123f);
                }
                stateTick = 0;
            }
        } else if (stateTick >= 0) {
            // While airborne, keep boosting speed
            double baseSpeed = MovementUtils.getSpeed();
            if (stateTick == 0) {
                speedBoost = 0.39 + MovementUtils.getSpeedBoost() * 0.077;
            } else {
                speedBoost *= 0.99375;
            }
            MovementUtils.setMotion(event, speedBoost);
            if (event.getY() < 0.0) this.legacyDownAccum -= event.getY();
            if (this.legacyDownAccum > 3.0) {
                this.legacyDownAccum = 0.0;
                try {
                    // send a onGround-style packet to replicate legacy CPlayerPacket(true)
                    client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket(true));
                } catch (Throwable ignored) {
                }
            }
            stateTick++;
        }
    }
}
