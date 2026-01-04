package io.github.sst.remake.gui.screen.bird;

import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ShaderUtils;
import io.github.sst.remake.util.render.font.FontUtils;

public class BirdGameScreen extends Screen implements IMinecraft {
    private final AnimationUtils introAnimation = new AnimationUtils(200, 0);
    private final BirdGameWidget birdGameWidget;
    private int maxScore = 0;

    public BirdGameScreen() {
        super("BirdGameScreen");
        this.setListening(false);
        ShaderUtils.applyBlurShader();
        int width = 48;
        int height = 27;
        int scale = 14;
        int widgetWidth = width * scale;
        int widgetHeight = height * scale;
        this.addToList(this.birdGameWidget = new BirdGameWidget(this, "bird", (this.width - widgetWidth) / 2, (this.getHeight() - widgetHeight) / 2 - 30));
    }

    @Override
    public void draw(float partialTicks) {
        if (this.birdGameWidget.gameOver) {
            this.maxScore = Math.max(this.maxScore, this.birdGameWidget.score);
            this.birdGameWidget.reset();
        }
        partialTicks = this.introAnimation.calcPercent();
        float easedPercent = EasingFunctions.easeOutBack(partialTicks, 0.0F, 1.0F, 1.0F);
        this.setScale(0.8F + easedPercent * 0.2F, 0.8F + easedPercent * 0.2F);
        float alpha = 0.25F * partialTicks;
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) (this.x + this.width),
                (float) (this.y + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), alpha)
        );
        super.applyScaleTransforms();
        RenderUtils.drawRoundedRect(
                (float) this.birdGameWidget.getX(),
                (float) this.birdGameWidget.getY(),
                (float) this.birdGameWidget.getWidth(),
                (float) this.birdGameWidget.getHeight(),
                40.0F,
                partialTicks
        );
        RenderUtils.drawRoundedRect(
                (float) (this.birdGameWidget.getX() - 20),
                (float) (this.birdGameWidget.getY() - 20),
                (float) (this.birdGameWidget.getWidth() + 40),
                (float) (this.birdGameWidget.getHeight() + 40),
                14.0F,
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );
        super.draw(partialTicks);
        int widgetX = (this.width - this.birdGameWidget.getWidth()) / 2;
        int widgetY = (this.height - this.birdGameWidget.getHeight()) / 2;
        RenderUtils.drawString(FontUtils.HELVETICA_MEDIUM_40, (float) widgetX, (float) (widgetY - 60), "Flappy Bird", ClientColors.LIGHT_GREYISH_BLUE.getColor());
        String scoreText = "Max: " + this.maxScore + "   |   Score: " + this.birdGameWidget.score;
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                (float) (widgetX + this.birdGameWidget.getWidth() - FontUtils.HELVETICA_LIGHT_20.getWidth(scoreText)),
                (float) (widgetY - 50),
                scoreText,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F)
        );
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            ShaderUtils.resetShader();
            client.openScreen(null);
        }
    }
}
