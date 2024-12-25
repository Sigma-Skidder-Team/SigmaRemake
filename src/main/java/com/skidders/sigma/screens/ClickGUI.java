package com.skidders.sigma.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.SigmaReborn;
import com.skidders.sigma.utils.render.GameRendererAccessor;
import com.skidders.sigma.module.Category;
import com.skidders.sigma.module.Module;
import com.skidders.sigma.utils.IMinecraft;
import com.skidders.sigma.utils.render.font.Renderer;
import com.skidders.sigma.utils.render.RenderUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ClickGUI extends Screen implements IMinecraft {

    public final Renderer moduleName = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Medium", 40);
    public final Renderer settingName = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 24);
    public final Renderer sliderValue = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 14);
    public final Renderer stringValue = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 18);
    public final Renderer settingS = SigmaReborn.INSTANCE.fontManager.getFont("SFUIDisplay-Regular", 17);
    public final Renderer settingSB = SigmaReborn.INSTANCE.fontManager.getFont("SFUIDisplay-Bold", 17);

    private final Renderer light25 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 25);
    public final Renderer light20 = SigmaReborn.INSTANCE.fontManager.getFont("HelveticaNeue-Light", 20);

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
    protected void init() {
        super.init();
        GameRenderer gameRenderer = mc.gameRenderer;

        if (gameRenderer instanceof GameRendererAccessor accessor) {
            accessor.sigmaRemake$invokeLoadShader(new Identifier("shaders/post/blur.json"));
        } else {
            throw new IllegalStateException("GameRenderer does not implement GameRendererAccessor");
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        mc.gameRenderer.disableShader();
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
        super.render(matrices, mouseX, mouseY, delta);

        for (Category category : Category.values()) {
            Point position = categoryPositions.get(category);
            if (position == null) continue;

            float xOffset = position.x;
            float yOffset = position.y;

            RenderUtil.drawRectangle(matrices, xOffset, yOffset, frameWidth, categoryHeight, new Color(250, 250, 250, 230));
            RenderUtil.drawRectangle(matrices, xOffset, yOffset + categoryHeight, frameWidth, frameHeight, new Color(250, 250, 250));
            light25.drawString(category.name, xOffset + 8, yOffset + 8, new Color(119, 121, 124).getRGB());

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
                light20.drawString(module.name, xOffset + (module.enabled ? 10 : 8), modOffset + 2,
                        module.enabled ? Color.WHITE.getRGB() : Color.BLACK.getRGB());

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
