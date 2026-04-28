package io.github.sst.remake.util.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.util.porting.StateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.*;

public class StencilUtils {
    public static void beginStencilWrite() {
        StateManager.pushMatrix();
        resetFramebufferDepth();
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
        RenderSystem.stencilFunc(GL11.GL_ACCUM_BUFFER_BIT, 1, 1);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(1);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
    }

    public static void configureStencilTest(RenderShapeMode mode) {
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.stencilMask(0);
        RenderSystem.stencilFunc(mode == RenderShapeMode.FILLED ? GL11.GL_EQUAL : GL11.GL_NOTEQUAL, 1, 1);
    }

    public static void configureStencilTest() {
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.stencilMask(0);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 1);
    }

    public static void endStencil() {
        RenderSystem.stencilMask(-1);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        StateManager.popMatrix();
    }

    private static void recreateFramebufferDepth(Framebuffer framebuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(framebuffer.depthAttachment);

        int depthBufferId = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(GL30.GL_RENDERBUFFER, depthBufferId);
        EXTFramebufferObject.glRenderbufferStorageEXT(
                GL30.GL_RENDERBUFFER,
                GL30.GL_DEPTH_STENCIL,
                MinecraftClient.getInstance().getWindow().getFramebufferWidth(),
                MinecraftClient.getInstance().getWindow().getFramebufferHeight()
        );
        EXTFramebufferObject.glFramebufferRenderbufferEXT(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBufferId);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBufferId);
    }

    private static void resetFramebufferDepth() {
        Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();

        if (framebuffer != null && framebuffer.depthAttachment > -1) {
            recreateFramebufferDepth(framebuffer);
            framebuffer.depthAttachment = -1;
        }
    }

    public enum RenderShapeMode {
        FILLED,
        OUTLINE
    }
}