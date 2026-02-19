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
    private boolean enableScissor = true;
    public int scrollBarWidth = 35;
    public boolean reserveScrollbarSpace = false;

    public ScrollablePanel(GuiComponent var1, String name, int var3, int var4, int var5, int var6) {
        super(var1, name, var3, var4, var5, var6, false);
        this.initContentAndScrollBar();
    }

    public ScrollablePanel(GuiComponent var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7) {
        super(var1, name, var3, var4, var5, var6, var7, false);
        this.initContentAndScrollBar();
    }

    public ScrollablePanel(GuiComponent var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7, String var8) {
        super(var1, name, var3, var4, var5, var6, var7, var8, false);
        this.initContentAndScrollBar();
    }

    public ScrollablePanel(GuiComponent var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, TrueTypeFont var9) {
        super(var1, name, var3, var4, var5, var6, var7, var8, var9, false);
        this.initContentAndScrollBar();
    }

    private void initContentAndScrollBar() {
        this.getChildren().add(this.content = new GuiComponent(this, "content", 0, 0, this.width, this.height));
        this.content.addWidthSetter(new ContentSize());
        this.getChildren().add(this.scrollBar = new VerticalScrollBar(this, 11));
        this.scrollBar.setReAddChildren(true);
    }

    public void setScrollOffset(int var1) {
        this.scrollBar.setOffset(var1);
    }

    public int getScrollOffset() {
        return this.scrollBar != null ? this.scrollBar.getOffset() : 0;
    }

    public void setAllowUpdatesWhenHidden(boolean allowUpdatesWhenHidden) {
        this.allowUpdatesWhenHidden = allowUpdatesWhenHidden;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (!this.allowUpdatesWhenHidden || this.isSelfVisible()) {
            super.updatePanelDimensions(mouseX, mouseY);
            this.content.setY(-1 * this.scrollBar.getOffset());

            for (GuiComponent var6 : this.getContent().getChildren()) {
                for (WidthSetter var8 : var6.getWidthSetters()) {
                    var8.setWidth(var6, this);
                }
            }
        }
    }

    public void setScissorEnabled(boolean enableScissor) {
        this.enableScissor = enableScissor;
    }

    public boolean isScissorEnabled() {
        return this.enableScissor;
    }

    @Override
    public void draw(float partialTicks) {
        this.applyScaleTransforms();
        if (!this.allowUpdatesWhenHidden || this.isSelfVisible()) {
            if (this.enableScissor) {
                ScissorUtils.startScissor(this);
            }

            super.draw(partialTicks);
            if (this.enableScissor) {
                ScissorUtils.restoreScissor();
            }
        }
    }

    @Override
    public void addToList(GuiComponent var1) {
        this.content.addToList(var1);
    }

    @Override
    public boolean hasChild(GuiComponent child) {
        return this.content.hasChild(child);
    }

    @Override
    public boolean hasChildWithName(String var1) {
        return this.content.hasChildWithName(var1);
    }

    public GuiComponent getContent() {
        return this.content;
    }

    public void setReserveScrollbarSpace(boolean var1) {
        this.reserveScrollbarSpace = var1;
    }
}
