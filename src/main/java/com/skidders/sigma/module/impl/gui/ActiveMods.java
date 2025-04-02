package com.skidders.sigma.module.impl.gui;

import com.skidders.SigmaReborn;
import com.skidders.sigma.event.impl.FastRender2DEvent;
import com.skidders.sigma.module.Category;
import com.skidders.sigma.module.Module;
import com.skidders.sigma.util.client.modules.ModuleNameLengthComparator;
import com.skidders.sigma.module.settings.impl.BooleanSetting;
import com.skidders.sigma.module.settings.impl.ModeSetting;
import com.skidders.sigma.module.settings.impl.NumberSetting;
import com.skidders.sigma.screen.Animation;
import com.skidders.sigma.util.client.events.Listen;
import com.skidders.sigma.util.client.interfaces.IFonts;
import com.skidders.sigma.util.client.interfaces.ITextures;
import com.skidders.sigma.util.client.render.ColorUtil;
import com.skidders.sigma.util.client.render.font.FontUtil;
import com.skidders.sigma.util.client.render.image.ImageUtil;
import com.skidders.sigma.util.system.math.QuadraticEasing;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.TrueTypeFont;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActiveMods extends Module {

    private final HashMap<Module, Animation> animations = new HashMap<>();
    private final List<Module> activeModules = new ArrayList<>();
    private TrueTypeFont font = IFonts.JelloLightFont20;
    private int offsetY = 0;

    public ActiveMods() {
        super("ActiveMods", "Renders active mods", Category.GUI, GLFW.GLFW_KEY_V);
        registerSetting(new ModeSetting("Size", "The font size", "Normal", new String[]{"Normal", "Small", "Tiny"}));
        registerSetting(new BooleanSetting("Animations", "Scale in animation", true));
        registerSetting(new NumberSetting("Slider", "OMG, it's a slider!", 3, 1, 5, 1.0f));
        setKey(GLFW.GLFW_KEY_Y);
    }

    @Override
    public void onInit() {
        activeModules.clear();

        for (Module module : SigmaReborn.INSTANCE.moduleManager.getList()) {
            if (module.getCategory() != Category.GUI) {
                activeModules.add(module);
                animations.put(module, new Animation(150, 150, Animation.Direction.BACKWARDS));

                if (!(Boolean) getSettingByName("Animations").value) {
                    continue;
                }
                animations.get(module).changeDirection(!module.isEnabled() ? Animation.Direction.BACKWARDS : Animation.Direction.FORWARDS);
            }
        }

        activeModules.sort(new ModuleNameLengthComparator());
    }

    @Override
    public void onEnable() {
        switch ((String) getSettingByName("Size").value) {
            case "Normal" -> font = IFonts.JelloLightFont20;
            case "Small" -> font = IFonts.JelloLightFont18;
            case "Tiny" -> font = IFonts.JelloLightFont14;
        }
    }

    @Listen
    public void on2D(FastRender2DEvent event) {
        for (Module module : animations.keySet()) {
            if ((Boolean) getSettingByName("Animations").value) {
                animations.get(module).changeDirection(!module.isEnabled() ? Animation.Direction.BACKWARDS : Animation.Direction.FORWARDS);
            }
        }

        if (mc.options.debugEnabled) {
            return;
        }

        if (!mc.options.hudHidden) {
            int margin = 10;
            float scale = 1;
            int screenWidth = mc.getWindow().getWidth();
            int screenHeight = margin - 4;

            if (font == IFonts.JelloLightFont14) {
                margin -= 3;
            }

            if (mc.options.debugEnabled) {
                screenHeight = (int) ((double) (SigmaReborn.INSTANCE.screenHandler.rightText.size() * 9) * mc.getWindow().getScaleFactor() + 7.0);
            }

            int color = ColorUtil.applyAlpha(-1, 0.95F);
            for (Module module : this.activeModules) {
                float animationScale = 1.0F;
                float transparency = 1.0F;

                if (!(Boolean) getSettingByName("Animations").value) {
                    if (!module.isEnabled()) {
                        continue;
                    }
                } else {
                    Animation animation = this.animations.get(module);
                    if (animation.calcPercent() == 0.0F) {
                        continue;
                    }

                    transparency = animation.calcPercent();
                    animationScale = 0.86F + 0.14F * transparency;
                }

                String moduleName = module.getName();
                GL11.glAlphaFunc(519, 0.0F);
                GL11.glPushMatrix();

                int xPos = screenWidth - margin - font.getWidth(moduleName) / 2;
                int yPos = screenHeight + 12;

                GL11.glTranslatef((float) xPos, (float) yPos, 0.0F);
                GL11.glScalef(animationScale, animationScale, 1.0F);
                GL11.glTranslatef((float) (-xPos), (float) (-yPos), 0.0F);

                float scaleFactor = (float) Math.sqrt(Math.min(1.2F, (float) font.getWidth(moduleName) / 63.0F));
                ImageUtil.drawImage((float) screenWidth - (float) font.getWidth(moduleName) * 1.5F - (float) margin - 20.0F, (float) (screenHeight - 20), (float) font.getWidth(moduleName) * 3.0F, font.getHeight() + scale + 40, ITextures.shadow, ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.36F * transparency * scaleFactor));
                FontUtil.drawString(
                        font, (float) (screenWidth - margin - font.getWidth(moduleName)), (float) screenHeight, moduleName, transparency != 1.0F ? ColorUtil.applyAlpha(-1, transparency * 0.95F) : color
                );
                GL11.glPopMatrix();
                screenHeight = (int) ((float) screenHeight + (font.getHeight() + scale) * QuadraticEasing.easeInOutQuad(transparency, 0.0F, 1.0F, 1.0F));
            }

            offsetY = screenHeight;
        }
    }

}
