package io.github.sst.remake.util.client.yt;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SongData {
    public final String id, title, url;

    public SongData(String id, String title) {
        this(id, title, "");
    }

    public String getThumbnailUrl() {
        return "https://i.ytimg.com/vi/" + id + "/mqdefault.jpg";
    }
}
