package io.github.sst.remake.util.http;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.client.yt.SongData;
import io.github.sst.remake.util.java.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeUtils {
    private static final String YOUTUBE_SEARCH_BASE_URL = "https://www.youtube.com/results?search_query=";
    private static final String YOUTUBE_DEFAULT_FALLBACK_URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

    public static String formatSecondsAsTimestamp(int totalSeconds) {
        if (totalSeconds < 0) {
            totalSeconds = 0;
        }

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return hours + ":"
                    + (minutes >= 10 ? "" : "0") + minutes + ":"
                    + (seconds >= 10 ? "" : "0") + seconds;
        }

        return minutes + ":"
                + (seconds >= 10 ? "" : "0") + seconds;
    }

    public static URL buildYouTubeWatchUrl(String videoId) {
        try {
            return new URL("https://www.youtube.com/watch?v=" + videoId);
        } catch (IOException e) {
            Client.LOGGER.error("Failed to build youtube url for {}", videoId, e);
            return null;
        }
    }

    public static SongData[] getFromChannel(String id) {
        return extractYoutubeThumbnails(NetUtils.getStringFromURL("https://www.youtube.com/@" + id + "/videos?disable_polymer=1"));
    }

    public static SongData[] getFromPlaylist(String id) {
        return extractYoutubeThumbnails(NetUtils.getStringFromURL("https://www.youtube.com/playlist?list=" + id + "&disable_polymer=1"));
    }

    public static SongData[] getFromSearch(String search) {
        String query = StringUtils.encode(search);
        // "sp=EgIQAQ%3D%3D" filters to videos and avoids channel/playlist-only results.
        String url = YOUTUBE_SEARCH_BASE_URL + query + "&sp=EgIQAQ%253D%253D";
        return extractSearchYoutubeThumbnails(NetUtils.getStringFromURL(url));
    }

    private static SongData[] extractYoutubeThumbnails(String htmlContent) {
        htmlContent = StringUtils.normalizeHtmlContent(htmlContent);

        Pattern pattern = Pattern.compile
                (
                        "r\":\\{\"videoId\":\"(.{11})\"(.*?)\"text\":\"(.{1,100})\"\\}]",
                        Pattern.MULTILINE
                );
        Matcher matcher = pattern.matcher(htmlContent);

        List<SongData> videos = new ArrayList<>();
        while (matcher.find()) {
            String id = matcher.group(1);
            String title = StringUtils.decode(StringEscapeUtils.unescapeJava(matcher.group(3)));
            String fullUrl = "https://i.ytimg.com/vi/" + id + "/mqdefault.jpg";

            if (!containsVideo(videos, id)) {
                videos.add(new SongData(id, title, fullUrl));
            }
        }

        return videos.toArray(new SongData[0]);
    }

    private static SongData[] extractSearchYoutubeThumbnails(String htmlContent) {
        htmlContent = StringUtils.normalizeHtmlContent(htmlContent);

        List<SongData> videos = new ArrayList<>();
        Pattern rendererPattern = Pattern.compile("\"videoRenderer\":\\{(.*?)\"thumbnailOverlays\":\\[", Pattern.DOTALL);
        Matcher rendererMatcher = rendererPattern.matcher(htmlContent);

        Pattern idPattern = Pattern.compile("\"videoId\":\"([A-Za-z0-9_-]{11})\"");
        Pattern runsTitlePattern = Pattern.compile("\"title\":\\{\"runs\":\\[\\{\"text\":\"((?:\\\\.|[^\"\\\\]){1,200})\"");
        Pattern simpleTitlePattern = Pattern.compile("\"title\":\\{\"simpleText\":\"((?:\\\\.|[^\"\\\\]){1,200})\"");

        while (rendererMatcher.find()) {
            String rendererChunk = rendererMatcher.group(1);

            Matcher idMatcher = idPattern.matcher(rendererChunk);
            if (!idMatcher.find()) {
                continue;
            }
            String id = idMatcher.group(1);

            if (containsVideo(videos, id)) {
                continue;
            }

            String rawTitle = null;
            Matcher runsTitleMatcher = runsTitlePattern.matcher(rendererChunk);
            if (runsTitleMatcher.find()) {
                rawTitle = runsTitleMatcher.group(1);
            } else {
                Matcher simpleTitleMatcher = simpleTitlePattern.matcher(rendererChunk);
                if (simpleTitleMatcher.find()) {
                    rawTitle = simpleTitleMatcher.group(1);
                }
            }

            if (rawTitle == null || rawTitle.trim().isEmpty()) {
                continue;
            }

            String title = StringUtils.decode(StringEscapeUtils.unescapeJava(rawTitle));
            if (title.isEmpty()) {
                continue;
            }

            String fullUrl = "https://i.ytimg.com/vi/" + id + "/mqdefault.jpg";
            videos.add(new SongData(id, title, fullUrl));
        }

        return videos.toArray(new SongData[0]);
    }

    private static boolean containsVideo(List<SongData> videos, String id) {
        return videos.stream().anyMatch(p -> p != null && p.id.equals(id));
    }
}
