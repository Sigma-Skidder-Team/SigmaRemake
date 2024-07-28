package info.opensigma.ui.clickgui.frame;

import info.opensigma.system.IMinecraft;
import info.opensigma.util.math.GeometryUtils;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public abstract class Frame extends DrawableHelper implements IMinecraft {

    protected double posX, posY;

    private boolean dragging;
    private double dragX, dragY;

    public final void draw(final MatrixStack matrices, final int mouseX, final int mouseY) {
        if (dragging) {
            posX = mouseX - dragX;
            posY = mouseY - dragY;
        }

        drawFrame(matrices, mouseX, mouseY);
    }

    protected abstract void drawFrame(final MatrixStack matrices, final int mouseX, final int mouseY);

    public final boolean mouseClick(final double mouseX, final double mouseY, int button) {
        if (GeometryUtils.isInBounds(mouseX, mouseY, posX, posY, getInteractionBounds()[0], getInteractionBounds()[1], true)) {
            dragX = mouseX - posX;
            dragY = mouseY - posY;
            dragging = true;

            return true;
        }

        return mouseClickFrame(mouseX, mouseY, button);
    }

    protected abstract boolean mouseClickFrame(final double mouseX, final double mouseY, final int button);

    public final boolean mouseRelease(final double mouseX, final double mouseY, final int button) {
        if (dragging) {
            dragging = false;
            dragX = 0;
            dragY = 0;

            return true;
        }

        return mouseReleaseFrame(mouseX, mouseY, button);
    }

    protected abstract boolean mouseReleaseFrame(final double mouseX, final double mouseY, final int button);

    protected abstract double[] getInteractionBounds();

}
