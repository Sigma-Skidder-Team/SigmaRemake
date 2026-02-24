package io.github.sst.remake.manager.impl;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.event.impl.game.render.Render2DEvent;
import io.github.sst.remake.event.impl.game.render.Render3DEvent;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.render.shader.impl.SigmaBlurShader;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.shader.ShaderUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public final class HUDManager extends Manager implements IMinecraft {
    private static ShaderEffect blurShaderGroup;

    private static Framebuffer blurFramebuffer;
    private static Framebuffer blurSwapFramebuffer;

    private static int maxBlurX = 0;
    private static int maxBlurY = 0;
    private static int blurWidth = client.getFramebuffer().viewportWidth;
    private static int blurHeight = client.getFramebuffer().viewportHeight;

    @Subscribe(priority = Priority.HIGHEST)
    public void onRender(Render2DEvent event) {
        RenderSystem.pushMatrix();

        double localScaleFactor = client.getWindow().getScaleFactor() / (double) ((float) Math.pow(client.getWindow().getScaleFactor(), 2.0));
        GL11.glScaled(localScaleFactor, localScaleFactor, 1.0);
        GL11.glScaled(Client.INSTANCE.screenManager.scaleFactor, Client.INSTANCE.screenManager.scaleFactor, 1.0);
        RenderSystem.disableDepthTest();
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0F, 0.0F, 1000.0F);

        if (client.world != null) {
            GL11.glDisable(GL11.GL_LIGHTING);
            int x = 0;
            int y = 0;

            int imageWidth = 170;

            if (client.options.debugEnabled) {
                x = client.getWindow().getWidth() / 2 - imageWidth / 2;
            }

            if (!(Client.INSTANCE.screenManager.scaleFactor > 1.0F)) {
                client.getTextureManager().bindTexture(Resources.WATERMARK);
            } else {
                client.getTextureManager().bindTexture(Resources.WATERMARK_2X);
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            DrawableHelper.drawTexture(new MatrixStack(), x, y, 0, 0, (int) 170.0F, (int) 104.0F, (int) 170.0F, (int) 104.0F);

            RenderSystem.disableBlend();

            new RenderClient2DEvent().call();
        }

        if (Client.INSTANCE.screenManager.currentScreen != null && client.overlay == null) {
            Client.INSTANCE.screenManager.currentScreen.draw(1.0F);
        }

        RenderSystem.popMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.1F);

        RenderSystem.popMatrix();
    }

    @Subscribe
    public void onPacketReceive(ReceivePacketEvent event) {
        if (event.packet instanceof CloseScreenS2CPacket) {
            ShaderUtils.resetShader();
        }
    }

    @Subscribe(priority = Priority.LOWEST)
    public void onRender3D(Render3DEvent event) {
        if (Client.INSTANCE.configManager.hqBlur
                && blurWidth < maxBlurX
                && blurHeight < maxBlurY) {
            if (blurFramebuffer == null) {
                try {
                    blurShaderGroup = new ShaderEffect(
                            client.getTextureManager(),
                            new SigmaBlurShader(),
                            client.getFramebuffer(),
                            new Identifier("jelloblur")
                    );

                    blurShaderGroup.setupDimensions(
                            client.getFramebuffer().viewportWidth,
                            client.getFramebuffer().viewportHeight
                    );

                    blurShaderGroup.passes.get(0)
                            .getProgram()
                            .getUniformByName("Radius")
                            .set(35.0F);

                    blurShaderGroup.passes.get(1)
                            .getProgram()
                            .getUniformByName("Radius")
                            .set(35.0F);

                    blurFramebuffer = blurShaderGroup.getSecondaryTarget("jello");
                    blurSwapFramebuffer = blurShaderGroup.getSecondaryTarget("jelloswap");

                } catch (IOException | JsonSyntaxException e) {
                    Client.LOGGER.error("Failed to create an instance of the blur shader");
                }
            }

            if (blurFramebuffer.viewportWidth != client.getFramebuffer().viewportWidth
                    || blurFramebuffer.viewportHeight != client.getFramebuffer().viewportHeight) {
                blurShaderGroup.setupDimensions(
                        client.getFramebuffer().viewportWidth,
                        client.getFramebuffer().viewportHeight
                );
            }

            RenderSystem.blendFuncSeparate(
                    GlStateManager.SrcFactor.SRC_ALPHA,
                    GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SrcFactor.ONE,
                    GlStateManager.DstFactor.ZERO
            );

            RenderSystem.enableBlend();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            RenderSystem.disableBlend();

            blurFramebuffer.clear(true);
            blurSwapFramebuffer.clear(true);

            RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
            RenderSystem.matrixMode(GL11.GL_PROJECTION);
            RenderSystem.loadIdentity();
            RenderSystem.ortho(
                    0.0,
                    client.getWindow().getFramebufferWidth() / client.getWindow().getScaleFactor(),
                    client.getWindow().getFramebufferHeight() / client.getWindow().getScaleFactor(),
                    0.0,
                    1000.0,
                    3000.0
            );

            RenderSystem.matrixMode(GL11.GL_MODELVIEW);
            RenderSystem.loadIdentity();
            RenderSystem.translatef(0.0F, 0.0F, -2000.0F);

            GL11.glScaled(
                    1.0 / client.getWindow().getScaleFactor() * Client.INSTANCE.screenManager.scaleFactor,
                    1.0 / client.getWindow().getScaleFactor() * Client.INSTANCE.screenManager.scaleFactor,
                    1.0
            );

            int blurRadius = 35;
            ScissorUtils.startScissor(
                    blurWidth,
                    blurHeight - blurRadius,
                    maxBlurX,
                    maxBlurY + blurRadius
            );

            blurShaderGroup.render(client.renderTickCounter.tickDelta);
            ScissorUtils.restoreScissor();

            GL11.glEnable(GL11.GL_ALPHA_TEST);

            blurFramebuffer.beginWrite(true);
            client.getFramebuffer().beginWrite(true);
        }

        blurWidth = client.getFramebuffer().viewportWidth;
        blurHeight = client.getFramebuffer().viewportHeight;
        maxBlurX = 0;
        maxBlurY = 0;
    }

    public static void registerBlurArea(int x, int y, int width, int height) {
        blurWidth = Math.min(x, blurWidth);
        blurHeight = Math.min(y, blurHeight);
        maxBlurX = Math.max(x + width, maxBlurX);
        maxBlurY = Math.max(y + height, maxBlurY);
    }

    public static void renderFinalBlur() {
        if (blurFramebuffer == null) {
            return;
        }

        GL11.glPushMatrix();
        blurFramebuffer.beginRead();
        blurFramebuffer.draw(
                client.getFramebuffer().viewportWidth,
                client.getFramebuffer().viewportHeight
        );
        GL11.glPopMatrix();

        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(
                0.0,
                client.getWindow().getFramebufferWidth() / client.getWindow().getScaleFactor(),
                client.getWindow().getFramebufferHeight() / client.getWindow().getScaleFactor(),
                0.0,
                1000.0,
                3000.0
        );

        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);

        GL11.glScaled(
                1.0 / client.getWindow().getScaleFactor() * Client.INSTANCE.screenManager.scaleFactor,
                1.0 / client.getWindow().getScaleFactor() * Client.INSTANCE.screenManager.scaleFactor,
                1.0
        );

        client.getFramebuffer().beginWrite(true);
    }

}
