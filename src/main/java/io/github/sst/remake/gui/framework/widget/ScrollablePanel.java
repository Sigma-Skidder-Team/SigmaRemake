package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.gui.framework.layout.ContentSize;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.layout.WidthSetter;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.ScissorUtils;
import org.newdawn.slick.opengl.font.TrueTypeFont;

public class ScrollablePanel extends Widget {
    private boolean allowUpdatesWhenHidden = false;
    public GuiComponent content;
    public VerticalScrollBar scrollBar;
    private boolean scissorEnabled = true;
    public int scrollBarWidth = 35;
    public boolean reserveScrollbarSpace = false;

    public ScrollablePanel(GuiComponent parent, String name, int x, int y, int width, int height) {
        super(parent, name, x, y, width, height, false);
        this.initContentAndScrollBar();
    }

    public ScrollablePanel(GuiComponent parent, String name, int x, int y, int width, int height, ColorHelper textColor) {
        super(parent, name, x, y, width, height, textColor, false);
        this.initContentAndScrollBar();
    }

    public ScrollablePanel(
            GuiComponent parent,
            String name,
            int x,
            int y,
            int width,
            int height,
            ColorHelper textColor,
            String text
    ) {
        super(parent, name, x, y, width, height, textColor, text, false);
        this.initContentAndScrollBar();
    }

    public ScrollablePanel(
            GuiComponent parent,
            String name,
            int x,
            int y,
            int width,
            int height,
            ColorHelper textColor,
            String text,
            TrueTypeFont font
    ) {
        super(parent, name, x, y, width, height, textColor, text, font, false);
        this.initContentAndScrollBar();
    }

    private void initContentAndScrollBar() {
        this.getChildren().add(this.content = new GuiComponent(this, "content", 0, 0, this.width, this.height));
        this.content.addWidthSetter(new ContentSize());
        this.getChildren().add(this.scrollBar = new VerticalScrollBar(this, 11));
        this.scrollBar.setReAddChildren(true);
    }

    public void setScrollOffset(int offset) {
        this.scrollBar.setOffset(offset);
    }

    public int getScrollOffset() {
        return this.scrollBar != null ? this.scrollBar.getOffset() : 0;
    }

    public void setAllowUpdatesWhenHidden(boolean allowUpdatesWhenHidden) {
        this.allowUpdatesWhenHidden = allowUpdatesWhenHidden;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.allowUpdatesWhenHidden || this.isSelfVisible()) {
            super.updatePanelDimensions(mouseX, mouseY);

            this.content.setY(-this.scrollBar.getOffset());

            for (GuiComponent child : this.getContent().getChildren()) {
                for (WidthSetter widthSetter : child.getWidthSetters()) {
                    widthSetter.setWidth(child, this);
                }
            }
        }
    }

    public void setScissorEnabled(boolean enableScissor) {
        this.scissorEnabled = enableScissor;
    }

    public boolean isScissorEnabled() {
        return this.scissorEnabled;
    }

    @Override
    public void draw(float partialTicks) {
        this.applyScaleTransforms();

        if (!this.allowUpdatesWhenHidden && !this.isSelfVisible()) {
            return;
        }

        if (this.scissorEnabled) {
            ScissorUtils.startScissor(this);
        }

        super.draw(partialTicks);

        if (this.scissorEnabled) {
            ScissorUtils.restoreScissor();
        }
    }

    @Override
    public void addToList(GuiComponent component) {
        this.content.addToList(component);
    }

    @Override
    public boolean hasChild(GuiComponent child) {
        return this.content.hasChild(child);
    }

    @Override
    public boolean hasChildWithName(String name) {
        return this.content.hasChildWithName(name);
    }

    public GuiComponent getContent() {
        return this.content;
    }

    public void setReserveScrollbarSpace(boolean reserveScrollbarSpace) {
        this.reserveScrollbarSpace = reserveScrollbarSpace;
    }
}