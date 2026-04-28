package io.github.sst.remake.module.impl.render;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.event.impl.game.render.Render3DEvent;
import io.github.sst.remake.event.impl.game.world.LoadWorldEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.util.client.waypoint.Waypoint;
import io.github.sst.remake.util.game.world.EntityUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.porting.StateManager;
import io.github.sst.remake.util.render.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class WaypointsModule extends Module {
    private final BooleanSetting unspawn = new BooleanSetting("Death positions", "Adds a waypoint when a player dies", false);

    private final HashMap<UUID, Waypoint> unspawnedWaypoints = new HashMap<>();

    public WaypointsModule() {
        super(Category.RENDER, "Waypoints", "Renders waypoints you added in Jello maps.");
    }

    @Override
    public void onDisable() {
        this.unspawnedWaypoints.clear();
    }

    @Subscribe
    public void onLoad(LoadWorldEvent ignoredEvent) {
        this.unspawnedWaypoints.clear();
    }

    @Subscribe
    public void onPacket(ReceivePacketEvent event) {
        if (client.world == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof EntityDestroyS2CPacket destroyPacket) {
            Entity entity = client.world.getEntityById(destroyPacket.getEntityId());
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;

                this.unspawnedWaypoints.remove(player.getUuid());
                this.unspawnedWaypoints.put(
                        player.getUuid(),
                        new Waypoint(
                                player.getName().getString() + " Unspawn",
                                (int) player.getX(),
                                (int) player.getY(),
                                (int) player.getZ(),
                                ClientColors.DARK_OLIVE
                        )
                );
            }
            return;
        }

        if (packet instanceof EntitySpawnS2CPacket) {
            EntitySpawnS2CPacket p = (EntitySpawnS2CPacket) packet;
            this.unspawnedWaypoints.remove(p.getUuid());
            return;
        }

        if (packet instanceof MobSpawnS2CPacket) {
            MobSpawnS2CPacket p = (MobSpawnS2CPacket) packet;
            this.unspawnedWaypoints.remove(p.getUuid());
            return;
        }

        if (packet instanceof PlayerSpawnS2CPacket) {
            PlayerSpawnS2CPacket p = (PlayerSpawnS2CPacket) packet;
            this.unspawnedWaypoints.remove(p.getPlayerUuid());
        }
    }

    @Subscribe
    public void onRender(Render3DEvent ignoredEvent) {
        if (client.world == null) return;

        for (Waypoint waypoint : collectAndSortWaypointsByDistance()) {
            BlockPos pos = new BlockPos(
                    waypoint.x - (waypoint.x <= 0 ? 1 : 0), waypoint.y,
                    waypoint.z - (waypoint.z <= 0 ? 1 : 0));
            double distance = Math.sqrt(EntityUtils.calculateDistanceSquared(pos));

            if (!(distance > 300.0)) {
                if (client.world.getChunk(pos) != null && waypoint.config) {
                    int x = pos.getX() % 16;
                    int z = pos.getZ() % 16;

                    if (z < 0) {
                        z += 16;
                    }

                    if (x < 0) {
                        x += 16;
                    }

                    int height = client.world.getChunk(pos).getHeightmap(Heightmap.Type.WORLD_SURFACE).get(x, z);
                    if (height == 0) {
                        height = 64;
                    }

                    if ((float) height != waypoint.y) {
                        waypoint.y = waypoint.y + ((float) height - waypoint.y) * 0.1F;
                    }
                }

                float x = (float) ((double) waypoint.x
                        - client.gameRenderer.getCamera().getPos().getX());
                float y = (float) ((double) waypoint.y
                        - client.gameRenderer.getCamera().getPos().getY());
                float z = (float) ((double) waypoint.z
                        - client.gameRenderer.getCamera().getPos().getZ());

                if (waypoint.x < 0) {
                    x--;
                }

                if (waypoint.z < 0) {
                    z--;
                }

                float scale = (float) Math.max(1.0, Math.sqrt(EntityUtils.calculateDistanceSquared(pos) / 30.0));
                RenderUtils.drawWaypointIndicator(x, y, z, waypoint.name, waypoint.color, scale);
            }
        }

        StateManager.glMultiTexCoord2f(33986, 240.0F, 240.0F);
    }

    private List<Waypoint> collectAndSortWaypointsByDistance() {
        List<Waypoint> waypoints = new ArrayList<>(Client.INSTANCE.waypointTracker.getWaypoints());

        if (unspawn.value) {
            waypoints.addAll(this.unspawnedWaypoints.values());
        }

        waypoints.sort((a, b) -> {
            double distanceA = client.player.squaredDistanceTo(a.x, a.y, a.z);
            double distanceB = client.player.squaredDistanceTo(b.x, b.y, b.z);
            return distanceA < distanceB ? 1 : -1;
        });

        return waypoints;
    }
}