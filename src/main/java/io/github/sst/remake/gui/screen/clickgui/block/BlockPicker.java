package io.github.sst.remake.gui.screen.clickgui.block;

import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.layout.FlowWrapLayoutVisitor;
import io.github.sst.remake.gui.framework.widget.TextField;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.util.game.InventoryUtils;
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
import java.util.List;

public class BlockPicker extends InteractiveWidget {
    private final List<String> selectedIds = new ArrayList<>();
    private ScrollablePanel itemScrollPanel;
    private final TextField searchField;
    private final boolean blocksOnly;

    public BlockPicker(GuiComponent parent, String title, int x, int y, int width, int height, boolean blocksOnly, String... initialSelectedIds) {
        super(parent, title, x, y, width, height, false);

        this.blocksOnly = blocksOnly;

        this.searchField = new TextField(this, "searchField", 0, 0, width, 32, TextField.DEFAULT_COLORS, "", "Search...");
        this.addToList(this.searchField);

        this.searchField.setFont(FontUtils.HELVETICA_LIGHT_18);
        this.searchField.addChangeListener(ignored -> this.rebuildItemList(this.searchField.getText()));

        this.setSelectedValues(initialSelectedIds);
        this.rebuildItemList("");
    }

    public void rebuildItemList(String searchTerm) {
        this.addRunnable(() -> {
            if (this.itemScrollPanel != null) {
                this.removeChildren(this.itemScrollPanel);
            }

            this.itemScrollPanel = new ScrollablePanel(this, "itemScrollPanel", 0, 40, this.width, this.height - 40);
            this.addToList(this.itemScrollPanel);

            List<Item> allItems = new ArrayList<>();
            for (Item item : Registry.ITEM) {
                allItems.add(item);
            }

            allItems.add(new BlockItem(Blocks.NETHER_PORTAL, new Item.Settings().group(ItemGroup.MISC)));
            allItems.add(new BlockItem(Blocks.END_PORTAL, new Item.Settings().group(ItemGroup.MISC)));

            for (Item item : InventoryUtils.sortItemsBySearchPriority(allItems, searchTerm)) {
                if (item == Items.AIR) {
                    continue;
                }
                if (this.blocksOnly && !(item instanceof BlockItem)) {
                    continue;
                }

                Identifier itemId = Registry.ITEM.getId(item);
                String selectionId = InventoryUtils.resolveSelectionId(item, itemId);

                BlockItemButton button = new BlockItemButton(
                        this,
                        "btn_" + selectionId,
                        0,
                        0,
                        40,
                        40,
                        item.getDefaultStack()
                );
                this.itemScrollPanel.addToList(button);

                button.setSelected(this.selectedIds.contains(selectionId), false);
                button.onPress(clicked -> {
                    int oldSize = this.selectedIds.size();

                    this.selectedIds.remove(selectionId);
                    if (button.isSelected()) {
                        this.selectedIds.add(selectionId);
                    }

                    if (oldSize != this.selectedIds.size()) {
                        this.firePressHandlers();
                    }
                });
            }

            this.itemScrollPanel.getContent().accept(new FlowWrapLayoutVisitor(0));
        });
    }

    @Override
    public void draw(float partialTicks) {
        super.draw(partialTicks);
    }

    public void setSelectedValues(String... values) {
        this.selectedIds.clear();
        this.selectedIds.addAll(Arrays.asList(values));
    }

    public List<String> getSelectedIds() {
        return this.selectedIds;
    }
}