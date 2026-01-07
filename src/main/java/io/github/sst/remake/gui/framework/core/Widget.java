package io.github.sst.remake.gui.framework.core;

import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.gui.framework.event.DragListener;
import io.github.sst.remake.gui.framework.event.DragHandler;
import io.github.sst.remake.util.math.TimerUtils;
import io.github.sst.remake.util.math.color.ColorHelper;
import org.newdawn.slick.opengl.font.TrueTypeFont;

import java.util.ArrayList;
import java.util.List;

public class Widget extends GuiComponent implements DragHandler, IMinecraft {
    public boolean draggable;
    public boolean dragging;
    public int mouseX;
    public int mouseY;
    public int sizeWidthThingy;
    public int sizeHeightThingy;
    public boolean field20882 = true;
    public boolean field20883 = false;
    public boolean field20884 = true;
    public boolean field20885 = true;
    public boolean field20886 = false;
    public final TimerUtils timerUtils = new TimerUtils();
    public int field20888 = 300;
    public int field20889 = 2;
    private final List<DragListener> dragListeners = new ArrayList<>();

    public Widget(GuiComponent screen, String name, int x, int y, int width, int height, boolean var7) {
        super(screen, name, x, y, width, height);
        this.draggable = var7;
    }

    public Widget(GuiComponent screen, String name, int x, int y, int width, int height, ColorHelper colorHelper, boolean var8) {
        super(screen, name, x, y, width, height, colorHelper);
        this.draggable = var8;
    }

    public Widget(GuiComponent screen, String name, int x, int y, int width, int height, ColorHelper colorHelper, String text, boolean var9) {
        super(screen, name, x, y, width, height, colorHelper, text);
        this.draggable = var9;
    }

    public Widget(GuiComponent screen, String name, int x, int y, int width, int height, ColorHelper colorHelper, String var8, TrueTypeFont font, boolean var10) {
        super(screen, name, x, y, width, height, colorHelper, var8, font);
        this.draggable = var10;
    }

    @Override
    public boolean isMouseDownOverComponent() {
        return this.isMouseDownOverComponent && !this.isDragging();
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        if (this.isDraggable()) {
            if (!this.isMouseDownOverComponent && !this.dragging) {
                this.sizeWidthThingy = this.getWidth() / 2;
                this.sizeHeightThingy = this.getHeight() / 2;
            }

            this.handleMovementAndCheckBoundaries(mouseX, mouseY);
        }
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseDown(mouseX, mouseY, mouseButton)) {
            if (this.isDraggable()) {
                this.timerUtils.start();
                this.mouseX = mouseX;
                this.mouseY = mouseY;
                this.sizeWidthThingy = this.mouseX - this.getAbsoluteX();
                this.sizeHeightThingy = this.mouseY - this.getAbsoluteY();
            }

            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {
        super.onMouseRelease(mouseX, mouseY, mouseButton);
        if (this.isDraggable()) {
            this.timerUtils.stop();
            this.timerUtils.reset();
        }

        this.setDragging(false);
    }

    @Override
    public void handleMovementAndCheckBoundaries(int mouseX, int mouseY) {
        boolean var5 = this.dragging;
        if (!this.isDragging() && this.isDraggable()) {
            boolean var6 = this.field20884 && this.timerUtils.getElapsedTime() >= (long) this.field20888;
            boolean var7 = this.field20885
                    && this.isMouseDownOverComponent
                    && (Math.abs(this.mouseX - mouseX) > this.field20889 || Math.abs(this.mouseY - mouseY) > this.field20889);
            boolean var8 = this.field20886 && this.isMouseDownOverComponent;
            if (var6 || var7 || var8) {
                this.setDragging(true);
            }
        } else if (this.isDragging()) {
            this.setX(mouseX - this.sizeWidthThingy - (this.parent == null ? 0 : this.parent.getAbsoluteX()));
            this.setY(mouseY - this.sizeHeightThingy - (this.parent == null ? 0 : this.parent.getAbsoluteY()));
            if (this.field20882) {
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
                } else {
                    if (this.getX() < 0) {
                        this.setX(0);
                    }

                    if (this.getX() + this.getWidth() > this.parent.getWidth()) {
                        this.setX(this.parent.getWidth() - this.getWidth());
                    }

                    if (this.getY() < 0) {
                        this.setY(0);
                    }

                    if (this.getY() + this.getHeight() > this.parent.getHeight() && !this.field20883) {
                        this.setY(this.parent.getHeight() - this.getHeight());
                    }
                }
            }
        }

        if (this.isDragging() && !var5) {
            this.timerUtils.stop();
            this.timerUtils.reset();
        }
    }

    @Override
    public boolean isDraggable() {
        return this.draggable;
    }

    @Override
    public void setDraggable(boolean var1) {
        this.draggable = var1;
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
}
