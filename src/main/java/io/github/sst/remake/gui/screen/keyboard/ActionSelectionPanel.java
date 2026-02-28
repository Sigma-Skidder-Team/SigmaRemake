package io.github.sst.remake.gui.screen.keyboard;

import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.InteractiveWidget;
import io.github.sst.remake.gui.framework.widget.Button;
import io.github.sst.remake.gui.framework.widget.TextField;
import io.github.sst.remake.gui.framework.widget.VerticalScrollBar;
import io.github.sst.remake.gui.framework.event.BindableActionListener;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.client.ScreenUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.anim.ease.EasingFunctions;
import io.github.sst.remake.util.math.anim.ease.QuadraticEasing;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontAlignment;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ActionSelectionPanel extends InteractiveWidget {
    public AnimationUtils openCloseAnimation;
    public int panelX;
    public int panelY;
    public int panelWidth;
    public int panelHeight;
    public String searchText;
    public ScrollablePanel listPanel;
    public BindableAction selectedBindableAction;
    public boolean closing = false;
    private final List<BindableActionListener> bindableActionListeners = new ArrayList<>();

    public ActionSelectionPanel(GuiComponent parent, String id, int x, int y, int width, int height) {
        super(parent, id, x, y, width, height, false);

        this.panelWidth = 500;
        this.panelHeight = 600;

        this.panelX = (width - this.panelWidth) / 2;
        this.panelY = (height - this.panelHeight) / 2;

        TextField searchField = new TextField(
                this,
                "search",
                this.panelX + 30,
                this.panelY + 30 + 50,
                this.panelWidth - 60,
                60,
                TextField.DEFAULT_COLORS,
                "",
                "Search..."
        );
        this.addToList(searchField);

        searchField.addChangeListener(ignored -> {
            this.searchText = searchField.getText();
            this.listPanel.setScrollOffset(0);
        });
        searchField.requestFocus();

        this.listPanel = new ScrollablePanel(
                this,
                "mods",
                this.panelX + 30,
                this.panelY + 30 + 120,
                this.panelWidth - 60,
                this.panelHeight - 60 - 120
        );
        this.addToList(this.listPanel);

        int yIndex = 10;

        for (Entry<Class<? extends Screen>, String> entry : ScreenUtils.screenToScreenName.entrySet()) {
            BindableAction screenAction = new BindableAction(entry.getKey());

            ColorHelper style = new ColorHelper(
                    ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.02F),
                    -986896
            ).setTextColor(ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F))
                    .setWidthAlignment(FontAlignment.CENTER);

            Button button = new Button(
                    this.listPanel,
                    screenAction.getName(),
                    0,
                    yIndex++ * 55,
                    this.listPanel.getWidth(),
                    55,
                    style,
                    screenAction.getName()
            );
            this.listPanel.addToList(button);

            button.onClick((clicked, mouseButton) -> {
                if (!this.closing) {
                    this.selectedBindableAction = new BindableAction(entry.getKey());
                    this.closing = true;
                }
            });
        }

        yIndex += 50;

        for (Module module : Client.INSTANCE.moduleManager.modules) {
            ColorHelper style = new ColorHelper(16777215, -986896)
                    .setTextColor(ClientColors.DEEP_TEAL.getColor())
                    .setWidthAlignment(FontAlignment.LEFT);

            Button button = new Button(
                    this.listPanel,
                    module.getName(),
                    0,
                    yIndex++ * 40,
                    this.listPanel.getWidth(),
                    40,
                    style,
                    new BindableAction(module).getName()
            );
            this.listPanel.addToList(button);

            button.setTextOffsetX(10);

            button.onClick((clicked, mouseButton) -> {
                if (!this.closing) {
                    this.selectedBindableAction = new BindableAction(module);
                    this.closing = true;
                }
            });
        }

        this.openCloseAnimation = new AnimationUtils(200, 120);
        this.setListening(false);
    }

    @Override
    public void updatePanelDimensions(int mouseX, int mouseY) {
        if (this.isMouseDownOverComponent() && isClickOutsidePanel(mouseX, mouseY)) {
            this.closing = true;
        }

        this.openCloseAnimation.changeDirection(
                this.closing ? AnimationUtils.Direction.FORWARDS : AnimationUtils.Direction.BACKWARDS
        );

        applySearchFilterAndRelayout();

        super.updatePanelDimensions(mouseX, mouseY);
    }

    private boolean isClickOutsidePanel(int mouseX, int mouseY) {
        return mouseX < this.panelX
                || mouseY < this.panelY
                || mouseX > this.panelX + this.panelWidth
                || mouseY > this.panelY + this.panelHeight;
    }

    private void applySearchFilterAndRelayout() {
        Map<String, Button> visibleAll = new TreeMap<>();
        Map<String, Button> visibleStartsWith = new TreeMap<>();
        Map<String, Button> visibleContains = new TreeMap<>();
        List<Button> hidden = new ArrayList<>();

        for (GuiComponent child : this.listPanel.getChildren()) {
            if (child instanceof VerticalScrollBar) {
                continue;
            }

            for (GuiComponent inner : child.getChildren()) {
                if (!(inner instanceof Button)) {
                    continue;
                }

                Button button = (Button) inner;
                boolean isModuleButton = button.getHeight() == 40;

                if (isEmptySearch(this.searchText)) {
                    if (isModuleButton) {
                        visibleAll.put(button.getText(), button);
                    } else {
                        hidden.add(button);
                    }
                    continue;
                }

                if (!isModuleButton) {
                    // TODO: clarify intended search behavior for screen buttons; keeping them visible when searching.
                    visibleAll.put(button.getText(), button);
                    continue;
                }

                if (startsWithIgnoreCase(this.searchText, button.getText())) {
                    visibleStartsWith.put(button.getText(), button);
                } else if (containsIgnoreCase(this.searchText, button.getText())) {
                    visibleContains.put(button.getText(), button);
                } else {
                    hidden.add(button);
                }
            }
        }

        int y = visibleAll.isEmpty() ? 0 : 10;

        for (Button button : visibleAll.values()) {
            button.setSelfVisible(true);
            button.setY(y);
            y += button.getHeight();
        }

        if (!visibleAll.isEmpty()) {
            y += 10;
        }

        for (Button button : visibleStartsWith.values()) {
            button.setSelfVisible(true);
            button.setY(y);
            y += button.getHeight();
        }

        for (Button button : visibleContains.values()) {
            button.setSelfVisible(true);
            button.setY(y);
            y += button.getHeight();
        }

        for (Button button : hidden) {
            button.setSelfVisible(false);
        }
    }

    private boolean isEmptySearch(String query) {
        return query == null || query.length() == 0;
    }

    private boolean containsIgnoreCase(String query, String text) {
        return query == null
                || query.length() == 0
                || text == null
                || text.toLowerCase().contains(query.toLowerCase());
    }

    private boolean startsWithIgnoreCase(String query, String text) {
        return query == null
                || query.length() == 0
                || text == null
                || text.toLowerCase().startsWith(query.toLowerCase());
    }

    @Override
    public void draw(float partialTicks) {
        float anim = this.openCloseAnimation.calcPercent();

        float eased = EasingFunctions.easeOutBack(anim, 0.0F, 1.0F, 1.0F);
        if (this.closing) {
            eased = QuadraticEasing.easeOutQuad(anim, 0.0F, 1.0F, 1.0F);
        }

        this.setScale(0.8F + eased * 0.2F, 0.8F + eased * 0.2F);

        if (anim == 0.0F && this.closing) {
            this.notifyBindableActionSelected(this.selectedBindableAction);
        }

        RenderUtils.drawRoundedRect(
                (float) this.x,
                (float) this.y,
                (float) this.width,
                (float) this.height,
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.3F * anim)
        );

        super.applyScaleTransforms();

        RenderUtils.drawRoundedRect(
                (float) this.panelX,
                (float) this.panelY,
                (float) this.panelWidth,
                (float) this.panelHeight,
                10.0F,
                ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), anim)
        );

        RenderUtils.drawString(
                FontUtils.HELVETICA_LIGHT_36,
                (float) (30 + this.panelX),
                (float) (30 + this.panelY),
                "Select mod to bind",
                ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), anim * 0.7F)
        );

        super.draw(anim);
    }

    public void addBindableActionSelectedListener(BindableActionListener listener) {
        this.bindableActionListeners.add(listener);
    }

    public void notifyBindableActionSelected(BindableAction action) {
        for (BindableActionListener listener : this.bindableActionListeners) {
            listener.onBindableActionSelected(this, action);
        }
    }
}