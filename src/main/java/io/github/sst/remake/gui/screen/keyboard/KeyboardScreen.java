package io.github.sst.remake.gui.screen.keyboard;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ScreenUtils;
import io.github.sst.remake.util.client.BindUtils;
import io.github.sst.remake.util.math.anim.ease.EasingFunctions;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.shader.ShaderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

public class KeyboardScreen extends Screen implements IMinecraft {
    public Date openTime;
    public KeybindsPopOver pendingPopover;
    public Keyboard keyboard;
    public ActionSelectionPanel actionSelectionPanel;
    public int lastPopoverKeyCode;

    public KeyboardScreen() {
        super("KeybindManager");

        this.openTime = new Date();

        this.keyboard = new Keyboard(this, "keyboard", (this.width - 1060) / 2, (this.height - 357) / 2);
        this.addToList(this.keyboard);
        this.keyboard.setScale(0.4F, 0.4F);

        this.keyboard.onPress(widget -> {
            boolean popoverOpen = hasOpenPopover();

            if (this.keyboard.selectedKeyCode == this.lastPopoverKeyCode && popoverOpen) {
                closePopoverAndOverlays();
                return;
            }

            int[] anchor = this.keyboard.getKeyAnchorPosition(this.keyboard.selectedKeyCode);
            String keyName = BindUtils.getKeyName(this.keyboard.selectedKeyCode);

            KeybindsPopOver popover = new KeybindsPopOver(
                    this,
                    "popover",
                    this.keyboard.getX() + anchor[0],
                    this.keyboard.getY() + anchor[1],
                    this.keyboard.selectedKeyCode,
                    keyName
            );

            popover.onPress(ignored -> scheduleKeyboardReset(this.keyboard));
            popover.addAddButtonListener(pop -> {
                pop.setReAddChildren(false);
                openActionSelectionPanel();
            });

            this.pendingPopover = popover;
            this.lastPopoverKeyCode = this.keyboard.selectedKeyCode;
        });

        ShaderUtils.applyBlurShader();
    }

    public static List<BindableAction> getBindableActions() {
        List<BindableAction> actions = new ArrayList<>();

        for (Module module : Client.INSTANCE.moduleManager.modules) {
            actions.add(new BindableAction(module));
        }

        for (Entry<Class<? extends net.minecraft.client.gui.screen.Screen>, String> entry : ScreenUtils.screenToScreenName.entrySet()) {
            actions.add(new BindableAction(entry.getKey()));
        }

        return actions;
    }

    private boolean hasOpenPopover() {
        for (GuiComponent child : this.getChildren()) {
            if (child instanceof KeybindsPopOver) {
                return true;
            }
        }
        return false;
    }

    private void scheduleKeyboardReset(Keyboard keyboard) {
        this.addRunnable(keyboard::resetKeyButtonStates);
    }

    private void openActionSelectionPanel() {
        this.addRunnable(() -> {
            this.actionSelectionPanel = new ActionSelectionPanel(this, "mods", 0, 0, width, height);
            this.addToList(this.actionSelectionPanel);

            this.actionSelectionPanel.addBindableActionSelectedListener((panel, action) -> {
                if (action != null) {
                    action.setBind(this.keyboard.selectedKeyCode);
                }
                refreshPopoverAndCloseSelection();
            });

            this.actionSelectionPanel.setReAddChildren(true);
        });
    }

    public void refreshPopoverAndCloseSelection() {
        this.addRunnable(() -> {
            for (GuiComponent child : this.getChildren()) {
                if (!(child instanceof KeybindsPopOver)) {
                    continue;
                }

                KeybindsPopOver popover = (KeybindsPopOver) child;
                popover.refreshEntries();

                this.keyboard.resetKeyButtonStates();

                popover.setReAddChildren(true);
                popover.requestFocus();

                this.queueChildRemoval(this.actionSelectionPanel);
            }
        });
    }

    private void closePopoverAndOverlays() {
        this.addRunnable(() -> {
            this.keyboard.requestFocus();
            this.clearChildren();
            this.lastPopoverKeyCode = 0;
        });
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.isMouseDownOverComponent()) {
            this.keyboard.requestFocus();
            this.clearChildren();
            this.lastPopoverKeyCode = 0;
            this.pendingPopover = null;
        }

        if (this.pendingPopover != null) {
            this.keyboard.requestFocus();
            this.clearChildren();
            this.addToList(this.pendingPopover);
            this.pendingPopover = null;
        }

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

        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_40,
                (float) ((this.width - 1060) / 2),
                (float) ((this.height - 357) / 2 - 90),
                "Keybind Manager",
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );

        super.draw(introProgress);
    }
}