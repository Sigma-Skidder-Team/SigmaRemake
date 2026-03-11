package io.github.sst.remake.module.impl.movement.speed;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.util.game.player.MovementUtils;

public class LegitSpeed extends SubModule {
    public final BooleanSetting sprint = new BooleanSetting("Sprint", "Sprints when walking", true);
    public final BooleanSetting autoJump = new BooleanSetting("AutoJump", "Automatically jumps for you", true);

    public LegitSpeed() {
        super("Legit");
    }

    @Override
    public void onDisable() {
        client.options.keySprint.setPressed(false);
        client.options.keyJump.setPressed(false);
    }

    @Subscribe
    public void onMove(MoveEvent event) {
        if (MovementUtils.isMoving()) {
            if (sprint.value) client.options.keySprint.setPressed(true);
            if (autoJump.value) client.options.keyJump.setPressed(true);
        }
    }

    @Subscribe
    public void onMotion(io.github.sst.remake.event.impl.game.player.MotionEvent event) {
        // Keep sprint/jump key state consistent each tick like legacy
        if (MovementUtils.isMoving()) {
            client.options.keySprint.setPressed(sprint.value);
            client.options.keyJump.setPressed(autoJump.value);
        } else {
            client.options.keySprint.setPressed(false);
            client.options.keyJump.setPressed(false);
        }
    }
}
