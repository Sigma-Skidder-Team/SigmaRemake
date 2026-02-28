package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.font.TrueTypeFont;
import org.newdawn.slick.opengl.texture.Texture;

public class Image extends Button {
    private static final ColorHelper DEFAULT_COLORS = new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ColorHelper.shiftTowardsBlack(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.1F));
    public Texture texture;

    public Image(GuiComponent parent, String id, int x, int y, int width, int height, Texture texture, ColorHelper colors, String label, TrueTypeFont font) {
        super(parent, id, x, y, width, height, colors, label, font);
        this.texture = texture;
    }

    public Image(GuiComponent parent, String id, int x, int y, int width, int height, Texture texture, ColorHelper colors, String label) {
        super(parent, id, x, y, width, height, colors, label);
        this.texture = texture;
    }

    public Image(GuiComponent parent, String id, int x, int y, int width, int height, Texture texture, ColorHelper colors) {
        super(parent, id, x, y, width, height, colors);
        this.texture = texture;
    }

    public Image(GuiComponent screen, String iconName, int x, int y, int width, int height, Texture texture) {
        super(screen, iconName, x, y, width, height, DEFAULT_COLORS);
        this.texture = texture;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public void draw(float partialTicks) {
        float opacityFactor = !this.isHovered() ? 0.3F : (!this.isDragging() ? (!this.isMouseDownOverComponent() ? Math.max(partialTicks * this.hoverFade, 0.0F) : 1.5F) : 0.0F);

        RenderUtils.drawImage(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth(),
                (float) this.getHeight(),
                this.getTexture(),
                ColorHelper.applyAlpha(
                        ColorHelper.shiftTowardsOther(this.textColor.getPrimaryColor(), this.textColor.getSecondaryColor(), 1.0F - opacityFactor),
                        (float) (this.textColor.getPrimaryColor() >> 24 & 0xFF) / 255.0F * partialTicks
                )
        );

        if (this.getText() != null) {
            RenderUtils.drawString(
                    this.getFont(),
                    (float) (this.getX() + this.getWidth() / 2),
                    (float) (this.getY() + this.getHeight() / 2),
                    this.getText(),
                    ColorHelper.applyAlpha(this.textColor.getTextColor(), partialTicks),
                    this.textColor.getWidthAlignment(),
                    this.textColor.getHeightAlignment()
            );
        }

        GL11.glPushMatrix();
        super.drawChildren(partialTicks);
        GL11.glPopMatrix();
    }
}
