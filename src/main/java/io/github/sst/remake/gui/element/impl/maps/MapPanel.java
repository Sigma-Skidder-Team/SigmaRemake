package io.github.sst.remake.gui.element.impl.maps;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.util.client.WaypointUtils;
import io.github.sst.remake.util.client.waypoint.Waypoint;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.Vector3m;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.StencilUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class MapPanel extends Element {
    public int field20613;
    public MapFrame field20614;
    public WaypointList waypointList;
    public int field20616;
    private final List<Class9514> field20617 = new ArrayList<>();

    public MapPanel(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.field20616 = 260;
        this.addToList(this.waypointList = new WaypointList(this, "waypointList", 0, 65, this.field20616, this.height - 65));

        for (Waypoint var10 : Client.INSTANCE.waypointManager.waypoints) {
            this.waypointList.addWaypoint(var10.name, new Vec3i(var10.x, 64, var10.z), var10.color);
        }

        this.addToList(this.field20614 = new MapFrame(this, "mapFrame", this.field20616, 0, this.width - this.field20616, this.height));
        this.setListening(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        if (this.isMouseDownOverComponent) {
            WaypointUtils.field36375.clear();
        }
    }

    @Override
    public void draw(float partialTicks) {
        int var4 = 14;
        RenderUtils.drawRoundedRect(
                (float) (this.x + var4 / 2),
                (float) (this.y + var4 / 2),
                (float) (this.width - var4),
                (float) (this.height - var4),
                20.0F,
                partialTicks * 0.9F
        );
        float var5 = 0.88F;
        if (!Client.INSTANCE.configManager.guiBlur) {
            var5 = 0.95F;
        }

        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                14.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var5)
        );
        RenderUtils.drawRoundedButton(
                (float) (this.x + this.field20616),
                (float) this.y,
                (float) (this.width - this.field20616),
                (float) this.height,
                14.0F,
                -7687425
        );
        StencilUtils.beginStencilWrite();
        RenderUtils.drawRoundedButton(
                (float) this.x, (float) this.y, (float) this.width, (float) this.height, 14.0F, ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );
        StencilUtils.beginStencilRead();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.getX(), (float) this.getY(), 0.0F);
        this.waypointList.draw(partialTicks);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.getX(), (float) this.getY(), 0.0F);
        this.field20614.draw(partialTicks);
        GL11.glPopMatrix();
        StencilUtils.endStencil();
        RenderUtils.drawRoundedRect2(
                (float) (this.x + this.field20616),
                (float) (this.y),
                1.0F,
                (float) this.height,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.14F)
        );
        int var6 = ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.6F);
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_25, (float) (this.x + 30), (float) (this.y + 25), "Waypoints", var6);
        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_40,
                (float) this.x,
                (float) ((this.parent.getHeight() - this.height) / 2 - 70),
                "Jello Maps",
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );
        String var7 = WaypointUtils.getWorldIdentifier().replace("/", " - ");
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_24,
                (float) (this.x + this.width - FontUtils.HELVETICA_LIGHT_24.getWidth(var7) - 10),
                (float) ((this.parent.getHeight() - this.height) / 2 - 62),
                var7,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F)
        );
    }

    public final void method13043(Class9514 var1) {
        this.field20617.add(var1);
    }

    public final void method13044(int var1, int var2, Vector3m var3) {
        for (Class9514 var7 : this.field20617) {
            var7.method36764(this, var1, var2, var3);
        }
    }
}
