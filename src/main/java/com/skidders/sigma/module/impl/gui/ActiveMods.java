package com.skidders.sigma.module.impl.gui;

import com.google.common.eventbus.Subscribe;
import com.skidders.SigmaReborn;
import com.skidders.sigma.events.impl.Render2DEvent;
import com.skidders.sigma.module.Category;
import com.skidders.sigma.module.Module;
import com.skidders.sigma.module.settings.impl.BooleanSetting;
import com.skidders.sigma.module.settings.impl.ModeSetting;
import com.skidders.sigma.module.settings.impl.NumberSetting;
import dev.sxmurxy.renderutil.TemplateMod;
import dev.sxmurxy.renderutil.util.render.DrawHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ActiveMods extends Module {

    public ActiveMods() {
        super("ActiveMods", "Renders active mods", Category.GUI, GLFW.GLFW_KEY_V);
        registerSetting(new ModeSetting("Size", "The font size", "Normal", new String[]{"Normal", "Small", "Tiny"}));
        registerSetting(new BooleanSetting("Animations", "Scale in animation", true));
        registerSetting(new NumberSetting("Slider", "OMG, it's a slider!", 3, 1, 5, 1.0f));
        //font = font20;
    }

    @Override
    public void onEnable() {
        switch ((String) getSettingByName("Size").value) {
            //case "Normal" -> font = font20;
            //case "Small" -> font = font18;
            //case "Tiny" -> font = font14;
        }
    }

    @Subscribe
    public void on2D(Render2DEvent event) {
        if (mc.options.debugEnabled) {
            return;
        }

        DrawHelper.drawRoundedHorizontalGradientRect(5, 5 + 18, 120, 18, 5, new Color(255,255,255), new Color(192,30,66));

        float offsetY = 3;
        int screenWidth = mc.getWindow().getWidth();
        for (Module module : SigmaReborn.INSTANCE.moduleManager.modules) {
            float x, y = offsetY;

            //x = (float) screenWidth / 2 - font.getWidth(module.name) - 3;

            //font.drawString(module.name, x, y, new Color(255,255,255, 150).getRGB());

            offsetY += 12;
        }

        TemplateMod.onRender(event.matrixStack);
    }

}
