package com.skidders.sigma.util.system.file;

import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    public static void createFolder(String name) {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getRunningPath() {
        return Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "sigma5");
    }

    public static void copyResourceToFile(String sourceFileName, String destinationPath) throws IOException {
        try (InputStream in = FileUtil.class.getResourceAsStream(sourceFileName); OutputStream out = new FileOutputStream(destinationPath)) {
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
