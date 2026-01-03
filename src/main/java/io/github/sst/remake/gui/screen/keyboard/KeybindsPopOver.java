package io.github.sst.remake.gui.screen.keyboard;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.widget.TextButton;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class KeybindsPopOver extends InteractiveWidget {
    private final int field21376;
    private final AnimationUtils field21377;
    private boolean field21378 = false;
    private final List<AddButtonListener> addButtonListeners = new ArrayList<AddButtonListener>();

    public KeybindsPopOver(GuiComponent var1, String var2, int var3, int var4, int var5, String var6) {
        super(var1, var2, var3 - 125, var4, 250, 330, ColorHelper.DEFAULT_COLOR, var6, false);
        if (this.y + this.height <= MinecraftClient.getInstance().getWindow().getHeight()) {
            this.y += 10;
        } else {
            this.y -= 400;
            this.field21378 = true;
        }

        this.field21376 = var5;
        this.field21377 = new AnimationUtils(250, 0);
        this.setReAddChildren(true);
        this.setListening(false);
        this.method13712();
        TextButton var9;
        this.addToList(
                var9 = new TextButton(
                        this,
                        "addButton",
                        this.width - 70,
                        this.height - 70,
                        FontUtils.HELVETICA_LIGHT_25.getWidth("Add"),
                        70,
                        ColorHelper.DEFAULT_COLOR,
                        "Add",
                        FontUtils.HELVETICA_LIGHT_25
                )
        );
        var9.onClick((var1x, var2x) -> this.notifyAddButtonListeners());
    }

    public void method13712() {
        int var3 = 1;
        List<String> children = new ArrayList<>();

        for (GuiComponent child : this.getChildren()) {
            if (child.getHeight() != 0) {
                children.add(child.getName());
            }
        }

        this.requestFocus();
        this.setFocused(true);
        this.clearChildren();

        for (BindableAction var10 : KeyboardScreen.getBindableActions()) {
            int var7 = var10.getBind();
            if (var7 == this.field21376) {
                BindableActionEntry var8;
                this.addToList(var8 = new BindableActionEntry(this, var10.getName(), 0, 20 + 55 * var3, this.width, 55, var10, var3++));
                var8.onPress(var2 -> {
                    var10.setBind(0);
                    this.callUIHandlers();
                });
                if (!children.isEmpty() && !children.contains(var10.getName())) {
                    var8.startHeightAnimation();
                }
            }
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        Map<Integer, BindableActionEntry> var5 = new HashMap<>();

        for (GuiComponent child : this.getChildren()) {
            if (child instanceof BindableActionEntry) {
                var5.put(((BindableActionEntry) child).entryIndex, (BindableActionEntry) child);
            }
        }

        int var9 = 75;

        for (Entry<Integer, BindableActionEntry> var11 : var5.entrySet()) {
            var11.getValue().setY(var9);
            var9 += var11.getValue().getHeight();
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = this.field21377.calcPercent();
        float var4 = EasingFunctions.easeOutBack(partialTicks, 0.0F, 1.0F, 1.0F);
        this.setScale(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
        this.setTranslateX((int) ((float) this.width * 0.2F * (1.0F - var4)) * (!this.field21378 ? 1 : -1));
        super.applyScaleTransforms();
        int var6 = ColorHelper.applyAlpha(-723724, QuadraticEasing.easeOutQuad(partialTicks, 0.0F, 1.0F, 1.0F));
        RenderUtils.drawRoundedRect(
                (float) (this.x + 10 / 2),
                (float) (this.y + 10 / 2),
                (float) (this.width - 10),
                (float) (this.height - 10),
                35.0F,
                partialTicks
        );
        RenderUtils.drawRoundedRect(
                (float) (this.x + 10 / 2),
                (float) (this.y + 10 / 2),
                (float) (this.x - 10 / 2 + this.width),
                (float) (this.y - 10 / 2 + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.25F)
        );
        RenderUtils.drawRoundedRect((float) this.x, (float) this.y, (float) this.width, (float) this.height, (float) 10, var6);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.x, (float) this.y, 0.0F);
        GL11.glRotatef(!this.field21378 ? -90.0F : 90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-this.x), (float) (-this.y), 0.0F);
        RenderUtils.drawImage(
                (float) (this.x + (!this.field21378 ? 0 : this.height)),
                (float) this.y + (float) ((this.width - 47) / 2) * (!this.field21378 ? 1.0F : -1.5F),
                18.0F,
                47.0F,
                Resources.selectPNG,
                var6
        );
        GL11.glPopMatrix();
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25,
                (float) (this.x + 25),
                (float) (this.y + 20),
                this.text + " Key",
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F * partialTicks)
        );
        RenderUtils.drawRoundedRect(
                (float) (this.x + 25),
                (float) (this.y + 68),
                (float) (this.x + this.width - 25),
                (float) (this.y + 69),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * partialTicks)
        );
        super.draw(partialTicks);
    }

    public final void addAddButtonListener(AddButtonListener listener) {
        this.addButtonListeners.add(listener);
    }

    public final void notifyAddButtonListeners() {
        for (AddButtonListener listener : this.addButtonListeners) {
            listener.onAddButtonClicked(this);
        }
    }

    public interface AddButtonListener {
        void onAddButtonClicked(KeybindsPopOver popover);
    }
}
