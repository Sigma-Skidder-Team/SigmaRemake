package io.github.sst.remake.util.client.yt;

import com.jfposton.ytdlp.YtDlp;
import com.jfposton.ytdlp.YtDlpException;
import com.jfposton.ytdlp.YtDlpRequest;
import com.jfposton.ytdlp.YtDlpResponse;
import io.github.sst.remake.Client;
import io.github.sst.remake.util.client.ConfigUtils;
import io.github.sst.remake.util.http.NetUtils;
import io.github.sst.remake.util.system.io.FileUtils;
import io.github.sst.remake.util.java.RandomUtils;
import io.github.sst.remake.util.java.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
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
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String assetName = isWindows ? "yt-dlp.exe" : "yt-dlp";

        File targetFile = new File(ConfigUtils.MUSIC_FOLDER, assetName);
        File versionFile = new File(ConfigUtils.MUSIC_FOLDER, "yt-dlp.version");

        if (targetFile.getParentFile() != null) {
            targetFile.getParentFile().mkdirs();
        }

        String latestBaseUrl = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = NetUtils.getHttpClient();
            latestBaseUrl = resolveLatestAssetBaseUrl(httpClient, assetName);
        } catch (IOException e) {
            Client.LOGGER.warn("Failed to resolve latest YT-DLP asset URL (will fallback to current file if present)", e);
        } finally {
            RandomUtils.closeQuietly(httpClient);
        }

        boolean needsDownload = !targetFile.exists();

        if (!needsDownload && latestBaseUrl != null) {
            String saved = null;
            if (versionFile.exists()) {
                String content = FileUtils.readFile(versionFile).trim();
                if (!content.isEmpty()) {
                    saved = content;
                }
            }

            if (saved == null || !saved.equals(latestBaseUrl)) {
                Client.LOGGER.info("YT-DLP appears outdated (saved={}, latest={}), downloading...", saved, latestBaseUrl);
                needsDownload = true;
            }
        }

        if (needsDownload) {
            Client.LOGGER.info("Downloading YT-DLP...");
            downloadYtDlp(targetFile, assetName);
            if (latestBaseUrl != null) {
                FileUtils.writeFile(versionFile, latestBaseUrl);
            }
        }

        if (!isWindows) {
            targetFile.setExecutable(true);
        }

        YtDlp.setExecutablePath(targetFile.getAbsolutePath());
    }

    private static void downloadYtDlp(File targetFile, String assetName) {
        String downloadUrl = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/" + assetName;

        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = NetUtils.getHttpClient();
            HttpGet request = new HttpGet(downloadUrl);
            response = client.execute(request);

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                Client.LOGGER.error("Failed to download YT-DLP: empty response entity");
                return;
            }

            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(targetFile);
                entity.writeTo(outputStream);
            } finally {
                RandomUtils.closeQuietly(outputStream);
            }
        } catch (IOException e) {
            Client.LOGGER.error("Failed to download YT-DLP", e);
        } finally {
            RandomUtils.closeQuietly(response);
            RandomUtils.closeQuietly(client);
        }
    }

    private static String resolveLatestAssetBaseUrl(CloseableHttpClient client, String assetName) throws IOException {
        String url = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/" + assetName;

        RequestConfig noRedirects = RequestConfig.custom()
                .setRedirectsEnabled(false)
                .build();

        for (int i = 0; i < 10; i++) {
            HttpHead head = new HttpHead(url);
            head.setConfig(noRedirects);

            CloseableHttpResponse response = null;
            try {
                response = client.execute(head);
                int code = response.getStatusLine().getStatusCode();

                if (code >= 300 && code < 400) {
                    Header location = response.getFirstHeader("Location");
                    if (location == null) {
                        break;
                    }
                    url = location.getValue();
                    continue;
                }

                return StringUtils.stripQuery(url);
            } finally {
                RandomUtils.closeQuietly(response);
            }
        }

        return StringUtils.stripQuery(url);
    }
}