package io.github.sst.remake.gui.framework.core;

import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.gui.framework.event.DragListener;
import io.github.sst.remake.gui.framework.event.DragHandler;
import io.github.sst.remake.util.math.timer.TogglableTimer;
import io.github.sst.remake.util.math.color.ColorHelper;
import org.newdawn.slick.opengl.font.TrueTypeFont;

import java.util.ArrayList;
import java.util.List;

public class Widget extends GuiComponent implements DragHandler, IMinecraft {
    public boolean draggable;
    public boolean dragging;
    public int dragStartMouseX;
    public int dragStartMouseY;
    public int dragOffsetX;
    public int dragOffsetY;
    public boolean clampToBounds = true;
    public boolean allowBottomOverflow = false;
    public boolean enableHoldToDrag = true;
    public boolean enableMoveThresholdToDrag = true;
    public boolean enableImmediateDrag = false;
    public final TogglableTimer dragStartTimer = new TogglableTimer();
    public int dragStartDelayMs = 300;
    public int dragStartMoveThresholdPx = 2;
    private final List<DragListener> dragListeners = new ArrayList<>();

    public Widget(GuiComponent screen, String name, int x, int y, int width, int height, boolean draggable) {
        super(screen, name, x, y, width, height);
        this.draggable = draggable;
    }

    public Widget(GuiComponent screen, String name, int x, int y, int width, int height, ColorHelper textColor, boolean draggable) {
        super(screen, name, x, y, width, height, textColor);
        this.draggable = draggable;
    }

    public Widget(
            GuiComponent screen,
            String name,
            int x,
            int y,
            int width,
            int height,
            ColorHelper textColor,
            String text,
            boolean draggable
    ) {
        super(screen, name, x, y, width, height, textColor, text);
        this.draggable = draggable;
    }

    public Widget(
            GuiComponent screen,
            String name,
            int x,
            int y,
            int width,
            int height,
            ColorHelper textColor,
            String text,
            TrueTypeFont font,
            boolean draggable
    ) {
        super(screen, name, x, y, width, height, textColor, text, font);
        this.draggable = draggable;
    }

    @Override
    public boolean isMouseDownOverComponent() {
        return this.isMouseDownOverComponent && !this.isDragging();
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        if (!this.isDraggable()) {
            return;
        }

        if (!this.isMouseDownOverComponent && !this.dragging) {
            this.dragOffsetX = this.getWidth() / 2;
            this.dragOffsetY = this.getHeight() / 2;
        }

        this.handleMovementAndCheckBoundaries(mouseX, mouseY);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (super.onMouseDown(mouseX, mouseY, mouseButton)) {
            return true;
        }

        if (this.isDraggable()) {
            this.dragStartTimer.start();

            this.dragStartMouseX = mouseX;
            this.dragStartMouseY = mouseY;

            this.dragOffsetX = this.dragStartMouseX - this.getAbsoluteX();
            this.dragOffsetY = this.dragStartMouseY - this.getAbsoluteY();
        }

        return false;
    }

    @Override
    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {
        super.onMouseRelease(mouseX, mouseY, mouseButton);

        if (this.isDraggable()) {
            this.dragStartTimer.stop();
            this.dragStartTimer.reset();
        }

        this.setDragging(false);
    }

    @Override
    public void handleMovementAndCheckBoundaries(int mouseX, int mouseY) {
        boolean wasDragging = this.dragging;
        if (!this.isDragging() && this.isDraggable()) {
            boolean shouldStartFromHold =
                    this.enableHoldToDrag && this.dragStartTimer.getElapsedTime() >= (long) this.dragStartDelayMs;

            boolean shouldStartFromMove =
                    this.enableMoveThresholdToDrag
                            && this.isMouseDownOverComponent
                            && (Math.abs(this.dragStartMouseX - mouseX) > this.dragStartMoveThresholdPx
                            || Math.abs(this.dragStartMouseY - mouseY) > this.dragStartMoveThresholdPx);

            boolean shouldStartImmediately =
                    this.enableImmediateDrag && this.isMouseDownOverComponent;

            if (shouldStartFromHold || shouldStartFromMove || shouldStartImmediately) {
                this.setDragging(true);
            }
        } else if (this.isDragging()) {
            int parentAbsX = this.parent == null ? 0 : this.parent.getAbsoluteX();
            int parentAbsY = this.parent == null ? 0 : this.parent.getAbsoluteY();

            this.setX(mouseX - this.dragOffsetX - parentAbsX);
            this.setY(mouseY - this.dragOffsetY - parentAbsY);

            if (this.clampToBounds) {
                this.clampToParentBounds();
            }
        }

        if (this.isDragging() && !wasDragging) {
            this.dragStartTimer.stop();
            this.dragStartTimer.reset();
        }
    }

    @Override
    public boolean isDraggable() {
        return this.draggable;
    }

    @Override
    public void setDraggable(boolean state) {
        this.draggable = state;
    }

    @Override
    public boolean isDragging() {
        return this.dragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
        if (dragging) {
            this.setDraggable(true);
            this.notifyDragListeners();
        }
    }

    public Widget addDragListener(DragListener listener) {
        this.dragListeners.add(listener);
        return this;
    }

    public void notifyDragListeners() {
        for (DragListener listener : this.dragListeners) {
            listener.onDragStart(this);
        }
    }

    private void clampToParentBounds() {
        if (this.parent == null) {
            if (this.getX() < 0) {
                this.setX(0);
            }
            if (this.getX() + this.getWidth() > client.getWindow().getWidth()) {
                this.setX(client.getWindow().getWidth() - this.getWidth());
            }
            if (this.getY() < 0) {
                this.setY(0);
            }
            if (this.getY() + this.getHeight() > client.getWindow().getHeight()) {
                this.setY(client.getWindow().getHeight() - this.getHeight());
            }
            return;
        }

        if (this.getX() < 0) {
            this.setX(0);
        }
        if (this.getX() + this.getWidth() > this.parent.getWidth()) {
            this.setX(this.parent.getWidth() - this.getWidth());
        }
        if (this.getY() < 0) {
            this.setY(0);
        }
        if (this.getY() + this.getHeight() > this.parent.getHeight() && !this.allowBottomOverflow) {
            this.setY(this.parent.getHeight() - this.getHeight());
        }
    }
}
