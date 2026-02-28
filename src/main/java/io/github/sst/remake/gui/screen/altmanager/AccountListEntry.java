package io.github.sst.remake.gui.screen.altmanager;

import io.github.sst.remake.data.alt.Account;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.gui.framework.widget.internal.LoadingIndicator;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;

public class AccountListEntry extends Widget {
    public Account account;
    private final LoadingIndicator loadingIndicator;
    private boolean isRefreshing = false;

    private final AnimationUtils selectionAnim = new AnimationUtils(814, 114, AnimationUtils.Direction.FORWARDS);
    private float refreshFade = 0.0F;
    public AnimationUtils slideAnim = new AnimationUtils(800, 300, AnimationUtils.Direction.FORWARDS);
    private int errorBlinkTicks = 0;
    private int errorBlinkMaxTicks = 0;
    private int hoverColor = ColorHelper.shiftTowardsOther(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 20.0F);

    public AccountListEntry(GuiComponent parent, String id, int x, int y, int width, int height, Account account) {
        super(parent, id, x, y, width, height, false);
        this.account = account;
        this.addToList(this.loadingIndicator = new LoadingIndicator(this, "loading", width - 50, 35, 30, 30));
        this.loadingIndicator.setHovered(false);
    }

    public void setSelected(boolean selected) {
        this.setSelected(selected, false);
    }

    public void setSelected(boolean selected, boolean instant) {
        this.selectionAnim.changeDirection(!selected ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        if (instant) {
            this.selectionAnim.updateStartTime(1.0F);
        }
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();
        this.hoverColor = ColorHelper.shiftTowardsOther(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor(), 2.0F);

        int scrollOffset = ((ScrollablePanel) this.parent.getParent()).getScrollOffset();
        int drawY = Math.max(0, this.y - scrollOffset);
        int drawHeight = Math.max(0, this.height + Math.min(100, this.y - scrollOffset - drawY));
        float alphaScale = (float) Math.min(50, drawHeight) / 50.0F;

        drawY += scrollOffset;

        if (alphaScale > 0.0F) {
            RenderUtils.drawFloatingPanelScaled(
                    this.x,
                    drawY,
                    this.width,
                    Math.max(20, drawHeight),
                    ColorHelper.applyAlpha(!this.isMouseDownOverComponent() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.hoverColor, alphaScale)
            );

            ScissorUtils.startScissor(this.x, drawY, this.x + this.width + 20, drawY + drawHeight, true);
            if (this.account != null) {
                this.drawAccountHead();
                this.drawAccountUsername();
                this.drawStatusIcons(alphaScale);

                if (this.selectionAnim.calcPercent() > 0.0F && drawHeight > 55) {
                    RenderUtils.drawImage(
                            (float) (this.x + this.getWidth()),
                            (float) drawY + (float) (26 * drawHeight) / 100.0F,
                            18.0F * this.selectionAnim.calcPercent() * (float) drawHeight / 100.0F,
                            (float) (47 * drawHeight) / 100.0F,
                            Resources.SELECTED_ICON,
                            !this.isMouseDownOverComponent() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.hoverColor
                    );
                }

                super.draw(partialTicks * alphaScale);
                ScissorUtils.restoreScissor();
            }
        }
    }

    public void drawAccountHead() {
        RenderUtils.drawImage((float) (this.x + 13), (float) (this.y + 13), 75.0F, 75.0F, this.account.setHeadTexture(), ClientColors.LIGHT_GREYISH_BLUE.getColor(), true);
        RenderUtils.drawPanelShadow((float) (this.x + 13), (float) (this.y + 13), 75.0F, 75.0F, 20.0F, 1.0F);
        RenderUtils.drawImage((float) (this.x + 1), (float) this.y, 100.0F, 100.0F, Resources.OUTLINE, !this.isMouseDownOverComponent() ? ClientColors.LIGHT_GREYISH_BLUE.getColor() : this.hoverColor);
    }

    public void drawAccountUsername() {
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_25, (float) (this.x + 110), (float) (this.y + 18), this.account.name, ClientColors.DEEP_TEAL.getColor());
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_14, (float) (this.x + 110), (float) (this.y + 50), "Token: " + "asdddddddddddddddddddddddddddddddddddddddddddd".replaceAll(".", "·"), ClientColors.MID_GREY.getColor());
    }

    public void drawStatusIcons(float alphaScale) {
        this.refreshFade += (this.isRefreshing ? 0.33333334F : -0.33333334F);
        this.refreshFade = Math.min(1.0F, Math.max(0.0F, this.refreshFade));
        this.errorBlinkTicks = Math.max(0, this.errorBlinkTicks - 1);

        float threshold = 20.0F;
        float blinkProgress = (this.errorBlinkTicks <= threshold) ? (this.errorBlinkTicks / threshold) : 1.0F; // Simplified logic

        RenderUtils.drawImage((float) (this.x + this.width - 45), (float) (this.y + 42), 17.0F, 17.0F, Resources.X_ICON, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), blinkProgress * alphaScale));
        RenderUtils.drawImage((float) (this.x + this.width - 45), (float) (this.y + 45), 17.0F, 13.0F, Resources.CHECKMARK_ICON, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), this.refreshFade * alphaScale));
    }

    public void setAccountListRefreshing(boolean refreshing) {
        this.isRefreshing = refreshing;
    }

    public void setErrorBlinkTicks(int errorCode) {
        this.errorBlinkTicks = errorCode;
        this.errorBlinkMaxTicks = errorCode;
    }

    public void setLoadingIndicator(boolean isLoading) {
        this.loadingIndicator.setHovered(isLoading);
    }
}