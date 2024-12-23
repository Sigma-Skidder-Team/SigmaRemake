package com.skidders.sigma.utils.font.data;

import java.nio.ByteBuffer;

public record TextureData(int textureId, int width, int height, ByteBuffer buffer) {
}