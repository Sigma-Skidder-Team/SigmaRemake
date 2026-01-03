package io.github.sst.remake.gui.element.impl.cgui;

import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.element.InteractiveWidget;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;

public class SettingGroup extends InteractiveWidget {
    public AnimationUtils animation1;
    public AnimationUtils animation;
    public int settingY;
    public int settingX;
    public int settingWidth;
    public int settingHeight;
    public SettingPanel field20668;
    public final Module module;
    public boolean field20671 = false;

    public SettingGroup(GuiComponent var1, String var2, int x, int y, int width, int height, Module var7) {
        super(var1, var2, x, y, width, height, false);
        this.settingWidth = 500;
        this.settingHeight = (int) Math.min(600.0F, (float) height * 0.7F);
        this.settingX = (width - this.settingWidth) / 2;
        this.settingY = (height - this.settingHeight) / 2 + 20;
        this.module = var7;
        int var10 = 10;
        int var11 = 59;
        this.addToList(
                this.field20668 = new SettingPanel(
                        this, "mods", this.settingX + var10, this.settingY + var11, this.settingWidth - var10 * 2, this.settingHeight - var11 - var10, var7
                )
        );
        this.animation1 = new AnimationUtils(200, 120);
        this.animation = new AnimationUtils(240, 200);
        this.setListening(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.isMouseDownOverComponent()
                && (mouseX < this.settingX || mouseY < this.settingY || mouseX > this.settingX + this.settingWidth || mouseY > this.settingY + this.settingHeight)) {
            this.field20671 = true;
        }

        this.animation1.changeDirection(this.field20671 ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        this.animation.changeDirection(this.field20671 ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = this.animation1.calcPercent();
        float var4 = EasingFunctions.easeOutBack(partialTicks, 0.0F, 1.0F, 1.0F);
        if (this.field20671) {
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
                (float) this.settingX,
                (float) this.settingY,
                (float) this.settingWidth,
                (float) this.settingHeight,
                10.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_40,
                (float) this.settingX,
                (float) (this.settingY - 60),
                this.module.getName(),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        String description = this.module.getDescription();
        int maxWidth = this.settingWidth - 60;

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
                (float) (30 + this.settingX),
                (float) (30 + this.settingY),
                description,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.7F)
        );

        super.draw(partialTicks);
    }
}
