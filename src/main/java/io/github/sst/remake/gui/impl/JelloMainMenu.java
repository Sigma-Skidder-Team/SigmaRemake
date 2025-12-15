package io.github.sst.remake.gui.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.element.impl.Button;
import io.github.sst.remake.gui.element.impl.Text;
import io.github.sst.remake.gui.element.impl.TextButton;
import io.github.sst.remake.gui.element.impl.jello.MainMenuButton;
import io.github.sst.remake.gui.screen.MainMenuScreen;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.io.audio.SoundUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.ResourceRegistry;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;

public class JelloMainMenu extends CustomGuiScreen implements IMinecraft {
    private final Button singleplayerButton;
    private final Button multiplayerButton;
    private final Button realmsButton;
    private final Button optionsButton;
    private final Button altManagerButton;

    private final Text copyright;
    private final Text version;

    private final TextButton changelogButton;
    private final TextButton quitButton;

    public JelloMainMenu(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6);
        this.setListening(false);
        TrueTypeFont font = ResourceRegistry.JelloLightFont20;
        int var17 = 0;

        String prod = "© Sigma Prod";

        String version = "Jello for Fabric " + Client.VERSION;

        this.addToList(
                this.singleplayerButton = new MainMenuButton(
                        this,
                        "Singleplayer",
                        this.method13447(var17++),
                        this.method13448(),
                        128,
                        128,
                        Resources.singleplayerPNG,
                        new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
                )
        );

        this.addToList(
                this.multiplayerButton = new MainMenuButton(
                        this,
                        "Multiplayer",
                        this.method13447(var17++),
                        this.method13448(),
                        128,
                        128,
                        Resources.multiplayerPNG,
                        new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
                )
        );

        this.addToList(
                this.realmsButton = new MainMenuButton(
                        this,
                        "Realms",
                        this.method13447(var17++),
                        this.method13448(),
                        128,
                        128,
                        Resources.shopPNG,
                        new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
                )
        );

        this.addToList(
                this.optionsButton = new MainMenuButton(
                        this,
                        "Options",
                        this.method13447(var17++),
                        this.method13448(),
                        128,
                        128,
                        Resources.optionsPNG,
                        new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
                )
        );

        this.addToList(this.altManagerButton = new MainMenuButton(this, "Alt Manager", this.method13447(var17++), this.method13448(), 128, 128, Resources.altPNG, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())));
        this.addToList(this.copyright = new Text(this, "Copyright", 10, this.getHeightA() - 31, font.getWidth(prod), 128, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), prod, font));
        this.addToList(this.version = new Text(this, "Version", this.getWidthA() - font.getWidth(version) - 9, this.getHeightA() - 31, 128, 128, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), version, font));
        this.copyright.shadow = true;
        this.version.shadow = true;
        this.copyright.shadow = true;
        this.addToList(this.changelogButton = new TextButton(this, "changelog", 432, 24, 110, 50, new ColorHelper(ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.7F)), "Changelog", ResourceRegistry.JelloLightFont20));
        this.addToList(this.quitButton = new TextButton(this, "quit", 30, 24, 50, 50, new ColorHelper(ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.4F)), "Exit", ResourceRegistry.JelloLightFont20));
        this.quitButton.onClick((var1x, var2x) -> {
            ((MainMenuScreen) this.getParent()).method13341();
            new Thread(() -> {
                try {
                    Thread.sleep(2000L);
                    MinecraftClient.getInstance().scheduleStop();
                } catch (InterruptedException e) {
                    MinecraftClient.getInstance().scheduleStop();
                }
            }).start();
        });
        this.changelogButton.onClick((var1x, var2x) -> ((MainMenuScreen) this.getParent()).animateIn());
        this.singleplayerButton.onClick((var1x, var2x) -> this.displayGUI(new SelectWorldScreen(client.currentScreen)));
        this.multiplayerButton.onClick((var1x, var2x) -> this.displayGUI(new MultiplayerScreen(client.currentScreen)));
        this.optionsButton.onClick((var1x, var2x) -> this.displayGUI(new OptionsScreen(client.currentScreen, client.options)));
        //this.altManagerButton.onClick((var1x, var2x) -> this.displayScreen(new AltManagerScreen()));
        this.realmsButton.onClick((var1x, var2x) -> this.method13443());
    }

    public void method13443() {
        this.playClickSound();
    }

    @Override
    public void draw(float partialTicks) {
        this.method13224();
        Texture largeLogo = Resources.logoLargePNG;
        int imageWidth = largeLogo.getImageWidth();
        int imageHeight = largeLogo.getImageHeight();

        if (Client.INSTANCE.screenManager.scaleFactor > 1.0F) {
            largeLogo = Resources.logoLarge2xPNG;
        }

        RenderUtils.drawImage(
                (float) (this.getWidthA() / 2 - imageWidth / 2),
                (float) (this.getHeightA() / 2 - imageHeight),
                (float) imageWidth,
                (float) imageHeight,
                largeLogo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        super.draw(partialTicks);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        int var5 = 30;
        int var6 = 90;
        this.changelogButton.setXA(var6 + 0);
        this.quitButton.setXA(var5 + 0);
        super.updatePanelDimensions(mouseX, mouseY);
    }

    public void playClickSound() {
        SoundUtils.play("clicksound");
    }

    public void displayGUI(net.minecraft.client.gui.screen.Screen var1) {
        MinecraftClient.getInstance().openScreen(var1);
        this.playClickSound();
    }

    public void displayScreen(Screen screen) {
        Client.INSTANCE.screenManager.handle(screen);
        this.playClickSound();
    }

    private int method13447(int var1) {
        return this.getWidthA() / 2 - 305 + var1 * 128 + var1 * -6;
    }

    private int method13448() {
        return this.getHeightA() / 2 + 14;
    }
}
