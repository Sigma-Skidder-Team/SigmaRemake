package com.skidders.sigma.screens;

import com.skidders.SigmaReborn;
import com.skidders.sigma.module.Module;
import com.skidders.sigma.module.settings.Setting;
import com.skidders.sigma.module.settings.impl.BooleanSetting;
import com.skidders.sigma.module.settings.impl.ModeSetting;
import com.skidders.sigma.utils.misc.MouseHandler;
import com.skidders.sigma.utils.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class SettingGUI {
    private final Module parent;
    private final ClickGUI screen;
    private final MouseHandler mouseHandler;

    public SettingGUI(Module parent) {
        this.parent = parent;
        this.screen = SigmaReborn.INSTANCE.screenProcessor.clickGUI;
        this.mouseHandler = new MouseHandler(MinecraftClient.getInstance().getWindow().getHandle());
    }

    public void draw(MatrixStack matrices, double mouseX, double mouseY) {
        float width = 210, height = 240;
        float x = (float) screen.width / 2 - width / 2,
                y = (float) screen.height / 2 - height / 2;

        RenderUtil.drawRectangle(matrices, 0, 0, screen.width, screen.height, new Color(0, 0, 0, 150));

        RenderUtil.drawRectangle(matrices, x, y, width, height, new Color(254, 254, 254));
        screen.moduleName.drawString(parent.name, x, y - 30, new Color(254, 254, 254));
        screen.light20.drawString(parent.desc, x + 12, y + 15, new Color(100, 100, 100));

        if (!parent.settings.isEmpty()) {
            float yOffset = y + 35;
            for (Setting<?> setting : parent.settings) {
                //full setting bounds - x, yOffset - 2, width, 18
                screen.settingName.drawString(setting.name, x + 12, yOffset, Color.BLACK);
                if (RenderUtil.hovered(mouseX, mouseY, x, yOffset - 2, width, 18)) {
                    screen.settingSB.drawString("§l" + setting.name, x + 7, y + height + 7, new Color(255, 255, 255, 127));
                    screen.settingS.drawString(setting.desc, x + 7 + screen.settingSB.getWidth(setting.name), y + height + 7, new Color(255, 255, 255, 127));
                }

                if (setting instanceof BooleanSetting bruh) {
                    bruh.value = bruh.checkboxComponent.draw(mouseHandler, bruh.value, mouseX, mouseY, x + width - 20, yOffset + 1.3f);
                } else if (setting instanceof ModeSetting bruh) {

                }

                yOffset += 18;
            }
        }
    }

    public boolean click(double mouseX, double mouseY, int button) {
        float width = 210, height = 240;
        float x = (float) screen.width / 2 - width / 2,
                y = (float) screen.height / 2 - height / 2;

        if (button == 0) {
            if (RenderUtil.hovered(mouseX, mouseY, x, y, width, height)) {

            } else {
                screen.settingGUI = null;
            }
        }

        return true;
    }


}
