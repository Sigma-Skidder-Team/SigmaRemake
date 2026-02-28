package io.github.sst.remake.gui.screen.keyboard;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.util.client.bind.Keys;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.render.RenderUtils;

public class Keyboard extends InteractiveWidget {
    public int selectedKeyCode;

    public Keyboard(GuiComponent parent, String id, int x, int y) {
        super(parent, id, x, y, 1060, 357, false);

        for (Keys key : Keys.values()) {
            KeyButton keyButton = new KeyButton(
                    this,
                    "KEY_" + key.keycode + this.getChildren().size(),
                    key.getX(),
                    key.getRowY(),
                    key.getY(),
                    key.getHeight(),
                    key.name,
                    key.keycode
            );
            this.addToList(keyButton);

            keyButton.onClick((clicked, mouseButton) -> {
                this.selectedKeyCode = keyButton.keyCode;
                this.firePressHandlers();
            });
        }

        this.setListening(false);
    }


    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton <= 1) {
            return super.onMouseDown(mouseX, mouseY, mouseButton);
        }

        this.selectedKeyCode = mouseButton;
        this.firePressHandlers();
        return false;
    }

    @Override
    public void keyPressed(int keyCode) {
        for (Keys key : Keys.values()) {
            if (key.keycode == keyCode) {
                super.keyPressed(keyCode);
                return;
            }
        }

        this.selectedKeyCode = keyCode;
        this.firePressHandlers();
        super.keyPressed(keyCode);
    }

    public void resetKeyButtonStates() {
        for (GuiComponent child : this.getChildren()) {
            if (child instanceof KeyButton) {
                ((KeyButton) child).refreshBoundState();
            }
        }
    }

    public int[] getKeyAnchorPosition(int keyCode) {
        for (Keys key : Keys.values()) {
            if (key.keycode == keyCode) {
                return new int[]{key.getX() + key.getY() / 2, key.getRowY() + key.getHeight()};
            }
        }

        return new int[]{this.getWidth() / 2, 20};
    }

    @Override
    public void draw(float partialTicks) {
        int bgX = this.x - 20;
        int bgY = this.y - 20;
        int bgWidth = this.width + 40;
        int bgHeight = this.height + 5 + 40;

        RenderUtils.drawRoundedRect(
                (float) (bgX + 14 / 2),
                (float) (bgY + 14 / 2),
                (float) (bgWidth - 14),
                (float) (bgHeight - 14),
                20.0F,
                partialTicks * 0.5F
        );
        RenderUtils.drawRoundedButton(
                (float) bgX,
                (float) bgY,
                (float) bgWidth,
                (float) bgHeight,
                14.0F,
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );

        super.draw(partialTicks);
    }
}
