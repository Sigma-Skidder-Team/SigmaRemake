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
    private final AnimationUtils visibilityAnimation = new AnimationUtils(80, 150, AnimationUtils.Direction.FORWARDS);
    public boolean isAnimatingBack = false;

    private final static int PLAYER_WIDTH = 250;
    private final static int HEADER_HEIGHT = 64;
    private final static int FOOTER_HEIGHT = 94;

    private String currentPlaylistName = "Music Player";
    private final ScrollablePanel musicTabs;
    private ScrollablePanel activePlaylistPanel;
    private final MusicManager musicManager = Client.INSTANCE.musicManager;
    public static Map<String, PlaylistData> videoMap = new LinkedHashMap<>();
    private final Button play;
    private final Button pause;
    private final Button forwards;
    private final Button backwards;
    private final VolumeSlider volumeSlider;
    private int lastScrollOffset;
    private Texture texture;
    private final GuiComponent reShowView;
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
        this.addToList(this.musicTabs = new ScrollablePanel(this, "musictabs", 0, HEADER_HEIGHT + 14, PLAYER_WIDTH, this.getHeight() - 64 - FOOTER_HEIGHT));
        GuiComponent musicControls;
        this.addToList(
                musicControls = new ScrollablePanel(
                        this, "musiccontrols", PLAYER_WIDTH, this.getHeight() - FOOTER_HEIGHT, this.getWidth() - PLAYER_WIDTH, FOOTER_HEIGHT
                )
        );
        this.addToList(this.reShowView = new GuiComponent(this, "reShowView", 0, 0, 1, this.getHeight()));
        SpectrumButton spectrumBtn;
        this.addToList(spectrumBtn = new SpectrumButton(this, "spectrumButton", 15, this.height - 140, 40, 40, this.musicManager.spectrum));
        spectrumBtn.setReAddChildren(true);
        spectrumBtn.onClick((parent2, mouseButton) -> {
            this.musicManager.spectrum = !this.musicManager.spectrum;
            ((SpectrumButton) parent2).setSpectrumEnabled(this.musicManager.spectrum);
        });
        this.musicTabs.setListening(false);
        spectrumBtn.setListening(false);
        musicControls.setListening(false);
        this.reShowView.setListening(false);
        ColorHelper color = new ColorHelper(1250067, -15329770).setTextColor(ClientColors.LIGHT_GREYISH_BLUE.getColor()).setHeightAlignment(FontAlignment.CENTER);
        List<Thread> threads = new ArrayList<>();

        for (PlaylistData data : musicManager.playlists) {
            ScrollablePanel queue = this.createPlaylistUI(data, color);
            threads.add(new Thread(() -> {
                if (!videoMap.containsKey(data.id) && !data.updated) {
                    data.refresh();
                    data.updated = true;

                    videoMap.put(data.id, data);
                }

                this.addRunnable(() -> populatePlaylistSongs(queue, data));
            }));
            threads.get(threads.size() - 1).start();
        }

        int x = (this.getWidth() - PLAYER_WIDTH - 38) / 2;
        musicControls
                .addToList(
                        this.play = new Image(
                                musicControls, "play", x, 27, 38, 38, Resources.PLAY, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        musicControls
                .addToList(
                        this.pause = new Image(
                                musicControls, "pause", x, 27, 38, 38, Resources.PAUSE, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        musicControls
                .addToList(
                        this.forwards = new Image(
                                musicControls, "forwards", x + 114, 23, 46, 46, Resources.FORWARDS, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        musicControls
                .addToList(
                        this.backwards = new Image(
                                musicControls, "backwards", x - 114, 23, 46, 46, Resources.BACKWARDS, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        musicControls.addToList(this.volumeSlider = new VolumeSlider(musicControls, "volume", this.getWidth() - PLAYER_WIDTH - 19, 14, 4, 40));
        ChangingButton repeat;
        musicControls.addToList(repeat = new ChangingButton(musicControls, "repeat", 14, 34, 27, 20, this.musicManager.repeat));
        repeat.onPress(interactiveWidget -> this.musicManager.repeat = repeat.repeatModeIndex);
        this.addToList(this.songProgressBar = new ProgressBar(this, "progress", PLAYER_WIDTH, this.getHeight() - 5, this.getWidth() - PLAYER_WIDTH, 5));
        this.songProgressBar.setReAddChildren(true);
        this.songProgressBar.setListening(false);
        this.reShowView.setReAddChildren(true);
        this.reShowView.addMouseButtonCallback((screen, mouseButton) -> {
            isAnimatingBack = true;
            this.targetX = (float) this.getX();
            this.targetY = (float) this.getY();
        });
        this.pause.setSelfVisible(false);
        this.play.setSelfVisible(false);
        this.play.onClick((parent2, mouseButton) -> this.musicManager.setPlaying(true));
        this.pause.onClick((parent2, mouseButton) -> this.musicManager.setPlaying(false));
        this.forwards.onClick((parent2, mouseButton) -> this.musicManager.playNext());
        this.backwards.onClick((parent2, mouseButton) -> this.musicManager.playPrevious());
        this.volumeSlider.addVolumeChangeListener(slider -> this.musicManager.volume = (int) ((1.0F - this.volumeSlider.getVolume()) * 100.0F));
        this.volumeSlider.setVolume(1.0F - (float) this.musicManager.volume / 100.0F);
        this.addToList(
                this.searchBox = new SearchBox(
                        this, "search", PLAYER_WIDTH, 0, this.getWidth() - PLAYER_WIDTH, this.getHeight() - FOOTER_HEIGHT, "Search..."
                )
        );
        this.searchBox.setSelfVisible(true);
        this.searchBox.setListening(false);
    }

    private void showPlaylistPanel(ScrollablePanel var1) {
        if (this.activePlaylistPanel != null) {
            this.activePlaylistPanel.setSelfVisible(false);
        }

        var1.setSelfVisible(true);
        this.currentPlaylistName = var1.getText();
        this.activePlaylistPanel = var1;
        this.searchBox.setSelfVisible(false);
        this.activePlaylistPanel.scrollBarWidth = 65;
    }

    @Override
    public void loadConfig(JsonObject config) {
        super.loadConfig(config);

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
        long var5 = System.nanoTime() - lastFrameTime;
        float var7 = Math.min(10.0F, Math.max(0.0F, (float) var5 / 1.810361E7F));
        lastFrameTime = System.nanoTime();
        super.updatePanelDimensions(mouseX, mouseY);
        if (this.parent != null) {
            if (!this.isDragging()) {
                if ((this.isMouseDownOverComponent || isAnimatingBack) && !this.isDraggable()) {
                    isAnimatingBack = true;
                    int var11 = this.parent.getWidth() - 20 - this.getWidth();
                    this.targetX = Math.max(this.targetX - (this.targetX - (float) var11) * 0.25F * var7, (float) var11);

                    if (!(this.targetX - (float) var11 < 0.0F)) {
                        if (this.targetX - (float) var11 - (float) this.getWidth() > 0.0F) {
                            this.targetX = (float) var11;
                        }
                    } else {
                        this.targetX = (float) var11;
                    }

                    this.setX((int) this.targetX);
                    this.setY((int) this.targetY);
                    if (Math.abs(this.targetX - (float) var11) < 2.0F) {
                        this.setDraggable(true);
                        isAnimatingBack = false;
                    }
                } else if (this.getX() + this.getWidth() > this.parent.getWidth() || this.getX() < 0 || this.getY() < 0) {
                    if (this.targetX == 0.0F || this.targetY == 0.0F) {
                        this.targetX = (float) this.getX();
                        this.targetY = (float) this.getY();
                    }

                    int var8 = this.parent.getWidth() - 40;
                    this.targetX = Math.min(this.targetX - (this.targetX - (float) var8) * 0.25F * var7, (float) var8);

                    if (!(this.targetX - (float) var8 > 0.0F)) {
                        if (this.targetX - (float) var8 + (float) this.getWidth() < 0.0F) {
                            this.targetX = (float) var8;
                        }
                    } else {
                        this.targetX = (float) var8;
                    }

                    if (Math.abs(this.targetX - (float) var8) < 2.0F) {
                        this.targetX = (float) this.getX();
                        this.targetY = (float) this.getY();
                    }

                    this.setX((int) this.targetX);
                    this.setY((int) this.targetY);
                    this.setDraggable(false);
                    this.setDragging(false);
                }
            } else {
                int var12 = mouseX - this.dragOffsetX - (this.parent == null ? 0 : this.parent.getAbsoluteX());
                int var14 = 200;
                assert this.parent != null;
                if (var12 + this.getWidth() > this.parent.getWidth() + var14 && mouseX - this.dragStartMouseX > 70) {
                    int var15 = var12 - this.getX() - var14;
                    this.setX((int) ((float) this.getX() + (float) var15 * 0.5F));
                    this.targetX = (float) this.getX();
                    this.targetY = (float) this.getY();
                }
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        super.applyScaleTransforms();
        super.applyTranslationTransforms();
        this.reShowView.setWidth(this.getX() + this.getWidth() <= this.parent.getWidth() ? 0 : 41);
        this.visibilityAnimation.changeDirection(this.getX() + this.getWidth() > this.parent.getWidth() && !isAnimatingBack ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
        partialTicks *= 0.5F + (1.0F - this.visibilityAnimation.calcPercent()) * 0.5F;
        if (this.musicManager.isPlaying()) {
            this.play.setSelfVisible(false);
            this.pause.setSelfVisible(true);
        } else {
            this.play.setSelfVisible(true);
            this.pause.setSelfVisible(false);
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
        float offset = 55;

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_40,
                offset + this.getX(),
                (float) (this.getY() + 20),
                "Jello",
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_20,
                offset + this.getX() + 80,
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
        int total = this.musicManager.getTotalDuration();
        int duration = this.musicManager.getDuration();
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + PLAYER_WIDTH + 14),
                (float) (this.getY() + this.getHeight() - 10) - 22.0F * partialTicks,
                YoutubeUtils.formatSecondsAsTimestamp(total),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * partialTicks)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + this.getWidth() - 14 - FontUtils.HELVETICA_LIGHT_14.getWidth(YoutubeUtils.formatSecondsAsTimestamp(duration))),
                (float) (this.getY() + this.getHeight() - 10) - 22.0F * partialTicks,
                YoutubeUtils.formatSecondsAsTimestamp(duration),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * partialTicks)
        );
    }

    private void drawThumbnail(float partialTicks) {
        Texture var4 = this.musicManager.getNotification();
        Texture var5 = this.musicManager.getSongThumbnail();
        if (var4 != null && var5 != null) {
            RenderUtils.drawImage(
                    (float) this.getX(),
                    (float) (this.getY() + this.getHeight() - FOOTER_HEIGHT),
                    (float) this.getWidth(),
                    (float) FOOTER_HEIGHT,
                    var5,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * partialTicks)
            );
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
            RenderUtils.drawImage(
                    (float) (this.getX() + (PLAYER_WIDTH - 114) / 2),
                    (float) (this.getY() + this.getHeight() - 170),
                    114.0F,
                    114.0F,
                    var4,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
            );
            RenderUtils.drawRoundedRect(
                    (float) (this.getX() + (PLAYER_WIDTH - 114) / 2), (float) (this.getY() + this.getHeight() - 170), 114.0F, 114.0F, 14.0F, partialTicks
            );
        } else {
            RenderUtils.drawImage(
                    (float) this.getX(),
                    (float) (this.getY() + this.getHeight() - FOOTER_HEIGHT),
                    (float) this.getWidth(),
                    (float) FOOTER_HEIGHT,
                    Resources.BACKGROUND,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * partialTicks)
            );
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
            RenderUtils.drawImage(
                    (float) (this.getX() + (PLAYER_WIDTH - 114) / 2),
                    (float) (this.getY() + this.getHeight() - 170),
                    114.0F,
                    114.0F,
                    Resources.ARTWORK,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
            );
            RenderUtils.drawRoundedRect(
                    (float) (this.getX() + (PLAYER_WIDTH - 114) / 2), (float) (this.getY() + this.getHeight() - 170), 114.0F, 114.0F, 14.0F, partialTicks
            );
        }
    }

    private void drawSongTitle(float partialTicks) {
        if (this.musicManager.getSongProcessed() == null) {
            this.drawString(partialTicks, "Jello Music", PLAYER_WIDTH - 30 * 2, 12, 0);
            return;
        }

        if (this.musicManager.getSongProcessed().title != null) {
            String[] titles = this.musicManager.getSongProcessed().title.split(" - ");
            int offset = 30;
            if (titles.length <= 1) {
                this.drawString(partialTicks, !titles[0].isEmpty() ? titles[0] : "Jello Music", PLAYER_WIDTH - offset * 2, 12, 0);
            } else {
                this.drawString(partialTicks, titles[1], PLAYER_WIDTH - offset * 2, 0, 0);
                this.drawString(partialTicks, titles[0], PLAYER_WIDTH - offset * 2, 20, -1000);
            }
        }
    }

    private void drawString(float alpha, String text, int var3, int var4, int var5) {
        Date date = new Date();
        float animationOffset = (float) ((date.getTime() + (long) var5) % 8500) / 8500.0F;
        if (!(animationOffset < 0.4F)) {
            animationOffset -= 0.4F;
            animationOffset = (float) ((double) animationOffset * (5.0 / 3.0));
        } else {
            animationOffset = 0.0F;
        }

        animationOffset = QuadraticEasing.easeInOutQuad(animationOffset, 0.0F, 1.0F, 1.0F);
        int textWidth = FontUtils.HELVETICA_LIGHT_14.getWidth(text);
        int width = Math.min(var3, textWidth);
        int height = FontUtils.HELVETICA_LIGHT_14.getHeight();
        int x = this.getX() + (PLAYER_WIDTH - width) / 2;
        int y = this.getY() + this.getHeight() - 50 + var4;

        if (textWidth <= var3) {
            animationOffset = 0.0F;
        }

        ScissorUtils.startScissor(x, y, x + width, y + height, true);
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) x - (float) textWidth * animationOffset - 50.0F * animationOffset,
                (float) y,
                text,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha * alpha * Math.min(1.0F, Math.max(0.0F, 1.0F - animationOffset * 0.75F)))
        );
        if (animationOffset > 0.0F) {
            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_14,
                    (float) x - (float) textWidth * animationOffset + (float) textWidth,
                    (float) y,
                    text,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alpha * alpha)
            );
        }

        ScissorUtils.restoreScissor();
    }

    private void drawHeaderAndBackground(float var1) {
        this.activePlaylistPanel.setReAddChildren(false);
        if (this.lastScrollOffset != this.activePlaylistPanel.getScrollOffset()) {
            try {
                if (this.texture != null) {
                    this.texture.release();
                }

                this.texture = ImageUtils.createTexture(
                        "blur",
                        ImageUtils.captureAndProcessRegion(this.getX() + PLAYER_WIDTH, this.getY(), this.getWidth() - PLAYER_WIDTH, HEADER_HEIGHT, 10, 10)
                );
            } catch (IOException e) {
                Client.LOGGER.error("Failed to blur image", e);
            }
        }

        float var4 = this.lastScrollOffset < 50 ? (float) this.lastScrollOffset / 50.0F : 1.0F;
        if (this.texture != null) {
            RenderUtils.drawTexture(
                    (float) PLAYER_WIDTH,
                    0.0F,
                    (float) (this.getWidth() - PLAYER_WIDTH),
                    (float) HEADER_HEIGHT,
                    this.texture,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1)
            );
        }

        RenderUtils.drawRoundedRect(
                (float) PLAYER_WIDTH,
                0.0F,
                (float) this.getWidth(),
                (float) HEADER_HEIGHT,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1 * 0.2F)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25,
                (float) ((this.getWidth() - FontUtils.HELVETICA_LIGHT_25.getWidth(this.currentPlaylistName) + PLAYER_WIDTH) / 2),
                16.0F + (1.0F - var4) * 14.0F,
                this.currentPlaylistName,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_25,
                (float) ((this.getWidth() - FontUtils.HELVETICA_MEDIUM_25.getWidth(this.currentPlaylistName) + PLAYER_WIDTH) / 2),
                16.0F + (1.0F - var4) * 14.0F,
                this.currentPlaylistName,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 1.0F - var4)
        );
        RenderUtils.drawImage(
                (float) PLAYER_WIDTH,
                (float) HEADER_HEIGHT,
                (float) (this.getWidth() - PLAYER_WIDTH),
                20.0F,
                Resources.SHADOW_BOTTOM,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1 * 0.5F)
        );
        this.lastScrollOffset = this.activePlaylistPanel.getScrollOffset();
    }

    private ScrollablePanel createPlaylistUI(PlaylistData playlist, ColorHelper colorHelper) {
        if (!this.musicTabs.hasChildWithName(playlist.id)) {
            int index = this.musicTabs.getContent().getChildren().size();
            Button playlistName;
            this.musicTabs.addToList(
                    playlistName = new Button(
                            this.musicTabs,
                            playlist.id,
                            0,
                            index * 40,
                            PLAYER_WIDTH,
                            40,
                            colorHelper,
                            playlist.name,
                            FontUtils.HELVETICA_LIGHT_14
                    )
            );
            ScrollablePanel queue;
            this.addToList(
                    queue = new ScrollablePanel(
                            this,
                            playlist.id,
                            PLAYER_WIDTH,
                            0,
                            this.getWidth() - PLAYER_WIDTH,
                            this.getHeight() - FOOTER_HEIGHT,
                            ColorHelper.DEFAULT_COLOR,
                            playlist.name
                    )
            );
            queue.setAllowUpdatesWhenHidden(true);
            queue.setSelfVisible(false);
            queue.setListening(false);
            playlistName.onClick((parent, mouseButton) -> this.showPlaylistPanel(queue));
            return queue;
        }

        return (ScrollablePanel) this.getChildByName(playlist.id);
    }

    private void populatePlaylistSongs(ScrollablePanel queue, PlaylistData playlist) {
        if (queue == null) {
            return;
        }

        for (int i = 0; i < playlist.songs.size(); i++) {
            SongData song = playlist.songs.get(i);
            ThumbnailButton btnThumbnail;
            int x = 65;
            int y = 10;
            if (!queue.hasChildWithName(song.id)) {
                queue.addToList(
                        btnThumbnail = new ThumbnailButton(
                                queue,
                                y + i % 3 * 183 - (i % 3 <= 0 ? 0 : y) - (i % 3 <= 1 ? 0 : y),
                                x + y + (i - i % 3) / 3 * 210,
                                183,
                                220,
                                song
                        )
                );
                btnThumbnail.onClick((parent, mouseButton) -> {
                    if (this.parent.checkMusicPlayerDependencies())
                        musicManager.play(playlist, song);
                });
            }
        }
    }
}
