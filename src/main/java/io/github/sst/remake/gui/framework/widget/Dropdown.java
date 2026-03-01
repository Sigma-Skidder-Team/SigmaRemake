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
    public List<String> options;
    public int selectedIndex;
    public boolean expanded;
    private final AnimationUtils expandAnimation = new AnimationUtils(220, 220);
    private final Map<Integer, DropdownMenu> subMenusByIndex = new HashMap<>();

    public Dropdown(GuiComponent parent, String id, int x, int y, int width, int height, List<String> options, int selectedIndex) {
        super(parent, id, x, y, width, height, DEFAULT_COLORS, false);
        this.options = options;
        this.selectedIndex = selectedIndex;
        this.initializeButtons();
    }

    public void addSubMenu(List<String> subOptions, int parentIndex) {
        DropdownMenu menu = new DropdownMenu(this, "sub" + parentIndex, this.width + 10, this.getHeight() * (parentIndex + 1), 200, this.getHeight(), subOptions, 0);
        this.subMenusByIndex.put(parentIndex, menu);
        menu.setSelfVisible(false);
        menu.onPress(interactiveWidget -> {
            this.setSelectedIndex(parentIndex);
            this.setExpanded(false);
            this.firePressHandlers();
        });
        this.addToList(menu);
    }

    public DropdownMenu getSubMenu(int parentIndex) {
        for (Entry<Integer, DropdownMenu> entry : this.subMenusByIndex.entrySet()) {
            if (entry.getKey() == parentIndex) {
                return entry.getValue();
            }
        }

        return null;
    }

    private void initializeButtons() {
        this.getChildren().clear();
        this.font = FontUtils.HELVETICA_LIGHT_18;

        Button toggleButton;
        this.addToList(toggleButton = new Button(this, "dropdownButton", 0, 0, this.getHeight(), this.getHeight(), this.textColor));
        toggleButton.addWidthSetter((widget, unused) -> {
            widget.setX(0);
            widget.setY(0);
            widget.setWidth(this.getWidth());
            widget.setHeight(this.getHeight());
        });
        toggleButton.onClick((parent, mouseButton) -> this.setExpanded(!this.isExpanded()));

        for (String option : this.options) {
            Button button = new Button(
                    this,
                    option,
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
                    option,
                    this.getFont()
            );
            button.setTextOffsetX(10);
            button.onClick((parent, mouseButton) -> {
                int previousIndex = this.getSelectedIndex();
                this.setSelectedIndex(this.options.indexOf(option));
                this.setExpanded(false);
                if (previousIndex != this.getSelectedIndex()) {
                    this.firePressHandlers();
                }
            });
            this.addToList(button);
        }

        this.expandAnimation.changeDirection(AnimationUtils.Direction.FORWARDS);
        this.accept(new GridLayoutVisitor(1));
    }

    private int getExpandedContentHeight() {
        int height = this.getAnimatedExpandHeight();
        for (DropdownMenu menu : this.subMenusByIndex.values()) {
            if (menu.isSelfVisible()) {
                height = Math.max(height, (menu.values.size() - 1) * menu.getHeight() + menu.getY());
            }
        }
        return height;
    }

    private int getAnimatedExpandHeight() {
        float percent = this.expandAnimation.calcPercent();
        float progress = AnimationUtils.easeOutCubic(percent, 0.0F, 1.0F, 1.0F);

        if (this.expandAnimation.getDirection() != AnimationUtils.Direction.BACKWARDS) {
            progress = QuadraticEasing.easeInQuad(percent, 0.0F, 1.0F, 1.0F);
        }

        return (int) ((float) (this.getHeight() * this.options.size() + 1) * progress);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        if (!this.isMouseOverComponent(mouseX, mouseY) && this.expandAnimation.getDirection() == AnimationUtils.Direction.BACKWARDS) {
            this.setExpanded(false);
        }

        int hoveredOptionIndex = (mouseY - this.getAbsoluteY()) / this.getHeight() - 1;
        boolean isMouseInBounds = mouseX - this.getAbsoluteX() < this.getWidth();

        if (hoveredOptionIndex >= 0 && hoveredOptionIndex < this.options.size()
                && this.expandAnimation.getDirection() == AnimationUtils.Direction.BACKWARDS
                && this.expandAnimation.calcPercent() == 1.0F && isMouseInBounds) {

            for (Entry<Integer, DropdownMenu> entry : this.subMenusByIndex.entrySet()) {
                entry.getValue().setSelfVisible(entry.getKey() == hoveredOptionIndex);
            }
        } else if (!this.isMouseOverComponent(mouseX, mouseY) || this.expandAnimation.getDirection() == AnimationUtils.Direction.FORWARDS) {
            for (DropdownMenu menu : this.subMenusByIndex.values()) {
                menu.setSelfVisible(false);
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        float animationProgress = partialTicks * this.expandAnimation.calcPercent();

        RenderUtils.drawRoundedRect((float) this.getX(), (float) this.getY(), (float) (this.getX() + this.getWidth()), (float) (this.getY() + this.getHeight()), ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), animationProgress));
        RenderUtils.drawRoundedRect((float) this.getX(), (float) this.getY(), (float) this.getWidth(), (float) (this.getHeight() + this.getAnimatedExpandHeight() - 1), 6.0F, partialTicks * 0.1F * this.expandAnimation.calcPercent());
        RenderUtils.drawRoundedRect((float) this.getX(), (float) this.getY(), (float) this.getWidth(), (float) (this.getHeight() + this.getAnimatedExpandHeight() - 1), 20.0F, partialTicks * 0.2F * this.expandAnimation.calcPercent());

        if (this.getText() != null) {
            ScissorUtils.startScissor(this);
            String subLabel = "";
            DropdownMenu activeSub = this.subMenusByIndex.get(this.selectedIndex);
            if (activeSub != null && !activeSub.values.isEmpty()) {
                subLabel = " (" + activeSub.values.get(activeSub.selectedIndex) + ")";
            }

            RenderUtils.drawString(this.getFont(), (float) (this.getX() + 10), (float) (this.getY() + (this.getHeight() - this.getFont().getHeight()) / 2 + 1), this.getText() + subLabel, ColorHelper.applyAlpha(this.textColor.getPrimaryColor(), partialTicks * 0.7F));
            ScissorUtils.restoreScissor();
        }

        // Animation clipping
        boolean isAnimating = this.expandAnimation.calcPercent() < 1.0F;
        if (isAnimating) {
            ScissorUtils.startScissorNoGL(this.getAbsoluteX(), this.getAbsoluteY(), this.getAbsoluteX() + this.getWidth() + 140, this.getAbsoluteY() + this.getHeight() + this.getExpandedContentHeight());
        }

        GL11.glPushMatrix();
        if (this.expandAnimation.calcPercent() > 0.0F) {
            super.draw(partialTicks);
        }
        GL11.glPopMatrix();

        if (isAnimating) {
            ScissorUtils.restoreScissor();
        }

        int iconX = this.getWidth() - (int) ((float) this.getHeight() / 2.0F + 0.5F);
        int iconY = (int) ((float) this.getHeight() / 2.0F + 0.5F) + 1;
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.getX() + iconX), (float) (this.getY() + iconY), 0.0F);
        GL11.glRotatef(90.0F * this.expandAnimation.calcPercent(), 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-this.getX() - iconX), (float) (-this.getY() - iconY), 0.0F);
        RenderUtils.drawString(this.font, (float) (this.getX() + iconX - 6), (float) (this.getY() + iconY - 14), ">", ColorHelper.applyAlpha(this.textColor.getPrimaryColor(), partialTicks * 0.7F * (!this.isMouseOverComponent(this.getMouseX(), this.getMouseY()) ? 0.5F : 1.0F)));
        GL11.glPopMatrix();
    }

    public List<String> getValues() {
        return this.options;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
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
        return this.getValues().size() <= 0 ? null : this.getValues().get(this.getSelectedIndex());
    }

    @Override
    public boolean isMouseOverComponent(int mouseX, int mouseY) {
        for (DropdownMenu menu : this.subMenusByIndex.values()) {
            if (menu.isSelfVisible() && menu.isMouseOverComponent(mouseX, mouseY)) return true;
        }
        int localX = mouseX - this.getAbsoluteX();
        int localY = mouseY - this.getAbsoluteY();
        return localX >= 0 && localX <= this.getWidth() && localY >= 0 && localY <= this.getHeight() + this.getAnimatedExpandHeight();
    }
}