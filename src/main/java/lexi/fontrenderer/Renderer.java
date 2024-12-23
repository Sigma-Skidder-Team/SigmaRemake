package lexi.fontrenderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lexi.fontrenderer.data.CharacterData;
import lexi.fontrenderer.data.TextureData;
import lexi.fontrenderer.utils.GLUtils;
import lexi.fontrenderer.utils.MathUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Renderer {

    public final Font font;
    public final boolean antiAlias;

    private final boolean fractionalMetrics;
    private final CharacterData[] regularData, boldData, italicsData;
    private final int[] colorCodes;

    public Renderer(ConcurrentLinkedQueue<TextureData> textureQueue, Font font) {
        this(textureQueue, font, 256);
    }

    public Renderer(ConcurrentLinkedQueue<TextureData> textureQueue, Font font, int characterCount) {
        this(textureQueue, font, characterCount, true);
    }

    public Renderer(ConcurrentLinkedQueue<TextureData> textureQueue, Font font, boolean antiAlias) {
        this(textureQueue, font, 256, antiAlias);
    }

    public Renderer(ConcurrentLinkedQueue<TextureData> textureQueue, Font font, int characterCount, boolean antiAlias) {
        this.colorCodes = new int[32];
        this.font = font;
        this.fractionalMetrics = true;
        this.antiAlias = antiAlias;
        int[] regularTexturesIds = new int[characterCount];
        int[] boldTexturesIds = new int[characterCount];
        int[] italicTexturesIds = new int[characterCount];

        GLUtils.ensureOpenGLContext();

        for (int i = 0; i < characterCount; ++i) {
            regularTexturesIds[i] = GL11.glGenTextures();
            boldTexturesIds[i] = GL11.glGenTextures();
            italicTexturesIds[i] = GL11.glGenTextures();
        }

        regularData = setup(new CharacterData[characterCount], regularTexturesIds, textureQueue, Font.PLAIN);
        boldData = setup(new CharacterData[characterCount], boldTexturesIds, textureQueue, Font.BOLD);
        italicsData = setup(new CharacterData[characterCount], italicTexturesIds, textureQueue, Font.ITALIC);
    }

    private CharacterData[] setup(CharacterData[] characterData, int[] texturesIds, ConcurrentLinkedQueue<TextureData> textureQueue, int type) {
        generateColors();

        Font font = this.font.deriveFont(type);
        BufferedImage utilityImage = new BufferedImage(1, 1, 2);

        Graphics2D utilityGraphics = (Graphics2D) utilityImage.getGraphics();
        utilityGraphics.setFont(font);

        FontMetrics fontMetrics = utilityGraphics.getFontMetrics();

        for (int index = 0; index < characterData.length; ++index) {

            char character = (char) index;
            Rectangle2D characterBounds = fontMetrics.getStringBounds(character + "", utilityGraphics);

            float width = (float) characterBounds.getWidth() + 8.0f;
            float height = (float) characterBounds.getHeight();

            BufferedImage characterImage = new BufferedImage(MathUtil.ceiling_double_int(width), MathUtil.ceiling_double_int(height), 2);
            Graphics2D graphics = (Graphics2D) characterImage.getGraphics();

            graphics.setFont(font);
            graphics.setColor(new Color(255, 255, 255, 0));
            graphics.fillRect(0, 0, characterImage.getWidth(), characterImage.getHeight());
            graphics.setColor(Color.WHITE);

            if (antiAlias) {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            }

            graphics.drawString(character + "", 4, fontMetrics.getAscent());
            int textureId = texturesIds[index];

            createTexture(textureId, characterImage, textureQueue);

            characterData[index] = new CharacterData(character, (float) characterImage.getWidth(), (float) characterImage.getHeight(), textureId);
        }

        return characterData;
    }

    private void createTexture(int textureId, BufferedImage image, ConcurrentLinkedQueue<TextureData> textureQueue) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) (pixel >> 16 & 0xFF));
                buffer.put((byte) (pixel >> 8 & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) (pixel >> 24 & 0xFF));
            }
        }

        buffer.flip();
        textureQueue.add(new TextureData(textureId, image.getWidth(), image.getHeight(), buffer));
    }

    private void generateColors() {
        for (int i = 0; i < 32; ++i) {

            int thingy = (i >> 3 & 0x1) * 85;
            int red = (i >> 2 & 0x1) * 170 + thingy;
            int green = (i >> 1 & 0x1) * 170 + thingy;
            int blue = (i & 0x1) * 170 + thingy;

            if (i == 6) {
                red += 85;
            }

            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            colorCodes[i] = ((red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF));
        }
    }

    public void drawString(PoseStack matrixStack, String text, float x, float y, int color) {
        GLUtils.enableAlphaBlending();
        renderString(matrixStack, text, x, y, color, false);
    }

    public void drawString(PoseStack matrixStack, String text, int x, int y, int color) {
        drawString(matrixStack, text, (float) x, (float) y, color);
    }

    public void drawStringWithShadow(PoseStack matrixStack, String text, float x, float y, int color) {
        GL11.glTranslated(0.5, 0.5, 0.0);
        renderString(matrixStack, text, x, y, color, true);

        GL11.glTranslated(-0.5, -0.5, 0.0);
        renderString(matrixStack, text, x, y, color, false);
    }

    public void drawStringWithShadow(PoseStack matrixStack, String text, int x, int y, int color) {
        drawStringWithShadow(matrixStack, text, (float) x, (float) y, color);
    }

    private void renderString(PoseStack matrixStack, String text, float x, float y, int color, boolean shadow) {
        if (text.isEmpty()) {
            return;
        }

        x = Math.round(x * 10.0F) / 10.0F;
        y = Math.round(y * 10.0F) / 10.0F;

        matrixStack.pushPose();
        matrixStack.scale(0.5f, 0.5f, 1.0f);
        GLUtils.enableAlphaBlending();

        x -= 2.0f;
        y -= 2.0f;
        x += 0.5f;
        y += 0.5f;
        x *= 2.0f;
        y *= 2.0f;

        CharacterData[] characterData = regularData;

        boolean underlined = false;
        boolean strikethrough = false;
        boolean obfuscated = false;

        int length = text.length();
        double multiplier = 255.0 * (shadow ? 4 : 1);
        Color c = new Color(color);

        GL11.glColor4d(c.getRed() / multiplier, c.getGreen() / multiplier, c.getBlue() / multiplier, (color >> 24 & 0xFF) / 255.0);

        for (int i = 0; i < length; ++i) {
            char character = text.charAt(i);
            char previous = (i > 0) ? text.charAt(i - 1) : '.';

            if (previous != '§') {
                if (character == '§') {
                    try {
                        int index = "0123456789abcdefklmnor".indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));
                        if (index < 16) {

                            obfuscated = false;
                            strikethrough = false;
                            underlined = false;
                            characterData = regularData;

                            if (index < 0) {
                                index = 15;
                            }

                            if (shadow) {
                                index += 16;
                            }

                            int textColor = colorCodes[index];

                            GL11.glColor4d((textColor >> 16) / 255.0, (textColor >> 8 & 0xFF) / 255.0, (textColor & 0xFF) / 255.0, (color >> 24 & 0xFF) / 255.0);
                        } else if (index <= 20) {
                            switch (index) {
                                case 16 -> obfuscated = true;
                                case 17 -> characterData = boldData;
                                case 18 -> strikethrough = true;
                                case 19 -> underlined = true;
                                case 20 -> characterData = italicsData;
                            }
                        } else {
                            obfuscated = false;
                            strikethrough = false;
                            underlined = false;
                            characterData = regularData;

                            GL11.glColor4d((shadow ? 0.25 : 1.0), (shadow ? 0.25 : 1.0), (shadow ? 0.25 : 1.0), (color >> 24 & 0xFF) / 255.0);
                        }

                    } catch (StringIndexOutOfBoundsException ignored) {
                    }
                } else if (character <= 'ÿ') {
                    if (obfuscated) {
                        character += 1;
                    }

                    GLUtils.drawChar(character, characterData, x, y);
                    CharacterData charData = characterData[character];

                    if (strikethrough) {
                        GLUtils.drawLine(new Vec2f(0.0f, charData.height() / 2.0f), new Vec2f(charData.width(), charData.height() / 2.0f), 3.0f);
                    }

                    if (underlined) {
                        GLUtils.drawLine(new Vec2f(0.0f, charData.height() - 15.0f), new Vec2f(charData.width(), charData.height() - 15.0f), 3.0f);
                    }

                    x += charData.width() - 8.0f;
                }
            }
        }

        matrixStack.pop();

        RenderSystem.disableBlend();
        RenderSystem.bindTexture(0);
        RenderSystem.clearColor(-1.0f, -1.0f, -1.0f, -1.0f);

    }

    public float getWidth(String text) {
        float width = 0.0f;

        try {
            for (int length = text.length(), i = 0; i < length; ++i) {
                char character = text.charAt(i);

                CharacterData charData = regularData[character];
                width += (charData.width() - 8.0f) / 2.0f;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return getWidth("A");
        }

        return width + 2.0f;
    }

    public float getHeight(String text) {
        float height = 0.0f;

        for (int length = text.length(), i = 0; i < length; ++i) {
            char character = text.charAt(i);
            CharacterData charData = regularData[character];

            height = Math.max(height, charData.height());
        }

        return height / 2.0f - 2.0f;
    }

    public float getHeight() {
        return getHeight("I");
    }

}
