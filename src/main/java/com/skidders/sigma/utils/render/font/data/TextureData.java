package com.skidders.sigma.utils.render.font.data;

import java.nio.ByteBuffer;

public record TextureData(int textureId, int width, int height, ByteBuffer buffer) {
}