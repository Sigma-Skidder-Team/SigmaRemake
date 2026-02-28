package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.game.world.WaypointUtils;
import io.github.sst.remake.util.client.waypoint.Waypoint;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.shader.StencilUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

public class MapPanel extends InteractiveWidget {
    public MapFrame mapFrame;
    public WaypointList waypointList;
    public int waypointListWidth;

    public MapPanel(GuiComponent parent, String id, int x, int y, int width, int height) {
        super(parent, id, x, y, width, height, false);

        this.waypointListWidth = 260;

        this.waypointList = new WaypointList(this, "waypointList", 0, 65, this.waypointListWidth, this.height - 65);
        this.addToList(this.waypointList);

        for (Waypoint waypoint : Client.INSTANCE.waypointTracker.getWaypoints()) {
            this.waypointList.addWaypoint(
                    waypoint.name,
                    new Vec3i(waypoint.x, 64, waypoint.z),
                    waypoint.color
            );
        }

        this.mapFrame = new MapFrame(
                this,
                "mapFrame",
                this.waypointListWidth,
                0,
                this.width - this.waypointListWidth,
                this.height
        );
        this.addToList(this.mapFrame);

        this.setListening(false);
    }

    @Override
    public void onScroll(float scroll) {
        int mouseX = this.getMouseX();
        int mouseY = this.getMouseY();
        this.updatePanelDimensions(mouseX, mouseY);
        if (!this.isMouseOverComponent(mouseX, mouseY)) {
            return;
        }

        if (this.waypointList != null
                && this.waypointList.isSelfVisible()
                && this.waypointList.isMouseOverComponent(mouseX, mouseY)) {
            this.waypointList.onScroll(scroll);
            return;
        }

        if (this.mapFrame != null
                && this.mapFrame.isSelfVisible()
                && this.mapFrame.isMouseOverComponent(mouseX, mouseY)) {
            this.mapFrame.onScroll(scroll);
        }
    }

    @Override
    public void draw(float partialTicks) {
        int padding = 14;

        RenderUtils.drawRoundedRect(
                (float) (this.x + padding / 2),
                (float) (this.y + padding / 2),
                (float) (this.width - padding),
                (float) (this.height - padding),
                20.0F,
                partialTicks * 0.9F
        );

        float panelAlpha = Client.INSTANCE.configManager.guiBlur ? 0.88F : 0.95F;

        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                14.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), panelAlpha)
        );

        RenderUtils.drawRoundedButton(
                (float) (this.x + this.waypointListWidth),
                (float) this.y,
                (float) (this.width - this.waypointListWidth),
                (float) this.height,
                14.0F,
                -7687425
        );

        StencilUtils.beginStencilWrite();
        RenderUtils.drawRoundedButton(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                14.0F,
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );
        StencilUtils.beginStencilRead();

        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.getX(), (float) this.getY(), 0.0F);
        this.waypointList.draw(partialTicks);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.getX(), (float) this.getY(), 0.0F);
        this.mapFrame.draw(partialTicks);
        GL11.glPopMatrix();

        StencilUtils.endStencil();

        RenderUtils.drawRoundedRect2(
                (float) (this.x + this.waypointListWidth),
                (float) this.y,
                1.0F,
                (float) this.height,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.14F)
        );

        int headerColor = ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.6F);
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25,
                (float) (this.x + 30),
                (float) (this.y + 25),
                "Waypoints",
                headerColor
        );

        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_40,
                (float) this.x,
                (float) ((this.parent.getHeight() - this.height) / 2 - 70),
                "Jello Maps",
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );

        String worldLabel = WaypointUtils.getWorldIdentifier().replace("/", " - ");
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_24,
                (float) (this.x + this.width - FontUtils.HELVETICA_LIGHT_24.getWidth(worldLabel) - 10),
                (float) ((this.parent.getHeight() - this.height) / 2 - 62),
                worldLabel,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F)
        );
    }
}