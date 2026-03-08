package io.github.sst.remake.module.impl.world;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.SliderSetting;

@SuppressWarnings("unused")
public class TimerModule extends Module {
    private final SliderSetting gameSpeed = new SliderSetting("Game speed", "Timer value", 0.3f, 0.1f, 10.0f, 0.1f);

    public TimerModule() {
        super("Timer", "Alter the game speed.", Category.WORLD);
    }

    @Override
    public void onDisable() {
        setTimer(1.0f);
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        setTimer(gameSpeed.value);
    }
}