package io.github.sst.remake.gui.framework.widget.internal;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.layout.GridLayoutVisitor;
import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class DropdownMenu extends InteractiveWidget {
    public static final ColorHelper color = new ColorHelper(1250067, -15329770).setTextColor(ClientColors.DEEP_TEAL.getColor()).setHeightAlignment(FontAlignment.CENTER);
    public List<String> values;
    public int selectedIndex;

    public DropdownMenu(GuiComponent screen, String iconName, int x, int y, int width, int height, List<String> values, int selectedIndex) {
        super(screen, iconName, x, y, width, height, color, false);
        this.values = values;
        this.selectedIndex = selectedIndex;
        this.rebuildButtons();
    }

    private void rebuildButtons() {
        this.getChildren().clear();
        this.font = FontUtils.HELVETICA_LIGHT_18;

        for (String value : this.values) {
            Button button;
            this.addToList(button = new Button(
                            this,
                            value,
                            0,
                            0,
                            this.getWidth(),
                            this.getHeight(),
                            new ColorHelper(
                                    ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                                    -1381654,
                                    this.textColor.getPrimaryColor(),
                                    FontAlignment.LEFT,
                                    FontAlignment.CENTER
                            ),
                            value,
                            this.getFont()
                    )
            );
            button.setTextOffsetX(10);
            button.onClick((mouseX, mouseY) -> {
                this.setSelectedIndex(this.values.indexOf(value));
                this.firePressHandlers();
            });
        }

        this.accept(new GridLayoutVisitor(1));
    }

    private int getExpandedHeight() {
        return this.getHeight() * (this.values.size() - 1);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) (this.getX() + this.getWidth()),
                (float) (this.getY() + this.getHeight() + this.getExpandedHeight()),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth(),
                (float) (this.getHeight() + this.getExpandedHeight() - 1),
                6.0F,
                partialTicks * 0.1F
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth(),
                (float) (this.getHeight() + this.getExpandedHeight() - 1),
                20.0F,
                partialTicks * 0.2F
        );
        GL11.glPushMatrix();
        super.draw(partialTicks);
        GL11.glPopMatrix();
    }

    public List<String> getValues() {
        return this.values;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
    }

    @Override
    public String getText() {
        return this.getValues().size() <= 0 ? null : this.getValues().get(this.getSelectedIndex());
    }

    @Override
    public boolean isMouseOverComponent(int mouseX, int mouseY) {
        mouseX -= this.getAbsoluteX();
        mouseY -= this.getAbsoluteY();
        return mouseX >= -10 && mouseX <= this.getWidth() && mouseY >= 0 && mouseY <= this.getHeight() + this.getExpandedHeight();
    }
}
