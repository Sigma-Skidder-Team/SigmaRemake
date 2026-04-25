package io.github.sst.remake.module.impl.item;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.PreOpenScreenEvent;
import io.github.sst.remake.event.impl.game.EndTickEvent;
import io.github.sst.remake.event.impl.game.player.MotionEvent;
import io.github.sst.remake.event.impl.game.player.RotateEvent;
import io.github.sst.remake.event.impl.game.world.LoadWorldEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.util.game.combat.RotationUtils;
import io.github.sst.remake.util.game.combat.data.Rotation;
import io.github.sst.remake.util.game.player.InventoryUtils;
import io.github.sst.remake.util.game.world.BlockUtils;
import io.github.sst.remake.util.game.world.RaytraceUtils;
import io.github.sst.remake.util.math.timer.TogglableTimer;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused", "DataFlowIssue"})
public class ChestStealerModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Stealer mode", 0, "Normal", "Smart");
    private final BooleanSetting silent = new BooleanSetting("Silent", "Steals without showing the chest GUI.", false);
    private final BooleanSetting aura = new BooleanSetting("Aura", "Automatically open chests near you.", false);
    private final BooleanSetting ignoreJunk = new BooleanSetting("Ignore junk", "Ignores useless items.", true);
    private final BooleanSetting close = new BooleanSetting("Close", "Automatically closes the chest when done", true)
            .hide(() -> silent.value);
    private final SliderSetting delay = new SliderSetting("Delay", "Click delay", 0.2f, 0.0f, 1.0f, 0.01f)
            .hide(() -> mode.value.equals("Smart"));
    private final SliderSetting firstItem = new SliderSetting("First item delay", "Tick delay before grabbing first item", 0.2f, 0.0f, 1.0f, 0.01f)
            .hide(() -> mode.value.equals("Smart"));

    public boolean stealingInProgress;
    private boolean silentStealing;
    private final ConcurrentHashMap<ChestBlockEntity, Boolean> chests = new ConcurrentHashMap<>();
    private final TogglableTimer clickTimer = new TogglableTimer();
    private final TogglableTimer auraTimer = new TogglableTimer();
    private ChestBlockEntity targetChest;
    private Rotation auraRotation;

    // Smart mode fields (Fitts' Law model)
    private double lastClickX = -1;
    private double lastClickY = -1;
    private final Random random = new Random();
    private int clickCount = 0;
    private double lastDelay = 0;
    private static final double FITTS_A = 0.05;
    private static final double FITTS_B = 0.08;
    private static final double FITTS_W = 32.0;

    public ChestStealerModule() {
        super("ChestStealer", "Steals items from chests.", Category.ITEM);
    }

    @Override
    public void onEnable() {
        targetChest = null;
        auraRotation = null;
        stealingInProgress = false;
        silentStealing = false;
        resetHumanState();
        if (!chests.isEmpty()) {
            chests.clear();
        }
    }

    @Subscribe
    public void onRotate(RotateEvent event) {
        if (aura.value && auraRotation != null) {
            event.yaw = auraRotation.yaw;
            event.pitch = auraRotation.pitch;
        }
    }

    @Subscribe
    public void onLoadWorld(LoadWorldEvent event) {
        if (!chests.isEmpty()) {
            chests.clear();
        }
        silentStealing = false;
    }

    @Subscribe
    public void onTick(EndTickEvent event) {
        if (silent.value) {
            handleSilentTick();
        } else {
            handleNormalTick();
        }
    }

    @Subscribe
    public void onPreOpenScreen(PreOpenScreenEvent event) {
        if (!silent.value) return;
        if (event.screen instanceof GenericContainerScreen) {
            event.cancel();
            silentStealing = true;
            stealingInProgress = true;
            clickTimer.reset();
            if (!clickTimer.enabled) {
                clickTimer.start();
            }
        }
    }

    @Subscribe
    public void onMotion(MotionEvent event) {
        if (!event.isPre()) return;

        if (aura.value) {
            if (auraTimer.getElapsedTime() > 2000L && stealingInProgress) {
                auraTimer.reset();
                stealingInProgress = false;
            }

            if (!auraTimer.enabled) {
                auraTimer.start();
            }

            scanForChests();

            if (targetChest != null && client.currentScreen == null && !silentStealing && auraTimer.getElapsedTime() > 1000L) {
                Vec3d chestCenter = new Vec3d(
                        targetChest.getPos().getX() + 0.5,
                        targetChest.getPos().getY() + 0.45,
                        targetChest.getPos().getZ() + 0.5
                );
                Rotation rot = RotationUtils.getRotationsToVector(chestCenter);
                BlockHitResult hitResult = RaytraceUtils.rayTrace(rot.yaw, rot.pitch, 5.0f);

                if (hitResult.getBlockPos().getX() == targetChest.getPos().getX()
                        && hitResult.getBlockPos().getY() == targetChest.getPos().getY()
                        && hitResult.getBlockPos().getZ() == targetChest.getPos().getZ()) {
                    stealingInProgress = true;
                    client.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult));
                    client.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                    auraTimer.reset();
                }
            }

            boolean rotated = false;

            for (ConcurrentHashMap.Entry<ChestBlockEntity, Boolean> entry : chests.entrySet()) {
                ChestBlockEntity chest = entry.getKey();
                boolean opened = entry.getValue();
                float cx = (float) chest.getPos().getX();
                float cy = (float) chest.getPos().getY() + 0.1f;
                float cz = (float) chest.getPos().getZ();

                if (!stealingInProgress
                        && (targetChest == null || client.player.squaredDistanceTo(cx, cy, cz)
                        > client.player.squaredDistanceTo(cx, cy, cz))
                        && !opened
                        && Math.sqrt(client.player.squaredDistanceTo(cx, cy, cz)) < 5.0
                        && auraTimer.getElapsedTime() > 1000L
                        && client.currentScreen == null
                        && !silentStealing) {

                    Vec3d chestCenter = new Vec3d(cx + 0.5, cy + 0.35, cz + 0.5);
                    Rotation rot = RotationUtils.getRotationsToVector(chestCenter);
                    BlockHitResult hitResult = RaytraceUtils.rayTrace(rot.yaw, rot.pitch, 5.0f);

                    if (hitResult.getBlockPos().getX() == chest.getPos().getX()
                            && hitResult.getBlockPos().getY() == chest.getPos().getY()
                            && hitResult.getBlockPos().getZ() == chest.getPos().getZ()) {
                        targetChest = chest;
                        auraRotation = rot;
                        rotated = true;
                    }
                }
            }

            if (!rotated && client.currentScreen == null && !silentStealing && targetChest != null) {
                chests.put(targetChest, true);
                targetChest = null;
                auraRotation = null;
            }
        }
    }

    private void handleSilentTick() {
        if (!silentStealing) {
            clickTimer.stop();
            clickTimer.reset();
            return;
        }

        if (client.player == null) return;

        ScreenHandler handler = client.player.currentScreenHandler;

        // If the handler is no longer a chest, we lost the container
        if (!(handler instanceof GenericContainerScreenHandler)) {
            silentStealing = false;
            stealingInProgress = false;
            clickTimer.stop();
            clickTimer.reset();
            return;
        }

        GenericContainerScreenHandler chestHandler = (GenericContainerScreenHandler) handler;
        int chestSlotCount = chestHandler.getRows() * 9;

        if (InventoryUtils.hasAllSlotsFilled()) {
            client.player.closeHandledScreen();
            silentStealing = false;
            stealingInProgress = false;
            clickTimer.stop();
            clickTimer.reset();
            return;
        }

        // Apply delay (Normal mode only; Smart mode doesn't make sense without a GUI)
        if (delay.value > 0.0f && clickTimer.getElapsedTime() < delay.value * 1000.0f) {
            return;
        }

        boolean allDone = true;

        for (Slot slot : chestHandler.slots) {
            if (slot.hasStack() && slot.id < chestSlotCount) {
                ItemStack stack = slot.getStack();

                if (InventoryUtils.isJunkItem(stack, ignoreJunk.value)) {
                    continue;
                }

                client.interactionManager.clickSlot(
                        chestHandler.syncId,
                        slot.id, 0,
                        SlotActionType.QUICK_MOVE,
                        client.player
                );

                clickTimer.reset();
                allDone = false;

                if (delay.value > 0.0f) {
                    break;
                }
            }
        }

        if (allDone) {
            client.player.closeHandledScreen();
            silentStealing = false;
            stealingInProgress = false;
            clickTimer.stop();
            clickTimer.reset();

            for (ChestBlockEntity chest : chests.keySet()) {
                if (chest == targetChest) {
                    targetChest = null;
                    chests.put(chest, true);
                }
            }
        }
    }

    private void handleNormalTick() {
        if (!(client.currentScreen instanceof GenericContainerScreen)) {
            stealingInProgress = false;
            clickTimer.stop();
            clickTimer.reset();
            resetHumanState();
            if (client.currentScreen == null && InventoryUtils.hasAllSlotsFilled()) {
                auraTimer.reset();
            }
        } else {
            if (!clickTimer.enabled) {
                clickTimer.start();
            }

            if (InventoryUtils.hasAllSlotsFilled()) {
                if (close.value) {
                    client.player.closeHandledScreen();
                }
                return;
            }

            GenericContainerScreen chestScreen = (GenericContainerScreen) client.currentScreen;

            if (!shouldSteal(chestScreen)) {
                if (targetChest != null) {
                    chests.put(targetChest, true);
                }
                return;
            }

            boolean allDone = true;

            for (Slot slot : chestScreen.getScreenHandler().slots) {
                if (slot.hasStack() && slot.id < chestScreen.getScreenHandler().getRows() * 9) {
                    ItemStack stack = slot.getStack();

                    if (InventoryUtils.isJunkItem(stack, ignoreJunk.value)) {
                        continue;
                    }

                    if (mode.value.equals("Smart")) {
                        HandledScreen<?> containerScreen = (HandledScreen<?>) client.currentScreen;
                        int guiLeft = containerScreen.x;
                        int guiTop = containerScreen.y;
                        double targetX = guiLeft + slot.x + 8;
                        double targetY = guiTop + slot.y + 8;

                        if (lastClickX == -1) {
                            double mouseX = client.mouse.getX()
                                    * (double) client.getWindow().getScaledWidth()
                                    / (double) client.getWindow().getWidth();
                            double mouseY = client.mouse.getY()
                                    * (double) client.getWindow().getScaledHeight()
                                    / (double) client.getWindow().getHeight();
                            lastClickX = mouseX;
                            lastClickY = mouseY;
                        }

                        double dist = Math.sqrt(
                                Math.pow(targetX - lastClickX, 2) +
                                        Math.pow(targetY - lastClickY, 2)
                        );

                        double smartDelay = computeSmartDelay(dist);

                        if (clickTimer.getElapsedTime() < smartDelay * 1000) {
                            return;
                        }

                        lastClickX = targetX;
                        lastClickY = targetY;
                    } else {
                        if (!stealingInProgress) {
                            if ((float) clickTimer.getElapsedTime() < firstItem.value * 1000.0f) {
                                return;
                            }
                            stealingInProgress = true;
                        }
                    }

                    client.interactionManager.clickSlot(
                            chestScreen.getScreenHandler().syncId,
                            slot.id, 0,
                            SlotActionType.QUICK_MOVE,
                            client.player
                    );

                    clickTimer.reset();
                    clickCount++;
                    allDone = false;

                    if (mode.value.equals("Smart") || delay.value > 0.0f) {
                        break;
                    }
                }
            }

            if (allDone) {
                if (stealingInProgress) {
                    stealingInProgress = false;
                }

                if (close.value) {
                    client.player.closeHandledScreen();
                }

                for (ChestBlockEntity chest : chests.keySet()) {
                    if (chest == targetChest) {
                        targetChest = null;
                        chests.put(chest, true);
                    }
                }
            }
        }
    }

    private void resetHumanState() {
        clickCount = 0;
        lastDelay = 0;
        lastClickX = -1;
        lastClickY = -1;
    }

    private double computeSmartDelay(double dist) {
        double indexOfDifficulty = Math.log(1.0 + (2.0 * dist / FITTS_W)) / Math.log(2);
        double fitts = FITTS_A + FITTS_B * indexOfDifficulty;

        double noise = random.nextGaussian() * 0.025;
        double hesitation = random.nextDouble() < 0.08 ? random.nextDouble() * 0.12 : 0.0;
        double fatigue = Math.min(clickCount * 0.003, 0.06);

        double raw = Math.max(0.10, fitts + noise + hesitation + fatigue);
        double smoothed = lastDelay == 0.0 ? raw : lastDelay * 0.35 + raw * 0.65;
        lastDelay = smoothed;

        return smoothed;
    }

    private boolean shouldSteal(GenericContainerScreen chestScreen) {
        if (client.player == null || client.world == null) return false;

        BlockPos playerPos = client.player.getBlockPos();
        int range = 8;

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (BlockUtils.getBlockAt(pos) instanceof ChestBlock) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // --- Aura: scan world for chest tile entities ---

    private void scanForChests() {
        List<BlockEntity> blockEntities = new ArrayList<>(client.world.blockEntities);
        blockEntities.removeIf(be -> !(be instanceof ChestBlockEntity));

        for (BlockEntity be : blockEntities) {
            if (!chests.containsKey((ChestBlockEntity) be)) {
                chests.put((ChestBlockEntity) be, false);
            }
        }

        for (ChestBlockEntity chest : chests.keySet()) {
            if (!blockEntities.contains(chest)) {
                chests.remove(chest);
            }
        }
    }
}