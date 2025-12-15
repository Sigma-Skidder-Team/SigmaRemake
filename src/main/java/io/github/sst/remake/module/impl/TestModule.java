package io.github.sst.remake.module.impl;

import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.ModeSetting;

public class TestModule extends Module {
    private final ModeSetting
            mode = new ModeSetting("Test Mode", "A test mode setting.", 0, "Mode1", "Mode2", "Mode3");

    public TestModule() {
        super(Category.MISC, "Test", "A module for testing purposes.");
    }
}
