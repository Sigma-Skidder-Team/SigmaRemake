package lexi.fontrenderer.data;

import java.nio.ByteBuffer;

public record TextureData(int textureId, int width, int height, ByteBuffer buffer) {
}