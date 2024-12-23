package lexi.fontrenderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import info.opensigma.OpenSigma;
import lexi.fontrenderer.data.CharacterData;
import lexi.fontrenderer.data.TextureData;
import lexi.fontrenderer.utils.FileUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.opengl.GL11.*;

public class FontManager {

    public final List<String> availableFonts = new ArrayList<>();

    private final HashMap<String, Renderer> fonts = new HashMap<>();
    private Renderer defaultFont;
    private ConcurrentLinkedQueue<TextureData> textureQueue;

    public void init() {
        String sourcePath = "/assets/opensigma/fonts/";
        String destinationPath = "sigma/fonts/";

        try {
            File tempDir = new File(destinationPath);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            File folder = new File(getClass().getResource(sourcePath).toURI());
            if (folder.exists() && folder.isDirectory()) {
                for (File file : folder.listFiles()) {
                    if (file.isFile()) {
                        copyResourceToFile(file.getName(), destinationPath + file.getName());
                    }
                }
            } else {
                throw new FileNotFoundException("Resource directory not found or not a directory: " + sourcePath);
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        this.textureQueue = new ConcurrentLinkedQueue<>();
        this.defaultFont = new Renderer(textureQueue, new Font("Roboto-Regular", Font.PLAIN, 16));
        OpenSigma.Companion.getLOGGER().info("set the default font to: {}", defaultFont.font.getFontName());

        try {
            OpenSigma.Companion.getLOGGER().info("{}", FileUtil.getFilesInDirectory("sigma/fonts/"));

            for (String fontFile : FileUtil.getFilesInDirectory("sigma/fonts/")) {
                String fontName = fontFile.substring(0, fontFile.lastIndexOf('.'));
                availableFonts.add(fontName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        OpenSigma.Companion.getLOGGER().info("Finished loading fonts");
    }

    private void copyResourceToFile(String sourceFileName, String destinationPath) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/assets/opensigma/fonts/" + sourceFileName);
             OutputStream out = new FileOutputStream(destinationPath)) {
            if (in == null) {
                throw new FileNotFoundException("Font resource not found: " + sourceFileName);
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private void loadFont(String fontPath, String fontName, int[] sizes) {
        for (int size : sizes) {
            Path fontFile = Paths.get(fontPath);
            if (Files.exists(fontFile)) {
                try (InputStream iStream = Files.newInputStream(fontFile)) {
                    Font myFont = Font.createFont(Font.TRUETYPE_FONT, iStream);
                    myFont = myFont.deriveFont(Font.PLAIN, (float) size);
                    this.fonts.put(fontName + " " + size, new Renderer(textureQueue, myFont));
                    return;
                } catch (IOException | FontFormatException e) {
                    OpenSigma.Companion.getLOGGER().error("Failed to load font: {} due to {}", fontPath, e.getMessage());
                }
            } else {
                OpenSigma.Companion.getLOGGER().info("Font file not found: {}", fontPath);
            }
        }
    }

    public Renderer getFont(final String key, final int size) {
        if (fonts.containsKey(key)) {
            return fonts.get(key);
        } else {
            try {
                final String[] split = key.split(" "),
                        nameSplit = split[0].split("-");

                final String family = nameSplit[0],
                        style = nameSplit[1];

                String path = FileUtil.getRunningPath() + "/fonts/" + family + "-" + style + ".ttf";
                loadFont(path, family + "-" + style, new int[]{size});

                while (!textureQueue.isEmpty()) {
                    final TextureData textureData = textureQueue.poll();
                    RenderSystem.bindTexture(textureData.textureId());
                    GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                    GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                    GL11.glTexImage2D(GL_TEXTURE_2D, GL_ZERO, GL_RGBA, textureData.width(), textureData.height(), GL_ZERO, GL_RGBA, GL_UNSIGNED_BYTE, textureData.buffer());
                }

                String fixedKey = key + " " + size;
                if (fonts.containsKey(fixedKey)) {
                    return fonts.get(fixedKey);
                } else {
                    OpenSigma.Companion.getLOGGER().error("Failed to create font {}", key);
                    return defaultFont;
                }
            } catch (Exception e) {
                OpenSigma.Companion.getLOGGER().error("Failed to create font {} due to {}", key, e);
                return defaultFont;
            }
        }
    }

}
