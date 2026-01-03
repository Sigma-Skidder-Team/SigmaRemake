package io.github.sst.remake.gui.framework.widget;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.font.TrueTypeFont;

public class Text extends Widget {
    public static ColorHelper defaultColorHelper = new ColorHelper(
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            FontAlignment.LEFT,
            FontAlignment.CENTER
    );
    public boolean shadow = false;

    public Text(GuiComponent screen, String id, int var3, int var4, int var5, int var6, ColorHelper colorHelper, String var8) {
        super(screen, id, var3, var4, var5, var6, colorHelper, var8, false);
    }

    public Text(GuiComponent screen, String id, int var3, int var4, int var5, int var6, ColorHelper colorHelper, String var8, TrueTypeFont font) {
        super(screen, id, var3, var4, var5, var6, colorHelper, var8, font, false);
    }

    public Text(GuiComponent screen, String id, int var3, int var4, int var5, int var6, ColorHelper colorHelper, String var8, TrueTypeFont font, boolean shadow) {
        super(screen, id, var3, var4, var5, var6, colorHelper, var8, font, false);
        this.shadow = shadow;
    }

    @Override
    public void draw(float partialTicks) {
        if (this.shadow) {
            GL11.glAlphaFunc(518, 0.01F);
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_18_BASIC,
                    (float) this.getX(),
                    (float) this.getY(),
                    this.getText(),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks)
            );
            GL11.glAlphaFunc(519, 0.0F);
        }

        if (this.text != null) {
            RenderUtils.drawString(
                    this.getFont(),
                    (float) this.getX(),
                    (float) this.getY(),
                    this.getText(),
                    ColorHelper.applyAlpha(this.textColor.getTextColor(), partialTicks * ColorHelper.getAlpha(this.textColor.getTextColor()))
            );
        }
    }
}
