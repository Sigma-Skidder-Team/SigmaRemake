package io.github.sst.remake.gui.element.impl.jello;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.impl.Checkbox;
import io.github.sst.remake.gui.element.impl.TextButton;
import io.github.sst.remake.util.java.BindUtils;
import io.github.sst.remake.util.java.bind.Keys;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.ResourceRegistry;

public class JelloOptionsGroup extends CustomGuiScreen {
    public JelloOptionsGroup(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6);
        this.setListening(false);
        ColorHelper var9 = ColorHelper.field27961.clone();
        var9.setPrimaryColor(ClientColors.LIGHT_GREYISH_BLUE.getColor());
        TextButton openKeybinds;
        this.addToList(openKeybinds = new TextButton(this, "openKeybinds", var5 / 2 - 300, var6 - 80, 300, 38, var9, "Open Keybind Manager", ResourceRegistry.JelloLightFont24));
        TextButton openGui;
        this.addToList(openGui = new TextButton(this, "openGui", var5 / 2, var6 - 80, 300, 38, var9, "Open Jello's Click GUI", ResourceRegistry.JelloLightFont24));
        TextButton credits;
        this.addToList(credits = new TextButton(this, "credits", var5 / 2 - 100, var6 - 280, 200, 38, var9, "Credits", ResourceRegistry.JelloLightFont18));
        //openKeybinds.onClick((var0, var1x) -> JelloOptions.showGUI(new KeyboardHolder(new StringTextComponent("Keybind Manager"))));
        //openGui.onClick((var0, var1x) -> JelloOptions.showGUI(new ClickGuiHolder(new StringTextComponent("Click GUI"))));
        //credits.onClick((var0, var1x) -> JelloOptions.showGUI(new CreditsHolder(new StringTextComponent("GuiCredits"))));
        Checkbox var13;
        this.addToList(var13 = new Checkbox(this, "guiBlurCheckBox", var5 / 2 - 70, var6 - 220, 25, 25));
        var13.method13705(Client.INSTANCE.configManager.guiBlur, false);
        var13.onPress(var1x -> Client.INSTANCE.configManager.guiBlur = var13.method13703());
        Checkbox var14;
        this.addToList(var14 = new Checkbox(this, "guiBlurIngameCheckBox", var5 / 2 + 130, var6 - 220, 25, 25));
        var14.method13705(Client.INSTANCE.configManager.hqBlur, false);
        var14.onPress(var1x -> Client.INSTANCE.configManager.hqBlur = var14.method13703());
    }

    @Override
    public void draw(float partialTicks) {
        this.drawTitle(this.x + (this.getWidth() - 202) / 2, this.y + 10, partialTicks);

        String versionInfo = "You're currently using Sigma " + Client.VERSION;
        RenderUtils.drawString(
                ResourceRegistry.JelloLightFont20,
                (float) (this.x + (this.getWidth() - ResourceRegistry.JelloLightFont20.getWidth(versionInfo)) / 2),
                (float) (this.y + 70),
                versionInfo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.4F * partialTicks)
        );
        String clickGuiInfo = "Click GUI is currently bound to: "
                + BindUtils.getKeyName(Keys.RIGHT_SHIFT.keycode)
                + " Key";
        RenderUtils.drawString(
                ResourceRegistry.JelloLightFont20,
                (float) (this.getX() + (this.getWidth() - ResourceRegistry.JelloLightFont20.getWidth(clickGuiInfo)) / 2),
                (float) (this.getY() + this.getHeight() - 180),
                clickGuiInfo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F * partialTicks)
        );
        String keybindInfo = "Configure all your keybinds in the keybind manager!";
        RenderUtils.drawString(
                ResourceRegistry.JelloLightFont14,
                (float) (this.getX() + (this.getWidth() - ResourceRegistry.JelloLightFont14.getWidth(keybindInfo)) / 2),
                (float) (this.getY() + this.getHeight() - 150),
                keybindInfo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.4F * partialTicks)
        );
        String blurInfo = "GUI Blur: ";
        RenderUtils.drawString(
                ResourceRegistry.JelloLightFont20,
                (float) (this.getX() + (this.getWidth() - ResourceRegistry.JelloLightFont20.getWidth(blurInfo)) / 2 - 114),
                (float) (this.getY() + this.getHeight() - 221),
                blurInfo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * partialTicks)
        );
        String gpuInfo = "GPU Accelerated: ";
        RenderUtils.drawString(
                ResourceRegistry.JelloLightFont20,
                (float) (this.getX() + (this.getWidth() - ResourceRegistry.JelloLightFont20.getWidth(gpuInfo)) / 2 + 52),
                (float) (this.getY() + this.getHeight() - 221),
                gpuInfo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * partialTicks)
        );
        super.draw(partialTicks);
    }

    private void drawTitle(int var1, int var2, float var3) {
        RenderUtils.drawString(ResourceRegistry.JelloMediumFont40, (float) var1, (float) (var2 + 1), "Jello", ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var3));
        RenderUtils.drawString(
                ResourceRegistry.JelloLightFont25, (float) (var1 + 95), (float) (var2 + 14), "for Sigma", ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.86F * var3)
        );
    }
}
