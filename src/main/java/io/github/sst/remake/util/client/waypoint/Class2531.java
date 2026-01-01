package io.github.sst.remake.util.client.waypoint;

import net.minecraft.util.math.ChunkPos;

import java.io.Serializable;

public class Class2531 implements Serializable {
    public int field16734;
    public int field16735;

    public Class2531(int var1, int var2) {
        this.field16734 = var1;
        this.field16735 = var2;
    }

    public Class2531(long var1) {
        this.field16734 = (int) var1;
        this.field16735 = (int) (var1 >> 32);
    }

    public Long method10678() {
        return ChunkPos.toLong(this.field16734, this.field16735);
    }
}