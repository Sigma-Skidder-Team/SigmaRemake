package io.github.sst.remake.module.impl.render;

import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.module.impl.render.esp.ShadowESP;
import io.github.sst.remake.module.impl.render.esp.SimsESP;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.SubModuleSetting;
import io.github.sst.remake.tracker.impl.BotTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class ESPModule extends Module {
    private final SubModuleSetting mode = new SubModuleSetting("Mode", "ESP mode", new SimsESP(), new ShadowESP());
    private final BooleanSetting showPlayers = new BooleanSetting("Show players", "Outline players?", true);
    private final BooleanSetting showMonsters = new BooleanSetting("Show monsters", "Outline monsters?", false);
    private final BooleanSetting showAnimals = new BooleanSetting("Show animals", "Outline animals/passive mobs?", false);
    private final BooleanSetting showInvisibles = new BooleanSetting("Show invisibles", "Outline invisible entities?", true);

    public ESPModule() {
        super("ESP", "Helps you see entities.", Category.RENDER);
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