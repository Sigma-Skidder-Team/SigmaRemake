package io.github.sst.remake.module.impl.combat;

import io.github.sst.remake.Client;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.data.rotation.Rotatable;
import io.github.sst.remake.data.rotation.Rotation;
import io.github.sst.remake.event.impl.game.RunLoopEvent;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.world.LoadWorldEvent;
import io.github.sst.remake.gui.screen.notifications.Notification;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.util.game.RotationUtils;
import io.github.sst.remake.util.math.BasicTimer;
import io.github.sst.remake.util.math.TogglableTimer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings({"unused", "DataFlowIssue"})
public class KillAuraModule extends Rotatable {

    private final ModeSetting mode = new ModeSetting("Mode", "Attack mode", 0, "Single", "Switch", "Multi", "Multi2");
    private final ModeSetting sortMode = new ModeSetting("Sort Mode", "Target sort mode", 0, "Range", "Health", "Angle", "Armor", "Prev Range");
    private final ModeSetting attackMode = new ModeSetting("Attack Mode", "Attack mode", 0, "Pre", "Post");
    private final ModeSetting rotationMode = new ModeSetting("Rotation Mode", "Rotation mode", 0, "NCP", "AAC", "Smooth", "LockView", "None");

    private final SliderSetting range = new SliderSetting("Range", "Working range", 4, 2.8f, 8, 0.01f);

    private final SliderSetting minCPS = new SliderSetting("Min CPS", "Minimal attack cps", 11, 0, 20, 1);
    private final SliderSetting maxCPS = new SliderSetting("Max CPS", "Maximal attack cps", 14, 1, 20, 1);
    private final SliderSetting hitChance = new SliderSetting("Hit Chance", "Chance of attacks landing (%)", 100, 25, 100, 1);

    private final BooleanSetting players = new BooleanSetting("Players", "Target players", true);
    private final BooleanSetting animals = new BooleanSetting("Animals", "Target animals", false);
    private final BooleanSetting monsters = new BooleanSetting("Monsters", "Target monsters", false);
    private final BooleanSetting invisibles = new BooleanSetting("Invisibles", "Target invisible entities", true);

    private final BooleanSetting raytrace = new BooleanSetting("Raytrace", "Raytrace to target", true);
    private final BooleanSetting throughWalls = new BooleanSetting("Through walls", "Hit entities through walls", false);
    private final BooleanSetting cooldown = new BooleanSetting("Cooldown", "Use 1.9+ attack cooldown", false);
    private final BooleanSetting noSwing = new BooleanSetting("No swing", "Skip swinging animation", false);

    private final BooleanSetting deathToggle = new BooleanSetting("Disable on death", "Toggle Aura on death", true);

    private Entity target;
    private final List<LivingEntity> targets = new ArrayList<>();

    private final BasicTimer attackTimer = new BasicTimer();

    public KillAuraModule() {
        super("KillAura", "Attacks nearby entities.", Category.COMBAT, 100);
    }

    @Override
    public void onEnable() {
        target = null;
        targets.clear();
    }

    @Override
    public void onDisable() {
        target = null;
        targets.clear();
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

        if (event.isPost()) return;

        updateTargets();
    }

    @Subscribe
    public void onRunLoop(RunLoopEvent event) {
        if (!event.isPre() || target == null) return;

        if (cooldown.value && client.player.getAttackCooldownProgress(0) >= 1) {
            client.doAttack();
            return;
        }

        int cps = (int) ((minCPS.value + maxCPS.value) / 2);
        long delay = 1000 / cps;

        if (attackTimer.hasElapsed(delay, true)) {
            client.doAttack();
        }
    }

    private void updateTargets() {
        for (Entity entity : client.world.getEntities()) {
            if (!entity.isAlive() || !entity.isAttackable() || !(entity instanceof LivingEntity)) continue;
            if (entity == client.player) continue;

            LivingEntity livingEntity = (LivingEntity) entity;

            if (players.value && entity instanceof PlayerEntity) {
                targets.add(livingEntity);
            }

            if (animals.value && (entity instanceof AnimalEntity || entity instanceof WaterCreatureEntity)) {
                targets.add(livingEntity);
            }

            if (monsters.value && (entity instanceof MobEntity || entity instanceof MerchantEntity || entity instanceof Monster)) {
                targets.add(livingEntity);
            }

            if (invisibles.value && entity.isInvisible()) {
                targets.add(livingEntity);
            }
        }

        Comparator<LivingEntity> comparator;

        switch (sortMode.value) {
            case "Health":
                comparator = Comparator.comparing(LivingEntity::getHealth);
                break;
            case "Armor":
                comparator = Comparator.comparing(LivingEntity::getArmor);
                break;
            default:
                comparator = Comparator.comparing(e -> e.squaredDistanceTo(client.player));
                break;
        }

        targets.sort(comparator);

        target = targets.get(0);
    }

    @Override
    public Rotation getRotations() {
        if (target == null) return null;

        switch (rotationMode.value) {
            case "LockView":
                Rotation basicRotations = RotationUtils.getBasicRotations(target);
                client.player.pitch = basicRotations.pitch;
                client.player.yaw = basicRotations.yaw;
                return basicRotations;

            default:
                return RotationUtils.getBasicRotations(target);
        }
    }
}
