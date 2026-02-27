package io.github.sst.remake.manager.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.render.RenderHudEvent;
import io.github.sst.remake.gui.screen.notifications.Notification;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.client.ConfigUtils;
import io.github.sst.remake.util.client.yt.PlaylistData;
import io.github.sst.remake.util.client.yt.SongData;
import io.github.sst.remake.util.client.yt.YtDlpUtils;
import io.github.sst.remake.util.http.NetUtils;
import io.github.sst.remake.util.http.YoutubeUtils;
import io.github.sst.remake.util.system.io.audio.stream.MusicStream;
import io.github.sst.remake.util.math.fft.JavaFFT;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.shader.StencilUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
import io.github.sst.remake.util.render.image.Resources;
import io.github.sst.remake.util.system.VersionUtils;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.texture.Texture;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class MusicManager extends Manager implements IMinecraft {
    private static final int DEFAULT_THUMBNAIL_SIZE = 180;
    private static final int BLURRED_BANNER_WIDTH = 512;
    private static final int BLURRED_BANNER_HEIGHT = 32;

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
    private volatile boolean playing = false;
    private volatile boolean seekRequested = false;
    private volatile boolean processing = false;

    private SourceDataLine dataLine;
    private transient volatile Thread audioThread;
    private final AtomicInteger playbackSession = new AtomicInteger(0);
    private final AtomicInteger thumbnailProcessGeneration = new AtomicInteger(0);

    private PlaylistData playlist;
    @Getter
    private volatile SongData currentPlayingSongData;
    private volatile SongData thumbnailProcessingSongData;
    private volatile SongData thumbnailPreparedForSong;

    public SongData getSongProcessed() {
        return currentPlayingSongData;
    }

    private int startVideoIndex;
    private int currentlyPlayingVideoIndex;

    @Getter
    private Texture notification, songThumbnail;
    private volatile BufferedImage thumbnailImage, scaledThumbnailImage;
    private volatile boolean thumbnailFailed = false;

    public List<PlaylistData> playlists;

    private List<double[]> visualizer;
    private List<Double> amplitudes;

    @Override
    public void init() {
        playlists = new ArrayList<>();
        visualizer = new ArrayList<>();
        amplitudes = new ArrayList<>();

        if (!ConfigUtils.MUSIC_FOLDER.exists()) {
            ConfigUtils.MUSIC_FOLDER.mkdirs();
        }

        YtDlpUtils.prepareExecutable();

        boolean hasPy = VersionUtils.hasPython3_11();
        boolean hasFF = VersionUtils.hasFFMPEG();

        if (!hasPy || !hasFF) {
            Client.LOGGER.warn("Music Player will not work, because either Python 3.11 or FFMPEG was not found on your system.");
        }

        playlists.add(new PlaylistData("Trap Nation", "PLC1og_v3eb4hrv4wsqG1G5dsNZh9bIscJ"));
        playlists.add(new PlaylistData("Chill Nation", "PL3EfCK9aCbkptFjtgWYJ8wiXgJQw5k3M3"));
        playlists.add(new PlaylistData("VEVO", "PL9tY0BWXOZFu8MzzbNVtUvHs0cQ_gZ03m"));
        playlists.add(new PlaylistData("Rap Nation", "PLayVKgoNNljOZifkJNtvwfmrmh2OglYzx"));
        playlists.add(new PlaylistData("MrSuicideSheep", "PLyqoPTKp-zlrI_PEqytQ7J9FgPhptcC64"));
        playlists.add(new PlaylistData("Trap City", "PLU_bQfSFrM2PemIeyVUSjZjJhm6G7auOY"));
        playlists.add(new PlaylistData("CloudKid", "PLejelFTZDTZM1yOroUyveJkjE7IY9Zj73"));
        playlists.add(new PlaylistData("NCS", "PLRBp0Fe2Gpgm_u2w2a2isHw29SugZ34cD"));

        super.init();
    }

    @Override
    public void shutdown() {
        playbackSession.incrementAndGet();
        Thread thread = audioThread;
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        audioThread = null;
        if (dataLine != null) {
            try {
                dataLine.stop();
                dataLine.flush();
                dataLine.close();
            } catch (Exception ignored) {
            }
        }

        notification = null;
        songThumbnail = null;
        dataLine = null;

        thumbnailImage = null;
        scaledThumbnailImage = null;
        thumbnailFailed = false;
        thumbnailPreparedForSong = null;

        playing = false;
        playlist = null;
        
        currentPlayingSongData = null;
        thumbnailProcessingSongData = null;

        visualizer.clear();
        amplitudes.clear();
        playlists.clear();

        super.shutdown();
    }

    @Subscribe
    public void onRender(RenderHudEvent ignoredEvent) {
        if (!playing || !spectrum || notification == null) return;
        if (visualizer.isEmpty() || amplitudes.isEmpty()) return;

        renderBars();
        renderThumbnail();
        renderSongTitle();
        RenderUtils.resetHudGlState();
    }

    @Subscribe
    public void onRender2D(RenderClient2DEvent ignoredEvent) {
        if (!playing || visualizer.isEmpty()) return;

        double[] visualize = visualizer.get(0);

        if (amplitudes.size() < visualize.length) {
            for (int i = amplitudes.size(); i < visualize.length; i++) {
                amplitudes.add(visualize[i]);
            }
        }

        int currentFps = Math.max(1, MinecraftClient.currentFps);
        float fps = 60.0F / (float) currentFps;

        for (int i = 0; i < visualize.length; i++) {
            double var7 = amplitudes.get(i) - visualize[i];
            boolean over = !(amplitudes.get(i) < Double.MAX_VALUE);
            amplitudes.set(i, Math.min(2.256E7, Math.max(0.0, amplitudes.get(i) - var7 * (double) Math.min(0.335F * fps, 1.0F))));
            if (over) {
                amplitudes.set(i, 0.0);
            }
        }
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent ignoredEvent) throws IOException {
        if (!playing) {
            visualizer.clear();
            amplitudes.clear();
        }

        if (processing) {
            if (thumbnailImage != null
                    && scaledThumbnailImage != null
                    && thumbnailPreparedForSong != null
                    && isSameSong(currentPlayingSongData, thumbnailPreparedForSong)
                    && !client.isPaused()) {
                if (songThumbnail != null && songThumbnail != Resources.ARTWORK) {
                    songThumbnail.release();
                }

                if (notification != null && notification != Resources.ARTWORK) {
                    notification.release();
                }

                thumbnailImage = ImageUtils.toCompatibleImageType(thumbnailImage);
                scaledThumbnailImage = ImageUtils.toCompatibleImageType(scaledThumbnailImage);
                RenderUtils.resetGlUnpackState();

                String textureKey = "music-" + currentPlayingSongData.id + "-" + System.nanoTime();
                songThumbnail = ImageUtils.createTexture(textureKey + "-bg", thumbnailImage);
                notification = ImageUtils.createTexture(textureKey + "-cover", scaledThumbnailImage);
                thumbnailFailed = false;
                Client.INSTANCE.notificationManager
                        .send(new Notification("Now Playing", currentPlayingSongData.title, 7000, notification));
                thumbnailProcessingSongData = null;
                processing = false;
            } else if (thumbnailFailed
                    && currentPlayingSongData != null
                    && thumbnailPreparedForSong != null
                    && isSameSong(currentPlayingSongData, thumbnailPreparedForSong)
                    && !client.isPaused()) {
                if (songThumbnail != null && songThumbnail != Resources.ARTWORK) {
                    songThumbnail.release();
                }
                if (notification != null && notification != Resources.ARTWORK) {
                    notification.release();
                }
                songThumbnail = Resources.ARTWORK;
                notification = Resources.ARTWORK;
                thumbnailProcessingSongData = null;
                processing = false;
            } else if (thumbnailPreparedForSong != null
                    && currentPlayingSongData != null
                    && !isSameSong(currentPlayingSongData, thumbnailPreparedForSong)) {
                // Drop stale processed thumbnails if playback switched songs before upload.
                thumbnailImage = null;
                scaledThumbnailImage = null;
                thumbnailFailed = false;
                processing = false;
                thumbnailPreparedForSong = null;
            }
        }

        if (thumbnailProcessingSongData != null) {
            SongData songData = thumbnailProcessingSongData;
            thumbnailProcessingSongData = null;
            visualizer.clear();
            int generation = thumbnailProcessGeneration.incrementAndGet();
            new Thread(() -> {
                try {
                    processThumbnail(songData, generation);
                } catch (Exception e) {
                    if (generation != thumbnailProcessGeneration.get()) {
                        return;
                    }
                    thumbnailFailed = true;
                    processing = false;
                    thumbnailProcessingSongData = null;
                }
            }).start();
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
        resetPlaybackTimers();

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
            resetPlaybackTimers();
            initPlaybackLoop();
        }
    }

    public void playPrevious() {
        if (playlist != null) {
            startVideoIndex = currentlyPlayingVideoIndex - 1;
            resetPlaybackTimers();
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

    private void resetPlaybackTimers() {
        totalDuration = 0;
        duration = -1;
        playbackProgress = 0;
    }

    private void initPlaybackLoop() {
        visualizer.clear();
        if (playlist != null) {
            int sessionId = playbackSession.incrementAndGet();
            Thread previous = audioThread;
            if (previous != null && previous.isAlive()) {
                previous.interrupt();
            }
            if (dataLine != null) {
                dataLine.stop();
                dataLine.flush();
                dataLine.close();
                dataLine = null;
            }
            audioThread = new Thread(() -> playbackLoop(sessionId), "Sigma-Music-" + sessionId);
            audioThread.setDaemon(true);
            audioThread.start();
        }
    }

    private boolean shouldStopPlayback(int sessionId) {
        return Thread.currentThread().isInterrupted() || sessionId != playbackSession.get();
    }

    private void playbackLoop(int sessionId) {
        if (startVideoIndex < 0 || startVideoIndex >= playlist.songs.size()) {
            startVideoIndex = 0;
        }

        for (int i = startVideoIndex; i < playlist.songs.size(); i++) {
            try {
                if (shouldStopPlayback(sessionId)) {
                    return;
                }
                currentlyPlayingVideoIndex = i;
                currentPlayingSongData = playlist.songs.get(i);
                visualizer.clear();

                while (!this.playing) {
                    if (shouldStopPlayback(sessionId)) {
                        if (dataLine != null) {
                            dataLine.close();
                        }
                        return;
                    }
                    Thread.sleep(300);

                    visualizer.clear();
                }

                playTrack(currentPlayingSongData, sessionId);

                if (shouldStopPlayback(sessionId)) {
                    return;
                }

                switch (repeat) {
                    case 2:
                        i--;
                        break;
                    case 1:
                        if (i == playlist.songs.size() - 1)
                            i = -1;
                        break;
                    case 0:
                        return;
                }

                if (i >= playlist.songs.size()) {
                    i = 0;
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                Client.LOGGER.error("Failed to play track", e);
            }
        }
    }

    private void playTrack(SongData data, int sessionId) throws IOException, LineUnavailableException, InterruptedException {
        if (shouldStopPlayback(sessionId)) {
            return;
        }
        thumbnailProcessGeneration.incrementAndGet();
        thumbnailProcessingSongData = data;
        thumbnailPreparedForSong = data;
        thumbnailFailed = false;

        URL videoStreamUrl = YoutubeUtils.buildYouTubeWatchUrl(data.id);
        URL audioStreamUrl = YtDlpUtils.resolveStream(videoStreamUrl.toString());

        if (audioStreamUrl == null) {
            URL fallbackUrl = YtDlpUtils.resolveFallbackStream(videoStreamUrl.toString());
            if (fallbackUrl != null && VersionUtils.hasFFMPEG() && !shouldStopPlayback(sessionId)) {
                AudioFormat fallbackFormat = new AudioFormat(44100.0F, 16, 2, true, true);
                streamAudioDataWithFfmpeg(fallbackUrl, fallbackFormat, sessionId);
                return;
            }
            if (!shouldStopPlayback(sessionId)) {
                Thread.sleep(1000);
            }
            return;
        }

        MusicStream mS = new MusicStream(NetUtils.getConnection(audioStreamUrl).getInputStream());

        MP4Container container = new MP4Container(mS);

        Movie movie = container.getMovie();
        AudioTrack track = selectAudioTrack(movie, data);
        AudioFormat audioFormat = new AudioFormat((float) track.getSampleRate(), 16, track.getChannelCount(), true, true);

        dataLine = AudioSystem.getSourceDataLine(audioFormat);
        dataLine.open();
        dataLine.start();
        duration = (long) movie.getDuration();

        if (duration > 1300) {
            mS.close();
            Client.INSTANCE.notificationManager.send(new Notification("Song skipped", "Music is too long.", Resources.ALERT_ICON));
        }

        try {
            streamAudioData(track, mS, audioFormat, sessionId);
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw (InterruptedException) e;
            }

            if (isAacDecodeFailure(e) && !shouldStopPlayback(sessionId)) {
                if (VersionUtils.hasFFMPEG()) {
                    try {
                        mS.close();
                    } catch (IOException ignored) {
                    }
                    if (dataLine != null) {
                        dataLine.close();
                    }
                    streamAudioDataWithFfmpeg(audioStreamUrl, audioFormat, sessionId);
                    return;
                }
            }

            if (e instanceof IOException) {
                throw (IOException) e;
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IOException("Unexpected playback failure", e);
        }
    }

    private void streamAudioData(AudioTrack track, MusicStream mS, AudioFormat audioFormat, int sessionId) throws InterruptedException, IOException {
        Decoder aacDecoder = new Decoder(track.getDecoderSpecificInfo());
        SampleBuffer sampleBuffer = new SampleBuffer();

        while (track.hasMoreFrames()) {
            while (!playing) {
                if (shouldStopPlayback(sessionId)) {
                    if (dataLine != null) {
                        dataLine.close();
                    }
                    return;
                }
                Thread.sleep(300);
                visualizer.clear();
            }

            if (shouldStopPlayback(sessionId)) {
                if (dataLine != null) {
                    dataLine.close();
                }
                return;
            }

            Frame frame = track.readNextFrame();
            byte[] frameData = frame.getData();
            aacDecoder.decodeFrame(frameData, sampleBuffer);

            byte[] pcmBufferData = sampleBuffer.getData();
            dataLine.write(pcmBufferData, 0, pcmBufferData.length);

            float[] pcmSamples = JavaFFT.convertToPCMFloatArray(pcmBufferData, audioFormat);
            JavaFFT fftProcessor = new JavaFFT(pcmSamples.length);

            float[][] fftResult = fftProcessor.transform(pcmSamples);
            float[] realSpectrum = fftResult[0];
            float[] imaginarySpectrum = fftResult[1];

            visualizer.add(JavaFFT.calculateAmplitudes(realSpectrum, imaginarySpectrum));
            if (visualizer.size() > 18) {
                visualizer.remove(0);
            }

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
                    || repeat == 1)
                    && playlist.songs.size() == 1
            ) {
                track.seek(0);
                totalDuration = 0;
            }

            if (shouldStopPlayback(sessionId)) {
                dataLine.close();
                return;
            }
        }

        dataLine.close();
        mS.close();
    }

    private void streamAudioDataWithFfmpeg(URL audioStreamUrl, AudioFormat audioFormat, int sessionId) throws IOException, InterruptedException, LineUnavailableException {
        Process process = new ProcessBuilder(
                "ffmpeg",
                "-hide_banner",
                "-loglevel", "error",
                "-i", audioStreamUrl.toString(),
                "-f", "s16be",
                "-acodec", "pcm_s16be",
                "-ac", Integer.toString(audioFormat.getChannels()),
                "-ar", Integer.toString((int) audioFormat.getSampleRate()),
                "-"
        ).start();

        Thread errReader = new Thread(() -> drainFfmpegErrors(process.getErrorStream()), "Sigma-Music-ffmpeg-err-" + sessionId);
        errReader.setDaemon(true);
        errReader.start();

        dataLine = AudioSystem.getSourceDataLine(audioFormat);
        dataLine.open();
        dataLine.start();

        InputStream pcmStream = process.getInputStream();
        byte[] pcmBuffer = new byte[4096];
        double playedSeconds = 0.0D;

        try {
            while (true) {
                while (!playing) {
                    if (shouldStopPlayback(sessionId)) {
                        process.destroyForcibly();
                        return;
                    }
                    Thread.sleep(300);
                    visualizer.clear();
                }

                if (shouldStopPlayback(sessionId)) {
                    process.destroyForcibly();
                    return;
                }

                int bytesRead = pcmStream.read(pcmBuffer);
                if (bytesRead <= 0) {
                    break;
                }

                dataLine.write(pcmBuffer, 0, bytesRead);

                byte[] fftBuffer = bytesRead == pcmBuffer.length ? pcmBuffer : Arrays.copyOf(pcmBuffer, bytesRead);
                float[] pcmSamples = JavaFFT.convertToPCMFloatArray(fftBuffer, audioFormat);
                int fftSize = largestPowerOfTwo(pcmSamples.length);
                if (fftSize >= 2) {
                    float[] fftSamples = pcmSamples.length == fftSize ? pcmSamples : Arrays.copyOf(pcmSamples, fftSize);
                    JavaFFT fftProcessor = new JavaFFT(fftSize);
                    float[][] fftResult = fftProcessor.transform(fftSamples);
                    visualizer.add(JavaFFT.calculateAmplitudes(fftResult[0], fftResult[1]));
                    if (visualizer.size() > 18) {
                        visualizer.remove(0);
                    }
                }

                adjustVolume(dataLine, volume);

                double frameRate = audioFormat.getFrameRate();
                int frameSize = audioFormat.getFrameSize();
                if (frameRate > 0 && frameSize > 0) {
                    playedSeconds += bytesRead / (frameRate * frameSize);
                    totalDuration = Math.round(playedSeconds);
                    playbackProgress = playedSeconds;
                }
            }
        } finally {
            try {
                pcmStream.close();
            } catch (IOException ignored) {
            }
            if (dataLine != null) {
                dataLine.close();
            }
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        }
    }

    private void drainFfmpegErrors(InputStream errStream) {
        byte[] buffer = new byte[1024];
        try {
            while (!Thread.currentThread().isInterrupted() && errStream.read(buffer) != -1) {
            }
        } catch (IOException ignored) {
        } finally {
            try {
                errStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private boolean isAacDecodeFailure(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String className = current.getClass().getName();
            if ("net.sourceforge.jaad.aac.AACException".equals(className)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static int largestPowerOfTwo(int value) {
        if (value <= 0) {
            return 0;
        }
        int result = 1;
        while ((result << 1) > 0 && (result << 1) <= value) {
            result <<= 1;
        }
        return result;
    }

    private AudioTrack selectAudioTrack(Movie movie, SongData data) throws IOException {
        AudioTrack bestTrack = null;
        int bestTrackSampleRate = -1;

        List<?> tracks = movie.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            Object track = tracks.get(i);
            boolean isAudioTrack = track instanceof AudioTrack;

            if (!isAudioTrack) {
                continue;
            }

            AudioTrack audioTrack = (AudioTrack) track;
            int sampleRate = audioTrack.getSampleRate();

            if (bestTrack == null || sampleRate > bestTrackSampleRate) {
                bestTrack = audioTrack;
                bestTrackSampleRate = sampleRate;
            }
        }

        if (bestTrack == null) {
            throw new IOException("No audio tracks found for song '" + safeSongTitle(data) + "' (id=" + safeSongId(data) + ")");
        }
        return bestTrack;
    }

    private static String safeSongId(SongData song) {
        return song == null || song.id == null ? "unknown" : song.id;
    }

    private static String safeSongTitle(SongData song) {
        return song == null || song.title == null ? "unknown" : song.title;
    }

    private void adjustVolume(SourceDataLine source, int volume) {
        try {
            FloatControl gain = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
            BooleanControl muteControl = (BooleanControl) source.getControl(javax.sound.sampled.BooleanControl.Type.MUTE);

            if (volume == 0) {
                muteControl.setValue(true);
            } else {
                muteControl.setValue(false);
                gain.setValue((float) (Math.log((double) volume / 100.0) / Math.log(10.0) * 20.0));
            }
        } catch (Exception e) {
            Client.LOGGER.warn("Failed to adjust volume to {}", volume, e);
        }
    }

    private void processThumbnail(SongData data, int generation) throws IOException {
        if (generation != thumbnailProcessGeneration.get()) {
            return;
        }
        processing = true;
        thumbnailFailed = false;
        thumbnailImage = null;
        scaledThumbnailImage = null;
        String thumbnailUrl = resolveThumbnailUrl(data);
        BufferedImage buffImage = ImageIO.read(new URL(thumbnailUrl));
        if (buffImage == null) {
            thumbnailFailed = true;
            processing = false;
            return;
        }

        BufferedImage source = ImageUtils.toCompatibleImageType(buffImage);
        BufferedImage blurred = ImageUtils.applyBlur(source, 15);
        if (blurred == null) {
            thumbnailFailed = true;
            processing = false;
            return;
        }

        int startY = (int) (blurred.getHeight() * 0.75F);
        int cropHeight = Math.max(1, (int) (blurred.getHeight() * 0.2F));
        BufferedImage blurredStrip = ImageUtils.copySubImageSafe(blurred, 0, startY, blurred.getWidth(), cropHeight);
        if (blurredStrip == null) {
            thumbnailFailed = true;
            processing = false;
            return;
        }
        BufferedImage bannerTexture = ImageUtils.toCompatibleImageType(
                ImageUtils.scaleImage(
                        blurredStrip,
                        (double) BLURRED_BANNER_WIDTH / (double) blurredStrip.getWidth(),
                        (double) BLURRED_BANNER_HEIGHT / (double) blurredStrip.getHeight()
                )
        );
        if (bannerTexture == null) {
            thumbnailFailed = true;
            processing = false;
            return;
        }

        BufferedImage squareThumbnail = ImageUtils.createSquareThumbnail(source, DEFAULT_THUMBNAIL_SIZE);
        if (squareThumbnail == null) {
            thumbnailFailed = true;
            processing = false;
            return;
        }

        thumbnailImage = bannerTexture;
        scaledThumbnailImage = squareThumbnail;
        thumbnailPreparedForSong = data;
        if (generation != thumbnailProcessGeneration.get()) {
            return;
        }
        thumbnailProcessingSongData = null;
    }

    private boolean isSameSong(SongData first, SongData second) {
        return first != null
                && second != null
                && first.id != null
                && first.id.equals(second.id);
    }

    private String resolveThumbnailUrl(SongData data) {
        if (data != null && data.url != null && !data.url.trim().isEmpty()) {
            return data.url;
        }
        return data != null ? data.getThumbnailUrl() : "";
    }

    private void renderBars() {
        float maxWidth = 114.0F;
        float width = (float) Math.ceil((float) client.getWindow().getWidth() / maxWidth);
        int barCount = Math.min((int) maxWidth, amplitudes.size());

        if (barCount == 0) {
            return;
        }

        for (int i = 0; i < barCount; i++) {
            float alpha = 1.0F - (float) (i + 1) / maxWidth;
            float heightRatio = (float) client.getWindow().getHeight() / 1080.0F;
            float height = ((float) (Math.sqrt(amplitudes.get(i)) / 12.0) - 5.0F) * heightRatio;
            RenderUtils.drawRoundedRect2(
                    (float) i * width,
                    (float) client.getWindow().getHeight() - height,
                    width,
                    height,
                    ColorHelper.applyAlpha(ClientColors.MID_GREY.getColor(), 0.2F * alpha)
            );
        }

        StencilUtils.beginStencilWrite();
        try {
            for (int i = 0; i < barCount; i++) {
                float heightRatio = (float) client.getWindow().getHeight() / 1080.0F;
                float height = ((float) (Math.sqrt(amplitudes.get(i)) / 12.0) - 5.0F) * heightRatio;
                RenderUtils.drawRoundedRect2((float) i * width, (float) client.getWindow().getHeight() - height, width, height, ClientColors.LIGHT_GREYISH_BLUE.getColor());
            }
            StencilUtils.beginStencilRead();
            if (notification != null && songThumbnail != null) {
                // Use linear filtering on the fullscreen blurred strip to avoid nearest-neighbor artifacts.
                RenderUtils.drawImage(
                        0.0F,
                        0.0F,
                        (float) client.getWindow().getWidth(),
                        (float) client.getWindow().getHeight(),
                        songThumbnail,
                        ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.4F),
                        false
                );
            }
        } finally {
            StencilUtils.endStencil();
        }
    }

    private void renderThumbnail() {
        double var9 = 0.0;
        float var16 = 4750;

        for (int i = 0; i < 3; i++) {
            var9 = Math.max(var9, Math.sqrt(this.amplitudes.get(i)) - 1000.0);
        }

        float scale = 1.0F + (float) Math.round((float) (var9 / (double) (var16 - 1000)) * 0.14F * 75.0F) / 75.0F;
        GL11.glPushMatrix();
        GL11.glTranslated(60.0, client.getWindow().getHeight() - 55, 0.0);
        GL11.glScalef(scale, scale, 0.0F);
        GL11.glTranslated(-60.0, -(client.getWindow().getHeight() - 55), 0.0);
        RenderUtils.drawImage(10.0F, (float) (client.getWindow().getHeight() - 110), 100.0F, 100.0F, notification);
        RenderUtils.drawRoundedRect(10.0F, (float) (client.getWindow().getHeight() - 110), 100.0F, 100.0F, 14.0F, 0.3F);
        GL11.glPopMatrix();
    }

    private void renderSongTitle() {
        if (currentPlayingSongData == null) return;
        String[] titleParts = currentPlayingSongData.title.split(" - ");
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
}
