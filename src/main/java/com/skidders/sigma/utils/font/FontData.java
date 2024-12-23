package com.skidders.sigma.utils.font;

import java.nio.ByteBuffer;

public record FontData(int textureId, int width, int height, ByteBuffer buffer) {
}