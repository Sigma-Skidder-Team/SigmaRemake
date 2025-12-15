package io.github.sst.remake.gui.element.impl.changelog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.impl.Text;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.image.ResourceRegistry;
import org.lwjgl.opengl.GL11;

public class Change extends CustomGuiScreen {
    public AnimationUtils animation2 = new AnimationUtils(370, 150, AnimationUtils.Direction.BACKWARDS);

    public Change(CustomGuiScreen var1, String var2, JsonObject var3) throws JsonParseException {
        super(var1, var2);
        this.setWidth(this.getParent().getWidth());
        int height = 0;
        if (var3.has("deprecated")) {
            GL11.glTexEnvi(8960, 8704, 260);
        }

        String var7 = var3.get("title").getAsString();
        JsonArray var8 = var3.getAsJsonArray("changes");
        this.addToList(new Text(this, "title", 0, height, 0, 0, ColorHelper.field27961, var7, ResourceRegistry.JelloMediumFont40));
        height += 55;

        for (int var9 = 0; var9 < var8.size(); var9++) {
            String var10 = " - " + var8.get(var9).getAsString();
            this.addToList(
                    new Text(
                            this,
                            "change" + var9,
                            0,
                            height,
                            0,
                            0,
                            new ColorHelper(0, 0, 0, ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.8F)),
                            var10,
                            ResourceRegistry.JelloLightFont20
                    )
            );
            height += 22;
        }

        height += 75;
        this.setHeight(height);
    }

    @Override
    public void draw(float partialTicks) {
        float anim = VecUtils.interpolate(this.animation2.calcPercent(), 0.17, 1.0, 0.51, 1.0);
        this.drawBackground((int) ((1.0F - anim) * 100.0F));
        this.method13225();
        partialTicks *= this.animation2.calcPercent();
        super.draw(partialTicks);
    }
}
