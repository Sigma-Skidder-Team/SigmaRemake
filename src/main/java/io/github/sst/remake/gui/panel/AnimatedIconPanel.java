package io.github.sst.remake.gui.panel;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.gui.interfaces.Class6751;
import io.github.sst.remake.gui.interfaces.INestedGuiEventHandler;
import io.github.sst.remake.util.math.TimerUtils;
import io.github.sst.remake.util.math.color.ColorHelper;
import org.newdawn.slick.TrueTypeFont;

import java.util.ArrayList;
import java.util.List;

public class AnimatedIconPanel extends CustomGuiScreen implements INestedGuiEventHandler, IMinecraft {
    public boolean field20876;
    public boolean field20877;
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
    private final List<Class6751> field20890 = new ArrayList<Class6751>();

    public AnimatedIconPanel(CustomGuiScreen screen, String iconName, int x, int y, int width, int height, boolean var7) {
        super(screen, iconName, x, y, width, height);
        this.field20876 = var7;
    }

    public AnimatedIconPanel(CustomGuiScreen screen, String iconName, int x, int y, int width, int height, ColorHelper colorHelper, boolean var8) {
        super(screen, iconName, x, y, width, height, colorHelper);
        this.field20876 = var8;
    }

    public AnimatedIconPanel(CustomGuiScreen screen, String iconName, int x, int y, int width, int height, ColorHelper colorHelper, String text, boolean var9) {
        super(screen, iconName, x, y, width, height, colorHelper, text);
        this.field20876 = var9;
    }

    public AnimatedIconPanel(CustomGuiScreen screen, String iconName, int x, int y, int width, int height, ColorHelper colorHelper, String var8, TrueTypeFont font, boolean var10) {
        super(screen, iconName, x, y, width, height, colorHelper, var8, font);
        this.field20876 = var10;
    }

    @Override
    public boolean method13212() {
        return this.field20909 && !this.method13216();
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        super.updatePanelDimensions(newHeight, newWidth);
        if (this.method13214()) {
            if (!this.field20909 && !this.field20877) {
                this.sizeWidthThingy = this.getWidthA() / 2;
                this.sizeHeightThingy = this.getHeightA() / 2;
            }

            this.handleMovementAndCheckBoundaries(newHeight, newWidth);
        }
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int mouseButton) {
        if (!super.onClick(mouseX, mouseY, mouseButton)) {
            if (this.method13214()) {
                this.timerUtils.start();
                this.mouseX = mouseX;
                this.mouseY = mouseY;
                this.sizeWidthThingy = this.mouseX - this.method13271();
                this.sizeHeightThingy = this.mouseY - this.method13272();
            }

            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onClick2(int mouseX, int mouseY, int mouseButton) {
        super.onClick2(mouseX, mouseY, mouseButton);
        if (this.method13214()) {
            this.timerUtils.stop();
            this.timerUtils.reset();
        }

        this.method13217(false);
    }

    @Override
    public void handleMovementAndCheckBoundaries(int newHeight, int newWidth) {
        boolean var5 = this.field20877;
        if (!this.method13216() && this.method13214()) {
            boolean var6 = this.field20884 && this.timerUtils.getElapsedTime() >= (long) this.field20888;
            boolean var7 = this.field20885
                    && this.field20909
                    && (Math.abs(this.mouseX - newHeight) > this.field20889 || Math.abs(this.mouseY - newWidth) > this.field20889);
            boolean var8 = this.field20886 && this.field20909;
            if (var6 || var7 || var8) {
                this.method13217(true);
            }
        } else if (this.method13216()) {
            this.setXA(newHeight - this.sizeWidthThingy - (this.parent == null ? 0 : this.parent.method13271()));
            this.setYA(newWidth - this.sizeHeightThingy - (this.parent == null ? 0 : this.parent.method13272()));
            if (this.field20882) {
                if (this.parent == null) {
                    if (this.getXA() < 0) {
                        this.setXA(0);
                    }

                    if (this.getXA() + this.getWidthA() > client.getWindow().getWidth()) {
                        this.setXA(client.getWindow().getWidth() - this.getWidthA());
                    }

                    if (this.getYA() < 0) {
                        this.setYA(0);
                    }

                    if (this.getYA() + this.getHeightA() > client.getWindow().getHeight()) {
                        this.setYA(client.getWindow().getHeight() - this.getHeightA());
                    }
                } else {
                    if (this.getXA() < 0) {
                        this.setXA(0);
                    }

                    if (this.getXA() + this.getWidthA() > this.parent.getWidthA()) {
                        this.setXA(this.parent.getWidthA() - this.getWidthA());
                    }

                    if (this.getYA() < 0) {
                        this.setYA(0);
                    }

                    if (this.getYA() + this.getHeightA() > this.parent.getHeightA() && !this.field20883) {
                        this.setYA(this.parent.getHeightA() - this.getHeightA());
                    }
                }
            }
        }

        if (this.method13216() && !var5) {
            this.timerUtils.stop();
            this.timerUtils.reset();
        }
    }

    @Override
    public boolean method13214() {
        return this.field20876;
    }

    @Override
    public void method13215(boolean var1) {
        this.field20876 = var1;
    }

    @Override
    public boolean method13216() {
        return this.field20877;
    }

    @Override
    public void method13217(boolean var1) {
        this.field20877 = var1;
        if (var1) {
            this.method13215(true);
            this.method13219();
        }
    }

    public AnimatedIconPanel method13218(Class6751 var1) {
        this.field20890.add(var1);
        return this;
    }

    public void method13219() {
        for (Class6751 var4 : this.field20890) {
            var4.method20580(this);
        }
    }
}
