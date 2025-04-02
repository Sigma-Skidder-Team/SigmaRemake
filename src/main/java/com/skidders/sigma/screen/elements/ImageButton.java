package com.skidders.sigma.screen.elements;

import com.mojang.blaze3d.platform.GlStateManager;
import com.skidders.sigma.screen.Animation;
import com.skidders.sigma.util.client.interfaces.IMinecraft;
import com.skidders.sigma.util.client.interfaces.ITextures;
import com.skidders.sigma.util.client.render.ColorUtil;
import com.skidders.sigma.util.client.render.RenderUtil;
import com.skidders.sigma.util.client.render.image.ImageUtil;
import com.skidders.sigma.util.system.math.SmoothInterpolator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

@RequiredArgsConstructor
@Getter
public class ImageButton implements IMinecraft {

    private final String identificator; //not a word apparently ? https://www.wordreference.com/definition/Identificator
    private final float x, y, width, height;
    private final Texture texture;

    private final Animation hoverAnim = new Animation(150, 190, Animation.Direction.BACKWARDS);

    public void draw(float delta, int mouseX, int mouseY) {
        boolean hover = RenderUtil.hovered(mouseX, mouseY, x, y, width, height);
        this.hoverAnim.changeDirection(hover ? Animation.Direction.FORWARDS : Animation.Direction.BACKWARDS);
        float var4 = SmoothInterpolator.interpolate(this.hoverAnim.calcPercent(), 0.07, 0.73, 0.63, 1.01);
        if (this.hoverAnim.getDirection() == Animation.Direction.BACKWARDS) {
            var4 = SmoothInterpolator.interpolate(this.hoverAnim.calcPercent(), 0.71, 0.18, 0.95, 0.57);
        }


        ImageUtil.drawImage(
                x,
                y - var4 * 3.0f,
                width,
                height,
                ITextures.switch_background_faded
        );

        if (hover) {
            RenderUtil.drawRoundedRect2(
                    x,
                    y - var4 * 3.0F,
                    width,
                    height,
                    ColorUtil.applyAlpha(-12319668, 0.5F)
            );
        }

        ImageUtil.drawImage(
                x,
                y - var4 * 3.0F,
                width,
                height,
                texture,
                ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );

        GL11.glPushMatrix();
        GlStateManager.enableAlphaTest();
        GL11.glAlphaFunc(519, 0.0F);
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glPopMatrix();
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return RenderUtil.hovered(mouseX, mouseY, x, y, width, height);
    }

}
