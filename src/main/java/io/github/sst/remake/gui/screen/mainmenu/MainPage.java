package io.github.sst.remake.gui.screen.mainmenu;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.gui.framework.widget.Text;
import io.github.sst.remake.gui.framework.widget.TextButton;
import io.github.sst.remake.gui.screen.altmanager.AltManagerScreen;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.system.io.audio.SoundUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import org.newdawn.slick.opengl.font.TrueTypeFont;
import org.newdawn.slick.opengl.texture.Texture;

public class MainPage extends GuiComponent implements IMinecraft {
    private final Button singleplayerButton;
    private final Button multiplayerButton;
    private final Button realmsButton;
    private final Button optionsButton;
    private final Button altManagerButton;

    private final TextButton changelogButton;
    private final TextButton quitButton;

    public MainPage(GuiComponent parent, String id, int x, int y, int width, int height) {
        super(parent, id, x, y, width, height);
        this.setListening(false);

        TrueTypeFont textFont = FontUtils.HELVETICA_LIGHT_20;

        String copyright = "© Sigma Prod";
        String versionText = "Jello for Fabric " + Client.VERSION;

        int index = 0;

        this.singleplayerButton = new AnimatedRoundButton(
                this,
                "Singleplayer",
                calculateButtonX(index++),
                calculateButtonY(),
                128,
                128,
                Resources.SINGLEPLAYER_ICON,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
        );
        this.addToList(this.singleplayerButton);

        this.multiplayerButton = new AnimatedRoundButton(
                this,
                "Multiplayer",
                calculateButtonX(index++),
                calculateButtonY(),
                128,
                128,
                Resources.MULTIPLAYER_ICON,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
        );
        this.addToList(this.multiplayerButton);

        this.realmsButton = new AnimatedRoundButton(
                this,
                "Realms",
                calculateButtonX(index++),
                calculateButtonY(),
                128,
                128,
                Resources.WORLD_ICON,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
        );
        this.addToList(this.realmsButton);

        this.optionsButton = new AnimatedRoundButton(
                this,
                "Options",
                calculateButtonX(index++),
                calculateButtonY(),
                128,
                128,
                Resources.OPTIONS_ICON,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
        );
        this.addToList(this.optionsButton);

        this.altManagerButton = new AnimatedRoundButton(
                this,
                "Alt Manager",
                calculateButtonX(index),
                calculateButtonY(),
                128,
                128,
                Resources.ALT_ICON,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor(), ClientColors.DEEP_TEAL.getColor())
        );
        this.addToList(this.altManagerButton);

        this.addToList(new Text(
                this,
                "Copyright",
                10,
                this.getHeight() - 31,
                textFont.getWidth(copyright),
                128,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()),
                copyright,
                textFont,
                true
        ));

        this.addToList(new Text(
                this,
                "Version",
                this.getWidth() - textFont.getWidth(versionText) - 9,
                this.getHeight() - 31,
                128,
                128,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()),
                versionText,
                textFont,
                true
        ));

        this.changelogButton = new TextButton(
                this,
                "changelog",
                432,
                24,
                110,
                50,
                new ColorHelper(ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.7F)),
                "Changelog",
                FontUtils.HELVETICA_LIGHT_20
        );
        this.addToList(this.changelogButton);

        this.quitButton = new TextButton(
                this,
                "quit",
                30,
                24,
                50,
                50,
                new ColorHelper(ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.4F)),
                "Exit",
                FontUtils.HELVETICA_LIGHT_20
        );
        this.addToList(this.quitButton);

        this.quitButton.onClick((component, mouseButton) -> {
            ((MainMenuScreen) this.getParent()).startQuitAnimation();
            new Thread(() -> {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ignored) {
                    // stop immediately
                }
                MinecraftClient.getInstance().scheduleStop();
            }).start();
        });

        this.changelogButton.onClick((component, mouseButton) -> ((MainMenuScreen) this.getParent()).showChangelog());
        this.singleplayerButton.onClick((component, mouseButton) -> openMinecraftScreen(new SelectWorldScreen(client.currentScreen)));
        this.multiplayerButton.onClick((component, mouseButton) -> openMinecraftScreen(new MultiplayerScreen(client.currentScreen)));
        this.optionsButton.onClick((component, mouseButton) -> openMinecraftScreen(new OptionsScreen(client.currentScreen, client.options)));
        this.altManagerButton.onClick((component, mouseButton) -> openClientScreen(new AltManagerScreen()));
        this.realmsButton.onClick((component, mouseButton) -> openMinecraftScreen(new RealmsMainScreen(client.currentScreen)));
    }

    @Override
    public void draw(float partialTicks) {
        this.applyScaleTransforms();

        Texture largeLogo = Resources.LOGO_LARGE;
        int imageWidth = largeLogo.getImageWidth();
        int imageHeight = largeLogo.getImageHeight();

        if (Client.INSTANCE.screenManager.scaleFactor > 1.0F) {
            largeLogo = Resources.LOGO_LARGE_2X;
        }

        RenderUtils.drawImage((float) (this.getWidth() / 2 - imageWidth / 2), (float) (this.getHeight() / 2 - imageHeight), (float) imageWidth, (float) imageHeight, largeLogo, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks));

        super.draw(partialTicks);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        this.changelogButton.setX(90);
        this.quitButton.setX(30);
        super.updatePanelDimensions(mouseX, mouseY);
    }

    private void playClickSound() {
        SoundUtils.play("clicksound");
    }

    private void openMinecraftScreen(net.minecraft.client.gui.screen.Screen screen) {
        MinecraftClient.getInstance().openScreen(screen);
        this.playClickSound();
    }

    private void openClientScreen(Screen screen) {
        Client.INSTANCE.screenManager.handle(screen);
        this.playClickSound();
    }

    private int calculateButtonX(int index) {
        return this.getWidth() / 2 - 305 + index * 128 + index * -6;
    }

    private int calculateButtonY() {
        return this.getHeight() / 2 + 14;
    }
}
