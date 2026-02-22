package io.github.sst.remake.gui.screen.altmanager;

import io.github.sst.remake.data.alt.Account;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.internal.LoadingIndicator;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;

public class AccountListEntry extends Widget {
    public Account selectedAccount;
    private final LoadingIndicator loadingIndicator;
    private boolean refreshing = false;

    private final AnimationUtils selectedIndicatorAnim = new AnimationUtils(814, 114, AnimationUtils.Direction.FORWARDS);
    private float refreshFade = 0.0F;
    public AnimationUtils entrySlideAnim = new AnimationUtils(800, 300, AnimationUtils.Direction.FORWARDS);
    private int errorBlinkTicks = 0;
    private int errorBlinkMaxTicks = 0;
    private int hoverColor = ColorHelper.shiftTowardsOther(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 20.0F);

    public AccountListEntry(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, Account var7) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.selectedAccount = var7;
        this.addToList(this.loadingIndicator = new LoadingIndicator(this, "loading", var5 - 50, 35, 30, 30));
        this.loadingIndicator.setHovered(false);
    }

    public void setSelected(boolean selected) {
        this.setSelected(selected, false);
    }

    public void setSelected(boolean selected, boolean instant) {
        this.selectedIndicatorAnim.changeDirection(!selected ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        if (instant) {
            this.selectedIndicatorAnim.updateStartTime(1.0F);
        }
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();
        this.hoverColor = ColorHelper.shiftTowardsOther(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 2.0F);
        int var4 = ((ScrollablePanel) this.parent.getParent()).getScrollOffset();
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
                        ColorHelper.applyAlpha(!this.isMouseDownOverComponent() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.hoverColor, var7)
                );
                ScissorUtils.startScissor(this.x, var5, this.x + this.width + 20, var5 + var6, true);
                if (this.selectedAccount != null) {
                    this.drawAccountHead();
                    this.drawAccountUsername();
                    this.drawStatusIcons(var7);
                    if (this.selectedIndicatorAnim.calcPercent() > 0.0F && var6 > 55) {
                        RenderUtils.drawImage(
                                (float) (this.x + this.getWidth()),
                                (float) var5 + (float) (26 * var6) / 100.0F,
                                18.0F * this.selectedIndicatorAnim.calcPercent() * (float) var6 / 100.0F,
                                (float) (47 * var6) / 100.0F,
                                Resources.SELECTED_ICON,
                                !this.isMouseDownOverComponent() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.hoverColor
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
                Resources.OUTLINE,
                !this.isMouseDownOverComponent() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.hoverColor
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

    public void drawStatusIcons(float alphaScale) {
        this.refreshFade = this.refreshFade + (this.refreshing ? 0.33333334F : -0.33333334F);
        this.refreshFade = Math.min(1.0F, Math.max(0.0F, this.refreshFade));
        this.errorBlinkTicks = Math.max(0, this.errorBlinkTicks - 1);
        float var4 = this.errorBlinkTicks <= 20 ? 20.0F : -20.0F;
        float var5 = (float) this.errorBlinkTicks >= var4 && (float) this.errorBlinkTicks <= (float) this.errorBlinkMaxTicks - var4 ? 1.0F : (float) this.errorBlinkTicks % var4 / var4;
        RenderUtils.drawImage(
                (float) (this.x + this.width - 45),
                (float) (this.y + 42),
                17.0F,
                17.0F,
                Resources.X_ICON,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5 * alphaScale)
        );
        RenderUtils.drawImage(
                (float) (this.x + this.width - 45),
                (float) (this.y + 45),
                17.0F,
                13.0F,
                Resources.CHECKMARK_ICON,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.refreshFade * alphaScale)
        );
    }

    public void setAccountListRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    public void setErrorBlinkTicks(int errorCode) {
        this.errorBlinkTicks = errorCode;
        this.errorBlinkMaxTicks = errorCode;
    }

    public void setLoadingIndicator(boolean isLoading) {
        this.loadingIndicator.setHovered(isLoading);
    }
}
