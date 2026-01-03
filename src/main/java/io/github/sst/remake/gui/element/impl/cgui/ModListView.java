package io.github.sst.remake.gui.element.impl.cgui;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.element.impl.Button;
import io.github.sst.remake.gui.element.impl.drop.GridLayoutVisitor;
import io.github.sst.remake.gui.interfaces.Animatable;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class ModListView extends ScrollableContentPanel implements Animatable {
    public final Category category;
    private final List<Button> field21215 = new ArrayList<>();
    private final boolean field21217;
    private float field21218 = 1.0F;

    public ModListView(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, Category var7) {
        super(var1, var2, var3, var4, var5, var6);
        this.category = var7;
        ((CategoryPanel) var1).field21195 = 1.0F;
        this.field21217 = true;
        this.setListening(false);
        this.method13511();
    }

    public void method13511() {
        int var3 = 0;

        for (Module var5 : Client.INSTANCE.moduleManager.getModulesByCategory(this.category)) {
            int var9 = ColorHelper.applyAlpha(-3487030, 0.0F);
            ColorHelper var12 = new ColorHelper(!var5.isEnabled() ? 1895167477 : -14047489, !var5.isEnabled() ? var9 : -14042881)
                    .setTextColor(!var5.isEnabled() ? ClientColors.DEEP_TEAL.getColor() : ClientColors.LIGHT_GREYISH_BLUE.getColor());
            var12.setWidthAlignment(FontAlignment.LEFT);
            Button var13;
            this.getButton()
                    .addToList(
                            var13 = new Button(
                                    this.getButton(), var5.getName() + "Button", 0, var3 * 30, this.getWidth(), 30, var12, var5.getName(), FontUtils.HELVETICA_LIGHT_20
                            )
                    );
            if (!var5.isEnabled()) {
                var13.method13034(22);
            } else {
                var13.method13034(30);
            }

            this.field21215.add(var13);
            var13.onClick(
                    (var3x, var4) -> {
                        Button var7 = (Button) var3x;
                        if (var4 != 0) {
                            if (var4 == 1) {
                                CategoryPanel var8 = (CategoryPanel) this.getParent();
                                var8.method13508(var5);
                            }
                        } else {
                            var5.toggle();
                            ColorHelper var9x = new ColorHelper(!var5.isEnabled() ? 1895167477 : -14047489, !var5.isEnabled() ? var9 : -14042881)
                                    .setTextColor(!var5.isEnabled() ? ClientColors.DEEP_TEAL.getColor() : ClientColors.LIGHT_GREYISH_BLUE.getColor());
                            if (!var5.isEnabled()) {
                                var7.method13034(22);
                            } else {
                                var7.method13034(30);
                            }

                            var9x.setWidthAlignment(FontAlignment.LEFT);
                            var7.setTextColor(var9x);
                        }
                    }
            );
            var13.addWidthSetter(new ModListViewSize());
            var3++;
        }

        this.getButton().accept(new GridLayoutVisitor(1));
    }

    private float method13523() {
        return this.field21218 * this.field21218 * (3.0F - 2.0F * this.field21218);
    }

    private float method13524(float var1, float var2, float var3, float var4) {
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
        this.field21218 = this.field21218 + (!this.shouldAnimate() ? 0.0F : (!this.field21217 ? var6 : -var6));
        this.field21218 = Math.max(0.0F, Math.min(1.0F, this.field21218));
        var5.field21195 = this.method13524(this.field21218, 0.0F, 1.0F, 1.0F);
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();
        super.draw(partialTicks * ((CategoryPanel) this.parent).field21195);
    }

    @Override
    public boolean shouldAnimate() {
        return false;
    }

    public int method13529(Module var1) {
        int var4 = 0;

        for (Button var6 : this.field21215) {
            var4++;
            if (var6.getName().equals(var1.getName() + "Button")) {
                break;
            }
        }

        return var4 * 30;
    }
}
