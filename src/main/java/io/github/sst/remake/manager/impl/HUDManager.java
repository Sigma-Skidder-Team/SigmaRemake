package io.github.sst.remake.manager.impl;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlStateManager;
import io.github.sst.remake.util.porting.StateManager;
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
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.shader.impl.SigmaBlurShader;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.shader.ShaderUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.texture.TextureManager;
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

    @Subscribe(priority = Priority.HIGH)
    public void onRender(Render2DEvent event) {
        StateManager.pushMatrix();

        double localScaleFactor = client.getWindow().getScaleFactor() / (double) ((float) Math.pow(client.getWindow().getScaleFactor(), 2.0));
        StateManager.scalef((float) localScaleFactor, (float) localScaleFactor, 1.0F);
        StateManager.scalef((float) Client.INSTANCE.screenManager.scaleFactor, (float) Client.INSTANCE.screenManager.scaleFactor, 1.0F);
        RenderSystem.disableDepthTest();
        StateManager.pushMatrix();
        StateManager.translatef(0.0F, 0.0F, 1000.0F);

        if (client.world != null) {
            StateManager.disableLighting();
            int x = 0;
            int y = 0;

            int imageWidth = 170;

            if (client.options.debugEnabled) {
                x = client.getWindow().getWidth() / 2 - imageWidth / 2;
            }

            StateManager.alphaFunc(519, 0.0F);

            RenderUtils.drawImage((float) x, y, 170.0F, 104.0F,
                    !(Client.INSTANCE.screenManager.scaleFactor > 1.0F) ? Resources.WATERMARK
                            : Resources.WATERMARK_2X);

            new RenderClient2DEvent().call();
        }

        if (Client.INSTANCE.screenManager.currentScreen != null && client.overlay == null) {
            Client.INSTANCE.screenManager.currentScreen.draw(1.0F);
        }

        StateManager.popMatrix();
        RenderSystem.enableDepthTest();
        StateManager.enableAlphaTest();
        StateManager.alphaFunc(GL11.GL_GEQUAL, 0.1F);

        client.getTextureManager().bindTexture(TextureManager.MISSING_IDENTIFIER);

        StateManager.popMatrix();
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
            RenderSystem.disableDepthTest();
            StateManager.disableAlphaTest();
            RenderSystem.disableBlend();

            blurFramebuffer.clear(true);
            blurSwapFramebuffer.clear(true);

            RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
            StateManager.matrixMode(GL11.GL_PROJECTION);
            StateManager.loadIdentity();
            StateManager.ortho(
                    0.0,
                    client.getWindow().getFramebufferWidth() / client.getWindow().getScaleFactor(),
                    client.getWindow().getFramebufferHeight() / client.getWindow().getScaleFactor(),
                    0.0,
                    1000.0,
                    3000.0
            );

            StateManager.matrixMode(GL11.GL_MODELVIEW);
            StateManager.loadIdentity();
            StateManager.translatef(0.0F, 0.0F, -2000.0F);

            StateManager.scalef(
                    (float) (1.0 / client.getWindow().getScaleFactor() * Client.INSTANCE.screenManager.scaleFactor),
                    (float) (1.0 / client.getWindow().getScaleFactor() * Client.INSTANCE.screenManager.scaleFactor),
                    1.0F
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

            StateManager.enableAlphaTest();

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

        StateManager.pushMatrix();
        blurFramebuffer.method_35610();
        blurFramebuffer.draw(
                client.getFramebuffer().viewportWidth,
                client.getFramebuffer().viewportHeight
        );
        StateManager.popMatrix();

        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
        StateManager.matrixMode(GL11.GL_PROJECTION);
        StateManager.loadIdentity();
        StateManager.ortho(
                0.0,
                client.getWindow().getFramebufferWidth() / client.getWindow().getScaleFactor(),
                client.getWindow().getFramebufferHeight() / client.getWindow().getScaleFactor(),
                0.0,
                1000.0,
                3000.0
        );

        StateManager.matrixMode(GL11.GL_MODELVIEW);
        StateManager.loadIdentity();
        StateManager.translatef(0.0F, 0.0F, -2000.0F);

        StateManager.scalef(
                (float) (1.0 / client.getWindow().getScaleFactor() * Client.INSTANCE.screenManager.scaleFactor),
                (float) (1.0 / client.getWindow().getScaleFactor() * Client.INSTANCE.screenManager.scaleFactor),
                1.0F
        );

        client.getFramebuffer().beginWrite(true);
    }

}
