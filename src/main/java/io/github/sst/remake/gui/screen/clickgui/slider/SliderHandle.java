package io.github.sst.remake.gui.screen.clickgui.slider;

import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

public class SliderHandle extends Button {
    private final SettingSlider slider;
    private final AnimationUtils interactionAnimation = new AnimationUtils(125, 125);

    public SliderHandle(SettingSlider slider, int size) {
        super(
                slider,
                "sliderButton",
                0,
                0,
                size,
                size,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor())
        );

        this.interactionAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);

        this.setDraggable(true);
        this.enableImmediateDrag = true;

        this.slider = slider;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        float normalized =
                (float) this.getX() / (float) (this.parent.getWidth() - this.getWidth());

        boolean idle = !this.isMouseDownOverComponent() && !this.isHoveredInHierarchy() && !this.isDragging();
        this.interactionAnimation.changeDirection(idle ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);

        this.slider.setValueFromHandle(normalized);
    }

    @Override
    public void draw(float partialTicks) {
        int inset = 5;
        float radius = (float) this.getWidth();

        RenderUtils.drawRoundedRect(
                (float) (this.getX() + inset),
                (float) (this.getY() + inset),
                (float) (this.getWidth() - inset * 2),
                (float) (this.getHeight() - inset * 2),
                10.0F,
                partialTicks * 0.8F
        );

        RenderUtils.drawCircle(
                (float) (this.getX() + this.getWidth() / 2),
                (float) (this.getY() + this.getWidth() / 2),
                radius,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );

        super.drawChildren(partialTicks);
    }
}