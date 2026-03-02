package io.github.sst.remake.module.impl.gui;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.KeyPressEvent;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.render.Render3DEvent;
import io.github.sst.remake.event.impl.game.render.RenderLevelEvent;
import io.github.sst.remake.manager.impl.HUDManager;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.MinecraftClient;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TabGuiModule extends Module {

    private final static int HIGHTLIGHT_FILL = ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.0625f);
    private final static int HIGHTLIGHT_SHADOW_TINT = ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.3f);
    private final static int HQ_BLUR_BACKGROUND = ColorHelper.applyAlpha(ClientColors.MID_GREY.getColor(), 0.05f);

    private final static int ROW_HEIGHT = 30;
    private final static int ROW_PADDING = 4;

    private final Map<Category, Float> categoryTextOffset = new HashMap<>();
    private final Map<Module, Float> moduleTextOffset = new HashMap<>();

    private boolean submenuOpen;

    private final List<TabGuiSelectionEffect> effects = new ArrayList<>();

    private final List<Category> categories = Arrays.asList(Category.values());

    private final Color[] catGradientTop = new Color[3];
    private final Color[] catGradientBottom = new Color[3];
    private final Color[] modGradientTop = new Color[3];
    private final Color[] modGradientBottom = new Color[3];
    private final Color[] modGradientMid = new Color[3];

    private int x = 10;
    private int y = 90;
    private int categoryWidth = 150;
    private int categoryHeight = 150;

    private int selectedCategoryIndex = 0;
    private int categorySelectorY = 0;
    private int moduleSelectorY = 0;

    private float animSpeed = 1.0F;

    private Category selectedCategory;
    private int modulePanelHeight = 0;

    private int selectedModuleIndex = 0;
    private Module selectedModule;

    private final int moduleWidth = 170;

    private float categoryScrollOffset = 0.0F;

    public TabGuiModule() {
        super("TabGui", "Interact with modules via the keyboard", Category.GUI);
    }

    @Subscribe
    public void onRender(RenderLevelEvent event) {
        if (client.player == null) return;
        if (client.options.debugEnabled || client.options.hudHidden) return;
        if (!Client.INSTANCE.configManager.hqBlur) return;

        HUDManager.registerBlurArea(x, y, categoryWidth, categoryHeight);
        if (submenuOpen) {
            HUDManager.registerBlurArea(moduleWidth, y, moduleWidth, modulePanelHeight);
        }
    }

    @Subscribe
    private void onTick3D(Render3DEvent event) {
        if (client.player == null) return;

        this.updateSampledPanelColors();
        this.animSpeed = (float) Math.max(Math.round(6.0F - (float) MinecraftClient.currentFps / 10.0F), 1);
    }

    @Subscribe
    public void onKeyPress(KeyPressEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        switch (event.key) {
            case 257: // Enter
                if (this.submenuOpen) {
                    this.selectedModule.toggle();
                    this.effects.add(new TabGuiSelectionEffect(this.submenuOpen));
                }
                break;

            case 262: // Right
                this.effects.add(new TabGuiSelectionEffect(this.submenuOpen));
                if (this.submenuOpen) {
                    this.selectedModule.toggle();
                }
                this.submenuOpen = true;
                break;

            case 263: // Left
                this.submenuOpen = false;
                break;

            case 264: // Down
                if (!this.submenuOpen) {
                    this.selectedCategoryIndex++;
                    this.selectedModuleIndex = 0;
                } else {
                    this.selectedModuleIndex++;
                }
                break;

            case 265: // Up
                if (!this.submenuOpen) {
                    this.selectedCategoryIndex--;
                    this.selectedModuleIndex = 0;
                } else {
                    this.selectedModuleIndex--;
                }
                break;

            default:
                return;
        }

        // Wrap category selection.
        if (this.selectedCategoryIndex >= this.categories.size()) {
            this.selectedCategoryIndex = 0;
            this.categorySelectorY = this.selectedCategoryIndex * ROW_HEIGHT - ROW_HEIGHT;
        } else if (this.selectedCategoryIndex < 0) {
            this.selectedCategoryIndex = this.categories.size() - 1;
            this.categorySelectorY = this.selectedCategoryIndex * ROW_HEIGHT + ROW_HEIGHT;
        }

        // Clamp module selection to current category.
        List<Module> modules = this.getModulesForCategory(this.selectedCategory);
        if (this.selectedModuleIndex >= modules.size()) {
            this.selectedModuleIndex = modules.size() - 1;
        } else if (this.selectedModuleIndex < 0) {
            this.selectedModuleIndex = 0;
        }
    }

    @Subscribe
    public void onRenderHud(RenderClient2DEvent event) {
        if (client.player == null || client.world == null) return;
        if (client.options.debugEnabled || client.options.hudHidden) return;

        categoryHeight = 5 * ROW_HEIGHT + ROW_PADDING;

        float targetScroll = (float) getDesiredCategoryScroll();
        float delta = Math.abs(targetScroll - categoryScrollOffset);
        boolean scrollUp = targetScroll - categoryScrollOffset < 0.0F;
        categoryScrollOffset = categoryScrollOffset
                + Math.min(delta, delta * 0.14F * animSpeed) * (scrollUp ? -1.0F : 1.0F);

        y = event.getOffset();

        drawPanelBackground(
                x, y, categoryWidth, categoryHeight,
                catGradientTop, null, catGradientBottom,
                1.0F
        );

        ScissorUtils.startScissor((float) x, (float) y, (float) categoryWidth, (float) categoryHeight);

        drawSelector(
                x,
                y - Math.round(categoryScrollOffset),
                categories.size() * ROW_HEIGHT + ROW_PADDING,
                categoryWidth,
                selectedCategoryIndex,
                false,
                1.0F
        );

        drawCategories(x, y - Math.round(categoryScrollOffset), categories);

        ScissorUtils.restoreScissor();

        if (submenuOpen) {
            List<Module> modules = getModulesForCategory(selectedCategory);
            modulePanelHeight = modules.size() * ROW_HEIGHT + ROW_PADDING;

            drawPanelBackground(
                    moduleWidth, y, moduleWidth, modulePanelHeight,
                    modGradientTop, modGradientMid, modGradientBottom,
                    1.0F
            );

            drawSelector(
                    moduleWidth, y,
                    modulePanelHeight,
                    moduleWidth,
                    selectedModuleIndex,
                    true,
                    1.0F
            );

            drawModules(moduleWidth, y, modules);
        }

        event.increment(categoryHeight + 10);
    }

    private void drawCategories(int x, int y, List<Category> categories) {
        int i = 0;
        for (Category category : categories) {
            if (selectedCategoryIndex == i) {
                selectedCategory = category;
            }

            if (!categoryTextOffset.containsKey(category)) {
                categoryTextOffset.put(category, 0.0F);
            }

            // Slide text slightly to the right when selected.
            if (selectedCategoryIndex == i && categoryTextOffset.get(category) < 14.0F) {
                categoryTextOffset.put(category, categoryTextOffset.get(category) + animSpeed);
            } else if (selectedCategoryIndex != i && categoryTextOffset.get(category) > 0.0F) {
                categoryTextOffset.put(category, categoryTextOffset.get(category) - animSpeed);
            }

            RenderUtils.drawString(
                    FontUtils.HELVETICA_LIGHT_20,
                    (float) (x + 11) + categoryTextOffset.get(category),
                    (float) (y + ROW_HEIGHT / 2 - FontUtils.HELVETICA_LIGHT_20.getHeight() / 2 + 2 + i * ROW_HEIGHT),
                    category.toString(),
                    -1
            );

            i++;
        }
    }

    private void drawModules(int x, int y, List<Module> modules) {
        int i = 0;
        for (Module module : modules) {
            if (selectedModuleIndex == i) {
                selectedModule = module;
            }

            if (!moduleTextOffset.containsKey(module)) {
                moduleTextOffset.put(module, 0.0F);
            }

            if (selectedModuleIndex == i && moduleTextOffset.get(module) < 14.0F) {
                moduleTextOffset.put(module, moduleTextOffset.get(module) + animSpeed);
            } else if (selectedModuleIndex != i && moduleTextOffset.get(module) > 0.0F) {
                moduleTextOffset.put(module, moduleTextOffset.get(module) - animSpeed);
            }

            if (module.isEnabled()) {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_MEDIUM_20,
                        (float) (x + 11) + moduleTextOffset.get(module),
                        (float) (y + ROW_HEIGHT / 2 - FontUtils.HELVETICA_MEDIUM_20.getHeight() / 2 + 3 + i * ROW_HEIGHT),
                        module.getName(),
                        ClientColors.LIGHT_GREYISH_BLUE.getColor()
                );
            } else {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_LIGHT_20,
                        (float) (x + 11) + moduleTextOffset.get(module),
                        (float) (y + ROW_HEIGHT / 2 - FontUtils.HELVETICA_LIGHT_20.getHeight() / 2 + 2 + i * ROW_HEIGHT),
                        module.getName(),
                        ClientColors.LIGHT_GREYISH_BLUE.getColor()
                );
            }

            i++;
        }
    }

    private void drawSelector(int x, int y, int contentHeight, int width, int selectedIndex, boolean submenu, float alpha) {
        int selectorOffsetY;

        if (submenu) {
            float delta = (float) (selectedIndex * ROW_HEIGHT - moduleSelectorY);

            if (moduleSelectorY > selectedIndex * ROW_HEIGHT) {
                moduleSelectorY = (int) ((float) moduleSelectorY
                        + (delta * 0.14F * animSpeed >= 1.0F ? -animSpeed : delta * 0.14F * animSpeed));
            }

            if (moduleSelectorY < selectedIndex * ROW_HEIGHT) {
                moduleSelectorY = (int) ((float) moduleSelectorY
                        + (delta * 0.14F * animSpeed <= 1.0F ? animSpeed : delta * 0.14F * animSpeed));
            }

            if (delta > 0.0F && moduleSelectorY > selectedIndex * ROW_HEIGHT) {
                moduleSelectorY = selectedIndex * ROW_HEIGHT;
            }
            if (delta < 0.0F && moduleSelectorY < selectedIndex * ROW_HEIGHT) {
                moduleSelectorY = selectedIndex * ROW_HEIGHT;
            }

            selectorOffsetY = moduleSelectorY;
        } else {
            float delta = (float) (selectedIndex * ROW_HEIGHT - categorySelectorY);

            if (categorySelectorY > selectedIndex * ROW_HEIGHT) {
                categorySelectorY = (int) ((float) categorySelectorY
                        + (delta * 0.14F * animSpeed >= 1.0F ? -animSpeed : delta * 0.14F * animSpeed));
            }

            if (categorySelectorY < selectedIndex * ROW_HEIGHT) {
                categorySelectorY = (int) ((float) categorySelectorY
                        + (delta * 0.14F * animSpeed <= 1.0F ? animSpeed : delta * 0.14F * animSpeed));
            }

            if (delta > 0.0F && categorySelectorY > selectedIndex * ROW_HEIGHT) {
                categorySelectorY = selectedIndex * ROW_HEIGHT;
            }
            if (delta < 0.0F && categorySelectorY < selectedIndex * ROW_HEIGHT) {
                categorySelectorY = selectedIndex * ROW_HEIGHT;
            }

            selectorOffsetY = categorySelectorY;
        }

        if (Math.round(categoryScrollOffset) > 0 && categorySelectorY > 120) {
            categorySelectorY = Math.max(categorySelectorY, 120 + Math.round(categoryScrollOffset));
        }

        RenderUtils.drawRect(
                (float) x,
                selectorOffsetY >= 0 ? (float) (selectorOffsetY + y) : (float) y,
                (float) (x + width),
                selectorOffsetY + ROW_PADDING + ROW_HEIGHT <= contentHeight
                        ? (float) (selectorOffsetY + y + ROW_HEIGHT + ROW_PADDING)
                        : (float) (y + contentHeight + ROW_PADDING),
                HIGHTLIGHT_FILL
        );

        RenderUtils.drawImage(
                (float) x,
                selectorOffsetY + ROW_PADDING + ROW_HEIGHT <= contentHeight
                        ? (float) (selectorOffsetY + y + ROW_HEIGHT - 10)
                        : (float) (y + contentHeight - 10),
                (float) width,
                14.0F,
                Resources.SHADOW_TOP,
                HIGHTLIGHT_SHADOW_TINT
        );

        RenderUtils.drawImage(
                (float) x,
                selectorOffsetY >= 0 ? (float) (selectorOffsetY + y) : (float) y,
                (float) width,
                14.0F,
                Resources.SHADOW_BOTTOM,
                HIGHTLIGHT_SHADOW_TINT
        );

        ScissorUtils.startScissorNoGL(
                x,
                selectorOffsetY >= 0 ? selectorOffsetY + y : y,
                x + width,
                selectorOffsetY + ROW_PADDING + ROW_HEIGHT <= contentHeight
                        ? selectorOffsetY + y + ROW_HEIGHT + ROW_PADDING
                        : y + contentHeight + ROW_PADDING
        );

        Iterator<TabGuiSelectionEffect> it = effects.iterator();
        while (it.hasNext()) {
            TabGuiSelectionEffect effect = it.next();
            if (effect.submenu == submenu) {
                float t = effect.animation.calcPercent();

                int arcColor = ColorHelper.applyAlpha(-5658199, (1.0F - t * (0.5F + t * 0.5F)) * 0.8F);
                if (Client.INSTANCE.configManager.hqBlur) {
                    arcColor = ColorHelper.applyAlpha(-1, (1.0F - t) * 0.14F);
                }

                RenderUtils.drawFilledArc(
                        (float) x,
                        selectorOffsetY >= 0 ? (float) (selectorOffsetY + y + 14) : (float) y,
                        (float) width * QuadraticEasing.easeOutQuad(t, 0.0F, 1.0F, 1.0F) + 4.0F,
                        arcColor
                );

                if (effect.animation.calcPercent() == 1.0F) {
                    it.remove();
                }
            }
        }

        ScissorUtils.restoreScissor();
    }

    private int getDesiredCategoryScroll() {
        return Math.max(selectedCategoryIndex * ROW_HEIGHT - 4 * ROW_HEIGHT, 0);
    }

    private List<Module> getModulesForCategory(Category category) {
        return Client.INSTANCE.moduleManager.getModulesByCategory(category);
    }

    private void drawPanelBackground(
            int x, int y, int width, int height,
            Color[] topColors, Color[] midColorsOrNull, Color[] bottomColors,
            float alpha
    ) {
        boolean hqBlur = Client.INSTANCE.configManager.hqBlur;

        int top = ColorHelper.averageColors(topColors).getRGB();
        int bottom = ColorHelper.averageColors(bottomColors).getRGB();

        if (midColorsOrNull != null) {
            int mid = ColorHelper.averageColors(midColorsOrNull).getRGB();
            top = ColorHelper.blendColors(top, mid, 0.75F);
            bottom = ColorHelper.blendColors(bottom, mid, 0.75F);
        }

        if (!hqBlur) {
            RenderUtils.drawVerticalGradientRect(x, y, x + width, y + height, top, bottom);
        } else {
            ScissorUtils.startScissor((float) x, (float) y, (float) width, (float) height);
            HUDManager.renderFinalBlur();
            ScissorUtils.restoreScissor();

            RenderUtils.drawRect((float) x, (float) y, (float) (x + width), (float) (y + height), HQ_BLUR_BACKGROUND);
        }

        RenderUtils.drawRoundedRect((float) x, (float) y, (float) width, (float) height, 8.0F, 0.7F * alpha);
    }

    private void updateSampledPanelColors() {
        if (Client.INSTANCE.configManager.hqBlur) {
            return;
        }
        if (client.options.debugEnabled || client.options.hudHidden) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            this.catGradientTop[i] = this.sampleScreenColor(this.x + this.categoryWidth / 3 * i, this.y, this.catGradientTop[i]);
            this.catGradientBottom[i] = this.sampleScreenColor(this.x + this.categoryWidth / 3 * i, this.y + this.categoryHeight, this.catGradientBottom[i]);

            this.modGradientTop[i] = this.sampleScreenColor(this.x + this.categoryWidth + 56 * i, this.y, this.modGradientTop[i]);
            this.modGradientBottom[i] = this.sampleScreenColor(this.x + this.categoryWidth + 56 * i, this.y + this.modulePanelHeight, this.modGradientBottom[i]);
            this.modGradientMid[i] = this.sampleScreenColor(this.x + this.categoryWidth + 56 * i, this.y + this.modulePanelHeight / 2, this.modGradientMid[i]);
        }
    }

    private Color sampleScreenColor(int x, int y, Color previous) {
        Color sampled = ColorHelper.sampleScreenColor(x, y);
        if (previous != null) {
            sampled = ColorHelper.blendColor(sampled, previous, 0.08F * this.animSpeed);
        }
        return sampled;
    }

    public static class TabGuiSelectionEffect {
        public final boolean submenu;
        public final AnimationUtils animation;

        public TabGuiSelectionEffect(boolean submenu) {
            this.animation = new AnimationUtils(250, 0);
            this.submenu = submenu;
        }
    }
}