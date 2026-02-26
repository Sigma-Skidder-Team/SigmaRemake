package io.github.sst.remake.module.impl.movement.blockfly;

import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.setting.impl.BooleanSetting;

public class NCPBlockFly extends SubModule {
    private final BooleanSetting test2 = new BooleanSetting("Test 2", "testing setting ", false);

    public NCPBlockFly() {
        super("NCP");
    }

    @Override
    public void onEnable() {
        sendChatMessage("Hello from NCP BlcoFkly!");
    }
}
