package io.github.sst.remake.gui.screen.clickgui.config;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;

public class ProfileEditPanel extends Widget {
    private final int baseWidth;

    public ProfileEditPanel(
            GuiComponent parent,
            String id,
            int x,
            int y,
            int width,
            int height
    ) {
        super(parent, id, x, y, width, height, false);
        this.baseWidth = width;
    }

    @Override
    public void draw(float partialTicks) {
        if (this.getWidth() == 0) {
            return;
        }

        this.applyTranslationTransforms();

        float collapseProgress = 1.0F - Math.min(
                1.0F,
                Math.max((float) this.getWidth() / (float) this.baseWidth, 0.0F)
        );

        RenderUtils.drawRoundedRect2(
                (float) this.x,
                (float) this.y,
                (float) this.baseWidth,
                (float) this.height,
                ColorHelper.applyAlpha(-3254955, partialTicks)
        );

        // Fade children based on open progress.
        super.draw(partialTicks * (1.0F - collapseProgress));

        RenderUtils.drawImage(
                0.0F,
                0.0F,
                20.0F,
                (float) this.height,
                Resources.SHADOW_RIGHT,
                ColorHelper.applyAlpha(
                        ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                        collapseProgress * partialTicks
                )
        );
    }
}
