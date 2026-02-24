package io.github.sst.remake.gui.screen.keyboard;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.gui.framework.widget.TextField;
import io.github.sst.remake.gui.framework.widget.VerticalScrollBar;
import io.github.sst.remake.gui.framework.event.BindableActionListener;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.client.ScreenUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.EasingFunctions;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ActionSelectionPanel extends InteractiveWidget {
    public AnimationUtils field21302;
    public int field21303;
    public int field21304;
    public int field21305;
    public int field21306;
    public String field21307;
    public ScrollablePanel field21308;
    public BindableAction selectedBindableAction;
    public boolean field21311 = false;
    private final List<BindableActionListener> bindableActionListeners = new ArrayList<>();

    public ActionSelectionPanel(GuiComponent var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.field21305 = 500;
        this.field21306 = 600;
        this.field21304 = (var5 - this.field21305) / 2;
        this.field21303 = (var6 - this.field21306) / 2;
        TextField var10;
        this.addToList(
                var10 = new TextField(
                        this, "search", this.field21304 + 30, this.field21303 + 30 + 50, this.field21305 - 30 * 2, 60, TextField.DEFAULT_COLORS, "", "Search..."
                )
        );
        var10.addChangeListener(var2x -> {
            this.field21307 = var10.getText();
            this.field21308.setScrollOffset(0);
        });
        var10.requestFocus();
        this.addToList(
                this.field21308 = new ScrollablePanel(
                        this, "mods", this.field21304 + 30, this.field21303 + 30 + 120, this.field21305 - 30 * 2, this.field21306 - 30 * 2 - 120
                )
        );
        int var11 = 10;

        for (Entry var13 : ScreenUtils.screenToScreenName.entrySet()) {
            BindableAction var14 = new BindableAction((Class<? extends Screen>) var13.getKey());
            ColorHelper var15 = new ColorHelper(ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.02F), -986896)
                    .setTextColor(ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F))
                    .setWidthAlignment(FontAlignment.CENTER);
            Button var16;
            this.field21308
                    .addToList(
                            var16 = new Button(this.field21308, var14.getName(), 0, var11++ * 55, this.field21308.getWidth(), 55, var15, var14.getName())
                    );
            var16.onClick((parent, mouseButton) -> {
                for (Entry<Class<? extends Screen>, String> entry : ScreenUtils.screenToScreenName.entrySet()) {
                    BindableAction action = new BindableAction(entry.getKey());
                    if (action.getName().equals(var16.getName()) && !this.field21311) {
                        this.selectedBindableAction = action;
                        this.field21311 = true;
                        break;
                    }
                }
            });
        }

        var11 += 50;

        for (Module mod : Client.INSTANCE.moduleManager.modules) {
            ColorHelper var20 = new ColorHelper(16777215, -986896).setTextColor(ClientColors.DEEP_TEAL.getColor()).setWidthAlignment(FontAlignment.LEFT);
            Button var21;
            this.field21308
                    .addToList(
                            var21 = new Button(
                                    this.field21308, mod.getName(), 0, var11++ * 40, this.field21308.getWidth(), 40, var20, new BindableAction(mod).getName()
                            )
                    );
            var21.setTextOffsetX(10);
            var21.onClick((parent, mouseButton) -> {
                for (Module mod2 : Client.INSTANCE.moduleManager.modules) {
                    if (mod2.getName().equals(var21.getText()) && !this.field21311) {
                        this.selectedBindableAction = new BindableAction(mod2);
                        this.field21311 = true;
                        break;
                    }
                }
            });
        }

        this.field21302 = new AnimationUtils(200, 120);
        this.setListening(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.isMouseDownOverComponent()
                && (mouseX < this.field21304 || mouseY < this.field21303 || mouseX > this.field21304 + this.field21305 || mouseY > this.field21303 + this.field21306)) {
            this.field21311 = true;
        }

        this.field21302.changeDirection(this.field21311 ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        Map<String, Button> var5 = new TreeMap();
        Map<String, Button> var6 = new TreeMap();
        Map<String, Button> var7 = new TreeMap();
        List<Button> var8 = new ArrayList();

        for (GuiComponent var10 : this.field21308.getChildren()) {
            if (!(var10 instanceof VerticalScrollBar)) {
                for (GuiComponent var12 : var10.getChildren()) {
                    if (var12 instanceof Button) {
                        Button var13 = (Button) var12;
                        boolean var14 = var13.getHeight() != 40;
                        if (!var14 || this.field21307 != null && (this.field21307 == null || this.field21307.length() != 0)) {
                            if (!var14 && this.method13622(this.field21307, var13.getText())) {
                                var6.put(var13.getText(), var13);
                            } else if (!var14 && this.method13621(this.field21307, var13.getText())) {
                                var7.put(var13.getText(), var13);
                            } else {
                                var8.add(var13);
                            }
                        } else {
                            var5.put(var13.getText(), var13);
                        }
                    }
                }
            }
        }

        int var15 = var5.size() <= 0 ? 0 : 10;

        for (Button var20 : var5.values()) {
            var20.setSelfVisible(true);
            var20.setY(var15);
            var15 += var20.getHeight();
        }

        if (var5.size() > 0) {
            var15 += 10;
        }

        for (Button var21 : var6.values()) {
            var21.setSelfVisible(true);
            var21.setY(var15);
            var15 += var21.getHeight();
        }

        for (Button var22 : var7.values()) {
            var22.setSelfVisible(true);
            var22.setY(var15);
            var15 += var22.getHeight();
        }

        for (Button var23 : var8) {
            var23.setSelfVisible(false);
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    private boolean method13621(String var1, String var2) {
        return var1 == null || var1 == "" || var2 == null || var2.toLowerCase().contains(var1.toLowerCase());
    }

    private boolean method13622(String var1, String var2) {
        return var1 == null || var1 == "" || var2 == null || var2.toLowerCase().startsWith(var1.toLowerCase());
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = this.field21302.calcPercent();
        float var4 = EasingFunctions.easeOutBack(partialTicks, 0.0F, 1.0F, 1.0F);
        if (this.field21311) {
            var4 = QuadraticEasing.easeOutQuad(partialTicks, 0.0F, 1.0F, 1.0F);
        }

        this.setScale(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
        if (partialTicks == 0.0F && this.field21311) {
            this.notifyBindableActionSelected(this.selectedBindableAction);
        }

        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F * partialTicks)
        );
        super.applyScaleTransforms();
        RenderUtils.drawRoundedRect(
                (float) this.field21304,
                (float) this.field21303,
                (float) this.field21305,
                (float) this.field21306,
                10.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_36,
                (float) (30 + this.field21304),
                (float) (30 + this.field21303),
                "Select mod to bind",
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.7F)
        );
        super.draw(partialTicks);
    }

    public final void addBindableActionSelectedListener(BindableActionListener listener) {
        this.bindableActionListeners.add(listener);
    }

    public final void notifyBindableActionSelected(BindableAction action) {
        for (BindableActionListener listener : this.bindableActionListeners) {
            listener.onBindableActionSelected(this, action);
        }
    }
}
