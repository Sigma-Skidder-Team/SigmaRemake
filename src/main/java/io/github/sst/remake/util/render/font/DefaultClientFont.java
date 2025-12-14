package io.github.sst.remake.util.render.font;

import java.awt.Font;

import io.github.sst.remake.util.IMinecraft;
import net.minecraft.client.util.math.MatrixStack;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.Color;
import org.lwjgl.opengl.GL11;

public class DefaultClientFont extends TrueTypeFont implements IMinecraft {
    public final int field31945;

    public DefaultClientFont(int var1) {
        super(new Font("Arial", Font.PLAIN, var1), false);
        this.field31945 = var1;
    }

    private int method23949(char var1) {
        return client.textRenderer.getWidth(String.valueOf(var1)) * this.field31945;
    }

    @Override
    public int getWidth(String var1) {
        return client.textRenderer.getWidth(var1) * this.field31945;
    }

    @Override
    public int getHeight() {
        return 9 * this.field31945;
    }

    @Override
    public int getHeight(String var1) {
        return 9 * this.field31945;
    }

    @Override
    public int getLineHeight() {
        return 9 * this.field31945;
    }

    @Override
    public void drawString(float x, float y, String string, Color color) {
        this.drawString(x, y, string, color, 0, string.length() - 1);
    }

    @Override
    public void drawString(float x, float y, String string, Color color, int startIndex, int endIndex) {
        GL11.glPushMatrix();
        GL11.glScalef((float) this.field31945, (float) this.field31945, 0.0F);
        GL11.glTranslatef(-x / (float) this.field31945, -y / (float) this.field31945 + 1.0F, 0.0F);
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
