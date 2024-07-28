package info.opensigma.module.impl;

import info.opensigma.module.Module;
import info.opensigma.ui.clickgui.ClickGui;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Module {

    private final ClickGui clickGui = new ClickGui();

    public ClickGuiModule() {
        super("ClickGui", "ClickGui", GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        client.openScreen(clickGui);

        toggle();
    }

}
