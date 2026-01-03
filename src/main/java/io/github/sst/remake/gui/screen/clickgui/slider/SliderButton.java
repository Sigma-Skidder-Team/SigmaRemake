package io.github.sst.remake.gui.screen.clickgui.slider;

import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

public class SliderButton extends Button {
    private final Slider field20600;
    private final AnimationUtils field20601 = new AnimationUtils(125, 125);

    public SliderButton(Slider var1, int var2) {
        super(var1, "sliderButton", 0, 0, var2, var2, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()));
        this.field20601.changeDirection(AnimationUtils.Direction.FORWARDS);
        this.setDraggable(true);
        this.field20886 = true;
        this.field20600 = var1;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        float var6 = (float) this.getX() / (float) (this.parent.getWidth() - this.getWidth());
        if (!this.isMouseDownOverComponent() && !this.isHoveredInHierarchy() && !this.isDragging()) {
            this.field20601.changeDirection(AnimationUtils.Direction.FORWARDS);
        } else {
            this.field20601.changeDirection(AnimationUtils.Direction.BACKWARDS);
        }

        this.field20600.method13139(var6);
    }

    @Override
    public void draw(float partialTicks) {
        int var5 = 5;
        float var6 = (float) this.getWidth();
        RenderUtils.drawRoundedRect(
                (float) (this.getX() + var5),
                (float) (this.getY() + var5),
                (float) (this.getWidth() - var5 * 2),
                (float) (this.getHeight() - var5 * 2),
                10.0F,
                partialTicks * 0.8F
        );
        RenderUtils.drawCircle(
                (float) (this.getX() + this.getWidth() / 2),
                (float) (this.getY() + this.getWidth() / 2),
                var6,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );

        super.drawChildren(partialTicks);
    }
}
