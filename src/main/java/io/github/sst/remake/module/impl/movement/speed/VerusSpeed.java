package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.util.game.player.MovementUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;

public class VerusSpeed extends SubModule {
    public final ModeSetting verusMode = new ModeSetting("Mode", "Speed mode", 0, "Basic", "Low", "Ground", "Glide");
    public final BooleanSetting damageBoost = new BooleanSetting("DamageBoost", "Boost on damage", false);
    public final SliderSetting damageBoostTime = new SliderSetting("DamageBoostTime", "Seconds to boost after damage", 3f, 0.05f, 11f, 0.5f);
    public final BooleanSetting useTimer = new BooleanSetting("Timer", "Use timer (not Fabric compatible)", false);
    public final SliderSetting timerSpeed = new SliderSetting("TimerSpeed", "Timer speed", 1.0f, 0.1f, 10f, 0.1f);
    
    private double speed = 0;
    private int airTicks = 0;
    private long boostStartTime = 0;

    public VerusSpeed() {
        super("Verus");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        boostStartTime = 0;
        speed = MovementUtils.getSpeed();
        airTicks = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        setTimer(1.0f);
        boostStartTime = 0;
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        boolean justDamaged = client.player.hurtTime > 0;
        boolean boostActive = (System.currentTimeMillis() - boostStartTime) < (damageBoostTime.value * 1000);

        if (justDamaged && damageBoost.value) {
            boostStartTime = System.currentTimeMillis();
        }

        // optionally apply timer like legacy when enabled
        if (useTimer.value) setTimer((float) timerSpeed.value);

        switch (verusMode.value) {
            case "Basic":
                if (!client.player.isOnGround()) {
                    speed *= 0.9999999999;
                    airTicks++;
                } else {
                    airTicks = 0;
                    speed = 0.377;
                    if (client.player.hasStatusEffect(StatusEffects.SPEED)) {
                        speed += (client.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1) * (client.player.sidewaysSpeed == 0 ? 0.122 : 0.121);
                    }
                    if (!client.player.isSprinting()) speed *= 0.78;
                    float slip = getBlockSlipperiness();
                    if (slip != 0.6f) speed += (slip * 0.3652);
                    client.player.jump();
                }
                break;
            case "Low":
                if (!client.player.isOnGround()) {
                    speed *= 0.9999999999;
                    airTicks++;
                    if (airTicks == 2)
                        client.player.setVelocity(client.player.getVelocity().x, -0.0784, client.player.getVelocity().z);
                } else {
                    airTicks = 0;
                    speed = 0.3772;
                    if (client.player.hasStatusEffect(StatusEffects.SPEED))
                        speed += (client.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1) * 0.018;
                    if (!client.player.isSprinting()) speed *= 0.78;
                    float slip = getBlockSlipperiness();
                    if (slip != 0.6f) speed += (slip * 0.3652);
                    client.player.jump();
                }
                break;
            case "Ground":
                if (client.player.isOnGround()) {
                    speed = MovementUtils.getSpeed() - (MovementUtils.getSpeed() * 0.2);
                    float slip = getBlockSlipperiness();
                    if (slip != 0.6f) speed += (slip * 0.35);
                    MovementUtils.strafe(speed);
                }
                break;
            case "Glide":
                if (!client.player.isOnGround()) {
                    speed *= 0.9999999999;
                    airTicks++;
                } else {
                    airTicks = 0;
                    speed = client.player.hasStatusEffect(StatusEffects.SPEED) ? 0.498 : 0.377;
                    float slip = getBlockSlipperiness();
                    if (slip != 0.6f) speed += (slip * 0.37);
                    client.player.jump();
                }
                if (!client.player.isSprinting()) speed *= 0.78;
                client.player.setVelocity(client.player.getVelocity().x, Math.max(client.player.getVelocity().y, -0.098), client.player.getVelocity().z);
                break;
        }
        // Damage boost logic
        if ((justDamaged || boostActive) && damageBoost.value) {
            boolean groundMode = verusMode.value.equals("Ground");
            MovementUtils.strafe(groundMode ? 1.2 : 0.86);
            if (groundMode)
                client.player.setVelocity(client.player.getVelocity().x, -1, client.player.getVelocity().z);
        } else {
            MovementUtils.strafe(speed);
        }
    }

    @Subscribe
    public void onReceivePacket(io.github.sst.remake.event.impl.game.net.ReceivePacketEvent event) {
        // reset boost state on server position look
        if (event.packet instanceof net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket) {
            boostStartTime = 0;
            // restore timer on lagback
            if (useTimer.value) setTimer(1.0f);
        }
    }

    private float getBlockSlipperiness() {
        BlockPos below = new BlockPos(client.player.getX(), client.player.getY() - 0.5, client.player.getZ());
        BlockState state = client.world.getBlockState(below);
        Block b = state.getBlock();
        return b.getSlipperiness();
    }
}
