package io.github.sst.remake.gui.element.impl.cgui.block;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.Element;
import io.github.sst.remake.gui.element.impl.TextField;
import io.github.sst.remake.gui.interfaces.ICustomGuiScreenVisitor;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Picker extends Element {
    private final List<String> values = new ArrayList<>();
    private ScrollableContentPanel field20642;
    private final TextField textField;
    private final boolean field20644;

    public Picker(CustomGuiScreen parent, String text, int x, int y, int width, int height, boolean var7, String... var8) {
        super(parent, text, x, y, width, height, false);
        this.field20644 = var7;
        this.addToList(this.textField = new TextField(this, "textbox", 0, 0, width, 32, TextField.field20741, "", "Search...", FontUtils.HELVETICA_LIGHT_14));
        this.textField.setFont(FontUtils.HELVETICA_LIGHT_18);
        this.textField.addChangeListener(var1x -> this.method13069(this.textField.getText()));
        this.method13071(var8);
        this.method13069("");
    }

    public void method13069(String var1) {
        this.addRunnable(() -> {
            if (this.field20642 != null) {
                this.removeChildren(this.field20642);
            }

            this.addToList(this.field20642 = new ScrollableContentPanel(this, "scrollview", 0, 40, this.width, this.height - 40));
            List<Item> items = new ArrayList<>();

            for (Item item : Registries.ITEM) {
                items.add(item);
            }

            // TODO(version/1.19.4): idk
//            items.add(new BlockItem(Blocks.NETHER_PORTAL, new Item.Settings().group(ItemGroups.MISC)));
//            items.add(new BlockItem(Blocks.END_PORTAL, new Item.Settings().group(ItemGroup.MISC)));

            for (Item item : prioritizeItemsByName(items, var1)) {
                if (item != Items.AIR && (!this.field20644 || item instanceof BlockItem)) {
                    Identifier texture = Registries.ITEM.getId(item);
                    String var9;
                    if (item instanceof BlockItem && texture.getPath().equals("air")) {
                        var9 = Registries.BLOCK.getKey(((BlockItem) item).getBlock()).toString();
                    } else {
                        var9 = texture.toString();
                    }

                    BlockButton var10;
                    this.field20642.addToList(var10 = new BlockButton(this, "btn" + var9, 0, 0, 40, 40, item.getDefaultStack()));
                    var10.method13702(this.values.contains(var9), false);
                    var10.onPress(var3 -> {
                        int var6 = this.values.size();
                        this.values.remove(var9);
                        if (var10.method13700()) {
                            this.values.add(var9);
                        }

                        if (var6 != this.values.size()) {
                            this.callUIHandlers();
                        }
                    });
                }
            }

            this.field20642.getButton().accept(new Class7260(0));
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

    public void method13071(String... var1) {
        this.values.clear();
        this.values.addAll(Arrays.asList(var1));
    }

    public List<String> method13072() {
        return this.values;
    }

    public static class Class7260 implements ICustomGuiScreenVisitor {
        public int field31149;

        public Class7260(int var1) {
            this.field31149 = var1;
        }

        @Override
        public void visit(CustomGuiScreen screen) {
            if (!screen.getChildren().isEmpty()) {
                int var4 = 0;
                int var5 = 0;
                int var6 = 0;

                for (int var7 = 0; var7 < screen.getChildren().size(); var7++) {
                    CustomGuiScreen var8 = screen.getChildren().get(var7);
                    if (var4 + var8.getWidth() + this.field31149 > screen.getWidth()) {
                        var4 = 0;
                        var5 += var6;
                    }

                    var8.setY(var5);
                    var8.setX(var4);
                    var4 += var8.getWidth() + this.field31149;
                    var6 = Math.max(var8.getHeight(), var6);
                }
            }
        }
    }
}
