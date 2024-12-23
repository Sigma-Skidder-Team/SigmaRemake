package lexi.fontrenderer.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lexi.fontrenderer.Renderer;
import lexi.fontrenderer.data.CharacterData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class GLUtils {

    public static void drawChar(final char character, final CharacterData[] characterData, final float x, final float y) {
        if (character >= characterData.length) return;

        final CharacterData charData = characterData[character];
        charData.bind();

        GL11.glBegin(6);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2d(x, y);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2d(x, y + charData.height());
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2d(x + charData.width(), y + charData.height());
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2d(x + charData.width(), y);
        GL11.glEnd();
    }

    public static void drawLine(final Vec2 start, final Vec2 end) {
        GL11.glDisable(3553);
        GL11.glLineWidth((float) 3.0);
        GL11.glBegin(1);
        GL11.glVertex2f(start.x, start.y);
        GL11.glVertex2f(end.x, end.y);
        GL11.glEnd();
        GL11.glEnable(3553);
    }

    public static void drawLine(final Vec2 start, final Vec2 end, final float width) {
        GL11.glDisable(3553);
        GL11.glLineWidth(width);
        GL11.glBegin(1);
        GL11.glVertex2f(start.x, start.y);
        GL11.glVertex2f(end.x, end.y);
        GL11.glEnd();
        GL11.glEnable(3553);
    }

    public static void enableAlphaBlending() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static void ensureOpenGLContext() {
        if (GLFW.glfwGetCurrentContext() == 0L) {
            GLFW.glfwMakeContextCurrent(Minecraft.getInstance().getWindow().getWindow());
            GL.createCapabilities();
        }
    }

}
