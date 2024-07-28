package info.opensigma.ui.clickgui.frame.impl;

import info.opensigma.module.data.ModuleCategory;
import info.opensigma.ui.clickgui.frame.Frame;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class CategoryFrame extends Frame {

    public final ModuleCategory category;

    public CategoryFrame(final ModuleCategory category, final double posX, final double posY) {
        this.category = category;

        this.posX = posX;
        this.posY = posY;
    }

    @Override
    protected void drawFrame(MatrixStack matrices, int mouseX, int mouseY) {
        fill(matrices, (int) posX, (int) posY, (int) posX + 100, (int) posY + 30, new Color(255, 255, 255, 210).getRGB());
        fill(matrices, (int) posX, (int) posY + 30, (int) posX + 100, (int) posY + 160, -1);
    }

    @Override
    protected boolean mouseClickFrame(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected boolean mouseReleaseFrame(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected double[] getInteractionBounds() {
        return new double[] {posX, posY, 100, 30};
    }

}
