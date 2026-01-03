package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.WaypointUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import io.github.sst.remake.util.client.waypoint.Chunk;
import io.github.sst.remake.util.client.waypoint.Waypoint;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class MapFrame extends InteractiveWidget implements IMinecraft {
    public Zoom zoom;
    public ChunkPos chunkPos;
    public int zoomLevel = 8;
    public float chunkOffsetY = 0.0F;
    public float chunkOffsetX = 0.0F;
    public int lastMouseX;
    public int lastMouseY;
    public int lastZoomLevel;
    public float lastChunkOffsetY;
    public float lastChunkOffsetX;
    private final List<MapRightClickListener> rightClickListeners = new ArrayList<>();
    private final List<MapFrameUpdateListener> updateListeners = new ArrayList<>();

    private static class MapTextureData {
        final Chunk texture;
        final ChunkPos center;

        MapTextureData(Chunk texture, ChunkPos center) {
            this.texture = texture;
            this.center = center;
        }
    }

    private MapTextureData currentMapTextureData;
    private volatile MapTextureData nextMapTextureData;
    private volatile boolean isGeneratingTexture = false;

    public MapFrame(GuiComponent var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
        int var9 = 90;
        int var10 = 40;
        int var11 = var5 - var10 - 10;
        int var12 = var6 - var9 - 10;
        this.addToList(this.zoom = new Zoom(this, "zoom", var11, var12, var10, var9));
        this.chunkPos = client.world.getChunk(client.player.getBlockPos()).getPos();
        this.setListening(false);
    }

    public void zoom(boolean in) {
        this.zoomLevel = Math.max(3, Math.min(33, !in ? this.zoomLevel + 1 : this.zoomLevel - 1));
        this.fireUpdateEvent();
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        if (this.isMouseDownOverComponent) {
            int var5 = mouseX - this.lastMouseX;
            int var6 = mouseY - this.lastMouseY;
            float var7 = ((float) this.zoomLevel - 1.0F) / (float) this.zoomLevel;
            float var8 = (float) this.width / ((float) this.zoomLevel * 2.0F * var7);
            this.chunkOffsetX += (float) var5 / var8;
            this.chunkOffsetY += (float) var6 / var8;
        }

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    public void centerOn(int x, int z) {
        this.chunkPos = new ChunkPos(x / 16, z / 16);
        this.chunkOffsetX = -0.5F;
        this.chunkOffsetY = -0.5F;
        this.zoom.needsRedraw = true;
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (this.isHoveredInHierarchy() && mouseButton == 1) {
            int var6 = Math.max(this.width, this.height);
            float var7 = (float) (this.width - var6) / 2.0F;
            float var8 = (float) (this.height - var6) / 2.0F;
            float var9 = (float) mouseX - ((float) this.getAbsoluteX() + var8 + (float) (var6 / 2));
            float var10 = (float) (client.getWindow().getHeight() - mouseY) - ((float) this.getAbsoluteY() + var7 + (float) (var6 / 2));
            float var11 = (float) var6 / ((float) (this.zoomLevel - 1) * 2.0F);
            float var12 = (float) (this.chunkPos.x * 16) - this.chunkOffsetX * 16.0F;
            float var13 = (float) (this.chunkPos.z * 16) - this.chunkOffsetY * 16.0F;
            float var14 = var12 + var9 / var11 * 16.0F;
            float var15 = var13 - var10 / var11 * 16.0F;
            this.fireRightClickEvent(mouseX, mouseY, new Vec3i(Math.round(var14), 0, Math.round(var15)));
            return false;
        } else {
            this.fireUpdateEvent();
            return super.onMouseDown(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void onScroll(float scroll) {
        super.onScroll(scroll);
        if (this.isHoveredInHierarchy()) {
            this.zoomLevel = Math.round(Math.max(3.0F, Math.min(33.0F, (float) this.zoomLevel + scroll / 10.0F)));
            this.fireUpdateEvent();
        }
    }

    @Override
    public void draw(float partialTicks) {
        ChunkPos idealCenterChunk = new ChunkPos((int)((double)this.chunkPos.x - Math.floor(this.chunkOffsetX)), (int)((double)this.chunkPos.z - Math.floor(this.chunkOffsetY)));
        if (partialTicks != 1.0F) {
            this.zoom.needsRedraw = true;
        }

        if (this.nextMapTextureData != null) {
            this.currentMapTextureData = this.nextMapTextureData;
            this.nextMapTextureData = null;
            this.isGeneratingTexture = false;
        }

        boolean needsNewTexture = this.currentMapTextureData == null || this.zoomLevel != this.lastZoomLevel || !this.currentMapTextureData.center.equals(idealCenterChunk);
        if (needsNewTexture && !this.isGeneratingTexture) {
            this.isGeneratingTexture = true;
            new Thread(() -> {
                Chunk newTexture = WaypointUtils.createMapTexture(idealCenterChunk, this.zoomLevel * 2);
                this.nextMapTextureData = new MapTextureData(newTexture, idealCenterChunk);
            }).start();
        }

        if (this.currentMapTextureData == null || this.zoomLevel != this.lastZoomLevel || this.chunkOffsetX != this.lastChunkOffsetX || this.chunkOffsetY != this.lastChunkOffsetY) {
            this.zoom.needsRedraw = true;
        }

        if (this.currentMapTextureData != null) {
            int var6 = Math.max(this.width, this.height);
            int var7 = (this.width - var6) / 2;
            int var8 = (this.height - var6) / 2;
            float var9 = (float) this.zoomLevel / ((float) this.zoomLevel - 1.0F);
            float pixelsPerChunk = (float)var6 / ((float)this.zoomLevel * 2.0F);

            double floorX = this.chunkPos.x - this.currentMapTextureData.center.x;
            double floorZ = this.chunkPos.z - this.currentMapTextureData.center.z;

            double translationY = (this.chunkOffsetX - floorX) * pixelsPerChunk;
            double translationX = (this.chunkOffsetY - floorZ) * pixelsPerChunk;

            client.getTextureManager().bindTexture(TextureManager.MISSING_IDENTIFIER);
            ScissorUtils.startScissor(this.x, this.y, this.x + this.width, this.y + this.height, true);
            GL11.glPushMatrix();
            GL11.glTranslatef((float) (this.x + this.width / 2), (float) (this.y + this.height / 2), 0.0F);
            GL11.glScalef(var9, var9, 0.0F);
            GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef((float) (-this.x - this.width / 2), (float) (-this.y - this.height / 2), 0.0F);
            GL11.glTranslated(-translationX, translationY, 0.0);
            RenderUtils.drawTexturedQuad(
                    (float) (this.x + var7),
                    (float) (this.y + var8),
                    (float) var6,
                    (float) var6,
                    this.currentMapTextureData.texture.pixelBuffer,
                    ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                    0.0F,
                    0.0F,
                    (float) this.currentMapTextureData.texture.width,
                    (float) this.currentMapTextureData.texture.height,
                    true,
                    false
            );
            GL11.glPopMatrix();

            for (Waypoint var16 : Client.INSTANCE.waypointManager.getWaypoints()) {
                float var17 = (float) (this.chunkPos.x * 16) - this.chunkOffsetX * 16.0F;
                float var18 = (float) (this.chunkPos.z * 16) - this.chunkOffsetY * 16.0F;
                float var19 = (float) var16.x - var17 + 1.0F;
                float var20 = (float) var16.z - var18 + 1.0F;
                float var21 = (float) var6 / ((float) (this.zoomLevel - 1) * 2.0F);
                RenderUtils.drawImage(
                        (float) (this.x + Math.round(var19 * var21 / 16.0F) + this.width / 2 - 16),
                        (float) (this.y + Math.round(var20 * var21 / 16.0F) + this.height / 2 - 42),
                        32.0F,
                        46.0F,
                        Resources.waypointPNG,
                        var16.color
                );
            }

            ScissorUtils.restoreScissor();
        }
        int var22 = Math.round((float) (this.chunkPos.x * 16) - this.chunkOffsetX * 16.0F);
        int var23 = Math.round((float) (this.chunkPos.z * 16) - this.chunkOffsetY * 16.0F);
        String var24 = var22 + "  " + var23;
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.x - FontUtils.HELVETICA_LIGHT_14.getWidth(var24) - 23),
                (float) (this.y + 35),
                var24,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.4F)
        );
        this.lastChunkOffsetY = this.chunkOffsetY;
        this.lastChunkOffsetX = this.chunkOffsetX;
        this.lastZoomLevel = this.zoomLevel;
        super.draw(partialTicks);
    }

    public final void addRightClickListener(MapRightClickListener listener) {
        this.rightClickListeners.add(listener);
    }

    public final void fireRightClickEvent(int mouseX, int mouseY, Vec3i position) {
        for (MapRightClickListener listener : this.rightClickListeners) {
            listener.onRightClick(this, mouseX, mouseY, position);
        }
    }

    public final void addUpdateListener(MapFrameUpdateListener listener) {
        this.updateListeners.add(listener);
    }

    public final void fireUpdateEvent() {
        for (MapFrameUpdateListener listener : this.updateListeners) {
            listener.onUpdate(this);
        }
    }

    public interface MapRightClickListener {
        void onRightClick(MapFrame frame, int mouseX, int mouseY, Vec3i position);
    }
}
