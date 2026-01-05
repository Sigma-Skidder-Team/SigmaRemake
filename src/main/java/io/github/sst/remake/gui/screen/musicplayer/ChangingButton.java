package io.github.sst.remake.gui.screen.musicplayer;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.image.Resources;

public class ChangingButton extends InteractiveWidget {
    public int repeatMode;

    public ChangingButton(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, int repeatMode) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.repeatMode = repeatMode;
        this.onClick((parent, mouseButton) -> {
            this.repeatMode = (this.repeatMode + 1) % 3; // cycles 0 -> 1 -> 2 -> 0
            this.callUIHandlers();
        });
    }

    @Override
    public void draw(float partialTicks) {
        ScissorUtils.startScissor((float) this.x, (float) this.y, (float) this.width, (float) this.height);
        RenderUtils.drawImage(
                (float) (this.x - this.repeatMode * this.width),
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
