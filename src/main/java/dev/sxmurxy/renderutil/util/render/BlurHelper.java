package dev.sxmurxy.renderutil.util.render;

import java.util.concurrent.ConcurrentLinkedQueue;

import dev.sxmurxy.renderutil.util.misc.IRenderCall;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GlStateManager;

import dev.sxmurxy.renderutil.Wrapper;
import dev.sxmurxy.renderutil.util.misc.Utils;

public class BlurHelper implements Wrapper {

    private static final Shader blur = new Shader("blur.frag");
    private static final ConcurrentLinkedQueue<IRenderCall> renderQueue = Queues.newConcurrentLinkedQueue();
    private static final Framebuffer inFrameBuffer = new Framebuffer((int)(WINDOW.getWidth() / 2d), (int)(WINDOW.getHeight() / 2d), true, MinecraftClient.IS_SYSTEM_MAC);
    private static final Framebuffer outFrameBuffer = new Framebuffer((int)(WINDOW.getWidth() / 2d), (int)(WINDOW.getHeight() / 2d), true, MinecraftClient.IS_SYSTEM_MAC);
    
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
    	
    	blur.load();
    	blur.setUniformf("radius", radius);
    	blur.setUniformi("sampler1", 0);
    	blur.setUniformi("sampler2", 20);
    	blur.setUniformfb("kernel", Utils.getKernel(radius));
        blur.setUniformf("texelSize", 1.0F / (float)WINDOW.getWidth(), 1.0F / (float)WINDOW.getHeight());
        blur.setUniformf("direction", 2.0F, 0.0F);

        GlStateManager.disableBlend();
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
	    MC.getFramebuffer().beginRead();
	    Shader.draw();
    	
	    MC.getFramebuffer().beginWrite(true);
	    
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
    	if(frameBuffer.textureWidth != (int)(WINDOW.getWidth() / 2d) || frameBuffer.textureHeight != (int)(WINDOW.getHeight() / 2d))
			frameBuffer.resize((int)(WINDOW.getWidth() / 2d), (int)(WINDOW.getHeight() / 2d), MinecraftClient.IS_SYSTEM_MAC);
		else 
			frameBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
		
		return frameBuffer;
	}
    
}
