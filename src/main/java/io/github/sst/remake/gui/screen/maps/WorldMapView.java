package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.waypoint.Waypoint;
import io.github.sst.remake.util.game.world.WaypointUtils;
import io.github.sst.remake.util.game.world.data.Chunk;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class WorldMapView extends InteractiveWidget implements IMinecraft {
    public MapZoomControl mapZoomControlControl;
    public ChunkPos anchorChunkPos;
    public int chunksRadius = 8;
    public float panOffsetYChunks = 0.0F;
    public float panOffsetXChunks = 0.0F;
    public int lastMouseX;
    public int lastMouseY;
    public int lastChunksRadius;
    public float lastPanOffsetYChunks;
    public float lastPanOffsetXChunks;
    private final List<MapRightClickListener> rightClickListeners = new ArrayList<>();
    private final List<MapFrameUpdateListener> updateListeners = new ArrayList<>();

    private static class MapTextureSnapshot {
        final Chunk mapTexture;
        final ChunkPos textureCenterChunk;
        final long mapDataVersion;

        MapTextureSnapshot(Chunk mapTexture, ChunkPos textureCenterChunk, long mapDataVersion) {
            this.mapTexture = mapTexture;
            this.textureCenterChunk = textureCenterChunk;
            this.mapDataVersion = mapDataVersion;
        }
    }

    private MapTextureSnapshot currentTexture;
    private volatile MapTextureSnapshot pendingTexture;
    private volatile boolean textureGenerationInProgress = false;

    public WorldMapView(GuiComponent parent, String id, int x, int y, int width, int height) {
        super(parent, id, x, y, width, height, false);

        int zoomControlHeight = 90;
        int zoomControlWidth = 40;
        int zoomX = width - zoomControlWidth - 10;
        int zoomY = height - zoomControlHeight - 10;

        this.mapZoomControlControl = new MapZoomControl(this, "zoom", zoomX, zoomY, zoomControlWidth, zoomControlHeight);
        this.addToList(this.mapZoomControlControl);

        this.anchorChunkPos = client.world.getChunk(client.player.getBlockPos()).getPos();
        this.setListening(false);
    }

    public void zoom(boolean zoomIn) {
        this.chunksRadius = Math.max(3, Math.min(33, zoomIn ? this.chunksRadius - 1 : this.chunksRadius + 1));
        fireUpdateEvent();
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        if (this.isMouseDownOverComponent) {
            int deltaX = mouseX - this.lastMouseX;
            int deltaY = mouseY - this.lastMouseY;

            float scaleCompensation = ((float) this.chunksRadius - 1.0F) / (float) this.chunksRadius;
            float pixelsPerBlock = (float) this.width / ((float) this.chunksRadius * 2.0F * scaleCompensation);

            this.panOffsetXChunks += (float) deltaX / pixelsPerBlock;
            this.panOffsetYChunks += (float) deltaY / pixelsPerBlock;
        }

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    public void centerOn(int x, int z) {
        this.anchorChunkPos = new ChunkPos(x / 16, z / 16);
        this.panOffsetXChunks = -0.5F;
        this.panOffsetYChunks = -0.5F;
        this.mapZoomControlControl.shouldRecaptureBackground = true;
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (this.isHoveredInHierarchy() && mouseButton == 1) {
            Vec3i worldPos = getWorldPositionFromMouse(mouseX, mouseY);
            fireRightClickEvent(mouseX, mouseY, worldPos);
            return false;
        }

        fireUpdateEvent();
        return super.onMouseDown(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onScroll(float scroll) {
        super.onScroll(scroll);

        if (this.isMouseOverComponent(this.getMouseX(), this.getMouseY())) {
            int direction = scroll > 0.0F ? -1 : 1;
            int steps = Math.max(1, Math.round(Math.abs(scroll)));

            this.chunksRadius = Math.max(3, Math.min(33, this.chunksRadius + direction * steps));
            fireUpdateEvent();
        }
    }

    @Override
    public void draw(float partialTicks) {
        ChunkPos idealCenterChunk = computeIdealCenterChunk();

        if (partialTicks != 1.0F) {
            this.mapZoomControlControl.shouldRecaptureBackground = true;
        }

        applyPendingTextureIfReady();
        requestTextureIfNeeded(idealCenterChunk);

        if (needsRedraw()) {
            this.mapZoomControlControl.shouldRecaptureBackground = true;
        }

        if (this.currentTexture != null) {
            drawMapAndWaypoints();
        }

        drawCoordinateReadout();

        this.lastPanOffsetYChunks = this.panOffsetYChunks;
        this.lastPanOffsetXChunks = this.panOffsetXChunks;
        this.lastChunksRadius = this.chunksRadius;

        super.draw(partialTicks);
    }

    public void addRightClickListener(MapRightClickListener listener) {
        this.rightClickListeners.add(listener);
    }

    public void fireRightClickEvent(int mouseX, int mouseY, Vec3i position) {
        for (MapRightClickListener listener : this.rightClickListeners) {
            listener.onRightClick(this, mouseX, mouseY, position);
        }
    }

    public void addUpdateListener(MapFrameUpdateListener listener) {
        this.updateListeners.add(listener);
    }

    public void fireUpdateEvent() {
        for (MapFrameUpdateListener listener : this.updateListeners) {
            listener.onUpdate(this);
        }
    }

    private Vec3i getWorldPositionFromMouse(int mouseX, int mouseY) {
        int squareSize = Math.max(this.width, this.height);

        float padY = (float) (this.width - squareSize) / 2.0F;
        float padX = (float) (this.height - squareSize) / 2.0F;

        float localX = (float) mouseX - ((float) this.getAbsoluteX() + padX + (float) (squareSize / 2));
        float localY = (float) (client.getWindow().getHeight() - mouseY)
                - ((float) this.getAbsoluteY() + padY + (float) (squareSize / 2));

        float pixelsPerChunk = (float) squareSize / ((float) (this.chunksRadius - 1) * 2.0F);

        float originBlockX = (float) (this.anchorChunkPos.x * 16) - this.panOffsetXChunks * 16.0F;
        float originBlockZ = (float) (this.anchorChunkPos.z * 16) - this.panOffsetYChunks * 16.0F;

        float worldX = originBlockX + localX / pixelsPerChunk * 16.0F;
        float worldZ = originBlockZ - localY / pixelsPerChunk * 16.0F;

        return new Vec3i(Math.round(worldX), 0, Math.round(worldZ));
    }

    private ChunkPos computeIdealCenterChunk() {
        int centerX = (int) ((double) this.anchorChunkPos.x - Math.floor(this.panOffsetXChunks));
        int centerZ = (int) ((double) this.anchorChunkPos.z - Math.floor(this.panOffsetYChunks));
        return new ChunkPos(centerX, centerZ);
    }

    private void applyPendingTextureIfReady() {
        if (this.pendingTexture != null) {
            this.currentTexture = this.pendingTexture;
            this.pendingTexture = null;
            this.textureGenerationInProgress = false;
        }
    }

    private void requestTextureIfNeeded(ChunkPos idealCenterChunk) {
        long currentVersion = WaypointUtils.getMapDataVersion();

        boolean needsNewTexture = this.currentTexture == null
                || this.chunksRadius != this.lastChunksRadius
                || !this.currentTexture.textureCenterChunk.equals(idealCenterChunk)
                || this.currentTexture.mapDataVersion != currentVersion;

        if (!needsNewTexture || this.textureGenerationInProgress) {
            return;
        }

        this.textureGenerationInProgress = true;

        final long requestedVersion = currentVersion;
        final ChunkPos requestedCenter = idealCenterChunk;
        final int requestedSize = this.chunksRadius * 2;

        new Thread(() -> {
            Chunk texture = WaypointUtils.createMapTexture(requestedCenter, requestedSize);
            this.pendingTexture = new MapTextureSnapshot(texture, requestedCenter, requestedVersion);
        }).start();
    }

    private boolean needsRedraw() {
        return this.currentTexture == null
                || this.chunksRadius != this.lastChunksRadius
                || this.panOffsetXChunks != this.lastPanOffsetXChunks
                || this.panOffsetYChunks != this.lastPanOffsetYChunks;
    }

    private void drawMapAndWaypoints() {
        int squareSize = Math.max(this.width, this.height);
        int padX = (this.width - squareSize) / 2;
        int padY = (this.height - squareSize) / 2;

        float mapScale = (float) this.chunksRadius / ((float) this.chunksRadius - 1.0F);
        float pixelsPerChunk = (float) squareSize / ((float) this.chunksRadius * 2.0F);

        double centerDeltaX = this.anchorChunkPos.x - this.currentTexture.textureCenterChunk.x;
        double centerDeltaZ = this.anchorChunkPos.z - this.currentTexture.textureCenterChunk.z;

        double translateY = (this.panOffsetXChunks - centerDeltaX) * pixelsPerChunk;
        double translateX = (this.panOffsetYChunks - centerDeltaZ) * pixelsPerChunk;

        client.getTextureManager().bindTexture(TextureManager.MISSING_IDENTIFIER);

        ScissorUtils.startScissor(this.x, this.y, this.x + this.width, this.y + this.height, true);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.x + this.width / 2), (float) (this.y + this.height / 2), 0.0F);
        GL11.glScalef(mapScale, mapScale, 0.0F);
        GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-this.x - this.width / 2), (float) (-this.y - this.height / 2), 0.0F);
        GL11.glTranslated(-translateX, translateY, 0.0);

        RenderUtils.drawTexturedQuad(
                (float) (this.x + padX),
                (float) (this.y + padY),
                (float) squareSize,
                (float) squareSize,
                this.currentTexture.mapTexture.pixelBuffer,
                ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                0.0F,
                0.0F,
                (float) this.currentTexture.mapTexture.width,
                (float) this.currentTexture.mapTexture.height,
                true,
                false
        );

        GL11.glPopMatrix();

        drawWaypoints(squareSize);

        ScissorUtils.restoreScissor();
    }

    private void drawCoordinateReadout() {
        int originX = Math.round((float) (this.anchorChunkPos.x * 16) - this.panOffsetXChunks * 16.0F);
        int originZ = Math.round((float) (this.anchorChunkPos.z * 16) - this.panOffsetYChunks * 16.0F);

        String label = originX + "  " + originZ;

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.x - FontUtils.HELVETICA_LIGHT_14.getWidth(label) - 23),
                (float) (this.y + 35),
                label,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.4F)
        );
    }

    private void drawWaypoints(int squareSize) {
        float originBlockX = (float) (this.anchorChunkPos.x * 16) - this.panOffsetXChunks * 16.0F;
        float originBlockZ = (float) (this.anchorChunkPos.z * 16) - this.panOffsetYChunks * 16.0F;
        float pixelsPerBlock = (float) squareSize / ((float) (this.chunksRadius - 1) * 2.0F);

        for (Waypoint waypoint : Client.INSTANCE.waypointTracker.getWaypoints()) {
            float localBlockX = (float) waypoint.x - originBlockX + 1.0F;
            float localBlockZ = (float) waypoint.z - originBlockZ + 1.0F;

            RenderUtils.drawImage(
                    (float) (this.x + Math.round(localBlockX * pixelsPerBlock / 16.0F) + this.width / 2 - 16),
                    (float) (this.y + Math.round(localBlockZ * pixelsPerBlock / 16.0F) + this.height / 2 - 42),
                    32.0F,
                    46.0F,
                    Resources.WAYPOINT,
                    waypoint.color
            );
        }
    }
}