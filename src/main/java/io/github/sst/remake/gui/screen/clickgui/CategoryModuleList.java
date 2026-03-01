package io.github.sst.remake.gui.screen.clickgui;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.layout.FullWidthResizer;
import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.gui.framework.layout.GridLayoutVisitor;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.client.MinecraftClient;

public class CategoryModuleList extends ScrollablePanel {
    public final Category category;
    private final boolean animateCollapse;
    private float expandAnimationProgress = 1.0F;

    public CategoryModuleList(CategoryPanel parent, String id, int x, int y, int width, int height, Category category) {
        super(parent, id, x, y, width, height);
        this.category = category;
        parent.expandProgress = 1.0F;

        this.animateCollapse = true;
        this.setListening(false);

        this.rebuildModuleButtons();
    }

    public void rebuildModuleButtons() {
        int rowIndex = 0;

        for (Module module : Client.INSTANCE.moduleManager.getModulesByCategory(this.category)) {
            final Module currentModule = module;

            int transparentBackground = ColorHelper.applyAlpha(-3487030, 0.0F);
            ColorHelper buttonStyle = createModuleButtonStyle(currentModule, transparentBackground);

            Button moduleButton = new Button(
                    this.getContent(),
                    currentModule.getName() + "Button",
                    0,
                    rowIndex * 30,
                    this.getWidth(),
                    30,
                    buttonStyle,
                    currentModule.getName(),
                    FontUtils.HELVETICA_LIGHT_20
            );

            applyModuleButtonPadding(moduleButton, currentModule);

            this.getContent().addToList(moduleButton);

            moduleButton.onClick((component, mouseButton) -> {
                Button clickedButton = (Button) component;

                if (mouseButton == 0) {
                    currentModule.toggle();

                    ColorHelper updatedStyle = createModuleButtonStyle(currentModule, transparentBackground);
                    applyModuleButtonPadding(clickedButton, currentModule);

                    updatedStyle.setWidthAlignment(FontAlignment.LEFT);
                    clickedButton.setTextColor(updatedStyle);
                    return;
                }

                if (mouseButton == 1) {
                    CategoryPanel categoryPanel = (CategoryPanel) this.getParent();
                    categoryPanel.notifyModuleClickListeners(currentModule);
                }
            });

            moduleButton.addWidthSetter(new FullWidthResizer());
            rowIndex++;
        }

        this.getContent().accept(new GridLayoutVisitor(1));
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        CategoryPanel categoryPanel = (CategoryPanel) this.parent;
        float step = (float) (0.07F * (60.0 / (double) MinecraftClient.currentFps));

        float delta = !isAnimationEnabled() ? 0.0F : (this.animateCollapse ? -step : step);
        this.expandAnimationProgress = this.expandAnimationProgress + delta;
        this.expandAnimationProgress = Math.max(0.0F, Math.min(1.0F, this.expandAnimationProgress));

        categoryPanel.expandProgress = AnimationUtils.easeInOutQuad(this.expandAnimationProgress, 0.0F, 1.0F, 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();
        super.draw(partialTicks * ((CategoryPanel) this.parent).expandProgress);
    }

    public boolean isAnimationEnabled() {
        return false;
    }

    private static void applyModuleButtonPadding(Button button, Module module) {
        button.setTextOffsetX(module.isEnabled() ? 30 : 22);
    }

    private static ColorHelper createModuleButtonStyle(Module module, int transparentBackground) {
        boolean enabled = module.isEnabled();

        int background = enabled ? -14047489 : 1895167477;
        int hoverBackground = enabled ? -14042881 : transparentBackground;
        int textColor = enabled ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : ClientColors.DEEP_TEAL.getColor();

        ColorHelper style = new ColorHelper(background, hoverBackground).setTextColor(textColor);
        style.setWidthAlignment(FontAlignment.LEFT);
        return style;
    }
}