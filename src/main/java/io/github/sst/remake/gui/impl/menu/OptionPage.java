package io.github.sst.remake.gui.impl.menu;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.impl.Checkbox;
import io.github.sst.remake.gui.element.impl.TextButton;
import io.github.sst.remake.gui.impl.JelloOptions;
import io.github.sst.remake.gui.screen.holder.CreditsHolder;
import io.github.sst.remake.gui.screen.holder.KeybindsHolder;
import io.github.sst.remake.util.client.BindUtils;
import io.github.sst.remake.util.client.bind.Keys;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.text.Text;

public class OptionPage extends CustomGuiScreen {
    public OptionPage(CustomGuiScreen parent, String name, int x, int y, int width, int height) {
        super(parent, name, x, y, width, height);
        this.setListening(false);
        ColorHelper color = ColorHelper.DEFAULT_COLOR.clone();
        color.setPrimaryColor(ClientColors.LIGHT_GREYISH_BLUE.getColor());
        TextButton openKeybinds;
        this.addToList(openKeybinds = new TextButton(this, "openKeybinds", width / 2 - 300, height - 80, 300, 38, color, "Open Keybind Manager", FontUtils.HELVETICA_LIGHT_24));
        TextButton openGui;
        this.addToList(openGui = new TextButton(this, "openGui", width / 2, height - 80, 300, 38, color, "Open Jello's Click GUI", FontUtils.HELVETICA_LIGHT_24));
        TextButton credits;
        this.addToList(credits = new TextButton(this, "credits", width / 2 - 100, height - 280, 200, 38, color, "Credits", FontUtils.HELVETICA_LIGHT_18));
        openKeybinds.onClick((screen, mouseButton) -> JelloOptions.showGUI(new KeybindsHolder(Text.literal("Keybind Manager"))));
        //openGui.onClick((screen, mouseButton) -> JelloOptions.showGUI(new ClickGuiHolder(new StringTextComponent("Click GUI"))));
        credits.onClick((screen, mouseButton) -> JelloOptions.showGUI(new CreditsHolder(Text.literal("Jello Credits"))));
        Checkbox blurCheckbox;
        this.addToList(blurCheckbox = new Checkbox(this, "guiBlurCheckBox", width / 2 - 70, height - 220, 25, 25));
        blurCheckbox.method13705(Client.INSTANCE.configManager.guiBlur, false);
        blurCheckbox.onPress(e -> Client.INSTANCE.configManager.guiBlur = blurCheckbox.getValue());
        Checkbox ingameBlurCheckbox;
        this.addToList(ingameBlurCheckbox = new Checkbox(this, "guiBlurIngameCheckBox", width / 2 + 130, height - 220, 25, 25));
        ingameBlurCheckbox.method13705(Client.INSTANCE.configManager.hqBlur, false);
        ingameBlurCheckbox.onPress(e -> Client.INSTANCE.configManager.hqBlur = ingameBlurCheckbox.getValue());
    }

    @Override
    public void draw(float partialTicks) {
        float x = this.x + (this.getWidth() - 202) / 2f;
        float y = this.y + 10;

        RenderUtils.drawString(FontUtils.HELVETICA_MEDIUM_40, x, y + 1, "Jello", ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks));
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_25, x + 95, y + 14, "for Sigma", ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.86F * partialTicks));

        String versionInfo = "You're currently using Sigma " + Client.VERSION;
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                (float) (this.getX() + (this.getWidth() - FontUtils.HELVETICA_LIGHT_20.getWidth(versionInfo)) / 2),
                (float) (this.getY() + 70),
                versionInfo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.4F * partialTicks)
        );

        String clickGuiInfo = "Click GUI is currently bound to: " + BindUtils.getKeyName(Keys.RIGHT_SHIFT.keycode) + " Key";
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                (float) (this.getX() + (this.getWidth() - FontUtils.HELVETICA_LIGHT_20.getWidth(clickGuiInfo)) / 2),
                (float) (this.getY() + this.getHeight() - 180),
                clickGuiInfo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F * partialTicks)
        );

        String keybindInfo = "Configure all your keybinds in the keybind manager!";
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + (this.getWidth() - FontUtils.HELVETICA_LIGHT_14.getWidth(keybindInfo)) / 2),
                (float) (this.getY() + this.getHeight() - 150),
                keybindInfo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.4F * partialTicks)
        );

        String blurInfo = "GUI Blur: ";
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                (float) (this.getX() + (this.getWidth() - FontUtils.HELVETICA_LIGHT_20.getWidth(blurInfo)) / 2 - 114),
                (float) (this.getY() + this.getHeight() - 221),
                blurInfo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * partialTicks)
        );

        String gpuInfo = "GPU Accelerated: ";
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                (float) (this.getX() + (this.getWidth() - FontUtils.HELVETICA_LIGHT_20.getWidth(gpuInfo)) / 2 + 52),
                (float) (this.getY() + this.getHeight() - 221),
                gpuInfo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * partialTicks)
        );

        super.draw(partialTicks);
    }
}
