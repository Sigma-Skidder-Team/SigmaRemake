package com.skidders.sigma.screens.clickgui;

import com.skidders.SigmaReborn;
import com.skidders.sigma.module.Module;
import com.skidders.sigma.module.settings.Setting;
import com.skidders.sigma.module.settings.impl.BooleanSetting;
import com.skidders.sigma.module.settings.impl.NumberSetting;
import com.skidders.sigma.utils.misc.MouseHandler;
import com.skidders.sigma.utils.render.RenderUtil;
import com.skidders.sigma.utils.render.font.styled.StyledFontRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

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
        StyledFontRenderer.drawString(matrices, screen.light20, parent.name, x, y - 30, new Color(254, 254, 254));
        StyledFontRenderer.drawString(matrices, screen.light20, parent.desc, x + 12, y + 15, new Color(100, 100, 100));

        boolean mouseDown = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        if (!parent.settings.isEmpty()) {
            float offset = y + 35;
            for (Setting<?> setting : parent.settings) {
                //full setting bounds - x, offset - 2, width, 18
                StyledFontRenderer.drawString(matrices, screen.settingName, setting.name, x + 12, offset, Color.BLACK);

                if (RenderUtil.hovered(mouseX, mouseY, x, offset - 2, width, 18)) {
                    StyledFontRenderer.drawString(matrices, screen.settingSB, "§l" + setting.name, x + 7, y + height + 7, new Color(255, 255, 255, 127));
                    StyledFontRenderer.drawString(matrices, screen.settingS, setting.desc, x + 7 + screen.settingSB.getWidth(setting.name), y + height + 7, new Color(255, 255, 255, 127));
                }

                if (setting instanceof BooleanSetting sett) {
                    sett.value = sett.checkboxComponent.draw(mouseHandler, sett.value, mouseX, mouseY, x + width - 20, offset + 1.3f);
                } else if (setting instanceof NumberSetting numb) {
                    float min = numb.min;
                    float max = numb.max;
                    float val = numb.value.floatValue();
                    int point = numb.getDecimalPlaces();

                    float normalizedValue = (val - min) / (max - min);
                    float sliderWidth = normalizedValue * 50;

                    Color bgColor = new Color(215, 234, 254);
                    Color filledColor = new Color(59, 153, 253);

                    RenderUtil.drawRectangle(matrices, x + width - 66, offset + 5.5f, 50, 3.5f, bgColor);
                    RenderUtil.drawRectangle(matrices, x + width - 66, offset + 5.5f, sliderWidth, 3.5f, filledColor);

                    float circleX = x + width - 66 + sliderWidth;

                    boolean hoverSlider = RenderUtil.hovered(mouseX, mouseY, x + width - 69, offset + 2, 55, 10);
                    if (hoverSlider) {
                        int textOffset = val <= min + (max - min) * 0.07 ? -5 : 0;

                        StyledFontRenderer.drawString(matrices, screen.sliderValue, numb.value.toString(), x + width - 70 - screen.sliderValue.getWidth(numb.value.toString()) + textOffset, (float) (offset + 4.25), new Color(125, 125, 125));
                        if (mouseDown) {
                            double normalizedX = (mouseX - (x + width - 66)) / (50);
                            double newValue = min + normalizedX * (max - min);
                            newValue = Math.min(Math.max(newValue, min), max);
                            numb.value = newValue;
                        }
                    }

                    RenderUtil.drawCircle(circleX, offset + 7, 6, new Color(200, 200, 200   ));
                    RenderUtil.drawCircle(circleX, offset + 7, 5.5, new Color(254, 254, 254));
                }

                offset += 18;
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
