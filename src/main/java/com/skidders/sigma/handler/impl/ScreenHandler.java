package com.skidders.sigma.handler.impl;

import com.skidders.sigma.event.impl.KeyPressEvent;
import com.skidders.sigma.event.impl.WindowSizeChangeEvent;
import com.skidders.sigma.handler.Handler;
import com.skidders.sigma.screen.guis.SwitchGUI;
import com.skidders.sigma.util.client.events.Listen;
import com.skidders.sigma.util.client.interfaces.IMinecraft;
import com.skidders.sigma.util.system.StringUtil;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

public class ScreenHandler extends Handler<Screen> implements IMinecraft {

    //public ClickGUI clickGUI;
    public int framerateLimit = 60;
    public int clickGuiBind = 344;
    public String clickGuiBindName;

    public boolean guiBlur = true, gpuAccelerated = true;

    public int guiScaleFactor = 1;
    public float resizingScaleFactor = 1.0F;

    @Override
    public void init() {
        super.init();

        list.add(new SwitchGUI());

        //clickGUI = new ClickGUI("Jello ClickGUI");
        clickGuiBindName = StringUtil.convertKeyToName(clickGuiBind);
        resizingScaleFactor = (float) (mc.getWindow().getFramebufferHeight() / mc.getWindow().getHeight());
    }

    @Listen
    private void onKey(KeyPressEvent event) {
        if (event.action == GLFW.GLFW_RELEASE && event.key == clickGuiBind && mc.world != null && mc.currentScreen == null) {
            //mc.openScreen(clickGUI);
            clickGuiBindName = StringUtil.convertKeyToName(clickGuiBind);
        }
    }

    @Listen
    private void onResize(WindowSizeChangeEvent event) {
        if (mc.getWindow().getWidth() != 0 && mc.getWindow().getHeight() != 0) {
            resizingScaleFactor = (float) Math.max(
                    mc.getWindow().getFramebufferWidth() / mc.getWindow().getWidth(),
                    mc.getWindow().getFramebufferHeight() / mc.getWindow().getHeight()
            );
        }
    }
}