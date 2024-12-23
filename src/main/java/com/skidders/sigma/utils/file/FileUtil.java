package com.skidders.sigma.utils.file;

import com.mojang.blaze3d.systems.RenderSystem;
import com.skidders.SigmaReborn;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.TextureUtil;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static void createFolder(String name) {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getRunningPath() {
        return Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "sigma");
    }

    public static java.util.List<String> getFilesInDirectory(String directoryPath) throws IOException {
        List<String> filenames = new ArrayList<>();

        Path path = Paths.get(directoryPath);
        if (Files.exists(path) && Files.isDirectory(path)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                for (Path entry : directoryStream) {
                    String filename = entry.getFileName().toString();
                    filenames.add(filename);
                }
            }
        } else {
            SigmaReborn.LOGGER.info("Directory not found or is not a directory: {}", directoryPath);
        }

        return filenames;
    }

    public static void copyResourceToFile(String sourceFileName, String destinationPath) throws IOException {
        try (InputStream in = FileUtil.class.getResourceAsStream(sourceFileName); OutputStream out = new FileOutputStream(destinationPath)) {
            System.out.println("Getting " + sourceFileName);
            System.out.println("Sending to " + destinationPath);

            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + sourceFileName);
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

}
