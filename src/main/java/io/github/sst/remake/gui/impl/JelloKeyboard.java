package io.github.sst.remake.gui.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.element.impl.keyboard.BindableAction;
import io.github.sst.remake.gui.element.impl.keyboard.Keyboard;
import io.github.sst.remake.gui.element.impl.keyboard.ModsPanel;
import io.github.sst.remake.gui.element.impl.keyboard.PopOver;
import io.github.sst.remake.gui.element.impl.keyboard.runnables.*;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ScreenUtils;
import io.github.sst.remake.util.client.BindUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ShaderUtils;
import io.github.sst.remake.util.render.font.FontUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

public class JelloKeyboard extends Screen implements IMinecraft {
    public Date field20955;
    public PopOver field20956;
    public Keyboard field20957;
    public ModsPanel field20960;
    public int field20961;

    public JelloKeyboard() {
        super("KeybindManager");
        this.field20955 = new Date();
        this.addToList(this.field20957 = new Keyboard(this, "keyboard", (this.width - 1060) / 2, (this.height - 357) / 2));
        this.field20957.setScale(0.4F, 0.4F);
        this.field20957
                .onPress(
                        var2 -> {
                            boolean popOver = false;

                            for (CustomGuiScreen child : this.getChildren()) {
                                if (child instanceof PopOver) {
                                    popOver = true;
                                    break;
                                }
                            }

                            if (this.field20957.field20696 == this.field20961 && popOver) {
                                this.method13333();
                            } else {
                                int[] var8 = this.field20957.method13105(this.field20957.field20696);
                                String bind = BindUtils.getKeyName(this.field20957.field20696);
                                this.field20956 = new PopOver(
                                        this, "popover", this.field20957.getX() + var8[0], this.field20957.getY() + var8[1], this.field20957.field20696, bind
                                );
                                this.field20956.onPress(var1x -> this.method13329(this.field20957));
                                this.field20956.addAddButtonListener(pop -> {
                                    pop.setReAddChildren(false);
                                    this.method13331();
                                });
                                this.field20961 = this.field20957.field20696;
                            }
                        }
                );
        ShaderUtils.applyBlurShader();
    }

    public static List<BindableAction> getBindableActions() {
        List<BindableAction> var2 = new ArrayList<>();

        for (Module var4 : Client.INSTANCE.moduleManager.modules) {
            var2.add(new BindableAction(var4));
        }

        for (Entry var6 : ScreenUtils.screenToScreenName.entrySet()) {
            var2.add(new BindableAction((Class<? extends net.minecraft.client.gui.screen.Screen>) var6.getKey()));
        }

        return var2;
    }

    private void method13329(Keyboard var1) {
        this.addRunnable(var1::method13104);
    }

    private void method13330() {
        this.addRunnable(() -> {
            for (CustomGuiScreen child : this.getChildren()) {
                if (child instanceof PopOver) {
                    PopOver pop = (PopOver) child;
                    pop.method13712();
                }
            }
        });
    }

    private void method13331() {
        this.addRunnable(() -> {
            this.addToList(this.field20960 = new ModsPanel(this, "mods", 0, 0, width, height));
            this.field20960.addBindableActionSelectedListener((panel, action) -> {
                if (action != null) {
                    action.setBind(this.field20957.field20696);
                }

                this.method13332();
            });
            this.field20960.setReAddChildren(true);
        });
    }

    public void method13332() {
        this.addRunnable(() -> {
            for (CustomGuiScreen child : this.getChildren()) {
                if (child instanceof PopOver) {
                    PopOver pop = (PopOver) child;
                    pop.method13712();
                    this.field20957.method13104();
                    pop.setReAddChildren(true);
                    pop.requestFocus();
                    this.queueChildRemoval(this.field20960);
                }
            }
        });
    }

    private void method13333() {
        this.addRunnable(() -> {
            this.field20957.requestFocus();
            this.clearChildren();
            this.field20961 = 0;
        });
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.isMouseDownOverComponent()) {
            this.field20957.requestFocus();
            this.clearChildren();
            this.field20961 = 0;
            this.field20956 = null;
        }

        if (this.field20956 != null) {
            this.field20957.requestFocus();
            this.clearChildren();
            this.addToList(this.field20956);
            this.field20956 = null;
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
        if (keyCode == 256) {
            ShaderUtils.resetShader();
            client.openScreen(null);
        }
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = (float) Math.min(200L, new Date().getTime() - this.field20955.getTime()) / 200.0F;
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
        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_40,
                (float) ((this.width - 1060) / 2),
                (float) ((this.height - 357) / 2 - 90),
                "Keybind Manager",
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );
        super.draw(partialTicks);
    }
}
