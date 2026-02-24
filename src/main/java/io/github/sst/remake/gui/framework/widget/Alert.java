package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.gui.framework.widget.internal.AlertComponent;
import io.github.sst.remake.gui.framework.widget.internal.ComponentType;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
import net.minecraft.client.MinecraftClient;
import org.newdawn.slick.opengl.texture.Texture;
import org.newdawn.slick.util.image.BufferedImageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alert extends InteractiveWidget {
    private final AnimationUtils openCloseAnimation = new AnimationUtils(285, 100);
    private final GuiComponent screen;
    public Texture backgroundBlurTexture;
    public int contentWidth = 240;
    public int contentHeight = 0;
    private Button clickedButton;

    private final List<AlertCloseListener> closeListeners = new ArrayList<>();
    private Map<String, String> inputMap;

    public Alert(GuiComponent screen, String iconName, boolean var3, String name, AlertComponent... var5) {
        super(screen, iconName, 0, 0, MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight(), false);

        this.setHovered(false);
        this.setReAddChildren(false);
        this.defocusSiblings();

        TextField selectedField = null;
        TextField selectedField2 = null;

        for (AlertComponent var13 : var5) {
            this.contentHeight = this.contentHeight + var13.componentHeight + 10;
        }

        this.contentHeight -= 10;
        this.addToList(
                this.screen = new GuiComponent(
                        this, "modalContent", (this.width - this.contentWidth) / 2, (this.height - this.contentHeight) / 2, this.contentWidth, this.contentHeight
                )
        );
        int index = 0;
        int offset = 0;

        for (AlertComponent component : var5) {
            index++;
            if (component.componentType != ComponentType.FIRST_LINE) {
                if (component.componentType != ComponentType.SECOND_LINE) {
                    if (component.componentType != ComponentType.BUTTON) {
                        if (component.componentType == ComponentType.HEADER) {
                            this.screen.addToList(new Text(
                                            this.screen,
                                            "Item" + index,
                                            0,
                                            offset,
                                            this.contentWidth,
                                            component.componentHeight,
                                            new ColorHelper(
                                                    ClientColors.DEEP_TEAL.getColor(),
                                                    ClientColors.DEEP_TEAL.getColor(),
                                                    ClientColors.DEEP_TEAL.getColor()
                                            ),
                                            component.text,
                                            FontUtils.HELVETICA_LIGHT_36
                                    )
                            );
                        }
                    } else {
                        Button button;
                        this.screen.addToList(button = new Button(this.screen, "Item" + index, 0, offset, this.contentWidth, component.componentHeight, ColorHelper.DEFAULT_COLOR, component.text));
                        button.cornerRadius = 4;
                        button.onClick((parent, mouseButton) -> this.onButtonClick(button));
                    }
                } else {
                    TextField text;
                    this.screen
                            .addToList(
                                    text = new TextField(
                                            this.screen, "Item" + index, 0, offset, this.contentWidth, component.componentHeight, TextField.DEFAULT_COLORS, "", component.text
                                    )
                            );
                    if (!component.text.contains("Password")) {
                        if (component.text.contains("Email")) {
                            selectedField = text;
                        }
                    } else {
                        selectedField2 = text;
                        text.setCensorText(true);
                    }
                }
            } else {
                this.screen
                        .addToList(
                                new Text(
                                        this.screen,
                                        "Item" + index,
                                        0,
                                        offset,
                                        this.contentWidth,
                                        component.componentHeight,
                                        new ColorHelper(
                                                ClientColors.MID_GREY.getColor(), ClientColors.MID_GREY.getColor(), ClientColors.MID_GREY.getColor()
                                        ),
                                        component.text,
                                        FontUtils.HELVETICA_LIGHT_20
                                )
                        );
            }

            offset += component.componentHeight + 10;
        }

        if (selectedField != null && selectedField2 != null) {
            TextField var20 = selectedField2;
            selectedField.addChangeListener(var2x -> {
                String var5x = var2x.getText();
                if (var5x != null && var5x.contains(":")) {
                    String[] var6 = var5x.split(":");
                    if (var6.length <= 2) {
                        if (var6.length > 0) {
                            var2x.setText(var6[0].replace("\n", ""));
                            if (var6.length == 2) {
                                var20.setText(var6[1].replace("\n", ""));
                            }
                        }
                    } else {
                        this.onButtonClick();
                    }
                }
            });
        }
    }

    @Override
    public void setHovered(boolean hovered) {
        if (hovered) {
            for (GuiComponent var5 : this.screen.getChildren()) {
                if (var5 instanceof TextField) {
                    var5.setText("");
                    ((TextField) var5).resetTextOffset();
                }
            }
        }

        this.openCloseAnimation.changeDirection(!hovered ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        super.setHovered(hovered);
    }

    private Map<String, String> collectInputMap() {
        Map<String, String> var3 = new HashMap<>();

        for (GuiComponent var5 : this.screen.getChildren()) {
            Widget var6 = (Widget) var5;
            if (var6 instanceof TextField) {
                TextField var7 = (TextField) var6;
                var3.put(var7.getPlaceholder(), var7.getText());
            }
        }

        return var3;
    }

    public Map<String, String> getInputMap() {
        return this.inputMap;
    }

    public Button getClickedButton() {
        return this.clickedButton;
    }

    public void onButtonClick(Button button) {
        this.clickedButton = button;
        this.inputMap = this.collectInputMap();
        this.setOpen(false);
        this.callUIHandlers();
    }

    public void onButtonClick() {
        this.clickedButton = null;
        this.inputMap = this.collectInputMap();
        this.setOpen(false);
        this.callUIHandlers();
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClick(mouseX, mouseY, mouseButton);
    }

    public float calcOpenScale(float progress, float period) {
        return this.openCloseAnimation.getDirection() != AnimationUtils.Direction.FORWARDS
                ? (float) (Math.pow(2.0, -10.0F * progress) * Math.sin((double) (progress - period / 4.0F) * (Math.PI * 2) / (double) period) + 1.0)
                : 0.5F + QuadraticEasing.easeOutQuad(progress, 0.0F, 1.0F, 1.0F) * 0.5F;
    }

    @Override
    public void draw(float partialTicks) {
        if (this.openCloseAnimation.calcPercent() != 0.0F) {
            int var4 = this.contentWidth + 60;
            int var5 = this.contentHeight + 60;
            float var7 = !this.isHovered() ? this.openCloseAnimation.calcPercent() : Math.min(this.openCloseAnimation.calcPercent() / 0.25F, 1.0F);
            float var8 = this.calcOpenScale(this.openCloseAnimation.calcPercent(), 1.0F);
            var4 = (int) ((float) var4 * var8);
            var5 = (int) ((float) var5 * var8);
            RenderUtils.drawTexture(
                    -5.0F,
                    -5.0F,
                    (float) (this.getWidth() + 10),
                    (float) (this.getHeight() + 10),
                    this.backgroundBlurTexture,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var7)
            );
            RenderUtils.drawRoundedRect(
                    0.0F, 0.0F, (float) this.getWidth(), (float) this.getHeight(), ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F * var7)
            );
            if (var4 > 0) {
                RenderUtils.drawFloatingPanel(
                        (this.width - var4) / 2, (this.height - var5) / 2, var4, var5, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var7)
                );
            }

            super.setScale(var8, var8);
            super.applyScaleTransforms();
            super.draw(var7);
        } else {
            if (this.isFocused()) {
                this.setFocused(false);
                this.setSelfVisible(false);
                this.defocusSiblings();
            }
        }
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseDown(mouseX, mouseY, mouseButton)) {
            int var6 = this.contentWidth + 60;
            int var7 = this.contentHeight + 60;
            if (mouseX <= (this.width - var6) / 2
                    || mouseX >= (this.width - var6) / 2 + var6
                    || mouseY <= (this.height - var7) / 2
                    || mouseY >= (this.height - var7) / 2 + var7) {
                this.setOpen(false);
            }
            return false;
        } else {
            return true;
        }
    }

    public void setOpen(boolean open) {
        if (open && !this.isHovered()) {
            try {
                if (this.backgroundBlurTexture != null) {
                    this.backgroundBlurTexture.release();
                }

                this.backgroundBlurTexture = BufferedImageUtil.getTexture(
                        "blur", ImageUtils.captureFramebufferRegion(0, 0, this.getWidth(), this.getHeight(), 5, 10, ClientColors.LIGHT_GREYISH_BLUE.getColor(), true)
                );
            } catch (IOException e) {
                Client.LOGGER.error(e.getMessage());
            }
        }

        if (this.isHovered() != open && !open) {
            this.notifyCloseListeners();
        }

        this.setHovered(open);
        if (open) {
            this.setSelfVisible(true);
        }

        this.setReAddChildren(open);
    }

    public void addCloseListener(AlertCloseListener listener) {
        this.closeListeners.add(listener);
    }

    public void notifyCloseListeners() {
        for (AlertCloseListener listener : this.closeListeners) {
            listener.onClose(this);
        }
    }

    public interface AlertCloseListener {
        void onClose(InteractiveWidget widget);
    }
}