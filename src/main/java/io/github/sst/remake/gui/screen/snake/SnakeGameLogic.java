package io.github.sst.remake.gui.screen.snake;

import io.github.sst.remake.util.client.ScreenDimension;
import io.github.sst.remake.util.io.audio.SoundUtils;

public class SnakeGameLogic {
    public final int boardWidth;
    public final int boardHeight;
    private Snake snake;
    private ScreenDimension food;

    public SnakeGameLogic(int width, int height) {
        this.boardWidth = width;
        this.boardHeight = height;
        this.snake = new Snake(new ScreenDimension(this.boardWidth / 2, this.boardHeight / 2));
        this.spawnFood();
    }

    public void update() {
        this.snake.move();
        if (this.snake.isCollidingWith(this.food)) {
            this.spawnFood();
            this.snake.grow();
            SoundUtils.play("pop");
        }

        if (this.snake.hasCollidedWithSelf() || this.isSnakeOutOfBounds()) {
            this.reset();
        }
    }

    public boolean isSnakeOutOfBounds() {
        for (ScreenDimension bodyPart : this.snake.getBodyParts()) {
            if (bodyPart.width < 0 || bodyPart.height < 0 || bodyPart.width >= this.boardWidth || bodyPart.height >= this.boardHeight) {
                return true;
            }
        }

        return false;
    }

    public boolean isOutOfBounds(ScreenDimension position) {
        return position.width < 0 || position.height < 0 || position.width >= this.boardWidth || position.height >= this.boardHeight;
    }

    public void reset() {
        this.snake = new Snake(new ScreenDimension(this.boardWidth / 2, this.boardHeight / 2));
        this.spawnFood();
    }

    public ScreenDimension generateFoodPosition() {
        ScreenDimension position = null;

        while (this.snake.isCollidingWith(position) || this.isOutOfBounds(position)) {
            position = new ScreenDimension((int) Math.round(Math.random() * (double) this.boardWidth), (int) Math.round(Math.random() * (double) this.boardHeight));
        }

        return position;
    }

    public void spawnFood() {
        this.food = this.generateFoodPosition();
    }

    public ScreenDimension getFood() {
        return this.food;
    }

    public Snake getSnake() {
        return this.snake;
    }
}
