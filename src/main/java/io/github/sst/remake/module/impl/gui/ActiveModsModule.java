package io.github.sst.remake.module.impl.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.render.RenderScoreboardEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.font.TrueTypeFont;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ActiveModsModule extends Module {
    private final HashMap<Module, AnimationUtils> scaleInAnimations = new HashMap<>();
    private final List<Module> activeModules = new ArrayList<>();

    private final ModeSetting fontSize = new ModeSetting("Size", "The font size", 0, "Normal", "Small", "Tiny");
    private final BooleanSetting animations = new BooleanSetting("Animations", "Scale in animation", true);
    public final BooleanSetting toggleSound = new BooleanSetting("Sound", "Toggle sound", true);

    public ActiveModsModule() {
        super(Category.GUI, "ActiveMods", "Renders active mods");
        fontSize.addListener(sett -> setFontSize());
    }

    private TrueTypeFont font = FontUtils.HELVETICA_LIGHT_20;
    private int totalHeight;

    @Subscribe
    public void onRenderScoreboard(RenderScoreboardEvent event) {
        if (event.post) {
            GlStateManager.translatef(0.0F, (float) (-this.totalHeight), 0.0F);
            return;
        }

        Collection<ScoreboardPlayerScore> scores = getScores();
        int offset = 0;

        for (Module module : this.activeModules) {
            if (module.isEnabled()) {
                offset++;
            }
        }

        int y = 23 + offset * (this.font.getHeight() + 1);
        int totalScores = scores.size();

        int windowHeight = client.getWindow().getHeight();
        int windowCenterY = windowHeight / 2 - (9 + 5) * (totalScores - 3 + 2);

        if (y <= windowCenterY) {
            this.totalHeight = 0;
        } else {
            this.totalHeight = (y - windowCenterY) / 2;
            GlStateManager.translatef(0.0F, (float) this.totalHeight, 0.0F);
        }
    }

    @Subscribe
    public void onRender(RenderClient2DEvent ignoredEvent) {
        if (client.options.hudHidden || client.player == null) return;

        for (Module module : this.scaleInAnimations.keySet()) {
            if (animations.value) {
                this.scaleInAnimations.get(module).changeDirection(!module.isEnabled() ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
            }
        }

        int margin = 10;
        float scale = 1;
        int screenWidth = client.getWindow().getWidth();
        int screenHeight = margin - 4;

        if (fontSize.value.equals("Tiny")) {
            margin -= 3;
        }

        if (client.options.debugEnabled) {
            screenHeight = (int) ((double) (client.inGameHud.debugHud.getRightText().size() * 9) * client.getWindow().getScaleFactor() + 7.0);
        }

        int color = ColorHelper.applyAlpha(-1, 0.95F);

        for (Module module : this.activeModules) {
            float animationScale = 1.0F;
            float transparency = 1.0F;

            if (animations.value) {
                AnimationUtils animation = this.scaleInAnimations.get(module);
                if (animation.calcPercent() == 0.0F) {
                    continue;
                }

                transparency = animation.calcPercent();
                animationScale = 0.86F + 0.14F * transparency;
            } else {
                if (!module.isEnabled()) {
                    continue;
                }
            }

            String moduleName = module.getName();
            GL11.glAlphaFunc(519, 0.0F);
            GL11.glPushMatrix();

            int xPos = screenWidth - margin - this.font.getWidth(moduleName) / 2;
            int yPos = screenHeight + 12;

            GL11.glTranslatef((float) xPos, (float) yPos, 0.0F);
            GL11.glScalef(animationScale, animationScale, 1.0F);
            GL11.glTranslatef((float) (-xPos), (float) (-yPos), 0.0F);

            float scaleFactor = (float) Math.sqrt(Math.min(1.2F, (float) this.font.getWidth(moduleName) / 63.0F));
            RenderUtils.drawImage(
                    (float) screenWidth - (float) this.font.getWidth(moduleName) * 1.5F - (float) margin - 20.0F,
                    (float) (screenHeight - 20),
                    (float) this.font.getWidth(moduleName) * 3.0F,
                    this.font.getHeight() + scale + 40,
                    Resources.SHADOW,
                    ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.36F * transparency * scaleFactor)
            );
            RenderUtils.drawString(
                    this.font, (float) (screenWidth - margin - this.font.getWidth(moduleName)), (float) screenHeight, moduleName, transparency != 1.0F ? ColorHelper.applyAlpha(-1, transparency * 0.95F) : color
            );
            GL11.glPopMatrix();
            screenHeight = (int) ((float) screenHeight + (this.font.getHeight() + scale) * QuadraticEasing.easeInOutQuad(transparency, 0.0F, 1.0F, 1.0F));
        }
    }

    @Override
    public void onEnable() {
        setFontSize();
    }

    @Override
    public void onInit() {
        for (Module module : Client.INSTANCE.moduleManager.modules) {
            if (module.getCategory() != Category.GUI) {
                this.activeModules.add(module);
                this.scaleInAnimations.put(module, new AnimationUtils(150, 150, AnimationUtils.Direction.FORWARDS));

                if (!this.animations.value) {
                    continue;
                }
                this.scaleInAnimations.get(module).changeDirection(!module.isEnabled() ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
            }
        }

        this.activeModules.sort((a, b) -> {
            int aLength = FontUtils.HELVETICA_LIGHT_20.getWidth(a.name);
            int bLength = FontUtils.HELVETICA_LIGHT_20.getWidth(b.name);
            if (aLength <= bLength) {
                return aLength != bLength ? 1 : 0;
            } else {
                return -1;
            }
        });
    }

    private void setFontSize() {
        switch (fontSize.value) {
            case "Normal":
                this.font = FontUtils.HELVETICA_LIGHT_20;
                break;
            case "Small":
                this.font = FontUtils.HELVETICA_LIGHT_18;
                break;
            default:
                this.font = FontUtils.HELVETICA_LIGHT_14;
        }
    }

    private Collection<ScoreboardPlayerScore> getScores() {
        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective scoreobjective = null;
        Team playerTeam = scoreboard.getPlayerTeam(client.player.getEntityName());

        if (playerTeam != null) {
            int colorIndex = playerTeam.getColor().getColorIndex();
            if (colorIndex >= 0) {
                scoreobjective = scoreboard.getObjectiveForSlot(3 + colorIndex);
            }
        }

        ScoreboardObjective scoreObjective = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveForSlot(1);
        return scoreboard.getAllPlayerScores(scoreObjective);
    }
}
