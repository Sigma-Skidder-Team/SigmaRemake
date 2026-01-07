package io.github.sst.remake.gui.screen.musicplayer;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.manager.impl.MusicManager;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;

public class ProgressBar extends InteractiveWidget {
    private final MusicManager musicManager = Client.INSTANCE.musicManager;
    public float progress = -1.0F;

    public ProgressBar(GuiComponent parentScreen, String var2, int var3, int var4, int var5, int var6) {
        super(parentScreen, var2, var3, var4, var5, var6, false);
        this.addMouseButtonCallback((var1x, var2x) -> {
            int var5x = (int) this.musicManager.getTotalDuration();
            int var6x = this.musicManager.getDuration();
            this.progress = Math.min((float) var5x / (float) var6x, 1.0F);
        });
        this.addMouseListener((var1x, var2x) -> {
            if (this.isHoveredInHierarchy() && this.isFocused()) {
                int var5x = (int) Math.min((int) (this.progress * (float) this.musicManager.getDuration()), this.musicManager.getPlaybackProgress());
                this.musicManager.seekTo(var5x);
            }
        });
    }

    @Override
    public void draw(float partialTicks) {
        long durationLong = (int) this.musicManager.getTotalDuration();
        double var5 = this.musicManager.getPlaybackProgress();
        int durationInt = this.musicManager.getDuration();
        float var8 = Math.max(0.0F, Math.min((float) durationLong / (float) durationInt, 1.0F));
        float var9 = Math.max(0.0F, Math.min((float) var5 / (float) durationInt, 1.0F));
        if (this.isMouseDownOverComponent() && this.isHoveredInHierarchy() && var5 != 0.0) {
            int var10 = this.getMouseX() - this.getAbsoluteX();
            this.progress = Math.min(Math.max((float) var10 / (float) this.getWidth(), 0.0F), var9);
            var8 = this.progress;
        }

        if (durationLong == 0 && !this.musicManager.isPlaying()) {
            RenderUtils.drawRoundedRect2(
                    (float) this.getX(),
                    (float) this.getY(),
                    (float) this.getWidth(),
                    (float) this.getHeight(),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * partialTicks)
            );
        } else {
            RenderUtils.drawRoundedRect2(
                    (float) this.getX(),
                    (float) this.getY(),
                    (float) this.getWidth(),
                    (float) this.getHeight(),
                    ColorHelper.applyAlpha(ClientColors.MID_GREY.getColor(), 0.075F)
            );
            RenderUtils.drawRoundedRect2(
                    (float) this.getX() + (float) this.getWidth() * var9,
                    (float) this.getY(),
                    (float) this.getWidth() * (1.0F - var9),
                    (float) this.getHeight(),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * partialTicks)
            );
            RenderUtils.drawRoundedRect2(
                    (float) this.getX(),
                    (float) this.getY(),
                    (float) this.getWidth() * var8,
                    (float) this.getHeight(),
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * partialTicks)
            );
            if (var8 != 0.0F) {
                RenderUtils.drawImage((float) this.getX() + (float) this.getWidth() * var8, (float) this.getY(), 5.0F, 5.0F, Resources.SHADOW_RIGHT);
            }
        }
    }
}
