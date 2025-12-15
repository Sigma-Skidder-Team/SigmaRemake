package io.github.sst.remake.gui.element.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.gui.element.impl.jello.Change;
import io.github.sst.remake.gui.screen.ChangelogScreen;
import net.minecraft.util.Util;

public class ChangelogLoader implements Runnable {
    public final JsonArray changelogJson;
    public final ChangelogScreen changelogScreen;

    public ChangelogLoader(ChangelogScreen screen, JsonArray changelogJson) {
        this.changelogScreen = screen;
        this.changelogJson = changelogJson;
    }

    @Override
    public void run() {
        int y = 75;

        try {
            for (int i = 0; i < this.changelogJson.size(); i++) {
                JsonObject entry = this.changelogJson.get(i).getAsJsonObject();
                Change change;
                if (entry.has("url")) {
                    Util.getOperatingSystem().open(entry.get("url").getAsString());
                }

                this.changelogScreen.scrollPanel.getButton().showAlert(change = new Change(this.changelogScreen.scrollPanel, "changelog" + i, entry));
                change.setY(y);
                y += change.getHeight();
            }
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        }
    }
}

