package io.github.sst.remake.gui.element.impl.cgui.config;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.gui.element.impl.TextButton;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;

public class ConfigGroup extends Element {
    public AnimationUtils field20703 = new AnimationUtils(300, 200, AnimationUtils.Direction.BACKWARDS);
    private final int field20704;
    private final ScrollableContentPanel field20705;

    public ConfigGroup(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, 0, ColorHelper.DEFAULT_COLOR, false);
        TextButton blankButton;
        this.addToList(
                blankButton = new TextButton(this, "blankButton", 25, 0, FontUtils.HELVETICA_LIGHT_20.getWidth("Blank"), 30, ColorHelper.DEFAULT_COLOR, "Blank", FontUtils.HELVETICA_LIGHT_20)
        );
        blankButton.onClick((var1x, var2x) -> {
            ConfigScreen var5x = (ConfigScreen) this.getParent();
            var5x.method13612();
        });
        TextButton var10;
        this.addToList(
                var10 = new TextButton(
                        this,
                        "dupeButton",
                        var5 - 25 - FontUtils.HELVETICA_LIGHT_20.getWidth("Duplicate"),
                        0,
                        FontUtils.HELVETICA_LIGHT_20.getWidth("Duplicate"),
                        30,
                        ColorHelper.DEFAULT_COLOR,
                        "Duplicate",
                        FontUtils.HELVETICA_LIGHT_20
                )
        );
        var10.onClick((var1x, var2x) -> {
            ConfigScreen var5x = (ConfigScreen) this.getParent();
            var5x.method13610();
        });
        this.addToList(this.field20705 = new ScrollableContentPanel(this, "defaultProfiles", 0, 40, var5, var6 - 40));
        this.field20704 = var6;
    }

    public void method13118(boolean var1) {
        this.field20705.setSelfVisible(!var1);
    }

    public void method13119(boolean var1) {
        this.field20703.changeDirection(!var1 ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
    }

    public boolean method13120() {
        return this.field20703.getDirection() == AnimationUtils.Direction.FORWARDS;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        float var4 = VecUtils.interpolate(this.field20703.calcPercent(), 0.1, 0.81, 0.14, 1.0);
        if (this.field20703.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            var4 = VecUtils.interpolate(this.field20703.calcPercent(), 0.61, 0.01, 0.87, 0.16);
        }

        this.setHeight((int) ((float) this.field20704 * var4));
        if (this.field20703.calcPercent() != 0.0F) {
            RenderUtils.drawImage(
                    (float) this.x,
                    (float) (this.y + this.height),
                    (float) this.width,
                    50.0F,
                    Resources.shadowBottomPNG,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.field20703.calcPercent() * partialTicks * 0.3F)
            );
            ScissorUtils.startScissor(this);
            RenderUtils.drawRoundedRect2(
                    (float) this.x, (float) this.y, (float) this.width, (float) this.height, ColorHelper.applyAlpha(-723724, partialTicks)
            );

            super.draw(partialTicks);
            ScissorUtils.restoreScissor();
        }
    }
}
