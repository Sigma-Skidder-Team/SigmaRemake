package io.github.sst.remake.gui.screen.spotlight;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.widget.TextField;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SearchDialog extends InteractiveWidget {
    public TextField queryInput;
    public String queryText;

    public SearchDialog(GuiComponent screen, String name, int x, int y, int width, int height, boolean draggable) {
        super(screen, name, x, y, width, height, draggable);

        this.queryInput = new TextField(
                this,
                "search",
                50,
                0,
                width - 60,
                height - 2,
                TextField.DEFAULT_COLORS,
                "",
                "Search..."
        );
        this.addToList(this.queryInput);

        this.queryInput.setUnderlineEnabled(false);
        this.queryInput.addChangeListener(ignored -> this.queryText = this.queryInput.getText());
    }

    @Override
    public void draw(float partialTicks) {
        this.queryInput.setFocused(true);

        int padding = 10;

        RenderUtils.drawRoundedRect(
                (float) (this.x + padding / 2),
                (float) (this.y + padding / 2),
                (float) (this.width - padding),
                (float) (this.height - padding),
                9.0F,
                partialTicks * 0.9F
        );
        RenderUtils.drawRoundedRect(
                (float) (this.x + padding / 2),
                (float) (this.y + padding / 2),
                (float) (this.width - padding),
                (float) (this.height - padding),
                30.0F,
                partialTicks * 0.4F
        );
        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                (float) padding,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.97F)
        );

        RenderUtils.drawImage(
                (float) (this.x + 20),
                (float) (this.y + 20),
                20.0F,
                20.0F,
                Resources.SEARCH_ICON,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F)
        );

        List<Module> matches = this.getMatchingModules();
        if (!matches.isEmpty() && this.matchesQueryPrefix(this.queryText, matches.get(0).getName())) {
            Module first = matches.get(0);

            String moduleName = first.getName();
            String autoComplete =
                    this.queryText
                            + moduleName.substring(this.queryText.length())
                            + (first.isEnabled() ? " - Enabled" : " - Disabled");

            RenderUtils.drawString(
                    this.queryInput.getFont(),
                    (float) (this.x + 54),
                    (float) (this.y + 14),
                    autoComplete,
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.25F)
            );
        }

        super.draw(partialTicks);
    }

    public List<Module> getMatchingModules() {
        List<Module> matches = new ArrayList<>();

        if (this.queryText == null || this.queryText.isEmpty()) {
            return matches;
        }

        for (Module module : Client.INSTANCE.moduleManager.modules) {
            if (this.matchesQueryPrefix(this.queryText, module.getName())) {
                matches.add(module);
            }
        }

        return matches;
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            List<Module> matches = this.getMatchingModules();
            if (!matches.isEmpty()) {
                matches.get(0).toggle();
            }

            MinecraftClient.getInstance().openScreen(null);
        }
    }

    private boolean matchesQueryPrefix(String query, String candidate) {
        return query == null || query.isEmpty()
                || candidate == null
                || candidate.toLowerCase().startsWith(query.toLowerCase());
    }
}
