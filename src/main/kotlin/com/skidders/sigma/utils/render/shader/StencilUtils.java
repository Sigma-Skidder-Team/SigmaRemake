package com.skidders.sigma.utils.render.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.skidders.sigma.utils.IMinecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.HashMap;

public class StencilUtils implements IMinecraft {

    private static final HashMap<Integer, FloatBuffer> kernelCache = new HashMap<>();

    public static FloatBuffer getKernel(int radius) {
        FloatBuffer buffer = kernelCache.get(radius);
        if (buffer == null) {
            buffer = BufferUtils.createFloatBuffer(radius);
            float[] kernel = new float[radius];
            float sigma = radius / 2.0F;
            float total = 0.0F;
            for (int i = 0; i < radius; i++) {
                float multiplier = i / sigma;
                kernel[i] = 1.0F / (Math.abs(sigma) * 2.50662827463F) * (float) Math.exp(-0.5 * multiplier * multiplier);
                total += i > 0 ? kernel[i] * 2 : kernel[0];
            }
            for (int i = 0; i < radius; i++) {
                kernel[i] /= total;
            }
            buffer.put(kernel);
            buffer.flip();
            kernelCache.put(radius, buffer);
        }
        return buffer;
    }

    public static void initStencilReplace() {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GlStateManager.colorMask(false, false, false, false);
    }

    public static void uninitStencilReplace() {
        GlStateManager.colorMask(true, true, true, true);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1);
    }
}
	