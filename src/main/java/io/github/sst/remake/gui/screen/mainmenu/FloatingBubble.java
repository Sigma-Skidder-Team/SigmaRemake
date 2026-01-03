package io.github.sst.remake.gui.screen.mainmenu;

import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;

public class FloatingBubble extends GuiComponent implements IMinecraft {
    private final MainMenuScreen parent;

    public float velocityX;
    public float velocityY;

    public float baseVelocityX;
    public float baseVelocityY;

    public float posX;
    public float posY;

    public float interactionStrength;

    public int lastMouseX;
    public int lastMouseY;

    public int interactionRadius = 114;

    public FloatingBubble(MainMenuScreen parent, String id, int x, int y, int radius, int velocityX, int velocityY) {
        super(parent, id, x, y, radius, radius);
        this.parent = parent;
        this.velocityX = this.baseVelocityX = (float) velocityX;
        this.velocityY = this.baseVelocityY = (float) velocityY;
        this.posX = (float) x;
        this.posY = (float) y;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.posX == -9999.0F || this.posY == -9999.0F) {
            this.posX = (float) this.x;
            this.posY = (float) this.y;
        }

        this.posX = this.posX + this.velocityX * parent.deltaTime;
        this.posY = this.posY + this.velocityY * parent.deltaTime;
        this.x = Math.round(this.posX);
        this.y = Math.round(this.posY);

        if (!(this.posX + (float) this.width < 0.0F)) {
            if (this.posX > (float) client.getWindow().getWidth()) {
                this.posX = (float) (-this.width);
            }
        } else {
            this.posX = (float) client.getWindow().getWidth();
        }

        if (!(this.posY + (float) this.height < 0.0F)) {
            if (this.posY > (float) client.getWindow().getHeight()) {
                this.posY = (float) (-this.height);
            }
        } else {
            this.posY = (float) client.getWindow().getHeight();
        }

        float x = (float) (mouseX - this.getAbsoluteX());
        float y = (float) (mouseY - this.getAbsoluteY());
        this.interactionStrength = (float) (1.0 - Math.sqrt(x * x + y * y) / (double) this.interactionRadius);
        if (!(Math.sqrt(x * x + y * y) < (double) this.interactionRadius)) {
            this.velocityX = this.velocityX - (this.velocityX - this.baseVelocityX) * 0.05F * parent.deltaTime;
            this.velocityY = this.velocityY - (this.velocityY - this.baseVelocityY) * 0.05F * parent.deltaTime;
        } else {
            float deltaX = this.posX - (float) mouseX;
            float deltaY = this.posY - (float) mouseY;
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            float forceDivisor = distance / 2.0F;
            float forceX = deltaX / forceDivisor;
            float forceY = deltaY / forceDivisor;
            this.velocityX = this.velocityX + forceX / (1.0F + this.interactionStrength) * parent.deltaTime;
            this.velocityY = this.velocityY + forceY / (1.0F + this.interactionStrength) * parent.deltaTime;
        }

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawFilledArc(
                (float) this.x,
                (float) this.y,
                (float) this.getWidth(),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.07F + (!(this.interactionStrength > 0.0F) ? 0.0F : this.interactionStrength * 0.3F))
        );
        super.draw(partialTicks);
    }
}
