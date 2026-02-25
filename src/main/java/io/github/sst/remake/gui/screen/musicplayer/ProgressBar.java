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
    public float dragProgress = -1.0F;

    public ProgressBar(GuiComponent parentScreen, String name, int x, int y, int width, int height) {
        super(parentScreen, name, x, y, width, height, false);

        this.addMouseButtonCallback((screen, mouseButton) -> {
            int totalDuration = this.musicManager.getTotalDuration();
            int duration = this.musicManager.getDuration();
            this.dragProgress = duration <= 0 ? 0.0F : Math.min((float) totalDuration / (float) duration, 1.0F);
        });

        this.addMouseListener((screen, mouseButton) -> {
            if (!this.isHoveredInHierarchy() || !this.isFocused()) {
                return;
            }

            int duration = this.musicManager.getDuration();
            int playbackProgress = (int) this.musicManager.getPlaybackProgress();

            int targetMs = Math.min((int) (this.dragProgress * (float) duration), playbackProgress);
            this.musicManager.seekTo(targetMs);
        });
    }

    @Override
    public void draw(float partialTicks) {
        int totalDuration = this.musicManager.getTotalDuration();
        double playbackProgress = this.musicManager.getPlaybackProgress();
        int duration = this.musicManager.getDuration();

        if (duration <= 0) {
            RenderUtils.drawRoundedRect2(
                    (float) this.getX(),
                    (float) this.getY(),
                    (float) this.getWidth(),
                    (float) this.getHeight(),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * partialTicks)
            );
            return;
        }

        float bufferedPercent = Math.max(0.0F, Math.min((float) totalDuration / (float) duration, 1.0F));
        float playedPercent = Math.max(0.0F, Math.min((float) playbackProgress / (float) duration, 1.0F));

        if (this.isMouseDownOverComponent() && this.isHoveredInHierarchy() && playbackProgress != 0.0) {
            int mouseXInBar = this.getMouseX() - this.getAbsoluteX();
            this.dragProgress = Math.min(
                    Math.max((float) mouseXInBar / (float) this.getWidth(), 0.0F),
                    playedPercent
            );
            bufferedPercent = this.dragProgress;
        }

        // Base bar.
        RenderUtils.drawRoundedRect2(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth(),
                (float) this.getHeight(),
                ColorHelper.applyAlpha(ClientColors.MID_GREY.getColor(), 0.075F)
        );

        // Unplayed portion overlay.
        RenderUtils.drawRoundedRect2(
                (float) this.getX() + (float) this.getWidth() * playedPercent,
                (float) this.getY(),
                (float) this.getWidth() * (1.0F - playedPercent),
                (float) this.getHeight(),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * partialTicks)
        );

        // Buffered/preview portion.
        RenderUtils.drawRoundedRect2(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth() * bufferedPercent,
                (float) this.getHeight(),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * partialTicks)
        );

        if (bufferedPercent != 0.0F) {
            RenderUtils.drawImage(
                    (float) this.getX() + (float) this.getWidth() * bufferedPercent,
                    (float) this.getY(),
                    5.0F,
                    5.0F,
                    Resources.SHADOW_RIGHT
            );
        }
    }
}
