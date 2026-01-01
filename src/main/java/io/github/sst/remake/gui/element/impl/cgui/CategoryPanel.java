package io.github.sst.remake.gui.element.impl.cgui;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.panel.AnimatedIconPanel;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel extends AnimatedIconPanel {
    public final Category category;
    public ModListView modListView;
    public float field21195;
    public int field21197;
    public int field21198;
    private int field21199;
    private final List<Class9476> field21200 = new ArrayList<>();

    public CategoryPanel(CustomGuiScreen var1, int var3, int var4, Category category) {
        super(var1, category.toString(), var3, var4, 200, 350, true);
        this.setWidth(200);
        this.setHeight(350);
        this.field20886 = true;
        this.category = category;
        this.method13505();
    }

    public void method13504() {
        this.addRunnable(() -> {
            this.removeChildren(this.modListView);
            this.addToList(this.modListView = new ModListView(this, "modListView", 0, 60, this.getWidth(), this.getHeight() - 60, this.category));
        });
    }

    private void method13505() {
        this.addToList(this.modListView = new ModListView(this, "modListView", 0, 60, this.getWidth(), this.getHeight() - 60, this.category));
        this.modListView.addWidthSetter(new ModListViewSize());
        this.modListView.addWidthSetter((var0, var1) -> {
            var0.setY(60);
            var0.setHeight(var1.getHeight() - 60);
        });
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (!(this.field21195 >= 1.0F)) {
            this.setDraggable(false);
            this.isMouseDownOverComponent = false;
        } else {
            this.field21197 = this.getX();
            this.field21198 = this.getY();
            this.setDraggable(true);
        }

        float var5 = 200.0F;
        float var6 = 320.0F;
        float var7 = 0.7F;
        float var8 = 0.1F;
        int var9 = (int) (200.0F + 140.0F * (1.0F - this.field21195));
        int var10 = (int) (320.0F + 320.0F * 0.1F * (1.0F - this.field21195));
        int var11 = this.field21198;
        int var12 = (int) ((float) this.field21197 - ((float) var9 - 200.0F) / 2.0F + 0.5F);
        if (this.field21195 < 1.0F) {
            if (var12 < 0) {
                var12 = 0;
            }

            if (var12 + var9 > this.parent.getWidth()) {
                var12 = this.parent.getWidth() - var9;
            }

            if (var11 + var10 > this.parent.getHeight()) {
                var11 = this.parent.getHeight() - var10;
            }
        }

        this.setWidth(var9);
        this.setHeight(var10);
        this.setX(var12);
        this.setY(var11);
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        super.applyScaleTransforms();
        super.applyTranslationTransforms();
        int var4 = (int) (1.0F + 10.0F * (1.0F - this.field21195));
        RenderUtils.drawRoundedRect(
                (float) (this.getX() + (var4 - 1)),
                (float) (this.getY() + (var4 - 1)),
                (float) (this.getWidth() - (var4 - 1) * 2),
                (float) (this.getHeight() - (var4 - 1) * 2),
                (float) this.field21199 + (1.0F - this.field21195) * (float) var4,
                partialTicks
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) (this.getX() + this.getWidth()),
                (float) (this.getY() + 60),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), Math.min(1.0F, partialTicks * 0.9F * this.field21195))
        );
        RenderUtils.drawRoundedRect2(
                (float) this.getX(),
                (float) this.getY() + 60.0F * this.field21195,
                (float) this.getWidth(),
                (float) this.getHeight() - 60.0F * this.field21195,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        if (!(this.field21195 > 0.8F)) {
            if (this.field21195 < 0.2F) {
                this.field21199 = 30;
            }
        } else {
            this.field21199 = 20;
        }

        String categoryName = this.getCategory().toString();
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25,
                (float) (this.getX() + 20),
                (float) (this.getY() + 30),
                categoryName,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.5F * this.field21195),
                FontAlignment.LEFT,
                FontAlignment.CENTER
        );
        GL11.glPushMatrix();
        super.draw(partialTicks * partialTicks);
        GL11.glPopMatrix();
        if (this.modListView.getScrollOffset() > 0) {
            RenderUtils.drawImage(
                    (float) this.getX(),
                    (float) (this.getY() + 60),
                    (float) this.getWidth(),
                    18.0F,
                    Resources.shadowBottomPNG,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * this.field21195 * 0.5F)
            );
        }
    }

    public Category getCategory() {
        return this.category;
    }

    public final void method13507(Class9476 var1) {
        this.field21200.add(var1);
    }

    public final void method13508(Module var1) {
        for (Class9476 var5 : this.field21200) {
            var5.method36568(var1);
        }
    }

    public interface Class9476 {
       void method36568(Module var1);
    }
}
