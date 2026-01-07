package io.github.sst.remake.module.impl.misc;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.screen.notifications.Notification;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.*;
import io.github.sst.remake.util.render.image.Resources;

public class TestModule extends Module {
    private final ModeSetting dropdown = new ModeSetting("Dropdown", "A test mode setting.", 0, "Mode1", "Mode2", "Mode3");
    private final ColorSetting color = new ColorSetting("Color", "A color thingamabob", -1);
    private final BooleanSetting toggle = new BooleanSetting("Checkbox", "Yee", true);
    private final TextInputSetting input = new TextInputSetting("Input", "A", "...");
    private final BlockListSetting blocks = new BlockListSetting("Blocks", "A lot of blocks", true);
    private final SliderSetting slider = new SliderSetting("Slider", "A", 10, 0, 10, 0.1f);
    private final CurveSetting curve = new CurveSetting(
            "Curve",
            "OMG a curve??",
            1.0f,   // initial speed
            1.5f,   // mid speed
            2.0f,   // final stage speed
            3.0f    // maximum speed
    );

    public TestModule() {
        super(Category.MISC, "Test", "A module for testing purposes.");
    }

    @Override
    public void onEnable() {
        Client.INSTANCE.notificationManager.send(new Notification("Title", "Description", Resources.DIRECTION_ICON));
    }
}
