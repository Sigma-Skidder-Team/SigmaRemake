package io.github.sst.remake.gui.screen.musicplayer;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import lombok.Setter;

public class SpectrumButton extends InteractiveWidget {
    @Setter
    private boolean spectrumEnabled;
    private final AnimationUtils hoverAnimation = new AnimationUtils(100, 100);

    public SpectrumButton(GuiComponent parent, String name, int x, int y, int width, int height, boolean spectrumEnabled) {
        super(parent, name, x, y, width, height, false);
        this.spectrumEnabled = spectrumEnabled;
    }

    @Override
    public void draw(float partialTicks) {
        this.hoverAnimation.changeDirection(
                this.isHoveredInHierarchy() ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS
        );

        float hoverPercent = this.hoverAnimation.calcPercent();
        float alphaMultiplier = 0.09F + 0.25F * hoverPercent + (this.spectrumEnabled ? 0.2F : 0.0F);
        float alpha = partialTicks * alphaMultiplier;

        int barColor = ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha);

        RenderUtils.drawRoundedRect2((float) (this.x + 10), (float) (this.y + 16), 5.0F, 14.0F, barColor);
        RenderUtils.drawRoundedRect2((float) (this.x + 17), (float) (this.y + 10), 5.0F, 20.0F, barColor);
        RenderUtils.drawRoundedRect2((float) (this.x + 24), (float) (this.y + 20), 5.0F, 10.0F, barColor);

        super.draw(partialTicks);
    }
}
