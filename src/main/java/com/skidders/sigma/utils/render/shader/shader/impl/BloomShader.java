package com.skidders.sigma.utils.render.shader.shader.impl;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.skidders.sigma.utils.IMinecraft;
import com.skidders.sigma.utils.render.interfaces.IRenderCall;
import com.skidders.sigma.utils.render.shader.shader.Shader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GlStateManager;

import com.skidders.sigma.utils.render.shader.StencilUtils;

public class BloomShader implements IMinecraft {

	private static final Shader bloom = new Shader("bloom.frag");
	private static final ConcurrentLinkedQueue<IRenderCall> renderQueue = Queues.newConcurrentLinkedQueue();
	private static final Framebuffer inFrameBuffer = new Framebuffer(window.getWidth(), window.getHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
    private static final Framebuffer outFrameBuffer = new Framebuffer(window.getWidth(), window.getHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
    
    public static void registerRenderCall(IRenderCall rc) {
    	renderQueue.add(rc);
    }
    
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
    	
    	bloom.load();
    	bloom.setUniformf("radius", radius);
    	bloom.setUniformi("sampler1", 0);
    	bloom.setUniformi("sampler2", 20);
    	bloom.setUniformfb("kernel", StencilUtils.getKernel(radius));
    	bloom.setUniformf("texelSize", 1.0F / (float) window.getWidth(), 1.0F / (float) window.getHeight());
    	bloom.setUniformf("direction", 2.0F, 0.0F);
    	
    	GlStateManager.enableBlend();
    	GlStateManager.blendFunc(GL30.GL_ONE, GL30.GL_SRC_ALPHA);
    	GL30.glAlphaFunc(GL30.GL_GREATER, 0.0001f);
    	
	    inFrameBuffer.beginRead();
	    Shader.draw();
    	
	    mc.getFramebuffer().beginWrite(false);
	    GlStateManager.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
	    
	    bloom.setUniformf("direction", 0.0F, 2.0F);
	    
	    outFrameBuffer.beginRead();
	    GL30.glActiveTexture(GL30.GL_TEXTURE20);
	    inFrameBuffer.beginRead();
	    GL30.glActiveTexture(GL30.GL_TEXTURE0);
	    Shader.draw();
	    
	    bloom.unload();
	    inFrameBuffer.endRead();
	    GlStateManager.disableBlend();
    }
    
    private static Framebuffer setupBuffer(Framebuffer frameBuffer) {
		if(frameBuffer.textureWidth != window.getWidth() || frameBuffer.textureHeight != window.getHeight())
			frameBuffer.resize(window.getWidth(), window.getHeight(), MinecraftClient.IS_SYSTEM_MAC);
		else 
			frameBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
		frameBuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		return frameBuffer;
	}
    
}
