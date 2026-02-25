package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.java.StringUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.math.timer.TogglableTimer;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;

public class TextField extends Widget implements IMinecraft {
    public static final ColorHelper DEFAULT_COLORS = new ColorHelper(
            -892679478,
            -892679478,
            ClientColors.DEEP_TEAL.getColor(),
            FontAlignment.LEFT,
            FontAlignment.CENTER
    );

    public static final ColorHelper INVERTED_COLORS = new ColorHelper(
            -1,
            -1,
            ClientColors.LIGHT_GREYISH_BLUE.getColor(),
            FontAlignment.LEFT,
            FontAlignment.CENTER
    );

    private String placeholder = "";
    private float focusFade;
    private float textOffsetX;
    private float targetTextOffsetX;
    private int caretIndex;
    private int selectionStart;
    private int selectionEnd;
    private boolean selectingWithMouse;
    private boolean censorText = false;
    private final String censorChar = Character.toString('·');
    private final TogglableTimer cursorBlinkTimer = new TogglableTimer();
    private final List<ChangeListener> changeListeners = new ArrayList<>();
    private boolean underlineEnabled = true;

    public TextField(GuiComponent parent, String name, int x, int y, int width, int height) {
        super(parent, name, x, y, width, height, DEFAULT_COLORS, "", false);
        this.cursorBlinkTimer.start();
    }

    public TextField(GuiComponent parent, String name, int x, int y, int width, int height, ColorHelper colors) {
        super(parent, name, x, y, width, height, colors, "", false);
        this.cursorBlinkTimer.start();
    }

    public TextField(GuiComponent parent, String name, int x, int y, int width, int height, ColorHelper colors, String text) {
        super(parent, name, x, y, width, height, colors, text, false);
        this.cursorBlinkTimer.start();
    }

    public TextField(
            GuiComponent parent,
            String name,
            int x,
            int y,
            int width,
            int height,
            ColorHelper colors,
            String text,
            String placeholder
    ) {
        super(parent, name, x, y, width, height, colors, text, FontUtils.HELVETICA_LIGHT_25, false);
        this.placeholder = placeholder;
        this.cursorBlinkTimer.start();
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);

        String visibleText = this.getVisibleText();

        this.focusFade += (((!this.focused ? 0.0F : 1.0F) - this.focusFade) / 2.0F);

        if (this.focused) {
            if (this.selectingWithMouse) {
                this.caretIndex = StringUtils.getFittingCharacterCount(
                        visibleText,
                        this.font,
                        (float) this.getAbsoluteX(),
                        mouseX,
                        this.textOffsetX
                );
            }
        } else {
            this.caretIndex = 0;
            this.selectionStart = 0;
            this.targetTextOffsetX = 0.0F;
        }

        this.caretIndex = Math.min(Math.max(0, this.caretIndex), visibleText.length());
        this.selectionEnd = this.caretIndex;
    }

    public void resetTextOffset() {
        this.textOffsetX = 0.0F;
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (super.onMouseDown(mouseX, mouseY, mouseButton)) {
            return true;
        }

        String visibleText = this.getVisibleText();

        this.selectingWithMouse = true;

        this.caretIndex = StringUtils.getFittingCharacterCount(
                visibleText,
                this.font,
                (float) this.getAbsoluteX(),
                mouseX,
                this.textOffsetX
        );

        boolean shiftHeld =
                InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)
                        || InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);

        if (!shiftHeld) {
            this.selectionStart = this.caretIndex;
        }

        return false;
    }

    public void startFocus() {
        this.requestFocus();
        this.caretIndex = this.text.length();
        this.selectionStart = 0;
        this.selectionEnd = this.caretIndex;
    }

    @Override
    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {
        super.onMouseRelease(mouseX, mouseY, mouseButton);
        this.selectingWithMouse = false;
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);

        if (!this.focused) {
            return;
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_A:
                if (this.isModifierKeyPressed()) {
                    this.caretIndex = this.text.length();
                    this.selectionStart = 0;
                    this.selectionEnd = this.caretIndex;
                }
                break;

            case GLFW.GLFW_KEY_C:
                if (this.isModifierKeyPressed() && this.selectionStart != this.selectionEnd) {
                    GLFW.glfwSetClipboardString(
                            client.getWindow().getHandle(),
                            this.text.substring(
                                    Math.min(this.selectionStart, this.selectionEnd),
                                    Math.max(this.selectionStart, this.selectionEnd)
                            )
                    );
                }
                break;

            case GLFW.GLFW_KEY_V:
                if (!this.isModifierKeyPressed()) {
                    break;
                }

                String clipboard = "";
                try {
                    clipboard = GLFW.glfwGetClipboardString(client.getWindow().getHandle());
                    if (clipboard == null) {
                        clipboard = "";
                    }
                } catch (Exception ignored) {
                }

                if (!clipboard.isEmpty()) {
                    if (this.selectionStart != this.selectionEnd) {
                        this.text = StringUtils.cut(this.text, clipboard, this.selectionStart, this.selectionEnd);

                        if (this.caretIndex > this.selectionStart) {
                            this.caretIndex -= (Math.max(this.selectionStart, this.selectionEnd)
                                    - Math.min(this.selectionStart, this.selectionEnd));
                        }
                    } else {
                        this.text = StringUtils.paste(this.text, clipboard, this.caretIndex);
                    }

                    this.caretIndex += clipboard.length();
                    this.selectionStart = this.caretIndex;

                    this.notifyChangeListeners();
                }
                break;

            case GLFW.GLFW_KEY_X:
                if (this.isModifierKeyPressed() && this.selectionStart != this.selectionEnd) {
                    GLFW.glfwSetClipboardString(
                            client.getWindow().getHandle(),
                            this.text.substring(
                                    Math.min(this.selectionStart, this.selectionEnd),
                                    Math.max(this.selectionStart, this.selectionEnd)
                            )
                    );

                    this.text = StringUtils.cut(this.text, "", this.selectionStart, this.selectionEnd);

                    if (this.caretIndex > this.selectionStart) {
                        this.caretIndex -= (Math.max(this.selectionStart, this.selectionEnd)
                                - Math.min(this.selectionStart, this.selectionEnd));
                    }

                    this.selectionStart = this.caretIndex;
                    this.selectionEnd = this.caretIndex;

                    this.notifyChangeListeners();
                }
                break;

            case GLFW.GLFW_KEY_ESCAPE:
                this.setFocused(false);
                break;

            case GLFW.GLFW_KEY_BACKSPACE:
                this.handleBackspace();
                break;

            case GLFW.GLFW_KEY_RIGHT:
                this.moveCaretRight();
                break;

            case GLFW.GLFW_KEY_LEFT:
                this.moveCaretLeft();
                break;

            case GLFW.GLFW_KEY_HOME:
                this.caretIndex = 0;
                if (!this.isShiftPressed()) {
                    this.selectionStart = this.caretIndex;
                }
                break;

            case GLFW.GLFW_KEY_END:
                this.caretIndex = this.text.length();
                if (!this.isShiftPressed()) {
                    this.selectionStart = this.caretIndex;
                }
                break;

            default:
                break;
        }
    }

    public boolean isModifierKeyPressed() {
        long handle = client.getWindow().getHandle();
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL)
                || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SUPER);
    }

    @Override
    public void charTyped(char ch) {
        super.charTyped(ch);

        if (!this.isFocused() || !StringUtils.isPrintableCharacter(ch)) {
            return;
        }

        if (this.selectionStart == this.selectionEnd) {
            this.text = StringUtils.paste(this.text, Character.toString(ch), this.caretIndex);
        } else {
            this.text = StringUtils.cut(this.text, Character.toString(ch), this.selectionStart, this.selectionEnd);
        }

        this.caretIndex++;
        this.selectionStart = this.caretIndex;
        this.notifyChangeListeners();
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();

        float blinkPeriodMs = 1000.0F;
        boolean cursorDim = this.focused && (float) this.cursorBlinkTimer.getElapsedTime() > blinkPeriodMs / 2.0F;
        if ((float) this.cursorBlinkTimer.getElapsedTime() > blinkPeriodMs) {
            this.cursorBlinkTimer.reset();
        }

        String visibleText = this.getVisibleText();

        ScissorUtils.startScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, true);

        int textStartX = this.x + 4;
        int textViewportWidth = this.width - 4;

        float caretX = (float) textStartX
                + this.textOffsetX
                + (float) this.font.getWidth(visibleText.substring(0, this.caretIndex));

        if (this.isFocused()) {
            RenderUtils.drawRoundedRect(
                    caretX + (float) (visibleText.isEmpty() ? 0 : -1),
                    (float) (this.y + this.height / 2 - this.font.getHeight(visibleText) / 2 + 2),
                    caretX + (float) (visibleText.isEmpty() ? 1 : 0),
                    (float) (this.y + this.height / 2 + this.font.getHeight(visibleText) / 2 - 1),
                    ColorHelper.applyAlpha(this.textColor.getTextColor(), !cursorDim ? 0.1F * partialTicks : 0.8F)
            );

            float caretXNoSmooth = (float) (textStartX + this.font.getWidth(visibleText.substring(0, this.caretIndex)))
                    + this.targetTextOffsetX;

            if (caretXNoSmooth < (float) textStartX) {
                this.targetTextOffsetX += (float) textStartX - caretXNoSmooth;
                this.targetTextOffsetX -= Math.min((float) textViewportWidth, this.targetTextOffsetX);
            }

            if (caretXNoSmooth > (float) (textStartX + textViewportWidth)) {
                this.targetTextOffsetX += (float) (textStartX + textViewportWidth) - caretXNoSmooth;
            }
        }

        this.textOffsetX += (this.targetTextOffsetX - this.textOffsetX) / 2.0F;

        this.selectionStart = Math.min(Math.max(0, this.selectionStart), visibleText.length());
        this.selectionEnd = Math.min(Math.max(0, this.selectionEnd), visibleText.length());

        float selectionX1 = (float) textStartX + this.textOffsetX
                + (float) this.font.getWidth(visibleText.substring(0, this.selectionStart));
        float selectionX2 = (float) textStartX + this.textOffsetX
                + (float) this.font.getWidth(visibleText.substring(0, this.selectionEnd));

        RenderUtils.drawRoundedRect(
                selectionX1,
                (float) (this.y + this.height / 2 - this.font.getHeight(visibleText) / 2),
                selectionX2,
                (float) (this.y + this.height / 2 + this.font.getHeight(visibleText) / 2),
                ColorHelper.applyAlpha(-5516546, partialTicks)
        );

        FontAlignment widthAlignment = this.textColor.getWidthAlignment();
        FontAlignment heightAlignment = this.textColor.getHeightAlignment();

        RenderUtils.drawString(
                this.font,
                (float) textStartX + this.textOffsetX,
                (float) (this.y + this.height / 2),
                visibleText.isEmpty() && (!this.focused || visibleText.length() <= 0) ? this.placeholder : visibleText,
                ColorHelper.applyAlpha(
                        this.textColor.getTextColor(),
                        (this.focusFade / 2.0F + 0.4F) * partialTicks * (this.focused && visibleText.length() > 0 ? 1.0F : 0.5F)
                ),
                widthAlignment,
                heightAlignment
        );

        ScissorUtils.restoreScissor();

        if (this.underlineEnabled) {
            RenderUtils.drawRoundedRect(
                    (float) this.x,
                    (float) (this.y + this.height - 2),
                    (float) (this.x + this.width),
                    (float) (this.y + this.height),
                    ColorHelper.applyAlpha(
                            this.textColor.getPrimaryColor(),
                            (this.focusFade / 2.0F + 0.5F) * partialTicks
                    )
            );
        }

        super.draw(partialTicks);
    }

    public final void addChangeListener(ChangeListener listener) {
        this.changeListeners.add(listener);
    }

    public void notifyChangeListeners() {
        for (ChangeListener listener : this.changeListeners) {
            listener.onChange(this);
        }
    }

    public String getPlaceholder() {
        return this.placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public void setCensorText(boolean censorText) {
        this.censorText = censorText;
    }

    public void setUnderlineEnabled(boolean underlineEnabled) {
        this.underlineEnabled = underlineEnabled;
    }

    public interface ChangeListener {
        void onChange(TextField field);
    }

    private void handleBackspace() {
        if (this.text.isEmpty()) {
            this.selectionStart = this.caretIndex;
            return;
        }

        if (this.selectionStart != this.selectionEnd) {
            this.text = StringUtils.cut(this.text, "", this.selectionStart, this.selectionEnd);

            if (this.caretIndex > this.selectionStart) {
                this.caretIndex -= (Math.max(this.selectionStart, this.selectionEnd)
                        - Math.min(this.selectionStart, this.selectionEnd));
            }
        } else if (this.isModifierKeyPressed()) {
            int deleteFromIndex = findPreviousWordBoundaryIndex();

            if (deleteFromIndex != -1) {
                this.text = StringUtils.cut(this.text, "", deleteFromIndex, this.caretIndex);
                this.caretIndex = deleteFromIndex;
            }
        } else {
            this.text = StringUtils.cut(this.text, "", this.caretIndex - 1, this.caretIndex);
            this.caretIndex--;
        }

        this.notifyChangeListeners();
        this.selectionStart = this.caretIndex;
    }

    private int findPreviousWordBoundaryIndex() {
        int deleteFromIndex = -1;

        for (int i = Math.max(this.caretIndex - 1, 0); i >= 0; i--) {
            if (isWordBoundary(this.text, i) && Math.abs(this.caretIndex - i) > 1) {
                deleteFromIndex = i + (i == 0 ? 0 : 1);
                break;
            }
        }

        return deleteFromIndex;
    }

    private void moveCaretRight() {
        if (!this.isModifierKeyPressed()) {
            this.caretIndex++;
        } else {
            int nextIndex = findNextWordBoundaryIndex();
            if (nextIndex != -1) {
                this.caretIndex = nextIndex;
            }
        }

        if (!this.isShiftPressed()) {
            this.selectionStart = this.caretIndex;
        }
    }

    private int findNextWordBoundaryIndex() {
        int nextIndex = -1;

        for (int i = this.caretIndex; i < this.text.length(); i++) {
            try {
                if ((isWordBoundary(this.text, i) || i == this.text.length() - 1)
                        && (Math.abs(this.caretIndex - i) > 1 || i == this.text.length() - 1)) {
                    nextIndex = i + 1;
                    break;
                }
            } catch (Exception ignored) {
                break;
            }
        }

        return nextIndex;
    }

    private void moveCaretLeft() {
        if (!this.isModifierKeyPressed()) {
            this.caretIndex--;
        } else {
            int previousIndex = -1;

            for (int i = Math.max(this.caretIndex - 1, 0); i >= 0; i--) {
                try {
                    if ((isWordBoundary(this.text, i) || i == 0) && Math.abs(this.caretIndex - i) > 1) {
                        previousIndex = i;
                        break;
                    }
                } catch (Exception ignored) {
                    break;
                }
            }

            if (previousIndex != -1) {
                this.caretIndex = previousIndex;
            }
        }

        if (!this.isShiftPressed()) {
            this.selectionStart = this.caretIndex;
        }
    }

    private boolean isShiftPressed() {
        long handle = client.getWindow().getHandle();
        return InputUtil.isKeyPressed(handle, GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(handle, GLFW_KEY_RIGHT_SHIFT);
    }

    private boolean isWordBoundary(String text, int index) {
        return " ".equalsIgnoreCase(String.valueOf(text.charAt(index))) || index == 0;
    }

    private String getVisibleText() {
        if (!this.censorText) {
            return this.text;
        }
        return this.text.replaceAll(".", this.censorChar);
    }
}
