package io.github.sst.remake.util.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileUtils {

    public static String readFile(File file) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
        } catch (Exception e) {
            return "";
        }

        return content.toString();
    }

}
