package com.skidders.sigma.util.system.file;

import com.skidders.SigmaReborn;
import net.minecraft.client.MinecraftClient;

import java.io.*;
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
        return Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "sigma5");
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
