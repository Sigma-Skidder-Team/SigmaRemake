package io.github.sst.remake.util.render.font;

import java.awt.Font;

import io.github.sst.remake.util.IMinecraft;
import net.minecraft.client.util.math.MatrixStack;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.Color;
import org.lwjgl.opengl.GL11;

public class DefaultClientFont extends TrueTypeFont implements IMinecraft {
    public final int size;

    public DefaultClientFont(int size) {
        super(new Font("Arial", Font.PLAIN, size), false);
        this.size = size;
    }

    @Override
    public int getWidth(String input) {
        return client.textRenderer.getWidth(input) * this.size;
    }

    @Override
    public int getHeight() {
        return 9 * this.size;
    }

    @Override
    public int getHeight(String input) {
        return 9 * this.size;
    }

    @Override
    public int getLineHeight() {
        return 9 * this.size;
    }

    @Override
    public void drawString(float x, float y, String string, Color color) {
        this.drawString(x, y, string, color, 0, string.length() - 1);
    }

    @Override
    public void drawString(float x, float y, String string, Color color, int startIndex, int endIndex) {
        GL11.glPushMatrix();
        GL11.glScalef((float) this.size, (float) this.size, 0.0F);
        GL11.glTranslatef(-x / (float) this.size, -y / (float) this.size + 1.0F, 0.0F);
        client
                .textRenderer
                .draw(
                        string,
                        x,
                        y,
                        new java.awt.Color(color.r, color.g, color.b, color.a).getRGB(),
                        new MatrixStack().peek().getModel(),
                        false,
                        false
                );
        GL11.glPopMatrix();
    }

    @Override
    public void drawString(float x, float y, String string) {
        this.drawString(x, y, string, Color.white);
    }
}
