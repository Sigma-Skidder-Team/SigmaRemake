package io.github.sst.remake.gui.screen.keyboard;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import org.newdawn.slick.opengl.font.TrueTypeFont;

public class Child extends InteractiveWidget {
    public final int field20690;
    private float field20691;
    private boolean field20692 = false;
    private boolean field20693 = false;

    public Child(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, String var7, int var8) {
        super(var1, var2, var3, var4, var5, var6, ColorHelper.DEFAULT_COLOR, var7, false);
        this.field20690 = var8;
        this.method13102();
    }

    public void method13102() {
        for (BindableAction var4 : KeyboardScreen.getBindableActions()) {
            int var5 = var4.getBind();
            if (var5 == this.field20690) {
                this.field20693 = true;
                return;
            }
        }

        this.field20693 = false;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        this.field20691 = Math.max(0.0F, Math.min(1.0F, this.field20691 + 0.2F * (float) (!this.isMouseDownOverComponent() && !this.field20692 ? -1 : 1)));
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawRoundedButton(
                (float) this.x,
                (float) (this.y + 5),
                (float) this.width,
                (float) this.height,
                8.0F,
                ColorHelper.shiftTowardsOther(-3092272, -2171170, this.field20691)
        );
        RenderUtils.drawRoundedButton(
                (float) this.x, (float) this.y + 3.0F * this.field20691, (float) this.width, (float) this.height, 8.0F, -986896
        );
        TrueTypeFont var4 = FontUtils.HELVETICA_LIGHT_20;
        if (this.text.contains("Lock")) {
            RenderUtils.drawCircle(
                    (float) (this.x + 14),
                    (float) (this.y + 11) + 3.0F * this.field20691,
                    10.0F,
                    ColorHelper.applyAlpha(ClientColors.DARK_SLATE_GREY.getColor(), this.field20691)
            );
        }

        if (!this.text.equals("Return")) {
            if (!this.text.equals("Back")) {
                if (!this.text.equals("Meta")) {
                    if (!this.text.equals("Menu")) {
                        if (!this.text.equals("Space")) {
                            if (this.field20693) {
                                var4 = FontUtils.REGULAR_20;
                            }

                            RenderUtils.drawString(
                                    var4,
                                    (float) (this.x + (this.width - var4.getWidth(this.text)) / 2),
                                    (float) (this.y + 19) + 3.0F * this.field20691,
                                    this.text,
                                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.4F + (!this.field20693 ? 0.0F : 0.2F))
                            );
                        }
                    } else {
                        int var5 = this.x + 25;
                        int var6 = this.y + 25 + (int) (3.0F * this.field20691);
                        RenderUtils.drawRoundedRect3(
                                (float) var5,
                                (float) var6,
                                (float) (var5 + 14),
                                (float) (var6 + 3),
                                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + (!this.field20693 ? 0.0F : 0.2F))
                        );
                        RenderUtils.drawRoundedRect(
                                (float) var5,
                                (float) (var6 + 4),
                                (float) (var5 + 14),
                                (float) (var6 + 7),
                                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + (!this.field20693 ? 0.0F : 0.2F))
                        );
                        RenderUtils.drawRoundedRect3(
                                (float) var5,
                                (float) (var6 + 8),
                                (float) (var5 + 14),
                                (float) (var6 + 11),
                                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + (!this.field20693 ? 0.0F : 0.2F))
                        );
                        RenderUtils.drawRoundedRect3(
                                (float) var5,
                                (float) (var6 + 12),
                                (float) (var5 + 14),
                                (float) (var6 + 15),
                                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + (!this.field20693 ? 0.0F : 0.2F))
                        );
                    }
                } else {
                    int var7 = this.x + 32;
                    int var10 = this.y + 32 + (int) (3.0F * this.field20691);
                    RenderUtils.drawCircle(
                            (float) var7, (float) var10, 14.0F, ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + (!this.field20693 ? 0.0F : 0.2F))
                    );
                }
            } else {
                int var8 = this.x + 43;
                int var11 = this.y + 33 + (int) (3.0F * this.field20691);
                RenderUtils.drawTriangle(
                        (float) var8,
                        (float) var11,
                        (float) (var8 + 6),
                        (float) (var11 - 3),
                        (float) (var8 + 6),
                        (float) (var11 + 3),
                        ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + (!this.field20693 ? 0.0F : 0.2F))
                );
                RenderUtils.drawRoundedRect(
                        (float) (var8 + 6),
                        (float) (var11 - 1),
                        (float) (var8 + 27),
                        (float) (var11 + 1),
                        ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + (!this.field20693 ? 0.0F : 0.2F))
                );
            }
        } else {
            int var9 = this.x + 50;
            int var12 = this.y + 33 + (int) (3.0F * this.field20691);
            RenderUtils.drawTriangle(
                    (float) var9,
                    (float) var12,
                    (float) (var9 + 6),
                    (float) (var12 - 3),
                    (float) (var9 + 6),
                    (float) (var12 + 3),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + (!this.field20693 ? 0.0F : 0.2F))
            );
            RenderUtils.drawRoundedRect(
                    (float) (var9 + 6),
                    (float) (var12 - 1),
                    (float) (var9 + 27),
                    (float) (var12 + 1),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + (!this.field20693 ? 0.0F : 0.2F))
            );
            RenderUtils.drawRoundedRect(
                    (float) (var9 + 25),
                    (float) (var12 - 8),
                    (float) (var9 + 27),
                    (float) (var12 - 1),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F + (!this.field20693 ? 0.0F : 0.2F))
            );
        }

        super.draw(partialTicks);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == this.field20690) {
            this.field20692 = true;
        }

        super.keyPressed(keyCode);
    }

    @Override
    public void modifierPressed(int var1) {
        if (var1 == this.field20690) {
            this.field20692 = false;
        }

        super.modifierPressed(var1);
    }
}
