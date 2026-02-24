package io.github.sst.remake.manager.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.world.LoadWorldEvent;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ConfigUtils;
import io.github.sst.remake.util.client.WaypointUtils;
import io.github.sst.remake.util.client.waypoint.MapRegion;
import io.github.sst.remake.util.client.waypoint.RegionPos;
import io.github.sst.remake.util.client.waypoint.Waypoint;
import io.github.sst.remake.util.io.FileUtils;
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

public final class WaypointManager extends Manager implements IMinecraft {
    private List<Waypoint> waypoints;
    public String mapRegionIdentifier;
    private String logicalIdentifier;
    public int pendingChunkSaveCount = 0;
    private boolean loaded = false;

    @Override
    public void init() {
        waypoints = new ArrayList<>();

        if (!ConfigUtils.WAYPOINTS_FOLDER.exists()) {
            ConfigUtils.WAYPOINTS_FOLDER.mkdirs();
        }

        load();

        int color = -7687425;

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                WaypointUtils.defaultChunkBuffer.put((byte) (color >> 16 & 0xFF));
                WaypointUtils.defaultChunkBuffer.put((byte) (color >> 8 & 0xFF));
                WaypointUtils.defaultChunkBuffer.put((byte) (color & 0xFF));
            }
        }

        ((Buffer) WaypointUtils.defaultChunkBuffer).flip();

        super.init();
    }

    public List<Waypoint> getWaypoints() {
        if (!loaded) {
            load();
        }

        return waypoints;
    }

    @Subscribe
    public void onLoadWorld(LoadWorldEvent event) {
        try {
            save();
            saveModifiedRegions();
        } catch (IOException e) {
            Client.LOGGER.error("Failed to save waypoints", e);
        }

        this.mapRegionIdentifier = this.getFormattedIdentifier();
        this.logicalIdentifier = this.getFormattedIdentifier();
        WaypointUtils.regionCache.clear();
        WaypointUtils.processedChunks.clear();
        WaypointUtils.borderChunks.clear();
        this.loaded = false;
        this.waypoints.clear();
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        if (client.world != null) {
            if (this.mapRegionIdentifier != null) {
                if (client.player.age % 140 == 0) {
                    RegionPos playerRegion = RegionPos.fromChunkPos(client.world.getChunk(client.player.getBlockPos()).getPos());
                    Iterator<Map.Entry<Long, MapRegion>> regionIterator = WaypointUtils.regionCache.entrySet().iterator();

                    while (regionIterator.hasNext()) {
                        Map.Entry<Long, MapRegion> entry = regionIterator.next();
                        RegionPos regionPos = new RegionPos(entry.getKey());
                        double deltaX = playerRegion.x - regionPos.x;
                        double deltaZ = playerRegion.z - regionPos.z;
                        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                        if (distance > 2.0) {
                            try {
                                ObjectOutputStream out = new ObjectOutputStream(
                                        new FileOutputStream(WaypointUtils.getRegionFilePath(this.mapRegionIdentifier, entry.getValue()))
                                );
                                entry.getValue().write(out);
                                out.close();
                            } catch (IOException e) {
                                Client.LOGGER.warn("Failed to write region", e);
                            }

                            this.pendingChunkSaveCount = Math.max(0, this.pendingChunkSaveCount - entry.getValue().chunkData.size());
                            regionIterator.remove();
                        }
                    }
                }

                String id = this.mapRegionIdentifier;
                int processedChunksCount = 0;

                for (int i = 0; i < client.world.getChunkManager().chunks.chunks.length(); i++) {
                    WorldChunk chunk = client.world.getChunkManager().chunks.chunks.get(i);
                    if (chunk != null) {
                        boolean isProcessed = WaypointUtils.processedChunks.contains(chunk.getPos());
                        boolean isBorder = WaypointUtils.borderChunks.contains(chunk.getPos());
                        if ((!isProcessed || isBorder)
                                && !chunk.isEmpty()
                                && client.world.getChunkManager().isChunkLoaded(chunk.getPos().x, chunk.getPos().z)
                                && client.world.getRegistryKey() == World.OVERWORLD) {
                            if (!isProcessed) {
                                WaypointUtils.processedChunks.add(chunk.getPos());
                            }

                            boolean neighborsLoaded = WaypointUtils.areNeighborsLoaded(chunk);
                            if (!neighborsLoaded && !isBorder) {
                                WaypointUtils.borderChunks.add(chunk.getPos());
                            } else if (neighborsLoaded && isBorder) {
                                WaypointUtils.borderChunks.remove(chunk.getPos());
                            } else if (!neighborsLoaded && isBorder) {
                                continue;
                            }

                            new Thread(() -> {
                                try {
                                    new File(id).mkdirs();
                                    File regionFile = new File(WaypointUtils.getRegionFilePath(id, chunk));
                                    RegionPos regionPos = RegionPos.fromChunkPos(chunk.getPos());
                                    MapRegion mapRegion = WaypointUtils.regionCache.get(regionPos.toLong());
                                    ByteBuffer chunkMap = WaypointUtils.generateChunkMap(chunk, WaypointUtils.areNeighborsLoaded(chunk));
                                    if (mapRegion != null) {
                                        mapRegion.setChunkData(chunkMap, chunk.getPos());
                                    } else if (!regionFile.exists()) {
                                        mapRegion = new MapRegion(regionPos.x, regionPos.z);
                                        mapRegion.setChunkData(chunkMap, chunk.getPos());
                                        WaypointUtils.regionCache.put(regionPos.toLong(), mapRegion);
                                        WaypointUtils.missingRegionFiles.clear();
                                    } else if (WaypointUtils.loadRegionFromFile(regionPos)) {
                                        mapRegion = WaypointUtils.regionCache.get(regionPos.toLong());
                                        mapRegion.setChunkData(chunkMap, chunk.getPos());
                                    }

                                    this.pendingChunkSaveCount++;
                                } catch (IOException e) {
                                    Client.LOGGER.warn("Failed to process chunk for map", e);
                                }
                            }).start();
                            if (++processedChunksCount > 6) {
                                break;
                            }
                        }
                    }
                }

                if (this.pendingChunkSaveCount > 32) {
                    this.pendingChunkSaveCount = 0;

                    try {
                        this.saveModifiedRegions();
                    } catch (IOException e) {
                        Client.LOGGER.warn("Failed to save modified regions", e);
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
        try {
            if (this.logicalIdentifier == null) return;

            String fileContent = FileUtils.readFile(ConfigUtils.WAYPOINTS_FILE);
            JsonObject allWaypointsObject = fileContent.trim().isEmpty() ? new JsonObject() : new JsonParser().parse(fileContent).getAsJsonObject();

            JsonArray currentWorldWaypointsArray = new JsonArray();
            for (Waypoint waypoint : this.waypoints) {
                JsonObject waypointObject = new JsonObject();
                waypointObject.addProperty("name", waypoint.name);
                waypointObject.addProperty("color", waypoint.color);
                waypointObject.addProperty("x", waypoint.x);
                waypointObject.addProperty("z", waypoint.z);
                currentWorldWaypointsArray.add(waypointObject);
            }

            allWaypointsObject.add(this.logicalIdentifier, currentWorldWaypointsArray);

            GsonUtils.save(allWaypointsObject, ConfigUtils.WAYPOINTS_FILE);
        } catch (Exception e) {
            Client.LOGGER.error("Failed to save waypoints", e);
        }
    }

    public void load() {
        try {
            this.waypoints.clear();
            this.logicalIdentifier = WaypointUtils.getWorldIdentifier();

            String fileContent = FileUtils.readFile(ConfigUtils.WAYPOINTS_FILE);
            if (fileContent.trim().isEmpty()) {
                this.loaded = true;
                return;
            }

            JsonObject allWaypointsObject = new JsonParser().parse(fileContent).getAsJsonObject();

            if (allWaypointsObject.has(this.logicalIdentifier)) {
                JsonArray waypointsArray = allWaypointsObject.getAsJsonArray(this.logicalIdentifier);
                for (int i = 0; i < waypointsArray.size(); i++) {
                    JsonObject waypointJson = waypointsArray.get(i).getAsJsonObject();
                    this.waypoints.add(new Waypoint(waypointJson, this.logicalIdentifier));
                }
            }

            this.loaded = true;
        } catch (Exception e) {
            Client.LOGGER.error("Failed to load waypoints from file", e);
        }
    }

    public void saveModifiedRegions() throws IOException {
        if (this.mapRegionIdentifier != null) {
            try {
                for (Map.Entry<Long, MapRegion> entry : WaypointUtils.regionCache.entrySet()) {
                    ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(Paths.get(WaypointUtils.getRegionFilePath(mapRegionIdentifier, entry.getValue()))));
                    entry.getValue().write(outputStream);
                    outputStream.close();
                }
            }
            catch (ConcurrentModificationException e) {
                Client.LOGGER.warn("Failed to save modified regions", e);
            }
        }
    }

    private String getFormattedIdentifier() {
        return new File(ConfigUtils.WAYPOINTS_FOLDER, WaypointUtils.getWorldIdentifier()).getAbsolutePath();
    }
}