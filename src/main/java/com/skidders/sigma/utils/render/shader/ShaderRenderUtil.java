package com.skidders.sigma.utils.render.shader;

import static com.mojang.blaze3d.platform.GlStateManager.*;
import static org.lwjgl.opengl.GL11.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import com.skidders.SigmaReborn;
import com.skidders.sigma.utils.IMinecraft;
import com.skidders.sigma.utils.render.ColorUtil;
import com.skidders.sigma.utils.render.image.TextureLoader;
import com.skidders.sigma.utils.render.shader.shader.Shader;
import com.skidders.sigma.utils.render.shader.shader.impl.GlowShader;
import net.minecraft.util.Identifier;

public class ShaderRenderUtil implements IMinecraft {
    public static final HashMap<Integer, Integer> glowCache = new HashMap<Integer, Integer>();
    private static final Shader ROUNDED = new Shader("rounded.frag");
    private static final Shader ROUNDED_GRADIENT = new Shader("rounded_gradient.frag");
    private static final Shader ROUNDED_BLURRED = new Shader("rounded_blurred.frag");
    private static final Shader ROUNDED_BLURRED_GRADIENT = new Shader("rounded_blurred_gradient.frag");
    private static final Shader ROUNDED_OUTLINE = new Shader("rounded_outline.frag");
    private static final Shader ROUNDED_TEXTURE = new Shader("rounded_texture.frag");
    public static final int STEPS = 60;
    public static final double ANGLE = Math.PI * 2 / STEPS;
    public static final int EX_STEPS = 120;
    public static final double EX_ANGLE = Math.PI * 2 / EX_STEPS;

    public enum Part {
        FIRST_QUARTER(4, Math.PI / 2),
        SECOND_QUARTER(4, Math.PI),
        THIRD_QUARTER(4, 3 * Math.PI / 2),
        FOURTH_QUARTER(4, 0d),
        FIRST_HALF(2, Math.PI / 2),
        SECOND_HALF(2, Math.PI),
        THIRD_HALF(2, 3 * Math.PI / 2),
        FOURTH_HALF(2, 0d);

        private int ratio;
        private double additionalAngle;

        private Part(int ratio, double addAngle) {
            this.ratio = ratio;
            this.additionalAngle = addAngle;
        }
    }

    public static void drawCircle(double x, double y, double radius, Color color) {
        drawSetup();
        applyColor(color);

        glBegin(GL_TRIANGLE_FAN);
        for (int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(ANGLE * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glLineWidth(1.5f);
        glEnable(GL_LINE_SMOOTH);

        glBegin(GL_LINE_LOOP);
        for (int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(ANGLE * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    // progress [1;100]
    // direction: 1 - по часовой стрелке; 0 - против часовой стрелки
    public static void drawCircle(double x, double y, double radius, int progress, int direction, Color color) {
        double angle1 = direction == 0 ? ANGLE : -ANGLE;
        float steps = (STEPS / 100f) * progress;

        drawSetup();
        applyColor(color);

        glBegin(GL_TRIANGLE_FAN);
        glVertex2d(x, y);
        for (int i = 0; i <= steps; i++) {
            glVertex2d(x + radius * Math.sin(angle1 * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glLineWidth(1.5f);
        glEnable(GL_LINE_SMOOTH);

        glBegin(GL_LINE_LOOP);
        glVertex2d(x, y);
        for (int i = 0; i <= steps; i++) {
            glVertex2d(x + radius * Math.sin(angle1 * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    public static void drawCirclePart(double x, double y, double radius, Part part, Color color) {
        double angle = ANGLE / part.ratio;

        drawSetup();
        applyColor(color);

        glBegin(GL_TRIANGLE_FAN);
        glVertex2d(x, y);
        for (int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(part.additionalAngle + angle * i),
                    y + radius * Math.cos(part.additionalAngle + angle * i)
            );
        }
        glEnd();

        glLineWidth(1.5f);
        glEnable(GL_LINE_SMOOTH);

        glBegin(GL_LINE_LOOP);
        glVertex2d(x, y);
        for (int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(part.additionalAngle + angle * i),
                    y + radius * Math.cos(part.additionalAngle + angle * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    public static void drawBlurredCircle(double x, double y, double radius, double blurRadius, Color color) {
        Color transparent = ColorUtil.injectAlpha(color, 0);

        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);
        glShadeModel(GL_SMOOTH);
        applyColor(color);

        glBegin(GL_TRIANGLE_FAN);
        for (int i = 0; i <= EX_STEPS; i++) {
            glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                    y + radius * Math.cos(EX_ANGLE * i)
            );
        }
        glEnd();

        glBegin(GL_TRIANGLE_STRIP);
        for (int i = 0; i <= EX_STEPS + 1; i++) {
            if (i % 2 == 1) {
                applyColor(transparent);
                glVertex2d(x + (radius + blurRadius) * Math.sin(EX_ANGLE * i),
                        y + (radius + blurRadius) * Math.cos(EX_ANGLE * i));
            } else {
                applyColor(color);
                glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                        y + radius * Math.cos(EX_ANGLE * i));
            }
        }
        glEnd();

        glShadeModel(GL_FLAT);
        glDisable(GL_ALPHA_TEST);
        drawFinish();
    }

    public static void drawCircleOutline(double x, double y, double radius, float thikness, Color color) {
        drawSetup();
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(thikness);
        applyColor(color);

        glBegin(GL_LINE_LOOP);
        for (int i = 0; i <= STEPS; i++) {
            glVertex2d(x + radius * Math.sin(ANGLE * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    // progress [1;100]
    // direction: 1 - по часовой стрелке; 0 - против часовой стрелки
    public static void drawCircleOutline(double x, double y, double radius, float thikness, int progress, int direction, Color color) {
        double angle1 = direction == 0 ? ANGLE : -ANGLE;
        float steps = (STEPS / 100f) * progress;

        drawSetup();
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(thikness);
        applyColor(color);

        glBegin(GL_LINE_STRIP);
        for (int i = 0; i <= steps; i++) {
            glVertex2d(x + radius * Math.sin(angle1 * i),
                    y + radius * Math.cos(ANGLE * i)
            );
        }
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        drawFinish();
    }

    public static void drawRainbowCircle(double x, double y, double radius, double blurRadius) {
        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);
        glShadeModel(GL_SMOOTH);
        applyColor(Color.WHITE);

        glBegin(GL_TRIANGLE_FAN);
        glVertex2d(x, y);
        for (int i = 0; i <= EX_STEPS; i++) {
            applyColor(Color.getHSBColor((float) i / EX_STEPS, 1f, 1f));
            glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                    y + radius * Math.cos(EX_ANGLE * i)
            );
        }
        glEnd();

        glBegin(GL_TRIANGLE_STRIP);
        for (int i = 0; i <= EX_STEPS + 1; i++) {
            if (i % 2 == 1) {
                applyColor(ColorUtil.injectAlpha(Color.getHSBColor((float) i / EX_STEPS, 1f, 1f), 0));
                glVertex2d(x + (radius + blurRadius) * Math.sin(EX_ANGLE * i),
                        y + (radius + blurRadius) * Math.cos(EX_ANGLE * i));
            } else {
                applyColor(Color.getHSBColor((float) i / EX_STEPS, 1f, 1f));
                glVertex2d(x + radius * Math.sin(EX_ANGLE * i),
                        y + radius * Math.cos(EX_ANGLE * i));
            }
        }
        glEnd();

        glShadeModel(GL_FLAT);
        glDisable(GL_ALPHA_TEST);
        drawFinish();
    }

    public static void drawRect(double x, double y, double width, double height, Color color) {
        drawSetup();
        applyColor(color);

        glBegin(GL_QUADS);
        glVertex2d(x, y);
        glVertex2d(x + width, y);
        glVertex2d(x + width, y - height);
        glVertex2d(x, y - height);
        glEnd();

        drawFinish();
    }

    public static void drawGradientRect(double x, double y, double width, double height, Color... clrs) {
        drawSetup();
        glShadeModel(GL_SMOOTH);

        glBegin(GL_QUADS);
        applyColor(clrs[1]);
        glVertex2d(x, y);
        applyColor(clrs[2]);
        glVertex2d(x + width, y);
        applyColor(clrs[3]);
        glVertex2d(x + width, y - height);
        applyColor(clrs[0]);
        glVertex2d(x, y - height);
        glEnd();

        glShadeModel(GL_FLAT);
        drawFinish();
    }

    public static void drawRoundedRect(double x, double y, double width, double height, double radius, Color color) {
        float[] c = ColorUtil.getColorComps(color);

        drawSetup();

        ROUNDED.load();
        ROUNDED.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED.setUniformf("round", (float) radius * 2);
        ROUNDED.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw(x, y, width, height);
        ROUNDED.unload();

        drawFinish();
    }

    public static void drawRoundedHorizontalGradientRect(double x, double y, double width, double height, double radius, Color topLeftColor, Color bottomLeftColor, Color topRightColor, Color bottomRightColor) {
        float[] c = ColorUtil.getColorComps(topLeftColor); //top left
        float[] c1 = ColorUtil.getColorComps(bottomLeftColor); //bottom left
        float[] c2 = ColorUtil.getColorComps(topRightColor); //top right
        float[] c3 = ColorUtil.getColorComps(bottomRightColor); //bottom right

        drawSetup();

        ROUNDED_GRADIENT.load();
        ROUNDED_GRADIENT.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED_GRADIENT.setUniformf("round", (float) radius * 2);
        ROUNDED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3]);
        ROUNDED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3]);
        ROUNDED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3]);
        ROUNDED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3]);
        Shader.draw(x, y, width, height);
        ROUNDED_GRADIENT.unload();

        drawFinish();
    }

    public static void drawRoundedVerticalGradientRect(double x, double y, double width, double height, double radius, Color color1, Color color2) {
        float[] c = ColorUtil.getColorComps(color1); //top left
        float[] c1 = ColorUtil.getColorComps(color2); //bottom left
        float[] c2 = ColorUtil.getColorComps(color1); //top right
        float[] c3 = ColorUtil.getColorComps(color2); //bottom right

        drawSetup();

        ROUNDED_GRADIENT.load();
        ROUNDED_GRADIENT.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED_GRADIENT.setUniformf("round", (float) radius * 2);
        ROUNDED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3]);
        ROUNDED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3]);
        ROUNDED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3]);
        ROUNDED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3]);
        Shader.draw(x, y, width, height);
        ROUNDED_GRADIENT.unload();

        drawFinish();
    }

    public static void drawRoundedHorizontalGradientRect(double x, double y, double width, double height, double radius, Color color1, Color color2) {
        float[] c = ColorUtil.getColorComps(color1); //top left
        float[] c1 = ColorUtil.getColorComps(color1); //bottom left
        float[] c2 = ColorUtil.getColorComps(color2); //top right
        float[] c3 = ColorUtil.getColorComps(color2); //bottom right

        drawSetup();

        ROUNDED_GRADIENT.load();
        ROUNDED_GRADIENT.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED_GRADIENT.setUniformf("round", (float) radius * 2);
        ROUNDED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3]);
        ROUNDED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3]);
        ROUNDED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3]);
        ROUNDED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3]);
        Shader.draw(x, y, width, height);
        ROUNDED_GRADIENT.unload();

        drawFinish();
    }

    public static void drawRoundedBlurredRect(double x, double y, double width, double height, double roundR, float blurR, Color color) {
        float[] c = ColorUtil.getColorComps(color);

        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);

        ROUNDED_BLURRED.load();
        ROUNDED_BLURRED.setUniformf("size", (float) (width + 2 * blurR), (float) (height + 2 * blurR));
        ROUNDED_BLURRED.setUniformf("softness", blurR);
        ROUNDED_BLURRED.setUniformf("radius", (float) roundR);
        ROUNDED_BLURRED.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw(x - blurR, y - blurR, width + blurR * 2, height + blurR * 2);
        ROUNDED_BLURRED.unload();

        glDisable(GL_ALPHA_TEST);
        drawFinish();
    }

    public static void drawRoundedGradientBlurredRect(double x, double y, double width, double height, double roundR, float blurR, Color... colors) {
        float[] c = ColorUtil.getColorComps(colors[0]);
        float[] c1 = ColorUtil.getColorComps(colors[1]);
        float[] c2 = ColorUtil.getColorComps(colors[2]);
        float[] c3 = ColorUtil.getColorComps(colors[3]);

        drawSetup();
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);

        ROUNDED_BLURRED_GRADIENT.load();
        ROUNDED_BLURRED_GRADIENT.setUniformf("size", (float) (width + 2 * blurR), (float) (height + 2 * blurR));
        ROUNDED_BLURRED_GRADIENT.setUniformf("softness", blurR);
        ROUNDED_BLURRED_GRADIENT.setUniformf("radius", (float) roundR);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color1", c[0], c[1], c[2], c[3]);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color2", c1[0], c1[1], c1[2], c1[3]);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color3", c2[0], c2[1], c2[2], c2[3]);
        ROUNDED_BLURRED_GRADIENT.setUniformf("color4", c3[0], c3[1], c3[2], c3[3]);
        Shader.draw(x - blurR, y - blurR, width + blurR * 2, height + blurR * 2);
        ROUNDED_BLURRED_GRADIENT.unload();

        glDisable(GL_ALPHA_TEST);
        drawFinish();
    }

    public static void drawSmoothRect(double x, double y, double width, double height, Color color) {
        drawRoundedRect(x, y, width, height, 1.5, color);
    }

    public static void drawRoundedRectOutline(double x, double y, double width, double height, double radius, float thickness, Color color) {
        float[] c = ColorUtil.getColorComps(color);

        drawSetup();

        ROUNDED_OUTLINE.load();
        ROUNDED_OUTLINE.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED_OUTLINE.setUniformf("round", (float) radius * 2);
        ROUNDED_OUTLINE.setUniformf("thickness", thickness);
        ROUNDED_OUTLINE.setUniformf("color", c[0], c[1], c[2], c[3]);
        Shader.draw(x, y, width, height);
        ROUNDED_OUTLINE.unload();

        drawFinish();
    }

    public static void drawTexture(Identifier identifier, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight) {
        drawTexture(TextureLoader.getTextureId(identifier), x, y, width, height, texX, texY, texWidth, texHeight);
    }

    public static void drawTexture(int texId, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight) {
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        resetColor();

        bindTexture(texId);

        int iWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int iHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
        y -= height;
        texX = texX / iWidth;
        texY = texY / iHeight;
        texWidth = texWidth / iWidth;
        texHeight = texHeight / iHeight;

        glBegin(GL_QUADS);
        glTexCoord2d(texX, texY);
        glVertex2d(x, y);
        glTexCoord2d(texX, texY + texHeight);
        glVertex2d(x, y + height);
        glTexCoord2d(texX + texWidth, texY + texHeight);
        glVertex2d(x + width, y + height);
        glTexCoord2d(texX + texWidth, texY);
        glVertex2d(x + width, y);
        glEnd();

        bindTexture(0);
        disableBlend();
    }

    public static void drawRoundedTexture(Identifier identifier, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight, double radius) {
        drawRoundedTexture(TextureLoader.getTextureId(identifier), x, y, width, height, texX, texY, texWidth, texHeight, radius);
    }

    public static void drawRoundedTexture(int texId, double x, double y, double width, double height, double texX, double texY, double texWidth, double texHeight, double radius) {
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.3f);

        mc.getFramebuffer().beginWrite(false);
        StencilUtils.initStencilReplace();
        drawRoundedRect(x, y, width, height, radius, Color.WHITE);
        StencilUtils.uninitStencilReplace();

        bindTexture(texId);

        int iWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        int iHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
        y -= height;
        texX = texX / iWidth;
        texY = texY / iHeight;
        texWidth = texWidth / iWidth;
        texHeight = texHeight / iHeight;

        glBegin(GL_QUADS);
        glTexCoord2d(texX, texY);
        glVertex2d(x, y);
        glTexCoord2d(texX, texY + texHeight);
        glVertex2d(x, y + height);
        glTexCoord2d(texX + texWidth, texY + texHeight);
        glVertex2d(x + width, y + height);
        glTexCoord2d(texX + texWidth, texY);
        glVertex2d(x + width, y);
        glEnd();

        bindTexture(0);
        glDisable(GL_STENCIL_TEST);
        glDisable(GL_ALPHA_TEST);
        disableBlend();
    }

    public static void drawRoundedTexture(Identifier identifier, double x, double y, double width, double height, double radius) {
        drawRoundedTexture(TextureLoader.getTextureId(identifier), x, y, width, height, radius);
    }

    public static void drawRoundedTexture(int texId, double x, double y, double width, double height, double radius) {
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        resetColor();

        ROUNDED_TEXTURE.load();
        ROUNDED_TEXTURE.setUniformf("size", (float) width * 2, (float) height * 2);
        ROUNDED_TEXTURE.setUniformf("round", (float) radius * 2);
        bindTexture(texId);
        Shader.draw(x, y, width, height);
        bindTexture(0);
        ROUNDED_TEXTURE.unload();

        disableBlend();
    }

    public static void drawGlow(double x, double y, int width, int height, int glowRadius, Color color) {
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0001f);

        bindTexture(getGlowTexture(width, height, glowRadius));
        width += glowRadius * 2;
        height += glowRadius * 2;
        x -= glowRadius;
        y -= glowRadius;

        applyColor(color);
        glBegin(GL_QUADS);
        glTexCoord2d(0, 1);
        glVertex2d(x, y);
        glTexCoord2d(0, 0);
        glVertex2d(x, y + height);
        glTexCoord2d(1, 0);
        glVertex2d(x + width, y + height);
        glTexCoord2d(1, 1);
        glVertex2d(x + width, y);
        glEnd();

        bindTexture(0);
        glDisable(GL_ALPHA_TEST);
        disableBlend();
    }

    public static int getGlowTexture(int width, int height, int blurRadius) {
        int identifier = (width * 401 + height) * 407 + blurRadius;
        int texId = glowCache.getOrDefault(identifier, -1);

        if (texId == -1) {
            BufferedImage original = new BufferedImage((int) (width + blurRadius * 2), (int) (height + blurRadius * 2), BufferedImage.TYPE_INT_ARGB_PRE);

            Graphics g = original.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(blurRadius, blurRadius, (int) width, (int) height);
            g.dispose();

            GlowShader glow = new GlowShader(blurRadius);
            BufferedImage blurred = glow.filter(original, null);
            try {
                texId = TextureLoader.loadTexture2(blurred);
                glowCache.put(identifier, texId);
            } catch (Exception e) {
                SigmaReborn.LOGGER.error(e.getMessage());
            }
        }
        return texId;
    }

    // для scaledHeight, scale - юзайте WINDOW.getGuiScaledHeight(), WINDOW.getGuiScale() если сами не меняли их
    public static void scissor(double x, double y, double width, double height, double scale, double scaledHeight) {
        glScissor((int) (x * scale),
                (int) ((scaledHeight - y) * scale),
                (int) (width * scale),
                (int) (height * scale));
    }

    public static void applyColor(Color color) {
        glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    public static void resetColor() {
        glColor4f(1f, 1f, 1f, 1f);
    }

    public static void enableScissor() {
        glEnable(GL_SCISSOR_TEST);
    }

    public static void disableScissor() {
        glDisable(GL_SCISSOR_TEST);
    }

    public static void drawSetup() {
        disableTexture();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void drawFinish() {
        enableTexture();
        disableBlend();
        resetColor();
    }
}
