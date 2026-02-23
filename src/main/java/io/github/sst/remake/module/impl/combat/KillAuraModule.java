package io.github.sst.remake.module.impl.combat;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.data.rotation.Rotatable;
import io.github.sst.remake.data.rotation.Rotation;
import io.github.sst.remake.event.impl.client.ActionEvent;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.world.LoadWorldEvent;
import io.github.sst.remake.gui.screen.notifications.Notification;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.util.game.RotationUtils;
import io.github.sst.remake.util.math.BasicTimer;
import io.github.sst.remake.util.math.ClickDelayCalculator;
import io.github.sst.remake.util.viaversion.AttackUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"ALL"})
public class KillAuraModule extends Rotatable {

    private final ModeSetting mode = new ModeSetting("Mode", "Attack mode", 0, "Single", "Switch", "Multi", "Multi2");
    private final ModeSetting sortMode = new ModeSetting("Sort mode", "Target sort mode", 0, "Range", "Health", "Angle", "Armor", "Prev Range");
    private final ModeSetting attackMode = new ModeSetting("Attack mode", "Attack mode", 0, "Mouse", "Packet");
    private final ModeSetting rotationMode = new ModeSetting("Rotation mode", "Rotation mode", 0, "NCP", "LockView", "None");
    private final ModeSetting clickMode = new ModeSetting("Click mode", "Click mode", 0, "CPS", "1.9");

    private final SliderSetting aimRange = new SliderSetting("Aim range", "Rotation range", 6, 1, 8, 0.01f);
    private final SliderSetting attackRange = new SliderSetting("Attack range", "Working range", 3.0f, 1, 8, 0.01f);
    private final SliderSetting searchRange = new SliderSetting("Search range", "Search range (heavy!) (radius)", 9, 1, 15, 0.01f);

    private final SliderSetting minCPS = new SliderSetting("Min CPS", "Minimal attack cps", 11, 0, 20, 1);
    private final SliderSetting maxCPS = new SliderSetting("Max CPS", "Maximal attack cps", 14, 1, 20, 1);
    private final SliderSetting hitChance = new SliderSetting("Hit chance", "Chance of attacks landing (%)", 100, 25, 100, 1);

    private final BooleanSetting delayPatterns = new BooleanSetting("Delay patterns", "Use delay patterns", false).hide(() -> !clickMode.value.equals("CPS"));
    private final SliderSetting pattern1 = new SliderSetting("1st pattern", "First delay pattern value", 90, 0, 700, 10).hide(() -> !clickMode.value.equals("CPS") || !delayPatterns.value);
    private final SliderSetting pattern2 = new SliderSetting("2nd pattern", "Second delay pattern value", 110, 0, 700, 10).hide(() -> !clickMode.value.equals("CPS") || !delayPatterns.value);
    private final SliderSetting pattern3 = new SliderSetting("3rd pattern", "Third delay pattern value", 130, 0, 700, 10).hide(() -> !clickMode.value.equals("CPS") || !delayPatterns.value);

    private final BooleanSetting players = new BooleanSetting("Players", "Target players", true);
    private final BooleanSetting animals = new BooleanSetting("Animals", "Target animals", false);
    private final BooleanSetting monsters = new BooleanSetting("Monsters", "Target monsters", false);
    private final BooleanSetting invisibles = new BooleanSetting("Invisibles", "Target invisible entities", true);

    private final BooleanSetting raytrace = new BooleanSetting("Raytrace", "Raytrace to target", true);
    private final BooleanSetting throughWalls = new BooleanSetting("Through walls", "Target entities behind walls", false);
    private final BooleanSetting noSwing = new BooleanSetting("No swing", "Skip swinging animation", false).hide(() -> !attackMode.value.equals("Packet"));

    private final BooleanSetting deathToggle = new BooleanSetting("Disable on death", "Toggle Aura on death", true);

    private final ClickDelayCalculator cpsCalculator = new ClickDelayCalculator(minCPS.value, maxCPS.value);
    private final List<LivingEntity> targets = new ArrayList<>();
    private final BasicTimer attackTimer = new BasicTimer();
    private Entity target;

    public KillAuraModule() {
        super("KillAura", "Attacks nearby entities.", Category.COMBAT, 100);

        minCPS.addListener(setting -> cpsCalculator.setMinCPS(setting.value));
        maxCPS.addListener(setting -> cpsCalculator.setMaxCPS(setting.value));
        delayPatterns.addListener(setting -> cpsCalculator.setPatternEnabled(setting.value));
        pattern1.addListener(setting -> cpsCalculator.setDelayPattern1(setting.value));
        pattern2.addListener(setting -> cpsCalculator.setDelayPattern2(setting.value));
        pattern3.addListener(setting -> cpsCalculator.setDelayPattern3(setting.value));
    }

    @Override
    public void onEnable() {
        reset();
    }

    @Override
    public void onDisable() {
        reset();
    }

    @Subscribe
    public void onLoadWorld(LoadWorldEvent event) {
        if (deathToggle.value) {
            Client.INSTANCE.notificationManager.send(new Notification("Aura", "Aura disabled due to world change"));
            toggle();
        }
    }

    @Subscribe
    public void onClientPlayerTick(ClientPlayerTickEvent event) {
        if (!client.player.isAlive() && deathToggle.value) {
            Client.INSTANCE.notificationManager.send(new Notification("Aura", "Aura disabled due to respawn"));
            toggle();
            return;
        }

        updateTargets();
    }

    @Subscribe
    public void onAction(ActionEvent event) {
        if (target == null) return;

        double maxRange = attackRange.value;
        if (client.player.squaredDistanceTo(target) > (maxRange * maxRange)) {
            return;
        }

        if (!throughWalls.value && !client.player.canSee(target)) return;

        if (Math.random() * 100 > hitChance.value) return;

        if (clickMode.value.equals("1.9") && client.player.getAttackCooldownProgress(0) >= 1) {
            attack(target);
            return;
        }

        if (attackTimer.hasElapsed(cpsCalculator.getClickDelay(), true)) {
            attack(target);
        }
    }

    private void attack(Entity target) {
        switch (attackMode.value) {
            case "Packet":
                AttackUtils.attackEntity(target, !noSwing.value);
                break;
            default:
                client.doAttack();
        }
    }

    private void updateTargets() {
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
        );

        LivingEntity best = null;
        double bestMetric = Double.POSITIVE_INFINITY;

        for (LivingEntity livingEntity : entities) {
            boolean eligible = false;

            if (players.value && livingEntity instanceof PlayerEntity) {
                eligible = true;
            } else if (animals.value && (livingEntity instanceof AnimalEntity
                    || livingEntity instanceof WaterCreatureEntity)) {
                eligible = true;
            } else if (monsters.value && (livingEntity instanceof MobEntity
                    || livingEntity instanceof MerchantEntity
                    || livingEntity instanceof Monster)) {
                eligible = true;
            } else if (invisibles.value && livingEntity.isInvisible()) {
                eligible = true;
            }

            if (!eligible) continue;

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
                    double angle = RotationUtils.getAngleMetricToEntity(livingEntity, client.player.yaw);
                    double distSq = livingEntity.squaredDistanceTo(client.player);
                    metric = angle * 1_000_000.0 + distSq; // big weight to angle, distance breaks ties
                    break;

                case "Prev Range":
                    Entity anchor = target != null ? target : client.player;
                    float dist = anchor.distanceTo(livingEntity);
                    metric = dist;
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

    @Override
    public Rotation getRotations() {
        if (target == null) return null;

        double maxRange = aimRange.value;
        if (client.player.squaredDistanceTo(target) > (maxRange * maxRange)) {
            return null;
        }

        if (!throughWalls.value && !client.player.canSee(target)) {
            return null;
        }

        Rotation rotations = RotationUtils.getRotationsSmart(target, raytrace.value);

        switch (rotationMode.value) {
            case "None":
                return new Rotation(client.player.yaw, client.player.pitch);

            case "LockView":
                client.player.pitch = rotations.pitch;
                client.player.yaw = rotations.yaw;
                return rotations;

            case "NCP":
            default:
                return rotations;
        }
    }

    private void reset() {
        target = null;
        targets.clear();
    }
}
