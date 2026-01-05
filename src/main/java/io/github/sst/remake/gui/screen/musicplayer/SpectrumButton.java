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
    private boolean spectrum;
    private final AnimationUtils hoverAnimation = new AnimationUtils(100, 100);

    public SpectrumButton(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, boolean var7) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.spectrum = var7;
    }

    @Override
    public void draw(float partialTicks) {
        this.hoverAnimation.changeDirection(!this.isHoveredInHierarchy() ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        partialTicks *= 0.09F + 0.25F * this.hoverAnimation.calcPercent() + (this.spectrum ? 0.0F : 0.2F);
        RenderUtils.drawRoundedRect2(
                (float) (this.x + 10), (float) (this.y + 16), 5.0F, 14.0F, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtils.drawRoundedRect2(
                (float) (this.x + 17), (float) (this.y + 10), 5.0F, 20.0F, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtils.drawRoundedRect2(
                (float) (this.x + 24), (float) (this.y + 20), 5.0F, 10.0F, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        super.draw(partialTicks);
    }
}
