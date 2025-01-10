package dev.sxmurxy.renderutil.util.render;

import java.util.concurrent.ConcurrentLinkedQueue;

import dev.sxmurxy.renderutil.util.misc.IRenderCall;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GlStateManager;

import dev.sxmurxy.renderutil.Wrapper;
import dev.sxmurxy.renderutil.util.misc.Utils;

public class BloomHelper implements Wrapper {

	private static final Shader bloom = new Shader("bloom.frag");
	private static final ConcurrentLinkedQueue<IRenderCall> renderQueue = Queues.newConcurrentLinkedQueue();
	private static final Framebuffer inFrameBuffer = new Framebuffer(WINDOW.getWidth(), WINDOW.getHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
    private static final Framebuffer outFrameBuffer = new Framebuffer(WINDOW.getWidth(), WINDOW.getHeight(), true, MinecraftClient.IS_SYSTEM_MAC);
    
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
    	bloom.setUniformfb("kernel", Utils.getKernel(radius));
    	bloom.setUniformf("texelSize", 1.0F / (float)WINDOW.getWidth(), 1.0F / (float)WINDOW.getHeight());
    	bloom.setUniformf("direction", 2.0F, 0.0F);
    	
    	GlStateManager.enableBlend();
    	GlStateManager.blendFunc(GL30.GL_ONE, GL30.GL_SRC_ALPHA);
    	GL30.glAlphaFunc(GL30.GL_GREATER, 0.0001f);
    	
	    inFrameBuffer.beginRead();
	    Shader.draw();
    	
	    MC.getFramebuffer().beginWrite(false);
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
		if(frameBuffer.textureWidth != WINDOW.getWidth() || frameBuffer.textureHeight != WINDOW.getHeight())
			frameBuffer.resize(WINDOW.getWidth(), WINDOW.getHeight(), MinecraftClient.IS_SYSTEM_MAC);
		else 
			frameBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
		frameBuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		return frameBuffer;
	}
    
}
