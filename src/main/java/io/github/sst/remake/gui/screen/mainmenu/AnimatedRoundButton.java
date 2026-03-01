package io.github.sst.remake.gui.screen.mainmenu;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.Image;
import io.github.sst.remake.util.math.BufferUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.font.TrueTypeFont;
import org.newdawn.slick.opengl.texture.Texture;

public class AnimatedRoundButton extends Image {
    public boolean hovered = false;
    public AnimationUtils hoverAnimation = new AnimationUtils(160, 140, AnimationUtils.Direction.FORWARDS);

    public AnimatedRoundButton(GuiComponent parent, String id, int x, int y, int width, int height, Texture texture, ColorHelper colors) {
        super(parent, id, x, y, width, height, texture, colors);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        this.hovered = this.isHoveredInHierarchy();

        if (this.hovered) {
            this.hoverAnimation.changeDirection(AnimationUtils.Direction.BACKWARDS);
            this.setReAddChildren(true);
            return;
        }

        if (isHoverAnimationSettled()) {
            this.hoverAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
            this.setReAddChildren(false);
        }
    }

    public boolean isHoverAnimationSettled() {
        return Math.abs(this.getHoverExpandFactor() - this.getUnhoverExpandFactor()) < 0.6F;
    }

    public float getHoverExpandFactor() {
        return VecUtils.interpolate(this.hoverAnimation.calcPercent(), 0.24, 0.88, 0.3, 1.0);
    }

    public float getUnhoverExpandFactor() {
        return VecUtils.interpolate(this.hoverAnimation.calcPercent(), 0.45, 0.02, 0.59, 0.28);
    }

    @Override
    public void draw(float partialTicks) {
        final float pressedBlend = this.isMouseDownOverComponent() ? 0.1F : 0.0F;

        float expandFactor = getHoverExpandFactor();
        if (this.hoverAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            expandFactor = getUnhoverExpandFactor();
        }

        int scaledWidth = (int) ((double) this.getWidth() * (1.0 + (double) expandFactor * 0.2));
        int scaledHeight = (int) ((double) this.getHeight() * (1.0 + (double) expandFactor * 0.2));

        int drawX = this.getX() - (scaledWidth - this.getWidth()) / 2;
        int drawY = (int) ((double) (this.getY() - (scaledHeight - this.getHeight()) / 2)
                - (double) ((float) (this.getHeight() / 2) * expandFactor) * 0.2);

        float[] fit = BufferUtils.calculateAspectRatioFit(
                this.getTexture().getWidth(),
                this.getTexture().getHeight(),
                (float) scaledWidth,
                (float) scaledHeight
        );

        final float shadowPadding = 85.0F;

        RenderUtils.drawImage(
                (float) drawX + fit[0] - shadowPadding,
                (float) drawY + fit[1] - shadowPadding,
                fit[2] + (shadowPadding * 2.0F),
                fit[3] + (shadowPadding * 2.0F),
                Resources.SHADOW,
                ColorHelper.applyAlpha(
                        ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                        this.hoverAnimation.calcPercent() * 0.7F * partialTicks
                )
        );

        RenderUtils.drawImage(
                (float) drawX + fit[0],
                (float) drawY + fit[1],
                fit[2],
                fit[3],
                this.getTexture(),
                ColorHelper.applyAlpha(
                        ColorHelper.shiftTowardsOther(
                                this.textColor.getPrimaryColor(),
                                this.textColor.getSecondaryColor(),
                                1.0F - pressedBlend
                        ),
                        partialTicks
                )
        );

        if (this.getText() != null) {
            RenderUtils.drawString(
                    this.getFont(),
                    (float) (drawX + scaledWidth / 2),
                    (float) (drawY + scaledHeight / 2),
                    this.getText(),
                    ColorHelper.applyAlpha(this.textColor.getTextColor(), partialTicks),
                    this.textColor.getWidthAlignment(),
                    this.textColor.getHeightAlignment()
            );
        }

        TrueTypeFont font = this.getFont();
        float hoverTextScale = 0.8F + expandFactor * 0.2F;

        if (expandFactor > 0.0F) {
            GL11.glPushMatrix();

            String label = this.getText() != null ? this.getText() : this.name;

            GL11.glTranslatef(
                    (float) (this.getX() + this.getWidth() / 2 - font.getWidth(label) / 2),
                    (float) (this.getY() + this.getHeight() - 40),
                    0.0F
            );
            GL11.glScalef(hoverTextScale, hoverTextScale, hoverTextScale);
            GL11.glAlphaFunc(519, 0.0F);

            RenderUtils.drawImage(
                    (1.0F - hoverTextScale) * (float) font.getWidth(label) / 2.0F + 1.0F - (float) font.getWidth(label) / 2.0F,
                    (float) font.getHeight(label) / 3.0F,
                    (float) (font.getWidth(label) * 2),
                    (float) font.getHeight(label) * 3.0F,
                    Resources.SHADOW,
                    expandFactor * 0.6F * partialTicks
            );

            RenderUtils.drawString(
                    font,
                    (1.0F - hoverTextScale) * (float) font.getWidth(label) / 2.0F + 1.0F,
                    40.0F,
                    label,
                    ColorHelper.applyAlpha(this.getTextColor().getPrimaryColor(), expandFactor * 0.6F * partialTicks)
            );

            GL11.glPopMatrix();
        }

        super.drawChildren(partialTicks);
    }
}