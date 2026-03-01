package io.github.sst.remake.gui.screen.options;

import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.EasingFunctions;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class OptionsScreen extends Screen {
    private static AnimationUtils openCloseAnimation;
    private static net.minecraft.client.gui.screen.Screen returnScreen = null;

    public OptionsScreen() {
        super("options");

        this.setListening(false);

        int panelHeight = Math.max((int) ((float) this.height * 0.8F), 420);
        int panelWidth = (int) ((float) this.width * 0.8F);

        this.addToList(new OptionsPage(
                this,
                "centerBlock",
                this.getWidth() - panelWidth,
                this.getHeight() - panelHeight,
                panelWidth - (this.getWidth() - panelWidth),
                panelHeight - (this.getHeight() - panelHeight)
        ));

        openCloseAnimation = new AnimationUtils(300, 100);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (openCloseAnimation.getDirection() == AnimationUtils.Direction.FORWARDS
                && openCloseAnimation.calcPercent() == 0.0F
                && returnScreen != null) {
            MinecraftClient.getInstance().openScreen(returnScreen);
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        float scale = 1.3F - EasingFunctions.easeOutBack(openCloseAnimation.calcPercent(), 0.0F, 1.0F, 1.0F) * 0.3F;
        float blend = 1.0F;

        if (openCloseAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            scale = 0.7F + QuadraticEasing.easeOutQuad(openCloseAnimation.calcPercent(), 0.0F, 1.0F, 1.0F) * 0.3F;
            blend = openCloseAnimation.calcPercent();
        }

        int bgTop = ColorHelper.shiftTowardsOther(
                -1072689136,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F),
                blend
        );
        int bgBottom = ColorHelper.shiftTowardsOther(
                -804253680,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F),
                blend
        );

        RenderUtils.drawGradient(0, 0, this.getWidth(), this.getHeight(), bgTop, bgBottom);

        this.setScale(scale, scale);
        this.applyScaleTransforms();

        super.draw(openCloseAnimation.calcPercent());
    }

    public static void showGUI(net.minecraft.client.gui.screen.Screen screen) {
        returnScreen = screen;
        openCloseAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().openScreen(null);
        }
    }
}