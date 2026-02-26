package io.github.sst.remake.util.game.world;

import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.WaypointUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.BufferUtils;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ChunkColorCache implements IMinecraft {
    public WorldChunk chunk;
    public ByteBuffer chunkBuffer;
    public boolean isBufferUpdated;

    public ChunkColorCache(WorldChunk var1) {
        this.chunk = var1;
        this.chunkBuffer = BufferUtils.createByteBuffer(768);
        this.updateChunkBuffer();
    }

    public void updateChunkBuffer() {
        this.chunkBuffer = BufferUtils.createByteBuffer(768);
        int chunkPosX = this.chunk.getPos().x * 16;
        int chunkPosZ = this.chunk.getPos().z * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                BlockPos blockPos = new BlockPos(chunkPosX + x, 64, chunkPosZ + z);
                int waypoint = WaypointUtils.getWaypointHeight(
                        new BlockPos(blockPos.getX(),
                                this.chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(x, z) - 1,
                                blockPos.getZ()),
                        true);
                this.chunkBuffer.put((byte) (waypoint >> 16 & 0xFF));
                this.chunkBuffer.put((byte) (waypoint >> 8 & 0xFF));
                this.chunkBuffer.put((byte) (waypoint & 0xFF));
            }
        }

        ((Buffer) this.chunkBuffer).flip();
        this.isBufferUpdated = this.areNeighboringChunksLoaded();
    }

    public void checkAndUpdateBuffer() {
        if (!this.isBufferUpdated && this.areNeighboringChunksLoaded()) {
            this.updateChunkBuffer();
        }
    }

    private boolean areNeighboringChunksLoaded() {
        WorldChunk chunkNorth = client.world.getChunk(this.chunk.getPos().x, this.chunk.getPos().z + 1);
        WorldChunk chunkSouth = client.world.getChunk(this.chunk.getPos().x, this.chunk.getPos().z - 1);
        return chunkNorth != null && chunkNorth.loadedToWorld && chunkSouth != null && chunkSouth.loadedToWorld;
    }

    public boolean matchesChunk(WorldChunk otherChunk) {
        return otherChunk.getPos().x == this.chunk.getPos().x && otherChunk.getPos().z == this.chunk.getPos().z;
    }
}