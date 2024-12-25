package com.skidders.sigma.utils.render.font.utils;

import com.skidders.sigma.utils.render.font.data.CharacterData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class GLUtils {

    public static void ensureOpenGLContext() {
        if (GLFW.glfwGetCurrentContext() == 0L) {
            GLFW.glfwMakeContextCurrent(MinecraftClient.getInstance().getWindow().getHandle());
            GL.createCapabilities();
        }
    }

    public static void drawChar(char character, CharacterData[] characterData, float x, float y) {
        if (character >= characterData.length) return;

        CharacterData charData = characterData[character];
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

    public static void drawLine(Vec2f start, Vec2f end) {
        GL11.glDisable(3553);
        GL11.glLineWidth((float) 3.0);
        GL11.glBegin(1);
        GL11.glVertex2f(start.x, start.y);
        GL11.glVertex2f(end.x, end.y);
        GL11.glEnd();
        GL11.glEnable(3553);
    }

    public static void drawLine(Vec2f start, Vec2f end, float width) {
        GL11.glDisable(3553);
        GL11.glLineWidth(width);
        GL11.glBegin(1);
        GL11.glVertex2f(start.x, start.y);
        GL11.glVertex2f(end.x, end.y);
        GL11.glEnd();
        GL11.glEnable(3553);
    }

}
