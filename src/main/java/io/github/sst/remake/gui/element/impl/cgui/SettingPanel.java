package io.github.sst.remake.gui.element.impl.cgui;

import io.github.sst.remake.gui.ContentSize;
import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.element.impl.Checkbox;
import io.github.sst.remake.gui.element.impl.Dropdown;
import io.github.sst.remake.gui.element.impl.Text;
import io.github.sst.remake.gui.element.impl.TextField;
import io.github.sst.remake.gui.element.impl.cgui.block.Picker;
import io.github.sst.remake.gui.element.impl.cgui.color.ColorPicker;
import io.github.sst.remake.gui.element.impl.cgui.slider.Slider;
import io.github.sst.remake.gui.interfaces.Animatable;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.impl.*;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map.Entry;

public class SettingPanel extends ScrollableContentPanel implements Animatable {
    private final Module module;
    public int field21222 = 200;
    private final HashMap<Text, Setting> field21223 = new HashMap<>();
    public HashMap<Module, CustomGuiScreen> field21224 = new HashMap<>();
    public AnimationUtils field21225 = new AnimationUtils(114, 114);
    private String field21226 = "";
    private String field21227 = "";

    public SettingPanel(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, Module module) {
        super(var1, var2, var3, var4, var5, var6);
        this.module = module;
        this.setListening(false);
        this.addSettings();
    }

    private int addSetting(CustomGuiScreen panel, Setting setting, int var3, int var4, int var5) {
        switch (setting.settingType) {
            case CHECKBOX:
                Text var37 = new Text(panel, setting.name + "lbl", var3, var4, this.field21222, 24, Text.defaultColorHelper, setting.name);
                Checkbox var45 = new Checkbox(panel, setting.name + "checkbox", panel.getWidth() - 24 - var5, var4 + 6, 24, 24);
                this.field21223.put(var37, setting);
                var45.method13705((Boolean) setting.value, false);
                setting.addListener(var1x -> {
                    if (var45.getValue() != (Boolean) var1x.value) {
                        var45.method13705((Boolean) var1x.value, false);
                    }
                });
                var45.onPress(element -> setting.setValue(((Checkbox) element).getValue()));
                var45.addWidthSetter((var1x, var2x) -> var1x.setX(var2x.getWidth() - 24 - var5));
                panel.addToList(var37);
                panel.addToList(var45);
                var4 += 24 + var5;
                break;
            case SLIDER:
                Text var36 = new Text(panel, setting.name + "lbl", var3, var4, this.field21222, 24, Text.defaultColorHelper, setting.name);
                this.field21223.put(var36, setting);
                SliderSetting numbaSetting = (SliderSetting) setting;
                Slider var47 = new Slider(panel, setting.name + "slider", panel.getWidth() - 126 - var5, var4 + 6, 126, 24);
                var47.method13137().setFont(FontUtils.HELVETICA_LIGHT_14);
                var47.setText(Float.toString((Float) setting.value));
                var47.method13140(Slider.method13134(numbaSetting.min, numbaSetting.max, numbaSetting.value), false);
                var47.method13143(-1.0F);
                int var13 = numbaSetting.getPlaces();
                numbaSetting.addListener(
                        var3x -> {
                            if (Slider.method13135(var47.method13138(), numbaSetting.min, numbaSetting.max, numbaSetting.increment, var13)
                                    != (Float) var3x.value) {
                                var47.setText(Float.toString((Float) var3x.value));
                                var47.method13140(Slider.method13134(numbaSetting.min, numbaSetting.max, var3x.value), false);
                            }
                        }
                );
                var47.onPress(var4x -> {
                    float var7 = ((Slider) var4x).method13138();
                    float var8x = Slider.method13135(var7, numbaSetting.min, numbaSetting.max, numbaSetting.increment, var13);
                    if (var8x != (Float) setting.value) {
                        var47.setText(Float.toString(var8x));
                        setting.setValue(var8x);
                    }
                });
                var47.addWidthSetter((var1x, var2x) -> var1x.setX(var2x.getWidth() - 126 - var5));
                panel.addToList(var36);
                panel.addToList(var47);
                var4 += 24 + var5;
                break;
            case TEXT_INPUT:
                int var19 = 114;
                int var27 = 27;
                Text var43;
                this.addToList(
                        var43 = new Text(panel, setting.name + "lbl", var3, var4, this.field21222, var27, Text.defaultColorHelper, setting.name)
                );
                this.field21223.put(var43, setting);
                TextField var35;
                this.addToList(
                        var35 = new TextField(
                                panel,
                                setting.name + "txt",
                                panel.getWidth() - var5 - var19,
                                var4 + var27 / 4 - 1,
                                var19,
                                var27,
                                TextField.field20741,
                                (String) setting.value
                        )
                );
                var35.setFont(FontUtils.HELVETICA_LIGHT_18);
                var35.addChangeListener(var1x -> setting.setValue(var1x.getText()));
                setting.addListener(var2x -> {
                    if (var35.getText() != setting.value) {
                        var35.setText((String) setting.value);
                    }
                });
                var4 += var27 + var5;
                break;
            case DROPDOWN:
                Text var34 = new Text(panel, setting.name + "lbl", var3, var4 + 2, this.field21222, 27, Text.defaultColorHelper, setting.name);
                Dropdown var42 = new Dropdown(
                        panel,
                        setting.name + "btn",
                        panel.getWidth() - var5,
                        var4 + 6 - 1,
                        123,
                        27,
                        ((ModeSetting) setting).modes,
                        ((ModeSetting) setting).getModeIndex()
                );
                this.field21223.put(var34, setting);
                setting.addListener(var2x -> {
                    if (var42.getIndex() != ((ModeSetting) setting).getModeIndex()) {
                        var42.method13656(((ModeSetting) setting).getModeIndex());
                    }
                });
                var42.onPress(var2x -> {
                    ((ModeSetting) setting).setModeByIndex(((Dropdown) var2x).getIndex());
                    var42.method13656(((ModeSetting) setting).getModeIndex());
                });
                var42.addWidthSetter((var2x, var3x) -> var2x.setX(panel.getWidth() - 123 - var5));
                panel.addToList(var34);
                panel.addToList(var42);
                var4 += 27 + var5;
                break;
            case GROUP:
                CustomGuiScreen var17 = new CustomGuiScreen(panel, setting.name + "view", var3, var4, panel.getWidth(), 0);
                int var25 = 0;

                for (Setting<?> var41 : ((GroupSetting) setting).subSettings) {
                    var25 = this.addSetting(var17, var41, 0, var25, var5);
                }

                new ContentSize().setWidth(var17, panel);
                var17.addWidthSetter((var1x, var2x) -> var1x.setWidth(var2x.getWidth() - var5));
                panel.addToList(var17);
                var4 += var17.getHeight() + var5;
                break;
            case BLOCKS:
                Text var31 = new Text(panel, setting.name + "lbl", var3, var4, this.field21222, 200, Text.defaultColorHelper, setting.name);
                Picker var39 = new Picker(
                        panel,
                        setting.name + "picker",
                        panel.getWidth() - var5,
                        var4 + 5,
                        175,
                        200,
                        ((BlockListSetting) setting).enabled,
                        ((BlockListSetting) setting).value.toArray(new String[0])
                );
                this.field21223.put(var31, setting);
                var39.onPress(var2x -> setting.setValue(var39.method13072()));
                var39.addWidthSetter((var2x, var3x) -> var2x.setX(panel.getWidth() - 175 - var5));
                panel.addToList(var31);
                panel.addToList(var39);
                var4 += 200 + var5;
                break;
            case COLOR:
                ColorSetting var30 = (ColorSetting) setting;
                Text var38 = new Text(panel, setting.name + "lbl", var3, var4, this.field21222, 24, Text.defaultColorHelper, setting.name);
                ColorPicker var46 = new ColorPicker(
                        panel, setting.name + "color", panel.getWidth() - 160 - var5 + 10, var4, 160, 114, (Integer) setting.value, var30.rainbow
                );
                this.field21223.put(var38, setting);
                setting.addListener(var3x -> {
                    var46.method13048((Integer) setting.value);
                    var46.method13046(var30.rainbow);
                });
                var46.onPress(var2x -> {
                    setting.setValue(((ColorPicker) var2x).method13049(), false);
                    var30.rainbow = ((ColorPicker) var2x).method13047();
                });
                panel.addToList(var38);
                panel.addToList(var46);
                var4 += 114 + var5 - 10;
                break;
        }

        return var4 - (var5 - 10);
    }

    private void addSettings() {
        int yOffset = 20;

        for (Setting<?> setting : this.module.settings) {
            if (setting.isHidden()) continue;

            yOffset = this.addSetting(this, setting, 20, yOffset, 20);
        }

        int var17 = yOffset;
        /*
        if (this.module instanceof ModuleWithModuleSettings var18) {

            for (Module var10 : var18.moduleArray) {
                int var11 = 0;
                CustomGuiScreen var12 = new CustomGuiScreen(this, var10.getName() + "SubView", 0, var17, this.width, this.height - yOffset);
                var12.setSize((var0, var1) -> var0.setWidth(var1.getWidth()));

                for (Setting<?> var14 : var10.settings) {
                    var11 = this.method13531(var12, var14, 20, var11, 20);
                }

                yOffset = Math.max(yOffset + var11, yOffset);

                for (CustomGuiScreen var20 : var12.getChildren()) {
                    if (var20 instanceof Dropdown) {
                        Dropdown var15 = (Dropdown) var20;
                        int var16 = var15.method13649() + var15.getY() + var15.getHeight() + 14;
                        var11 = Math.max(var11, var16);
                    }
                }

                var12.setHeight(var11);
                this.addToList(var12);
                this.field21224.put(var10, var12);
            }

            var18.addModuleStateListener((parent, module, enabled) -> this.field21224.get(module).setSelfVisible(enabled));
            var18.calledOnEnable();
        }

         */

        this.addToList(new CustomGuiScreen(this, "extentionhack", 0, yOffset, 0, 20));
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        boolean var4 = false;

        for (Entry var6 : this.field21223.entrySet()) {
            Text var7 = (Text) var6.getKey();
            Setting var8 = (Setting) var6.getValue();
            if (var7.isHoveredInHierarchy() && var7.isVisible()) {
                var4 = true;
                this.field21226 = var8.description;
                this.field21227 = var8.name;
                break;
            }
        }

        GL11.glPushMatrix();
        super.draw(partialTicks);
        GL11.glPopMatrix();
        this.field21225.changeDirection(!var4 ? AnimationUtils.Direction.BACKWARDS : AnimationUtils.Direction.FORWARDS);
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + 10),
                (float) (this.getY() + this.getHeight() + 24),
                this.field21227,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * this.field21225.calcPercent())
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + 11),
                (float) (this.getY() + this.getHeight() + 24),
                this.field21227,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * this.field21225.calcPercent())
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + 14 + FontUtils.HELVETICA_LIGHT_14.getWidth(this.field21227) + 2),
                (float) (this.getY() + this.getHeight() + 24),
                this.field21226,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * this.field21225.calcPercent())
        );
    }

    @Override
    public boolean shouldAnimate() {
        return false;
    }

}
