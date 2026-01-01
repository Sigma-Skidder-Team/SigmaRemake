package io.github.sst.remake.gui.element.impl.alts;

import io.github.sst.remake.alt.Account;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.impl.alert.LoadingIndicator;
import io.github.sst.remake.gui.panel.AnimatedIconPanel;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;

public class AccountUI extends AnimatedIconPanel {
    public Account selectedAccount;
    private final LoadingIndicator loadingIndicator;
    private boolean refreshing = false;

    private final AnimationUtils field20803 = new AnimationUtils(814, 114, AnimationUtils.Direction.BACKWARDS);
    private float loadingProgress = 0.0F;
    public AnimationUtils field20805 = new AnimationUtils(800, 300, AnimationUtils.Direction.BACKWARDS);
    private int errorState = 0;
    private int lastErrorState = 0;
    private int color = ColorHelper.shiftTowardsOther(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 20.0F);

    public AccountUI(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Account var7) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.selectedAccount = var7;
        this.addToList(this.loadingIndicator = new LoadingIndicator(this, "loading", var5 - 50, 35, 30, 30));
        this.loadingIndicator.setHovered(false);
    }

    public void method13166(boolean var1) {
        this.method13167(var1, false);
    }

    public void method13167(boolean var1, boolean var2) {
        this.field20803.changeDirection(!var1 ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
        if (var2) {
            this.field20803.updateStartTime(1.0F);
        }
    }

    public boolean method13168() {
        return this.field20803.getDirection() == AnimationUtils.Direction.FORWARDS;
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();
        this.color = ColorHelper.shiftTowardsOther(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 2.0F);
        int var4 = ((ScrollableContentPanel) this.parent.getParent()).getScrollOffset();
        int var5 = Math.max(0, this.y - var4);
        int var6 = Math.max(0, this.height + Math.min(100, this.y - var4 - var5));
        float var7 = (float) Math.min(50, var6) / 50.0F;
        int var8 = this.getParent().getParent().getHeight() + this.getParent().getParent().getAbsoluteY();
        int var9 = 0;
        var5 += var4;
        if (var5 - var4 <= var8) {
            if (var7 != 0.0F) {
                RenderUtils.drawFloatingPanelScaled(
                        this.x,
                        var5,
                        this.width,
                        Math.max(20, var6),
                        ColorHelper.applyAlpha(!this.isMouseDownOverComponent() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.color, var7)
                );
                ScissorUtils.startScissor(this.x, var5, this.x + this.width + 20, var5 + var6, true);
                if (this.selectedAccount != null) {
                    this.drawAccountHead();
                    this.drawAccountUsername();
                    this.method13171(var7);
                    if (this.field20803.calcPercent() > 0.0F && var6 > 55) {
                        RenderUtils.drawImage(
                                (float) (this.x + this.getWidth()),
                                (float) var5 + (float) (26 * var6) / 100.0F,
                                18.0F * this.field20803.calcPercent() * (float) var6 / 100.0F,
                                (float) (47 * var6) / 100.0F,
                                Resources.selectPNG,
                                !this.isMouseDownOverComponent() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.color
                        );
                    }

                    super.draw(partialTicks * var7);
                    ScissorUtils.restoreScissor();
                }
            }
        }
    }

    public void drawAccountHead() {
        RenderUtils.drawImage(
                (float) (this.x + 13), (float) (this.y + 13), 75.0F, 75.0F, this.selectedAccount.setHeadTexture(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), true
        );
        RenderUtils.drawPanelShadow((float) (this.x + 13), (float) (this.y + 13), 75.0F, 75.0F, 20.0F, 1.0F);
        RenderUtils.drawImage(
                (float) (this.x + 1),
                (float) this.y,
                100.0F,
                100.0F,
                Resources.cerclePNG,
                !this.isMouseDownOverComponent() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.color
        );
    }

    public void drawAccountUsername() {
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25, (float) (this.x + 110), (float) (this.y + 18), this.selectedAccount.name, ClientColors.DEEP_TEAL.getColor()
        );

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.x + 110),
                (float) (this.y + 50),
                "Token: " + "asdddddddddddddddddddddddddddddddddddddddddddd".replaceAll(".", Character.toString('·')),
                ClientColors.MID_GREY.getColor()
        );
    }

    public void method13171(float var1) {
        this.loadingProgress = this.loadingProgress + (this.refreshing ? 0.33333334F : -0.33333334F);
        this.loadingProgress = Math.min(1.0F, Math.max(0.0F, this.loadingProgress));
        this.errorState = Math.max(0, this.errorState - 1);
        float var4 = this.errorState <= 20 ? 20.0F : -20.0F;
        float var5 = (float) this.errorState >= var4 && (float) this.errorState <= (float) this.lastErrorState - var4 ? 1.0F : (float) this.errorState % var4 / var4;
        RenderUtils.drawImage(
                (float) (this.x + this.width - 45),
                (float) (this.y + 42),
                17.0F,
                17.0F,
                Resources.errorsPNG,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5 * var1)
        );
        RenderUtils.drawImage(
                (float) (this.x + this.width - 45),
                (float) (this.y + 45),
                17.0F,
                13.0F,
                Resources.activePNG,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.loadingProgress * var1)
        );
    }

    public void setAccountListRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    public void setErrorState(int errorCode) {
        this.errorState = errorCode;
        this.lastErrorState = errorCode;
    }

    public void setLoadingIndicator(boolean isLoading) {
        this.loadingIndicator.setHovered(isLoading);
    }
}
