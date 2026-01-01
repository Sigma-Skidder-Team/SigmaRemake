package io.github.sst.remake.gui.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.element.impl.maps.MapPanel;
import io.github.sst.remake.gui.element.impl.maps.WaypointPanel;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ShaderUtils;
import io.github.sst.remake.util.client.waypoint.Waypoint;

import java.util.Date;

public class JelloMaps extends Screen implements IMinecraft {
    public Date creationTime;
    public MapPanel mapPanel;
    public WaypointPanel waypointPanel;

    public JelloMaps() {
        super("Maps");
        this.creationTime = new Date();
        int var3 = Math.max(300, Math.min(850, client.getWindow().getWidth() - 40));
        int var4 = Math.max(200, Math.min(550, client.getWindow().getHeight() - 80));
        this.addToList(this.mapPanel = new MapPanel(this, "mapView", (this.width - var3) / 2, (this.height - var4) / 2, var3, var4));
        this.mapPanel.mapFrame.addRightClickListener((frame, mouseX, mouseY, vec) -> this.addRunnable(() -> {
            if (this.waypointPanel == null) {
                this.addToList(this.waypointPanel = new WaypointPanel(this, "popover", mouseX, mouseY, vec));
                setupWaypointPanelListener(this.waypointPanel);
            }
        }));
        this.mapPanel.mapFrame.addUpdateListener(var1 -> this.closeWaypointPanel());
        ShaderUtils.applyBlurShader();
    }

    private void setupWaypointPanelListener(WaypointPanel panel) {
        panel.method13131((var1x, var2, var3, var4) -> {
            this.mapPanel.waypointList.addWaypoint(var2, var3, var4);
            Client.INSTANCE.waypointManager.add(new Waypoint(var2, var3.getX(), var3.getZ(), var4));
            this.closeWaypointPanel();
        });
    }

    private void closeWaypointPanel() {
        for (CustomGuiScreen child : this.getChildren()) {
            if (child instanceof WaypointPanel) {
                this.addRunnable(() -> {
                    this.removeChildren(child);
                    this.waypointPanel = null;
                });
            }
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.setListening(false);
    }

    @Override
    public int getFPS() {
        return 60;
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            ShaderUtils.resetShader();
            client.openScreen(null);
        }
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = (float) Math.min(200L, new Date().getTime() - this.creationTime.getTime()) / 200.0F;
        float var4 = EasingFunctions.easeOutBack(partialTicks, 0.0F, 1.0F, 1.0F);
        this.setScale(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
        float var5 = 0.25F * partialTicks;
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) (this.x + this.width),
                (float) (this.y + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), var5)
        );
        super.applyScaleTransforms();
        super.draw(partialTicks);
    }

}
