package info.opensigma.ui.clickgui;

import info.opensigma.module.data.ModuleCategory;
import info.opensigma.ui.clickgui.frame.Frame;
import info.opensigma.ui.clickgui.frame.impl.CategoryFrame;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {

    private final List<Frame> frames = new ArrayList<>();

    public ClickGui() {
        super(Text.of("ClickGui"));

        float x = 15, y = 15;
        int count = 0;

        for (final ModuleCategory category : ModuleCategory.values()) {

            frames.add(new CategoryFrame(category, x, y));

            if (count == 3) {
                x = 15;
                y = 160 + 5 + 15;
            } else {
                x += 100 + 5;
            }

            count++;
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        frames.forEach(frame -> frame.draw(matrices, mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (final Frame frame : frames) {
            if (frame.mouseClick(mouseX, mouseY, button))
                return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (final Frame frame : frames) {
            if (frame.mouseRelease(mouseX, mouseY, button))
                return true;
        }

        return false;
    }

}
