package io.github.sst.remake.gui.screen.musicplayer;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.gui.framework.widget.TextField;
import io.github.sst.remake.manager.impl.MusicManager;
import io.github.sst.remake.util.client.yt.SongData;
import io.github.sst.remake.util.http.YoutubeUtils;
import io.github.sst.remake.util.math.color.ColorHelper;

import java.util.ArrayList;

public class SearchBox extends Widget {
    public ScrollablePanel albumView;
    public TextField searchInput;
    private ArrayList<SongData> field20842;
    private final MusicManager musicManager = Client.INSTANCE.musicManager;

    public SearchBox(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, String var7) {
        super(var1, var2, var3, var4, var5, var6, ColorHelper.DEFAULT_COLOR, var7, false);
        this.addToList(this.albumView = new ScrollablePanel(this, "albumView", 0, 0, var5, var6, ColorHelper.DEFAULT_COLOR, "View"));
        this.addToList(this.searchInput = new TextField(this, "searchInput", 30, 14, var5 - 60, 70, TextField.field20742, "", "Search..."));
        this.searchInput.setReAddChildren(true);
    }

    @Override
    public void draw(float partialTicks) {
        super.draw(partialTicks);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == 257 && this.searchInput.isFocused()) {
            this.searchInput.setFocused(false);
            new Thread(
                    () -> {
                        this.field20842 = new ArrayList<>();
                        SongData[] thumbnails = YoutubeUtils.getFromSearch(this.searchInput.getText());

                        for (SongData data : thumbnails) {
                            this.field20842.add(new SongData(data.id, data.title, data.url));
                        }

                        this.addRunnable(
                                () -> {
                                    this.removeChildren(this.albumView);
                                    this.addToList(
                                            this.albumView = new ScrollablePanel(this, "albumView", 0, 0, this.width, this.height, ColorHelper.DEFAULT_COLOR, "View")
                                    );
                                    if (this.field20842 != null) {
                                        for (int var3x = 0; var3x < this.field20842.size(); var3x++) {
                                            SongData song = this.field20842.get(var3x);
                                            ThumbnailButton thumbnail;
                                            this.albumView
                                                    .addToList(
                                                            thumbnail = new ThumbnailButton(
                                                                    this.albumView,
                                                                    10 + var3x % 3 * 183 - (var3x % 3 <= 0 ? 0 : 10) - (var3x % 3 <= 1 ? 0 : 10),
                                                                    80 + 10 + (var3x - var3x % 3) / 3 * 210,
                                                                    183,
                                                                    220,
                                                                    song
                                                            )
                                                    );
                                            thumbnail.onClick((var2, var3xx) -> this.musicManager.play(null, song));
                                        }
                                    }
                                }
                        );
                    }
            )
                    .start();
        }

        super.keyPressed(keyCode);
    }
}
