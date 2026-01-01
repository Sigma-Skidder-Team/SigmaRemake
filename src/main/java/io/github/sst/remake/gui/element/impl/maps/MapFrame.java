package io.github.sst.remake.gui.element.impl.maps;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
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

public class MapFrame extends Element implements IMinecraft {
    public Zoom field20647;
    public ChunkPos chunkPos;
    public int field20649 = 8;
    public float field20650 = 0.0F;
    public float field20651 = 0.0F;
    public int field20652;
    public int field20653;
    public Chunk field20654;
    public int field20655;
    public float field20656;
    public float field20657;
    public ChunkPos field20658;
    private final List<Class8041> field20659 = new ArrayList<>();
    private final List<Class9693> field20660 = new ArrayList<>();

    public MapFrame(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
        int var9 = 90;
        int var10 = 40;
        int var11 = var5 - var10 - 10;
        int var12 = var6 - var9 - 10;
        this.addToList(this.field20647 = new Zoom(this, "zoom", var11, var12, var10, var9));
        this.chunkPos = client.world.getChunk(client.player.getBlockPos()).getPos();
        this.setListening(false);
    }

    public void method13076(boolean var1) {
        this.field20649 = Math.max(3, Math.min(33, !var1 ? this.field20649 + 1 : this.field20649 - 1));
        this.method13083();
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        if (this.isMouseDownOverComponent) {
            int var5 = mouseX - this.field20652;
            int var6 = mouseY - this.field20653;
            float var7 = ((float) this.field20649 - 1.0F) / (float) this.field20649;
            float var8 = (float) this.width / ((float) this.field20649 * 2.0F * var7);
            this.field20651 += (float) var5 / var8;
            this.field20650 += (float) var6 / var8;
        }

        this.field20652 = mouseX;
        this.field20653 = mouseY;
    }

    public void method13077(int var1, int var2) {
        this.chunkPos = new ChunkPos(var1 / 16, var2 / 16);
        this.field20651 = -0.5F;
        this.field20650 = -0.5F;
        this.field20647.field20687 = true;
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (this.isHoveredInHierarchy() && mouseButton == 1) {
            int var6 = Math.max(this.width, this.height);
            float var7 = (float) (this.width - var6) / 2.0F;
            float var8 = (float) (this.height - var6) / 2.0F;
            float var9 = (float) mouseX - ((float) this.getAbsoluteX() + var8 + (float) (var6 / 2));
            float var10 = (float) (client.getWindow().getHeight() - mouseY) - ((float) this.getAbsoluteY() + var7 + (float) (var6 / 2));
            float var11 = (float) var6 / ((float) (this.field20649 - 1) * 2.0F);
            float var12 = (float) (this.chunkPos.x * 16) - this.field20651 * 16.0F;
            float var13 = (float) (this.chunkPos.z * 16) - this.field20650 * 16.0F;
            float var14 = var12 + var9 / var11 * 16.0F;
            float var15 = var13 - var10 / var11 * 16.0F;
            this.method13081(mouseX, mouseY, new Vec3i(Math.round(var14), 0, Math.round(var15)));
            return false;
        } else {
            this.method13083();
            return super.onMouseDown(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void onScroll(float scroll) {
        super.onScroll(scroll);
        if (this.isHoveredInHierarchy()) {
            this.field20649 = Math.round(Math.max(3.0F, Math.min(33.0F, (float) this.field20649 + scroll / 10.0F)));
            this.method13083();
        }
    }

    @Override
    public void draw(float partialTicks) {
        ChunkPos var5 = new ChunkPos(this.chunkPos.x, this.chunkPos.z);
        var5.x = (int) ((double) var5.x - Math.floor(this.field20651));
        var5.z = (int) ((double) var5.z - Math.floor(this.field20650));
        if (partialTicks != 1.0F) {
            this.field20647.field20687 = true;
        }

        if (this.field20654 == null || this.field20649 != this.field20655 || !this.field20658.equals(var5)) {
            this.field20654 = WaypointUtils.method30003(var5, this.field20649 * 2);
        }

        if (this.field20654 == null || this.field20649 != this.field20655 || this.field20651 != this.field20657 || this.field20650 != this.field20656) {
            this.field20647.field20687 = true;
        }

        int var6 = Math.max(this.width, this.height);
        int var7 = (this.width - var6) / 2;
        int var8 = (this.height - var6) / 2;
        float var9 = (float) this.field20649 / ((float) this.field20649 - 1.0F);
        float var10 = (float) var6 / ((float) this.field20649 * 2.0F);
        double var11 = ((double) this.field20650 - Math.floor(this.field20650)) * (double) var10;
        double var13 = ((double) this.field20651 - Math.floor(this.field20651)) * (double) var10;
        TextureManager textureManager = client.getTextureManager();
        textureManager.bindTexture(TextureManager.MISSING_IDENTIFIER);
        ScissorUtils.startScissor(this.x, this.y, this.x + this.width, this.y + this.height, true);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.x + this.width / 2), (float) (this.y + this.height / 2), 0.0F);
        GL11.glScalef(var9, var9, 0.0F);
        GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-this.x - this.width / 2), (float) (-this.y - this.height / 2), 0.0F);
        GL11.glTranslated(-var11, var13, 0.0);
        RenderUtils.drawTexturedQuad(
                (float) (this.x + var7),
                (float) (this.y + var8),
                (float) var6,
                (float) var6,
                this.field20654.field30546,
                ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                0.0F,
                0.0F,
                (float) this.field20654.field30544,
                (float) this.field20654.field30545,
                true,
                false
        );
        GL11.glPopMatrix();

        for (Waypoint var16 : Client.INSTANCE.waypointManager.waypoints) {
            float var17 = (float) (this.chunkPos.x * 16) - this.field20651 * 16.0F;
            float var18 = (float) (this.chunkPos.z * 16) - this.field20650 * 16.0F;
            float var19 = (float) var16.x - var17 + 1.0F;
            float var20 = (float) var16.z - var18 + 1.0F;
            float var21 = (float) var6 / ((float) (this.field20649 - 1) * 2.0F);
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
        int var22 = Math.round((float) (this.chunkPos.x * 16) - this.field20651 * 16.0F);
        int var23 = Math.round((float) (this.chunkPos.z * 16) - this.field20650 * 16.0F);
        String var24 = var22 + "  " + var23;
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.x - FontUtils.HELVETICA_LIGHT_14.getWidth(var24) - 23),
                (float) (this.y + 35),
                var24,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.4F)
        );
        this.field20656 = this.field20650;
        this.field20657 = this.field20651;
        this.field20655 = this.field20649;
        this.field20658 = var5;
        super.draw(partialTicks);
    }

    public final void method13080(Class8041 var1) {
        this.field20659.add(var1);
    }

    public final void method13081(int mouseX, int mouseY, Vec3i vec) {
        for (Class8041 var7 : this.field20659) {
            var7.method27609(this, mouseX, mouseY, vec);
        }
    }

    public final void method13082(Class9693 var1) {
        this.field20660.add(var1);
    }

    public final void method13083() {
        for (Class9693 var4 : this.field20660) {
            var4.method37947(this);
        }
    }

    public interface Class8041 {
        void method27609(MapFrame frame, int mouseX, int mouseY, Vec3i vec);
    }
}
