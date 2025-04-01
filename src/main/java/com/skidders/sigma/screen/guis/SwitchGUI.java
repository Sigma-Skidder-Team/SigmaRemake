package com.skidders.sigma.screen.guis;

import com.skidders.sigma.screen.Animation;
import com.skidders.sigma.util.client.render.ColorUtil;
import com.skidders.sigma.util.client.render.RenderUtil;
import com.skidders.sigma.util.system.SmoothInterpolator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SwitchGUI extends Screen {
    private Animation anim;

    public SwitchGUI() {
        super(Text.of("Switch"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (anim == null) {
            anim = new Animation(1050, 200, Animation.Direction.BACKWARDS);
        }

        anim.changeDirection(Animation.Direction.FORWARDS);
        //TODO: Parallax/background follows mouse
        RenderUtil.drawImage(
                "loading/back.png",
                0,
                0,
                width,
                height
        );

        RenderUtil.drawRoundedRect2(
                0.0F,
                0.0F,
                (float) width,
                (float) height,
                ColorUtil.applyAlpha(ColorUtil.ClientColors.DEEP_TEAL.getColor(), 0.3F)
        );

        super.render(matrices, mouseX, mouseY, delta);

        RenderUtil.drawImage(
                "loading/back.png",
                0,
                0,
                width,
                height,
                ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.getColor(), 1.0F - anim.calcPercent())
        );
        RenderUtil.drawRoundedRect2(
                0.0F,
                0.0F,
                (float) width,
                (float) height,
                ColorUtil.applyAlpha(0, 0.75F * (1.0F - anim.calcPercent()))
        );

        float logoWidth = 455.0F / 2;
        float logoHeight = 78.0F / 2;
        float logoX = width / 2f - logoWidth / 2f;
        float centeredLogoY = height / 2f - logoHeight / 2f - 14.0F;

        float smoothPercent = SmoothInterpolator.interpolate(anim.calcPercent(), 0.16, 0.71, 0.0, 0.99);

        float logoY = centeredLogoY - (centeredLogoY - 40) * smoothPercent;

        RenderUtil.drawImage("loading/logo.png", logoX, logoY, logoWidth, logoHeight);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}