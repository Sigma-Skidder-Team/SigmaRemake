package io.github.sst.remake.util.client;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.waypoint.Chunk;
import io.github.sst.remake.util.client.waypoint.Class2531;
import io.github.sst.remake.util.client.waypoint.Class7927;
import net.minecraft.util.math.ChunkPos;
import org.lwjgl.BufferUtils;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WaypointUtils implements IMinecraft {

    private static final int field36370 = 10;
    public static HashMap<Long, Class7927> field36372 = new HashMap<>();
    public static HashMap<Long, ByteBuffer> field36375 = new HashMap<>();
    public static final List<ChunkPos> field36366 = new ArrayList<ChunkPos>();
    public static final List<ChunkPos> field36367 = new ArrayList<ChunkPos>();
    public static ByteBuffer field36376 = BufferUtils.createByteBuffer(field36370 * 16 * field36370 * 16 * 3);
    public static List<Class2531> field36374 = new ArrayList<>();

    public static String method30000(String var1, Class2531 var2) {
        return var1 + "/" + var2.field16734 + "c" + var2.field16735 + ".jmap";
    }

    public static String method30001(String var1, Class7927 var2) {
        return var1 + "/" + var2.field33957 + "c" + var2.field33958 + ".jmap";
    }

    public static String getWorldIdentifier() {
        String identifier = "local/local";

        if (client.getServer() == null && client.getCurrentServerEntry() != null) {
            identifier = "server/" + client.getCurrentServerEntry().address.replace("/", ":");
        } else if (client.getServer() != null) {
            identifier = "local/" + client.getServer().getSaveProperties().getLevelName();
        }

        return identifier;
    }

    public static Chunk method30003(ChunkPos var1, int var2) {
        List<ChunkPos> var5 = new ArrayList<>();

        for (int var6 = -var2 / 2; var6 < var2 / 2; var6++) {
            for (int var7 = -var2 / 2; var7 < var2 / 2; var7++) {
                var5.add(new ChunkPos(var1.x + var6, var1.z + var7));
            }
        }

        ByteBuffer var21 = BufferUtils.createByteBuffer(var2 * 16 * var2 * 16 * 3);
        int var22 = 0;
        int var8 = var21.position();

        for (ChunkPos var11 : var5) {
            ByteBuffer var12 = field36376.duplicate();
            Long var13 = ChunkPos.toLong(var11.x, var11.z);
            ((Buffer) var12).position(0);
            Class2531 var14 = Class7927.method26605(var11);
            Class7927 var15 = field36372.get(var14.method10678());
            if (var15 != null) {
                ByteBuffer var16 = var15.method26600(var11);
                if (var16 != null) {
                    var12 = var16.duplicate();
                }
            } else {
                try {
                    if (method29996(var14)) {
                        var15 = field36372.get(var14.method10678());
                        ByteBuffer var24 = var15.method26600(var11);
                        if (var24 != null) {
                            var12 = var24.duplicate();
                        }
                    }
                } catch (IOException e) {
                    Client.LOGGER.warn("Failed to method30003", e);
                }
            }

            int var25 = var21.position();
            int var17 = var21.position();

            for (int var18 = 0; var18 < 16; var18++) {
                for (int var19 = 0; var19 < 16; var19++) {
                    var21.put(var12.get());
                    var21.put(var12.get());
                    var21.put(var12.get());
                }

                var25 += 16 * var2 * 3;
                if (var25 < var21.limit()) {
                    ((Buffer) var21).position(var25);
                }
            }

            var8 += 48;
            if (var17 + 48 < var21.limit()) {
                ((Buffer) var21).position(var17 + 48);
            }

            if (var22 != var8 / (48 * var2)) {
                var22 = var8 / (48 * var2);
                if (256 * var2 * 3 * var22 < var21.limit()) {
                    ((Buffer) var21).position(256 * var2 * 3 * var22);
                }
            }

            ((Buffer) var12).position(0);
        }

        ((Buffer) var21).position(16 * var2 * 16 * var2 * 3);
        ((Buffer) var21).flip();
        return new Chunk(var21, 16 * var2, 16 * var2);
    }

    public static boolean method29996(Class2531 var1) throws IOException {
        if (!field36374.contains(var1)) {
            String identifier = Client.INSTANCE.waypointManager.identifier;
            File var5 = new File(method30000(identifier, var1));
            if (var5.exists()) {
                FileInputStream var6 = new FileInputStream(var5);
                ObjectInputStream var7 = new ObjectInputStream(var6);
                Class7927 var8 = new Class7927(var1.field16734, var1.field16735);
                var8.method26604(var7);
                field36372.put(var1.method10678(), var8);
                return true;
            } else {
                field36374.add(var1);
                return false;
            }
        } else {
            return false;
        }
    }

}
