package com.skidders.sigma.mixin;

import com.skidders.SigmaReborn;
import com.skidders.sigma.event.impl.WindowSizeChangeEvent;
import com.skidders.sigma.screen.guis.SwitchGUI;
import com.skidders.sigma.util.client.interfaces.ILogger;
import com.skidders.sigma.util.client.interfaces.IMinecraft;
import com.skidders.sigma.util.client.render.image.TextureLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFWImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;

import static org.lwjgl.glfw.GLFW.*;

@Mixin(Window.class)
public class WindowMixin implements ILogger, IMinecraft {

    @Inject(
            method = "onWindowSizeChanged",
            at = @At("TAIL")
    )
    public final void onWindowSizeUpdate(long window, int width, int height, CallbackInfo ci) {
        new WindowSizeChangeEvent(window, width, height).post();
    }

    @Inject(
            method = "setIcon",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectSetIcon(InputStream icon16, InputStream icon32, CallbackInfo ci) {
        if (!glfwInit()) logger.error("Could not initialise opengl4.5!");

        TextureLoader.ImageParser logo = TextureLoader.ImageParser.loadImage("/assets/sigma-reborn/icon_hd.png", "sigma/icon_hd.png");

        GLFWImage image = GLFWImage.malloc(); GLFWImage.Buffer imagebf = GLFWImage.malloc(1);
        image.set(logo.width(), logo.height(), logo.image());
        imagebf.put(0, image);
        glfwSetWindowIcon(MinecraftClient.getInstance().getWindow().getHandle(), imagebf);
        ci.cancel();
    }

    @Inject(method = "setFramerateLimit", at = @At("TAIL"))
    public void setFramerateLimit(int framerateLimit, CallbackInfo ci) {
        SigmaReborn.INSTANCE.screenHandler.framerateLimit = framerateLimit;
    }

    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    public void getFramerateLimit(CallbackInfoReturnable<Integer> cir) {
        if (mc.currentScreen instanceof SwitchGUI) {
            cir.setReturnValue(30);
        } else {
            cir.setReturnValue(SigmaReborn.INSTANCE.screenHandler.framerateLimit);
        }
    }
}