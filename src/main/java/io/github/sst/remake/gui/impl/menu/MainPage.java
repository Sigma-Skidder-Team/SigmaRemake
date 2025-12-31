package io.github.sst.remake.gui.impl.menu;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.Screen;
import io.github.sst.remake.gui.element.impl.Button;
import io.github.sst.remake.gui.element.impl.Text;
import io.github.sst.remake.gui.element.impl.TextButton;
import io.github.sst.remake.gui.element.impl.mainmenu.RoundButton;
import io.github.sst.remake.gui.impl.JelloMenu;
import io.github.sst.remake.gui.screen.AltManagerScreen;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.io.audio.SoundUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.newdawn.slick.opengl.font.TrueTypeFont;
import org.newdawn.slick.opengl.texture.Texture;

public class MainPage extends CustomGuiScreen implements IMinecraft {
    private final Button singleplayerButton;
    private final Button multiplayerButton;
    private final Button realmsButton;
    private final Button optionsButton;
    private final Button altManagerButton;

    private final TextButton changelogButton;
    private final TextButton quitButton;

    public MainPage(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6) {
        super(var1, var2, var3, var4, var5, var6);
        this.setListening(false);
        TrueTypeFont font = FontUtils.HELVETICA_LIGHT_20;
        int var17 = 0;

        String prod = "© Sigma Prod";

        String version = "Jello for Fabric " + Client.VERSION;

        this.addToList(
                this.singleplayerButton = new RoundButton(
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
                this.multiplayerButton = new RoundButton(
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
                this.realmsButton = new RoundButton(
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
                this.optionsButton = new RoundButton(
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

        this.addToList(this.altManagerButton = new RoundButton(this, "Alt Manager", this.method13447(var17++), this.method13448(), 128, 128, Resources.altPNG, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())));

        this.addToList(new Text(this, "Copyright", 10, this.getHeight() - 31, font.getWidth(prod), 128, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), prod, font, true));
        this.addToList(new Text(this, "Version", this.getWidth() - font.getWidth(version) - 9, this.getHeight() - 31, 128, 128, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), version, font, true));

        this.addToList(this.changelogButton = new TextButton(this, "changelog", 432, 24, 110, 50, new ColorHelper(ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.7F)), "Changelog", FontUtils.HELVETICA_LIGHT_20));
        this.addToList(this.quitButton = new TextButton(this, "quit", 30, 24, 50, 50, new ColorHelper(ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.4F)), "Exit", FontUtils.HELVETICA_LIGHT_20));

        this.quitButton.onClick((var1x, var2x) -> {
            ((JelloMenu) this.getParent()).method13341();
            new Thread(() -> {
                try {
                    Thread.sleep(2000L);
                    MinecraftClient.getInstance().scheduleStop();
                } catch (InterruptedException e) {
                    MinecraftClient.getInstance().scheduleStop();
                }
            }).start();
        });

        this.changelogButton.onClick((var1x, var2x) -> ((JelloMenu) this.getParent()).animateIn());
        this.singleplayerButton.onClick((var1x, var2x) -> this.displayGUI(new SelectWorldScreen(client.currentScreen)));
        this.multiplayerButton.onClick((var1x, var2x) -> this.displayGUI(new MultiplayerScreen(client.currentScreen)));
        this.optionsButton.onClick((var1x, var2x) -> this.displayGUI(new OptionsScreen(client.currentScreen, client.options)));
        this.altManagerButton.onClick((var1x, var2x) -> this.displayScreen(new AltManagerScreen()));
        this.realmsButton.onClick((var1x, var2x) -> this.playClickSound());
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
                (float) (this.getWidth() / 2 - imageWidth / 2),
                (float) (this.getHeight() / 2 - imageHeight),
                (float) imageWidth,
                (float) imageHeight,
                largeLogo,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        super.draw(partialTicks);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        this.changelogButton.setX(90);
        this.quitButton.setX(30);
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
        return this.getWidth() / 2 - 305 + var1 * 128 + var1 * -6;
    }

    private int method13448() {
        return this.getHeight() / 2 + 14;
    }
}
