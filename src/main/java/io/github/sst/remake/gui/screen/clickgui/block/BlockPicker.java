package io.github.sst.remake.gui.screen.clickgui.block;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.widget.TextField;
import io.github.sst.remake.gui.framework.layout.GuiComponentVisitor;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BlockPicker extends InteractiveWidget {
    private final List<String> selectedValues = new ArrayList<>();
    private ScrollablePanel scrollPanel;
    private final TextField searchField;
    private final boolean blocksOnly;

    public BlockPicker(GuiComponent parent, String text, int x, int y, int width, int height, boolean var7, String... var8) {
        super(parent, text, x, y, width, height, false);
        this.blocksOnly = var7;
        this.addToList(this.searchField = new TextField(this, "textbox", 0, 0, width, 32, TextField.DEFAULT_COLORS, "", "Search...", FontUtils.HELVETICA_LIGHT_14));
        this.searchField.setFont(FontUtils.HELVETICA_LIGHT_18);
        this.searchField.addChangeListener(var1x -> this.rebuildItemList(this.searchField.getText()));
        this.setSelectedValues(var8);
        this.rebuildItemList("");
    }

    public void rebuildItemList(String searchTerm) {
        this.addRunnable(() -> {
            if (this.scrollPanel != null) {
                this.removeChildren(this.scrollPanel);
            }

            this.addToList(this.scrollPanel = new ScrollablePanel(this, "scrollview", 0, 40, this.width, this.height - 40));
            List<Item> items = new ArrayList<>();

            for (Item item : Registry.ITEM) {
                items.add(item);
            }

            items.add(new BlockItem(Blocks.NETHER_PORTAL, new Item.Settings().group(ItemGroup.MISC)));
            items.add(new BlockItem(Blocks.END_PORTAL, new Item.Settings().group(ItemGroup.MISC)));

            for (Item item : prioritizeItemsByName(items, searchTerm)) {
                if (item != Items.AIR && (!this.blocksOnly || item instanceof BlockItem)) {
                    Identifier texture = Registry.ITEM.getId(item);
                    String var9;
                    if (item instanceof BlockItem && texture.getPath().equals("air")) {
                        var9 = Registry.BLOCK.getKey(((BlockItem) item).getBlock()).toString();
                    } else {
                        var9 = texture.toString();
                    }

                    BlockItemButton var10;
                    this.scrollPanel.addToList(var10 = new BlockItemButton(this, "btn" + var9, 0, 0, 40, 40, item.getDefaultStack()));
                    var10.setSelected(this.selectedValues.contains(var9), false);
                    var10.onPress(interactiveWidget -> {
                        int var6 = this.selectedValues.size();
                        this.selectedValues.remove(var9);
                        if (var10.isSelected()) {
                            this.selectedValues.add(var9);
                        }

                        if (var6 != this.selectedValues.size()) {
                            this.firePressHandlers();
                        }
                    });
                }
            }

            this.scrollPanel.getContent().accept(new WrapLayoutVisitor(0));
        });
    }

    public static List<Item> prioritizeItemsByName(List<Item> items, String searchTerm) {
        searchTerm = searchTerm.toLowerCase();
        if (!searchTerm.isEmpty()) {
            List<Item> prioritized = new ArrayList<>();
            Iterator<Item> iterator = items.iterator();

            while (iterator.hasNext()) {
                Item item = iterator.next();
                if (item.getName().getString().toLowerCase().startsWith(searchTerm)) {
                    prioritized.add(item);
                    iterator.remove();
                }
            }

            iterator = items.iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                if (item.getName().getString().toLowerCase().contains(searchTerm)) {
                    prioritized.add(item);
                    iterator.remove();
                }
            }

            prioritized.addAll(items);
            return prioritized;
        } else {
            return items;
        }
    }

    @Override
    public void draw(float partialTicks) {
        super.draw(partialTicks);
    }

    public void setSelectedValues(String... values) {
        this.selectedValues.clear();
        this.selectedValues.addAll(Arrays.asList(values));
    }

    public List<String> getSelectedValues() {
        return this.selectedValues;
    }

    public static class WrapLayoutVisitor implements GuiComponentVisitor {
        public int spacing;

        public WrapLayoutVisitor(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void visit(GuiComponent screen) {
            if (!screen.getChildren().isEmpty()) {
                int var4 = 0;
                int var5 = 0;
                int var6 = 0;

                for (int var7 = 0; var7 < screen.getChildren().size(); var7++) {
                    GuiComponent var8 = screen.getChildren().get(var7);
                    if (var4 + var8.getWidth() + this.spacing > screen.getWidth()) {
                        var4 = 0;
                        var5 += var6;
                    }

                    var8.setY(var5);
                    var8.setX(var4);
                    var4 += var8.getWidth() + this.spacing;
                    var6 = Math.max(var8.getHeight(), var6);
                }
            }
        }
    }
}
