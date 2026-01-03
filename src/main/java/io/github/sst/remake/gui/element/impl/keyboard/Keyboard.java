package io.github.sst.remake.gui.element.impl.keyboard;

import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.element.InteractiveWidget;
import io.github.sst.remake.util.client.bind.Keys;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.render.RenderUtils;

public class Keyboard extends InteractiveWidget {
    public int field20696;

    public Keyboard(GuiComponent var1, String var2, int var3, int var4) {
        super(var1, var2, var3, var4, 1060, 357, false);

        for (Keys key : Keys.values()) {
            Child var11;
            this.addToList(
                    var11 = new Child(
                            this,
                            "KEY_" + key.keycode + this.getChildren().size(),
                            key.getX(),
                            key.method9026(),
                            key.getY(),
                            key.method9029(),
                            key.name,
                            key.keycode
                    )
            );
            var11.onClick((var2x, var3x) -> {
                this.field20696 = var11.field20690;
                this.callUIHandlers();
            });
        }

        this.setListening(false);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton <= 1) {
            return super.onMouseDown(mouseX, mouseY, mouseButton);
        } else {
            this.field20696 = mouseButton;
            this.callUIHandlers();
            return false;
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        for (Keys var7 : Keys.values()) {
            if (var7.keycode == keyCode) {
                super.keyPressed(keyCode);
                return;
            }
        }

        this.field20696 = keyCode;
        this.callUIHandlers();
        super.keyPressed(keyCode);
    }

    public void method13104() {
        for (GuiComponent var4 : this.getChildren()) {
            if (var4 instanceof Child) {
                Child var5 = (Child) var4;
                var5.method13102();
            }
        }
    }

    public int[] method13105(int keycode) {
        for (Keys var7 : Keys.values()) {
            if (var7.keycode == keycode) {
                return new int[]{var7.getX() + var7.getY() / 2, var7.method9026() + var7.method9029()};
            }
        }

        return new int[]{this.getWidth() / 2, 20};
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        int var6 = this.x - 20;
        int var7 = this.y - 20;
        int var8 = this.width + 20 * 2;
        int var9 = this.height + 5 + 20 * 2;
        RenderUtils.drawRoundedRect((float) (var6 + 14 / 2), (float) (var7 + 14 / 2), (float) (var8 - 14), (float) (var9 - 14), 20.0F, partialTicks * 0.5F);
        RenderUtils.drawRoundedButton((float) var6, (float) var7, (float) var8, (float) var9, 14.0F, ClientColors.LIGHT_GREYISH_BLUE.getColor());
        super.draw(partialTicks);
    }
}
