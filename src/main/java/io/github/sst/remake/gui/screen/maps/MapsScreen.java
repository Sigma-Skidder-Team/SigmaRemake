package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.anim.ease.EasingFunctions;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.shader.ShaderUtils;
import io.github.sst.remake.util.client.waypoint.Waypoint;
import org.lwjgl.glfw.GLFW;

import java.util.Date;

public class MapsScreen extends Screen implements IMinecraft {
    public Date openTime;
    public MapPanel mapPanel;
    public AddWaypointDialog addWaypointDialog;

    public MapsScreen() {
        super("Maps");

        this.openTime = new Date();

        int panelWidth = Math.max(300, Math.min(850, client.getWindow().getWidth() - 40));
        int panelHeight = Math.max(200, Math.min(550, client.getWindow().getHeight() - 80));

        this.mapPanel = new MapPanel(
                this,
                "mapView",
                (this.width - panelWidth) / 2,
                (this.height - panelHeight) / 2,
                panelWidth,
                panelHeight
        );
        this.addToList(this.mapPanel);

        this.mapPanel.mapFrame.addRightClickListener((frame, mouseX, mouseY, worldPos) ->
                this.addRunnable(() -> this.openAddWaypointDialog(mouseX, mouseY, worldPos))
        );

        this.mapPanel.mapFrame.addUpdateListener(ignored -> this.closeAddWaypointDialog());

        ShaderUtils.applyBlurShader();
    }

    private void openAddWaypointDialog(int mouseX, int mouseY, net.minecraft.util.math.Vec3i coords) {
        if (this.addWaypointDialog != null) {
            return;
        }

        this.addWaypointDialog = new AddWaypointDialog(this, "popover", mouseX, mouseY, coords);
        this.addToList(this.addWaypointDialog);

        attachWaypointAddedListener(this.addWaypointDialog);
    }

    private void attachWaypointAddedListener(AddWaypointDialog dialog) {
        dialog.addWaypointAddListener((sourceDialog, name, coords, color) -> {
            this.mapPanel.waypointList.addWaypoint(name, coords, color);

            Client.INSTANCE.waypointTracker.add(new Waypoint(
                    name,
                    coords.getX(),
                    coords.getZ(),
                    color
            ));

            this.closeAddWaypointDialog();
        });
    }

    private void closeAddWaypointDialog() {
        for (GuiComponent child : this.getChildren()) {
            if (child instanceof AddWaypointDialog) {
                this.addRunnable(() -> {
                    this.removeChildren(child);
                    this.addWaypointDialog = null;
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
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            ShaderUtils.resetShader();
            client.openScreen(null);
        }
    }

    @Override
    public void onScroll(float scroll) {
        int mouseX = this.getMouseX();
        int mouseY = this.getMouseY();
        this.updatePanelDimensions(mouseX, mouseY);

        if (this.mapPanel != null) {
            this.mapPanel.onScroll(scroll);
            return;
        }

        super.onScroll(scroll);
    }

    @Override
    public void draw(float partialTicks) {
        float introProgress = (float) Math.min(200L, new Date().getTime() - this.openTime.getTime()) / 200.0F;
        float eased = EasingFunctions.easeOutBack(introProgress, 0.0F, 1.0F, 1.0F);

        this.setScale(0.8F + eased * 0.2F, 0.8F + eased * 0.2F);

        float overlayAlpha = 0.25F * introProgress;
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) (this.x + this.width),
                (float) (this.y + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), overlayAlpha)
        );

        super.applyScaleTransforms();
        super.draw(introProgress);
    }
}