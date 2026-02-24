package io.github.sst.remake.gui.screen.snake;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ScreenDimension;
import io.github.sst.remake.util.math.timer.TogglableTimer;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.render.RenderUtils;
import org.lwjgl.opengl.GL11;

public class SnakeGameWidget extends Widget implements IMinecraft {
    public SnakeGameLogic gameLogic;
    public TogglableTimer timer = new TogglableTimer();
    public int cellSize;

    public SnakeGameWidget(GuiComponent parent, String name, int x, int y, int width, int height, int cellSize) {
        super(parent, name, x, y, 100, 100, false);
        this.gameLogic = new SnakeGameLogic(width, height);
        this.width = width * cellSize;
        this.height = height * cellSize;
        this.cellSize = cellSize;
        this.timer.start();
    }

    @Override
    public void draw(float partialTicks) {
        if (this.timer.getElapsedTime() > 70L) {
            this.timer.reset();
            this.gameLogic.update();
        }

        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.x, (float) this.y, 0.0F);
        RenderUtils.drawRoundedRect2(0.0F, 0.0F, (float) this.width, (float) this.height, ClientColors.DEEP_TEAL.getColor());
        RenderUtils.drawRoundedButton(
                (float) (this.gameLogic.getFood().width * this.cellSize),
                (float) (this.gameLogic.getFood().height * this.cellSize),
                (float) this.cellSize,
                (float) this.cellSize,
                5.0F,
                ClientColors.PALE_ORANGE.getColor()
        );

        for (ScreenDimension bodyPart : this.gameLogic.getSnake().getBodyParts()) {
            RenderUtils.drawRoundedRect2(
                    (float) (bodyPart.width * this.cellSize),
                    (float) (bodyPart.height * this.cellSize),
                    (float) this.cellSize,
                    (float) this.cellSize,
                    ClientColors.LIGHT_GREYISH_BLUE.getColor()
            );
        }

        GL11.glPopMatrix();
        super.draw(partialTicks);
    }

    public int getScore() {
        return this.gameLogic.getSnake().getBodyParts().size();
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == client.options.keyForward.boundKey.getCode()) {
            this.gameLogic.getSnake().setDirection(SnakeDirection.UP);
        } else if (keyCode == client.options.keyBack.boundKey.getCode()) {
            this.gameLogic.getSnake().setDirection(SnakeDirection.DOWN);
        } else if (keyCode == client.options.keyLeft.boundKey.getCode()) {
            this.gameLogic.getSnake().setDirection(SnakeDirection.LEFT);
        } else if (keyCode == client.options.keyRight.boundKey.getCode()) {
            this.gameLogic.getSnake().setDirection(SnakeDirection.RIGHT);
        }
    }
}
