package io.github.sst.remake.gui.screen.snake;

import io.github.sst.remake.util.client.ScreenDimension;

public enum SnakeDirection {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    public final ScreenDimension vector;

    SnakeDirection(int x, int y) {
        this.vector = new ScreenDimension(x, y);
    }
}
