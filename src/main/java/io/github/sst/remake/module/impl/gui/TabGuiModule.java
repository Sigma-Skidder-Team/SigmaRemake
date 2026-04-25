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
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.*;
import java.util.List;

@SuppressWarnings("unused")
public class TabGuiModule extends Module {
    private static final int HIGHTLIGHT_FILL = ColorHelper.applyAlpha(ClientColors.DEEP_TEAL, 0.0625f);
    private static final int HIGHTLIGHT_SHADOW_TINT = ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE, 0.3f);
    private static final int HQ_BLUR_BACKGROUND = ColorHelper.applyAlpha(ClientColors.MID_GREY, 0.05f);

    private static final int ROW_HEIGHT = 30;
    private static final int ROW_PADDING = 4;
    private static final int MODULE_WIDTH = 170;
    private static final int CATEGORY_WIDTH = 150;
    private static final int INITIAL_X = 10;

    private final Map<Category, Float> categoryTextOffset = new HashMap<>();
    private final Map<Module, Float> moduleTextOffset = new HashMap<>();

    private boolean submenuOpen;

    private final List<TabGuiSelectionEffect> effects = new ArrayList<>();

    private final List<Category> categories = Arrays.asList(
            Category.MOVEMENT,
            Category.PLAYER,
            Category.COMBAT,
            Category.ITEM,
            Category.RENDER,
            Category.WORLD,
            Category.MISC
    );

    private final Color[] catGradientTop = new Color[3];
    private final Color[] catGradientBottom = new Color[3];
    private final Color[] modGradientTop = new Color[3];
    private final Color[] modGradientBottom = new Color[3];
    private final Color[] modGradientMid = new Color[3];

    private int y = 90;
    private int categoryHeight = 150;

    private int selectedCategoryIndex = 0;
    private int categorySelectorY = 0;
    private int moduleSelectorY = 0;

    private float animSpeed = 1.0F;

    private Category selectedCategory;
    private int modulePanelHeight = 0;

    private int selectedModuleIndex;
    private Module selectedModule;

    private float categoryScrollOffset = 0.0F;

    public TabGuiModule() {
        super("TabGui", "Interact with modules via the keyboard.", Category.GUI);
    }

    @Subscribe
    private void onTick3D(Render3DEvent event) {
        if (client.player == null) return;

        this.updateSampledPanelColors();
        this.animSpeed = (float) Math.max(Math.round(6.0F - (float) MinecraftClient.currentFps / 10.0F), 1);
    }

    @Subscribe
    public void onRender(RenderLevelEvent event) {
        if (client.player == null) return;
        if (client.options.debugEnabled || client.options.hudHidden) return;
        if (!Client.INSTANCE.configManager.hqBlur) return;

        HUDManager.registerBlurArea(INITIAL_X, y, CATEGORY_WIDTH, categoryHeight);
        if (submenuOpen && selectedCategory != null) {
            int selectedModulesHeight = getModulesForCategory(selectedCategory).size() * ROW_HEIGHT + ROW_PADDING;
            if (selectedModulesHeight > ROW_PADDING) {
                HUDManager.registerBlurArea(MODULE_WIDTH, y, MODULE_WIDTH, selectedModulesHeight);
            }
        }
    }

    @Subscribe
    public void onKeyPress(KeyPressEvent event) {
        switch (event.key) {
            case GLFW.GLFW_KEY_ENTER: // Enter
                if (this.submenuOpen && this.selectedModule != null) {
                    this.selectedModule.toggle();
                    this.effects.add(new TabGuiSelectionEffect(this.submenuOpen));
                }
                break;

            case GLFW.GLFW_KEY_RIGHT: // Right
                this.effects.add(new TabGuiSelectionEffect(this.submenuOpen));
                if (this.submenuOpen) {
                    if (this.selectedModule != null) {
                        this.selectedModule.toggle();
                    }
                } else if (this.selectedCategory != null && !this.getModulesForCategory(this.selectedCategory).isEmpty()) {
                    this.submenuOpen = true;
                }
                break;

            case GLFW.GLFW_KEY_LEFT: // Left
                this.submenuOpen = false;
                break;

            case GLFW.GLFW_KEY_DOWN: // Down
                if (!this.submenuOpen) {
                    this.selectedCategoryIndex++;
                    this.selectedModuleIndex = 0;
                } else {
                    this.selectedModuleIndex++;
                }
                break;

            case GLFW.GLFW_KEY_UP: // Up
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
            this.categorySelectorY = -ROW_HEIGHT;
        } else if (this.selectedCategoryIndex < 0) {
            this.selectedCategoryIndex = this.categories.size() - 1;
            this.categorySelectorY = this.selectedCategoryIndex * ROW_HEIGHT + ROW_HEIGHT;
        }

        // Clamp module selection to current category.
        List<Module> modules = this.selectedCategory == null
                ? Collections.emptyList()
                : this.getModulesForCategory(this.selectedCategory);
        if (this.selectedModuleIndex >= modules.size()) {
            this.selectedModuleIndex = Math.max(modules.size() - 1, 0);
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

        y = event.offset;
        this.selectedCategory = this.categories.get(this.selectedCategoryIndex);

        drawPanelBackground(
                INITIAL_X, y, CATEGORY_WIDTH, categoryHeight,
                catGradientTop, null, catGradientBottom
        );

        ScissorUtils.startScissorRect((float) INITIAL_X, (float) y, (float) CATEGORY_WIDTH, (float) categoryHeight);

        drawSelector(
                INITIAL_X,
                y - Math.round(categoryScrollOffset),
                categories.size() * ROW_HEIGHT + ROW_PADDING,
                CATEGORY_WIDTH,
                selectedCategoryIndex,
                false
        );

        drawCategories(y - Math.round(categoryScrollOffset), categories);

        ScissorUtils.restoreScissor();

        if (submenuOpen) {
            List<Module> modules = getModulesForCategory(selectedCategory);
            if (modules.isEmpty()) {
                submenuOpen = false;
                modulePanelHeight = 0;
                selectedModule = null;
            } else {
                modulePanelHeight = modules.size() * ROW_HEIGHT + ROW_PADDING;

                drawPanelBackground(
                        MODULE_WIDTH, y, MODULE_WIDTH, modulePanelHeight,
                        modGradientTop, modGradientMid, modGradientBottom
                );

                drawSelector(
                        MODULE_WIDTH, y,
                        modulePanelHeight,
                        MODULE_WIDTH,
                        selectedModuleIndex,
                        true
                );

                drawModules(y, modules);
            }
        }

        event.increment(categoryHeight + 10);
    }

    private void drawCategories(int y, List<Category> categories) {
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
                    (float) (TabGuiModule.INITIAL_X + 11) + categoryTextOffset.get(category),
                    (float) (y + ROW_HEIGHT / 2 - FontUtils.HELVETICA_LIGHT_20.getHeight() / 2 + 2 + i * ROW_HEIGHT),
                    category.toString(),
                    -1
            );

            i++;
        }
    }

    private void drawModules(int y, List<Module> modules) {
        if (modules.isEmpty()) {
            selectedModule = null;
            return;
        }

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

            if (module.enabled) {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_MEDIUM_20,
                        (float) (TabGuiModule.MODULE_WIDTH + 11) + moduleTextOffset.get(module),
                        (float) (y + ROW_HEIGHT / 2 - FontUtils.HELVETICA_MEDIUM_20.getHeight() / 2 + 3 + i * ROW_HEIGHT),
                        module.name,
                        ClientColors.LIGHT_GREYISH_BLUE
                );
            } else {
                RenderUtils.drawString(
                        FontUtils.HELVETICA_LIGHT_20,
                        (float) (TabGuiModule.MODULE_WIDTH + 11) + moduleTextOffset.get(module),
                        (float) (y + ROW_HEIGHT / 2 - FontUtils.HELVETICA_LIGHT_20.getHeight() / 2 + 2 + i * ROW_HEIGHT),
                        module.name,
                        ClientColors.LIGHT_GREYISH_BLUE
                );
            }

            i++;
        }
    }

    private void drawSelector(int x, int y, int contentHeight, int width, int selectedIndex, boolean submenu) {
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

        float selectorY = selectorOffsetY >= 0 ? (float) (selectorOffsetY + y) : (float) y;
        float selectorHeight = selectorOffsetY + ROW_PADDING + ROW_HEIGHT <= contentHeight
                ? (float) (ROW_HEIGHT + ROW_PADDING)
                : (float) (contentHeight + ROW_PADDING - selectorOffsetY);

        RenderUtils.drawRect((float) x, selectorY, (float) width, Math.max(selectorHeight, 0.0F), HIGHTLIGHT_FILL);

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

    private void drawPanelBackground(int x, int y, int width, int height, Color[] topColors, Color[] midColorsOrNull, Color[] bottomColors) {
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
            ScissorUtils.startScissorRect((float) x, (float) y, (float) width, (float) height);
            HUDManager.renderFinalBlur();
            ScissorUtils.restoreScissor();

            RenderUtils.drawRect((float) x, (float) y, (float) width, (float) height, HQ_BLUR_BACKGROUND);
        }

        RenderUtils.drawRoundedRect((float) x, (float) y, (float) width, (float) height, 8.0F, 0.7f);
    }

    private void updateSampledPanelColors() {
        if (Client.INSTANCE.configManager.hqBlur) return;
        if (client.options.debugEnabled || client.options.hudHidden) return;

        for (int i = 0; i < 3; i++) {
            catGradientTop[i] = sampleScreenColor(INITIAL_X + CATEGORY_WIDTH / 3 * i, y, catGradientTop[i]);
            catGradientBottom[i] = sampleScreenColor(INITIAL_X + CATEGORY_WIDTH / 3 * i, y + categoryHeight, catGradientBottom[i]);

            modGradientTop[i] = sampleScreenColor(INITIAL_X + CATEGORY_WIDTH + 56 * i, y, modGradientTop[i]);
            modGradientBottom[i] = sampleScreenColor(INITIAL_X + CATEGORY_WIDTH + 56 * i, y + modulePanelHeight, modGradientBottom[i]);
            modGradientMid[i] = sampleScreenColor(INITIAL_X + CATEGORY_WIDTH + 56 * i, y + modulePanelHeight / 2, modGradientMid[i]);
        }
    }

    private Color sampleScreenColor(int x, int y, Color previous) {
        Color sampled = ColorHelper.sampleScreenColor(x, y);
        if (previous != null) {
            sampled = ColorHelper.blendColor(sampled, previous, 0.08f * animSpeed);
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