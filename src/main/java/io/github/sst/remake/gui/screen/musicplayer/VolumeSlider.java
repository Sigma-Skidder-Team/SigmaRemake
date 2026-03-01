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
    private boolean isDragging = false;
    private final List<VolumeChangeListener> volumeChangeListeners = new ArrayList<>();

    public VolumeSlider(GuiComponent parent, String iconName, int x, int y, int width, int height) {
        super(parent, iconName, x, y, width, height, false);
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
            this.isDragging = true;
            return false;
        } else {
            return true;
        }
    }

    public float calculateVolumeFromMouseY(int mouseY) {
        return (float) (mouseY - this.getAbsoluteY()) / (float) this.height;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        if (this.isDragging) {
            this.setVolume(this.calculateVolumeFromMouseY(mouseY));
            this.fireVolumeChange();
        }
    }

    @Override
    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {
        super.onMouseRelease(mouseX, mouseY, mouseButton);
        this.isDragging = false;
    }

    @Override
    public void onScroll(float scroll) {
        if (this.isHoveredInHierarchy()) {
            this.setVolume(this.getVolume() - scroll / 2000.0F);
            this.fireVolumeChange();
        }

        super.onScroll(scroll);
    }

    public float getVolume() {
        return this.volume;
    }

    public void setVolume(float value) {
        this.volume = Math.min(Math.max(value, 0.0F), 1.0F);
    }

    public Widget addVolumeChangeListener(VolumeChangeListener listener) {
        this.volumeChangeListeners.add(listener);
        return this;
    }

    public void fireVolumeChange() {
        for (VolumeChangeListener listener : this.volumeChangeListeners) {
            listener.onVolumeChanged(this);
        }
    }

    public interface VolumeChangeListener{
        void onVolumeChanged(VolumeSlider slider);
    }
}