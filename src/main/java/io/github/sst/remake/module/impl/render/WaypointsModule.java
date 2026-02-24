package io.github.sst.remake.module.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.net.ReceivePacketEvent;
import io.github.sst.remake.event.impl.game.render.Render3DEvent;
import io.github.sst.remake.event.impl.game.world.LoadWorldEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.util.client.waypoint.Waypoint;
import io.github.sst.remake.util.game.EntityUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.render.RenderUtils;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.MobSpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import org.newdawn.slick.opengl.texture.TextureImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WaypointsModule extends Module {

    private final BooleanSetting unspawn = new BooleanSetting("Unspawn Positions", "Adds a waypoint when a player unspawns", false);

    private final HashMap<UUID, Waypoint> unspawnedWaypoints = new HashMap<>();

    public WaypointsModule() {
        super(Category.RENDER, "Waypoints", "Renders waypoints you added in Jello maps");
    }

    @Subscribe
    public void onLoad(LoadWorldEvent ignoredEvent) {
        this.unspawnedWaypoints.clear();
    }

    @Subscribe
    public void onPacket(ReceivePacketEvent event) {
        if (client.world == null) {
            return;
        }

        Object packet = event.packet;

        if (packet instanceof EntitiesDestroyS2CPacket) {
            EntitiesDestroyS2CPacket destroyPacket = (EntitiesDestroyS2CPacket) packet;

            for (int entityId : destroyPacket.getEntityIds()) {
                Entity entity = client.world.getEntityById(entityId);
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
                                    ClientColors.DARK_OLIVE.getColor()
                            )
                    );
                }
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
        if (this.isEnabled()) {
            for (Waypoint var5 : collectAndSortWaypointsByDistance()) {
                BlockPos var6 = new BlockPos(
                        var5.x - (var5.x <= 0 ? 1 : 0), var5.y,
                        var5.z - (var5.z <= 0 ? 1 : 0));
                double var7 = Math.sqrt(EntityUtils.calculateDistanceSquared(var6));
                if (!(var7 > 300.0)) {
                    if (client.world.getChunk(var6) != null && var5.config) {
                        int var9 = var6.getX() % 16;
                        int var10 = var6.getZ() % 16;
                        if (var10 < 0) {
                            var10 += 16;
                        }

                        if (var9 < 0) {
                            var9 += 16;
                        }

                        int var11 = client.world.getChunk(var6).getHeightmap(Heightmap.Type.WORLD_SURFACE).get(var9, var10);
                        if (var11 == 0) {
                            var11 = 64;
                        }

                        if ((float) var11 != var5.y) {
                            var5.y = var5.y + ((float) var11 - var5.y) * 0.1F;
                        }
                    }

                    float var13 = (float) ((double) var5.y
                            - client.gameRenderer.getCamera().getPos().getY());
                    float var14 = (float) ((double) var5.x
                            - client.gameRenderer.getCamera().getPos().getX());
                    float var15 = (float) ((double) var5.z
                            - client.gameRenderer.getCamera().getPos().getZ());
                    if (var5.x < 0) {
                        var14--;
                    }

                    if (var5.z < 0) {
                        var15--;
                    }

                    float var12 = (float) Math.max(1.0, Math.sqrt(EntityUtils.calculateDistanceSquared(var6) / 30.0));
                    RenderUtils.drawWaypointIndicator(var14, var13, var15, var5.name, var5.color, var12);
                }
            }

            RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
            TextureImpl.unbind();
            client.getTextureManager().bindTexture(TextureManager.MISSING_IDENTIFIER);
        }
    }

    @Override
    public void onDisable() {
        this.unspawnedWaypoints.clear();
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
