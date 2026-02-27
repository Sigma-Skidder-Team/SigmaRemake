package io.github.sst.remake.module.impl.movement;

import io.github.sst.remake.data.bus.Priority;
import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.net.SendPacketEvent;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.event.impl.game.player.MoveEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.module.impl.movement.blockfly.AACBlockFly;
import io.github.sst.remake.module.impl.movement.blockfly.HypixelBlockFly;
import io.github.sst.remake.module.impl.movement.blockfly.NCPBlockFly;
import io.github.sst.remake.module.impl.movement.blockfly.SmoothBlockFly;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.setting.impl.SliderSetting;
import io.github.sst.remake.setting.impl.SubModuleSetting;
import io.github.sst.remake.util.game.MovementUtils;
import io.github.sst.remake.util.game.WorldUtils;
import io.github.sst.remake.util.game.world.BlockUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.lwjgl.opengl.GL11;

@SuppressWarnings({"unused", "DataFlowIssue"})
public class BlockFlyModule extends Module {

    private final SubModuleSetting mode = new SubModuleSetting("Mode", "Scaffold mode", new AACBlockFly(), new SmoothBlockFly(), new NCPBlockFly(), new HypixelBlockFly());
    public final ModeSetting speedMode = new ModeSetting("Speed mode", "Scaffold speed mode", 0, "None", "Jump", "AAC", "Slow", "Sneak", "Cubecraft").hide(() -> !mode.value.name.equals("Smooth") && !mode.value.name.equals("NCP") && !mode.value.name.equals("Hypixel"));

    public final ModeSetting itemSpoofMode = new ModeSetting("Item spoof", "Item spoofing mode", 0, "None", "Switch", "Spoof", "LiteSpoof");
    public final ModeSetting towerMode = new ModeSetting("Tower mode", "Towering mode", 0, "None", "NCP", "AAC", "Vanilla");
    private final ModeSetting pickMode = new ModeSetting("Picking mode", "Item picking mode", 0, "Basic", "OpenInv");

    public final BooleanSetting keepRotations = new BooleanSetting("Keep rotations", "Keeps your rotations", true).hide(() -> !mode.value.name.equals("NCP") && !mode.value.name.equals("Hypixel"));
    public final BooleanSetting downwards = new BooleanSetting("Downwards", "Allows you to go down when sneaking", true).hide(() -> !mode.value.name.equals("NCP") && !mode.value.name.equals("Hypixel"));

    public final SliderSetting extend = new SliderSetting("Extend", "Block place extend", 0.0f, 0.0f, 6.0f, 0.1f).hide(() -> !mode.value.name.equals("NCP"));

    public final BooleanSetting moveAndTower = new BooleanSetting("Tower while moving", "Allow towering while moving", false).hide(() -> towerMode.value.equals("None"));
    private final BooleanSetting showBlockAmount = new BooleanSetting("Show block amount", "Render available blocks in inventory", true);
    private final BooleanSetting intelligentBlockPicker = new BooleanSetting("Intelligent block picker", "Calculate block amount and more", true);

    public final BooleanSetting noSprint = new BooleanSetting("No sprint", "Disable sprinting", false);

    private final AnimationUtils blockCountAnim = new AnimationUtils(114, 114, AnimationUtils.Direction.FORWARDS);
    private int cachedBlockCount = 0;
    public int lastSpoofedSlot;

    public BlockFlyModule() {
        super("BlockFly", "Helps you bridge.", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        blockCountAnim.changeDirection(AnimationUtils.Direction.FORWARDS);
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        if (showBlockAmount.value) {
            cachedBlockCount = countPlaceableBlocks();
        }
    }

    @Subscribe
    public void onRender(RenderClient2DEvent event) {
        blockCountAnim.changeDirection(AnimationUtils.Direction.BACKWARDS);
        if (blockCountAnim.calcPercent() != 0.0f && showBlockAmount.value) {
            renderBlockCountJello(
                    client.getWindow().getWidth() / 2,
                    client.getWindow().getHeight() - 138
                            - (int) (25.0F * AnimationUtils.easeOutCubic(blockCountAnim.calcPercent(), 0.0F,
                            1.0F, 1.0F)),
                    blockCountAnim.calcPercent());
        }
    }


    @Subscribe(priority = Priority.LOW)
    public void onSendPacket(SendPacketEvent event) {
        if (client.player == null) return;

        if (event.packet instanceof UpdateSelectedSlotC2SPacket && lastSpoofedSlot >= 0) {
            event.cancel();
        }
    }

    // Don't annotate with @Subscribe !!
    public void performTowering(MoveEvent event) {
        if (getTimer() == 0.8038576f) {
            setTimer(1.0f);
        }

        if (countPlaceableBlocks() != 0 && (!client.player.verticalCollision || towerMode.value.equals("Vanilla"))) {
            if (!MovementUtils.isMoving() || moveAndTower.value) {
                switch (towerMode.value) {
                    case "NCP":
                        if (event.getY() > 0.0) {
                            if (MovementUtils.getJumpBoost() == 0) {
                                if (event.getY() > 0.247 && event.getY() < 0.249) {
                                    event.setY((double) ((int) (client.player.getY() + event.getY())) - client.player.getY());
                                }
                            } else {
                                double yFloor = (int) (client.player.getY() + event.getY());
                                if (yFloor != (double) ((int) client.player.getY())
                                        && client.player.getY() + event.getY() - yFloor < 0.15) {
                                    event.setY(yFloor - client.player.getY());
                                }
                            }
                        }

                        if (client.player.getY() == (double) ((int) client.player.getY())
                                && WorldUtils.isAboveBounds(client.player, 0.001f)) {
                            if (client.options.keyJump.isPressed()) {
                                if (!MovementUtils.isMoving()) {
                                    MovementUtils.strafe(0.0);
                                    MovementUtils.setMotion(event, 0.0);
                                }
                                event.setY(MovementUtils.getJumpValue());
                            } else {
                                event.setY(-1.0E-5);
                            }
                        }
                        break;

                    case "AAC":
                        if (event.getY() > 0.247 && event.getY() < 0.249) {
                            event.setY((double) ((int) (client.player.getY() + event.getY())) - client.player.getY());
                            if (client.options.keyJump.isPressed() && !MovementUtils.isMoving()) {
                                MovementUtils.strafe(0.0);
                                MovementUtils.setMotion(event, 0.0);
                            }
                        } else if (client.player.getY() == (double) ((int) client.player.getY())
                                && WorldUtils.isAboveBounds(client.player, 0.001f)) {
                            event.setY(-1.0E-10);
                        }
                        break;

                    case "Vanilla":
                        if (client.options.keyJump.isPressed()
                                && WorldUtils.isAboveBounds(client.player, 0.001f)
                                && !client.world.getBlockCollisions(client.player, client.player.getBoundingBox().offset(0.0, 1.0, 0.0)).findAny().isPresent()) {
                            client.player.setPosition(client.player.getX(), client.player.getY() + 1.0, client.player.getZ());
                            event.setY(0.0);
                            MovementUtils.setMotion(event, 0.0);
                            setTimer(0.8038576f);
                        }
                }
            }
        } else if (!towerMode.value.equals("AAC")
                || !WorldUtils.isAboveBounds(client.player, 0.001f)
                || !client.options.keyJump.isPressed()) {
            if (!towerMode.value.equals("NCP")
                    && !towerMode.value.equals("Vanilla")
                    && WorldUtils.isAboveBounds(client.player, 0.001f)
                    && client.options.keyJump.isPressed()) {
                client.player.jumpingCooldown = 20;
                event.setY(MovementUtils.getJumpValue());
            }
        } else if (!MovementUtils.isMoving() || moveAndTower.value) {
            client.player.jumpingCooldown = 0;
            client.player.jump();
            MovementUtils.setMotion(event, MovementUtils.getSpeed());
            MovementUtils.strafe(MovementUtils.getSpeed());
        }

        if (!towerMode.value.equals("Vanilla")) {
            MovementUtils.setPlayerYMotion(event.getY());
        }
    }

    public void selectPlaceableHotbarSlot() {
        for (int containerSlot = 36; containerSlot < 45; containerSlot++) {
            int hotbarIndex = containerSlot - 36;

            Slot slot = client.player.playerScreenHandler.getSlot(containerSlot);
            if (!slot.hasStack()) {
                continue;
            }

            ItemStack stack = slot.getStack();
            if (stack.getCount() == 0 || !BlockUtils.isPlacableBlockItem(stack.getItem())) {
                continue;
            }

            if (client.player.inventory.selectedSlot == hotbarIndex) {
                return;
            }

            client.player.inventory.selectedSlot = hotbarIndex;

            if (itemSpoofMode.value.equals("LiteSpoof")
                    && (lastSpoofedSlot < 0 || lastSpoofedSlot != hotbarIndex)) {
                client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(hotbarIndex));
                lastSpoofedSlot = hotbarIndex;
            }

            break;
        }
    }

    public int countPlaceableBlocks() {
        int total = 0;

        for (int slotIndex = 0; slotIndex < 45; slotIndex++) {
            Slot slot = client.player.playerScreenHandler.getSlot(slotIndex);
            if (!slot.hasStack()) {
                continue;
            }

            ItemStack stack = slot.getStack();
            if (BlockUtils.isPlacableBlockItem(stack.getItem())) {
                total += stack.getCount();
            }
        }

        return total;
    }

    public void refillHotbarWithBlocks() {
        String pickingMode = pickMode.value;

        if (pickingMode.equals("OpenInv") && !(client.currentScreen instanceof InventoryScreen)) {
            return;
        }

        if (countPlaceableBlocks() == 0) {
            return;
        }

        int targetContainerSlot = 43; // default hotbar slot (container index), preserved from original
        boolean hasBlocksInHotbar = hasPlaceableBlockInHotbar();

        if (!intelligentBlockPicker.value) {
            if (hasBlocksInHotbar) {
                return;
            }

            int sourceSlot = -1;
            for (int slotIndex = 9; slotIndex < 36; slotIndex++) {
                Slot slot = client.player.playerScreenHandler.getSlot(slotIndex);
                if (slot.hasStack() && BlockUtils.isPlacableBlockItem(slot.getStack().getItem())) {
                    sourceSlot = slotIndex;
                    break;
                }
            }

            for (int slotIndex = 36; slotIndex < 45; slotIndex++) {
                if (!client.player.playerScreenHandler.getSlot(slotIndex).hasStack()) {
                    targetContainerSlot = slotIndex;
                    break;
                }
            }

            if (sourceSlot >= 0) {
                swapSlotToHotbar(sourceSlot, targetContainerSlot - 36);
            }

            return;
        }

        int sourceSlot = findLargestBlockStackSlot();

        if (!hasBlocksInHotbar) {
            for (int slotIndex = 36; slotIndex < 45; slotIndex++) {
                if (!client.player.playerScreenHandler.getSlot(slotIndex).hasStack()) {
                    targetContainerSlot = slotIndex;
                    break;
                }
            }
        } else {
            for (int slotIndex = 36; slotIndex < 45; slotIndex++) {
                Slot slot = client.player.playerScreenHandler.getSlot(slotIndex);
                if (!slot.hasStack()) {
                    continue;
                }

                ItemStack hotbarStack = slot.getStack();
                if (BlockUtils.isPlacableBlockItem(hotbarStack.getItem())) {
                    targetContainerSlot = slotIndex;

                    // If the hotbar stack equals the best stack size, skip swapping (original behavior: set -1).
                    ItemStack sourceStack = client.player.playerScreenHandler.getSlot(sourceSlot).getStack();
                    if (hotbarStack.getCount() == sourceStack.getCount()) {
                        targetContainerSlot = -1;
                    }
                    break;
                }
            }
        }

        if (targetContainerSlot >= 0 && client.player.playerScreenHandler.getSlot(targetContainerSlot).id != sourceSlot) {
            swapSlotToHotbar(sourceSlot, targetContainerSlot - 36);
        }
    }

    private int findLargestBlockStackSlot() {
        if (countPlaceableBlocks() == 0) {
            return -1;
        }

        int bestSlot = -1;
        int bestCount = 0;

        for (int slotIndex = 9; slotIndex < 45; slotIndex++) {
            Slot slot = client.player.playerScreenHandler.getSlot(slotIndex);
            if (!slot.hasStack()) {
                continue;
            }

            ItemStack stack = slot.getStack();
            if (BlockUtils.isPlacableBlockItem(stack.getItem()) && stack.getCount() > bestCount) {
                bestCount = stack.getCount();
                bestSlot = slotIndex;
            }
        }

        return bestSlot;
    }

    private boolean hasPlaceableBlockInHotbar() {
        for (int slotIndex = 36; slotIndex < 45; slotIndex++) {
            Slot slot = client.player.playerScreenHandler.getSlot(slotIndex);
            if (slot.hasStack() && BlockUtils.isPlacableBlockItem(slot.getStack().getItem())) {
                return true;
            }
        }

        return false;
    }

    public boolean canPlaceWithHand(Hand hand) {
        if (!itemSpoofMode.value.equals("None")) {
            return countPlaceableBlocks() != 0;
        }

        return BlockUtils.isPlacableBlockItem(client.player.getStackInHand(hand).getItem());
    }

    private void swapSlotToHotbar(int sourceSlot, int hotbarIndex) {
        client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, sourceSlot, hotbarIndex, SlotActionType.SWAP, client.player);
    }

    private void renderBlockCountJello(int x, int y, float alphaPercent) {
        int width = 0;

        int numberWidth = FontUtils.HELVETICA_LIGHT_18.getWidth(cachedBlockCount + "") + 3;
        width += numberWidth;
        width += FontUtils.HELVETICA_LIGHT_14.getWidth("Blocks");

        int boxWidth = width + 20;
        int boxHeight = 32;

        x -= boxWidth / 2;

        GL11.glPushMatrix();
        RenderUtils.drawFloatingPanel(x, y, boxWidth, boxHeight, ColorHelper.applyAlpha(-15461356, 0.8F * alphaPercent));

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_18,
                (float) (x + 10),
                (float) (y + 4),
                cachedBlockCount + "",
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), alphaPercent));

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (x + 10 + numberWidth),
                (float) (y + 8),
                "Blocks",
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F * alphaPercent));

        int arrowX = x + 11 + boxWidth / 2;
        int arrowY = y + boxHeight;

        GL11.glPushMatrix();
        GL11.glTranslatef((float) arrowX, (float) arrowY, 0.0F);
        GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
        GL11.glTranslatef((float) (-arrowX), (float) (-arrowY), 0.0F);

        RenderUtils.drawImage(
                (float) arrowX,
                (float) arrowY,
                9.0F,
                23.0F,
                Resources.SELECTED_ICON,
                ColorHelper.applyAlpha(-15461356, 0.8F * alphaPercent));

        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
}