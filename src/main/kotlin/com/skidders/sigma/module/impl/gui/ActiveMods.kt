package com.skidders.sigma.module.impl.gui;

import com.google.common.eventbus.Subscribe;
import com.skidders.SigmaReborn;
import com.skidders.sigma.events.impl.Render2DEvent;
import com.skidders.sigma.module.Category;
import com.skidders.sigma.module.Module;
import com.skidders.sigma.module.settings.impl.BooleanSetting;
import com.skidders.sigma.module.settings.impl.ModeSetting;
import com.skidders.sigma.module.settings.impl.NumberSetting;
import com.skidders.sigma.utils.render.font.styled.StyledFont;
import com.skidders.sigma.utils.render.font.styled.StyledFontRenderer;
import com.skidders.sigma.utils.render.interfaces.IFontRegistry;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ActiveMods : Module("ActiveMods", "Renders active mods", Category.GUI, GLFW.GLFW_KEY_V) {

    private StyledFont font = IFontRegistry.Light20;

    constructor() {
        registerSetting(ModeSetting("Size", "The font size", "Normal", new String[]{"Normal", "Small", "Tiny"}));
        registerSetting(BooleanSetting("Animations", "Scale in animation", true));
        registerSetting(NumberSetting("Slider", "OMG, it's a slider!", 3, 1, 5, 1.0f));
    }

    @Override
    public void onEnable() {
        switch ((String) getSettingByName("Size").value) {
            case "Normal" -> font = IFontRegistry.Light20;
            case "Small" -> font = IFontRegistry.Light18;
            case "Tiny" -> font = IFontRegistry.Light14;
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

            StyledFontRenderer.drawString(event.matrixStack, font, module.name, x, y, new Color(255, 255, 255, 150));

            offsetY += 12;
        }
    }

}
