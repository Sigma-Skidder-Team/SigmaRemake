package io.github.sst.remake.module.impl.render.nametags;

import io.github.sst.remake.util.IMinecraft;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;

import java.util.Optional;

public class FurnaceTracker implements IMinecraft {
    public int windowId;
    public float smeltProgress;
    public float smeltTime;
    public int cooldown;
    public int smeltDelay;
    public ItemStack inputStack;
    public ItemStack fuelStack;
    public ItemStack outputStack;

    public FurnaceTracker(int windowId) {
        this.windowId = windowId;
    }

    public void updateSmelting() {
        this.refreshOutput();
        boolean hasFuel = this.fuelStack != null && this.fuelStack.getCount() > 0;
        boolean hasInput = this.inputStack != null && this.inputStack.getCount() > 0;
        boolean canSmelt = this.getSmeltingResult() != null
                && this.outputStack != null
                && this.getSmeltingResult().equals(this.outputStack.getItem())
                && this.outputStack.getCount() < 64;

        if (this.smeltTime < this.smeltProgress && hasInput && canSmelt && this.smeltDelay > 0) {
            // Approximate tick-based progress (original used ping, we just increment by 1 per tick)
            this.smeltTime++;
        }

        if (this.smeltDelay > 0) {
            this.smeltDelay--;
        }

        if (this.smeltDelay == 0) {
            if (hasFuel && hasInput) {
                this.fuelStack.decrement(1);
                this.smeltDelay = this.cooldown;
            } else {
                this.smeltTime = 0.0F;
            }
        }

        if (this.smeltTime >= this.smeltProgress && this.smeltProgress != 0.0F) {
            if (hasInput) {
                this.inputStack.decrement(1);
            }

            this.smeltTime = 0.0F;
            if (this.outputStack != null) {
                ItemStack result = this.getSmeltingResultStack();
                if (result != null) {
                    this.outputStack.setCount(this.outputStack.getCount() + result.getCount());
                }
            }
        }

        if (this.inputStack != null && this.inputStack.getCount() == 0) {
            this.inputStack = null;
        }
    }

    public ItemStack getSmeltingResultStack() {
        if (this.inputStack == null || client.getNetworkHandler() == null) {
            return null;
        }

        Optional<SmeltingRecipe> recipe = client.getNetworkHandler().getRecipeManager()
                .getFirstMatch(RecipeType.SMELTING, new SimpleInventory(this.inputStack), client.world);

        if (recipe.isPresent()) {
            ItemStack result = recipe.get().getOutput();
            if (!result.isEmpty()) {
                return result.copy();
            }
        }

        return null;
    }

    public Item getSmeltingResult() {
        ItemStack resultStack = getSmeltingResultStack();
        return resultStack == null ? null : resultStack.getItem();
    }

    public ItemStack refreshOutput() {
        if (outputStack != null && outputStack.getItem() instanceof AirBlockItem) {
            outputStack = null;
        }
        if (inputStack != null && inputStack.getItem() instanceof AirBlockItem) {
            inputStack = null;
        }
        if (fuelStack != null && fuelStack.getItem() instanceof AirBlockItem) {
            fuelStack = null;
        }

        if (outputStack == null) {
            if (inputStack != null) {
                ItemStack result = getSmeltingResultStack();
                if (result != null) {
                    result.setCount(0);
                }
                return outputStack = result;
            } else {
                return null;
            }
        } else {
            return outputStack;
        }
    }
}
