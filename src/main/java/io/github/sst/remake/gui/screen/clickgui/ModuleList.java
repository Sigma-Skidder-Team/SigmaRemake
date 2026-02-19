package io.github.sst.remake.gui.screen.clickgui;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.gui.framework.layout.GridLayoutVisitor;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class ModuleList extends ScrollablePanel {
    public final Category category;
    private final List<Button> moduleButtons = new ArrayList<>();
    private final boolean animateBackward;
    private float animationProgress = 1.0F;

    public ModuleList(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, Category var7) {
        super(var1, var2, var3, var4, var5, var6);
        this.category = var7;
        ((CategoryPanel) var1).expandProgress = 1.0F;
        this.animateBackward = true;
        this.setListening(false);
        this.buildModuleButtons();
    }

    public void buildModuleButtons() {
        int var3 = 0;

        for (Module var5 : Client.INSTANCE.moduleManager.getModulesByCategory(this.category)) {
            int var9 = ColorHelper.applyAlpha(-3487030, 0.0F);
            ColorHelper var12 = new ColorHelper(!var5.isEnabled() ? 1895167477 : -14047489, !var5.isEnabled() ? var9 : -14042881)
                    .setTextColor(!var5.isEnabled() ? ClientColors.DEEP_TEAL.getColor() : ClientColors.LIGHT_GREYISH_BLUE.getColor());
            var12.setWidthAlignment(FontAlignment.LEFT);
            Button var13;
            this.getContent()
                    .addToList(
                            var13 = new Button(
                                    this.getContent(), var5.getName() + "Button", 0, var3 * 30, this.getWidth(), 30, var12, var5.getName(), FontUtils.HELVETICA_LIGHT_20
                            )
                    );
            if (!var5.isEnabled()) {
                var13.setTextOffsetX(22);
            } else {
                var13.setTextOffsetX(30);
            }

            this.moduleButtons.add(var13);
            var13.onClick(
                    (var3x, var4) -> {
                        Button var7 = (Button) var3x;
                        if (var4 != 0) {
                            if (var4 == 1) {
                                CategoryPanel var8 = (CategoryPanel) this.getParent();
                                var8.notifyModuleClickListeners(var5);
                            }
                        } else {
                            var5.toggle();
                            ColorHelper var9x = new ColorHelper(!var5.isEnabled() ? 1895167477 : -14047489, !var5.isEnabled() ? var9 : -14042881)
                                    .setTextColor(!var5.isEnabled() ? ClientColors.DEEP_TEAL.getColor() : ClientColors.LIGHT_GREYISH_BLUE.getColor());
                            if (!var5.isEnabled()) {
                                var7.setTextOffsetX(22);
                            } else {
                                var7.setTextOffsetX(30);
                            }

                            var9x.setWidthAlignment(FontAlignment.LEFT);
                            var7.setTextColor(var9x);
                        }
                    }
            );
            var13.addWidthSetter(new ModuleListResizer());
            var3++;
        }

        this.getContent().accept(new GridLayoutVisitor(1));
    }

    private float smoothStep() {
        return this.animationProgress * this.animationProgress * (3.0F - 2.0F * this.animationProgress);
    }

    private float easeInOutQuad(float var1, float var2, float var3, float var4) {
        var1 /= var4 / 2.0F;
        if (!(var1 < 1.0F)) {
            var1--;
            return -var3 / 2.0F * (var1 * (var1 - 2.0F) - 1.0F) + var2;
        } else {
            return var3 / 2.0F * var1 * var1 + var2;
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        CategoryPanel var5 = (CategoryPanel) this.parent;
        float var6 = (float) (0.07F * (60.0 / (double) MinecraftClient.currentFps));
        this.animationProgress = this.animationProgress + (!this.shouldAnimate() ? 0.0F : (!this.animateBackward ? var6 : -var6));
        this.animationProgress = Math.max(0.0F, Math.min(1.0F, this.animationProgress));
        var5.expandProgress = this.easeInOutQuad(this.animationProgress, 0.0F, 1.0F, 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();
        super.draw(partialTicks * ((CategoryPanel) this.parent).expandProgress);
    }

    public boolean shouldAnimate() {
        return false;
    }

    public int getButtonOffsetForModule(Module var1) {
        int var4 = 0;

        for (Button var6 : this.moduleButtons) {
            var4++;
            if (var6.getName().equals(var1.getName() + "Button")) {
                break;
            }
        }

        return var4 * 30;
    }
}
