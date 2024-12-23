package com.skidders.sigma.processors;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.sigma.events.impl.KeyPressEvent;
import com.skidders.sigma.events.impl.Render2DEvent;
import com.skidders.sigma.screens.ClickGUI;
import com.skidders.sigma.utils.IMinecraft;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ScreenProcessor implements IMinecraft {

    public ClickGUI clickGUI;
    public int clickGuiBind = 344;

    public static int scaleFactor = 1;

    public ScreenProcessor() {
        clickGUI = new ClickGUI("Jello ClickGUI");
    }

    @Subscribe
    public void onKey(KeyPressEvent event) {
        if (event.action == GLFW.GLFW_RELEASE && event.key == clickGuiBind && mc.world != null) {
            mc.openScreen(clickGUI);
        }
    }

    public void onResize() {
        //
    }

    public void renderWatermark() {
        scaleFactor = (int) mc.getWindow().getScaleFactor();

        if (scaleFactor > 2) {
            mc.getTextureManager().bindTexture(new Identifier("sigma-reborn", "jello/jello_watermark2x.png"));
        } else {
            mc.getTextureManager().bindTexture(new Identifier("sigma-reborn", "jello/jello_watermark.png"));
        }

        int x = 0, y = 0;
        if (mc.options.debugEnabled) {
            x = mc.getWindow().getScaledWidth() / 2 - (170 / 2) / 2;
        }

        float width = ((float) 170 / 2);
        float height = ((float) 104 / 2);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        DrawableHelper.drawTexture(new MatrixStack(), x, y, 0, 0, (int) width, (int) height, (int) width, (int) height);

        RenderSystem.disableBlend();
    }

}
