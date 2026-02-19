package io.github.sst.remake.gui.screen.clickgui;

import io.github.sst.remake.gui.framework.layout.ContentSize;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.widget.Checkbox;
import io.github.sst.remake.gui.framework.widget.Dropdown;
import io.github.sst.remake.gui.framework.widget.Text;
import io.github.sst.remake.gui.framework.widget.TextField;
import io.github.sst.remake.gui.screen.clickgui.block.BlockPicker;
import io.github.sst.remake.gui.screen.clickgui.color.ColorPicker;
import io.github.sst.remake.gui.screen.clickgui.math.BezierCurve;
import io.github.sst.remake.gui.screen.clickgui.slider.SettingSlider;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
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

public class ModuleSettingsList extends ScrollablePanel {
    private final Module module;
    private int labelWidth = 200;
    private final HashMap<Text, Setting> labelToSetting = new HashMap<>();
    //public HashMap<Module, GuiComponent> field21224 = new HashMap<>();
    private final AnimationUtils tooltipFade = new AnimationUtils(114, 114);
    private String hoveredSettingDescription = "";
    private String hoveredSettingName = "";

    public ModuleSettingsList(GuiComponent var1, String var2, int var3, int var4, int var5, int var6, Module module) {
        super(var1, var2, var3, var4, var5, var6);
        this.module = module;
        this.setListening(false);
        this.addSettings();
    }

    private int addSetting(GuiComponent panel, Setting setting, int var3, int yOffset, int var5) {
        switch (setting.settingType) {
            case CHECKBOX:
                Text checkBoxText = new Text(panel, setting.name + "lbl", var3, yOffset, this.labelWidth, 24, Text.defaultColorHelper, setting.name);
                Checkbox checkBox = new Checkbox(panel, setting.name + "checkbox", panel.getWidth() - 24 - var5, yOffset + 6, 24, 24);
                this.labelToSetting.put(checkBoxText, setting);
                checkBox.setValue((Boolean) setting.value, false);
                setting.addListener(var1x -> {
                    if (checkBox.getValue() != (Boolean) var1x.value) {
                        checkBox.setValue((Boolean) var1x.value, false);
                    }
                });
                checkBox.onPress(element -> setting.setValue(((Checkbox) element).getValue()));
                checkBox.addWidthSetter((var1x, var2x) -> var1x.setX(var2x.getWidth() - 24 - var5));
                panel.addToList(checkBoxText);
                panel.addToList(checkBox);
                yOffset += 24 + var5;
                break;
            case SLIDER:
                Text sliderName = new Text(panel, setting.name + "lbl", var3, yOffset, this.labelWidth, 24, Text.defaultColorHelper, setting.name);
                this.labelToSetting.put(sliderName, setting);
                SliderSetting sliderSetting = (SliderSetting) setting;
                SettingSlider slider = new SettingSlider(panel, setting.name + "slider", panel.getWidth() - 126 - var5, yOffset + 6, 126, 24);
                slider.getHandle().setFont(FontUtils.HELVETICA_LIGHT_14);
                slider.setText(Float.toString((Float) setting.value));
                slider.setValue(SettingSlider.normalizeValue(sliderSetting.min, sliderSetting.max, sliderSetting.value), false);
                slider.setSnapValue(-1.0F);
                int var13 = sliderSetting.getPlaces();
                sliderSetting.addListener(
                        lisSetting -> {
                            if (SettingSlider.denormalizeValue(slider.getValue(), sliderSetting.min, sliderSetting.max, sliderSetting.increment, var13)
                                    != (Float) lisSetting.value) {
                                slider.setText(Float.toString((Float) lisSetting.value));
                                slider.setValue(SettingSlider.normalizeValue(sliderSetting.min, sliderSetting.max, lisSetting.value), false);
                            }
                        }
                );
                slider.onPress(widget -> {
                    float var7 = ((SettingSlider) widget).getValue();
                    float var8x = SettingSlider.denormalizeValue(var7, sliderSetting.min, sliderSetting.max, sliderSetting.increment, var13);
                    if (var8x != (Float) setting.value) {
                        slider.setText(Float.toString(var8x));
                        setting.setValue(var8x);
                    }
                });
                slider.addWidthSetter((var1x, var2x) -> var1x.setX(var2x.getWidth() - 126 - var5));
                panel.addToList(sliderName);
                panel.addToList(slider);
                yOffset += 24 + var5;
                break;
            case TEXT_INPUT:
                int var19 = 114;
                int var27 = 27;
                Text textInputText;
                this.addToList(
                        textInputText = new Text(panel, setting.name + "lbl", var3, yOffset, this.labelWidth, var27, Text.defaultColorHelper, setting.name)
                );
                this.labelToSetting.put(textInputText, setting);
                TextField input;
                this.addToList(
                        input = new TextField(
                                panel,
                                setting.name + "txt",
                                panel.getWidth() - var5 - var19,
                                yOffset + var27 / 4 - 1,
                                var19,
                                var27,
                                TextField.DEFAULT_COLORS,
                                (String) setting.value
                        )
                );
                input.setFont(FontUtils.HELVETICA_LIGHT_18);
                input.addChangeListener(var1x -> setting.setValue(var1x.getText()));
                setting.addListener(ignored -> {
                    if (input.getText() != setting.value) {
                        input.setText((String) setting.value);
                    }
                });
                yOffset += var27 + var5;
                break;
            case DROPDOWN:
                Text dropdownText = new Text(panel, setting.name + "lbl", var3, yOffset + 2, this.labelWidth, 27, Text.defaultColorHelper, setting.name);
                Dropdown dropdown = new Dropdown(
                        panel,
                        setting.name + "btn",
                        panel.getWidth() - var5,
                        yOffset + 6 - 1,
                        123,
                        27,
                        ((ModeSetting) setting).modes,
                        ((ModeSetting) setting).getModeIndex()
                );
                this.labelToSetting.put(dropdownText, setting);
                setting.addListener(ignored -> {
                    if (dropdown.getIndex() != ((ModeSetting) setting).getModeIndex()) {
                        dropdown.setIndex(((ModeSetting) setting).getModeIndex());
                    }
                });
                dropdown.onPress(widget -> {
                    ((ModeSetting) setting).setModeByIndex(((Dropdown) widget).getIndex());
                    dropdown.setIndex(((ModeSetting) setting).getModeIndex());
                });
                dropdown.addWidthSetter((var2x, var3x) -> var2x.setX(panel.getWidth() - 123 - var5));
                panel.addToList(dropdownText);
                panel.addToList(dropdown);
                yOffset += 27 + var5;
                break;
            case GROUP:
                GuiComponent view = new GuiComponent(panel, setting.name + "view", var3, yOffset, panel.getWidth(), 0);
                int yOffset2 = 0;

                for (Setting<?> settings : ((GroupSetting) setting).subSettings) {
                    yOffset2 = this.addSetting(view, settings, 0, yOffset2, var5);
                }

                new ContentSize().setWidth(view, panel);
                view.addWidthSetter((var1x, var2x) -> var1x.setWidth(var2x.getWidth() - var5));
                panel.addToList(view);
                yOffset += view.getHeight() + var5;
                break;
            case BLOCKS:
                Text blocksText = new Text(panel, setting.name + "lbl", var3, yOffset, this.labelWidth, 200, Text.defaultColorHelper, setting.name);
                BlockPicker blockPicker = new BlockPicker(
                        panel,
                        setting.name + "picker",
                        panel.getWidth() - var5,
                        yOffset + 5,
                        175,
                        200,
                        ((BlockListSetting) setting).enabled,
                        ((BlockListSetting) setting).value.toArray(new String[0])
                );
                this.labelToSetting.put(blocksText, setting);
                blockPicker.onPress(widget -> setting.setValue(blockPicker.getSelectedValues()));
                blockPicker.addWidthSetter((var2x, var3x) -> var2x.setX(panel.getWidth() - 175 - var5));
                panel.addToList(blocksText);
                panel.addToList(blockPicker);
                yOffset += 200 + var5;
                break;
            case COLOR:
                ColorSetting colorSetting = (ColorSetting) setting;
                Text colorText = new Text(panel, setting.name + "lbl", var3, yOffset, this.labelWidth, 24, Text.defaultColorHelper, setting.name);
                ColorPicker picker = new ColorPicker(
                        panel, setting.name + "color", panel.getWidth() - 160 - var5 + 10, yOffset, 160, 114, (Integer) setting.value, colorSetting.rainbow
                );
                this.labelToSetting.put(colorText, setting);
                setting.addListener(var3x -> {
                    picker.setValue((Integer) setting.value);
                    picker.setRainbow(colorSetting.rainbow);
                });
                picker.onPress(widget -> {
                    setting.setValue(((ColorPicker) widget).getValue(), false);
                    colorSetting.rainbow = ((ColorPicker) widget).getRainbow();
                });
                panel.addToList(colorText);
                panel.addToList(picker);
                yOffset += 114 + var5 - 10;
                break;
            case CURVE:
                CurveSetting.Curve speedSetting = (CurveSetting.Curve) setting.value;
                Text text = new Text(panel, setting.name + "lbl", var3, yOffset, this.labelWidth, 24, Text.defaultColorHelper, setting.name);
                BezierCurve curve = new BezierCurve(
                        panel,
                        setting.name + "color",
                        panel.getWidth() - 150 - var5 + 10,
                        yOffset,
                        150,
                        150,
                        20,
                        speedSetting.initial,
                        speedSetting.mid,
                        speedSetting.finalStage,
                        speedSetting.maximum
                );
                this.labelToSetting.put(text, setting);
                setting.addListener(ignored -> {
                    CurveSetting.Curve profile = (CurveSetting.Curve) setting.value;
                    curve.setCurveValues(profile.initial, profile.mid, profile.finalStage, profile.maximum);
                });
                curve.onPress(
                        widget -> ((CurveSetting) setting).setValue(curve.getCurveValues()[0], curve.getCurveValues()[1], curve.getCurveValues()[2], curve.getCurveValues()[3])
                );
                panel.addToList(text);
                panel.addToList(curve);
                yOffset += 150 + var5 - 10;
                break;
        }

        return yOffset - (var5 - 10);
    }

    private void addSettings() {
        int yOffset = 20;

        for (Setting<?> setting : this.module.settings) {
            if (setting.isHidden()) continue;

            yOffset = this.addSetting(this, setting, 20, yOffset, 20);
        }

        /*
        int var17 = yOffset;
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

        this.addToList(new GuiComponent(this, "extentionhack", 0, yOffset, 0, 20));
    }

    @Override
    public void draw(float partialTicks) {
        boolean var4 = false;

        for (Entry var6 : this.labelToSetting.entrySet()) {
            Text var7 = (Text) var6.getKey();
            Setting var8 = (Setting) var6.getValue();
            if (var7.isHoveredInHierarchy() && var7.isVisible()) {
                var4 = true;
                this.hoveredSettingDescription = var8.description;
                this.hoveredSettingName = var8.name;
                break;
            }
        }

        GL11.glPushMatrix();
        super.draw(partialTicks);
        GL11.glPopMatrix();
        this.tooltipFade.changeDirection(!var4 ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + 10),
                (float) (this.getY() + this.getHeight() + 24),
                this.hoveredSettingName,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * this.tooltipFade.calcPercent())
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + 11),
                (float) (this.getY() + this.getHeight() + 24),
                this.hoveredSettingName,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * this.tooltipFade.calcPercent())
        );
        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_14,
                (float) (this.getX() + 14 + FontUtils.HELVETICA_LIGHT_14.getWidth(this.hoveredSettingName) + 2),
                (float) (this.getY() + this.getHeight() + 24),
                this.hoveredSettingDescription,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.5F * this.tooltipFade.calcPercent())
        );
    }

}
