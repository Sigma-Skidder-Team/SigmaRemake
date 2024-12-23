package com.skidders.sigma.module.impl.gui;

import com.google.common.eventbus.Subscribe;
import com.skidders.SigmaReborn;
import com.skidders.sigma.events.impl.Render2DEvent;
import com.skidders.sigma.module.Category;
import com.skidders.sigma.module.Module;
import com.skidders.sigma.module.settings.impl.BooleanSetting;
import com.skidders.sigma.module.settings.impl.StringSetting;
import com.skidders.sigma.utils.font.Renderer;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ActiveMods extends Module {

    private Renderer font = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 20);

    public ActiveMods() {
        super("ActiveMods", "Renders active mods", Category.GUI, GLFW.GLFW_KEY_V);
        registerSetting(new StringSetting("Size", "The font size", "Normal", new String[]{"Normal", "Small", "Tiny"}));
        registerSetting(new BooleanSetting("Animations", "Scale in animation", true));
    }

    @Override
    public void onEnable() {
        System.out.println("hi");
        switch ((String) getSettingByName("Size").value) {
            case "Normal" -> font = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 20);
            case "Small" -> font = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 18);
            case "Tiny" -> font = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 14);
        }
    }

    @Subscribe
    public void on2D(Render2DEvent event) {
        if (mc.options.debugEnabled) {
            return;
        }
        float offsetY = 3;
        int screenWidth = mc.getWindow().getWidth();
        for (Module module : SigmaReborn.INSTANCE.moduleManager.modules) {
            float x, y = offsetY;

            x = (float) screenWidth / 2 - font.getWidth(module.name) - 3;

            font.drawString(module.name, x, y, new Color(255,255,255, 150).getRGB());

            offsetY += 12;
        }
    }

}
