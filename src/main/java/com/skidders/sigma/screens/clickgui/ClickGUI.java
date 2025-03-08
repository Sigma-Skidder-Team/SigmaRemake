package com.skidders.sigma.screens.clickgui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.SigmaReborn;
import com.skidders.sigma.utils.render.font.styled.StyledFont;
import com.skidders.sigma.utils.render.font.styled.StyledFontRenderer;
import com.skidders.sigma.module.Category;
import com.skidders.sigma.module.Module;
import com.skidders.sigma.utils.IMinecraft;
import com.skidders.sigma.utils.render.RenderUtil;
import com.skidders.sigma.utils.render.interfaces.IFontRegistry;
import com.skidders.sigma.utils.render.shader.ShaderRenderUtil;
import com.skidders.sigma.utils.render.shader.shader.impl.BlurShader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ClickGUI extends Screen implements IMinecraft {

    public final StyledFont moduleName = IFontRegistry.Medium40;
    public final StyledFont settingName = IFontRegistry.Light24;
    public final StyledFont sliderValue = IFontRegistry.Light14;
    public final StyledFont stringValue = IFontRegistry.Light18;
    public final StyledFont settingS = IFontRegistry.SRegular17;
    public final StyledFont settingSB = IFontRegistry.SBold17;

    private final StyledFont light25 = IFontRegistry.Light25;
    public final StyledFont light20 = IFontRegistry.Light20;

    private final Map<Category, Point> categoryPositions = new HashMap<>();
    private final int moduleHeight = 14;

    private Category draggingCategory = null;
    private int dragOffsetX = 0, dragOffsetY = 0;

    private final float frameWidth = 110, frameHeight = 120, categoryHeight = 27;

    private Module hoveredModule = null;

    public SettingGUI settingGUI;

    public ClickGUI(String title) {
        super(Text.of(title));

        float xOffsetStart = 7, yOffsetStart = 10;
        float xOffset = xOffsetStart, yOffset = yOffsetStart;
        int count = 0, columns = 4;

        for (Category category : Category.values()) {
            categoryPositions.put(category, new Point((int) xOffset, (int) yOffset));
            xOffset += frameWidth + 5;

            if (++count % columns == 0) {
                xOffset = xOffsetStart;
                yOffset += categoryHeight + frameWidth + yOffsetStart + 5;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != -1) { //left mouse button
            if (button == 0) {
                for (Map.Entry<Category, Point> entry : categoryPositions.entrySet()) {
                    Category category = entry.getKey();
                    Point position = entry.getValue();

                    if (mouseX >= position.x && mouseX <= position.x + frameWidth &&
                            mouseY >= position.y && mouseY <= position.y + categoryHeight) {
                        draggingCategory = category;
                        dragOffsetX = (int) (mouseX - position.x);
                        dragOffsetY = (int) (mouseY - position.y);

                        return true;
                    }
                }
            }

            for (Category category : Category.values()) {
                Point position = categoryPositions.get(category);
                if (position == null) continue;

                float xOffset = position.x;
                float yOffset = position.y;

                float modOffset = yOffset + categoryHeight;
                for (Module module : SigmaReborn.INSTANCE.moduleManager.getModulesByCategory(category)) {
                    if (RenderUtil.hovered(mouseX, mouseY, xOffset, modOffset, frameWidth, moduleHeight)) {
                        if (button == 0) {
                            hoveredModule = module;
                        } else if (button == 1) {
                            settingGUI = new SettingGUI(module);
                        }
                    }

                    modOffset += moduleHeight;
                }
            }
        }

        if (settingGUI != null) {
            settingGUI.click(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) { //left mouse button released
            if (draggingCategory != null) {
                draggingCategory = null; //stop dragging
                return true;
            }

            if (hoveredModule != null) {
                Point position = categoryPositions.get(Category.valueOf(hoveredModule.category.name()));
                float xOffset = position.x;
                float yOffset = position.y;

                float modOffset = yOffset + categoryHeight + SigmaReborn.INSTANCE.moduleManager.getModulesByCategory(hoveredModule.category)
                        .indexOf(hoveredModule) * moduleHeight;
                if (RenderUtil.hovered(mouseX, mouseY, xOffset, modOffset, frameWidth, moduleHeight)) {
                    hoveredModule.setEnabled(!hoveredModule.enabled);
                }
                hoveredModule = null; //reset hoveredModule
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && draggingCategory != null) { //left mouse button dragging
            Point position = categoryPositions.get(draggingCategory);
            if (position != null) {
                position.setLocation(mouseX - dragOffsetX, mouseY - dragOffsetY);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

        BlurShader.registerRenderCall(() -> {
            ShaderRenderUtil.drawRect(0, 0, width, height, new Color(255, 255, 255, 150));
        });
        BlurShader.draw(5);

        for (Category category : Category.values()) {
            Point position = categoryPositions.get(category);
            if (position == null) continue;

            float xOffset = position.x;
            float yOffset = position.y;

            RenderUtil.drawRectangle(matrices, xOffset, yOffset, frameWidth, categoryHeight, new Color(250, 250, 250, 230));
            RenderUtil.drawRectangle(matrices, xOffset, yOffset + categoryHeight, frameWidth, frameHeight, new Color(250, 250, 250));
            StyledFontRenderer.drawString(matrices, light25, category.categoryName, xOffset + 8, yOffset + 8, new Color(119, 121, 124));

            float modOffset = yOffset + categoryHeight;
            for (Module module : SigmaReborn.INSTANCE.moduleManager.getModulesByCategory(category)) {
                if (modOffset + moduleHeight > yOffset + categoryHeight + frameHeight) {
                    int scissorHeight = (int) (yOffset + categoryHeight + frameHeight - modOffset);
                    RenderSystem.enableScissor((int) xOffset, (int) modOffset, (int) frameWidth, scissorHeight);
                }

                boolean hover = RenderUtil.hovered(mouseX, mouseY, xOffset, modOffset, frameWidth, moduleHeight);
                boolean mouse = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
                RenderUtil.drawRectangle(matrices, xOffset, modOffset, frameWidth, moduleHeight,
                        module.enabled ? hover ? mouse ? new Color(41, 193, 255) : new Color(41, 182, 255) : new Color(41, 166, 255)
                                : hover ? mouse ? new Color(221, 221, 221) : new Color(231, 231, 231) : new Color(250, 250, 250));

                StyledFontRenderer.drawString(matrices, light20, module.name, xOffset + (module.enabled ? 10 : 8), modOffset + 2,
                        module.enabled ? Color.WHITE : Color.BLACK);

                modOffset += moduleHeight;

                if (modOffset + moduleHeight > yOffset + categoryHeight + frameHeight) {
                    RenderSystem.disableScissor();
                }
            }
        }

        if (settingGUI != null) {
            settingGUI.draw(matrices, mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && settingGUI != null) {
            settingGUI = null;
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
