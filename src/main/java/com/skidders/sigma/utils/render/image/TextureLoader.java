package com.skidders.sigma.utils.render.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ReadOnlyBufferException;

import com.skidders.sigma.utils.IMinecraft;
import com.skidders.sigma.utils.file.FileUtil;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.stb.STBImage.stbi_load;

public final class TextureLoader {

	public static int loadTexture1(BufferedImage image) {
		int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length * 4);
        
        try {
	        for (int pixel : pixels) {
	            buffer.put((byte)((pixel >> 16) & 0xFF));
	            buffer.put((byte)((pixel >> 8) & 0xFF));
	            buffer.put((byte)(pixel & 0xFF));
	            buffer.put((byte)((pixel >> 24) & 0xFF));
	        }
	        buffer.flip();
        } catch (BufferOverflowException | ReadOnlyBufferException ex) {return -1;}
        
		int textureID = GlStateManager.genTextures();
		GlStateManager.bindTexture(textureID);
		GlStateManager.texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
		GlStateManager.texParameter(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);
		GlStateManager.bindTexture(0);
		
		return textureID;
	}

	public static int loadTexture2(BufferedImage image) {
		int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length * 4);

		for (int pixel : pixels) {
			buffer.put((byte) ((pixel >> 16) & 0xFF));
			buffer.put((byte) ((pixel >> 8) & 0xFF));
			buffer.put((byte) (pixel & 0xFF));
			buffer.put((byte) ((pixel >> 24) & 0xFF));
		}
		buffer.flip();

		int textureID = GlStateManager.genTextures();
		GlStateManager.bindTexture(textureID);
		GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL30.glTexParameterIi(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		GlStateManager.bindTexture(0);
		return textureID;
	}

	public static int getTextureId(Identifier identifier) {
		AbstractTexture abstractTexture = IMinecraft.mc.getTextureManager().getTexture(identifier);
		if (abstractTexture != null) {
			return abstractTexture.getGlId();
		}
		return 0;
	}

	public record ImageParser(int width, int height, ByteBuffer image) {
		public static ImageParser loadImage(String resource, String path) {
			try {
				FileUtil.copyResourceToFile(resource, path);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			ByteBuffer image;
			int width, height;

			try (MemoryStack stack = MemoryStack.stackPush()) {
				IntBuffer comp = stack.mallocInt(1);
				IntBuffer w = stack.mallocInt(1);
				IntBuffer h = stack.mallocInt(1);

				image = stbi_load(path, w, h, comp, 4);
				if (image == null) {
					throw new RuntimeException("Could not load image " + path);
				}
				width = w.get();
				height = h.get();
			}
			return new ImageParser(width, height, image);
		}
	}
}
