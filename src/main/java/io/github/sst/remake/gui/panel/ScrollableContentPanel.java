package io.github.sst.remake.gui.panel;

import io.github.sst.remake.gui.ContentSize;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.impl.VerticalScrollBar;
import io.github.sst.remake.gui.interfaces.IWidthSetter;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.ScissorUtils;
import org.newdawn.slick.opengl.font.TrueTypeFont;

public class ScrollableContentPanel extends AnimatedIconPanel {
    private boolean field21201;
    private boolean field21202;
    private boolean field21203 = false;
    public CustomGuiScreen buttonList;
    public VerticalScrollBar scrollBar;
    private boolean field21206 = true;
    public int field21207 = 35;
    public boolean field21208 = false;

    public ScrollableContentPanel(CustomGuiScreen var1, String name, int var3, int var4, int var5, int var6) {
        super(var1, name, var3, var4, var5, var6, false);
        this.method13511();
    }

    public ScrollableContentPanel(CustomGuiScreen var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7) {
        super(var1, name, var3, var4, var5, var6, var7, false);
        this.method13511();
    }

    public ScrollableContentPanel(CustomGuiScreen var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7, String var8) {
        super(var1, name, var3, var4, var5, var6, var7, var8, false);
        this.method13511();
    }

    public ScrollableContentPanel(CustomGuiScreen var1, String name, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, TrueTypeFont var9) {
        super(var1, name, var3, var4, var5, var6, var7, var8, var9, false);
        this.method13511();
    }

    private void method13511() {
        this.getChildren().add(this.buttonList = new CustomGuiScreen(this, "content", 0, 0, this.width, this.height));
        this.buttonList.setSize(new ContentSize());
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

            for (CustomGuiScreen var6 : this.getButton().getChildren()) {
                for (IWidthSetter var8 : var6.method13260()) {
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
        this.method13224();
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
    public void addToList(CustomGuiScreen var1) {
        this.buttonList.addToList(var1);
    }

    @Override
    public boolean hasChild(CustomGuiScreen child) {
        return this.buttonList.hasChild(child);
    }

    @Override
    public boolean isntQueue(String var1) {
        return this.buttonList.isntQueue(var1);
    }

    public CustomGuiScreen getButton() {
        return this.buttonList;
    }

    public void method13518(boolean var1) {
        this.field21208 = var1;
    }
}
