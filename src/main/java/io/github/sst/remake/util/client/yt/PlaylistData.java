package io.github.sst.remake.util.client.yt;

import io.github.sst.remake.util.http.YoutubeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaylistData {
    public String name;
    public String id;
    public DataType type;
    public boolean updated;
    public final List<SongData> songs = new ArrayList<>();

    public PlaylistData(String id) {
        this(id, id, DataType.PLAYLIST);
    }

    public PlaylistData(String name, String id) {
        this(name, id, DataType.PLAYLIST);
    }

    public PlaylistData(String name, String id, DataType type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public void refresh() {
        songs.clear();

        SongData[] thumbnails = new SongData[0];

        if (type == DataType.CHANNEL) {
            thumbnails = YoutubeUtils.getFromChannel(id);
        }

        if (type == DataType.PLAYLIST) {
            thumbnails = YoutubeUtils.getFromPlaylist(id);
        }

        for (SongData thumbnail : thumbnails) {
            songs.add(new SongData(thumbnail.id, thumbnail.title, thumbnail.url));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlaylistData)) return false;
        PlaylistData that = (PlaylistData) o;
        return Objects.equals(id, that.id);
    }
}
