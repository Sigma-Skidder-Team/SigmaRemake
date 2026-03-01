package io.github.sst.remake.util.game;

import io.github.sst.remake.util.IMinecraft;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InventoryUtils implements IMinecraft {
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
}