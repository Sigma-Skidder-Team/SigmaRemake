package io.github.sst.remake.gui.screen.keyboard;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.widget.TextButton;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.EasingFunctions;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class KeybindsPopOver extends InteractiveWidget {
    private final int selectedKeyCode;
    private final AnimationUtils openAnimation;
    private boolean openToLeft = false;
    private final List<AddButtonListener> addButtonListeners = new ArrayList<>();

    public KeybindsPopOver(GuiComponent parent, String id, int mouseX, int mouseY, int keyCode, String keyName) {
        super(parent, id, mouseX - 125, mouseY, 250, 330, ColorHelper.DEFAULT_COLOR, keyName, false);

        if (this.y + this.height <= MinecraftClient.getInstance().getWindow().getHeight()) {
            this.y += 10;
        } else {
            this.y -= 400;
            this.openToLeft = true;
        }

        this.selectedKeyCode = keyCode;
        this.openAnimation = new AnimationUtils(250, 0);

        this.setReAddChildren(true);
        this.setListening(false);

        this.refreshEntries();

        TextButton addButton = new TextButton(
                this,
                "addButton",
                this.width - 70,
                this.height - 70,
                FontUtils.HELVETICA_LIGHT_25.getWidth("Add"),
                70,
                ColorHelper.DEFAULT_COLOR,
                "Add",
                FontUtils.HELVETICA_LIGHT_25
        );
        this.addToList(addButton);
        addButton.onClick((clicked, mouseButton) -> this.notifyAddButtonListeners());
    }

    public void refreshEntries() {
        int index = 1;

        List<String> existingNames = new ArrayList<>();
        for (GuiComponent child : this.getChildren()) {
            if (child.getHeight() != 0) {
                existingNames.add(child.getName());
            }
        }

        this.requestFocus();
        this.setFocused(true);
        this.clearChildren();

        for (BindableAction action : KeyboardScreen.getBindableActions()) {
            if (action.getBind() != this.selectedKeyCode) {
                continue;
            }

            BindableActionEntry entry = new BindableActionEntry(
                    this,
                    action.getName(),
                    0,
                    20 + 55 * index,
                    this.width,
                    55,
                    action,
                    index
            );
            this.addToList(entry);

            int currentIndex = index;
            index++;

            entry.onPress(widget -> {
                action.setBind(0);
                this.firePressHandlers();
            });

            if (!existingNames.isEmpty() && !existingNames.contains(action.getName())) {
                entry.startHeightAnimation();
            }
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        Map<Integer, BindableActionEntry> entriesByIndex = new TreeMap<>();

        for (GuiComponent child : this.getChildren()) {
            if (child instanceof BindableActionEntry) {
                BindableActionEntry entry = (BindableActionEntry) child;
                entriesByIndex.put(entry.entryIndex, entry);
            }
        }

        int y = 75;
        for (BindableActionEntry entry : entriesByIndex.values()) {
            entry.setY(y);
            y += entry.getHeight();
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        float anim = this.openAnimation.calcPercent();

        float eased = EasingFunctions.easeOutBack(anim, 0.0F, 1.0F, 1.0F);
        this.setScale(0.8F + eased * 0.2F, 0.8F + eased * 0.2F);

        int slideDirection = this.openToLeft ? -1 : 1;
        this.setTranslateX((int) ((float) this.width * 0.2F * (1.0F - eased)) * slideDirection);

        super.applyScaleTransforms();

        int bgColor = ColorHelper.applyAlpha(
                -723724,
                QuadraticEasing.easeOutQuad(anim, 0.0F, 1.0F, 1.0F)
        );

        RenderUtils.drawRoundedRect(
                (float) (this.x + 10 / 2),
                (float) (this.y + 10 / 2),
                (float) (this.width - 10),
                (float) (this.height - 10),
                35.0F,
                anim
        );

        RenderUtils.drawRoundedRect(
                (float) (this.x + 10 / 2),
                (float) (this.y + 10 / 2),
                (float) (this.x - 10 / 2 + this.width),
                (float) (this.y - 10 / 2 + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), anim * 0.25F)
        );

        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                10.0F,
                bgColor
        );

        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.x, (float) this.y, 0.0F);
        GL11.glRotatef(!this.openToLeft ? -90.0F : 90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-this.x), (float) (-this.y), 0.0F);

        RenderUtils.drawImage(
                (float) (this.x + (!this.openToLeft ? 0 : this.height)),
                (float) this.y + (float) ((this.width - 47) / 2) * (!this.openToLeft ? 1.0F : -1.5F),
                18.0F,
                47.0F,
                Resources.SELECTED_ICON,
                bgColor
        );
        GL11.glPopMatrix();

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25,
                (float) (this.x + 25),
                (float) (this.y + 20),
                this.text + " Key",
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F * anim)
        );

        RenderUtils.drawRoundedRect(
                (float) (this.x + 25),
                (float) (this.y + 68),
                (float) (this.x + this.width - 25),
                (float) (this.y + 69),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * anim)
        );

        super.draw(anim);
    }

    public void addAddButtonListener(AddButtonListener listener) {
        this.addButtonListeners.add(listener);
    }

    public void notifyAddButtonListeners() {
        for (AddButtonListener listener : this.addButtonListeners) {
            listener.onAddButtonClicked(this);
        }
    }

    public interface AddButtonListener {
        void onAddButtonClicked(KeybindsPopOver popover);
    }
}