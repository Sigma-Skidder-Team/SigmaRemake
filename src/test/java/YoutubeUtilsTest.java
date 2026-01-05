import io.github.sst.remake.util.client.yt.SongData;
import io.github.sst.remake.util.http.YoutubeUtils;

public class YoutubeUtilsTest {

    public static void main(String[] args) {
        String playlistId = "PLZ3ISMURKOeGYWPTSuqA1ZvpWMvhx7bRC"; // A common test playlist ID
        System.out.println("Attempting to extract songs from playlist: " + playlistId);

        try {
            SongData[] songs = YoutubeUtils.getFromPlaylist(playlistId);

            if (songs.length == 0) {
                System.out.println("No songs extracted or an error occurred during extraction.");
            } else {
                System.out.println("Successfully extracted " + songs.length + " songs:");
                for (int i = 0; i < songs.length; i++) {
                    SongData song = songs[i];
                    System.out.println("  Song " + (i + 1) + ":");
                    System.out.println("    ID: " + song.id);
                    System.out.println("    Title: " + song.title);
                    System.out.println("    Full URL: " + song.url);
                }
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during playlist extraction:");
            e.printStackTrace();
        }
    }
}
