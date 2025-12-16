package io.github.sst.remake.gui.element.impl.keyboard;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.gui.element.impl.TextButton;
import io.github.sst.remake.gui.impl.JelloKeyboard;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.EasingFunctions;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.ResourceRegistry;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PopOver extends Element {
    private final int field21376;
    private final AnimationUtils field21377;
    private boolean field21378 = false;
    private final List<Class6601> field21379 = new ArrayList<Class6601>();

    public PopOver(CustomGuiScreen var1, String var2, int var3, int var4, int var5, String var6) {
        super(var1, var2, var3 - 125, var4, 250, 330, ColorHelper.DEFAULT_COLOR, var6, false);
        if (this.y + this.height <= MinecraftClient.getInstance().getWindow().getHeight()) {
            this.y += 10;
        } else {
            this.y -= 400;
            this.field21378 = true;
        }

        this.field21376 = var5;
        this.field21377 = new AnimationUtils(250, 0);
        this.setReAddChildren(true);
        this.setListening(false);
        this.method13712();
        TextButton var9;
        this.addToList(
                var9 = new TextButton(
                        this,
                        "addButton",
                        this.width - 70,
                        this.height - 70,
                        ResourceRegistry.JelloLightFont25.getWidth("Add"),
                        70,
                        ColorHelper.DEFAULT_COLOR,
                        "Add",
                        ResourceRegistry.JelloLightFont25
                )
        );
        var9.onClick((var1x, var2x) -> this.method13714());
    }

    public void method13712() {
        int var3 = 1;
        List<String> children = new ArrayList<>();

        for (CustomGuiScreen child : this.getChildren()) {
            if (child.getHeight() != 0) {
                children.add(child.getName());
            }
        }

        this.method13242();
        this.setFocused(true);
        this.clearChildren();

        for (Class6984 var10 : JelloKeyboard.method13328()) {
            int var7 = var10.getBind();
            if (var7 == this.field21376) {
                Class4253 var8;
                this.addToList(var8 = new Class4253(this, var10.method21596(), 0, 20 + 55 * var3, this.width, 55, var10, var3++));
                var8.onPress(var2 -> {
                    var10.setBind(0);
                    this.callUIHandlers();
                });
                if (!children.isEmpty() && !children.contains(var10.method21596())) {
                    var8.method13056();
                }
            }
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        Map<Integer, Class4253> var5 = new HashMap<>();

        for (CustomGuiScreen child : this.getChildren()) {
            if (child instanceof Class4253) {
                var5.put(((Class4253) child).field20626, (Class4253) child);
            }
        }

        int var9 = 75;

        for (Entry<Integer, Class4253> var11 : var5.entrySet()) {
            var11.getValue().setY(var9);
            var9 += var11.getValue().getHeight();
        }

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        partialTicks = this.field21377.calcPercent();
        float var4 = EasingFunctions.easeOutBack(partialTicks, 0.0F, 1.0F, 1.0F);
        this.method13279(0.8F + var4 * 0.2F, 0.8F + var4 * 0.2F);
        this.method13284((int) ((float) this.width * 0.2F * (1.0F - var4)) * (!this.field21378 ? 1 : -1));
        super.method13224();
        int var6 = ColorHelper.applyAlpha(-723724, QuadraticEasing.easeOutQuad(partialTicks, 0.0F, 1.0F, 1.0F));
        RenderUtils.drawRoundedRect(
                (float) (this.x + 10 / 2),
                (float) (this.y + 10 / 2),
                (float) (this.width - 10),
                (float) (this.height - 10),
                35.0F,
                partialTicks
        );
        RenderUtils.drawRoundedRect(
                (float) (this.x + 10 / 2),
                (float) (this.y + 10 / 2),
                (float) (this.x - 10 / 2 + this.width),
                (float) (this.y - 10 / 2 + this.height),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.25F)
        );
        RenderUtils.drawRoundedRect((float) this.x, (float) this.y, (float) this.width, (float) this.height, (float) 10, var6);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) this.x, (float) this.y, 0.0F);
        GL11.glRotatef(!this.field21378 ? -90.0F : 90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-this.x), (float) (-this.y), 0.0F);
        RenderUtils.drawImage(
                (float) (this.x + (!this.field21378 ? 0 : this.height)),
                (float) this.y + (float) ((this.width - 47) / 2) * (!this.field21378 ? 1.0F : -1.5F),
                18.0F,
                47.0F,
                Resources.selectPNG,
                var6
        );
        GL11.glPopMatrix();
        RenderUtils.drawString(
                ResourceRegistry.JelloLightFont25,
                (float) (this.x + 25),
                (float) (this.y + 20),
                this.text + " Key",
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F * partialTicks)
        );
        RenderUtils.drawRoundedRect(
                (float) (this.x + 25),
                (float) (this.y + 68),
                (float) (this.x + this.width - 25),
                (float) (this.y + 69),
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.05F * partialTicks)
        );
        super.draw(partialTicks);
    }

    public final void method13713(Class6601 var1) {
        this.field21379.add(var1);
    }

    public final void method13714() {
        for (Class6601 var4 : this.field21379) {
            var4.method20001(this);
        }
    }

    public interface Class6601 {
        void method20001(PopOver var1);
    }
}
