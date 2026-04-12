package io.github.sst.remake.module.impl.combat;

import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.player.RotateEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.tracker.impl.BotTracker;
import io.github.sst.remake.util.game.combat.RotationUtils;
import io.github.sst.remake.util.game.combat.data.Rotation;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.*;
import java.util.List;

@SuppressWarnings({"ALL"})
public class AimbotModule extends Module {
    private final ModeSetting sortMode = new ModeSetting("Sort mode", "Target sort mode", 0, "Range", "Health", "Angle", "Armor", "Prev Range");
    private final ModeSetting rotationMode = new ModeSetting("Rotation mode", "Rotation mode", 0, "Basic");

    private final SliderSetting aimRange = new SliderSetting("Aim range", "Rotation range", 6, 1, 8, 0.01f);
    private final SliderSetting searchRange = new SliderSetting("Search range", "Search range (heavy!) (radius)", 9, 1, 15, 0.01f);

    private final BooleanSetting players = new BooleanSetting("Players", "Target players", true);
    private final BooleanSetting animals = new BooleanSetting("Animals", "Target animals", false);
    private final BooleanSetting monsters = new BooleanSetting("Monsters", "Target monsters", false);
    private final BooleanSetting invisibles = new BooleanSetting("Invisibles", "Target invisible entities", true);

    private final List<LivingEntity> targets = new ArrayList<>();
    private Entity target;

    private final HashMap<Entity, AnimationUtils> outlinedTargets = new HashMap<>();

    public AimbotModule() {
        super("Aimbot", "Aims at nearby entities.", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        reset();
    }

    @Override
    public void onDisable() {
        reset();
    }

    @Subscribe(priority = Priority.HIGHEST)
    public void onClientPlayerTick(ClientPlayerTickEvent event) {
        updateTargets();
    }

    @Subscribe(priority = Priority.HIGHEST)
    public void onRotate(RotateEvent event) {
        if (target == null) return;

        double maxRange = aimRange.value;
        if (client.player.squaredDistanceTo(target) > (maxRange * maxRange)) {
            return;
        }

        Rotation rotations = RotationUtils.getRotationsSmart(target, true);

        if (rotations == null) return;

        switch (rotationMode.value) {
            default:
                client.player.setYaw(rotations.yaw);
                client.player.setYaw(rotations.pitch);
                break;
        }
    }

    private void reset() {
        target = null;
        targets.clear();
    }

    private boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        LivingEntity living = (LivingEntity) entity;

        if (!living.isAlive()) return false;

        double range = searchRange.value;
        if (client.player.squaredDistanceTo(living) > range * range) return false;

        if (!invisibles.value && living.isInvisible()) return false;

        if (living instanceof ArmorStandEntity) return false;

        boolean eligible =
                (players.value && living instanceof PlayerEntity && !BotTracker.isBot((PlayerEntity) living))
                        || (animals.value && (living instanceof AnimalEntity || living instanceof WaterCreatureEntity))
                        || (monsters.value && living instanceof Monster);

        return eligible;
    }

    private void updateTargets() {
        if (target != null && isValidTarget(target)) {
            targets.clear();
            targets.add((LivingEntity) target);
            return;
        }

        reset();

        if (client.player == null || client.world == null) return;

        double range = searchRange.value;

        Box box = new Box(
                client.player.getX() - range,
                client.player.getY() - range,
                client.player.getZ() - range,
                client.player.getX() + range,
                client.player.getY() + range,
                client.player.getZ() + range
        );

        List<LivingEntity> entities = client.world.getEntitiesByClass(
                LivingEntity.class,
                box,
                entity -> entity != client.player
                        && entity.isAlive()
                        && entity.isAttackable()
                        && !(entity instanceof ArmorStandEntity)
        );

        LivingEntity best = null;
        double bestMetric = Double.POSITIVE_INFINITY;

        for (LivingEntity livingEntity : entities) {
            if (!isValidTarget(livingEntity)) {
                continue;
            }

            targets.add(livingEntity);

            double metric;

            switch (sortMode.value) {
                case "Health":
                    metric = livingEntity.getHealth();
                    break;

                case "Armor":
                    metric = livingEntity.getArmor();
                    break;

                case "Angle":
                    double angle = RotationUtils.getAngleMetricToEntity(livingEntity, client.player.getYaw());
                    double distSq = livingEntity.squaredDistanceTo(client.player);
                    metric = angle * 1_000_000.0 + distSq;
                    break;

                case "Prev Range":
                    Entity anchor = target != null ? target : client.player;
                    metric = anchor.distanceTo(livingEntity);
                    break;

                case "Range":
                default:
                    metric = livingEntity.squaredDistanceTo(client.player);
                    break;
            }

            if (metric < bestMetric) {
                bestMetric = metric;
                best = livingEntity;
            }
        }

        target = best;
    }
}