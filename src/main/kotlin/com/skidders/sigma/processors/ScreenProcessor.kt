package com.skidders.sigma.processors;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.sigma.events.impl.KeyPressEvent;
import com.skidders.sigma.screens.clickgui.ClickGUI;
import com.skidders.sigma.utils.IMinecraft
import com.skidders.sigma.utils.IMinecraft.mc
import com.skidders.sigma.utils.misc.StringUtil;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

class ScreenProcessor : IMinecraft {

    var clickGUI: ClickGUI = ClickGUI("Jello ClickGUI");
    val clickGuiBind: Int = 344;
    var clickGuiBindName: String;

    var guiBlur = true
    var gpuAccelerated = true;

    var guiScaleFactor = 1;
    var resizingScaleFactor = 1.0F;

    constructor() {;
        clickGuiBindName = StringUtil.convertKeyToName(clickGuiBind);

        this.resizingScaleFactor = (mc.window.framebufferHeight / mc.window.height).toFloat();
    }

    @Subscribe
    fun onKey(event: KeyPressEvent) {
        if (event.action == GLFW.GLFW_RELEASE && event.key == clickGuiBind && mc.world != null && mc.currentScreen == null) {
            mc.openScreen(clickGUI);
            clickGuiBindName = StringUtil.convertKeyToName(clickGuiBind);
        }
    }

    fun onResize() {
        if (mc.window.width != 0 && mc.window.height != 0) {
            resizingScaleFactor = (mc.window.framebufferWidth / mc.window.width).coerceAtLeast(mc.window.framebufferHeight / mc.window.height)
                .toFloat();
        }
    }

    fun renderWatermark() {
        guiScaleFactor = mc.window.scaleFactor.toInt();

        if (guiScaleFactor > 2) {
            mc.textureManager.bindTexture(Identifier("sigma-reborn", "jello/jello_watermark2x.png"));
        } else {
            mc.textureManager.bindTexture(Identifier("sigma-reborn", "jello/jello_watermark.png"));
        }

        var x = 0
        var y = 0
        if (mc.options.debugEnabled) {
            x = mc.window.scaledWidth / 2 - (170 / 2) / 2;
        }

        val width = (170 / 2);
        val height = ((104 / 2));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        DrawableHelper.drawTexture(MatrixStack(), x, y, 0f, 0f,  width, height, width, height);

        RenderSystem.disableBlend();
    }

}
