package io.github.sst.remake.gui.screen.musicplayer;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.gui.framework.widget.Image;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.gui.screen.clickgui.ClickGuiScreen;
import io.github.sst.remake.manager.impl.MusicManager;
import io.github.sst.remake.util.client.yt.PlaylistData;
import io.github.sst.remake.util.client.yt.SongData;
import io.github.sst.remake.util.http.YoutubeUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.newdawn.slick.opengl.texture.Texture;

import java.io.IOException;
import java.util.*;

public class MusicPlayer extends Widget {
    private final AnimationUtils edgeDockAnimation = new AnimationUtils(80, 150, AnimationUtils.Direction.FORWARDS);
    public boolean isAnimatingBack = false;

    private final static int PLAYER_WIDTH = 250;
    private final static int HEADER_HEIGHT = 64;
    private final static int FOOTER_HEIGHT = 94;

    private String currentPlaylistName = "Music Player";
    private final ScrollablePanel playlistTabsPanel;
    private ScrollablePanel activePlaylistPanel;
    private final MusicManager musicManager = Client.INSTANCE.musicManager;
    public static Map<String, PlaylistData> videoMap = new LinkedHashMap<>();

    private final Button playButton;
    private final Button pauseButton;
    private final Button nextTrackButton;
    private final Button previousTrackButton;

    private final VolumeSlider volumeSlider;
    private int lastPlaylistScrollOffset;
    private Texture headerBlurTexture;
    private final GuiComponent edgeReShowHandle;
    public SearchBox searchBox;
    public ProgressBar songProgressBar;
    public static long lastFrameTime = 0L;

    public float targetX = 0.0F;
    public float targetY = 0.0F;

    public ClickGuiScreen parent;

    public MusicPlayer(ClickGuiScreen parent, String name) {
        super(parent, name, 875, 55, 800, 600, false);
        this.parent = parent;

        lastFrameTime = System.nanoTime();
        this.setWidth(800);
        this.setHeight(600);
        this.setX(Math.abs(this.getX()));
        this.setY(Math.abs(this.getY()));

        this.addToList(this.playlistTabsPanel = new ScrollablePanel(
                this, "musictabs", 0, HEADER_HEIGHT + 14, PLAYER_WIDTH, this.getHeight() - 64 - FOOTER_HEIGHT
        ));

        GuiComponent controlBar;
        this.addToList(controlBar = new ScrollablePanel(
                this, "musiccontrols", PLAYER_WIDTH, this.getHeight() - FOOTER_HEIGHT, this.getWidth() - PLAYER_WIDTH, FOOTER_HEIGHT
        ));

        this.addToList(this.edgeReShowHandle = new GuiComponent(this, "reShowView", 0, 0, 1, this.getHeight()));

        SpectrumButton spectrumButton;
        this.addToList(spectrumButton = new SpectrumButton(this, "spectrumButton", 15, this.height - 140, 40, 40, this.musicManager.spectrum));
        spectrumButton.setReAddChildren(true);
        spectrumButton.onClick((clicked, mouseButton) -> {
            this.musicManager.spectrum = !this.musicManager.spectrum;
            ((SpectrumButton) clicked).setSpectrumEnabled(this.musicManager.spectrum);
        });

        this.playlistTabsPanel.setListening(false);
        spectrumButton.setListening(false);
        controlBar.setListening(false);
        this.edgeReShowHandle.setListening(false);

        ColorHelper playlistButtonTheme = new ColorHelper(1250067, -15329770)
                .setTextColor(ClientColors.LIGHT_GREYISH_BLUE.getColor())
                .setHeightAlignment(FontAlignment.CENTER);

        List<Thread> refreshThreads = new ArrayList<>();
        for (PlaylistData playlist : musicManager.playlists) {
            ScrollablePanel playlistPanel = this.createPlaylistUI(playlist, playlistButtonTheme);
            refreshThreads.add(new Thread(() -> {
                if (!videoMap.containsKey(playlist.id) && !playlist.updated) {
                    playlist.refresh();
                    playlist.updated = true;
                    videoMap.put(playlist.id, playlist);
                }
                this.addRunnable(() -> populatePlaylistSongs(playlistPanel, playlist));
            }));
            refreshThreads.get(refreshThreads.size() - 1).start();
        }

        int centerX = (this.getWidth() - PLAYER_WIDTH - 38) / 2;

        controlBar.addToList(this.playButton = new Image(
                controlBar, "play", centerX, 27, 38, 38, Resources.PLAY,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
        ));

        controlBar.addToList(this.pauseButton = new Image(
                controlBar, "pause", centerX, 27, 38, 38, Resources.PAUSE,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
        ));

        controlBar.addToList(this.nextTrackButton = new Image(
                controlBar, "forwards", centerX + 114, 23, 46, 46, Resources.FORWARDS,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
        ));

        controlBar.addToList(this.previousTrackButton = new Image(
                controlBar, "backwards", centerX - 114, 23, 46, 46, Resources.BACKWARDS,
                new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
        ));

        controlBar.addToList(this.volumeSlider = new VolumeSlider(
                controlBar, "volume", this.getWidth() - PLAYER_WIDTH - 19, 14, 4, 40
        ));

        ChangingButton repeatModeButton;
        controlBar.addToList(repeatModeButton = new ChangingButton(
                controlBar, "repeat", 14, 34, 27, 20, this.musicManager.repeat
        ));
        repeatModeButton.onPress(interactiveWidget -> this.musicManager.repeat = repeatModeButton.repeatModeIndex);

        this.addToList(this.songProgressBar = new ProgressBar(
                this, "progress", PLAYER_WIDTH, this.getHeight() - 5, this.getWidth() - PLAYER_WIDTH, 5
        ));
        this.songProgressBar.setReAddChildren(true);
        this.songProgressBar.setListening(false);

        this.edgeReShowHandle.setReAddChildren(true);
        this.edgeReShowHandle.addMouseButtonCallback((screen, mouseButton) -> {
            isAnimatingBack = true;
            this.targetX = (float) this.getX();
            this.targetY = (float) this.getY();
        });

        this.pauseButton.setSelfVisible(false);
        this.playButton.setSelfVisible(false);

        this.playButton.onClick((clicked, mouseButton) -> this.musicManager.setPlaying(true));
        this.pauseButton.onClick((clicked, mouseButton) -> this.musicManager.setPlaying(false));
        this.nextTrackButton.onClick((clicked, mouseButton) -> this.musicManager.playNext());
        this.previousTrackButton.onClick((clicked, mouseButton) -> this.musicManager.playPrevious());

        this.volumeSlider.addVolumeChangeListener(slider -> this.musicManager.volume = (int) ((1.0F - this.volumeSlider.getVolume()) * 100.0F));
        this.volumeSlider.setVolume(1.0F - (float) this.musicManager.volume / 100.0F);

        this.addToList(this.searchBox = new SearchBox(
                this, "search", PLAYER_WIDTH, 0, this.getWidth() - PLAYER_WIDTH, this.getHeight() - FOOTER_HEIGHT, "Search..."
        ));
        this.searchBox.setSelfVisible(true);
        this.searchBox.setListening(false);
    }

    private void showPlaylistPanel(ScrollablePanel playlistPanel) {
        if (this.activePlaylistPanel != null) {
            this.activePlaylistPanel.setSelfVisible(false);
        }

        playlistPanel.setSelfVisible(true);
        this.currentPlaylistName = playlistPanel.getText();
        this.activePlaylistPanel = playlistPanel;
        this.searchBox.setSelfVisible(false);
        this.activePlaylistPanel.scrollBarWidth = 65;
    }

    @Override
    public void loadPersistedConfig(JsonObject config) {
        super.loadPersistedConfig(config);

        this.setDragging(false);
        this.isAnimatingBack = false;

        if (this.parent != null) {
            int minY = 0;
            int maxY = Math.max(0, this.parent.getHeight() - this.getHeight());
            this.setY(Math.max(minY, Math.min(this.getY(), maxY)));

            if (this.getX() + this.getWidth() > this.parent.getWidth()) {
                int hiddenX = this.parent.getWidth() - 40;
                this.setX(hiddenX);
                this.setDraggable(false);
            } else {
                int minX = 0;
                int maxX = Math.max(0, this.parent.getWidth() - this.getWidth());
                this.setX(Math.max(minX, Math.min(this.getX(), maxX)));
                this.setDraggable(true);
            }
        }

        this.targetX = (float) this.getX();
        this.targetY = (float) this.getY();
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        long deltaNanos = System.nanoTime() - lastFrameTime;
        float timeScale = Math.min(10.0F, Math.max(0.0F, (float) deltaNanos / 1.810361E7F));
        lastFrameTime = System.nanoTime();

        super.updatePanelDimensions(mouseX, mouseY);

        if (this.parent == null) {
            return;
        }

        if (!this.isDragging()) {
            if ((this.isMouseDownOverComponent || isAnimatingBack) && !this.isDraggable()) {
                animateDockBackIntoView(timeScale);
            } else if (this.getX() + this.getWidth() > this.parent.getWidth() || this.getX() < 0 || this.getY() < 0) {
                animateSnapToRightEdge(timeScale);
            }
            return;
        }

        handleOverdragBeyondRightEdge(mouseX);
    }

    @Override
    public void draw(float partialTicks) {
        super.applyScaleTransforms();
        super.applyTranslationTransforms();

        this.edgeReShowHandle.setWidth(this.getX() + this.getWidth() <= this.parent.getWidth() ? 0 : 41);

        this.edgeDockAnimation.changeDirection(
                this.getX() + this.getWidth() > this.parent.getWidth() && !isAnimatingBack
                        ? AnimationUtils.Direction.BACKWARDS
                        : AnimationUtils.Direction.FORWARDS
        );

        partialTicks *= 0.5F + (1.0F - this.edgeDockAnimation.calcPercent()) * 0.5F;

        if (this.musicManager.isPlaying()) {
            this.playButton.setSelfVisible(false);
            this.pauseButton.setSelfVisible(true);
        } else {
            this.playButton.setSelfVisible(true);
            this.pauseButton.setSelfVisible(false);
        }

        RenderUtils.drawRoundedRect(
                (float) (this.getX() + PLAYER_WIDTH),
                (float) this.getY(),
                (float) (this.getX() + this.getWidth()),
                (float) (this.getY() + this.getHeight() - FOOTER_HEIGHT),
                ColorHelper.applyAlpha(-14277082, partialTicks * 0.8F)
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) (this.getX() + PLAYER_WIDTH),
                (float) (this.getY() + this.getHeight() - FOOTER_HEIGHT),
                ColorHelper.applyAlpha(-16777216, partialTicks * 0.95F)
        );

        this.drawThumbnail(partialTicks);
        this.drawSongTitle(partialTicks);
        this.drawDuration(partialTicks);

        float brandOffsetX = 55;
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_40,
                brandOffsetX + this.getX(),
                (float) (this.getY() + 20),
                "Jello",
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                brandOffsetX + this.getX() + 80,
                (float) (this.getY() + 40),
                "music",
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );

        RenderUtils.drawRoundedRect((float) this.getX(), (float) this.getY(), (float) this.getWidth(), (float) this.getHeight(), 14.0F, partialTicks);

        super.draw(partialTicks);

        if (this.activePlaylistPanel != null) {
            this.drawHeaderAndBackground(partialTicks);
        }
    }

    private void drawDuration(float partialTicks) {
        int totalSeconds = this.musicManager.getTotalDuration();
        int currentSeconds = this.musicManager.getDuration();

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + PLAYER_WIDTH + 14),
                (float) (this.getY() + this.getHeight() - 10) - 22.0F * partialTicks,
                YoutubeUtils.formatSecondsAsTimestamp(totalSeconds),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * partialTicks)
        );

        String currentTimeText = YoutubeUtils.formatSecondsAsTimestamp(currentSeconds);
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + this.getWidth() - 14 - FontUtils.HELVETICA_LIGHT_14.getWidth(currentTimeText)),
                (float) (this.getY() + this.getHeight() - 10) - 22.0F * partialTicks,
                currentTimeText,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * partialTicks)
        );
    }

    private void drawThumbnail(float partialTicks) {
        Texture notificationTexture = this.musicManager.getNotification();
        Texture songThumbnailTexture = this.musicManager.getSongThumbnail();

        if (notificationTexture != null && songThumbnailTexture != null) {
            drawPlayerArtwork(partialTicks, songThumbnailTexture, notificationTexture);
            return;
        }

        RenderUtils.drawImage(
                (float) this.getX(),
                (float) (this.getY() + this.getHeight() - FOOTER_HEIGHT),
                (float) this.getWidth(),
                (float) FOOTER_HEIGHT,
                Resources.BACKGROUND,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * partialTicks)
        );

        drawArtworkOverlays(partialTicks);

        RenderUtils.drawImage(
                (float) (this.getX() + (PLAYER_WIDTH - 114) / 2),
                (float) (this.getY() + this.getHeight() - 170),
                114.0F,
                114.0F,
                Resources.ARTWORK,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtils.drawRoundedRect(
                (float) (this.getX() + (PLAYER_WIDTH - 114) / 2),
                (float) (this.getY() + this.getHeight() - 170),
                114.0F,
                114.0F,
                14.0F,
                partialTicks
        );
    }

    private void drawPlayerArtwork(float partialTicks, Texture backgroundArtwork, Texture notificationArtwork) {
        RenderUtils.drawImage(
                (float) this.getX(),
                (float) (this.getY() + this.getHeight() - FOOTER_HEIGHT),
                (float) this.getWidth(),
                (float) FOOTER_HEIGHT,
                backgroundArtwork,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * partialTicks)
        );

        drawArtworkOverlays(partialTicks);

        RenderUtils.drawImage(
                (float) (this.getX() + (PLAYER_WIDTH - 114) / 2),
                (float) (this.getY() + this.getHeight() - 170),
                114.0F,
                114.0F,
                notificationArtwork,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtils.drawRoundedRect(
                (float) (this.getX() + (PLAYER_WIDTH - 114) / 2),
                (float) (this.getY() + this.getHeight() - 170),
                114.0F,
                114.0F,
                14.0F,
                partialTicks
        );
    }

    private void drawArtworkOverlays(float partialTicks) {
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) (this.getY() + this.getHeight() - FOOTER_HEIGHT),
                (float) (this.getX() + this.getWidth()),
                (float) (this.getY() + this.getHeight() - 5),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * partialTicks)
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) (this.getY() + this.getHeight() - 5),
                (float) (this.getX() + PLAYER_WIDTH),
                (float) (this.getY() + this.getHeight()),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * partialTicks)
        );
    }

    private void drawSongTitle(float partialTicks) {
        if (this.musicManager.getSongProcessed() == null) {
            this.drawScrollingTitle(partialTicks, "Jello Music", PLAYER_WIDTH - 30 * 2, 12, 0);
            return;
        }

        if (this.musicManager.getSongProcessed().title == null) {
            return;
        }

        String[] parts = this.musicManager.getSongProcessed().title.split(" - ");
        int padding = 30;

        if (parts.length <= 1) {
            String title = (!parts[0].isEmpty()) ? parts[0] : "Jello Music";
            this.drawScrollingTitle(partialTicks, title, PLAYER_WIDTH - padding * 2, 12, 0);
            return;
        }

        this.drawScrollingTitle(partialTicks, parts[1], PLAYER_WIDTH - padding * 2, 0, 0);
        this.drawScrollingTitle(partialTicks, parts[0], PLAYER_WIDTH - padding * 2, 20, -1000);
    }

    private void drawScrollingTitle(float alpha, String text, int maxWidth, int yOffset, int timeOffsetMs) {
        long nowMs = new Date().getTime();
        float t = (float) ((nowMs + (long) timeOffsetMs) % 8500) / 8500.0F;

        if (t >= 0.4F) {
            t = (float) ((double) (t - 0.4F) * (5.0 / 3.0));
        } else {
            t = 0.0F;
        }

        float scroll = QuadraticEasing.easeInOutQuad(t, 0.0F, 1.0F, 1.0F);

        int textWidth = FontUtils.HELVETICA_LIGHT_14.getWidth(text);
        int clipWidth = Math.min(maxWidth, textWidth);
        int clipHeight = FontUtils.HELVETICA_LIGHT_14.getHeight();

        int clipX = this.getX() + (PLAYER_WIDTH - clipWidth) / 2;
        int clipY = this.getY() + this.getHeight() - 50 + yOffset;

        if (textWidth <= maxWidth) {
            scroll = 0.0F;
        }

        ScissorUtils.startScissor(clipX, clipY, clipX + clipWidth, clipY + clipHeight, true);

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) clipX - (float) textWidth * scroll - 50.0F * scroll,
                (float) clipY,
                text,
                ColorHelper.applyAlpha(
                        ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                        alpha * alpha * Math.min(1.0F, Math.max(0.0F, 1.0F - scroll * 0.75F))
                )
        );

        if (scroll > 0.0F) {
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_14,
                    (float) clipX - (float) textWidth * scroll + (float) textWidth,
                    (float) clipY,
                    text,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha * alpha)
            );
        }

        ScissorUtils.restoreScissor();
    }

    private void drawHeaderAndBackground(float partialTicks) {
        this.activePlaylistPanel.setReAddChildren(false);

        if (this.lastPlaylistScrollOffset != this.activePlaylistPanel.getScrollOffset()) {
            try {
                if (this.headerBlurTexture != null) {
                    this.headerBlurTexture.release();
                }

                this.headerBlurTexture = ImageUtils.createTexture(
                        "blur",
                        ImageUtils.captureAndProcessRegion(
                                this.getX() + PLAYER_WIDTH,
                                this.getY(),
                                this.getWidth() - PLAYER_WIDTH,
                                HEADER_HEIGHT,
                                10,
                                10
                        )
                );
            } catch (IOException e) {
                Client.LOGGER.error("Failed to blur image", e);
            }
        }

        float headerAlpha = this.lastPlaylistScrollOffset < 50 ? (float) this.lastPlaylistScrollOffset / 50.0F : 1.0F;

        if (this.headerBlurTexture != null) {
            RenderUtils.drawTexture(
                    (float) PLAYER_WIDTH,
                    0.0F,
                    (float) (this.getWidth() - PLAYER_WIDTH),
                    (float) HEADER_HEIGHT,
                    this.headerBlurTexture,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), headerAlpha * partialTicks)
            );
        }

        RenderUtils.drawRoundedRect(
                (float) PLAYER_WIDTH,
                0.0F,
                (float) this.getWidth(),
                (float) HEADER_HEIGHT,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), headerAlpha * partialTicks * 0.2F)
        );

        float titleXLight = (float) ((this.getWidth() - FontUtils.HELVETICA_LIGHT_25.getWidth(this.currentPlaylistName) + PLAYER_WIDTH) / 2);
        float titleXMedium = (float) ((this.getWidth() - FontUtils.HELVETICA_MEDIUM_25.getWidth(this.currentPlaylistName) + PLAYER_WIDTH) / 2);
        float titleY = 16.0F + (1.0F - headerAlpha) * 14.0F;

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25,
                titleXLight,
                titleY,
                this.currentPlaylistName,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), headerAlpha)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_25,
                titleXMedium,
                titleY,
                this.currentPlaylistName,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 1.0F - headerAlpha)
        );

        RenderUtils.drawImage(
                (float) PLAYER_WIDTH,
                (float) HEADER_HEIGHT,
                (float) (this.getWidth() - PLAYER_WIDTH),
                20.0F,
                Resources.SHADOW_BOTTOM,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), headerAlpha * partialTicks * 0.5F)
        );

        this.lastPlaylistScrollOffset = this.activePlaylistPanel.getScrollOffset();
    }

    private ScrollablePanel createPlaylistUI(PlaylistData playlist, ColorHelper colorHelper) {
        if (!this.playlistTabsPanel.hasChildWithName(playlist.id)) {
            int index = this.playlistTabsPanel.getContent().getChildren().size();

            Button playlistTabButton;
            this.playlistTabsPanel.addToList(playlistTabButton = new Button(
                    this.playlistTabsPanel,
                    playlist.id,
                    0,
                    index * 40,
                    PLAYER_WIDTH,
                    40,
                    colorHelper,
                    playlist.name,
                    FontUtils.HELVETICA_LIGHT_14
            ));

            ScrollablePanel playlistPanel;
            this.addToList(playlistPanel = new ScrollablePanel(
                    this,
                    playlist.id,
                    PLAYER_WIDTH,
                    0,
                    this.getWidth() - PLAYER_WIDTH,
                    this.getHeight() - FOOTER_HEIGHT,
                    ColorHelper.DEFAULT_COLOR,
                    playlist.name
            ));

            playlistPanel.setAllowUpdatesWhenHidden(true);
            playlistPanel.setSelfVisible(false);
            playlistPanel.setListening(false);

            playlistTabButton.onClick((clicked, mouseButton) -> this.showPlaylistPanel(playlistPanel));
            return playlistPanel;
        }

        return (ScrollablePanel) this.getChildByName(playlist.id);
    }

    private void populatePlaylistSongs(ScrollablePanel playlistPanel, PlaylistData playlist) {
        if (playlistPanel == null) {
            return;
        }

        for (int i = 0; i < playlist.songs.size(); i++) {
            SongData song = playlist.songs.get(i);

            int baseX = 65;
            int padding = 10;

            if (playlistPanel.hasChildWithName(song.id)) {
                continue;
            }

            int row = i % 3;
            int rowOffsetY = padding + row * 183 - (row <= 0 ? 0 : padding) - (row <= 1 ? 0 : padding);
            int colOffsetX = baseX + padding + (i - row) / 3 * 210;

            ThumbnailButton thumbnailButton = new ThumbnailButton(
                    playlistPanel,
                    rowOffsetY,
                    colOffsetX,
                    183,
                    220,
                    song
            );

            playlistPanel.addToList(thumbnailButton);
            thumbnailButton.onClick((clicked, mouseButton) -> {
                if (this.parent.checkMusicPlayerDependencies()) {
                    musicManager.play(playlist, song);
                }
            });
        }
    }

    private void animateDockBackIntoView(float timeScale) {
        isAnimatingBack = true;

        int dockedX = this.parent.getWidth() - 20 - this.getWidth();
        this.targetX = Math.max(this.targetX - (this.targetX - (float) dockedX) * 0.25F * timeScale, (float) dockedX);

        if (this.targetX - (float) dockedX < 0.0F) {
            this.targetX = (float) dockedX;
        } else if (this.targetX - (float) dockedX - (float) this.getWidth() > 0.0F) {
            this.targetX = (float) dockedX;
        }

        this.setX((int) this.targetX);
        this.setY((int) this.targetY);

        if (Math.abs(this.targetX - (float) dockedX) < 2.0F) {
            this.setDraggable(true);
            isAnimatingBack = false;
        }
    }

    private void animateSnapToRightEdge(float timeScale) {
        if (this.targetX == 0.0F || this.targetY == 0.0F) {
            this.targetX = (float) this.getX();
            this.targetY = (float) this.getY();
        }

        int hiddenX = this.parent.getWidth() - 40;
        this.targetX = Math.min(this.targetX - (this.targetX - (float) hiddenX) * 0.25F * timeScale, (float) hiddenX);

        if (this.targetX - (float) hiddenX > 0.0F) {
            this.targetX = (float) hiddenX;
        } else if (this.targetX - (float) hiddenX + (float) this.getWidth() < 0.0F) {
            this.targetX = (float) hiddenX;
        }

        if (Math.abs(this.targetX - (float) hiddenX) < 2.0F) {
            this.targetX = (float) this.getX();
            this.targetY = (float) this.getY();
        }

        this.setX((int) this.targetX);
        this.setY((int) this.targetY);
        this.setDraggable(false);
        this.setDragging(false);
    }

    private void handleOverdragBeyondRightEdge(int mouseX) {
        int dragX = mouseX - this.dragOffsetX - this.parent.getAbsoluteX();
        int gracePixels = 200;

        if (dragX + this.getWidth() > this.parent.getWidth() + gracePixels && mouseX - this.dragStartMouseX > 70) {
            int overflow = dragX - this.getX() - gracePixels;
            this.setX((int) ((float) this.getX() + (float) overflow * 0.5F));
            this.targetX = (float) this.getX();
            this.targetY = (float) this.getY();
        }
    }
}
