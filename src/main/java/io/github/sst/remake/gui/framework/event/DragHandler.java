package io.github.sst.remake.gui.framework.event;

public interface DragHandler {
    void handleMovementAndCheckBoundaries(int mouseX, int mouseY);

    boolean isDraggable();

    void setDraggable(boolean state);

    boolean isDragging();

    void setDragging(boolean dragging);
}
