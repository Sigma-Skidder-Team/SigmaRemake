package io.github.sst.remake.gui.element.impl.cgui.config;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.impl.TextField;
import io.github.sst.remake.gui.panel.AnimatedIconPanel;
import io.github.sst.remake.util.io.audio.SoundUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;

public class ProfileGroup extends AnimatedIconPanel {
    public CustomGuiScreen buttonList;
    public AnimationUtils field21264;
    public AnimationUtils field21265;
    public AnimationUtils animation;
    public TextField profileName;
    public final int field21270;
    public final int field21271;
    public boolean field21272 = false;

    public ProfileGroup(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.field21270 = (int) ((float) var5 * 0.8F);
        this.field21271 = var6;

        ColorHelper var11 = ColorHelper.DEFAULT_COLOR.clone();
        var11.setPrimaryColor(-11371052);
        var11.setSecondaryColor(-12096331);
        var11.setTextColor(ClientColors.LIGHT_GREYISH_BLUE.getColor());
        ColorHelper var12 = ColorHelper.DEFAULT_COLOR.clone();
        var12.setPrimaryColor(-3254955);
        var12.setSecondaryColor(-4700859);
        var12.setTextColor(ClientColors.LIGHT_GREYISH_BLUE.getColor());
        this.addToList(this.buttonList = new EditButton(this, "edit", var5 - this.field21270, 0, this.field21270, var6));
        ConfigButton var13;
        this.buttonList.addToList(var13 = new ConfigButton(this.buttonList, "rename", 0, 0, this.field21270 / 2, var6, var11, "Rename"));
        ConfigButton deleteButton;
        this.buttonList.addToList(deleteButton = new ConfigButton(this.buttonList, "remove", this.field21270 / 2, 0, this.field21270 / 2, var6, var12, "Delete"));
        this.buttonList.setHovered(false);
        var13.setFont(FontUtils.HELVETICA_LIGHT_18);
        deleteButton.setFont(FontUtils.HELVETICA_LIGHT_18);
        var13.addWidthSetter((var0, var1x) -> var0.setWidth(Math.round((float) var1x.getWidth() / 2.0F)));
        deleteButton.addWidthSetter((var0, var1x) -> {
            var0.setX(Math.round((float) var1x.getWidth() / 2.0F));
            var0.setWidth(Math.round((float) var1x.getWidth() / 2.0F));
        });
        var13.onClick((var1x, var2x) -> {
            this.field21265.changeDirection(AnimationUtils.Direction.BACKWARDS);
            this.profileName.setSelfVisible(true);
            this.profileName.method13148();
        });
        this.buttonList.setWidth(0);
        this.buttonList.setTranslateX(this.field21270);
        this.field21264 = new AnimationUtils(100, 100, AnimationUtils.Direction.BACKWARDS);
        this.field21265 = new AnimationUtils(290, 290, AnimationUtils.Direction.BACKWARDS);
        this.animation = new AnimationUtils(200, 100, AnimationUtils.Direction.BACKWARDS);
        this.onClick((var1x, var2x) -> {
            if (var2x != 1) {
                this.field21265.changeDirection(AnimationUtils.Direction.BACKWARDS);
                if (this.field21265.calcPercent() == 0.0F) {
                    SoundUtils.play("switch");
                    ConfigScreen var5x = (ConfigScreen) this.getParent().getParent().getParent();
                    var5x.addRunnable(() -> var5x.method13615());
                }
            } else {
                this.field21265.changeDirection(AnimationUtils.Direction.FORWARDS);
            }
        });
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (!this.profileName.isFocused() && this.profileName.isSelfVisible()) {
            this.profileName.setSelfVisible(false);
            this.profileName.setFocused(false);
        }

        this.field21264.changeDirection(this.isMouseOverComponent(mouseX, mouseY) ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        if (!this.isMouseOverComponent(mouseX, mouseY)) {
            this.field21265.changeDirection(AnimationUtils.Direction.BACKWARDS);
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        if (this.animation.calcPercent() == 1.0F && !this.field21272) {
            this.field21272 = true;
            ConfigScreen var4 = (ConfigScreen) this.getParent().getParent().getParent();
            var4.addRunnable(() -> var4.method13615());
        }

        float var8 = VecUtils.interpolate(this.animation.calcPercent(), 0.1, 0.81, 0.14, 1.0);
        this.setHeight(Math.round((1.0F - var8) * (float) this.field21271));
        partialTicks *= 1.0F - this.animation.calcPercent();
        float var5 = VecUtils.interpolate(this.field21265.calcPercent(), 0.28, 1.26, 0.33, 1.04);
        if (this.field21265.getDirection().equals(AnimationUtils.Direction.BACKWARDS)) {
            var5 = AnimationUtils.calculateBackwardTransition(this.field21265.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        this.buttonList.setHovered(this.field21265.calcPercent() == 1.0F);
        this.buttonList.setWidth(Math.max(0, (int) ((float) this.field21270 * var5)));
        this.buttonList.setTranslateX((int) ((float) this.field21270 * (1.0F - var5)));
        ScissorUtils.startScissor(this);
        float var6 = this.isMouseDownOverComponent() && this.field21265.getDirection().equals(AnimationUtils.Direction.BACKWARDS) ? 0.03F : 0.0F;
        RenderUtils.drawRoundedRect2(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.04F * this.field21264.calcPercent() + var6)
        );

        this.profileName.setTranslateX(Math.round(-var5 * (float) this.width));
        super.draw(partialTicks);
        ScissorUtils.restoreScissor();
    }
}
