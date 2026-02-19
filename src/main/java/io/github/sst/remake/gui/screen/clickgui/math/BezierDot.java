package io.github.sst.remake.gui.screen.clickgui.math;

import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

public class BezierDot extends Widget {
    public BezierCurve curve;

    public BezierDot(BezierCurve var1, int var2, String var3) {
        super(var1, "bezierButton-" + var3, 0, 0, var2, var2, true);
        this.setDraggable(true);
        this.enableImmediateDrag = true;
        this.curve = var1;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        int padding = this.curve.padding;
        int minX = padding - 5;
        int minY = padding - 5;
        int maxX = this.curve.getWidth() - padding - 5;
        int maxY = this.curve.getHeight() - padding - 5;

        if (this.getX() > maxX) {
            this.setX(maxX);
        }
        if (this.getY() > maxY) {
            this.setY(maxY);
        }
        if (this.getX() < minX) {
            this.setX(minX);
        }
        if (this.getY() < minY) {
            this.setY(minY);
        }
    }

    public void setPosition(float var1, float var2) {
        this.x = (int) var1;
        this.y = (int) var2;
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawCircle(
                (float) (this.x + 5),
                (float) (this.y + 5),
                10.0F,
                ColorHelper.applyAlpha(!this.isDragging() ? ClientColors.DARK_GREEN.getColor() : ClientColors.DARK_BLUE_GREY.getColor(), partialTicks)
        );
        super.draw(partialTicks);
    }
}
