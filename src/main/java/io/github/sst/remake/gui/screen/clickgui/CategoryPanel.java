package io.github.sst.remake.gui.screen.clickgui;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
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

public class CategoryPanel extends Widget {
    public final Category category;
    public ModuleList moduleList;
    public float expandProgress;
    public int baseX;
    public int baseY;
    private int panelCornerRadius;
    private final List<ModuleClickListener> moduleClickListeners = new ArrayList<>();

    public CategoryPanel(GuiComponent var1, int var3, int var4, Category category) {
        super(var1, category.toString(), var3, var4, 200, 350, true);
        this.setWidth(200);
        this.setHeight(350);
        this.enableImmediateDrag = true;
        this.category = category;
        this.initModuleList();
    }

    public void rebuildModuleList() {
        this.addRunnable(() -> {
            this.removeChildren(this.moduleList);
            this.addToList(this.moduleList = new ModuleList(this, "modListView", 0, 60, this.getWidth(), this.getHeight() - 60, this.category));
        });
    }

    private void initModuleList() {
        this.addToList(this.moduleList = new ModuleList(this, "modListView", 0, 60, this.getWidth(), this.getHeight() - 60, this.category));
        this.moduleList.addWidthSetter(new ModuleListResizer());
        this.moduleList.addWidthSetter((var0, var1) -> {
            var0.setY(60);
            var0.setHeight(var1.getHeight() - 60);
        });
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (!(this.expandProgress >= 1.0F)) {
            this.setDraggable(false);
            this.isMouseDownOverComponent = false;
        } else {
            this.baseX = this.getX();
            this.baseY = this.getY();
            this.setDraggable(true);
        }

        float var5 = 200.0F;
        float var6 = 320.0F;
        float var7 = 0.7F;
        float var8 = 0.1F;
        int var9 = (int) (200.0F + 140.0F * (1.0F - this.expandProgress));
        int var10 = (int) (320.0F + 320.0F * 0.1F * (1.0F - this.expandProgress));
        int var11 = this.baseY;
        int var12 = (int) ((float) this.baseX - ((float) var9 - 200.0F) / 2.0F + 0.5F);
        if (this.expandProgress < 1.0F) {
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
        int var4 = (int) (1.0F + 10.0F * (1.0F - this.expandProgress));
        RenderUtils.drawRoundedRect(
                (float) (this.getX() + (var4 - 1)),
                (float) (this.getY() + (var4 - 1)),
                (float) (this.getWidth() - (var4 - 1) * 2),
                (float) (this.getHeight() - (var4 - 1) * 2),
                (float) this.panelCornerRadius + (1.0F - this.expandProgress) * (float) var4,
                partialTicks
        );
        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) (this.getX() + this.getWidth()),
                (float) (this.getY() + 60),
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), Math.min(1.0F, partialTicks * 0.9F * this.expandProgress))
        );
        RenderUtils.drawRoundedRect2(
                (float) this.getX(),
                (float) this.getY() + 60.0F * this.expandProgress,
                (float) this.getWidth(),
                (float) this.getHeight() - 60.0F * this.expandProgress,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        if (!(this.expandProgress > 0.8F)) {
            if (this.expandProgress < 0.2F) {
                this.panelCornerRadius = 30;
            }
        } else {
            this.panelCornerRadius = 20;
        }

        String categoryName = this.getCategory().toString();
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_25,
                (float) (this.getX() + 20),
                (float) (this.getY() + 30),
                categoryName,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), partialTicks * 0.5F * this.expandProgress),
                FontAlignment.LEFT,
                FontAlignment.CENTER
        );
        GL11.glPushMatrix();
        super.draw(partialTicks * partialTicks);
        GL11.glPopMatrix();
        if (this.moduleList.getScrollOffset() > 0) {
            RenderUtils.drawImage(
                    (float) this.getX(),
                    (float) (this.getY() + 60),
                    (float) this.getWidth(),
                    18.0F,
                    Resources.SHADOW_BOTTOM,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks * this.expandProgress * 0.5F)
            );
        }
    }

    public Category getCategory() {
        return this.category;
    }

    public void addModuleClickListener(ModuleClickListener listener) {
        this.moduleClickListeners.add(listener);
    }

    public void notifyModuleClickListeners(Module module) {
        for (ModuleClickListener listener : this.moduleClickListeners) {
            listener.onModuleSelected(module);
        }
    }

    public interface ModuleClickListener {
       void onModuleSelected(Module module);
    }
}
