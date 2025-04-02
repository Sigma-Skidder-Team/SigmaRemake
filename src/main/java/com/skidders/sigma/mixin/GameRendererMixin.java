package com.skidders.sigma.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.SigmaReborn;
import com.skidders.sigma.event.impl.FastRender2DEvent;
import com.skidders.sigma.event.impl.Render3DEvent;
import com.skidders.sigma.util.client.interfaces.IAccessor;
import com.skidders.sigma.util.client.interfaces.IFonts;
import com.skidders.sigma.util.client.render.ColorUtil;
import com.skidders.sigma.util.client.render.RenderUtil;
import com.skidders.sigma.util.client.render.font.FontUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin implements IAccessor {
    @Inject(method = "renderWorld", at = @At(value = "HEAD"))
    public void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo ci) {
        new Render3DEvent(matrix, tickDelta, limitTime).post();
    }

    @Shadow
    private void loadShader(Identifier identifier) {

    }

    @Shadow
    @Final
    private MinecraftClient client;

    @Override
    public void sigmaRemake$invokeLoadShader(Identifier identifier) {
        loadShader(identifier);
    }

    @Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;overlay:Lnet/minecraft/client/gui/screen/Overlay;", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void onRender(float tickDelta, long startTime, boolean tick, CallbackInfo callbackInfo) {
        RenderSystem.pushMatrix();
        if (!SigmaReborn.MODE.equals(SigmaReborn.Mode.NOADDONS)) {
            double scaleFactor = this.client.getWindow().getScaleFactor() / (double) ((float) Math.pow(this.client.getWindow().getScaleFactor(), 2.0));
            GL11.glScaled(scaleFactor, scaleFactor, 1.0);
            GL11.glScaled(SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor, SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor, 1.0);
            RenderSystem.disableDepthTest();
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0F, 0.0F, 1000.0F);

            if (this.client.world != null) {
                GL11.glDisable(GL11.GL_LIGHTING);
                int var3 = 0;
                int var4 = 0;
                int var5 = 170;

                if (client.options.debugEnabled) {
                    var3 = client.getWindow().getWidth() / 2 - var5 / 2;
                }

                if (!SigmaReborn.MODE.equals(SigmaReborn.Mode.JELLO)) {
                    float var7 = 0.5F + 0.0f * 0.5F;
                    GL11.glAlphaFunc(516, 0.1F);
                    RenderUtil.drawRoundedRect2(4.0F, 2.0F, 106.0F, 28.0F, ColorUtil.applyAlpha(ColorUtil.ClientColors.DEEP_TEAL.getColor(), 0.6F * var7));
                    FontUtil.drawString(IFonts.bold22, 9.0F, 2.0F, "Sigma", ColorUtil.applyAlpha(ColorUtil.ClientColors.DEEP_TEAL.getColor(), 0.5F * var7));
                    FontUtil.drawString(
                            IFonts.bold22, 8.0F, 1.0F, "Sigma", ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.getColor(), Math.min(1.0F, var7 * 1.2F))
                    );
                } else {
                    if (!(scaleFactor > 1.0F)) {
                        client.getTextureManager().bindTexture(new Identifier("sigma-reborn", "jello_watermark.png"));
                    } else {
                        client.getTextureManager().bindTexture(new Identifier("sigma-reborn", "jello_watermark2x.png"));
                    }

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    DrawableHelper.drawTexture(new MatrixStack(), var3, var4, 0, 0, (int) 170.0F, (int) 104.0F, (int) 170.0F, (int) 104.0F);

                    RenderSystem.disableBlend();
                }
                new FastRender2DEvent(tickDelta, startTime, tick).post();
            }

            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.enableAlphaTest();
            GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.1F);
        }
        RenderSystem.popMatrix();
    }
}
