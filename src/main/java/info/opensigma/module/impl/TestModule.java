package info.opensigma.module.impl;

import info.opensigma.module.Module;
import org.lwjgl.glfw.GLFW;

public class TestModule extends Module {

    public TestModule() {
        super("Test", "A module purely for testing purposes", GLFW.GLFW_KEY_V);
    }

    @Override
    public void onEnable() {
        System.out.println("Hello, World!");
    }

}
