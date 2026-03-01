package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import org.newdawn.slick.opengl.font.TrueTypeFont;

public class TextButton extends InteractiveWidget {
    private final AnimationUtils underlineAnimation;

    public TextButton(GuiComponent parent,
                      String id,
                      int x,
                      int y,
                      int width,
                      int height,
                      ColorHelper textColor,
                      String text,
                      TrueTypeFont font) {
        super(parent, id, x, y, width, height, textColor, text, font, false);

        int animationDuration = (int) (210.0 * Math.sqrt((float) width / 242.0F));
        this.underlineAnimation = new AnimationUtils(animationDuration, animationDuration);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        this.underlineAnimation.changeDirection(
                this.isHoveredInHierarchy()
                        ? AnimationUtils.Direction.BACKWARDS
                        : AnimationUtils.Direction.FORWARDS
        );
    }

    @Override
    public void draw(float partialTicks) {
        if (this.getText() == null) {
            return;
        }

        int baseColor = this.textColor.getPrimaryColor();

        int textX = this.getX()
                + (
                this.textColor.getWidthAlignment() != FontAlignment.CENTER
                        ? 0
                        : (this.textColor.getWidthAlignment() != FontAlignment.RIGHT
                        ? this.getWidth() / 2
                        : this.getWidth())
        );

        int textY = this.getY()
                + (
                this.textColor.getHeightAlignment() != FontAlignment.CENTER
                        ? 0
                        : (this.textColor.getHeightAlignment() != FontAlignment.BOTTOM
                        ? this.getHeight() / 2
                        : this.getHeight())
        );

        int textWidth = this.getFont().getWidth(this.getText());
        float underlineOffsetY = 18.0F;
        float animationProgress = (float) Math.pow(this.underlineAnimation.calcPercent(), 3.0);

        int animatedColor = ColorHelper.applyAlpha(
                baseColor,
                partialTicks * ColorHelper.getAlpha(baseColor)
        );

        RenderUtils.drawString(
                this.getFont(),
                (float) textX,
                (float) textY,
                this.getText(),
                animatedColor,
                this.textColor.getWidthAlignment(),
                this.textColor.getHeightAlignment()
        );

        RenderUtils.drawRoundedRect(
                (float) textX - (textWidth / 2.0F) * animationProgress,
                textY + underlineOffsetY,
                (float) textX + (textWidth / 2.0F) * animationProgress,
                textY + underlineOffsetY + 2.0F,
                animatedColor
        );

        super.draw(partialTicks);
    }
}