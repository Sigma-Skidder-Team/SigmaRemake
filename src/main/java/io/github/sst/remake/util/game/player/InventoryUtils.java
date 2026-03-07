package io.github.sst.remake.util.game.player;

import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.game.world.BlockUtils;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InventoryUtils implements IMinecraft {

    public static final List<Item> JUNK_ITEMS = new ArrayList<>();

    static {
        JUNK_ITEMS.add(Items.COMPASS);
        JUNK_ITEMS.add(Items.FEATHER);
        JUNK_ITEMS.add(Items.FLINT);
        JUNK_ITEMS.add(Items.EGG);
        JUNK_ITEMS.add(Items.STRING);
        JUNK_ITEMS.add(Items.STICK);
        JUNK_ITEMS.add(Items.TNT);
        JUNK_ITEMS.add(Items.BUCKET);
        JUNK_ITEMS.add(Items.LAVA_BUCKET);
        JUNK_ITEMS.add(Items.WATER_BUCKET);
        JUNK_ITEMS.add(Items.SNOW);
        JUNK_ITEMS.add(Items.ENCHANTED_BOOK);
        JUNK_ITEMS.add(Items.EXPERIENCE_BOTTLE);
        JUNK_ITEMS.add(Items.SHEARS);
        JUNK_ITEMS.add(Items.ANVIL);
        JUNK_ITEMS.add(Items.TORCH);
        JUNK_ITEMS.add(Items.BEETROOT_SEEDS);
        JUNK_ITEMS.add(Items.MELON_SEEDS);
        JUNK_ITEMS.add(Items.PUMPKIN_SEEDS);
        JUNK_ITEMS.add(Items.WHEAT_SEEDS);
        JUNK_ITEMS.add(Items.LEATHER);
        JUNK_ITEMS.add(Items.GLASS_BOTTLE);
        JUNK_ITEMS.add(Items.PISTON);
        JUNK_ITEMS.add(Items.SNOWBALL);
        JUNK_ITEMS.add(Items.FISHING_ROD);
    }

    public static String resolveSelectionId(Item item, Identifier itemId) {
        if (item instanceof BlockItem && "air".equals(itemId.getPath())) {
            return Registry.BLOCK.getKey(((BlockItem) item).getBlock()).toString();
        }
        return itemId.toString();
    }

    public static List<Item> sortItemsBySearchPriority(List<Item> items, String searchTerm) {
        String normalizedSearch = searchTerm.toLowerCase();

        if (normalizedSearch.isEmpty()) {
            return items;
        }

        List<Item> prioritizedItems = new ArrayList<>();

        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            String itemName = item.getName().getString().toLowerCase();

            if (itemName.startsWith(normalizedSearch)) {
                prioritizedItems.add(item);
                iterator.remove();
            }
        }

        iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            String itemName = item.getName().getString().toLowerCase();

            if (itemName.contains(normalizedSearch)) {
                prioritizedItems.add(item);
                iterator.remove();
            }
        }

        prioritizedItems.addAll(items);

        return prioritizedItems;
    }

    public static boolean isJunkItem(ItemStack itemStack, boolean ignoreJunk) {
        Item item = itemStack.getItem();

        if (!ignoreJunk) {
            return false;
        }

        if (item instanceof SwordItem) {
            return !isBestSword(itemStack);
        } else if (item instanceof PickaxeItem) {
            return !isBestPickaxe(itemStack);
        } else if (item instanceof AxeItem) {
            return !isBestAxe(itemStack);
        } else if (item instanceof HoeItem) {
            return !isBestHoe(itemStack);
        } else if (item instanceof PotionItem) {
            return hasNegativePotionEffects(itemStack);
        } else if (item instanceof BlockItem) {
            return !BlockUtils.isPlacableBlockItem(item);
        } else if (item instanceof ArrowItem) {
            return true;
        } else if (item instanceof BowItem) {
            return true;
        } else {
            return JUNK_ITEMS.contains(item) || item.getName().getString().toLowerCase().contains("seed");
        }
    }

    public static boolean isBestSword(ItemStack stack) {
        float stackDamage = calculateItemDamage(stack);

        for (int i = 9; i < 45; i++) {
            Slot slot = client.player.playerScreenHandler.getSlot(i);
            if (!slot.hasStack()) continue;
            ItemStack other = slot.getStack();
            if (other.getItem() instanceof SwordItem) {
                if (calculateItemDamage(other) > stackDamage) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isBestPickaxe(ItemStack stack) {
        float stackEfficiency = getToolEfficiency(stack);

        for (int i = 9; i < 45; i++) {
            Slot slot = client.player.playerScreenHandler.getSlot(i);
            if (!slot.hasStack()) continue;
            ItemStack other = slot.getStack();
            if (other.getItem() instanceof PickaxeItem) {
                if (getToolEfficiency(other) > stackEfficiency) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isBestAxe(ItemStack stack) {
        float stackEfficiency = getToolEfficiency(stack);

        for (int i = 9; i < 45; i++) {
            Slot slot = client.player.playerScreenHandler.getSlot(i);
            if (!slot.hasStack()) continue;
            ItemStack other = slot.getStack();
            if (other.getItem() instanceof AxeItem) {
                if (getToolEfficiency(other) > stackEfficiency) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isBestHoe(ItemStack stack) {
        float stackEfficiency = getToolEfficiency(stack);

        for (int i = 9; i < 45; i++) {
            Slot slot = client.player.playerScreenHandler.getSlot(i);
            if (!slot.hasStack()) continue;
            ItemStack other = slot.getStack();
            if (other.getItem() instanceof HoeItem) {
                if (getToolEfficiency(other) > stackEfficiency) {
                    return false;
                }
            }
        }

        return true;
    }

    public static float calculateItemDamage(ItemStack stack) {
        if (stack.getItem() instanceof SwordItem) {
            return ((SwordItem) stack.getItem()).getAttackDamage();
        } else if (stack.getItem() instanceof ToolItem) {
            return ((ToolItem) stack.getItem()).getMaterial().getAttackDamage();
        }
        return 0;
    }

    public static float getToolEfficiency(ItemStack stack) {
        if (stack.getItem() instanceof ToolItem) {
            return ((ToolItem) stack.getItem()).getMaterial().getMiningSpeedMultiplier();
        }
        return 0;
    }

    public static boolean hasNegativePotionEffects(ItemStack stack) {
        if (stack.getItem() instanceof PotionItem) {
            for (StatusEffectInstance effect : PotionUtil.getPotionEffects(stack)) {
                if (effect.getEffectType() == StatusEffects.POISON
                        || effect.getEffectType() == StatusEffects.INSTANT_DAMAGE
                        || effect.getEffectType() == StatusEffects.SLOWNESS
                        || effect.getEffectType() == StatusEffects.WEAKNESS) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasAllSlotsFilled() {
        for (Slot slot : client.player.playerScreenHandler.slots) {
            if (!slot.hasStack() && slot.id > 8 && slot.id < 45) {
                return false;
            }
        }
        return true;
    }
}