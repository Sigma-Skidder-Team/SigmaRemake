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
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SearchBox extends Widget {
    public ScrollablePanel albumView;
    public TextField searchInput;
    private List<SongData> searchResults;
    private final MusicManager musicManager = Client.INSTANCE.musicManager;

    public SearchBox(GuiComponent parent, String name, int x, int y, int width, int height, String titleText) {
        super(parent, name, x, y, width, height, ColorHelper.DEFAULT_COLOR, titleText, false);

        this.albumView = new ScrollablePanel(this, "albumView", 0, 0, width, height, ColorHelper.DEFAULT_COLOR, "View");
        this.addToList(this.albumView);

        this.searchInput = new TextField(this, "searchInput", 30, 14, width - 60, 70, TextField.INVERTED_COLORS, "", "Search...");
        this.addToList(this.searchInput);

        this.searchInput.setReAddChildren(true);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ENTER && this.searchInput.isFocused()) {
            this.searchInput.setFocused(false);

            new Thread(() -> {
                this.searchResults = new ArrayList<>();

                SongData[] results = YoutubeUtils.getFromSearch(this.searchInput.getText());
                for (SongData result : results) {
                    this.searchResults.add(new SongData(result.id, result.title, result.url));
                }

                this.addRunnable(() -> {
                    this.removeChildren(this.albumView);

                    this.albumView = new ScrollablePanel(
                            this,
                            "albumView",
                            0,
                            0,
                            this.width,
                            this.height,
                            ColorHelper.DEFAULT_COLOR,
                            "View"
                    );
                    this.addToList(this.albumView);

                    if (this.searchResults == null) {
                        return;
                    }

                    for (int index = 0; index < this.searchResults.size(); index++) {
                        SongData song = this.searchResults.get(index);

                        ThumbnailButton thumbnail = createThumbnail(index, song);

                        this.albumView.addToList(thumbnail);
                        thumbnail.onClick((clickedParent, mouseButton) -> this.musicManager.play(null, song));
                    }
                });
            }).start();
        }

        super.keyPressed(keyCode);
    }

    private @NotNull ThumbnailButton createThumbnail(int index, SongData song) {
        int column = index % 3;
        int row = (index - column) / 3;

        int xOffset = 10 + column * 183 - (column <= 0 ? 0 : 10) - (column <= 1 ? 0 : 10);
        int yOffset = 80 + 10 + row * 210;

        return new ThumbnailButton(
                this.albumView,
                xOffset,
                yOffset,
                183,
                220,
                song
        );
    }
}
