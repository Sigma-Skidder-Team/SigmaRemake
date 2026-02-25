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
    public static ColorHelper DEFAULT_TEXT_STYLE = new ColorHelper(
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            ClientColors.DEEP_TEAL.getColor(),
            FontAlignment.LEFT,
            FontAlignment.CENTER
    );
    public boolean shadowEnabled = false;

    public Text(
            GuiComponent parent,
            String id,
            int x,
            int y,
            int width,
            int height,
            ColorHelper colorHelper,
            String text
    ) {
        super(parent, id, x, y, width, height, colorHelper, text, false);
    }

    public Text(
            GuiComponent parent,
            String id,
            int x,
            int y,
            int width,
            int height,
            ColorHelper colorHelper,
            String text,
            TrueTypeFont font
    ) {
        super(parent, id, x, y, width, height, colorHelper, text, font, false);
    }

    public Text(
            GuiComponent parent,
            String id,
            int x,
            int y,
            int width,
            int height,
            ColorHelper colorHelper,
            String text,
            TrueTypeFont font,
            boolean shadowEnabled
    ) {
        super(parent, id, x, y, width, height, colorHelper, text, font, false);
        this.shadowEnabled = shadowEnabled;
    }

    @Override
    public void draw(float partialTicks) {
        if (this.shadowEnabled) {
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
