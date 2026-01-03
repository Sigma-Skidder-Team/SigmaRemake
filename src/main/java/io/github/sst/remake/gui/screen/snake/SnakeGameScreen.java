package io.github.sst.remake.gui.screen.snake;

import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ShaderUtils;
import io.github.sst.remake.util.render.font.FontUtils;

public class SnakeGameScreen extends Screen implements IMinecraft {
    private final SnakeGameWidget snakeGameWidget;
    private final AnimationUtils animation = new AnimationUtils(200, 0);
    private int maxScore;

    public SnakeGameScreen() {
        super("SnakeGameScreen");
        this.setListening(false);
        ShaderUtils.applyBlurShader();
        int gridWidth = 48;
        int gridHeight = 27;
        int cellSize = 14;
        int widgetWidth = gridWidth * cellSize;
        int widgetHeight = gridHeight * cellSize;
        this.addToList(this.snakeGameWidget = new SnakeGameWidget(this, "snake", (this.width - widgetWidth) / 2, (this.height - widgetHeight) / 2 + 30, gridWidth, gridHeight, cellSize));
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = this.animation.calcPercent();
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
                (float) this.snakeGameWidget.getX(),
                (float) this.snakeGameWidget.getY(),
                (float) this.snakeGameWidget.getWidth(),
                (float) this.snakeGameWidget.getHeight(),
                40.0F,
                partialTicks
        );
        RenderUtils.drawRoundedRect(
                (float) (this.snakeGameWidget.getX() - 20),
                (float) (this.snakeGameWidget.getY() - 20),
                (float) (this.snakeGameWidget.getWidth() + 40),
                (float) (this.snakeGameWidget.getHeight() + 40),
                14.0F,
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );
        super.draw(partialTicks);
        int widgetX = (this.width - this.snakeGameWidget.getWidth()) / 2;
        int widgetY = (this.height - this.snakeGameWidget.getHeight()) / 2;
        RenderUtils.drawString(FontUtils.HELVETICA_MEDIUM_40, (float) widgetX, (float) (widgetY - 60), "Snake", ClientColors.LIGHT_GREYISH_BLUE.getColor());
        this.maxScore = Math.max(this.snakeGameWidget.getScore(), this.maxScore);
        String scoreText = "Max: " + this.maxScore + "   |   Score: " + this.snakeGameWidget.getScore();
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                (float) (widgetX + this.snakeGameWidget.getWidth() - FontUtils.HELVETICA_LIGHT_20.getWidth(scoreText)),
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
