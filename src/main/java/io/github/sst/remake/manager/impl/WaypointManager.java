package io.github.sst.remake.manager.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.Client;
import io.github.sst.remake.bus.Subscribe;
import io.github.sst.remake.event.impl.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.world.LoadWorldEvent;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ConfigUtils;
import io.github.sst.remake.util.client.WaypointUtils;
import io.github.sst.remake.util.client.waypoint.Class2531;
import io.github.sst.remake.util.client.waypoint.Class7927;
import io.github.sst.remake.util.client.waypoint.Waypoint;
import io.github.sst.remake.util.io.GsonUtils;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class WaypointManager extends Manager implements IMinecraft {

    public final List<Waypoint> waypoints = new ArrayList<>();
    public String identifier;
    public int field36373 = 0;
    private boolean field36369 = false;

    @Override
    public void init() {
        super.init();

        int var3 = -7687425;

        for (int var4 = 0; var4 < 16; var4++) {
            for (int var5 = 0; var5 < 16; var5++) {
                WaypointUtils.field36376.put((byte) (var3 >> 16 & 0xFF));
                WaypointUtils.field36376.put((byte) (var3 >> 8 & 0xFF));
                WaypointUtils.field36376.put((byte) (var3 & 0xFF));
            }
        }

        ((Buffer) WaypointUtils.field36376).flip();
    }

    @Override
    public void shutdown() {
        save();
    }

    @Subscribe
    public void onLoadWorld(LoadWorldEvent event) {
        try {
            save();
            method29997();
        } catch (IOException e) {
            Client.LOGGER.error("Failed to save waypoints", e);
        }

        this.identifier = this.getFormattedIdentifier();
        WaypointUtils.field36372.clear();
        WaypointUtils.field36366.clear();
        WaypointUtils.field36367.clear();
        this.field36369 = false;
        this.waypoints.clear();
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        if (client.world != null) {
            if (this.identifier != null) {
                boolean var4 = false;
                if (!var4) {
                    if (client.player.age % 140 == 0) {
                        Class2531 var5 = Class7927.method26605(client.world.getChunk(client.player.getBlockPos()).getPos());
                        Iterator<Map.Entry<Long, Class7927>> var6 = WaypointUtils.field36372.entrySet().iterator();

                        while (var6.hasNext()) {
                            Map.Entry var7 = var6.next();
                            Class2531 var8 = new Class2531((Long) var7.getKey());
                            double var9 = var5.field16734 - var8.field16734;
                            double var11 = var5.field16735 - var8.field16735;
                            double var13 = Math.sqrt(var9 * var9 + var11 * var11);
                            if (var13 > 2.0) {
                                try {
                                    ObjectOutputStream var15 = new ObjectOutputStream(
                                            new FileOutputStream(WaypointUtils.method30001(this.identifier, (Class7927) var7.getValue()))
                                    );
                                    ((Class7927) var7.getValue()).method26603(var15);
                                    var15.close();
                                } catch (IOException e) {
                                    Client.LOGGER.warn("Failed to method26603", e);
                                }

                                this.field36373 = Math.max(0, this.field36373 - ((Class7927) var7.getValue()).field33959.size());
                                var6.remove();
                            }
                        }
                    }

                    String var23 = this.identifier;
                    int var24 = 0;

                    for (int var25 = 0; var25 < client.world.getChunkManager().chunks.chunks.length(); var25++) {
                        WorldChunk var17 = client.world.getChunkManager().chunks.chunks.get(var25);
                        if (var17 != null) {
                            boolean var18 = WaypointUtils.field36366.contains(var17.getPos());
                            boolean var19 = WaypointUtils.field36367.contains(var17.getPos());
                            if ((!var18 || var19)
                                    && !var17.isEmpty()
                                    && client.world.getChunkManager().isChunkLoaded(var17.getPos().x, var17.getPos().z)
                                    && client.world.getRegistryKey() == World.OVERWORLD) {
                                if (!var18) {
                                    WaypointUtils.field36366.add(var17.getPos());
                                }

                                boolean var20 = WaypointUtils.method30004(var17);
                                if (!var20 && !var19) {
                                    WaypointUtils.field36367.add(var17.getPos());
                                } else if (var20 && var19) {
                                    WaypointUtils.field36367.remove(var17.getPos());
                                } else if (!var20 && var19) {
                                    continue;
                                }

                                new Thread(() -> {
                                    try {
                                        new File(var23).mkdirs();
                                        File var5x = new File(WaypointUtils.method30002(var23, var17));
                                        Class2531 var6x = Class7927.method26605(var17.getPos());
                                        Class7927 var7x = WaypointUtils.field36372.get(var6x.method10678());
                                        ByteBuffer var8x = WaypointUtils.method30005(var17, WaypointUtils.method30004(var17));
                                        if (var7x != null) {
                                            var7x.method26599(var8x, var17.getPos());
                                        } else if (!var5x.exists()) {
                                            var7x = new Class7927(var6x.field16734, var6x.field16735);
                                            var7x.method26599(var8x, var17.getPos());
                                            WaypointUtils.field36372.put(var6x.method10678(), var7x);
                                            WaypointUtils.field36374.clear();
                                        } else if (WaypointUtils.method29996(var6x)) {
                                            var7x = WaypointUtils.field36372.get(var6x.method10678());
                                            var7x.method26599(var8x, var17.getPos());
                                        }

                                        this.field36373++;
                                    } catch (IOException e) {
                                        Client.LOGGER.warn("Failed to do something", e);
                                    }
                                }).start();
                                if (++var24 > 6) {
                                    break;
                                }
                            }
                        }
                    }

                    if (this.field36373 > 32) {
                        this.field36373 = 0;

                        try {
                            this.method29997();
                        } catch (IOException e) {
                            Client.LOGGER.warn("Failed to do method29997", e);
                        }
                    }
                }
            }
        }
    }

    public void add(Waypoint waypoint) {
        this.waypoints.add(waypoint);
        save();
    }

    public void save() {
        if (this.identifier != null) {
            JsonObject waypointsObject = new JsonObject();
            JsonArray waypointsArray = new JsonArray();

            for (Waypoint waypoint : this.waypoints) {
                JsonObject waypointObject = new JsonObject();
                waypointObject.addProperty("name", waypoint.name);
                waypointObject.addProperty("color", waypoint.color);
                waypointObject.addProperty("x", waypoint.x);
                waypointObject.addProperty("z", waypoint.z);
                waypointsArray.add(waypointObject);
            }

            waypointsObject.add("waypoints", waypointsArray);

            try {
                GsonUtils.save(waypointsObject, ConfigUtils.WAYPOINTS_FILE);
            } catch (IOException | JsonParseException e) {
                Client.LOGGER.error("Failed to save waypoints", e);
            }
        }
    }

    public void method29997() throws IOException {
        if (this.identifier != null) {
            try {
                for (Map.Entry entry : WaypointUtils.field36372.entrySet()) {
                    ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(Paths.get(WaypointUtils.method30001(identifier, (Class7927) entry.getValue()))));
                    ((Class7927) entry.getValue()).method26603(outputStream);
                    outputStream.close();
                }
            } catch (ConcurrentModificationException e) {
                Client.LOGGER.warn("Failed to method29997", e);
            }
        }
    }

    private String getFormattedIdentifier() {
        return new File("jello") + "/maps/" + WaypointUtils.getWorldIdentifier();
    }

}
