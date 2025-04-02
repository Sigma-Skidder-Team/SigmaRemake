package com.skidders.sigma.screen.guis;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.SigmaReborn;
import com.skidders.sigma.screen.Animation;
import com.skidders.sigma.screen.elements.ImageButton;
import com.skidders.sigma.util.client.interfaces.ITextures;
import com.skidders.sigma.util.client.render.ColorUtil;
import com.skidders.sigma.util.client.render.RenderUtil;
import com.skidders.sigma.util.client.render.image.ImageUtil;
import com.skidders.sigma.util.system.math.SmoothInterpolator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class SwitchGUI extends Screen {
    private Animation anim;
    private final List<ImageButton> buttons = new ArrayList<>();

    public SwitchGUI() {
        super(Text.of("Switch"));
    }

    @Override
    protected void init() {
        super.init();
        buttons.clear();

        float bigWidth = 537.0f / 2;
        float smallWidth = 264.0f / 2;
        float bigHeight = 93.0f / 2;
        float smallHeight = 61.0f / 2;
        float x = (this.width - bigWidth) / 2;
        float y = (this.height - bigHeight) / 2 + 14;

        buttons.add(new ImageButton("pb", x, y, bigWidth, bigHeight, ITextures.switch_noAddonsButton));
        buttons.add(new ImageButton("pb2", x, bigHeight + y + 9, smallWidth, smallHeight, ITextures.switch_classicButton));
        buttons.add(new ImageButton("pb3", x + smallWidth + 5, bigHeight + y + 9, smallWidth, smallHeight, ITextures.switch_jelloButton));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (anim == null) {
            anim = new Animation(1050, 200, Animation.Direction.BACKWARDS);
        }

        anim.changeDirection(Animation.Direction.FORWARDS);
        //TODO: Parallax/background follows mouse
        ImageUtil.drawImage(
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

        ImageUtil.drawImage(
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

        ImageUtil.drawImage("loading/logo.png", logoX, logoY, logoWidth, logoHeight);

        buttons.forEach(b -> b.draw(delta, mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        buttons.forEach(b -> {
            if (b.isHovered(mouseX, mouseY)) {
                switch (b.getIdentificator()) {
                    case "pb" -> {
                        SigmaReborn.MODE = SigmaReborn.Mode.NOADDONS;
                        client.openScreen(new TitleScreen());
                    }
                    case "pb2" -> {
                        SigmaReborn.MODE = SigmaReborn.Mode.CLASSIC;
                        client.openScreen(new JelloGUI());
                    }
                    case "pb3" -> {
                        SigmaReborn.MODE = SigmaReborn.Mode.JELLO;
                        client.openScreen(new JelloGUI());
                    }
                }
            }
        });

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}