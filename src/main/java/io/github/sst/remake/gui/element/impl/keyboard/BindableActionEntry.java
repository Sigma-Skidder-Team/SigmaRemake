package io.github.sst.remake.gui.element.impl.keyboard;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Widget;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;

import java.util.Date;

public class BindableActionEntry extends Widget {
    public BindableAction bindableAction;
    public Date animationStartDate;
    public int entryIndex;
    public Date anotherAnimationDate;
    public DeleteButton deleteButton;

    public BindableActionEntry(CustomGuiScreen parent, String name, int x, int y, int width, int height, BindableAction bindableAction, int entryIndex) {
        super(parent, name, x, y, width, height, false);
        this.addToList(this.deleteButton = new DeleteButton(this, "delete", 200, 20, 20, 20));
        this.deleteButton.onClick((element, button) -> {
            this.animationStartDate = new Date();
            this.callUIHandlers();
        });
        this.bindableAction = bindableAction;
        this.entryIndex = entryIndex;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
    }

    public void startHeightAnimation() {
        this.setHeight(0);
        this.anotherAnimationDate = new Date();
    }

    @Override
    public void draw(float partialTicks) {
        if (this.anotherAnimationDate != null) {
            float progress = AnimationUtils.calculateProgress(this.anotherAnimationDate, 150.0F);
            progress = QuadraticEasing.easeOutQuad(progress, 0.0F, 1.0F, 1.0F);
            this.setHeight((int) (55.0F * progress));
            if (progress == 1.0F) {
                this.anotherAnimationDate = null;
            }
        }

        if (this.animationStartDate != null) {
            float progress = AnimationUtils.calculateProgress(this.animationStartDate, 180.0F);
            progress = QuadraticEasing.easeOutQuad(progress, 0.0F, 1.0F, 1.0F);
            this.setHeight((int) (55.0F * (1.0F - progress)));
            if (progress == 1.0F) {
                this.animationStartDate = null;
            }
        }

        ScissorUtils.startScissor(this.x, this.y, this.x + this.width, this.y + this.height, true);
        RenderUtils.drawString(
                FontUtils.REGULAR_20,
                (float) (this.x + 25),
                (float) this.y + (float) this.height / 2.0F - 17.5F,
                this.bindableAction.getName(),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.6F * partialTicks)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_12,
                (float) (this.x + 25),
                (float) this.y + (float) this.height / 2.0F + 7.5F,
                this.bindableAction.getType(),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.6F * partialTicks)
        );
        this.deleteButton.setY((int) ((float) this.height / 2.0F - 7.5F));
        super.draw(partialTicks);
        ScissorUtils.restoreScissor();
    }
}
