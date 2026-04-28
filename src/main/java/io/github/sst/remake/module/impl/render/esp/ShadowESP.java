package io.github.sst.remake.module.impl.render.esp;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.sst.remake.util.porting.StateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.render.HasLabelEvent;
import io.github.sst.remake.event.impl.game.render.Render3DEvent;
import io.github.sst.remake.event.impl.game.render.RenderEntityEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.module.impl.render.ESPModule;
import io.github.sst.remake.util.render.shader.StencilUtils;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

@SuppressWarnings("unused")
public class ShadowESP extends SubModule {
    private final VertexConsumerProvider.Immediate renderBuffer = VertexConsumerProvider.immediate(client.getBufferBuilders().entityBuilders, new BufferBuilder(256));
    private RenderState currentRenderMode = RenderState.DEFAULT;

    public ShadowESP() {
        super("Shadow");
    }

    @Override
    public ESPModule getParent() {
        return (ESPModule) super.getParent();
    }

    @Subscribe
    public void onRenderEntity(RenderEntityEvent event) {
        if (currentRenderMode != RenderState.DEFAULT) {
            event.render = false;
        }
    }

    @Subscribe
    public void onRenderNameTag(HasLabelEvent event) {
        if (event.entity instanceof PlayerEntity && currentRenderMode != RenderState.DEFAULT) {
            event.cancel();
        }
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (client.world == null) return;

        getParent().setup();
        StencilUtils.beginStencilWrite();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        applyRenderMode(RenderState.PRE_RENDER);
        StencilUtils.configureStencilTest(StencilUtils.RenderShapeMode.OUTLINE);
        GL11.glLineWidth(1.0f);

        getParent().renderShadowSprites();
        applyRenderMode(RenderState.OUTLINE);

        StateManager.alphaFunc(GL11.GL_GEQUAL, 0.0f);
        StateManager.enableAlphaTest();

        GL11.glColor4f(1.0f, 0.0f, 1.0f, 0.1f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        StencilUtils.endStencil();
        reset();
        renderBuffer.draw();
    }

    private void renderEntity(Entity entity, double offsetX, double offsetY, double offsetZ, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider typeBuffer) {
        double x = MathHelper.lerp(partialTicks, entity.lastRenderX, entity.getX());
        double y = MathHelper.lerp(partialTicks, entity.lastRenderY, entity.getY());
        double z = MathHelper.lerp(partialTicks, entity.lastRenderZ, entity.getZ());
        float yaw = MathHelper.lerp(partialTicks, entity.prevYaw, entity.yaw);

        client.worldRenderer.entityRenderDispatcher.render(entity, x - offsetX, y - offsetY, z - offsetZ, yaw, partialTicks, matrixStack, typeBuffer, 238);
    }

    public void reset() {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        StateManager.glMultiTexCoord2f(GL13.GL_TEXTURE2, 240.0f, 240.0f);
        client.gameRenderer.getLightmapTextureManager().enable();
        GL11.glLightModelfv(GL11.GL_LIGHT_MODEL_AMBIENT, new float[]{0.4f, 0.4f, 0.4f, 1.0f});
        currentRenderMode = RenderState.DEFAULT;
    }

    private void applyRenderMode(RenderState renderState) {
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        currentRenderMode = renderState;

        int color = getParent().color.getValue();
        float alpha = (float) (color >> 24 & 0xFF) / 255.0f;
        float red = (float) (color >> 16 & 0xFF) / 255.0f;
        float green = (float) (color >> 8 & 0xFF) / 255.0f;
        float blue = (float) (color & 0xFF) / 255.0f;

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glLightModelfv(GL11.GL_LIGHT_MODEL_AMBIENT, new float[]{red, green, blue, alpha});

        StateManager.enableLighting();

        if (currentRenderMode == RenderState.OUTLINE) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
            GL11.glLineWidth(2.0f);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_LIGHTING);
        }

        for (LivingEntity entity : getParent().getTargets()) {
            StateManager.pushMatrix();

            Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
            double renderPosX = cameraPos.getX();
            double renderPosY = cameraPos.getY();
            double renderPosZ = cameraPos.getZ();

            MatrixStack matrixStack = new MatrixStack();
            boolean previousShadowState = client.options.entityShadows;

            StateManager.disableLighting();
            StateManager.color4f(0.0f, 0.0f, 1.0f, 0.5f);
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
            RenderSystem.enableBlend();

            client.options.entityShadows = false;
            int fireTimer = entity.getFireTicks();
            boolean burning = entity.getFlag(0);

            entity.setOnFireFor(0);
            entity.setFlag(0, false);

            this.renderEntity(entity, renderPosX, renderPosY, renderPosZ, client.getTickDelta(), matrixStack, this.renderBuffer);

            entity.setOnFireFor(fireTimer);
            entity.setFlag(0, burning);
            client.options.entityShadows = previousShadowState;
            StateManager.popMatrix();
        }

        this.renderBuffer.draw(RenderLayer.getEntitySolid(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        this.renderBuffer.draw(RenderLayer.getEntityCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        this.renderBuffer.draw(RenderLayer.getEntityCutoutNoCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        this.renderBuffer.draw(RenderLayer.getEntitySmoothCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        this.renderBuffer.draw(RenderLayer.getLines());
        this.renderBuffer.draw();

        if (currentRenderMode == RenderState.OUTLINE) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE);
        }

        currentRenderMode = RenderState.DEFAULT;
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }
}