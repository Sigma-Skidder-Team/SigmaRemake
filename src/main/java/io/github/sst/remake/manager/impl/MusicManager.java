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
import io.github.sst.remake.util.http.YoutubeUtils;
import io.github.sst.remake.util.io.audio.stream.MusicStream;
import io.github.sst.remake.util.math.JavaFFT;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.StencilUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
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
import org.newdawn.slick.util.image.BufferedImageUtil;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MusicManager extends Manager implements IMinecraft {

    private static final int DEFAULT_THUMBNAIL_X = 70;
    private static final int DEFAULT_THUMBNAIL_Y = 0;
    private static final int DEFAULT_THUMBNAIL_SIZE = 180;

    public final List<PlaylistData> playlists = new ArrayList<>();

    private final List<double[]> visualizer = new ArrayList<>();
    private final List<Double> amplitudes = new ArrayList<>();

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
    private SongData currentPlayingSongData;
    private SongData thumbnailProcessingSongData;

    public SongData getSongProcessed() {
        return currentPlayingSongData;
    }

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

        /*
        playlists.add(new PlaylistData("Country", "RDCLAK5uy_lRZyKy_XqMaPeU5v-pvA2PLUn8ZMVMGoE"));
        playlists.add(new PlaylistData("Mellow", "RDCLAK5uy_kzhe4thDu2Gh_HJX-PhiswAlcxHsqjvfo"));
        playlists.add(new PlaylistData("Hip-Hop", "RDCLAK5uy_mFEnPWt71C547zB84TE8T42ORbAQiGe1M"));
        playlists.add(new PlaylistData("EDM", "RDCLAK5uy_md-KWXDxKwI1W3J1PjCERreBRd8hZLCLw"));
        playlists.add(new PlaylistData("Freestyle", "RDCLAK5uy_mGMqJUDr4XGV_mSXMwyRTHIJFtiaFSuj4"));
        playlists.add(new PlaylistData("Jazz", "RDCLAK5uy_nyRN5z0Kh9XP7r7qhm3ANS_3pCF_qco-o"));
        playlists.add(new PlaylistData("Blues", "RDCLAK5uy_k6B0CcfHO04oWPAyUVlO96Vvmg_pB62JM"));
         */

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
        notification.release();
        songThumbnail.release();
        notification = null;
        songThumbnail = null;

        thumbnailImage = null;
        scaledThumbnailImage = null;

        playing = false;
        playlist = null;
        currentPlayingSongData = null;
        thumbnailProcessingSongData = null;
        visualizer.clear();
        amplitudes.clear();
        dataLine.close();
        playlists.clear();
    }

    @Subscribe
    public void onRender(RenderHudEvent ignoredEvent) {
        if (!playing || !spectrum || notification == null) return;
        if (visualizer.isEmpty() || amplitudes.isEmpty()) return;

        renderBars();
        renderThumbnail();
        renderSongTitle();
    }

    @Subscribe
    public void onRender2D(RenderClient2DEvent ignoredEvent) {
        if (!playing || visualizer.isEmpty()) return;

        double[] visualize = visualizer.get(0);

        if (amplitudes.isEmpty()) {
            for (double v : visualize) {
                if (amplitudes.size() < 1024) {
                    amplitudes.add(v);
                }
            }
        }

        float fps = 60.0F / (float) MinecraftClient.currentFps;

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
            if (thumbnailImage != null && scaledThumbnailImage != null && currentPlayingSongData != null && !client.isPaused()) {
                if (songThumbnail != null) {
                    songThumbnail.release();
                }

                if (notification != null) {
                    notification.release();
                }

                thumbnailImage = toCompatibleImageType(thumbnailImage);
                scaledThumbnailImage = toCompatibleImageType(scaledThumbnailImage);

                songThumbnail = BufferedImageUtil.getTexture("picture", thumbnailImage);
                notification = BufferedImageUtil.getTexture("picture", scaledThumbnailImage);
                Client.INSTANCE.notificationManager
                        .send(new Notification("Now Playing", currentPlayingSongData.title, 7000, notification));
                thumbnailProcessingSongData = null;
                processing = false;
            }
        }

        if (thumbnailProcessingSongData != null) {
            SongData songData = thumbnailProcessingSongData;
            thumbnailProcessingSongData = null;
            visualizer.clear();
            new Thread(() -> {
                try {
                    processThumbnail(songData);
                } catch (IOException ignored) {
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
        visualizer.clear();
        if (playlist != null) {
            while (audioThread != null && audioThread.isAlive()) {
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
            try {
                currentlyPlayingVideoIndex = i;
                currentPlayingSongData = playlist.songs.get(i);
                visualizer.clear();

                while (!this.playing) {
                    Thread.sleep(300);

                    visualizer.clear();
                    if (Thread.interrupted()) {
                        if (dataLine != null)
                            dataLine.close();

                        return;
                    }
                }

                playTrack(currentPlayingSongData);

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
            } catch (Exception e) {
                Client.LOGGER.error("Failed to play track", e);
            }
        }
    }

    private void playTrack(SongData data) throws IOException, LineUnavailableException, InterruptedException {
        thumbnailProcessingSongData = data;

        URL videoStreamUrl = YoutubeUtils.buildYouTubeWatchUrl(data.id);
        URL audioStreamUrl = YtDlpUtils.resolveStream(videoStreamUrl.toString());

        if (audioStreamUrl == null) {
            Thread.sleep(1000);
            return;
        }

        MusicStream mS = new MusicStream(getConnection(audioStreamUrl).getInputStream());

        MP4Container container = new MP4Container(mS);

        Movie movie = container.getMovie();
        AudioTrack track = (AudioTrack) movie.getTracks().get(1);
        AudioFormat audioFormat = new AudioFormat((float) track.getSampleRate(), 16, track.getChannelCount(), true, true);

        dataLine = AudioSystem.getSourceDataLine(audioFormat);
        dataLine.open();
        dataLine.start();
        duration = (long) movie.getDuration();

        if (duration > 1300) {
            mS.close();
            Client.INSTANCE.notificationManager.send(new Notification("Now Playing", "Music is too long."));
        }

        streamAudioData(track, mS, audioFormat);
    }

    private void streamAudioData(AudioTrack track, MusicStream mS, AudioFormat audioFormat) throws InterruptedException, IOException {
        Decoder aacDecoder = new Decoder(track.getDecoderSpecificInfo());
        SampleBuffer sampleBuffer = new SampleBuffer();

        while (track.hasMoreFrames()) {
            while (!playing) {
                Thread.sleep(300);
                visualizer.clear();
                if (Thread.interrupted()) {
                    dataLine.close();
                    return;
                }
            }

            Frame frame = track.readNextFrame();
            aacDecoder.decodeFrame(frame.getData(), sampleBuffer);

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

    private URLConnection getConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(14000);
        connection.setReadTimeout(14000);
        connection.setUseCaches(true);

        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        return connection;
    }

    private BufferedImage toCompatibleImageType(BufferedImage image) {
        if (image == null || image.getType() == BufferedImage.TYPE_INT_ARGB) {
            return image;
        }
        BufferedImage compatibleImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        compatibleImage.getGraphics().drawImage(image, 0, 0, null);
        compatibleImage.getGraphics().dispose();
        return compatibleImage;
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
        thumbnailProcessingSongData = null;
    }

    private void renderBars() {
        float maxWidth = 114.0F;
        float width = (float) Math.ceil((float) client.getWindow().getWidth() / maxWidth);

        for (int i = 0; (float) i < maxWidth; i++) {
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
        for (int i = 0; (float) i < maxWidth; i++) {
            float heightRatio = (float) client.getWindow().getHeight() / 1080.0F;
            float height = ((float) (Math.sqrt(amplitudes.get(i)) / 12.0) - 5.0F) * heightRatio;
            RenderUtils.drawRoundedRect2((float) i * width, (float) client.getWindow().getHeight() - height, width, height, ClientColors.LIGHT_GREYISH_BLUE.getColor());
        }
        StencilUtils.beginStencilRead();
        if (notification != null && songThumbnail != null) {
            RenderUtils.drawImage(0.0F, 0.0F, (float) client.getWindow().getWidth(), (float) client.getWindow().getHeight(), songThumbnail, 0.4F);
        }
        StencilUtils.endStencil();
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