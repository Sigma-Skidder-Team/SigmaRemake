package io.github.sst.remake.gui.element.impl.drop;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.gui.element.impl.Button;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.image.ResourceRegistry;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class Sub extends Element {
    public static final ColorHelper color = new ColorHelper(1250067, -15329770).setTextColor(ClientColors.DEEP_TEAL.getColor()).setHeightAlignment(FontAlignment.CENTER);
    public List<String> values;
    public int field21324 = 0;

    public Sub(CustomGuiScreen screen, String iconName, int x, int y, int width, int height, List<String> values, int var8) {
        super(screen, iconName, x, y, width, height, color, false);
        this.values = values;
        this.field21324 = var8;
        this.method13634();
    }

    private void method13634() {
        this.getChildren().clear();
        this.font = ResourceRegistry.JelloLightFont18;

        for (String value : this.values) {
            Button button;
            this.addToList(
                    button = new Button(
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
                                    this.textColor.getPrimaryColor(),
                                    FontAlignment.LEFT,
                                    FontAlignment.CENTER
                            ),
                            value,
                            this.getFont()
                    )
            );
            button.method13034(10);
            button.onClick((var2, var3) -> {
                this.method13641(this.values.indexOf(value));
                this.callUIHandlers();
            });
        }

        this.accept(new Class7262(1));
    }

    private int method13635() {
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
                (float) (this.getY() + this.getHeight() + this.method13635()),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth(),
                (float) (this.getHeight() + this.method13635() - 1),
                6.0F,
                partialTicks * 0.1F
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) this.getWidth(),
                (float) (this.getHeight() + this.method13635() - 1),
                20.0F,
                partialTicks * 0.2F
        );
        GL11.glPushMatrix();
        super.draw(partialTicks);
        GL11.glPopMatrix();
    }

    public List<String> method13636() {
        return this.values;
    }

    public void method13637(String var1, int var2) {
        this.method13636().add(var2, var1);
        this.method13634();
    }

    public void method13638(String var1) {
        this.method13637(var1, this.values.size());
    }

    public <E extends Enum<E>> void method13639(Class<E> var1) {
        this.values.clear();

        for (Enum var7 : var1.getEnumConstants()) {
            String var8 = var7.toString().substring(0, 1).toUpperCase() + var7.toString().substring(1).toLowerCase();
            this.method13637(var8, var7.ordinal());
        }
    }

    public int method13640() {
        return this.field21324;
    }

    public void method13641(int var1) {
        this.field21324 = var1;
    }

    @Override
    public String getText() {
        return this.method13636().size() <= 0 ? null : this.method13636().get(this.method13640());
    }

    @Override
    public boolean method13114(int mouseX, int mouseY) {
        mouseX -= this.method13271();
        mouseY -= this.method13272();
        return mouseX >= -10 && mouseX <= this.getWidth() && mouseY >= 0 && mouseY <= this.getHeight() + this.method13635();
    }
}
