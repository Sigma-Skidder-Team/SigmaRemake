package io.github.sst.remake.util.client.waypoint;

import java.nio.ByteBuffer;

public class Chunk {
    public int field30544;
    public int field30545;
    public ByteBuffer field30546;

    public Chunk(ByteBuffer var1, int var2, int var3) {
        this.field30546 = var1;
        this.field30544 = var2;
        this.field30545 = var3;
    }
}
