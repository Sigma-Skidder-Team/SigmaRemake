package io.github.sst.remake.gui.screen.clickgui;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Widget;
import io.github.sst.remake.gui.framework.layout.FullWidthResizer;
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
    public CategoryModuleList moduleList;
    public float expandProgress;
    public int expandedBaseX;
    public int expandedBaseY;
    private int cornerRadius;
    private final List<ModuleClickListener> moduleClickListeners = new ArrayList<>();

    public CategoryPanel(GuiComponent parent, int x, int y, Category category) {
        super(parent, category.toString(), x, y, 200, 350, true);

        this.setWidth(200);
        this.setHeight(350);

        this.enableImmediateDrag = true;

        this.category = category;

        this.initModuleList();
    }

    public void rebuildModuleList() {
        this.addRunnable(() -> {
            this.removeChildren(this.moduleList);
            this.addToList(this.moduleList = new CategoryModuleList(this, "modListView", 0, 60, this.getWidth(), this.getHeight() - 60, this.category));
        });
    }

    private void initModuleList() {
        this.moduleList = new CategoryModuleList(this, "modListView", 0, 60, this.getWidth(), this.getHeight() - 60, this.category);
        this.addToList(this.moduleList);

        this.moduleList.addWidthSetter(new FullWidthResizer());
        this.moduleList.addWidthSetter((list, panel) -> {
            list.setY(60);
            list.setHeight(panel.getHeight() - 60);
        });
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.expandProgress < 1.0F) {
            this.setDraggable(false);
            this.isMouseDownOverComponent = false;
        } else {
            this.expandedBaseX = this.getX();
            this.expandedBaseY = this.getY();
            this.setDraggable(true);
        }

        int targetWidth = (int) (200.0F + 140.0F * (1.0F - this.expandProgress));
        int targetHeight = (int) (320.0F + 320.0F * 0.1F * (1.0F - this.expandProgress));

        int newY = this.expandedBaseY;
        int newX = (int) ((float) this.expandedBaseX - ((float) targetWidth - 200.0F) / 2.0F + 0.5F);

        if (this.expandProgress < 1.0F) {
            if (newX < 0) {
                newX = 0;
            }
            if (newX + targetWidth > this.parent.getWidth()) {
                newX = this.parent.getWidth() - targetWidth;
            }
            if (newY + targetHeight > this.parent.getHeight()) {
                newY = this.parent.getHeight() - targetHeight;
            }
        }

        this.setWidth(targetWidth);
        this.setHeight(targetHeight);
        this.setX(newX);
        this.setY(newY);

        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        super.applyScaleTransforms();
        super.applyTranslationTransforms();

        int inset = (int) (1.0F + 10.0F * (1.0F - this.expandProgress));

        RenderUtils.drawRoundedRect(
                (float) (this.getX() + (inset - 1)),
                (float) (this.getY() + (inset - 1)),
                (float) (this.getWidth() - (inset - 1) * 2),
                (float) (this.getHeight() - (inset - 1) * 2),
                (float) this.cornerRadius + (1.0F - this.expandProgress) * (float) inset,
                partialTicks
        );

        RenderUtils.drawRoundedRect(
                (float) this.getX(),
                (float) this.getY(),
                (float) (this.getX() + this.getWidth()),
                (float) (this.getY() + 60),
                ColorHelper.applyAlpha(
                        ClientColors.LIGHT_GREYISH_BLUE.getColor(),
                        Math.min(1.0F, partialTicks * 0.9F * this.expandProgress)
                )
        );

        RenderUtils.drawRoundedRect2(
                (float) this.getX(),
                (float) this.getY() + 60.0F * this.expandProgress,
                (float) this.getWidth(),
                (float) this.getHeight() - 60.0F * this.expandProgress,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );

        updateCornerRadius();

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

    private void updateCornerRadius() {
        if (this.expandProgress > 0.8F) {
            this.cornerRadius = 20;
            return;
        }
        if (this.expandProgress < 0.2F) {
            this.cornerRadius = 30;
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
