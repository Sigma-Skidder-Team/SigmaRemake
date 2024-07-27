package info.opensigma.module.impl;

import info.opensigma.module.Module;
import info.opensigma.setting.Setting;
import info.opensigma.setting.impl.primitive.PrimitiveSetting;
import org.lwjgl.glfw.GLFW;

public class TestModule extends Module {

    public final PrimitiveSetting<Boolean> testSetting = new PrimitiveSetting<>("Hello", "Purely for testing purposes", true);

    public TestModule() {
        super("Test", "A module purely for testing purposes", GLFW.GLFW_KEY_V);
    }

    @Override
    public void onEnable() {
        if (testSetting.getValue()) {
            System.out.printf("Hello, my bind is %d\n", key);
        } else {
            System.out.printf("My bind is %d\n", key);
        }
    }

}