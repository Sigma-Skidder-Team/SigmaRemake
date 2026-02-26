package io.github.sst.remake.module.impl.movement.blockfly;

import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;

public class AACBlockFly extends SubModule {
    private final BooleanSetting test1 = new BooleanSetting("Test 1", "testing setting", true);

    public AACBlockFly() {
        super("AAC");
    }

    @Override
    public void onEnable() {
        sendChatMessage("Hello from BlockFly!");
    }
}
