package com.skidders.sigma.screen.guis;

import com.mojang.blaze3d.platform.GlStateManager;
import com.skidders.SigmaReborn;
import com.skidders.sigma.util.client.interfaces.ITextures;
import com.skidders.sigma.util.client.render.ColorUtil;
import com.skidders.sigma.util.client.render.image.ImageUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;

public class JelloGUI extends Screen {

    private Texture logo = ITextures.mainMenu_logo;

    public JelloGUI() {
        super(Text.of("Jello Main Menu"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        if (client.overlay != null) {

        } else {
            float parallaxFactor = 0.5F - (float) 0 / (float) client.getWindow().getWidth() * -1.0F;
            float screenScale = (float) width / 1920.0F;
            int backgroundWidth = (int) (600.0F * screenScale);
            int middleWidth = (int) (450.0F * screenScale);
            int foregroundWidth = 0;

            ImageUtil.drawImage(
                    (float) 0 - (float) backgroundWidth * parallaxFactor,
                    (float) 0,
                    (float) (width + backgroundWidth),
                    (float) (height),
                    ITextures.mainMenu_background
            );
            ImageUtil.drawImage(
                    (float) 0 - (float) middleWidth * parallaxFactor,
                    (float) 0,
                    (float) (width + middleWidth),
                    (float) (height),
                    ITextures.mainMenu_middle
            );
            ImageUtil.drawImage(
                    (float) 0 - (float) foregroundWidth * parallaxFactor,
                    (float) 0,
                    (float) (width + foregroundWidth),
                    (float) (height),
                    ITextures.mainMenu_foreground
            );
        }

        GL11.glTranslatef(0 + width / 2f, 0 + height / 2f, 0.0F);
        GL11.glScalef(1.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0 - width / 2f, -0 - height / 2f, 0.0F);

        int imageWidth = logo.getImageWidth() / 2;
        int imageHeight = logo.getImageHeight() / 2;

        if (SigmaReborn.INSTANCE.screenHandler.resizingScaleFactor > 1.0F) {
            logo = ITextures.mainMenu_logo2X;
        }

        ImageUtil.drawImage(
                (float) (width / 2 - imageWidth / 2),
                (float) (height / 2 - imageHeight),
                (float) imageWidth,
                (float) imageHeight,
                logo,
                ColorUtil.applyAlpha(ColorUtil.ClientColors.LIGHT_GREYISH_BLUE.getColor(), delta)
        );
        GlStateManager.enableAlphaTest();
        GL11.glAlphaFunc(519, 0.0F);
        GL11.glTranslatef((float) 0, (float) 0, 0.0F);

    }
}
