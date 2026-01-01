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
    public Date field21035;
    public MapPanel field21036;
    public WaypointPanel field21041;

    public JelloMaps() {
        super("Maps");
        this.field21035 = new Date();
        int var3 = Math.max(300, Math.min(850, client.getWindow().getWidth() - 40));
        int var4 = Math.max(200, Math.min(550, client.getWindow().getHeight() - 80));
        this.addToList(this.field21036 = new MapPanel(this, "mapView", (this.width - var3) / 2, (this.height - var4) / 2, var3, var4));
        this.field21036.field20614.method13080((frame, mouseX, mouseY, vec) -> this.addRunnable(() -> {
            if (this.field21041 == null) {
                this.addToList(this.field21041 = new WaypointPanel(this, "popover", mouseX, mouseY, vec));
                method13389(this.field21041);
            }
        }));
        this.field21036.field20614.method13082(var1 -> this.method13390());
        ShaderUtils.applyBlurShader();
    }

    private void method13389(WaypointPanel var1) {
        var1.method13131((var1x, var2, var3, var4) -> {
            this.field21036.waypointList.addWaypoint(var2, var3, var4);
            Client.INSTANCE.waypointManager.add(new Waypoint(var2, var3.getX(), var3.getZ(), var4));
            this.method13390();
        });
    }

    private void method13390() {
        for (CustomGuiScreen child : this.getChildren()) {
            if (child instanceof WaypointPanel) {
                this.addRunnable(() -> {
                    this.removeChildren(child);
                    this.field21041 = null;
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
        partialTicks = (float) Math.min(200L, new Date().getTime() - this.field21035.getTime()) / 200.0F;
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
