package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.util.game.player.MovementUtils;

public class VanillaSpeed extends SubModule {
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", "Automatically jumps", false);
    public final SliderSetting speed = new SliderSetting("Speed", "Speed value", 4.0f, 1.0f, 10.0f, 0.1f);

    public VanillaSpeed() {
        super("Vanilla");
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        double speedInput = MovementUtils.getSpeed() * speed.value;
        MovementUtils.setMotion(event, speedInput);
        if (autoJump.value) {
            client.options.keyJump.setPressed(true);
        }
    }

    @Subscribe
    public void onMotion(io.github.sst.remake.event.impl.game.player.MotionEvent event) {
        // Preserve legacy hooks: if AutoJump is enabled, ensure jump key state mirrors expected behavior
        if (autoJump.value) {
            client.options.keyJump.setPressed(true);
        }
    }

    @Override
    public void onDisable() {
        // restore keys and timer
        try {
            client.options.keyJump.setPressed(false);
        } catch (Throwable ignored) {}
        setTimer(1.0f);
    }

    @Subscribe
    public void onJump(io.github.sst.remake.event.impl.game.player.JumpEvent event) {
        // No-op: preserved legacy hook
    }

    @Subscribe
    public void onReceivePacket(io.github.sst.remake.event.impl.game.net.ReceivePacketEvent event) {
        // No-op: preserved legacy hook
    }
}
