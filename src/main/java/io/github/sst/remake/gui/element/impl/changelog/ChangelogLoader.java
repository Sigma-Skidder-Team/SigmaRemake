package io.github.sst.remake.gui.element.impl.changelog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.gui.impl.menu.ChangelogPage;
import net.minecraft.util.Util;

public class ChangelogLoader implements Runnable {
    public final JsonArray changelogJson;
    public final ChangelogPage changelogPage;

    public ChangelogLoader(ChangelogPage screen, JsonArray changelogJson) {
        this.changelogPage = screen;
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

                this.changelogPage.scrollPanel.getButton().showAlert(change = new Change(this.changelogPage.scrollPanel, "changelog" + i, entry));
                change.setY(y);
                y += change.getHeight();
            }
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        }
    }
}

