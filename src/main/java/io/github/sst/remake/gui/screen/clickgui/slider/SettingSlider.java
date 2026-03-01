package io.github.sst.remake.gui.screen.clickgui.slider;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;

public class SettingSlider extends InteractiveWidget {
    private float value;
    private SliderHandle handle;
    private AnimationUtils labelFade;

    public SettingSlider(GuiComponent parent, String text, int x, int y, int width, int height) {
        super(parent, text, x, y, width, height, false);
        this.initHandle();
    }

    private void initHandle() {
        this.handle = new SliderHandle(this, this.getHeight());
        this.addToList(this.handle);

        this.labelFade = new AnimationUtils(114, 114, AnimationUtils.Direction.FORWARDS);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        boolean idle =
                !this.isHoveredInHierarchy()
                        && !this.handle.isHoveredInHierarchy()
                        && !this.isMouseDownOverComponent()
                        && !this.handle.isDragging();

        this.labelFade.changeDirection(idle ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        int barHeight = this.getHeight() / 4;

        int trackLeftX = this.getX() + this.handle.getWidth() / 4 + 3;
        int trackRightX = this.getWidth() - this.handle.getWidth() / 2 - 3;

        int trackY = this.getY() + this.getHeight() / 2 - barHeight / 2;

        int filledWidth = this.handle.getX() + this.handle.getWidth() / 2 - 6;

        float cornerRadius = barHeight / 2.0F;
        float baseAlpha = partialTicks * partialTicks * partialTicks;

        RenderUtils.drawRoundedRect(
                (float) trackLeftX,
                (float) trackY,
                (float) filledWidth,
                (float) barHeight,
                cornerRadius,
                ColorHelper.applyAlpha(this.textColor.getPrimaryColor(), baseAlpha)
        );

        RenderUtils.drawRoundedRect(
                (float) (trackLeftX + filledWidth),
                (float) trackY,
                (float) (trackRightX - filledWidth),
                (float) barHeight,
                cornerRadius,
                ColorHelper.applyAlpha(ColorHelper.adjustColorTowardsWhite(this.textColor.getPrimaryColor(), 0.8F), baseAlpha)
        );

        if (this.getText() != null) {
            int labelClampOffset = Math.max(0, 9 - this.handle.getX());

            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_14,
                    (float) (trackLeftX - FontUtils.HELVETICA_LIGHT_14.getWidth(this.getText()) - 10 - labelClampOffset),
                    (float) (trackY - 5),
                    this.getText(),
                    ColorHelper.applyAlpha(
                            ClientColors.DEEP_TEAL.getColor(),
                            0.5F * this.labelFade.calcPercent() * partialTicks
                    )
            );
        }

        super.draw(partialTicks);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseDown(mouseX, mouseY, mouseButton)) {
            this.handle.setDragging(true);
            return false;
        }
        return true;
    }

    public SliderHandle getHandle() {
        return this.handle;
    }

    public float getValue() {
        return this.value;
    }

    public void setValueFromHandle(float normalized) {
        this.setValue(normalized, true);
    }

    public void setValue(float normalized, boolean fireHandlers) {
        normalized = Math.min(Math.max(normalized, 0.0F), 1.0F);

        float oldValue = this.value;
        this.value = normalized;

        this.handle.setX((int) ((float) (this.getWidth() - this.handle.getWidth()) * normalized + 0.5F));

        if (fireHandlers && oldValue != normalized) {
            this.firePressHandlers();
        }
    }
}