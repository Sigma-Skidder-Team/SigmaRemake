package com.skidders.sigma.utils.render.shader.shader.impl;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GlStateManager;
import com.skidders.sigma.utils.IMinecraft;
import com.skidders.sigma.utils.render.interfaces.IRenderCall;
import com.skidders.sigma.utils.render.shader.StencilUtils;
import com.skidders.sigma.utils.render.shader.shader.Shader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BlurShader implements IMinecraft {

    private static final Shader blur = new Shader("blur.frag");
    private static final ConcurrentLinkedQueue<IRenderCall> renderQueue = Queues.newConcurrentLinkedQueue();
    private static final Framebuffer inFrameBuffer = new Framebuffer((int)(window.getWidth() / 2d), (int)(window.getHeight() / 2d), true, MinecraftClient.IS_SYSTEM_MAC);
    private static final Framebuffer outFrameBuffer = new Framebuffer((int)(window.getWidth() / 2d), (int)(window.getHeight() / 2d), true, MinecraftClient.IS_SYSTEM_MAC);
    
    public static void registerRenderCall(IRenderCall rc) {
    	renderQueue.add(rc);
    }
	//i forgor how to use it wait
    public static void draw(int radius) {
    	if(renderQueue.isEmpty())
			return;
    	
    	setupBuffer(inFrameBuffer);
    	setupBuffer(outFrameBuffer);
    	
    	inFrameBuffer.beginWrite(true);
    	
    	while(!renderQueue.isEmpty()) {
    		renderQueue.poll().execute();
    	}
    	
    	outFrameBuffer.beginWrite(true);
    	
    	blur.load();
    	blur.setUniformf("radius", radius);
    	blur.setUniformi("sampler1", 0);
    	blur.setUniformi("sampler2", 20);
    	blur.setUniformfb("kernel", StencilUtils.getKernel(radius));
        blur.setUniformf("texelSize", 1.0F / (float) window.getWidth(), 1.0F / (float) window.getHeight());
        blur.setUniformf("direction", 2.0F, 0.0F);

        GlStateManager.disableBlend();
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
	    mc.getFramebuffer().beginRead();
	    Shader.draw();
    	
	    mc.getFramebuffer().beginWrite(true);
	    
	    blur.setUniformf("direction", 0.0F, 2.0F);
	    
	    outFrameBuffer.beginRead();
	    GL30.glActiveTexture(GL30.GL_TEXTURE20);
	    inFrameBuffer.beginRead();
	    GL30.glActiveTexture(GL30.GL_TEXTURE0);
	    Shader.draw();
	    
	    blur.unload();
	    inFrameBuffer.endRead();
	    GlStateManager.disableBlend();
    }
    
    private static Framebuffer setupBuffer(Framebuffer frameBuffer) {
    	if(frameBuffer.textureWidth != (int)(window.getWidth() / 2d) || frameBuffer.textureHeight != (int)(window.getHeight() / 2d))
			frameBuffer.resize((int)(window.getWidth() / 2d), (int)(window.getHeight() / 2d), MinecraftClient.IS_SYSTEM_MAC);
		else 
			frameBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
		
		return frameBuffer;
	}
    
}
