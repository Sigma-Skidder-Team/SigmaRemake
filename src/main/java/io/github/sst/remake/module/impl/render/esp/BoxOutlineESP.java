package io.github.sst.remake.module.impl.render.esp;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.render.Render3DEvent;
import io.github.sst.remake.module.SubModule;
import io.github.sst.remake.module.impl.render.ESPModule;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.shader.StencilUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

@SuppressWarnings("unused")
public class BoxOutlineESP extends SubModule {
    public BoxOutlineESP() {
        super("Box Outline");
    }

    @Override
    public ESPModule getParent() {
        return (ESPModule) super.getParent();
    }

    @Subscribe
    public void onRender3D(Render3DEvent event) {
        if (client.world == null) return;

        getParent().setup();
        StencilUtils.beginStencilWrite();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        renderBox(false);
        StencilUtils.configureStencilTest(StencilUtils.RenderShapeMode.OUTLINE);
        GL11.glLineWidth(3.0f);
        GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.0F);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        getParent().renderShadowSprites();

        GL11.glColor4f(1.0f, 0.0f, 1.0f, 0.1f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        renderBox(true);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        StencilUtils.endStencil();
        reset();
    }

    private void reset() {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL13.glMultiTexCoord2f(GL13.GL_TEXTURE2, 240.0f, 240.0f);
        client.gameRenderer.getLightmapTextureManager().enable();
    }

    private void renderBox(boolean inside) {
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        for (LivingEntity target : getParent().getTargets()) {
            GL11.glPushMatrix();
            GL11.glTranslated(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);

            int color = getParent().color.getValue();
            double x = (target.getX() - target.lastRenderX) * (double) client.getTickDelta() - (target.getX() - target.lastRenderX);
            double y = (target.getY() - target.lastRenderY) * (double) client.getTickDelta() - (target.getY() - target.lastRenderY);
            double z = (target.getZ() - target.lastRenderZ) * (double) client.getTickDelta() - (target.getZ() - target.lastRenderZ);

            Box box = target.getBoundingBox().offset(x, y, z).expand(0.1f);

            if (inside) {
                RenderUtils.renderWireframeBox(box, 3.0f, ColorHelper.applyAlpha(color, 0.35f));
            } else {
                RenderUtils.render3DColoredBox(box, ClientColors.LIGHT_GREYISH_BLUE.getColor());
            }

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }
    }
}