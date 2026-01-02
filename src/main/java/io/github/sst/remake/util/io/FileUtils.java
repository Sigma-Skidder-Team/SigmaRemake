package io.github.sst.remake.util.io;

import tv.wunderbox.nfd.FileDialog;
import tv.wunderbox.nfd.FileDialogResult;
import tv.wunderbox.nfd.nfd.NfdFileDialog;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

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

    public static File openTxtFile() {
        NfdFileDialog nfdFileDialog = new NfdFileDialog();

        FileDialog.Filter txtFilter = new FileDialog.Filter(
                "Text Files",
                Collections.singletonList("txt")
        );
        List<FileDialog.Filter> filters = Collections.singletonList(txtFilter);

        FileDialogResult<File> result = nfdFileDialog.pickFile(filters, null);

        if (result instanceof FileDialogResult.Success) {
            FileDialogResult.Success<File> success = (FileDialogResult.Success<File>) result;
            return success.getValue();
        } else {
            return null;
        }
    }

}
