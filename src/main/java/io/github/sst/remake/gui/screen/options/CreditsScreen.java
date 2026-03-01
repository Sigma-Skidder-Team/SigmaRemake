package io.github.sst.remake.gui.screen.options;

import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.gui.framework.widget.Text;
import io.github.sst.remake.gui.screen.holder.OptionsHolder;
import io.github.sst.remake.util.java.StringUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.Base64;

public class CreditsScreen extends Screen {
    public AnimationUtils backgroundFadeAnimation = new AnimationUtils(300, 300);

    public CreditsScreen() {
        super("beta");
        this.setListening(false);

        String encodedCredits = StringUtils.SIGMA_CREDITS_BASE64;
        String decodedCredits = new String(Base64.getDecoder().decode(encodedCredits));

        ScrollablePanel creditsPanel = new ScrollablePanel(this, "sv", 0, 100, this.width, this.height - 100);
        this.addToList(creditsPanel);

        int lineIndex = 0;
        int lineHeight = 20;
        String[] creditLines = decodedCredits.split("\n");

        for (String line : creditLines) {
            creditsPanel.addToList(
                    new Text(
                            creditsPanel,
                            "lbl" + lineIndex,
                            40,
                            40 + lineHeight * lineIndex++,
                            0,
                            0,
                            ColorHelper.DEFAULT_COLOR,
                            line,
                            line.startsWith("*") ? FontUtils.HELVETICA_MEDIUM_20 : FontUtils.HELVETICA_LIGHT_20
                    )
            );
        }
    }

    @Override
    public void draw(float partialTicks) {
        float fade = this.backgroundFadeAnimation.calcPercent();

        RenderUtils.drawRoundedRect2(
                0.0F,
                0.0F,
                (float) this.width,
                (float) this.height,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), fade * 0.95F)
        );

        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_40,
                40.0F,
                40.0F,
                "Credits and third party licensing information",
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );

        super.draw(fade);
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            MinecraftClient.getInstance().openScreen(new OptionsHolder());
        }
    }
}