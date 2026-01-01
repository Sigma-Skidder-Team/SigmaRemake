package io.github.sst.remake.gui.element.impl.cgui;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;

public class SettingGroup extends Element {
    public AnimationUtils animation1;
    public AnimationUtils animation;
    public int settingY;
    public int settingX;
    public int settingWidth;
    public int settingHeight;
    public SettingPanel field20668;
    public final Module module;
    public boolean field20671 = false;

    public SettingGroup(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Module var7) {
        super(var1, var2, var3, var4, var5, var6, false);
        this.settingWidth = 500;
        this.settingHeight = (int) Math.min(600.0F, (float) var6 * 0.7F);
        this.settingX = (var5 - this.settingWidth) / 2;
        this.settingY = (var6 - this.settingHeight) / 2 + 20;
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

        this.animation1.changeDirection(this.field20671 ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
        this.animation.changeDirection(this.field20671 ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
        super.updatePanelDimensions(mouseX, mouseY);
    }

    private boolean method13084(String var1, String var2) {
        return var1 == null || var1 == "" || var2 == null || var2.toLowerCase().contains(var1.toLowerCase());
    }

    private boolean method13085(String var1, String var2) {
        return var1 == null || var1 == "" || var2 == null || var2.toLowerCase().startsWith(var1.toLowerCase());
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
        ScissorUtils.startScissor((float) this.settingX, (float) this.settingY, (float) (this.settingWidth - 30), (float) this.settingHeight);
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                (float) (30 + this.settingX),
                (float) (30 + this.settingY),
                this.module.getDescription(),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.7F)
        );
        ScissorUtils.restoreScissor();
        super.draw(partialTicks);
    }
}
