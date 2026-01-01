package io.github.sst.remake.util.client.waypoint;

import java.nio.ByteBuffer;

public class Chunk {
    public int width;
    public int height;
    public ByteBuffer pixelBuffer;

    public Chunk(ByteBuffer pixelBuffer, int width, int height) {
        this.pixelBuffer = pixelBuffer;
        this.width = width;
        this.height = height;
    }
}