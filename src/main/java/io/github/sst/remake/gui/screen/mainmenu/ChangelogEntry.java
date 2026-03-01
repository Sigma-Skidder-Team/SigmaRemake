package io.github.sst.remake.gui.screen.mainmenu;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.Text;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import org.lwjgl.opengl.GL11;

public class ChangelogEntry extends GuiComponent {

    public AnimationUtils entryAnimation = new AnimationUtils(370, 150, AnimationUtils.Direction.FORWARDS);

    public ChangelogEntry(GuiComponent parent, String id, JsonObject entryJson) throws JsonParseException {
        super(parent, id);

        this.setWidth(this.getParent().getWidth());

        int y = 0;

        String titleText = entryJson.get("title").getAsString();
        JsonArray changeLines = entryJson.getAsJsonArray("changes");

        this.addToList(new Text(
                this,
                "title",
                0,
                y,
                0,
                0,
                ColorHelper.DEFAULT_COLOR,
                titleText,
                FontUtils.HELVETICA_MEDIUM_40
        ));
        y += 55;

        for (int i = 0; i < changeLines.size(); i++) {
            String changeText = " - " + changeLines.get(i).getAsString();
            this.addToList(new Text(
                    this,
                    "change" + i,
                    0,
                    y,
                    0,
                    0,
                    new ColorHelper(0, 0, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F)),
                    changeText,
                    FontUtils.HELVETICA_LIGHT_20
            ));
            y += 22;
        }

        y += 75;
        this.setHeight(y);
    }

    @Override
    public void draw(float partialTicks) {
        float percent = this.entryAnimation.calcPercent();

        float eased = VecUtils.interpolate(percent, 0.17, 1.0, 0.51, 1.0);
        this.setTranslateY((int) ((1.0F - eased) * 100.0F));

        this.applyTranslationTransforms();

        super.draw(partialTicks * percent);
    }
}