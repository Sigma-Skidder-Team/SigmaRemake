package io.github.sst.remake.gui.element.impl;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.gui.element.impl.drop.Class7262;
import io.github.sst.remake.gui.element.impl.drop.Sub;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.image.ResourceRegistry;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Dropdown extends Element {
    public static final ColorHelper field21325 = new ColorHelper(1250067, -15329770).setTextColor(ClientColors.DEEP_TEAL.getColor()).setHeightAlignment(FontAlignment.CENTER);
    public List<String> values;
    public int selectedIdx;
    public boolean field21328;
    private final AnimationUtils animation = new AnimationUtils(220, 220);
    private final Map<Integer, Sub> field21331 = new HashMap<Integer, Sub>();

    public Dropdown(CustomGuiScreen var1, String typeThingIdk, int x, int y, int width, int height, List<String> values, int selectedIdx) {
        super(var1, typeThingIdk, x, y, width, height, field21325, false);
        this.values = values;
        this.selectedIdx = selectedIdx;
        this.addButtons();
    }

    public void method13643(List<String> var1, int var2) {
        Sub var5 = new Sub(this, "sub" + var2, this.width + 10, this.getHeight() * (var2 + 1), 200, this.getHeight(), var1, 0);
        this.field21331.put(var2, var5);
        var5.setSelfVisible(false);
        var5.onPress(var2x -> {
            this.method13656(var2);
            this.method13658(false);
            this.callUIHandlers();
        });
        this.addToList(var5);
    }

    public Sub method13645(int var1) {
        for (Entry var5 : this.field21331.entrySet()) {
            if ((Integer) var5.getKey() == var1) {
                return (Sub) var5.getValue();
            }
        }

        return null;
    }

    private void addButtons() {
        this.getChildren().clear();
        this.font = ResourceRegistry.JelloLightFont18;
        Button dropdownButton;
        this.addToList(dropdownButton = new Button(this, "dropdownButton", 0, 0, this.getHeight(), this.getHeight(), this.textColor));
        dropdownButton.setSize((var1, var2) -> {
            var1.setX(0);
            var1.setY(0);
            var1.setWidth(this.getWidth());
            var1.setHeight(this.getHeight());
        });
        dropdownButton.onClick((var1, var2) -> this.method13658(!this.method13657()));

        for (String mode : this.values) {
            Button button;
            this.addToList(
                    button = new Button(
                            this,
                            mode,
                            0,
                            this.getHeight(),
                            this.getWidth(),
                            this.getHeight(),
                            new ColorHelper(
                                    ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                                    -1381654,
                                    this.textColor.getPrimaryColor(),
                                    this.textColor.getPrimaryColor(),
                                    FontAlignment.LEFT,
                                    FontAlignment.CENTER
                            ),
                            mode,
                            this.getFont()
                    )
            );
            button.method13034(10);
            button.onClick((var2, var3x) -> {
                int var6x = this.getIndex();
                this.method13656(this.values.indexOf(mode));
                this.method13658(false);
                if (var6x != this.getIndex()) {
                    this.callUIHandlers();
                }
            });
        }

        this.animation.changeDirection(AnimationUtils.Direction.BACKWARDS);
        this.accept(new Class7262(1));
    }

    private int method13647() {
        int var3 = this.method13648();

        for (Entry var5 : this.field21331.entrySet()) {
            if (((Sub) var5.getValue()).isSelfVisible()) {
                var3 = Math.max(
                        var3,
                        (((Sub) var5.getValue()).values.size() - 1) * ((Sub) var5.getValue()).getHeight() + ((Sub) var5.getValue()).getY()
                );
            }
        }

        return var3;
    }

    private int method13648() {
        float var3 = AnimationUtils.calculateTransition(this.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.animation.getDirection() != AnimationUtils.Direction.FORWARDS) {
            var3 = QuadraticEasing.easeInQuad(this.animation.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        return (int) ((float) (this.getHeight() * this.values.size() + 1) * var3);
    }

    public int method13649() {
        return (int) ((float) (this.getHeight() * this.values.size() + 1));
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        if (!this.method13114(mouseX, mouseY) && this.animation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            this.method13658(false);
        }

        int var5 = (mouseY - this.method13272()) / this.getHeight() - 1;
        if (var5 >= 0
                && var5 < this.values.size()
                && this.animation.getDirection() == AnimationUtils.Direction.FORWARDS
                && this.animation.calcPercent() == 1.0F
                && mouseX - this.method13271() < this.getWidth()) {
            for (Entry var9 : this.field21331.entrySet()) {
                ((Sub) var9.getValue()).setSelfVisible((Integer) var9.getKey() == var5);
            }
        } else if (!this.method13114(mouseX, mouseY) || this.animation.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            for (Entry var7 : this.field21331.entrySet()) {
                ((Sub) var7.getValue()).setSelfVisible(false);
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) (this.getX() + this.getWidth()),
                (float) (this.getY() + this.getHeight()),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * this.animation.calcPercent())
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth(),
                (float) (this.getHeight() + this.method13648() - 1),
                6.0F,
                partialTicks * 0.1F * this.animation.calcPercent()
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth(),
                (float) (this.getHeight() + this.method13648() - 1),
                20.0F,
                partialTicks * 0.2F * this.animation.calcPercent()
        );
        if (this.getText() != null) {
            ScissorUtils.startScissor(this);
            String var4 = "";

            for (Entry var6 : this.field21331.entrySet()) {
                if (this.selectedIdx == (Integer) var6.getKey()) {
                    var4 = " (" + ((Sub) var6.getValue()).values.get(((Sub) var6.getValue()).field21324) + ")";
                }
            }

            RenderUtils.drawString(
                    this.getFont(),
                    (float) (this.getX() + 10),
                    (float) (this.getY() + (this.getHeight() - this.getFont().getHeight()) / 2 + 1),
                    this.getText() + var4,
                    ColorHelper.applyAlpha(this.textColor.getPrimaryColor(), partialTicks * 0.7F)
            );
            ScissorUtils.restoreScissor();
        }

        boolean var8 = this.animation.calcPercent() < 1.0F;
        if (var8) {
            ScissorUtils.startScissor(
                    this.method13271(), this.method13272(), this.method13271() + this.getWidth() + 140, this.method13272() + this.getHeight() + this.method13647()
            );
        }

        GL11.glPushMatrix();
        if (this.animation.calcPercent() > 0.0F) {
            super.draw(partialTicks);
        }

        GL11.glPopMatrix();
        if (var8) {
            ScissorUtils.restoreScissor();
        }

        int var9 = this.getWidth() - (int) ((float) this.getHeight() / 2.0F + 0.5F);
        int var10 = (int) ((float) this.getHeight() / 2.0F + 0.5F) + 1;
        int var7 = (int) ((float) this.getHeight() / 6.0F + 0.5F);
        GL11.glTranslatef((float) (this.getX() + var9), (float) (this.getY() + var10), 0.0F);
        GL11.glRotatef(90.0F * this.animation.calcPercent(), 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-this.getX() - var9), (float) (-this.getY() - var10), 0.0F);
        RenderUtils.drawString(
                this.font,
                (float) (this.getX() + var9 - 6),
                (float) (this.getY() + var10 - 14),
                ">",
                ColorHelper.applyAlpha(this.textColor.getPrimaryColor(), partialTicks * 0.7F * (!this.method13114(this.getMouseX(), this.getMouseY()) ? 0.5F : 1.0F))
        );
    }

    public List<String> method13651() {
        return this.values;
    }

    public void method13652(String var1, int var2) {
        this.method13651().add(var2, var1);
        this.addButtons();
    }

    public int getIndex() {
        return this.selectedIdx;
    }

    public void method13656(int var1) {
        this.selectedIdx = var1;
    }

    public boolean method13657() {
        return this.field21328;
    }

    public void method13658(boolean var1) {
        this.field21328 = var1;
        this.animation.changeDirection(!this.method13657() ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
    }

    @Override
    public String getText() {
        return this.method13651().size() <= 0 ? null : this.method13651().get(this.getIndex());
    }

    @Override
    public boolean method13114(int mouseX, int mouseY) {
        for (Entry var6 : this.field21331.entrySet()) {
            if (((Sub) var6.getValue()).isSelfVisible() && ((Sub) var6.getValue()).method13114(mouseX, mouseY)) {
                return true;
            }
        }

        mouseX -= this.method13271();
        mouseY -= this.method13272();
        return mouseX >= 0 && mouseX <= this.getWidth() && mouseY >= 0 && mouseY <= this.getHeight() + this.method13648();
    }
}
