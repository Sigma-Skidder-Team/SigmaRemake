package io.github.sst.remake.util.client.yt;

import com.jfposton.ytdlp.YtDlp;
import com.jfposton.ytdlp.YtDlpException;
import com.jfposton.ytdlp.YtDlpRequest;
import com.jfposton.ytdlp.YtDlpResponse;
import io.github.sst.remake.Client;
import io.github.sst.remake.util.client.ConfigUtils;
import io.github.sst.remake.util.http.NetUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class YtDlpUtils {

    public static URL resolveStream(String songUrl) {
        YtDlpRequest request = new YtDlpRequest(songUrl, ConfigUtils.MUSIC_FOLDER.getAbsolutePath());
        request.addOption("no-check-certificates");
        request.addOption("rm-cache-dir");
        request.addOption("get-url");
        request.addOption("retries", 10);
        request.addOption("format", 18);

        try {
            YtDlpResponse response = YtDlp.execute(request);
            return new URL(response.getOut());
        } catch (YtDlpException | MalformedURLException e) {
            Client.LOGGER.error("Failed to grab response from YT-DLP", e);
        }

        return null;
    }

    public static void prepareExecutable() {
        File targetFile = new File(ConfigUtils.MUSIC_FOLDER, "yt-dlp");

        if (!targetFile.exists()) {
            Client.LOGGER.info("YT-DLP was not found, downloading...");
            downloadYtDlp(targetFile);
        }

        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            targetFile.setExecutable(true);
        }

        YtDlp.setExecutablePath(targetFile.getAbsolutePath());
    }

    private static void downloadYtDlp(File targetFile) {
        String downloadUrl = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp";

        targetFile.getParentFile().mkdirs();

        try (CloseableHttpClient client = NetUtils.getHttpClient()) {
            HttpGet request = new HttpGet(downloadUrl);
            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                        entity.writeTo(outputStream);
                    }
                }
            }
        } catch (IOException e) {
            Client.LOGGER.error("Failed to download YT-DLP", e);
        }
    }

}