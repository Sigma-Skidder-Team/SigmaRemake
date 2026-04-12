package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.util.game.player.MovementUtils;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;

public class MineplexSpeed extends SubModule {
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", "Automatically jumps for you.", true);
    public final BooleanSetting onGroundSpeedMode = new BooleanSetting("OnGround", "OnGround Speed.", true);
    public final SliderSetting onGroundSpeed = new SliderSetting("OnGround Speed", "OnGround value.", 0.8f, 0.3f, 1.5f, 0.01f);

    private double internalSpeed = 0.3;
    private int tickAir = 0;
    private int tickGround = 0;
    private int lastSelectedSlot = -1;

    public MineplexSpeed() {
        super("Mineplex");
    }

    @Override
    public void onEnable() {
        this.internalSpeed = 0.3;
        this.tickAir = 0;
        this.tickGround = 0;
        this.lastSelectedSlot = -1;
    }

    @Override
    public void onDisable() {
        // restore defaults if needed
        this.internalSpeed = MovementUtils.getSpeed();
        this.tickAir = 0;
        this.tickGround = 0;
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (client.player == null) return;
        if (!MovementUtils.isMoving()) {
            internalSpeed = 0.3;
            return;
        }
        double oGSpeed = onGroundSpeed.value;
            if (!client.player.isOnGround()) {
                if (client.player.horizontalCollision) {
                    internalSpeed = 0.35;
                    tickAir = 1;
                }
            internalSpeed -= 0.01;
            if (internalSpeed < 0.3) internalSpeed = 0.3;
            MovementUtils.setMotion(event, internalSpeed);
            } else {
                if (tickGround > 1) {
                    tickAir = 0;
                } else {
                    tickGround++;
                }
            if (autoJump.value) {
                client.player.jump();
                event.setY(MovementUtils.getJumpValue());
            }
            if (event.getY() == 0.4199998) {
                MovementUtils.setMotion(event, 0.0);
                return;
            }
            if (!onGroundSpeedMode.value) {
                return;
            }
            // emulate legacy try-use-on-block packet to trigger server-side updates
            try {
                BlockPos below = new BlockPos(client.player.getBlockPos()).add(0, -1, 0);
                Vec3d vec = new Vec3d(0.475 + Math.random() * 0.05, 1.0, 0.475 + Math.random() * 0.05);
                BlockHitResult hit = new BlockHitResult(vec, Direction.UP, below, false);
                client.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit));
            } catch (Throwable ignored) {}

            internalSpeed += oGSpeed / 4.0;
            if (client.player.horizontalCollision) {
                internalSpeed /= 2.0;
            }
            if (internalSpeed > oGSpeed) internalSpeed = oGSpeed;
            if (internalSpeed < 0.3) internalSpeed = 0.3;
            MovementUtils.setMotion(event, internalSpeed);
        }
    }

    @Subscribe
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket) {
            // reset ticks when lagback detected
            tickAir = 0;
            tickGround = 0;
        }
    }

    @Subscribe
    public void onJump(io.github.sst.remake.event.impl.game.player.JumpEvent event) {
        if (client.player == null) return;

        // replicate legacy Mineplex jump handling and held-item change spoof
        this.internalSpeed = 0.81 + (double) this.tickAir * 0.095;
        if (client.player.getY() != (double) ((int) client.player.getY())) {
            this.internalSpeed = 0.52;
            this.tickAir = 1;
        }

        this.tickGround = 0;
        if (this.tickAir < 2) this.tickAir++;

        // Set vertical motion similar to legacy
        event.velocity = new net.minecraft.util.math.Vec3d(event.velocity.x, 0.4199998, event.velocity.z);

        // send held-item change packet if necessary to mirror legacy behavior
        try {
            int current = client.player.getInventory().selectedSlot;
            if (current != this.lastSelectedSlot) {
                client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(current));
                this.lastSelectedSlot = current;
            }
        } catch (Throwable ignored) {}
    }
}
