package io.github.sst.remake.gui.element.impl.spotlight;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.GuiComponent;
import io.github.sst.remake.gui.element.InteractiveWidget;
import io.github.sst.remake.gui.framework.widget.TextField;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class SpotlightDialog extends InteractiveWidget {
    public TextField query;
    public String field20640;

    public SpotlightDialog(GuiComponent screen, String iconName, int var3, int var4, int width, int height, boolean var7) {
        super(screen, iconName, var3, var4, width, height, var7);
        this.addToList(this.query = new TextField(this, "search", 50, 0, width - 60, height - 2, TextField.field20741, "", "Search..."));
        this.query.setRoundedThingy(false);
        this.query.addChangeListener(var1x -> this.field20640 = this.query.getText());
    }

    @Override
    public void draw(float partialTicks) {
        this.query.setFocused(true);
        int var4 = 10;
        RenderUtils.drawRoundedRect(
                (float) (this.x + var4 / 2),
                (float) (this.y + var4 / 2),
                (float) (this.width - var4),
                (float) (this.height - var4),
                9.0F,
                partialTicks * 0.9F
        );
        RenderUtils.drawRoundedRect(
                (float) (this.x + var4 / 2),
                (float) (this.y + var4 / 2),
                (float) (this.width - var4),
                (float) (this.height - var4),
                30.0F,
                partialTicks * 0.4F
        );
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                (float) var4,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.97F)
        );
        RenderUtils.drawImage(
                (float) (this.x + 20),
                (float) (this.y + 20),
                20.0F,
                20.0F,
                Resources.searchPNG,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F)
        );
        List<Module> modules = this.getModules();
        if (!modules.isEmpty() && this.sortByName(this.field20640, modules.get(0).getName())) {
            String var6 = modules.get(0).getName();
            String var7 = this.field20640
                    + modules.get(0).getName().substring(this.field20640.length(), var6.length())
                    + (!modules.get(0).isEnabled() ? " - Disabled" : " - Enabled");
            RenderUtils.drawString(
                    this.query.getFont(),
                    (float) (this.x + 54),
                    (float) (this.y + 14),
                    var7,
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.25F)
            );
        }

        super.draw(partialTicks);
    }

    public List<Module> getModules() {
        List<Module> var3 = new ArrayList<>();
        if (this.field20640 != null && !this.field20640.isEmpty()) {
            for (Module var5 : Client.INSTANCE.moduleManager.modules) {
                if (this.sortByName(this.field20640, var5.getName())) {
                    var3.add(var5);
                }
            }

            return var3;
        } else {
            return var3;
        }
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 257) {
            List<Module> var4 = this.getModules();
            if (!var4.isEmpty()) {
                var4.get(0).toggle();
            }

            MinecraftClient.getInstance().openScreen(null);
        }
    }

    private boolean sortByName(String var1, String var2) {
        return var1 == null || var1 == "" || var2 == null || var2.toLowerCase().startsWith(var1.toLowerCase());
    }
}
