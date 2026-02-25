package io.github.sst.remake.gui.screen.musicplayer;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.image.Resources;

public class ChangingButton extends InteractiveWidget {
    public int repeatModeIndex;

    public ChangingButton(GuiComponent parent, String name, int x, int y, int width, int height, int repeatModeIndex) {
        super(parent, name, x, y, width, height, false);

        this.repeatModeIndex = repeatModeIndex;

        this.onClick((clickedParent, mouseButton) -> {
            this.repeatModeIndex = (this.repeatModeIndex + 1) % 3; // 0 -> 1 -> 2 -> 0
            this.firePressHandlers();
        });
    }

    @Override
    public void draw(float partialTicks) {
        ScissorUtils.startScissorRect((float) this.x, (float) this.y, (float) this.width, (float) this.height);
        RenderUtils.drawImage(
                (float) (this.x - this.repeatModeIndex * this.width),
                (float) this.y,
                (float) (this.width * 3),
                (float) this.height,
                Resources.REPEAT,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.35F)
        );
        ScissorUtils.restoreScissor();
        super.draw(partialTicks);
    }
}
