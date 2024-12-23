package com.skidders.sigma.utils.render;

import com.skidders.sigma.utils.file.FileUtil;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.stbi_load;

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