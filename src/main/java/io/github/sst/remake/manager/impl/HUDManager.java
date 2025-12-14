package io.github.sst.remake.manager.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.Client;
import io.github.sst.remake.bus.Priority;
import io.github.sst.remake.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.render.Render2DEvent;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

public class HUDManager extends Manager implements IMinecraft {

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
                client.getTextureManager().bindTexture(new Identifier("sigma", "watermark/jello_watermark.png"));
            } else {
                client.getTextureManager().bindTexture(new Identifier("sigma", "watermark/jello_watermark2x.png"));
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            DrawableHelper.drawTexture(new MatrixStack(), x, y, 0, 0, (int) 170.0F, (int) 104.0F, (int) 170.0F, (int) 104.0F);

            RenderSystem.disableBlend();

            new RenderClient2DEvent().call();
        }

        RenderSystem.popMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.1F);

        RenderSystem.popMatrix();
    }

}
