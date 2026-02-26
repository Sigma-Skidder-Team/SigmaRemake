package io.github.sst.remake.module.impl.movement;

import io.github.sst.remake.data.bus.Subscribe;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.event.impl.game.player.ClientPlayerTickEvent;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.impl.BooleanSetting;
import io.github.sst.remake.setting.impl.ModeSetting;
import io.github.sst.remake.util.game.WorldUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.lwjgl.opengl.GL11;

public class BlockFlyModule extends Module {

    private final ModeSetting itemSpoofMode = new ModeSetting("Item spoof", "Item spoofing mode", 0, "None", "Switch", "Spoof", "LiteSpoof");
    private final ModeSetting towerMode = new ModeSetting("Tower mode", "Towering mode", 0, "None", "NCP", "AAC", "Vanilla");
    private final ModeSetting pickMode = new ModeSetting("Picking mode", "Item picking mode", 0, "Basic", "FakeInv", "OpenInv");
    private final BooleanSetting moveAndTower = new BooleanSetting("Tower while moving", "Allow towering while moving", false);
    private final BooleanSetting showBlockAmount = new BooleanSetting("Show block amount", "Render available blocks in inventory", true);
    private final BooleanSetting intelligentBlockPicker = new BooleanSetting("Intelligent block picker", "Calculate block amount and more", true);
    private final BooleanSetting noSprint = new BooleanSetting("No sprint", "Disable sprinting", false);

    private final AnimationUtils blockCountAnim = new AnimationUtils(114, 114, AnimationUtils.Direction.FORWARDS);
    private int cachedBlockCount = 0;
    private int lastSpoofedSlot;

    public BlockFlyModule() {
        super("BlockFly", "Helps you bridge.", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        this.blockCountAnim.changeDirection(AnimationUtils.Direction.FORWARDS);
    }

    @Subscribe
    public void onTick(ClientPlayerTickEvent event) {
        if (showBlockAmount.value) {
            this.cachedBlockCount = this.countPlaceableBlocks();
        }
    }

    @Subscribe
    public void onRender(RenderClient2DEvent event) {
        this.blockCountAnim.changeDirection(AnimationUtils.Direction.BACKWARDS);
        if (blockCountAnim.calcPercent() != 0.0f && showBlockAmount.value) {
            renderBlockCountJello(
                    client.getWindow().getWidth() / 2,
                    client.getWindow().getHeight() - 138
                            - (int) (25.0F * AnimationUtils.easeOutCubic(this.blockCountAnim.calcPercent(), 0.0F,
                            1.0F, 1.0F)),
                    this.blockCountAnim.calcPercent());
        }
    }

    private void selectPlaceableHotbarSlot() {
        try {
            for (int containerSlot = 36; containerSlot < 45; containerSlot++) {
                int hotbarIndex = containerSlot - 36;

                Slot slot = client.player.playerScreenHandler.getSlot(containerSlot);
                if (!slot.hasStack()) {
                    continue;
                }

                ItemStack stack = slot.getStack();
                if (stack.getCount() == 0 || !WorldUtils.isPlacableBlockItem(stack.getItem())) {
                    continue;
                }

                if (client.player.inventory.selectedSlot == hotbarIndex) {
                    return;
                }

                client.player.inventory.selectedSlot = hotbarIndex;

                if (itemSpoofMode.value.equals("LiteSpoof")
                        && (this.lastSpoofedSlot < 0 || this.lastSpoofedSlot != hotbarIndex)) {
                    client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(hotbarIndex));
                    this.lastSpoofedSlot = hotbarIndex;
                }

                break;
            }
        } catch (Exception ignored) {
        }
    }

    private int countPlaceableBlocks() {
        int total = 0;

        for (int slotIndex = 0; slotIndex < 45; slotIndex++) {
            Slot slot = client.player.playerScreenHandler.getSlot(slotIndex);
            if (!slot.hasStack()) {
                continue;
            }

            ItemStack stack = slot.getStack();
            if (WorldUtils.isPlacableBlockItem(stack.getItem())) {
                total += stack.getCount();
            }
        }

        return total;
    }

    private void refillHotbarWithBlocks() {
        String pickingMode = pickMode.value;

        if (pickingMode.equals("OpenInv") && !(client.currentScreen instanceof InventoryScreen)) {
            return;
        }

        if (this.countPlaceableBlocks() == 0) {
            return;
        }

        int targetContainerSlot = 43; // default hotbar slot (container index), preserved from original
        boolean hasBlocksInHotbar = this.hasPlaceableBlockInHotbar();

        if (!intelligentBlockPicker.value) {
            if (hasBlocksInHotbar) {
                return;
            }

            int sourceSlot = -1;
            for (int slotIndex = 9; slotIndex < 36; slotIndex++) {
                Slot slot = client.player.playerScreenHandler.getSlot(slotIndex);
                if (slot.hasStack() && WorldUtils.isPlacableBlockItem(slot.getStack().getItem())) {
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
                boolean fakeInv = pickingMode.equals("FakeInv") && !(client.currentScreen instanceof InventoryScreen);
                if (fakeInv) {
                    client.getNetworkHandler().sendPacket(new ClientStatusC2SPacket());
                }

                this.swapSlotToHotbar(sourceSlot, targetContainerSlot - 36);

                if (fakeInv) {
                    client.getNetworkHandler().sendPacket(new CloseScreenS2CPacket(-1));
                }
            }

            return;
        }

        int sourceSlot = this.findLargestBlockStackSlot();

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
                if (WorldUtils.isPlacableBlockItem(hotbarStack.getItem())) {
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
            boolean fakeInv = pickingMode.equals("FakeInv") && !(client.currentScreen instanceof InventoryScreen);
            if (fakeInv) {
                client.getNetworkHandler().sendPacket(new ClientStatusC2SPacket());
            }

            this.swapSlotToHotbar(sourceSlot, targetContainerSlot - 36);

            if (fakeInv) {
                client.getNetworkHandler().sendPacket(new CloseScreenS2CPacket(-1));
            }
        }
    }

    private int findLargestBlockStackSlot() {
        if (this.countPlaceableBlocks() == 0) {
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
            if (WorldUtils.isPlacableBlockItem(stack.getItem()) && stack.getCount() > bestCount) {
                bestCount = stack.getCount();
                bestSlot = slotIndex;
            }
        }

        return bestSlot;
    }

    private boolean hasPlaceableBlockInHotbar() {
        for (int slotIndex = 36; slotIndex < 45; slotIndex++) {
            Slot slot = client.player.playerScreenHandler.getSlot(slotIndex);
            if (slot.hasStack() && WorldUtils.isPlacableBlockItem(slot.getStack().getItem())) {
                return true;
            }
        }

        return false;
    }

    private boolean canPlaceWithHand(Hand hand) {
        if (!this.itemSpoofMode.value.equals("None")) {
            return this.countPlaceableBlocks() != 0;
        }

        return WorldUtils.isPlacableBlockItem(client.player.getStackInHand(hand).getItem());
    }

    private void swapSlotToHotbar(int sourceSlot, int hotbarIndex) {
        client.interactionManager.clickSlot(client.player.playerScreenHandler.syncId, sourceSlot, hotbarIndex, SlotActionType.SWAP, client.player);
    }

    private void renderBlockCountJello(int x, int y, float alphaPercent) {
        int width = 0;

        int numberWidth = FontUtils.HELVETICA_LIGHT_18.getWidth(this.cachedBlockCount + "") + 3;
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
                this.cachedBlockCount + "",
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