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
    private static final String GOOGLE_SEARCH_BASE_URL = "https://www.google.com/search?client=safari&num=21&gbv=1&tbm=vid&q=site:youtube.com/watch+music+";
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
        return extractSearchYoutubeThumbnails(NetUtils.getStringFromURL(GOOGLE_SEARCH_BASE_URL + StringUtils.encode(search)));
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

        Pattern pattern = Pattern.compile
                (
                        "<a(.*?)watch%3Fv%3D(.{11})[\"&](.*?)><div (.*?)>(.{1,100}) - YouTube</div></h3>",
                        Pattern.MULTILINE
                );
        Matcher matcher = pattern.matcher(htmlContent);

        List<SongData> videos = new ArrayList<>();
        while (matcher.find()) {
            String id = matcher.group(2);
            String rawTitle = matcher.group(5);

            if (rawTitle.contains("</")
                    || rawTitle.isEmpty()
                    || rawTitle.trim().isEmpty()
                    || matcher.group(1).contains("play-all")) {
                continue;
            }

            if (containsVideo(videos, id)) {
                continue;
            }

            String title = StringUtils.decode(StringEscapeUtils.unescapeJava(rawTitle.replaceAll("<(.*?)>", "")));

            if (!containsVideo(videos, id)) {
                videos.add(new SongData(id, title));
            }
        }

        return videos.toArray(new SongData[0]);
    }

    private static boolean containsVideo(List<SongData> videos, String id) {
        return videos.stream().anyMatch(p -> p != null && p.id.equals(id));
    }
}