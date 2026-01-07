package io.github.sst.remake.util.system;

import io.github.sst.remake.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtils {

    public static boolean hasPython3_11() {
        String version = getPythonVersion();

        if (version == null) {
            Client.LOGGER.warn("Python is not installed on this system.");
            return false;
        } else if (isVersionAtLeast(version, 3, 11)) {
            return true;
        } else {
            Client.LOGGER.warn("Python exists, but 3.11+ is required, found {}", version);
            return false;
        }
    }

    public static boolean hasFFMPEG() {
        return isToolAvailable("ffmpeg") && isToolAvailable("ffprobe");
    }

    private static String getPythonVersion() {
        String[] aliases = {"python", "python3", "py"};

        for (String alias : aliases) {
            try {
                Process process = new ProcessBuilder(alias, "--version").start();

                try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                     BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                    String line = stdInput.readLine();
                    if (line == null || line.isEmpty()) {
                        line = stdError.readLine();
                    }

                    if (line != null && line.toLowerCase().contains("python")) {
                        return line;
                    }
                }

                process.waitFor();

            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static boolean isVersionAtLeast(String versionOutput, int majorReq, int minorReq) {
        Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        Matcher matcher = pattern.matcher(versionOutput);

        if (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));

            if (major > majorReq) return true;
            if (major == majorReq && minor >= minorReq) return true;
        }
        return false;
    }

    private static boolean isToolAvailable(String toolName) {
        try {
            Process process = new ProcessBuilder(toolName, "-version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

}
