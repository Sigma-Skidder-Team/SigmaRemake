package io.github.sst.remake.gui.screen.maps;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.widget.TextButton;
import io.github.sst.remake.gui.framework.widget.TextField;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.EasingFunctions;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddWaypointDialog extends InteractiveWidget {
    private final Date openAnimationStart;
    private Date closeAnimationStart;

    private boolean openToLeft = false;

    public Vec3i defaultCoords;
    public TextField coordsField;
    public TextField nameField;
    public WaypointColorSelector colorSelector;

    private final List<WaypointAddListener> addListeners = new ArrayList<>();

    public AddWaypointDialog(GuiComponent parent, String id, int x, int y, Vec3i initialCoords) {
        super(parent, id, x - 107, y + 10, 214, 170, ColorHelper.DEFAULT_COLOR, "", false);

        this.defaultCoords = initialCoords;

        if (this.y + this.height <= MinecraftClient.getInstance().getWindow().getHeight()) {
            this.y += 10;
        } else {
            this.y = this.y - (this.height + 27);
            this.openToLeft = true;
        }

        this.openAnimationStart = new Date();

        this.setReAddChildren(true);
        this.setListening(false);

        TextButton addButton = new TextButton(
                this,
                "addButton",
                this.width - 66,
                this.height - 60,
                FontUtils.HELVETICA_LIGHT_25.getWidth("Add"),
                50,
                ColorHelper.DEFAULT_COLOR,
                "Add",
                FontUtils.HELVETICA_LIGHT_25
        );
        this.addToList(addButton);

        addButton.onClick((widget, mouseButton) ->
                this.fireWaypointAdded(this.nameField.getText(), this.parseCoordsOrDefault(), this.colorSelector.selectedColor)
        );

        this.nameField = new TextField(
                this,
                "Name",
                20,
                7,
                this.width - 40,
                60,
                TextField.DEFAULT_COLORS,
                "My waypoint",
                "My waypoint"
        );
        this.addToList(this.nameField);

        this.nameField.startFocus();
        this.nameField.setUnderlineEnabled(false);

        this.colorSelector = new WaypointColorSelector(this, "badgeSelect", 0, 86);
        this.addToList(this.colorSelector);

        String defaultCoordsText = initialCoords.getX() + " " + initialCoords.getZ();
        this.coordsField = new TextField(
                this,
                "Coords",
                20,
                this.height - 44,
                this.width - 100,
                20,
                TextField.DEFAULT_COLORS,
                defaultCoordsText,
                defaultCoordsText
        );
        this.addToList(this.coordsField);

        this.coordsField.setUnderlineEnabled(false);
        this.coordsField.setFont(FontUtils.HELVETICA_LIGHT_18);
    }

    public Vec3i parseCoordsOrDefault() {
        String text = this.coordsField.getText();
        if (text != null && text.contains(" ")) {
            String[] parts = text.split(" ");
            if (parts.length == 2 && parts[0].matches("-?\\d+") && parts[1].matches("-?\\d+")) {
                int x = Integer.parseInt(parts[0]);
                int z = Integer.parseInt(parts[1]);
                return new Vec3i(x, 0, z);
            }
        }
        return this.defaultCoords;
    }

    @Override
    public void draw(float partialTicks) {
        float animProgress = AnimationUtils.calculateProgressWithReverse(
                this.openAnimationStart,
                this.closeAnimationStart,
                250.0F,
                120.0F
        );

        float eased = EasingFunctions.easeOutBack(animProgress, 0.0F, 1.0F, 1.0F);

        this.setScale(0.8F + eased * 0.2F, 0.8F + eased * 0.2F);

        int slideDirection = this.openToLeft ? -1 : 1;
        this.setTranslateX((int) ((float) this.width * 0.2F * (1.0F - eased)) * slideDirection);

        super.applyScaleTransforms();

        int padding = 10;

        int bgColor = ColorHelper.applyAlpha(
                -723724,
                QuadraticEasing.easeOutQuad(animProgress, 0.0F, 1.0F, 1.0F)
        );

        RenderUtils.drawRoundedRect(
                (float) (this.x + padding / 2),
                (float) (this.y + padding / 2),
                (float) (this.width - padding),
                (float) (this.height - padding),
                35.0F,
                animProgress
        );

        RenderUtils.drawRoundedRect(
                (float) (this.x + padding / 2),
                (float) (this.y + padding / 2),
                (float) (this.x - padding / 2 + this.width),
                (float) (this.y - padding / 2 + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), animProgress * 0.25F)
        );

        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                (float) padding,
                bgColor
        );

        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.x, (float) this.y, 0.0F);
        GL11.glRotatef(!this.openToLeft ? -90.0F : 90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-this.x), (float) (-this.y), 0.0F);

        RenderUtils.drawImage(
                (float) (this.x + (!this.openToLeft ? 0 : this.height)),
                (float) this.y + (float) ((this.width - 47) / 2) * (!this.openToLeft ? 1.0F : -1.58F),
                18.0F,
                47.0F,
                Resources.SELECTED_ICON,
                bgColor
        );
        GL11.glPopMatrix();

        RenderUtils.drawRoundedRect(
                (float) (this.x + 25),
                (float) (this.y + 68),
                (float) (this.x + this.width - 25),
                (float) (this.y + 69),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * animProgress)
        );

        super.draw(animProgress);
    }

    public void addWaypointAddListener(WaypointAddListener var1) {
        this.addListeners.add(var1);
    }

    public void fireWaypointAdded(String name, Vec3i coords, int color) {
        for (WaypointAddListener listener : this.addListeners) {
            listener.onWaypointAdded(this, name, coords, color);
        }
    }

    public interface WaypointAddListener {
        void onWaypointAdded(AddWaypointDialog dialog, String name, Vec3i coords, int color);
    }
}