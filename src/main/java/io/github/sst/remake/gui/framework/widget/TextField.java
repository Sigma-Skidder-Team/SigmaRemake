package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.java.StringUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.math.TogglableTimer;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import net.minecraft.client.util.InputUtil;
import org.newdawn.slick.opengl.font.TrueTypeFont;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;

public class TextField extends Widget implements IMinecraft {
    public static final ColorHelper DEFAULT_COLORS = new ColorHelper(
            -892679478, -892679478, -892679478, ClientColors.DEEP_TEAL.getColor(), FontAlignment.LEFT, FontAlignment.CENTER
    );
    public static final ColorHelper INVERTED_COLORS = new ColorHelper(-1, -1, -1, ClientColors.LIGHT_GREYISH_BLUE.getColor(), FontAlignment.LEFT, FontAlignment.CENTER);
    private String placeholder = "";
    private float focusFade;
    private float textOffsetX;
    private float targetTextOffsetX;
    private int caretIndex;
    private int selectionStart;
    private int selectionEnd;
    private boolean isSelectingWithMouse;
    private boolean censorText = false;
    private String censorChar = Character.toString('·');
    private final TogglableTimer cursorBlinkTimer = new TogglableTimer();
    private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
    private boolean underlineEnabled = true;

    public TextField(GuiComponent var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6, DEFAULT_COLORS, "", false);
        this.cursorBlinkTimer.start();
    }

    public TextField(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7) {
        super(var1, var2, var3, var4, var5, var6, var7, "", false);
        this.cursorBlinkTimer.start();
    }

    public TextField(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8) {
        super(var1, var2, var3, var4, var5, var6, var7, var8, false);
        this.cursorBlinkTimer.start();
    }

    public TextField(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, ColorHelper var7, String var8, String placeholder) {
        super(var1, var2, var3, var4, var5, var6, var7, var8, FontUtils.HELVETICA_LIGHT_25, false);
        this.placeholder = placeholder;
        this.cursorBlinkTimer.start();
    }

    public TextField(GuiComponent screen, String id, int x, int y, int width, int height, ColorHelper color, String text, String placeholder, TrueTypeFont _font) {
        super(screen, id, x, y, width, height, color, text, false);
        this.placeholder = placeholder;
        this.cursorBlinkTimer.start();
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
        String text = this.text;
        if (this.censorText) {
            text = this.text.replaceAll(".", this.censorChar);
        }

        this.focusFade = this.focusFade + ((!this.focused ? 0.0F : 1.0F) - this.focusFade) / 2.0F;
        if (this.focused) {
            if (this.isSelectingWithMouse) {
                this.caretIndex = StringUtils.getFittingCharacterCount(text, this.font, (float) this.getAbsoluteX(), mouseX, this.textOffsetX);
            }
        } else {
            this.caretIndex = 0;
            this.selectionStart = 0;
            this.targetTextOffsetX = 0.0F;
        }

        this.caretIndex = Math.min(Math.max(0, this.caretIndex), text.length());
        this.selectionEnd = this.caretIndex;
    }

    public void resetTextOffset() {
        this.textOffsetX = 0.0F;
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseDown(mouseX, mouseY, mouseButton)) {
            String var6 = this.text;
            if (this.censorText) {
                var6 = this.text.replaceAll(".", this.censorChar);
            }

            this.isSelectingWithMouse = true;
            this.caretIndex = StringUtils.getFittingCharacterCount(var6, this.font, (float) this.getAbsoluteX(), mouseX, this.textOffsetX);
            if (!InputUtil.isKeyPressed(client.getWindow().getHandle(), 340)
                    && !InputUtil.isKeyPressed(client.getWindow().getHandle(), 344)) {
                this.selectionStart = this.caretIndex;
            }

            return false;
        } else {
            return true;
        }
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
        this.isSelectingWithMouse = false;
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (this.focused) {
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
                                this.text.substring(Math.min(this.selectionStart, this.selectionEnd), Math.max(this.selectionStart, this.selectionEnd))
                        );
                    }
                    break;
                case GLFW.GLFW_KEY_V:
                    if (!this.isModifierKeyPressed()) break;
                    String clip = "";

                    try {
                        clip = GLFW.glfwGetClipboardString(client.getWindow().getHandle());
                        if (clip == null) {
                            clip = "";
                        }
                    } catch (Exception ignored) {
                    }

                    if (!clip.isEmpty()) {
                        if (this.selectionStart != this.selectionEnd) {
                            this.text = StringUtils.cut(this.text, clip, this.selectionStart, this.selectionEnd);
                            if (this.caretIndex > this.selectionStart) {
                                this.caretIndex = this.caretIndex - (Math.max(this.selectionStart, this.selectionEnd) - Math.min(this.selectionStart, this.selectionEnd));
                            }

                        } else {
                            this.text = StringUtils.paste(this.text, clip, this.caretIndex);
                        }
                        this.caretIndex = this.caretIndex + clip.length();
                        this.selectionStart = this.caretIndex;

                        this.notifyChangeListeners();
                    }
                    break;
                case GLFW.GLFW_KEY_X:
                    if (this.isModifierKeyPressed() && this.selectionStart != this.selectionEnd) {
                        GLFW.glfwSetClipboardString(
                                client.getWindow().getHandle(),
                                this.text.substring(Math.min(this.selectionStart, this.selectionEnd), Math.max(this.selectionStart, this.selectionEnd))
                        );
                        this.text = StringUtils.cut(this.text, "", this.selectionStart, this.selectionEnd);
                        if (this.caretIndex > this.selectionStart) {
                            this.caretIndex = this.caretIndex - (Math.max(this.selectionStart, this.selectionEnd) - Math.min(this.selectionStart, this.selectionEnd));
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
                    if (!this.text.isEmpty()) {
                        if (this.selectionStart != this.selectionEnd) {
                            this.text = StringUtils.cut(this.text, "", this.selectionStart, this.selectionEnd);
                            if (this.caretIndex > this.selectionStart) {
                                this.caretIndex = this.caretIndex - (Math.max(this.selectionStart, this.selectionEnd) - Math.min(this.selectionStart, this.selectionEnd));
                            }
                        } else if (this.isModifierKeyPressed()) {
                            int var11 = -1;

                            for (int var14 = Math.max(this.caretIndex - 1, 0); var14 >= 0; var14--) {
                                if ((String.valueOf(this.text.charAt(var14)).equalsIgnoreCase(" ") || var14 == 0) && Math.abs(this.caretIndex - var14) > 1) {
                                    var11 = var14 + (var14 == 0 ? 0 : 1);
                                    break;
                                }
                            }

                            if (var11 != -1) {
                                this.text = StringUtils.cut(this.text, "", var11, this.caretIndex);
                                this.caretIndex = var11;
                            }
                        } else {
                            this.text = StringUtils.cut(this.text, "", this.caretIndex - 1, this.caretIndex);
                            this.caretIndex--;
                        }

                        this.notifyChangeListeners();
                    }

                    this.selectionStart = this.caretIndex;
                    break;
                case GLFW.GLFW_KEY_RIGHT:
                    if (!this.isModifierKeyPressed()) {
                        this.caretIndex++;
                    } else {
                        int previousIdx = -1;

                        for (int i = this.caretIndex; i < this.text.length(); i++) {
                            try {
                                if ((String.valueOf(this.text.charAt(i)).equalsIgnoreCase(" ") || i == this.text.length() - 1)
                                        && (Math.abs(this.caretIndex - i) > 1 || i == this.text.length() - 1)) {
                                    previousIdx = i + 1;
                                    break;
                                }
                            } catch (Exception ignored) {
                                break;
                            }
                        }

                        if (previousIdx != -1) {
                            this.caretIndex = previousIdx;
                        }
                    }

                    if (!InputUtil.isKeyPressed(client.getWindow().getHandle(), 340)
                            && !InputUtil.isKeyPressed(client.getWindow().getHandle(), 344)) {
                        this.selectionStart = this.caretIndex;
                    }
                    break;
                case GLFW.GLFW_KEY_LEFT:
                    if (!this.isModifierKeyPressed()) {
                        this.caretIndex--;
                    } else {
                        int previousIdx = -1;

                        for (int i = Math.max(this.caretIndex - 1, 0); i >= 0; i--) {
                            try {
                                if ((String.valueOf(this.text.charAt(i)).equalsIgnoreCase(" ") || i == 0) && Math.abs(this.caretIndex - i) > 1) {
                                    previousIdx = i;
                                    break;
                                }
                            } catch (Exception var8) {
                                break;
                            }
                        }

                        if (previousIdx != -1) {
                            this.caretIndex = previousIdx;
                        }
                    }

                    if (!InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW_KEY_LEFT_SHIFT)
                            && !InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW_KEY_RIGHT_SHIFT)) {
                        this.selectionStart = this.caretIndex;
                    }
                    break;
                case GLFW.GLFW_KEY_HOME:
                    this.caretIndex = 0;
                    if (!InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW_KEY_LEFT_SHIFT)
                            && !InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW_KEY_RIGHT_SHIFT)) {
                        this.selectionStart = this.caretIndex;
                    }
                    break;
                case GLFW.GLFW_KEY_END:
                    this.caretIndex = this.text.length();
                    if (!InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW_KEY_LEFT_SHIFT)
                            && !InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW_KEY_RIGHT_SHIFT)) {
                        this.selectionStart = this.caretIndex;
                    }
            }
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
        if (this.isFocused() && StringUtils.isPrintableCharacter(ch)) {
            if (this.selectionStart == this.selectionEnd) {
                this.text = StringUtils.paste(this.text, Character.toString(ch), this.caretIndex);
            } else {
                this.text = StringUtils.cut(this.text, Character.toString(ch), this.selectionStart, this.selectionEnd);
            }

            this.caretIndex++;
            this.selectionStart = this.caretIndex;
            this.notifyChangeListeners();
        }
    }

    @Override
    public void draw(float partialTicks) {
        this.applyTranslationTransforms();
        float var4 = 1000.0F;
        boolean var5 = this.focused && (float) this.cursorBlinkTimer.getElapsedTime() > var4 / 2.0F;
        if ((float) this.cursorBlinkTimer.getElapsedTime() > var4) {
            this.cursorBlinkTimer.reset();
        }

        String var6 = this.text;
        if (this.censorText) {
            var6 = this.text.replaceAll(".", this.censorChar);
        }

        ScissorUtils.startScissor(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, true);
        int var7 = this.x + 4;
        int var8 = this.width - 4;
        float var9 = (float) var7 + this.textOffsetX + (float) this.font.getWidth(var6.substring(0, this.caretIndex));
        if (this.isFocused()) {
            RenderUtils.drawRoundedRect(
                    var9 + (float) (var6.isEmpty() ? 0 : -1),
                    (float) (this.y + this.height / 2 - this.font.getHeight(var6) / 2 + 2),
                    var9 + (float) (var6.isEmpty() ? 1 : 0),
                    (float) (this.y + this.height / 2 + this.font.getHeight(var6) / 2 - 1),
                    ColorHelper.applyAlpha(this.textColor.getTextColor(), !var5 ? 0.1F * partialTicks : 0.8F)
            );
            float var10 = (float) (var7 + this.font.getWidth(var6.substring(0, this.caretIndex))) + this.targetTextOffsetX;
            if (var10 < (float) var7) {
                this.targetTextOffsetX += (float) var7 - var10;
                this.targetTextOffsetX = this.targetTextOffsetX - Math.min((float) var8, this.targetTextOffsetX);
            }

            if (var10 > (float) (var7 + var8)) {
                this.targetTextOffsetX += (float) (var7 + var8) - var10;
            }
        }

        this.textOffsetX = this.textOffsetX + (this.targetTextOffsetX - this.textOffsetX) / 2.0F;
        this.selectionStart = Math.min(Math.max(0, this.selectionStart), var6.length());
        this.selectionEnd = Math.min(Math.max(0, this.selectionEnd), var6.length());
        float var14 = (float) var7 + this.textOffsetX + (float) this.font.getWidth(var6.substring(0, this.selectionStart));
        float var11 = (float) var7 + this.textOffsetX + (float) this.font.getWidth(var6.substring(0, this.selectionEnd));
        RenderUtils.drawRoundedRect(
                var14,
                (float) (this.y + this.height / 2 - this.font.getHeight(var6) / 2),
                var11,
                (float) (this.y + this.height / 2 + this.font.getHeight(var6) / 2),
                ColorHelper.applyAlpha(-5516546, partialTicks)
        );
        FontAlignment widthAlignment = this.textColor.getWidthAlignment();
        FontAlignment heightAlignment = this.textColor.getHeightAlignment();
        RenderUtils.drawString(
                this.font,
                (float) var7 + this.textOffsetX,
                (float) (this.y + this.height / 2),
                var6.length() == 0 && (!this.focused || var6.length() <= 0) ? this.placeholder : var6,
                ColorHelper.applyAlpha(this.textColor.getTextColor(), (this.focusFade / 2.0F + 0.4F) * partialTicks * (this.focused && var6.length() > 0 ? 1.0F : 0.5F)),
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
                    ColorHelper.applyAlpha(this.textColor.getPrimaryColor(), (this.focusFade / 2.0F + 0.5F) * partialTicks)
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
}
