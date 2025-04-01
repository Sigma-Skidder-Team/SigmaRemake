package com.skidders.sigma.manager.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.sigma.event.impl.KeyPressEvent;
import com.skidders.sigma.manager.Handler;
import com.skidders.sigma.util.client.events.Listen;
import com.skidders.sigma.util.client.interfaces.IMinecraft;
import com.skidders.sigma.util.system.StringUtil;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ScreenHandler extends Handler<Screen> implements IMinecraft {

    //public ClickGUI clickGUI;
    public int clickGuiBind = 344;
    public String clickGuiBindName;

    public boolean guiBlur = true, gpuAccelerated = true;

    public int guiScaleFactor = 1;
    public float resizingScaleFactor = 1.0F;

    @Override
    public void init() {
        super.init();

        //clickGUI = new ClickGUI("Jello ClickGUI");
        clickGuiBindName = StringUtil.convertKeyToName(clickGuiBind);
        resizingScaleFactor = (float) (mc.getWindow().getFramebufferHeight() / mc.getWindow().getHeight());
    }

    @Listen
    public void onKey(KeyPressEvent event) {
        if (event.action == GLFW.GLFW_RELEASE && event.key == clickGuiBind && mc.world != null && mc.currentScreen == null) {
            //mc.openScreen(clickGUI);
            clickGuiBindName = StringUtil.convertKeyToName(clickGuiBind);
        }
    }

    public void onResize() {
        if (mc.getWindow().getWidth() != 0 && mc.getWindow().getHeight() != 0) {
            resizingScaleFactor = (float) Math.max(
                    mc.getWindow().getFramebufferWidth() / mc.getWindow().getWidth(),
                    mc.getWindow().getFramebufferHeight() / mc.getWindow().getHeight()
            );
        }
    }

    public void renderWatermark() {
        guiScaleFactor = (int) mc.getWindow().getScaleFactor();

        if (guiScaleFactor > 2) {
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
