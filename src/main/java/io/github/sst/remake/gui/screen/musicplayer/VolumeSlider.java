package io.github.sst.remake.gui.screen.musicplayer;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public class VolumeSlider extends InteractiveWidget {
    private float volume = 1.0F;
    private boolean field21373 = false;
    private final List<Class6649> field21374 = new ArrayList<>();

    public VolumeSlider(GuiComponent parent, String iconName, int xV, int yV, int width, int height) {
        super(parent, iconName, xV, yV, width, height, false);
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) (this.x + this.width),
                (float) this.y + (float) this.height * this.volume,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.2F)
        );
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) (this.y + this.height),
                (float) (this.x + this.width),
                (float) this.y + (float) this.height * this.volume,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.2F)
        );
        super.draw(partialTicks);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseDown(this.x, this.y, mouseButton)) {
            this.field21373 = true;
            return false;
        } else {
            return true;
        }
    }

    public float method13706(int var1) {
        return (float) (var1 - this.getAbsoluteY()) / (float) this.height;
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
        if (this.field21373) {
            this.setVolume(this.method13706(newWidth));
            this.method13710();
        }
    }

    @Override
    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {
        super.onMouseRelease(mouseX, mouseY, mouseButton);
        this.field21373 = false;
    }

    @Override
    public void onScroll(float scroll) {
        if (this.isHoveredInHierarchy()) {
            this.setVolume(this.getVolume() - scroll / 2000.0F);
            this.method13710();
        }

        super.onScroll(scroll);
    }

    public float getVolume() {
        return this.volume;
    }

    public void setVolume(float value) {
        this.volume = Math.min(Math.max(value, 0.0F), 1.0F);
    }

    public Widget method13709(Class6649 var1) {
        this.field21374.add(var1);
        return this;
    }

    public void method13710() {
        for (Class6649 var4 : this.field21374) {
            var4.method20301(this);
        }
    }

    public interface Class6649 {
        void method20301(VolumeSlider var1);
    }
}
