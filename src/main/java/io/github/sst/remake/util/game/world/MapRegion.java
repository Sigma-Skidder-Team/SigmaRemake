package io.github.sst.remake.util.game.world;

import io.github.sst.remake.Client;
import net.minecraft.util.math.ChunkPos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

@SuppressWarnings("unchecked")
public class MapRegion {
    public int regionX;
    public int regionZ;
    public HashMap<Integer, byte[]> chunkData = new HashMap<>();

    public MapRegion(int regionX, int regionZ) {
        this.regionX = regionX;
        this.regionZ = regionZ;
    }

    public MapRegion(ObjectInputStream in) {
        try {
            this.regionX = in.readInt();
            this.chunkData = (HashMap<Integer, byte[]>) in.readObject();
            this.regionZ = in.readInt();
        } catch (ClassNotFoundException | IOException e) {
            Client.LOGGER.warn("Failed to load map region", e);
        }
    }

    public void setChunkData(ByteBuffer data, ChunkPos pos) {
        byte[] bytes = new byte[data.capacity()];
        data.get(bytes, 0, bytes.length);
        this.chunkData.put(this.getChunkIndex(pos), bytes);
    }

    public ByteBuffer getChunkData(ChunkPos pos) {
        byte[] bytes = this.chunkData.get(this.getChunkIndex(pos));
        return bytes != null ? ByteBuffer.wrap(bytes) : null;
    }

    public int getChunkIndex(ChunkPos pos) {
        int x = Math.abs(pos.x) % 32;
        int z = Math.abs(pos.z) % 32;
        int index = x * 32 + z;
        return Math.max(0, Math.min(index, 1024));
    }

    public String getFileName() {
        return this.regionX + "c" + this.regionZ + ".jmap";
    }

    public void write(ObjectOutputStream out) throws IOException {
        out.writeInt(this.regionX);
        out.writeObject(this.chunkData);
        out.writeInt(this.regionZ);
    }

    public void read(ObjectInputStream in) {
        try {
            this.regionX = in.readInt();
            this.chunkData = (HashMap<Integer, byte[]>) in.readObject();
            this.regionZ = in.readInt();
        } catch (ClassNotFoundException | IOException ignored) {
        }
    }
}