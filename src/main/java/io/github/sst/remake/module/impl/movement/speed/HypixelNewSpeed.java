package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.util.game.player.MovementUtils;
import net.minecraft.entity.effect.StatusEffects;

public class HypixelNewSpeed extends SubModule {
    private int fallTicks = 0;

    public HypixelNewSpeed() {
        super("HypixelNew");
    }

    @Override
    public void onEnable() {
        fallTicks = 0;
    }

    @Override
    public void onDisable() {
        // reset state
        fallTicks = 0;
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (client.player == null || client.world == null || client.player.isTouchingWater() || client.player.isSpectator()) return;

        // Update fallTicks for phase transitions
        fallTicks = client.player.isOnGround() ? 0 : fallTicks + 1;

        // Ground behavior: jump and apply base directional motion, with potion-aware adjustment
        if (client.player.isOnGround()) {
            if (MovementUtils.isMoving()) {
                client.player.jump();
            }

            if (client.player.hasStatusEffect(StatusEffects.SPEED)) {
                int amp = client.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
                double extra = (amp == 0) ? 0.036 : 0.12;
                MovementUtils.setMotion(event, 0.46 + extra);
            } else {
                MovementUtils.setMotion(event, 0.467);
            }
        } else if (!client.player.horizontalCollision && !client.player.hasStatusEffect(StatusEffects.JUMP_BOOST) && client.player.hurtTime == 0) {
            double baseVelocityY = client.player.getVelocity().y;
            switch (fallTicks) {
                case 1:
                    MovementUtils.setMotion(event, 0.33);
                    if (client.player.hasStatusEffect(StatusEffects.SPEED)) {
                        MovementUtils.setMotion(event, 0.36);
                    }
                    event.setY(baseVelocityY + 0.057);
                    MovementUtils.setPlayerYMotion(event.getY());
                    break;
                case 3:
                    event.setY(baseVelocityY - 0.1309);
                    MovementUtils.setPlayerYMotion(event.getY());
                    break;
                case 4:
                    event.setY(baseVelocityY - 0.2);
                    MovementUtils.setPlayerYMotion(event.getY());
                    break;
                default:
                    break;
            }
        }

        // After mid-air adjustments, legacy also forces a jump when moving
        if (MovementUtils.isMoving()) {
            client.player.jump();
        }

        // Final directional speed application (mirror Rebase end-of-method calls)
        if (client.player.hasStatusEffect(StatusEffects.SPEED)) {
            int amp = client.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            double extra = (amp == 0) ? 0.036 : 0.12;
            MovementUtils.setMotion(event, 0.46 + extra);
        } else {
            MovementUtils.setMotion(event, 0.465);
        }
    }

    @Subscribe
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket) {
            fallTicks = 0;
        }
    }
}
