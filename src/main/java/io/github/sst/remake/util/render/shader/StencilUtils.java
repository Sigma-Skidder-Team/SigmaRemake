package io.github.sst.remake.util.render.shader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

public class StencilUtils {
    public static void beginStencilWrite() {
        GL11.glPushMatrix();
        resetFramebufferDepth();
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);
        GL11.glStencilFunc(GL11.GL_ACCUM_BUFFER_BIT, 1, 1);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(1);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
    }

    public static void beginStencilRead() {
        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glStencilMask(0);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1);
    }

    public static void endStencil() {
        GL11.glStencilMask(-1);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glPopMatrix();
    }

    private static void recreateFramebufferDepth(Framebuffer framebuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(framebuffer.depthAttachment);

        int depthBufferId = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(36161, depthBufferId);
        EXTFramebufferObject.glRenderbufferStorageEXT(
                36161,
                34041,
                MinecraftClient.getInstance().getWindow().getFramebufferWidth(),
                MinecraftClient.getInstance().getWindow().getFramebufferHeight()
        );
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, depthBufferId);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, depthBufferId);
    }

    private static void resetFramebufferDepth() {
        Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();

        if (framebuffer != null && framebuffer.depthAttachment > -1) {
            recreateFramebufferDepth(framebuffer);
            framebuffer.depthAttachment = -1;
        }
    }
}