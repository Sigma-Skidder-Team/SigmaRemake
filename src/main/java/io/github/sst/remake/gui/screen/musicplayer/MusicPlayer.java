package io.github.sst.remake.gui.screen.musicplayer;

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
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.ImageUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.newdawn.slick.opengl.texture.Texture;
import org.newdawn.slick.util.image.BufferedImageUtil;

import java.io.IOException;
import java.util.*;

public class MusicPlayer extends Widget {
    private final int playerWidth = 250;
    private final int field20847 = 64;
    private final int field20848 = 94;
    private String field20849 = "Music Player";
    private final ScrollablePanel musicTabs;
    private ScrollablePanel field20852;
    private final GuiComponent musicControls;
    private final MusicManager musicManager = Client.INSTANCE.musicManager;
    public static Map<String, PlaylistData> videoMap = new LinkedHashMap<>();
    private final Button play;
    private final Button pause;
    private final Button forwards;
    private final Button backwards;
    private final VolumeSlider volumeSlider;
    private int field20863;
    private Texture texture;
    private final GuiComponent reShowView;
    public SearchBox searchBox;
    public ProgressBar field20867;
    public static long time = 0L;
    public float field20871 = 0.0F;
    public float field20872 = 0.0F;
    private final AnimationUtils field20873 = new AnimationUtils(80, 150, AnimationUtils.Direction.FORWARDS);
    public boolean field20874 = false;

    public ClickGuiScreen parent;

    public MusicPlayer(ClickGuiScreen parent, String var2) {
        super(parent, var2, 875, 55, 800, 600, false);
        this.parent = parent;

        time = System.nanoTime();
        this.setWidth(800);
        this.setHeight(600);
        this.setX(Math.abs(this.getX()));
        this.setY(Math.abs(this.getY()));
        this.addToList(this.musicTabs = new ScrollablePanel(this, "musictabs", 0, this.field20847 + 14, this.playerWidth, this.getHeight() - 64 - this.field20848));
        this.addToList(
                this.musicControls = new ScrollablePanel(
                        this, "musiccontrols", this.playerWidth, this.getHeight() - this.field20848, this.getWidth() - this.playerWidth, this.field20848
                )
        );
        this.addToList(this.reShowView = new GuiComponent(this, "reShowView", 0, 0, 1, this.getHeight()));
        SpectrumButton spectrumBtn;
        this.addToList(spectrumBtn = new SpectrumButton(this, "spectrumButton", 15, this.height - 140, 40, 40, this.musicManager.spectrum));
        spectrumBtn.setReAddChildren(true);
        spectrumBtn.onClick((parent2, mouseButton) -> {
            this.musicManager.spectrum = !this.musicManager.spectrum;
            ((SpectrumButton) parent2).setSpectrum(this.musicManager.spectrum);
        });
        this.musicTabs.setListening(false);
        spectrumBtn.setListening(false);
        this.musicControls.setListening(false);
        this.reShowView.setListening(false);
        ColorHelper color = new ColorHelper(1250067, -15329770).setTextColor(ClientColors.LIGHT_GREYISH_BLUE.getColor()).setHeightAlignment(FontAlignment.CENTER);
        List<Thread> threads = new ArrayList<>();

        for (PlaylistData data : musicManager.playlists) {
            threads.add(new Thread(() -> {
                if (!videoMap.containsKey(data.id) && !data.updated) {
                    data.updated = true;
                    data.refresh();

                    videoMap.put(data.id, data);
                }

                this.addRunnable(() -> initializeMusicPlayerContent(data, color));
            }));
            threads.get(threads.size() - 1).start();
        }

        int var15 = (this.getWidth() - this.playerWidth - 38) / 2;
        this.musicControls
                .addToList(
                        this.play = new Image(
                                this.musicControls, "play", var15, 27, 38, 38, Resources.PLAY, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls
                .addToList(
                        this.pause = new Image(
                                this.musicControls, "pause", var15, 27, 38, 38, Resources.PAUSE, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls
                .addToList(
                        this.forwards = new Image(
                                this.musicControls, "forwards", var15 + 114, 23, 46, 46, Resources.FORWARDS, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls
                .addToList(
                        this.backwards = new Image(
                                this.musicControls, "backwards", var15 - 114, 23, 46, 46, Resources.BACKWARDS, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls.addToList(this.volumeSlider = new VolumeSlider(this.musicControls, "volume", this.getWidth() - this.playerWidth - 19, 14, 4, 40));
        ChangingButton repeat;
        this.musicControls.addToList(repeat = new ChangingButton(this.musicControls, "repeat", 14, 34, 27, 20, this.musicManager.repeat));
        repeat.onPress(var2x -> this.musicManager.repeat = repeat.repeatMode);
        this.addToList(this.field20867 = new ProgressBar(this, "progress", this.playerWidth, this.getHeight() - 5, this.getWidth() - this.playerWidth, 5));
        this.field20867.setReAddChildren(true);
        this.field20867.setListening(false);
        this.reShowView.setReAddChildren(true);
        this.reShowView.addMouseButtonCallback((var1x, var2x) -> {
            this.field20874 = true;
            this.field20871 = (float) this.getX();
            this.field20872 = (float) this.getY();
        });
        this.pause.setSelfVisible(false);
        this.play.setSelfVisible(false);
        this.play.onClick((var1x, var2x) -> this.musicManager.setPlaying(true));
        this.pause.onClick((var1x, var2x) -> this.musicManager.setPlaying(false));
        this.forwards.onClick((var1x, var2x) -> this.musicManager.playNext());
        this.backwards.onClick((var1x, var2x) -> this.musicManager.playPrevious());
        this.volumeSlider.method13709(var1x -> this.musicManager.volume = (int) ((1.0F - this.volumeSlider.getVolume()) * 100.0F));
        this.volumeSlider.setVolume(1.0F - (float) this.musicManager.volume / 100.0F);
        this.addToList(
                this.searchBox = new SearchBox(
                        this, "search", this.playerWidth, 0, this.getWidth() - this.playerWidth, this.getHeight() - this.field20848, "Search..."
                )
        );
        this.searchBox.setSelfVisible(true);
        this.searchBox.setListening(false);
    }

    private void method13189(ScrollablePanel var1) {
        if (this.field20852 != null) {
            this.field20852.setSelfVisible(false);
        }

        var1.setSelfVisible(true);
        this.field20849 = var1.getText();
        this.field20852 = var1;
        this.searchBox.setSelfVisible(false);
        this.field20852.field21207 = 65;
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        long var5 = System.nanoTime() - time;
        float var7 = Math.min(10.0F, Math.max(0.0F, (float) var5 / 1.810361E7F));
        time = System.nanoTime();
        super.updatePanelDimensions(mouseX, mouseY);
        if (this.parent != null) {
            if (!this.isDragging()) {
                if ((this.isMouseDownOverComponent || this.field20874) && !this.isDraggable()) {
                    this.field20874 = true;
                    int var11 = this.parent.getWidth() - 20 - this.getWidth();
                    int var13 = (this.parent.getHeight() - this.getHeight()) / 2;
                    this.field20871 = Math.max(this.field20871 - (this.field20871 - (float) var11) * 0.25F * var7, (float) var11);

                    if (!(this.field20871 - (float) var11 < 0.0F)) {
                        if (this.field20871 - (float) var11 - (float) this.getWidth() > 0.0F) {
                            this.field20871 = (float) var11;
                        }
                    } else {
                        this.field20871 = (float) var11;
                    }

                    this.setX((int) this.field20871);
                    this.setY((int) this.field20872);
                    if (Math.abs(this.field20871 - (float) var11) < 2.0F && Math.abs(this.field20872 - (float) var13) < 2.0F) {
                        this.setDraggable(true);
                        this.field20874 = false;
                    }
                } else if (this.getX() + this.getWidth() > this.parent.getWidth() || this.getX() < 0 || this.getY() < 0) {
                    if (this.field20871 == 0.0F || this.field20872 == 0.0F) {
                        this.field20871 = (float) this.getX();
                        this.field20872 = (float) this.getY();
                    }

                    int var8 = this.parent.getWidth() - 40;
                    int var9 = (this.parent.getHeight() - this.getHeight()) / 2;
                    this.field20871 = Math.min(this.field20871 - (this.field20871 - (float) var8) * 0.25F * var7, (float) var8);

                    if (!(this.field20871 - (float) var8 > 0.0F)) {
                        if (this.field20871 - (float) var8 + (float) this.getWidth() < 0.0F) {
                            this.field20871 = (float) var8;
                        }
                    } else {
                        this.field20871 = (float) var8;
                    }

                    if (Math.abs(this.field20871 - (float) var8) < 2.0F && Math.abs(this.field20872 - (float) var9) < 2.0F) {
                        this.field20871 = (float) this.getX();
                        this.field20872 = (float) this.getY();
                    }

                    this.setX((int) this.field20871);
                    this.setY((int) this.field20872);
                    this.setDraggable(false);
                    this.setDragging(false);
                }
            } else {
                int var12 = mouseX - this.sizeWidthThingy - (this.parent == null ? 0 : this.parent.getAbsoluteX());
                int var14 = 200;
                assert this.parent != null;
                if (var12 + this.getWidth() > this.parent.getWidth() + var14 && mouseX - this.mouseX > 70) {
                    int var15 = var12 - this.getX() - var14;
                    this.setX((int) ((float) this.getX() + (float) var15 * 0.5F));
                    this.field20871 = (float) this.getX();
                    this.field20872 = (float) this.getY();
                }
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        super.applyScaleTransforms();
        super.applyTranslationTransforms();
        this.reShowView.setWidth(this.getX() + this.getWidth() <= this.parent.getWidth() ? 0 : 41);
        this.field20873
                .changeDirection(this.getX() + this.getWidth() > this.parent.getWidth() && !this.field20874 ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
        partialTicks *= 0.5F + (1.0F - this.field20873.calcPercent()) * 0.5F;
        if (this.musicManager.isPlaying()) {
            this.play.setSelfVisible(false);
            this.pause.setSelfVisible(true);
        } else {
            this.play.setSelfVisible(true);
            this.pause.setSelfVisible(false);
        }

        RenderUtils.drawRoundedRect(
                (float) (this.getX() + this.playerWidth),
                (float) this.getY(),
                (float) (this.getX() + this.getWidth()),
                (float) (this.getY() + this.getHeight() - this.field20848),
                ColorHelper.applyAlpha(-14277082, partialTicks * 0.8F)
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) (this.getX() + this.playerWidth),
                (float) (this.getY() + this.getHeight() - this.field20848),
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
        if (this.field20852 != null) {
            this.method13196(partialTicks);
        }
    }

    private void drawDuration(float var1) {
        int total = this.musicManager.getTotalDuration();
        int duration = this.musicManager.getDuration();
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + this.playerWidth + 14),
                (float) (this.getY() + this.getHeight() - 10) - 22.0F * var1,
                YoutubeUtils.formatSecondsAsTimestamp(total),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + this.getWidth() - 14 - FontUtils.HELVETICA_LIGHT_14.getWidth(YoutubeUtils.formatSecondsAsTimestamp(duration))),
                (float) (this.getY() + this.getHeight() - 10) - 22.0F * var1,
                YoutubeUtils.formatSecondsAsTimestamp(duration),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
        );
    }

    private void drawThumbnail(float var1) {
        Texture var4 = this.musicManager.getNotification();
        Texture var5 = this.musicManager.getSongThumbnail();
        if (var4 != null && var5 != null) {
            RenderUtils.drawImage(
                    (float) this.getX(),
                    (float) (this.getY() + this.getHeight() - this.field20848),
                    (float) this.getWidth(),
                    (float) this.field20848,
                    var5,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
            );
            RenderUtils.drawRoundedRect(
                    (float) this.getX(),
                    (float) (this.getY() + this.getHeight() - this.field20848),
                    (float) (this.getX() + this.getWidth()),
                    (float) (this.getY() + this.getHeight() - 5),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtils.drawRoundedRect(
                    (float) this.getX(),
                    (float) (this.getY() + this.getHeight() - 5),
                    (float) (this.getX() + this.playerWidth),
                    (float) (this.getY() + this.getHeight()),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtils.drawImage(
                    (float) (this.getX() + (this.playerWidth - 114) / 2),
                    (float) (this.getY() + this.getHeight() - 170),
                    114.0F,
                    114.0F,
                    var4,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1)
            );
            RenderUtils.drawRoundedRect(
                    (float) (this.getX() + (this.playerWidth - 114) / 2), (float) (this.getY() + this.getHeight() - 170), 114.0F, 114.0F, 14.0F, var1
            );
        } else {
            RenderUtils.drawImage(
                    (float) this.getX(),
                    (float) (this.getY() + this.getHeight() - this.field20848),
                    (float) this.getWidth(),
                    (float) this.field20848,
                    Resources.BACKGROUND,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
            );
            RenderUtils.drawRoundedRect(
                    (float) this.getX(),
                    (float) (this.getY() + this.getHeight() - this.field20848),
                    (float) (this.getX() + this.getWidth()),
                    (float) (this.getY() + this.getHeight() - 5),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtils.drawRoundedRect(
                    (float) this.getX(),
                    (float) (this.getY() + this.getHeight() - 5),
                    (float) (this.getX() + this.playerWidth),
                    (float) (this.getY() + this.getHeight()),
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtils.drawImage(
                    (float) (this.getX() + (this.playerWidth - 114) / 2),
                    (float) (this.getY() + this.getHeight() - 170),
                    114.0F,
                    114.0F,
                    Resources.ARTWORK,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1)
            );
            RenderUtils.drawRoundedRect(
                    (float) (this.getX() + (this.playerWidth - 114) / 2), (float) (this.getY() + this.getHeight() - 170), 114.0F, 114.0F, 14.0F, var1
            );
        }
    }

    private void drawSongTitle(float alpha) {
        if (this.musicManager.getSongProcessed() == null) {
            this.drawString(alpha, "Jello Music", this.playerWidth - 30 * 2, 12, 0);
            return;
        }

        if (this.musicManager.getSongProcessed().title != null) {
            String[] titles = this.musicManager.getSongProcessed().title.split(" - ");
            int offset = 30;
            if (titles.length <= 1) {
                this.drawString(alpha, !titles[0].isEmpty() ? titles[0] : "Jello Music", this.playerWidth - offset * 2, 12, 0);
            } else {
                this.drawString(alpha, titles[1], this.playerWidth - offset * 2, 0, 0);
                this.drawString(alpha, titles[0], this.playerWidth - offset * 2, 20, -1000);
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
        int x = this.getX() + (this.playerWidth - width) / 2;
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

    private void method13196(float var1) {
        this.field20852.setReAddChildren(false);
        if (this.field20863 != this.field20852.getScrollOffset()) {
            try {
                if (this.texture != null) {
                    this.texture.release();
                }

                this.texture = BufferedImageUtil.getTexture(
                        "blur",
                        ImageUtils.captureAndProcessRegion(this.getX() + this.playerWidth, this.getY(), this.getWidth() - this.playerWidth, this.field20847, 10, 10)
                );
            } catch (IOException e) {
                Client.LOGGER.error("Failed to blur image", e);
            }
        }

        float var4 = this.field20863 < 50 ? (float) this.field20863 / 50.0F : 1.0F;
        if (this.texture != null) {
            RenderUtils.drawTexture(
                    (float) this.playerWidth,
                    0.0F,
                    (float) (this.getWidth() - this.playerWidth),
                    (float) this.field20847,
                    this.texture,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1)
            );
        }

        RenderUtils.drawRoundedRect(
                (float) this.playerWidth,
                0.0F,
                (float) this.getWidth(),
                (float) this.field20847,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1 * 0.2F)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25,
                (float) ((this.getWidth() - FontUtils.HELVETICA_LIGHT_25.getWidth(this.field20849) + this.playerWidth) / 2),
                16.0F + (1.0F - var4) * 14.0F,
                this.field20849,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4)
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_MEDIUM_25,
                (float) ((this.getWidth() - FontUtils.HELVETICA_MEDIUM_25.getWidth(this.field20849) + this.playerWidth) / 2),
                16.0F + (1.0F - var4) * 14.0F,
                this.field20849,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 1.0F - var4)
        );
        RenderUtils.drawImage(
                (float) this.playerWidth,
                (float) this.field20847,
                (float) (this.getWidth() - this.playerWidth),
                20.0F,
                Resources.SHADOW_BOTTOM,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1 * 0.5F)
        );
        this.field20863 = this.field20852.getScrollOffset();
    }

    private void initializeMusicPlayerContent(PlaylistData playlist, ColorHelper colorHelper) {
        if (!this.musicTabs.hasChildWithName(playlist.id)) {
            Button playlistName;
            this.musicTabs.addToList(
                            playlistName = new Button(
                                    this.musicTabs,
                                    playlist.id,
                                    0,
                                    this.musicTabs.getButton().getChildren().size() * 40,
                                    this.playerWidth,
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
                                    this.playerWidth,
                                    0,
                                    this.getWidth() - this.playerWidth,
                                    this.getHeight() - this.field20848,
                                    ColorHelper.DEFAULT_COLOR,
                                    playlist.name
                            )
                    );
            queue.method13514(true);
            queue.setSelfVisible(false);
            queue.setListening(false);
            for (int i = 0; i < playlist.songs.size(); i++) {
                SongData song = playlist.songs.get(i);
                ThumbnailButton btnThumbnail;
                int x = 65;
                int y = 10;
                if (!queue.hasChildWithName(playlist.id)) {
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

            playlistName.onClick((var2, var3x) -> this.method13189(queue));
        }
    }
}
