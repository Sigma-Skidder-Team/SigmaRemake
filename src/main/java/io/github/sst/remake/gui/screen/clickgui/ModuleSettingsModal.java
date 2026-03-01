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

public class ModuleSettingsModal extends InteractiveWidget {
    public AnimationUtils scaleAnimation;
    public AnimationUtils fadeAnimation;
    public int dialogY;
    public int dialogX;
    public int dialogWidth;
    public int dialogHeight;
    public ModuleSettingsList settingsList;
    public final Module module;
    public boolean closing = false;

    public ModuleSettingsModal(GuiComponent parent, String id, int x, int y, int width, int height, Module module) {
        super(parent, id, x, y, width, height, false);

        this.dialogWidth = 500;
        this.dialogHeight = (int) Math.min(600.0F, height * 0.7F);
        this.dialogX = (width - this.dialogWidth) / 2;
        this.dialogY = (height - this.dialogHeight) / 2 + 20;

        this.module = module;

        int padding = 10;
        int headerHeight = 59;

        this.settingsList = new ModuleSettingsList(
                this,
                "mods",
                this.dialogX + padding,
                this.dialogY + headerHeight,
                this.dialogWidth - padding * 2,
                this.dialogHeight - headerHeight - padding,
                module
        );
        this.addToList(this.settingsList);

        this.scaleAnimation = new AnimationUtils(200, 120);
        this.fadeAnimation = new AnimationUtils(240, 200);

        this.setListening(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.isMouseDownOverComponent() && isOutsideDialog(mouseX, mouseY)) {
            this.closing = true;
        }

        AnimationUtils.Direction direction = this.closing
                ? AnimationUtils.Direction.FORWARDS
                : AnimationUtils.Direction.BACKWARDS;

        this.scaleAnimation.changeDirection(direction);
        this.fadeAnimation.changeDirection(direction);

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        float animationProgress = this.scaleAnimation.calcPercent();

        float easedScale = this.closing
                ? QuadraticEasing.easeOutQuad(animationProgress, 0.0F, 1.0F, 1.0F)
                : EasingFunctions.easeOutBack(animationProgress, 0.0F, 1.0F, 1.0F);

        this.setScale(0.8F + easedScale * 0.2F, 0.8F + easedScale * 0.2F);

        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.45F * animationProgress)
        );

        super.applyScaleTransforms();

        RenderUtils.drawRoundedRect(
                (float) this.dialogX,
                (float) this.dialogY,
                (float) this.dialogWidth,
                (float) this.dialogHeight,
                10.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), animationProgress)
        );

        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_40,
                (float) this.dialogX,
                (float) (this.dialogY - 60),
                this.module.getName(),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), animationProgress)
        );

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                (float) (30 + this.dialogX),
                (float) (30 + this.dialogY),
                fitWithEllipsis(this.module.getDescription(), this.dialogWidth - 60),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), animationProgress * 0.7F)
        );

        super.draw(animationProgress);
    }

    private String fitWithEllipsis(String text, int maxWidth) {
        if (text == null) {
            return "";
        }

        if (FontUtils.HELVETICA_LIGHT_20.getWidth(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int ellipsisWidth = FontUtils.HELVETICA_LIGHT_20.getWidth(ellipsis);

        StringBuilder trimmed = new StringBuilder();
        int currentWidth = 0;

        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            int charWidth = FontUtils.HELVETICA_LIGHT_20.getWidth(ch);

            if (currentWidth + charWidth + ellipsisWidth > maxWidth) {
                break;
            }

            trimmed.append(ch);
            currentWidth += charWidth;
        }

        return trimmed.append(ellipsis).toString();
    }

    private boolean isOutsideDialog(int mouseX, int mouseY) {
        return mouseX < this.dialogX
                || mouseY < this.dialogY
                || mouseX > this.dialogX + this.dialogWidth
                || mouseY > this.dialogY + this.dialogHeight;
    }
}