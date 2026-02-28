package io.github.sst.remake.gui.screen.keyboard;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import org.newdawn.slick.opengl.font.TrueTypeFont;

public class KeyButton extends InteractiveWidget {
    public final int keyCode;
    private float hoverProgress;
    private boolean isKeyDown = false;
    private boolean isBound = false;

    public KeyButton(GuiComponent parent,
                     String id,
                     int x,
                     int y,
                     int width,
                     int height,
                     String label,
                     int keyCode) {
        super(parent, id, x, y, width, height, ColorHelper.DEFAULT_COLOR, label, false);
        this.keyCode = keyCode;
        this.refreshBoundState();
    }

    public void refreshBoundState() {
        for (BindableAction action : KeyboardScreen.getBindableActions()) {
            if (action.getBind() == this.keyCode) {
                this.isBound = true;
                return;
            }
        }
        this.isBound = false;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        float direction = (!this.isMouseDownOverComponent() && !this.isKeyDown) ? -1.0F : 1.0F;
        this.hoverProgress = Math.max(0.0F, Math.min(1.0F, this.hoverProgress + 0.2F * direction));
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawRoundedButton(
                (float) this.x,
                (float) (this.y + 5),
                (float) this.width,
                (float) this.height,
                8.0F,
                ColorHelper.shiftTowardsOther(-3092272, -2171170, this.hoverProgress)
        );

        RenderUtils.drawRoundedButton(
                (float) this.x,
                (float) (this.y + 3.0F * this.hoverProgress),
                (float) this.width,
                (float) this.height,
                8.0F,
                -986896
        );

        if (this.text.contains("Lock")) {
            RenderUtils.drawCircle(
                    (float) (this.x + 14),
                    (float) (this.y + 11) + 3.0F * this.hoverProgress,
                    10.0F,
                    ColorHelper.applyAlpha(ClientColors.DARK_SLATE_GREY.getColor(), this.hoverProgress)
            );
        }

        drawKeyContent();

        super.draw(partialTicks);
    }

    private void drawKeyContent() {
        switch (this.text) {
            case "Return":
                drawReturnIcon();
                return;
            case "Back":
                drawBackIcon();
                return;
            case "Meta":
                drawMetaIcon();
                return;
            case "Menu":
                drawMenuIcon();
                return;
            case "Space":
                return;
            default:
                drawLabel();
        }
    }

    private void drawLabel() {
        TrueTypeFont font = this.isBound ? FontUtils.REGULAR_20 : FontUtils.HELVETICA_LIGHT_20;

        RenderUtils.drawString(
                font,
                (float) (this.x + (this.width - font.getWidth(this.text)) / 2),
                (float) (this.y + 19) + 3.0F * this.hoverProgress,
                this.text,
                ColorHelper.applyAlpha(
                        ClientColors.DEEP_TEAL.getColor(),
                        0.4F + (this.isBound ? 0.2F : 0.0F)
                )
        );
    }

    private int getIconColor() {
        return ColorHelper.applyAlpha(
                ClientColors.DEEP_TEAL.getColor(),
                0.3F + (this.isBound ? 0.2F : 0.0F)
        );
    }

    private void drawMenuIcon() {
        int x = this.x + 25;
        int y = this.y + 25 + (int) (3.0F * this.hoverProgress);
        int color = getIconColor();

        RenderUtils.drawRoundedRect3((float) x, (float) y, (float) (x + 14), (float) (y + 3), color);
        RenderUtils.drawRoundedRect((float) x, (float) (y + 4), (float) (x + 14), (float) (y + 7), color);
        RenderUtils.drawRoundedRect3((float) x, (float) (y + 8), (float) (x + 14), (float) (y + 11), color);
        RenderUtils.drawRoundedRect3((float) x, (float) (y + 12), (float) (x + 14), (float) (y + 15), color);
    }

    private void drawMetaIcon() {
        int centerX = this.x + 32;
        int centerY = this.y + 32 + (int) (3.0F * this.hoverProgress);

        RenderUtils.drawCircle(
                (float) centerX,
                (float) centerY,
                14.0F,
                getIconColor()
        );
    }

    private void drawBackIcon() {
        int x = this.x + 43;
        int y = this.y + 33 + (int) (3.0F * this.hoverProgress);
        int color = getIconColor();

        RenderUtils.drawTriangle(
                (float) x,
                (float) y,
                (float) (x + 6),
                (float) (y - 3),
                (float) (x + 6),
                (float) (y + 3),
                color
        );
        RenderUtils.drawRoundedRect(
                (float) (x + 6),
                (float) (y - 1),
                (float) (x + 27),
                (float) (y + 1),
                color
        );
    }

    private void drawReturnIcon() {
        int x = this.x + 50;
        int y = this.y + 33 + (int) (3.0F * this.hoverProgress);
        int color = getIconColor();

        RenderUtils.drawTriangle(
                (float) x,
                (float) y,
                (float) (x + 6),
                (float) (y - 3),
                (float) (x + 6),
                (float) (y + 3),
                color
        );
        RenderUtils.drawRoundedRect(
                (float) (x + 6),
                (float) (y - 1),
                (float) (x + 27),
                (float) (y + 1),
                color
        );
        RenderUtils.drawRoundedRect(
                (float) (x + 25),
                (float) (y - 8),
                (float) (x + 27),
                (float) (y - 1),
                color
        );
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == this.keyCode) {
            this.isKeyDown = true;
        }
        super.keyPressed(keyCode);
    }

    @Override
    public void modifierPressed(int modifier) {
        if (modifier == this.keyCode) {
            this.isKeyDown = false;
        }
        super.modifierPressed(modifier);
    }
}