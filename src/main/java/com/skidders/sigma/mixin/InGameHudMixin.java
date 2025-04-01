package com.skidders.sigma.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.SigmaReborn;
import com.skidders.sigma.event.impl.Render2DEvent;
import com.skidders.sigma.util.client.interfaces.IMinecraft;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin implements IMinecraft {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        {
            matrices.push();
            SigmaReborn.INSTANCE.screenHandler.guiScaleFactor = (int) mc.getWindow().getScaleFactor();

            if (SigmaReborn.INSTANCE.screenHandler.guiScaleFactor > 2) {
                mc.getTextureManager().bindTexture(new Identifier("sigma-reborn", "jello_watermark2x.png"));
            } else {
                mc.getTextureManager().bindTexture(new Identifier("sigma-reborn", "jello_watermark.png"));
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
            matrices.pop();
        }

        new Render2DEvent(matrices).post();
    }
}