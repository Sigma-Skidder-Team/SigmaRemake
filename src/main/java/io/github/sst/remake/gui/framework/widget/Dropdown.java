package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.layout.GridLayoutVisitor;
import io.github.sst.remake.gui.framework.widget.internal.DropdownMenu;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Dropdown extends InteractiveWidget {
    public static final ColorHelper DEFAULT_COLORS = new ColorHelper(1250067, -15329770).setTextColor(ClientColors.DEEP_TEAL.getColor()).setHeightAlignment(FontAlignment.CENTER);
    public List<String> values;
    public int selectedIndex;
    public boolean expanded;
    private final AnimationUtils expandAnimation = new AnimationUtils(220, 220);
    private final Map<Integer, DropdownMenu> subMenusByIndex = new HashMap<>();

    public Dropdown(GuiComponent var1, String typeThingIdk, int x, int y, int width, int height, List<String> values, int selectedIndex) {
        super(var1, typeThingIdk, x, y, width, height, DEFAULT_COLORS, false);
        this.values = values;
        this.selectedIndex = selectedIndex;
        this.addButtons();
    }

    public void addSubMenu(List<String> values, int parentIndex) {
        DropdownMenu var5 = new DropdownMenu(this, "sub" + parentIndex, this.width + 10, this.getHeight() * (parentIndex + 1), 200, this.getHeight(), values, 0);
        this.subMenusByIndex.put(parentIndex, var5);
        var5.setSelfVisible(false);
        var5.onPress(interactiveWidget -> {
            this.setIndex(parentIndex);
            this.setExpanded(false);
            this.firePressHandlers();
        });
        this.addToList(var5);
    }

    public DropdownMenu getSubMenu(int parentIndex) {
        for (Entry<Integer, DropdownMenu> entry : this.subMenusByIndex.entrySet()) {
            if (entry.getKey() == parentIndex) {
                return entry.getValue();
            }
        }

        return null;
    }

    private void addButtons() {
        this.getChildren().clear();
        this.font = FontUtils.HELVETICA_LIGHT_18;
        Button dropdownButton;
        this.addToList(dropdownButton = new Button(this, "dropdownButton", 0, 0, this.getHeight(), this.getHeight(), this.textColor));
        dropdownButton.addWidthSetter((var1, var2) -> {
            var1.setX(0);
            var1.setY(0);
            var1.setWidth(this.getWidth());
            var1.setHeight(this.getHeight());
        });
        dropdownButton.onClick((parent, mouseButton) -> this.setExpanded(!this.isExpanded()));

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
                                    FontAlignment.LEFT,
                                    FontAlignment.CENTER
                            ),
                            mode,
                            this.getFont()
                    )
            );
            button.setTextOffsetX(10);
            button.onClick((parent, mouseButton) -> {
                int var6x = this.getIndex();
                this.setIndex(this.values.indexOf(mode));
                this.setExpanded(false);
                if (var6x != this.getIndex()) {
                    this.firePressHandlers();
                }
            });
        }

        this.expandAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        this.accept(new GridLayoutVisitor(1));
    }

    private int getExpandedContentHeight() {
        int height = this.getAnimatedExpandHeight();

        for (Entry<Integer, DropdownMenu> entry : this.subMenusByIndex.entrySet()) {
            if (entry.getValue().isSelfVisible()) {
                height = Math.max(
                        height,
                        (((DropdownMenu) entry.getValue()).values.size() - 1) * entry.getValue().getHeight() + entry.getValue().getY()
                );
            }
        }

        return height;
    }

    private int getAnimatedExpandHeight() {
        float var3 = AnimationUtils.easeOutCubic(this.expandAnimation.calcPercent(), 0.0F, 1.0F, 1.0F);
        if (this.expandAnimation.getDirection() != AnimationUtils.Direction.BACKWARDS) {
            var3 = QuadraticEasing.easeInQuad(this.expandAnimation.calcPercent(), 0.0F, 1.0F, 1.0F);
        }

        return (int) ((float) (this.getHeight() * this.values.size() + 1) * var3);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        if (!this.isMouseOverComponent(mouseX, mouseY) && this.expandAnimation.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            this.setExpanded(false);
        }

        int var5 = (mouseY - this.getAbsoluteY()) / this.getHeight() - 1;
        if (var5 >= 0
                && var5 < this.values.size()
                && this.expandAnimation.getDirection() == AnimationUtils.Direction.BACKWARDS
                && this.expandAnimation.calcPercent() == 1.0F
                && mouseX - this.getAbsoluteX() < this.getWidth()) {
            for (Entry<Integer, DropdownMenu> var9 : this.subMenusByIndex.entrySet()) {
                var9.getValue().setSelfVisible(var9.getKey() == var5);
            }
        } else if (!this.isMouseOverComponent(mouseX, mouseY) || this.expandAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            for (Entry<Integer, DropdownMenu> var7 : this.subMenusByIndex.entrySet()) {
                var7.getValue().setSelfVisible(false);
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
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * this.expandAnimation.calcPercent())
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth(),
                (float) (this.getHeight() + this.getAnimatedExpandHeight() - 1),
                6.0F,
                partialTicks * 0.1F * this.expandAnimation.calcPercent()
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth(),
                (float) (this.getHeight() + this.getAnimatedExpandHeight() - 1),
                20.0F,
                partialTicks * 0.2F * this.expandAnimation.calcPercent()
        );
        if (this.getText() != null) {
            ScissorUtils.startScissor(this);
            String var4 = "";

            for (Entry<Integer, DropdownMenu> var6 : this.subMenusByIndex.entrySet()) {
                if (this.selectedIndex == var6.getKey()) {
                    DropdownMenu sub = var6.getValue();
                    if (!sub.values.isEmpty()) {
                        var4 = " (" + sub.values.get(sub.selectedIndex) + ")";
                    }
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

        boolean var8 = this.expandAnimation.calcPercent() < 1.0F;
        if (var8) {
            ScissorUtils.startScissorNoGL(
                    this.getAbsoluteX(), this.getAbsoluteY(), this.getAbsoluteX() + this.getWidth() + 140, this.getAbsoluteY() + this.getHeight() + this.getExpandedContentHeight()
            );
        }

        GL11.glPushMatrix();
        if (this.expandAnimation.calcPercent() > 0.0F) {
            super.draw(partialTicks);
        }

        GL11.glPopMatrix();
        if (var8) {
            ScissorUtils.restoreScissor();
        }

        int var9 = this.getWidth() - (int) ((float) this.getHeight() / 2.0F + 0.5F);
        int var10 = (int) ((float) this.getHeight() / 2.0F + 0.5F) + 1;

        GL11.glTranslatef((float) (this.getX() + var9), (float) (this.getY() + var10), 0.0F);
        GL11.glRotatef(90.0F * this.expandAnimation.calcPercent(), 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-this.getX() - var9), (float) (-this.getY() - var10), 0.0F);

        RenderUtils.drawString(
                this.font,
                (float) (this.getX() + var9 - 6),
                (float) (this.getY() + var10 - 14),
                ">",
                ColorHelper.applyAlpha(this.textColor.getPrimaryColor(), partialTicks * 0.7F * (!this.isMouseOverComponent(this.getMouseX(), this.getMouseY()) ? 0.5F : 1.0F))
        );
    }

    public List<String> getValues() {
        return this.values;
    }

    public int getIndex() {
        return this.selectedIndex;
    }

    public void setIndex(int var1) {
        this.selectedIndex = var1;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        this.expandAnimation.changeDirection(!this.isExpanded() ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
    }

    @Override
    public String getText() {
        return this.getValues().size() <= 0 ? null : this.getValues().get(this.getIndex());
    }

    @Override
    public boolean isMouseOverComponent(int mouseX, int mouseY) {
        for (Entry<Integer, DropdownMenu> var6 : this.subMenusByIndex.entrySet()) {
            if (var6.getValue().isSelfVisible() && var6.getValue().isMouseOverComponent(mouseX, mouseY)) {
                return true;
            }
        }

        mouseX -= this.getAbsoluteX();
        mouseY -= this.getAbsoluteY();
        return mouseX >= 0 && mouseX <= this.getWidth() && mouseY >= 0 && mouseY <= this.getHeight() + this.getAnimatedExpandHeight();
    }
}
