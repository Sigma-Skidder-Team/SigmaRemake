package io.github.sst.remake.manager.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.render.RenderHudEvent;
import io.github.sst.remake.gui.screen.notifications.Notification;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ConfigUtils;
import io.github.sst.remake.util.client.yt.PlaylistData;
import io.github.sst.remake.util.client.yt.SongData;
import io.github.sst.remake.util.client.yt.YtDlpUtils;
import io.github.sst.remake.util.http.YoutubeUtils;
import io.github.sst.remake.util.io.audio.stream.MusicStream;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
import io.github.sst.remake.util.system.VersionUtils;
import lombok.Getter;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import org.newdawn.slick.opengl.texture.Texture;
import org.newdawn.slick.util.image.BufferedImageUtil;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class MusicManager extends Manager implements IMinecraft {

    private static final int DEFAULT_THUMBNAIL_X = 70;
    private static final int DEFAULT_THUMBNAIL_Y = 0;
    private static final int DEFAULT_THUMBNAIL_SIZE = 180;

    private long
            totalDuration = 0,
            duration = -1;

    @Getter
    private double playbackProgress = 0;
    private double seekTime = 0;

    public int volume = 50;
    public boolean spectrum = true;
    public int repeat = 0; //0 - NO REPEAT, 1 - REPEAT, 2 - LOOP

    @Getter
    private boolean playing = false;
    private boolean seekRequested = false;
    private boolean processing = false;

    private SourceDataLine dataLine;
    private transient volatile Thread audioThread;

    private PlaylistData playlist;
    @Getter
    private SongData songProcessed;

    private int startVideoIndex;
    private int currentlyPlayingVideoIndex;

    @Getter
    private Texture notification, songThumbnail;
    private BufferedImage thumbnailImage, scaledThumbnailImage;

    @Override
    public void init() {
        if (!ConfigUtils.MUSIC_FOLDER.exists()) {
            ConfigUtils.MUSIC_FOLDER.mkdirs();
        }

        if (!VersionUtils.hasPython3_11() || !VersionUtils.hasFFMPEG()) {
            Client.LOGGER.warn("Music Player will not work, because either Python 3.11 or FFMPEG was not found on your system.");
            return;
        }

        YtDlpUtils.prepareExecutable();
        super.init();
    }

    @Subscribe
    public void onRender(RenderHudEvent event) {
        if (!playing || !spectrum || notification == null) return;

        renderSongTitle();
    }

    private void renderSongTitle() {
        String[] titleParts = songProcessed.title.split(" - ");
        if (titleParts.length <= 1) {
            RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_18_BASIC, 130.0F, (float) (client.getWindow().getHeight() - 70), titleParts[0], ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F));
            RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_18, 130.0F, (float) (client.getWindow().getHeight() - 70), titleParts[0], ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.7F));
        } else {
            RenderUtils.drawString(FontUtils.HELVETICA_MEDIUM_20_BASIC, 130.0F, (float) (client.getWindow().getHeight() - 81), titleParts[0], ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.4F));
            RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_18_BASIC, 130.0F, (float) (client.getWindow().getHeight() - 56), titleParts[1], ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F));
            RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_18, 130.0F, (float) (client.getWindow().getHeight() - 56), titleParts[1], ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.7F));
            RenderUtils.drawString(FontUtils.HELVETICA_MEDIUM_20, 130.0F, (float) (client.getWindow().getHeight() - 81), titleParts[0], ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F));
        }
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) throws IOException {
        if (processing && thumbnailImage != null && scaledThumbnailImage != null && songProcessed == null && !client.isPaused()) {
            if (songThumbnail != null) {
                songThumbnail.release();
            }

            if (notification != null) {
                notification.release();
            }

            songThumbnail = BufferedImageUtil.getTexture("picture", thumbnailImage);
            notification = BufferedImageUtil.getTexture("picture", scaledThumbnailImage);
            Client.INSTANCE.notificationManager.send(new Notification("Now Playing", songProcessed.title, 7000, notification));
            processing = false;
        }

        if (!processing) {
            if (songProcessed != null) {
                new Thread(() -> {
                    try {
                        processThumbnail(songProcessed);
                    } catch (IOException ignored) {
                    }
                }).start();
            }
        }
    }

    public void setPlaying(boolean playing) {
        if (!playing && dataLine != null) {
            dataLine.flush();
        }

        this.playing = playing;
    }

    public void play(PlaylistData playlist, SongData song) {
        if (playlist == null) {
            playlist = new PlaylistData("temp");
            playlist.songs.add(song);
        }

        this.playlist = playlist;
        playing = true;
        totalDuration = 0;
        playbackProgress = 0;

        for (int i = 0; i < playlist.songs.size(); i++) {
            if (playlist.songs.get(i) == song) {
                startVideoIndex = i;
            }
        }

        initPlaybackLoop();
    }

    public void playNext() {
        if (playlist != null) {
            startVideoIndex = currentlyPlayingVideoIndex + 1;
            totalDuration = 0;
            playbackProgress = 0;
            initPlaybackLoop();
        }
    }

    public void playPrevious() {
        if (playlist != null) {
            startVideoIndex = currentlyPlayingVideoIndex - 1;
            totalDuration = 0;
            playbackProgress = 0;
            initPlaybackLoop();
        }
    }

    public void seekTo(double seekTime) {
        this.seekTime = seekTime;
        totalDuration = (long) seekTime;
        seekRequested = true;
    }

    public int getDuration() {
        return (int) this.duration;
    }

    public int getTotalDuration() {
        return (int) this.totalDuration;
    }

    private void initPlaybackLoop() {
        boolean bypass = true;
        if (playlist != null || bypass) {
            if (audioThread != null && audioThread.isAlive()) {
                audioThread.interrupt();
            }

            audioThread = new Thread(this::playbackLoop);
            audioThread.start();
        }
    }

    private void playbackLoop() {
        if (startVideoIndex < 0 || startVideoIndex >= playlist.songs.size()) {
            startVideoIndex = 0;
        }

        for (int i = startVideoIndex; i < playlist.songs.size(); i++) {
            while (!this.playing) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                }

                if (Thread.interrupted()) {
                    return;
                }
            }

            try {
                currentlyPlayingVideoIndex = i;
                playTrack(playlist.songs.get(i));
            } catch (Exception e) {
                Client.LOGGER.error("Failed to play track", e);
            }
        }
    }

    private void playTrack(SongData data) throws IOException, LineUnavailableException, InterruptedException {
        URL videoStreamUrl = YoutubeUtils.buildYouTubeWatchUrl(data.id);
        assert videoStreamUrl != null;
        URL audioStreamUrl = YtDlpUtils.resolveStream(videoStreamUrl.toString());

        if (audioStreamUrl == null) {
            Thread.sleep(1000);
            return;
        }

        MusicStream mS = new MusicStream(getConnection(videoStreamUrl).getInputStream());

        MP4Container container = new MP4Container(mS);

        Movie movie = container.getMovie();
        AudioTrack track = (AudioTrack) movie.getTracks().get(1);

        if (track == null) {
            Client.LOGGER.error("No audio track found in the stream.");
            return;
        }

        AudioFormat audioFormat = new AudioFormat((float) track.getSampleRate(), track.getSampleSize(), track.getChannelCount(), true, true);
        dataLine = AudioSystem.getSourceDataLine(audioFormat);
        dataLine.open();
        dataLine.start();
        duration = (long) movie.getDuration();

        if (duration > 1300) {
            mS.close();
            Client.INSTANCE.notificationManager.send(new Notification("Now Playing", "Music is too long."));
        }

        songProcessed = data;
        streamAudioData(track, mS);
    }


    private void streamAudioData(AudioTrack track, MusicStream mS) throws InterruptedException, IOException {
        Decoder aacDecoder = new Decoder(track.getDecoderSpecificInfo());
        SampleBuffer sampleBuffer = new SampleBuffer();

        while (track.hasMoreFrames()) {
            while (!playing) {
                Thread.sleep(300);
                if (Thread.interrupted()) {
                    dataLine.close();
                    return;
                }
            }

            Frame frame = track.readNextFrame();
            aacDecoder.decodeFrame(frame.getData(), sampleBuffer);

            adjustVolume(dataLine, volume);

            if (!Thread.interrupted()) {
                totalDuration = Math.round(track.getNextTimeStamp());
                playbackProgress = track.getAvailableEndTime();
                if (seekRequested) {
                    track.seek(seekTime);
                    totalDuration = (long) seekTime;
                    seekRequested = false;
                }
            }

            if (!track.hasMoreFrames()
                    && (repeat == 2
                    || repeat == 1)) {
                track.seek(0);
                totalDuration = 0;
            }

            if (Thread.interrupted()) {
                dataLine.close();
                return;
            }
        }

        dataLine.close();
        mS.close();
    }

    private void adjustVolume(SourceDataLine source, int volume) {
        try {
            FloatControl gainControl = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
            BooleanControl muteControl = (BooleanControl) source.getControl(javax.sound.sampled.BooleanControl.Type.MUTE);

            if (volume == 0) {
                muteControl.setValue(true);
            } else {
                muteControl.setValue(false);
                gainControl.setValue((float) (Math.log((double) volume / 100.0) / Math.log(10.0) * 20.0));
            }
        } catch (Exception e) {
            Client.LOGGER.warn("Failed to adjust volume to {}", volume, e);
        }
    }

    private URLConnection getConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(14000);
        connection.setReadTimeout(14000);
        connection.setUseCaches(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        return connection;
    }

    private void processThumbnail(SongData data) throws IOException {
        processing = true;
        BufferedImage buffImage = ImageIO.read(new URL(data.url));
        thumbnailImage = ImageUtils.applyBlur(buffImage, 15);
        thumbnailImage = thumbnailImage.getSubimage(0, (int) ((float) thumbnailImage.getHeight() * 0.75F), thumbnailImage.getWidth(), (int) ((float) thumbnailImage.getHeight() * 0.2F));

        if (buffImage.getHeight() != buffImage.getWidth()) {
            scaledThumbnailImage = buffImage.getSubimage(DEFAULT_THUMBNAIL_X, DEFAULT_THUMBNAIL_Y, DEFAULT_THUMBNAIL_SIZE, DEFAULT_THUMBNAIL_SIZE);
        } else {
            scaledThumbnailImage = buffImage;
        }

        songProcessed = null;
    }

}