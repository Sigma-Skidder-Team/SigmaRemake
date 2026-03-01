package io.github.sst.remake.gui.screen.clickgui;

import io.github.sst.remake.data.setting.DropdownSetting;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.layout.ContentSize;
import io.github.sst.remake.gui.framework.widget.*;
import io.github.sst.remake.gui.screen.clickgui.block.BlockPicker;
import io.github.sst.remake.gui.screen.clickgui.color.ColorPicker;
import io.github.sst.remake.gui.screen.clickgui.math.BezierCurve;
import io.github.sst.remake.gui.screen.clickgui.slider.SettingSlider;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.setting.Setting;
import io.github.sst.remake.setting.SettingType;
import io.github.sst.remake.setting.impl.*;
import io.github.sst.remake.util.java.RandomUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class ModuleSettingsList extends ScrollablePanel {
    private static final int LABEL_WIDTH = 200;

    private final AnimationUtils tooltipFade = new AnimationUtils(114, 114);
    private String hoveredSettingDescription = "";
    private String hoveredSettingName = "";

    private final HashMap<Text, Setting<?>> labelToSetting = new HashMap<>();
    private final List<SettingEntry> settingEntries = new ArrayList<>();
    private GuiComponent bottomSpacer;
    private final Module module;

    public ModuleSettingsList(GuiComponent parent, String name, int x, int y, int width, int height, Module module) {
        super(parent, name, x, y, width, height);
        this.module = module;

        this.setListening(false);
        this.addSettings();
    }

    private int addSetting(GuiComponent panel, Setting<?> setting, int x, int yOffset, int offset) {
        switch (setting.settingType) {
            case CHECKBOX:
                BooleanSetting bS = (BooleanSetting) setting;

                Text checkBoxText = new Text(panel, bS.name + "lbl", x, yOffset, LABEL_WIDTH, 24, Text.DEFAULT_TEXT_STYLE, bS.name);
                Checkbox checkBox = new Checkbox(panel, bS.name + "checkbox", panel.getWidth() - 24 - offset, yOffset + 6, 24, 24);

                this.labelToSetting.put(checkBoxText, bS);

                checkBox.setValue(bS.value, false);
                bS.addListener(sett -> {
                    BooleanSetting updated = (BooleanSetting) sett;
                    if (checkBox.getValue() != updated.value) {
                        checkBox.setValue(updated.value, false);
                    }
                });

                checkBox.onPress(interactiveWidget -> bS.setValue(((Checkbox) interactiveWidget).getValue()));
                checkBox.addWidthSetter((comp1, comp2) -> comp1.setX(comp2.getWidth() - 24 - offset));
                panel.addToList(checkBoxText);
                panel.addToList(checkBox);
                yOffset += 24 + offset;
                break;

            case SLIDER:
                SliderSetting sS = (SliderSetting) setting;

                Text sliderName = new Text(panel, sS.name + "lbl", x, yOffset, LABEL_WIDTH, 24, Text.DEFAULT_TEXT_STYLE, sS.name);
                this.labelToSetting.put(sliderName, sS);
                SettingSlider slider = new SettingSlider(panel, sS.name + "slider", panel.getWidth() - 126 - offset, yOffset + 6, 126, 24);
                slider.getHandle().setFont(FontUtils.HELVETICA_LIGHT_14);
                slider.setText(Float.toString(sS.value));
                slider.setValue(RandomUtils.normalizeValue(sS.min, sS.max, sS.value), false);
                sS.addListener(sett -> {
                    SliderSetting updated = (SliderSetting) sett;
                    float newValue = RandomUtils.denormalizeValue(
                            slider.getValue(),
                            updated.min,
                            updated.max,
                            updated.increment,
                            updated.getPlaces()
                    );
                    if (newValue != updated.value) {
                        slider.setText(Float.toString(updated.value));
                        slider.setValue(RandomUtils.normalizeValue(updated.min, updated.max, updated.value), false);
                    }
                });
                slider.onPress(interactiveWidget -> {
                    float sliderValue = ((SettingSlider) interactiveWidget).getValue();
                    float newValue = RandomUtils.denormalizeValue(sliderValue, sS.min, sS.max, sS.increment, sS.getPlaces());
                    if (newValue != sS.value) {
                        slider.setText(Float.toString(newValue));
                        sS.setValue(newValue);
                    }
                });
                slider.addWidthSetter((comp1, comp2) -> comp1.setX(comp2.getWidth() - 126 - offset));
                panel.addToList(sliderName);
                panel.addToList(slider);
                yOffset += 24 + offset;
                break;

            case TEXT_INPUT:
                TextInputSetting tIS = (TextInputSetting) setting;
                Text textInputText = new Text(panel, tIS.name + "lbl", x, yOffset, LABEL_WIDTH, 27, Text.DEFAULT_TEXT_STYLE, tIS.name);
                panel.addToList(textInputText);
                this.labelToSetting.put(textInputText, tIS);
                TextField input = new TextField(panel, tIS.name + "txt", panel.getWidth() - offset - 114, yOffset + 27 / 4 - 1, 114, 27, TextField.DEFAULT_COLORS, tIS.value);
                panel.addToList(input);
                input.setFont(FontUtils.HELVETICA_LIGHT_18);
                input.addChangeListener(textField -> tIS.setValue(textField.getText()));

                tIS.addListener(sett -> {
                    TextInputSetting updated = (TextInputSetting) sett;
                    if (!input.getText().equals(updated.value)) {
                        input.setText(updated.value);
                    }
                });

                yOffset += 27 + offset;
                break;

            case DROPDOWN: {
                final int W = 123, H = 27;

                DropdownSetting s = (DropdownSetting) setting;

                Text dropdownText = new Text(panel, s.getName() + "lbl", x, yOffset + 2, LABEL_WIDTH, H,
                        Text.DEFAULT_TEXT_STYLE, s.getName());

                List<String> labels = s.getModeLabels();
                Dropdown dropdown = new Dropdown(panel, s.getName() + "btn",
                        panel.getWidth() - offset, yOffset + 5, W, H,
                        labels, s.getModeIndex());

                this.labelToSetting.put(dropdownText, setting);

                setting.addListener(sett -> {
                    DropdownSetting updated = (DropdownSetting) sett;
                    int newIdx = updated.getModeIndex();
                    if (dropdown.getSelectedIndex() != newIdx) dropdown.setSelectedIndex(newIdx);
                });

                dropdown.onPress(w -> {
                    s.setModeByIndex(((Dropdown) w).getSelectedIndex());
                    dropdown.setSelectedIndex(s.getModeIndex());
                });

                dropdown.addWidthSetter((c1, c2) -> c1.setX(panel.getWidth() - W - offset));
                panel.addToList(dropdownText);
                panel.addToList(dropdown);

                yOffset += H + offset;
                break;
            }

            case GROUP:
                GuiComponent view = new GuiComponent(panel, setting.name + "view", x, yOffset, panel.getWidth(), 0);
                int yOffset2 = 0;

                for (Setting<?> settings : ((GroupSetting) setting).subSettings) {
                    yOffset2 = this.addSetting(view, settings, 0, yOffset2, offset);
                }

                new ContentSize().setWidth(view, panel);
                view.addWidthSetter((comp1, comp2) -> comp1.setWidth(comp2.getWidth() - offset));
                panel.addToList(view);
                yOffset += view.getHeight() + offset;
                break;

            case BLOCKS:
                BlockListSetting bLS = (BlockListSetting) setting;
                Text blocksText = new Text(panel, bLS.name + "lbl", x, yOffset, LABEL_WIDTH, 200, Text.DEFAULT_TEXT_STYLE, bLS.name);
                BlockPicker blockPicker = new BlockPicker(panel, bLS.name + "picker", panel.getWidth() - offset, yOffset + 5, 175, 200, bLS.enabled, bLS.value.toArray(new String[0]));
                this.labelToSetting.put(blocksText, bLS);
                blockPicker.onPress(interactiveWidget -> bLS.setValue(blockPicker.getSelectedIds()));
                blockPicker.addWidthSetter((comp1, comp2) -> comp1.setX(panel.getWidth() - 175 - offset));
                panel.addToList(blocksText);
                panel.addToList(blockPicker);
                yOffset += 200 + offset;
                break;

            case COLOR:
                ColorSetting cS = (ColorSetting) setting;
                Text colorText = new Text(panel, setting.name + "lbl", x, yOffset, LABEL_WIDTH, 24, Text.DEFAULT_TEXT_STYLE, setting.name);
                ColorPicker picker = new ColorPicker(
                        panel, setting.name + "color", panel.getWidth() - 160 - offset + 10, yOffset, 160, 114, (Integer) setting.value, cS.rainbow
                );
                this.labelToSetting.put(colorText, setting);
                cS.addListener(sett -> {
                    ColorSetting settC = (ColorSetting) setting;
                    picker.setValue(settC.value);
                    picker.setRainbowEnabled(settC.rainbow);
                });
                picker.onPress(interactiveWidget -> {
                    cS.setValue(((ColorPicker) interactiveWidget).getValue(), false);
                    cS.rainbow = ((ColorPicker) interactiveWidget).isRainbowEnabled();
                });
                panel.addToList(colorText);
                panel.addToList(picker);
                yOffset += 114 + offset - 10;
                break;

            case CURVE:
                CurveSetting crvS = (CurveSetting) setting;
                CurveSetting.Curve value = crvS.value;
                Text text = new Text(panel, crvS.name + "lbl", x, yOffset, LABEL_WIDTH, 24, Text.DEFAULT_TEXT_STYLE, crvS.name);
                BezierCurve curve = new BezierCurve(panel, crvS.name + "color", panel.getWidth() - 150 - offset + 10, yOffset, 150, 150, 20, value.initial, value.mid, value.finalStage, value.maximum);
                this.labelToSetting.put(text, setting);
                crvS.addListener(sett -> {
                    CurveSetting settC = (CurveSetting) sett;
                    CurveSetting.Curve profile = settC.value;
                    curve.setCurveValues(profile.initial, profile.mid, profile.finalStage, profile.maximum);
                });
                curve.onPress(interactiveWidget ->
                        crvS.setValue(curve.getCurveValues()[0], curve.getCurveValues()[1], curve.getCurveValues()[2], curve.getCurveValues()[3])
                );
                panel.addToList(text);
                panel.addToList(curve);
                yOffset += 150 + offset - 10;
                break;
        }

        return yOffset - (offset - 10);
    }

    private void addSettings() {
        int yOffset = 20;

        for (Setting<?> setting : this.module.settings) {
            GuiComponent container = new SettingRow(this, setting.name + "row", 0, yOffset, this.getWidth(), 0);
            int height = this.addSetting(container, setting, 20, 0, 20);

            container.setHeight(height);
            container.addWidthSetter((forScreen, fromWidthOfThisScreen) -> forScreen.setWidth(fromWidthOfThisScreen.getWidth()));

            this.addToList(container);

            Dropdown dropdown = null;

            if (setting.settingType == SettingType.DROPDOWN) {
                GuiComponent child = container.getChildByName(setting.name + "btn");
                if (child instanceof Dropdown) {
                    dropdown = (Dropdown) child;
                }
            }
            this.settingEntries.add(new SettingEntry(setting, container, height, dropdown));

            yOffset += height;
        }

        this.bottomSpacer = new GuiComponent(this, "extentionhack", 0, yOffset, 0, 20);
        this.addToList(this.bottomSpacer);
        this.updateSettingVisibilityAndLayout();
    }

    private void updateSettingVisibilityAndLayout() {
        int yOffset = 20;

        for (SettingEntry entry : this.settingEntries) {
            boolean hidden = entry.setting.isHidden();

            entry.container.setSelfVisible(!hidden);
            entry.container.setY(yOffset);

            if (entry.dropdown != null) {
                entry.container.setReAddChildren(entry.dropdown.isExpanded());
            } else {
                entry.container.setReAddChildren(false);
            }

            if (hidden) {
                entry.container.setHeight(0);
            } else {
                entry.container.setHeight(entry.height);
                yOffset += entry.height;
            }
        }

        if (this.bottomSpacer != null) {
            this.bottomSpacer.setY(yOffset);
            this.bottomSpacer.setHeight(20);
        }
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        this.updateSettingVisibilityAndLayout();
        super.updatePanelDimensions(mouseX, mouseY);
    }

    @Override
    public void draw(float partialTicks) {
        boolean visible = false;

        for (Entry<Text, Setting<?>> entry : this.labelToSetting.entrySet()) {
            Text text = entry.getKey();
            Setting<?> setting = entry.getValue();

            if (text.isHoveredInHierarchy() && text.isVisible()) {
                visible = true;
                this.hoveredSettingDescription = setting.description;
                this.hoveredSettingName = setting.name;
                break;
            }
        }

        GL11.glPushMatrix();
        super.draw(partialTicks);
        GL11.glPopMatrix();
        this.tooltipFade.changeDirection(!visible ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS);
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

    private static final class SettingEntry {
        private final Setting<?> setting;
        private final GuiComponent container;
        private final int height;
        private final Dropdown dropdown;

        private SettingEntry(Setting<?> setting, GuiComponent container, int height, Dropdown dropdown) {
            this.setting = setting;
            this.container = container;
            this.height = height;
            this.dropdown = dropdown;
        }
    }

    private static final class SettingRow extends GuiComponent {
        private SettingRow(GuiComponent parent, String name, int x, int y, int width, int height) {
            super(parent, name, x, y, width, height);
        }

        @Override
        public boolean isMouseOverComponent(int mouseX, int mouseY) {
            if (super.isMouseOverComponent(mouseX, mouseY)) {
                return true;
            }

            for (GuiComponent child : this.getChildren()) {
                if (child.isSelfVisible() && child.isMouseOverComponent(mouseX, mouseY)) {
                    return true;
                }
            }

            return false;
        }
    }

}
