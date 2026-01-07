package io.github.sst.remake.gui.screen.snake;

import io.github.sst.remake.util.client.ScreenDimension;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    private final List<ScreenDimension> bodyParts = new ArrayList<>();
    private SnakeDirection direction = SnakeDirection.LEFT;
    private boolean directionChanged = false;
    private boolean growing = false;
    private boolean collidedWithSelf = false;

    public Snake(ScreenDimension start) {
        this.bodyParts.add(start.add(this.getDirectionVector().add(this.getDirectionVector())));
        this.bodyParts.add(start.add(this.getDirectionVector()));
        this.bodyParts.add(start);
    }

    private ScreenDimension getDirectionVector() {
        return this.direction.vector;
    }

    public void move() {
        ScreenDimension newHead = this.bodyParts.get(0).add(this.getDirectionVector());
        this.collidedWithSelf = this.bodyParts.contains(newHead);
        this.bodyParts.add(0, newHead);
        if (!this.growing) {
            this.bodyParts.remove(this.bodyParts.size() - 1);
        }

        this.directionChanged = false;
        this.growing = false;
    }

    public void grow() {
        this.growing = true;
    }

    public void setDirection(SnakeDirection newDirection) {
        ScreenDimension movementVector = newDirection.vector.add(this.direction.vector);
        if ((movementVector.width != 0 || movementVector.height != 0) && newDirection != this.direction && !this.directionChanged) {
            this.direction = newDirection;
            this.directionChanged = true;
        }
    }

    public boolean hasCollidedWithSelf() {
        return this.collidedWithSelf;
    }

    public List<ScreenDimension> getBodyParts() {
        return this.bodyParts;
    }

    public boolean isCollidingWith(ScreenDimension position) {
        return position == null || this.bodyParts.contains(position);
    }
}
