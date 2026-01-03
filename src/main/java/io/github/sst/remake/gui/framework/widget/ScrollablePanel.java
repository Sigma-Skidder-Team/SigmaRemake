package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.gui.framework.layout.ContentSize;
import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.element.impl.VerticalScrollBar;
import io.github.sst.remake.gui.framework.layout.WidthSetter;
import io.github.sst.remake.gui.panel.Widget;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.ScissorUtils;
import org.newdawn.slick.opengl.font.TrueTypeFont;

public class ScrollablePanel extends Widget {
    private boolean field21201;
    private boolean field21202;
    private boolean field21203 = false;
    public GuiComponent buttonList;
    public VerticalScrollBar scrollBar;
    private boolean field21206 = true;
    public int field21207 = 35;
    public boolean field21208 = false;

    public ScrollablePanel(GuiComponent var1, String name, int var3, int var4, int var5, int var6) {
        super(var1, name, var3, var4, var5, var6, false);
        this.method13511();
    }

    public ScrollablePanel(GuiComponent var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7) {
        super(var1, name, var3, var4, var5, var6, var7, false);
        this.method13511();
    }

    public ScrollablePanel(GuiComponent var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7, String var8) {
        super(var1, name, var3, var4, var5, var6, var7, var8, false);
        this.method13511();
    }

    public ScrollablePanel(GuiComponent var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, TrueTypeFont var9) {
        super(var1, name, var3, var4, var5, var6, var7, var8, var9, false);
        this.method13511();
    }

    private void method13511() {
        this.getChildren().add(this.buttonList = new GuiComponent(this, "content", 0, 0, this.width, this.height));
        this.buttonList.addWidthSetter(new ContentSize());
        this.getChildren().add(this.scrollBar = new VerticalScrollBar(this, 11));
        this.scrollBar.setReAddChildren(true);
    }

    public void setScrollOffset(int var1) {
        this.scrollBar.setOffset(var1);
    }

    public int getScrollOffset() {
        return this.scrollBar != null ? this.scrollBar.getOffset() : 0;
    }

    public void method13514(boolean var1) {
        this.field21203 = var1;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (!this.field21203 || this.isSelfVisible()) {
            super.updatePanelDimensions(mouseX, mouseY);
            this.buttonList.setY(-1 * this.scrollBar.getOffset());

            for (GuiComponent var6 : this.getButton().getChildren()) {
                for (WidthSetter var8 : var6.getWidthSetters()) {
                    var8.setWidth(var6, this);
                }
            }
        }
    }

    public void method13515(boolean var1) {
        this.field21206 = var1;
    }

    public boolean method13516() {
        return this.field21206;
    }

    @Override
    public void draw(float partialTicks) {
        this.applyScaleTransforms();
        if (!this.field21203 || this.isSelfVisible()) {
            if (this.field21206) {
                ScissorUtils.startScissor(this);
            }

            super.draw(partialTicks);
            if (this.field21206) {
                ScissorUtils.restoreScissor();
            }
        }
    }

    @Override
    public void addToList(GuiComponent var1) {
        this.buttonList.addToList(var1);
    }

    @Override
    public boolean hasChild(GuiComponent child) {
        return this.buttonList.hasChild(child);
    }

    @Override
    public boolean hasChildWithName(String var1) {
        return this.buttonList.hasChildWithName(var1);
    }

    public GuiComponent getButton() {
        return this.buttonList;
    }

    public void method13518(boolean var1) {
        this.field21208 = var1;
    }
}
