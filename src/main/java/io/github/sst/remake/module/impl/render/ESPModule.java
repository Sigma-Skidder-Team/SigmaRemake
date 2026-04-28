package io.github.sst.remake.module.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.module.impl.render.esp.BoxOutlineESP;
import io.github.sst.remake.module.impl.render.esp.ShadowESP;
import io.github.sst.remake.module.impl.render.esp.SimsESP;
import io.github.sst.remake.module.impl.render.esp.VanillaESP;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.ColorSetting;
import io.github.sst.remake.setting.impl.SubModuleSetting;
import io.github.sst.remake.tracker.impl.BotTracker;
import io.github.sst.remake.util.game.world.EntityUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.porting.StateManager;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ESPModule extends Module {
    private final SubModuleSetting mode = new SubModuleSetting("Mode", "ESP mode", new SimsESP(), new ShadowESP(), new VanillaESP(), new BoxOutlineESP());
    public final ColorSetting color = new ColorSetting("Color", "ESP color", ClientColors.LIGHT_GREYISH_BLUE).hide(() -> !mode.value.name.equals("Shadow") && !mode.value.name.equals("Box Outline"));
    private final BooleanSetting showPlayers = new BooleanSetting("Show players", "Outline players?", true);
    private final BooleanSetting showMonsters = new BooleanSetting("Show monsters", "Outline monsters?", false);
    private final BooleanSetting showAnimals = new BooleanSetting("Show animals", "Outline animals/passive mobs?", false);
    private final BooleanSetting showInvisibles = new BooleanSetting("Show invisibles", "Outline invisible entities?", true);

    public ESPModule() {
        super("ESP", "Helps you see entities.", Category.RENDER);
    }

    public void setup() {
        RenderSystem.lineWidth(3.0f);
        RenderSystem.enableBlend();
        StateManager.disableLighting();
        StateManager.enableAlphaTest();
        RenderSystem.disableTexture();
        StateManager.disableColorMaterial();
        RenderSystem.disableDepthTest();
        StateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        client.gameRenderer.getLightmapTextureManager().enable();
    }

    public void renderShadowSprites() {
        int color = ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE, 0.8f);
        getTargets().forEach(entity -> {
            Vec3d pos = EntityUtils.getRelativePosition(entity);
            StateManager.pushMatrix();
            StateManager.alphaFunc(GL11.GL_ALWAYS, 0.0f);
            StateManager.translated(pos.x, pos.y, pos.z);
            StateManager.translatef(0.0f, entity.getHeight(), 0.0f);
            StateManager.translatef(0.0f, 0.1f, 0.0f);
            StateManager.rotatef(client.gameRenderer.getCamera().getYaw(), 0.0f, -1.0f, 0.0f);
            StateManager.scalef(-0.11f, -0.11f, -0.11f);
            RenderUtils.drawImage(
                    -entity.getWidth() * 22.0f,
                    -entity.getHeight() * 5.5f,
                    entity.getWidth() * 44.0f,
                    entity.getHeight() * 21.0f,
                    Resources.SHADOW,
                    color,
                    false);
            StateManager.popMatrix();
        });
    }

    public List<LivingEntity> getTargets() {
        List<LivingEntity> targets = new ArrayList<>();

        if (client.world == null) return targets;

        if (showPlayers.value) {
            for (PlayerEntity player : client.world.getPlayers()) {
                if (BotTracker.isBot(player)) continue;
                if (player == client.player) continue;
                if (!showInvisibles.value && player.isInvisible()) continue;
                targets.add(player);
            }
        }

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity)) continue;
            if (entity instanceof PlayerEntity) continue;

            LivingEntity living = (LivingEntity) entity;

            if (!showInvisibles.value && living.isInvisible()) continue;

            if (showMonsters.value && living instanceof Monster) {
                targets.add(living);
                continue;
            }

            if (showAnimals.value && living instanceof AnimalEntity) {
                targets.add(living);
            }
        }

        return targets;
    }
}