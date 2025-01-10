package dev.sxmurxy.renderutil;

import java.awt.Color;

import dev.sxmurxy.renderutil.common.Lang;
import dev.sxmurxy.renderutil.icon.IconFont;
import dev.sxmurxy.renderutil.icon.IconRenderer;
import dev.sxmurxy.renderutil.simplified.SimplifiedFontRenderer;
import dev.sxmurxy.renderutil.simplified.TextFont;
import dev.sxmurxy.renderutil.styled.StyledFont;
import dev.sxmurxy.renderutil.styled.StyledFontRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class TemplateMod {

	public static final String MOD_ID = "renderutil";
	public static final String FONT_DIR = "/assets/" + TemplateMod.MOD_ID + "/font/";
	private static StyledFont font = new StyledFont("Nunito-Medium.ttf", 35, 0.0f, 2.0f, 0.5f, Lang.ENG_RU);
	private static StyledFont font1 = new StyledFont("Montserrat Medium.ttf", 26, 0.0f, 2.0f, 0.5f, Lang.ENG_RU);
	private static TextFont font2 = new TextFont("Greycliff.ttf", 30, 0.0f, 2.0f, 0.5f, Lang.ENG_RU);
	private static TextFont font3 = new TextFont("Comfortaa.ttf", 35, 0.0f, 2.0f, 0.5f, Lang.ENG_RU);
	private static IconFont font4 = new IconFont("Icons.ttf", 60, 'a', 'b', 'c');

	public static void onRender(MatrixStack matrices) {
		StyledFontRenderer.drawString(matrices, font, "§a§labsc§nd§4egfdg§f§o§linbuu§6g§n§mfd43'543§b§moprb,4g[aa", 60, 120 - font.getFontHeight(), Color.WHITE);
		StyledFontRenderer.drawShadowedString(matrices, font1, "§a§labsc§nd§4перрарк§f§o§linbuu§6g§n§mfd43'543§b§mпепбуааb,4g[aa", 60, 140 - font.getFontHeight(), Color.WHITE);

		SimplifiedFontRenderer.drawString(matrices, font2, "Testing CUSTOMFONT renderer font Minecraft 1.16", 60, 180 - font.getFontHeight(), Color.WHITE);
		SimplifiedFontRenderer.drawString(matrices, font3, "Testing CUSTOMFONT renderer font Minecraft 1.16", 60, 200 - font.getFontHeight(), Color.YELLOW);
		SimplifiedFontRenderer.drawString(matrices, font3, "абвгдеёжзиклмнопрстфухцшщчаюяАБВГДЕЁЖЗИКЛМНОПРСТШЩУФХЦЧЭЮЯ", 60, 270 - font.getFontHeight(), Color.YELLOW);
		SimplifiedFontRenderer.drawString(matrices, font3, "-=)()((*?::%;##", 60, 300 - font.getFontHeight(), Color.BLUE);

		IconRenderer.drawIcon(matrices, font4, 'a', 60, 240 - font.getFontHeight(), Color.WHITE);
		IconRenderer.drawIcon(matrices, font4, 'b', 60 + 30, 240 - font.getFontHeight(), Color.DARK_GRAY);
		IconRenderer.drawIcon(matrices, font4, 'c', 60 + 60, 240 - font.getFontHeight(), Color.WHITE);
	}

}
