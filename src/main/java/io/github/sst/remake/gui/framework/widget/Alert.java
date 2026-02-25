package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.gui.framework.widget.internal.AlertComponent;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
import net.minecraft.client.MinecraftClient;
import org.newdawn.slick.opengl.texture.Texture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alert extends InteractiveWidget {
    private static final int CONTENT_WIDTH = 240;
    private static final int COMPONENT_SPACING = 10;
    private static final int PANEL_PADDING = 60;
    private final AnimationUtils openCloseAnimation = new AnimationUtils(285, 100);
    private final GuiComponent modalContent;
    public Texture backgroundBlurTexture;
    private String blurTextureId;
    public int contentWidth = CONTENT_WIDTH;
    public int contentHeight = 0;
    private Button clickedButton;

    private final List<AlertCloseListener> closeListeners = new ArrayList<>();
    private Map<String, String> inputMap;

    public Alert(GuiComponent screen, String iconName, AlertComponent... components) {
        super(
                screen,
                iconName,
                0,
                0,
                MinecraftClient.getInstance().getWindow().getWidth(),
                MinecraftClient.getInstance().getWindow().getHeight(),
                false
        );

        this.setHovered(false);
        this.setReAddChildren(false);
        this.defocusSiblings();

        TextField emailField = null;
        TextField passwordField = null;

        for (AlertComponent component : components) {
            this.contentHeight += component.height + COMPONENT_SPACING;
        }
        this.contentHeight -= COMPONENT_SPACING;

        this.modalContent = new GuiComponent(
                this,
                "modalContent",
                (this.width - this.contentWidth) / 2,
                (this.height - this.contentHeight) / 2,
                this.contentWidth,
                this.contentHeight
        );
        this.addToList(this.modalContent);

        int itemIndex = 0;
        int yOffset = 0;

        for (AlertComponent component : components) {
            itemIndex++;

            switch (component.type) {
                case FIRST_LINE:
                    this.modalContent.addToList(new Text(
                            this.modalContent,
                            "Item" + itemIndex,
                            0,
                            yOffset,
                            this.contentWidth,
                            component.height,
                            new ColorHelper(
                                    ClientColors.MID_GREY.getColor(),
                                    ClientColors.MID_GREY.getColor(),
                                    ClientColors.MID_GREY.getColor()
                            ),
                            component.text,
                            FontUtils.HELVETICA_LIGHT_20
                    ));
                    break;

                case SECOND_LINE: {
                    TextField field = new TextField(
                            this.modalContent,
                            "Item" + itemIndex,
                            0,
                            yOffset,
                            this.contentWidth,
                            component.height,
                            TextField.DEFAULT_COLORS,
                            "",
                            component.text
                    );
                    this.modalContent.addToList(field);

                    // heuristic mapping based on placeholder content.
                    if (component.text.contains("Password")) {
                        passwordField = field;
                        field.setCensorText(true);
                    } else if (component.text.contains("Email")) {
                        emailField = field;
                    }
                    break;
                }

                case BUTTON: {
                    Button button = new Button(
                            this.modalContent,
                            "Item" + itemIndex,
                            0,
                            yOffset,
                            this.contentWidth,
                            component.height,
                            ColorHelper.DEFAULT_COLOR,
                            component.text
                    );
                    button.cornerRadius = 4;
                    button.onClick((parent, mouseButton) -> this.onButtonClick(button));
                    this.modalContent.addToList(button);
                    break;
                }

                case HEADER:
                    this.modalContent.addToList(new Text(
                            this.modalContent,
                            "Item" + itemIndex,
                            0,
                            yOffset,
                            this.contentWidth,
                            component.height,
                            new ColorHelper(
                                    ClientColors.DEEP_TEAL.getColor(),
                                    ClientColors.DEEP_TEAL.getColor(),
                                    ClientColors.DEEP_TEAL.getColor()
                            ),
                            component.text,
                            FontUtils.HELVETICA_LIGHT_36
                    ));
                    break;

                default:
                    break;
            }

            yOffset += component.height + COMPONENT_SPACING;
        }

        // allow "email:password" paste into the email field to autofill password.
        if (emailField != null && passwordField != null) {
            final TextField targetPasswordField = passwordField;
            emailField.addChangeListener(changedField -> {
                String text = changedField.getText();
                if (text == null || !text.contains(":")) {
                    return;
                }

                String[] parts = text.split(":");
                if (parts.length > 2) {
                    this.onButtonClick(); // cancel/close on malformed input, matches original behavior
                    return;
                }

                if (parts.length > 0) {
                    changedField.setText(parts[0].replace("\n", ""));
                    if (parts.length == 2) {
                        targetPasswordField.setText(parts[1].replace("\n", ""));
                    }
                }
            });
        }
    }

    @Override
    public void setHovered(boolean open) {
        if (open) {
            for (GuiComponent child : this.modalContent.getChildren()) {
                if (child instanceof TextField) {
                    child.setText("");
                    ((TextField) child).resetTextOffset();
                }
            }
        }

        this.openCloseAnimation.changeDirection(!open
                ? AnimationUtils.Direction.FORWARDS
                : AnimationUtils.Direction.BACKWARDS
        );

        super.setHovered(open);
    }

    private Map<String, String> collectInputMap() {
        Map<String, String> valuesByPlaceholder = new HashMap<>();

        for (GuiComponent child : this.modalContent.getChildren()) {
            Widget widget = (Widget) child;
            if (widget instanceof TextField) {
                TextField field = (TextField) widget;
                valuesByPlaceholder.put(field.getPlaceholder(), field.getText());
            }
        }

        return valuesByPlaceholder;
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
        this.firePressHandlers();
    }

    public void onButtonClick() {
        this.clickedButton = null;
        this.inputMap = this.collectInputMap();
        this.setOpen(false);
        this.firePressHandlers();
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        super.onMouseClick(mouseX, mouseY, mouseButton);
    }

    public float calcOpenScale(float progress, float period) {
        return this.openCloseAnimation.getDirection() != AnimationUtils.Direction.FORWARDS
                ? (float) (Math.pow(2.0, -10.0F * progress)
                * Math.sin((double) (progress - period / 4.0F) * (Math.PI * 2) / (double) period) + 1.0)
                : 0.5F + QuadraticEasing.easeOutQuad(progress, 0.0F, 1.0F, 1.0F) * 0.5F;
    }

    @Override
    public void draw(float partialTicks) {
        if (this.openCloseAnimation.calcPercent() != 0.0F) {
            int panelWidth = this.contentWidth + PANEL_PADDING;
            int panelHeight = this.contentHeight + PANEL_PADDING;

            float alpha = !this.isHovered()
                    ? this.openCloseAnimation.calcPercent()
                    : Math.min(this.openCloseAnimation.calcPercent() / 0.25F, 1.0F);

            float scale = this.calcOpenScale(this.openCloseAnimation.calcPercent(), 1.0F);

            panelWidth = (int) ((float) panelWidth * scale);
            panelHeight = (int) ((float) panelHeight * scale);

            RenderUtils.drawTexture(
                    -5.0F,
                    -5.0F,
                    (float) (this.getWidth() + 10),
                    (float) (this.getHeight() + 10),
                    this.backgroundBlurTexture,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha)
            );

            RenderUtils.drawRoundedRect(
                    0.0F,
                    0.0F,
                    (float) this.getWidth(),
                    (float) this.getHeight(),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.1F * alpha)
            );

            if (panelWidth > 0) {
                RenderUtils.drawFloatingPanel(
                        (this.width - panelWidth) / 2,
                        (this.height - panelHeight) / 2,
                        panelWidth,
                        panelHeight,
                        ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha)
                );
            }

            super.setScale(scale, scale);
            super.applyScaleTransforms();
            super.draw(alpha);
            return;
        }

        if (this.isFocused()) {
            this.setFocused(false);
            this.setSelfVisible(false);
            this.defocusSiblings();
        }
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseDown(mouseX, mouseY, mouseButton)) {
            int panelWidth = this.contentWidth + PANEL_PADDING;
            int panelHeight = this.contentHeight + PANEL_PADDING;

            int left = (this.width - panelWidth) / 2;
            int top = (this.height - panelHeight) / 2;

            boolean clickedOutsidePanel =
                    mouseX <= left
                            || mouseX >= left + panelWidth
                            || mouseY <= top
                            || mouseY >= top + panelHeight;

            if (clickedOutsidePanel) {
                this.setOpen(false);
            }

            return false;
        }

        return true;
    }

    public void setOpen(boolean open) {
        if (open && !this.isHovered()) {
            try {
                if (this.backgroundBlurTexture != null) {
                    this.backgroundBlurTexture.release();
                }

                this.backgroundBlurTexture = ImageUtils.createTexture(
                        this.getBlurTextureId(),
                        ImageUtils.captureFramebufferRegion(
                                0,
                                0,
                                this.getWidth(),
                                this.getHeight(),
                                5,
                                10,
                                ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                                true
                        )
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

    private String getBlurTextureId() {
        if (this.blurTextureId == null) {
            this.blurTextureId = "alert-blur-" + System.nanoTime();
        }
        return this.blurTextureId;
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
