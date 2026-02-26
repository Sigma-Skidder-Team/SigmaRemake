package io.github.sst.remake.module.impl.gui;

import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.world.LoadWorldEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.game.world.ChunkColorCache;
import io.github.sst.remake.util.game.MovementUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.font.TrueTypeFont;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MiniMapModule extends Module {

    private static float smoothedPlayerY = 64.0F;

    private ByteBuffer minimapBuffer;
    private final List<ChunkColorCache> trackedChunks = new ArrayList<>();

    private int tickCounter;
    private final int mapChunkRadius = 10;

    private double playerChunkOffsetX;
    private double playerChunkOffsetZ;

    public MiniMapModule() {
        super(Category.GUI, "MiniMap", "Shows a mini map");
    }

    @Subscribe
    public void onWorldLoad(LoadWorldEvent ignoredEvent) {
        trackedChunks.clear();
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent ignoredEvent) {
        tickCounter++;

        if (smoothedPlayerY < client.player.getY() && client.player.isOnGround()) {
            smoothedPlayerY += 0.5F;
        } else if (smoothedPlayerY > client.player.getY() && client.player.isOnGround()) {
            smoothedPlayerY -= 0.5F;
        }

        if (tickCounter < 1) {
            return;
        }


        Iterator<ChunkColorCache> iterator = trackedChunks.iterator();
        while (iterator.hasNext()) {
            ChunkColorCache cache = iterator.next();
            int distance = cache.chunk.getPos()
                    .method_24022(new ChunkPos(client.player.chunkX, client.player.chunkZ));
            if (distance > 7) {
                iterator.remove();
            }
        }

        for (WorldChunk chunk : collectNearbyChunks()) {
            if (chunk == null) {
                return;
            }

            boolean exists = false;
            for (ChunkColorCache cache : trackedChunks) {
                if (cache.matchesChunk(chunk)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                trackedChunks.add(new ChunkColorCache(chunk));
                break;
            }
        }

        for (ChunkColorCache cache : trackedChunks) {
            cache.checkAndUpdateBuffer();
        }

        playerChunkOffsetX =
                (client.player.getX() - client.player.chunkX * 16.0) / 16.0;
        playerChunkOffsetZ =
                (client.player.getZ() - client.player.chunkZ * 16.0) / 16.0;

        minimapBuffer = buildMinimapBuffer();
        tickCounter = 0;
    }

    @Subscribe(priority = Priority.HIGH)
    public void onRender2D(RenderClient2DEvent event) {
        if (!isEnabled() || client.player == null || client.world == null) {
            return;
        }

        if (minimapBuffer == null || client.options.debugEnabled || client.options.hudHidden) {
            return;
        }

        int yOffset = event.getOffset();
        int mapSize = 150;
        int mapX = 10;

        float scale = 1.5F;

        GL11.glAlphaFunc(519, 0.0F);
        RenderUtils.drawRect(mapX, yOffset, mapSize, mapSize, -7687425);

        GL11.glPushMatrix();

        float chunkPixelSize = (float) mapSize / mapChunkRadius;
        float offsetX = (float) (chunkPixelSize * scale * playerChunkOffsetZ);
        float offsetY = (float) (-chunkPixelSize * scale * playerChunkOffsetX);

        GL11.glTranslatef(mapX + mapSize / 2.0F, yOffset + mapSize / 2.0F, 0.0F);
        GL11.glRotatef(90.0F - client.player.yaw, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-mapSize / 2.0F, -mapSize / 2.0F, 0.0F);

        float scaledWidth = mapSize * scale;
        float scaledHeight = mapSize * scale;

        ScissorUtils.startScissorNoGL(mapX, yOffset, mapX + mapSize, yOffset + mapSize);

        float drawX = -scaledWidth / 2.0F + mapSize / 2.0F + offsetX;
        float drawY = -scaledHeight / 2.0F + mapSize / 2.0F + offsetY;

        RenderUtils.drawTexturedQuad(
                drawX,
                drawY,
                scaledWidth,
                scaledHeight,
                minimapBuffer,
                ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                0.0F,
                0.0F,
                mapChunkRadius * 16.0F,
                mapChunkRadius * 16.0F,
                true,
                false
        );

        ScissorUtils.restoreScissor();
        GL11.glPopMatrix();

        int direction = (int) MovementUtils.getDirection();

        GL11.glPushMatrix();
        GL11.glTranslatef(mapX + mapSize / 2.0F + 1, yOffset + mapSize / 2.0F + 3, 0.0F);
        GL11.glRotatef(direction - client.player.yaw, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-(mapX + mapSize / 2.0F + 1), -(yOffset + mapSize / 2.0F), 0.0F);

        TrueTypeFont font = FontUtils.HELVETICA_MEDIUM_20;
        String arrow = "^";

        RenderUtils.drawString(
                font,
                mapX + mapSize / 2.0F - 4,
                yOffset + mapSize / 2.0F - 8,
                arrow,
                1879048192
        );
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(mapX + mapSize / 2.0F + 1, yOffset + mapSize / 2.0F, 0.0F);
        GL11.glRotatef(direction - client.player.yaw, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef(-(mapX + mapSize / 2.0F + 1), -(yOffset + mapSize / 2.0F), 0.0F);

        RenderUtils.drawString(
                font,
                mapX + mapSize / 2.0F - 4,
                yOffset + mapSize / 2.0F - 8,
                arrow,
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );
        GL11.glPopMatrix();

        RenderUtils.drawPanelShadow(mapX, yOffset, mapSize, mapSize, 23.0F, 0.75F);
        RenderUtils.drawRoundedRect(mapX, yOffset, mapSize, mapSize, 8.0F, 0.7F);

        event.increment(mapSize + 10);
    }

    @Override
    public void onDisable() {
        trackedChunks.clear();
    }

    private List<WorldChunk> collectNearbyChunks() {
        List<WorldChunk> chunks = new ArrayList<>();
        for (int x = -mapChunkRadius / 2; x < mapChunkRadius / 2; x++) {
            for (int z = -mapChunkRadius / 2; z < mapChunkRadius / 2; z++) {
                chunks.add(client.world.getChunk(client.player.chunkX + x, client.player.chunkZ + z));
            }
        }
        return chunks;
    }

    private ByteBuffer buildMinimapBuffer() {
        List<WorldChunk> chunks = collectNearbyChunks();
        ByteBuffer buffer = BufferUtils.createByteBuffer(mapChunkRadius * 16 * mapChunkRadius * 16 * 3);

        int column = 0;
        int rowOffset = buffer.position();

        for (WorldChunk chunk : chunks) {
            ByteBuffer chunkBuffer = BufferUtils.createByteBuffer(768);
            fillDefaultChunk(chunkBuffer);

            ChunkColorCache cache = null;
            for (ChunkColorCache entry : trackedChunks) {
                if (entry.matchesChunk(chunk)) {
                    cache = entry;
                    break;
                }
            }

            if (cache != null && cache.chunkBuffer != null) {
                chunkBuffer = cache.chunkBuffer;
            }

            int start = buffer.position();
            int base = buffer.position();

            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    buffer.put(chunkBuffer.get());
                    buffer.put(chunkBuffer.get());
                    buffer.put(chunkBuffer.get());
                }

                start += 16 * mapChunkRadius * 3;
                if (start < buffer.limit()) {
                    buffer.position(start);
                }
            }

            rowOffset += 48;
            if (base + 48 < buffer.limit()) {
                buffer.position(base + 48);
            }

            if (column != rowOffset / (48 * mapChunkRadius)) {
                column = rowOffset / (48 * mapChunkRadius);
                int pos = 256 * mapChunkRadius * 3 * column;
                if (pos < buffer.limit()) {
                    buffer.position(pos);
                }
            }

            chunkBuffer.position(0);
        }

        buffer.position(16 * mapChunkRadius * 16 * mapChunkRadius * 3);
        buffer.flip();
        return buffer;
    }

    private void fillDefaultChunk(ByteBuffer buffer) {
        int color = -7687425;

        for (int i = 0; i < 256; i++) {
            buffer.put((byte) (color >> 16 & 0xFF));
            buffer.put((byte) (color >> 8 & 0xFF));
            buffer.put((byte) (color & 0xFF));
        }

        buffer.flip();
    }
}
