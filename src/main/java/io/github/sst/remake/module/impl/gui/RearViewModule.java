package io.github.sst.remake.module.impl.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.render.RenderLevelEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.util.game.combat.RotationUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SuppressWarnings({"deprecation", "unused"})
public class RearViewModule extends Module {
    public static boolean RENDERING_REAR_VIEW = false;

    private final AnimationUtils visibilityAnimation = new AnimationUtils(230, 200, AnimationUtils.Direction.BACKWARDS);
    private Framebuffer rearViewFramebuffer;
    private int smartVisibilityTicks = 0;

    private final BooleanSetting showInGui = new BooleanSetting("Show in GUI", "Makes the Rear View visible in guis", false);
    private final BooleanSetting smartVisibility = new BooleanSetting("Smart visibility", "Only pops up when a player is behind you", false);
    private final SliderSetting size = new SliderSetting("Size", "Width", 400, 120, 1000, 1);

    public RearViewModule() {
        super("RearView", "Lets you see what's going on behind you.", Category.GUI);
    }

    @Override
    public void onEnable() {
        rebuild();
    }

    @Override
    public void onDisable() {
        visibilityAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        if (rearViewFramebuffer != null
                && (rearViewFramebuffer.viewportWidth != client.getWindow().getFramebufferWidth()
                || rearViewFramebuffer.viewportHeight != client.getWindow().getFramebufferHeight())) {
            rebuild();
        }

        if (!smartVisibility.value) {
            return;
        }

        List<PlayerEntity> nearbyPlayersBehindYou = client.world.getEntitiesByClass(
                PlayerEntity.class,
                client.player.getBoundingBox().expand(14.0),
                player -> player.distanceTo(client.player) < 12.0F
                        && !isEntityWithinViewAngle(player)
                        && client.player != player
        );

        if (nearbyPlayersBehindYou.isEmpty()) {
            if (smartVisibilityTicks > 0) {
                smartVisibilityTicks--;
            }
        } else {
            smartVisibilityTicks = 5;
        }
    }

    @Subscribe
    public void onRender(RenderClient2DEvent event) {
        if (rearViewFramebuffer == null) return;
        if (client.options.hudHidden) return;

        if (!smartVisibility.value) {
            boolean inScreenAndNotAllowed = client.currentScreen != null && !showInGui.value;
            visibilityAnimation.changeDirection(inScreenAndNotAllowed
                    ? AnimationUtils.Direction.FORWARDS
                    : AnimationUtils.Direction.BACKWARDS);
        } else {
            visibilityAnimation.changeDirection(smartVisibilityTicks <= 0
                    ? AnimationUtils.Direction.FORWARDS
                    : AnimationUtils.Direction.BACKWARDS);
        }

        float aspectRatio = (float) client.getWindow().getWidth() / (float) client.getWindow().getHeight();

        int rearViewWidth = size.value.intValue();
        int rearViewHeight = (int) ((float) rearViewWidth / aspectRatio);

        int padding = 10;
        int yOffset = -padding - rearViewHeight;

        float anim = visibilityAnimation.calcPercent();

        if (anim == 0.0F) {
            return;
        }

        if (visibilityAnimation.getDirection() != AnimationUtils.Direction.BACKWARDS) {
            yOffset = (int) ((float) yOffset * VecUtils.interpolate(anim, 0.49, 0.59, 0.16, 1.04));
        } else {
            yOffset = (int) ((float) yOffset * VecUtils.interpolate(anim, 0.3, 0.88, 0.47, 1.0));
        }

        RenderUtils.drawRoundedRect(
                (float) (client.getWindow().getWidth() - padding - rearViewWidth),
                (float) (client.getWindow().getHeight() + yOffset),
                (float) rearViewWidth,
                (float) (rearViewHeight - 1),
                14.0F,
                anim
        );

        int scaledWidth = (int) (rearViewWidth * Client.INSTANCE.screenManager.scaleFactor);
        int scaledHeight = (int) (rearViewHeight * Client.INSTANCE.screenManager.scaleFactor);
        int scaledPadding = (int) (padding * Client.INSTANCE.screenManager.scaleFactor);
        int scaledYOffset = (int) (yOffset * Client.INSTANCE.screenManager.scaleFactor);

        RenderSystem.pushMatrix();
        blitFramebufferToScreen(
                rearViewFramebuffer,
                scaledWidth,
                scaledHeight,
                client.getWindow().getFramebufferWidth() - scaledPadding - scaledWidth,
                client.getWindow().getFramebufferHeight() + scaledYOffset
        );
        RenderSystem.popMatrix();

        // Restore UI projection after custom ortho/viewport.
        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(
                0.0,
                (double) client.getWindow().getFramebufferWidth() / client.getWindow().getScaleFactor(),
                (double) client.getWindow().getFramebufferHeight() / client.getWindow().getScaleFactor(),
                0.0,
                1000.0,
                3000.0
        );
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
        GL11.glScaled(
                1.0 / client.getWindow().getScaleFactor() * (double) Client.INSTANCE.screenManager.scaleFactor,
                1.0 / client.getWindow().getScaleFactor() * (double) Client.INSTANCE.screenManager.scaleFactor,
                1.0
        );

        RenderSystem.viewport(0, 0, client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());
        client.getFramebuffer().beginWrite(true);
    }

    @Subscribe
    public void onRenderLevel(RenderLevelEvent event) {
        if (rearViewFramebuffer == null) {
            rebuild();
            return;
        }
        if (client.world == null || client.player == null) {
            return;
        }

        if (client.currentScreen != null
                && !showInGui.value
                && smartVisibilityTicks == 0) {
            return;
        }

        rearViewFramebuffer.beginWrite(true);
        RenderSystem.clear(16640, false);
        RenderSystem.enableTexture();
        RenderSystem.enableColorMaterial();
        RenderSystem.enableDepthTest();
        GL11.glAlphaFunc(519, 0.0F);

        float originalYaw = client.player.yaw;
        double originalFov = client.options.fov;
        boolean originalRenderHand = client.gameRenderer.renderHand;
        Framebuffer originalOutlineFbo = client.worldRenderer.entityOutlinesFramebuffer;

        try {
            RENDERING_REAR_VIEW = true;
            client.player.yaw += 180.0F;
            client.options.fov = 114.0;
            client.gameRenderer.renderHand = false;
            client.worldRenderer.entityOutlinesFramebuffer = null;

            client.gameRenderer.renderWorld(event.tickDelta, Util.getMeasuringTimeNano(), new MatrixStack());
        } finally {
            RENDERING_REAR_VIEW = false;
            client.worldRenderer.entityOutlinesFramebuffer = originalOutlineFbo;
            client.gameRenderer.renderHand = originalRenderHand;
            client.options.fov = originalFov;
            client.player.yaw = originalYaw;
            rearViewFramebuffer.endWrite();
        }

        client.getFramebuffer().beginWrite(true);
    }

    private boolean isEntityWithinViewAngle(LivingEntity targetEntity) {
        float yawToTarget = RotationUtils.getRotationsToEntityFrom(
                targetEntity,
                client.player.getX(),
                client.player.getY(),
                client.player.getZ()
        )[0];

        return RotationUtils.getWrappedAngleDifference(client.player.yaw, yawToTarget) <= 90.0F;
    }

    private void blitFramebufferToScreen(Framebuffer source, int width, int height, double posX, double posY) {
        posY = posY - (double) client.getWindow().getFramebufferHeight() + (double) height;

        RenderSystem.colorMask(true, true, true, false);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0, (double) width + posX, height, 0.0, 1000.0, 3000.0);

        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);

        RenderSystem.viewport(0, 0, width + (int) posX, height - (int) posY);

        RenderSystem.enableTexture();
        RenderSystem.disableLighting();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
        RenderSystem.enableColorMaterial();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        source.beginRead();

        float w = (float) width;
        float h = (float) height;
        float uMax = (float) source.viewportWidth / (float) source.textureWidth;
        float vMax = (float) source.viewportHeight / (float) source.textureHeight;

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(7, VertexFormats.POSITION_COLOR_TEXTURE);
        buffer.vertex(posX, (double) h + posY, 0.0).color(255, 255, 255, 255).texture(0.0F, 0.0F).next();
        buffer.vertex((double) w + posX, (double) h + posY, 0.0).color(255, 255, 255, 255).texture(uMax, 0.0F).next();
        buffer.vertex((double) w + posX, posY, 0.0).color(255, 255, 255, 255).texture(uMax, vMax).next();
        buffer.vertex(posX, posY, 0.0).color(255, 255, 255, 255).texture(0.0F, vMax).next();
        tessellator.draw();

        source.endRead();

        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
    }

    private void rebuild() {
        rearViewFramebuffer = new Framebuffer(
                client.getWindow().getFramebufferWidth(),
                client.getWindow().getFramebufferHeight(),
                true,
                MinecraftClient.IS_SYSTEM_MAC
        );
        rearViewFramebuffer.setClearColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}