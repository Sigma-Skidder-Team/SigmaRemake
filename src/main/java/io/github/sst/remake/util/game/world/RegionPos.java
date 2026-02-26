package io.github.sst.remake.util.game.world;

import net.minecraft.util.math.ChunkPos;

import java.io.Serializable;

public class RegionPos implements Serializable {
    public int x;
    public int z;

    public RegionPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public RegionPos(long longPos) {
        this.x = (int) longPos;
        this.z = (int) (longPos >> 32);
    }

    public Long toLong() {
        return ChunkPos.toLong(this.x, this.z);
    }

    public static RegionPos fromChunkPos(ChunkPos chunkPos) {
        int regionX = (int) Math.floor((double) chunkPos.x / 32.0);
        int regionZ = (int) Math.floor((double) chunkPos.z / 32.0);
        return new RegionPos(regionX, regionZ);
    }
}