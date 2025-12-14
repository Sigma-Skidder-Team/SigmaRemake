package io.github.sst.remake.gui.element.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.gui.element.impl.jello.Change;
import io.github.sst.remake.gui.screen.ChangelogScreen;
import net.minecraft.util.Util;

public class Class576 implements Runnable {
    public final JsonArray field2812;
    public final ChangelogScreen field2813;

    public Class576(ChangelogScreen var1, JsonArray var2) {
        this.field2813 = var1;
        this.field2812 = var2;
    }

    @Override
    public void run() {
        int var3 = 75;

        try {
            for (int var4 = 0; var4 < this.field2812.size(); var4++) {
                JsonObject var5 = this.field2812.get(var4).getAsJsonObject();
                Change var6;
                if (var5.has("url")) {
                    Util.getOperatingSystem().open(var5.get("url").getAsString());
                }

                this.field2813.scrollPanel.getButton().showAlert(var6 = new Change(this.field2813.scrollPanel, "changelog" + var4, var5));
                var6.setYA(var3);
                var3 += var6.getHeightA();
            }
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        }
    }
}
