package io.github.sst.remake.gui.screen.clickgui;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.EasingFunctions;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;

public class ModuleSettingsDialog extends InteractiveWidget {
    public AnimationUtils openScaleAnimation;
    public AnimationUtils fadeAnimation;
    public int panelY;
    public int panelX;
    public int panelWidth;
    public int panelHeight;
    public ModuleSettingsList settingsList;
    public final Module module;
    public boolean closing = false;

    public ModuleSettingsDialog(GuiComponent var1, String var2, int x, int y, int width, int height, Module var7) {
        super(var1, var2, x, y, width, height, false);
        this.panelWidth = 500;
        this.panelHeight = (int) Math.min(600.0F, (float) height * 0.7F);
        this.panelX = (width - this.panelWidth) / 2;
        this.panelY = (height - this.panelHeight) / 2 + 20;
        this.module = var7;
        int var10 = 10;
        int var11 = 59;
        this.addToList(
                this.settingsList = new ModuleSettingsList(
                        this, "mods", this.panelX + var10, this.panelY + var11, this.panelWidth - var10 * 2, this.panelHeight - var11 - var10, var7
                )
        );
        this.openScaleAnimation = new AnimationUtils(200, 120);
        this.fadeAnimation = new AnimationUtils(240, 200);
        this.setListening(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.isMouseDownOverComponent()
                && (mouseX < this.panelX || mouseY < this.panelY || mouseX > this.panelX + this.panelWidth || mouseY > this.panelY + this.panelHeight)) {
            this.closing = true;
        }

        this.openScaleAnimation.changeDirection(this.closing ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        this.fadeAnimation.changeDirection(this.closing ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = this.openScaleAnimation.calcPercent();
        float var4 = EasingFunctions.easeOutBack(partialTicks, 0.0F, 1.0F, 1.0F);
        if (this.closing) {
            var4 = QuadraticEasing.easeOutQuad(partialTicks, 0.0F, 1.0F, 1.0F);
        }

        this.setScale(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.45F * partialTicks)
        );
        super.applyScaleTransforms();
        RenderUtils.drawRoundedRect(
                (float) this.panelX,
                (float) this.panelY,
                (float) this.panelWidth,
                (float) this.panelHeight,
                10.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_40,
                (float) this.panelX,
                (float) (this.panelY - 60),
                this.module.getName(),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        String description = this.module.getDescription();
        int maxWidth = this.panelWidth - 60;

        if (FontUtils.HELVETICA_LIGHT_20.getWidth(description) > maxWidth) {
            String ellipsis = "...";
            int ellipsisWidth = FontUtils.HELVETICA_LIGHT_20.getWidth(ellipsis);
            StringBuilder trimmed = new StringBuilder();
            int currentWidth = 0;
            for (char c : description.toCharArray()) {
                int charWidth = FontUtils.HELVETICA_LIGHT_20.getWidth(String.valueOf(c));
                if (currentWidth + charWidth + ellipsisWidth > maxWidth) {
                    break;
                }
                trimmed.append(c);
                currentWidth += charWidth;
            }
            description = trimmed + ellipsis;
        }

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                (float) (30 + this.panelX),
                (float) (30 + this.panelY),
                description,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.7F)
        );

        super.draw(partialTicks);
    }
}
