package io.github.sst.remake.gui.framework.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.Client;
import io.github.sst.remake.gui.framework.widget.TextField;
import io.github.sst.remake.gui.framework.layout.GuiComponentVisitor;
import io.github.sst.remake.gui.framework.event.InputListener;
import io.github.sst.remake.gui.framework.layout.WidthSetter;
import io.github.sst.remake.gui.framework.widget.ScrollablePanel;
import io.github.sst.remake.util.system.io.GsonUtils;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.font.FontUtils;
import lombok.Getter;
import lombok.Setter;
import org.newdawn.slick.opengl.font.TrueTypeFont;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

@Setter
@Getter
public class GuiComponent implements InputListener {
    private final List<WidthSetter> widthSetters = new ArrayList<>();
    private final List<GuiComponent> childrenToAdd = new ArrayList<>();
    private final List<GuiComponent> childrenToRemove = new ArrayList<>();
    private final List<MouseButtonCallback> mouseButtonCallbacks = new ArrayList<>();

    private final List<MouseListener> mouseButtonListeners = new ArrayList<>();
    private final List<KeyPressedListener> keyPressedListeners = new ArrayList<>();
    private final List<CharTypedListener> charTypedListeners = new ArrayList<>();

    private final ArrayList<Runnable> runOnDimensionUpdate = new ArrayList<>();
    private final List<GuiComponent> children = new ArrayList<>();
    private final List<IRunnable> clickHandlers = new ArrayList<>();

    public float scaleX = 1.0F;
    public float scaleY = 1.0F;

    public int translateX = 0;
    public int translateY = 0;

    public boolean bringToFront;
    public boolean isHoveredInHierarchy;
    public boolean isMouseDownOverComponent;

    private GuiComponent focusedChild;
    private boolean updatingPanelDimensions;

    public String name;
    public int x;
    public int y;
    public int width;
    public int height;
    public String text;
    public TrueTypeFont font;
    public ColorHelper textColor;
    public GuiComponent parent;

    private int mouseX;
    private int mouseY;

    public boolean visible;
    public boolean hovered;
    public boolean focused;

    public boolean listening;
    public boolean saveSize;

    public boolean reAddChildren;

    public GuiComponent(GuiComponent parent, String name) {
        this(parent, name, 0, 0, 0, 0);
    }

    public GuiComponent(GuiComponent parent, String name, int x, int y, int width, int height) {
        this(parent, name, x, y, width, height, ColorHelper.DEFAULT_COLOR);
    }

    public GuiComponent(GuiComponent parent, String name, int x, int y, int width, int height, ColorHelper textColor) {
        this(parent, name, x, y, width, height, textColor, null);
    }

    public GuiComponent(GuiComponent parent, String name, int x, int y, int width, int height, ColorHelper textColor, String text) {
        this(parent, name, x, y, width, height, textColor, text, FontUtils.HELVETICA_LIGHT_25);
    }

    public GuiComponent(GuiComponent parent, String name, int x, int y, int width, int height, ColorHelper textColor, String text, TrueTypeFont font) {
        this.name = name;
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.textColor = textColor;
        this.font = font;
        this.visible = true;
        this.hovered = true;
        this.listening = true;
        this.saveSize = false;
    }

    private void reorderChildren() {
        for (GuiComponent screen : new ArrayList<>(this.children)) {
            if (screen.shouldReAddChildren()) {
                this.children.remove(screen);
                this.children.add(screen);
            }

            if (screen.shouldBringToFront()) {
                this.children.remove(screen);
                this.children.add(0, screen);
            }
        }
    }

    public GuiComponent getChildByName(String childName) {
        for (GuiComponent child : this.children) {
            if (child.getName().equals(childName)) {
                return child;
            }
        }

        return null;
    }

    public void addRunnable(Runnable runnable) {
        synchronized (this) {
            if (runnable != null) {
                this.runOnDimensionUpdate.add(runnable);
            }
        }
    }

    /**
     * Manages the arrangement and removal of CustomGuiScreen objects within various lists.
     * This method performs the following operations:
     * 1. Removes specified screens from iconPanelList and clears focusedChild if necessary.
     * 2. Clears and repopulates iconPanelList with elements from childrenToAdd.
     * 3. Ensures focusedChild, if not null, is at the end of iconPanelList.
     * 4. Calls reorderChildren() to further arrange the iconPanelList.
     * <p>
     * This method does not take any parameters and does not return a value.
     * It operates on the class's internal lists and fields.
     */
    private void processChildUpdates() {
        for (GuiComponent child : this.childrenToRemove) {
            this.children.remove(child);
            if (this.focusedChild == child) {
                this.focusedChild = null;
            }
        }

        this.childrenToRemove.clear();
        this.children.addAll(this.childrenToAdd);
        this.childrenToAdd.clear();
        if (this.focusedChild != null) {
            this.children.remove(this.focusedChild);
            this.children.add(this.focusedChild);
        }

        this.reorderChildren();
    }

    public void updatePanelDimensions(int mouseX, int mouseY) {
        this.mouseY = mouseY;
        this.mouseX = mouseX;
        this.isHoveredInHierarchy = this.isVisible() && this.isMouseOverExclusive(mouseX, mouseY);

        List<Runnable> pendingRunnables;
        synchronized (this) {
            pendingRunnables = new ArrayList<>(this.runOnDimensionUpdate);
            this.runOnDimensionUpdate.clear();
        }
        for (Runnable runnable : pendingRunnables) {
            if (runnable != null) {
                runnable.run();
            }
        }
        this.updatingPanelDimensions = true;

        try {
            for (GuiComponent iconPanel : new ArrayList<>(this.children)) {
                iconPanel.updatePanelDimensions(mouseX, mouseY);
            }
        } catch (ConcurrentModificationException e) {
            Client.LOGGER.warn("Failed to update panel dimensions", e);
        }

        this.isMouseDownOverComponent = this.isMouseDownOverComponent & this.isHoveredInHierarchy;

        for (WidthSetter widthSetter : this.getWidthSetters()) {
            if (this.visible) {
                widthSetter.setWidth(this, this.getParent());
            }
        }

        this.processChildUpdates();
        this.updatingPanelDimensions = false;
    }

    public void applyScaleTransforms() {
        GL11.glTranslatef((float) (this.getX() + this.getWidth() / 2), (float) (this.getY() + this.getHeight() / 2), 0.0F);
        GL11.glScalef(this.getScaleX(), this.getScaleY(), 0.0F);
        GL11.glTranslatef((float) (-this.getX() - this.getWidth() / 2), (float) (-this.getY() - this.getHeight() / 2), 0.0F);
    }

    public void applyTranslationTransforms() {
        GL11.glTranslatef((float) this.getTranslateX(), (float) this.getTranslateY(), 0.0F);
    }

    public void draw(float partialTicks) {
        this.drawChildren(partialTicks);
    }

    public void drawChildren(float partialTicks) {
        GlStateManager.enableAlphaTest();
        GL11.glAlphaFunc(519, 0.0F);
        GL11.glTranslatef((float) this.getX(), (float) this.getY(), 0.0F);

        for (GuiComponent child : this.children) {
            if (child.isSelfVisible()) {
                GL11.glPushMatrix();
                child.draw(partialTicks);
                GL11.glPopMatrix();
            }
        }
    }

    public boolean hasFocusedTextField() {
        for (GuiComponent child : this.getChildren()) {
            if (child instanceof TextField && child.focused) {
                return true;
            }

            if (child.hasFocusedTextField()) {
                return true;
            }
        }

        return false;
    }

    public void modifierPressed(int modifier) {
        for (GuiComponent child : this.children) {
            if (child.isHovered() && child.isSelfVisible()) {
                child.modifierPressed(modifier);
            }
        }
    }

    @Override
    public void charTyped(char ch) {
        for (GuiComponent child : this.children) {
            if (child.isHovered() && child.isSelfVisible()) {
                child.charTyped(ch);
            }
        }

        this.callCharTypedListeners(ch);
    }

    @Override
    public void keyPressed(int keyCode) {
        for (GuiComponent child : this.children) {
            if (child.isHovered() && child.isSelfVisible()) {
                child.keyPressed(keyCode);
            }
        }

        this.callKeyPressedListeners(keyCode);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        boolean over = false;

        for (int i = this.children.size() - 1; i >= 0; i--) {
            GuiComponent child = this.children.get(i);
            boolean isOver = child.getParent() != null
                    && child.getParent() instanceof ScrollablePanel
                    && child.getParent().isMouseOverComponent(mouseX, mouseY)
                    && child.getParent().isSelfVisible()
                    && child.getParent().isHovered();
            if (over || !child.isHovered() || !child.isSelfVisible() || !child.isMouseOverComponent(mouseX, mouseY) && !isOver) {
                child.setFocused(false);
                if (child != null) {
                    for (GuiComponent childsChild : child.getChildren()) {
                        childsChild.setFocused(false);
                    }
                }
            } else {
                child.onMouseDown(mouseX, mouseY, mouseButton);
                over = !isOver;
            }
        }

        if (!over) {
            this.isMouseDownOverComponent = this.isHoveredInHierarchy = true;
            this.requestFocus();
            this.callMouseButtonCallbacks(mouseButton);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {
        this.isHoveredInHierarchy = this.isMouseOverComponent(mouseX, mouseY);

        for (GuiComponent child : this.children) {
            if (child.isHovered() && child.isSelfVisible()) {
                child.onMouseRelease(mouseX, mouseY, mouseButton);
            }
        }

        this.onMouseButtonUsed(mouseButton);
        if (this.isMouseDownOverComponent() && this.isHoveredInHierarchy()) {
            this.onMouseClick(mouseX, mouseY, mouseButton);
        }

        this.isMouseDownOverComponent = false;
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        this.onClick(mouseButton);
    }

    @Override
    public void onScroll(float scroll) {
        for (GuiComponent child : this.children) {
            if (child.isHovered() && child.isSelfVisible()) {
                child.onScroll(scroll);
            }
        }
    }

    public boolean isMouseOverComponent(int mouseX, int mouseY) {
        mouseX -= this.getAbsoluteX();
        mouseY -= this.getAbsoluteY();
        return mouseX >= 0 && mouseX <= this.width && mouseY >= 0 && mouseY <= this.height;
    }

    public boolean isMouseOverComponentConsideringZOrder(int mouseX, int mouseY, boolean checkChildren) {
        boolean isMouseOver = this.isMouseOverComponent(mouseX, mouseY);
        if (isMouseOver && this.parent != null) {
            if (checkChildren) {
                for (GuiComponent child : this.getChildren()) {
                    if (child.isSelfVisible() && child.isMouseOverComponent(mouseX, mouseY)) {
                        return false;
                    }
                }
            }

            GuiComponent current = this;

            for (GuiComponent parent = this.getParent(); parent != null; parent = parent.getParent()) {
                for (int i = parent.findChild(current) + 1; i < parent.getChildren().size(); i++) {
                    GuiComponent sibling = parent.getChildren().get(i);
                    if (sibling != current && sibling.isSelfVisible() && sibling.isMouseOverComponent(mouseX, mouseY)) {
                        return false;
                    }
                }

                current = parent;
            }
        }

        return isMouseOver;
    }

    public boolean isMouseOverExclusive(int mouseX, int mouseY) {
        return this.isMouseOverComponentConsideringZOrder(mouseX, mouseY, true);
    }

    public void addToList(GuiComponent child) {
        if (child != null) {
            for (GuiComponent thisChild : this.getChildren()) {
                if (thisChild.getName().equals(child.getName())) {
                    return;
                }
            }

            child.setParent(this);
            if (this.updatingPanelDimensions) {
                this.childrenToAdd.add(child);
            } else {
                try {
                    this.children.add(child);
                } catch (ConcurrentModificationException var6) {
                    this.childrenToAdd.add(child);
                }
            }
        }
    }

    public boolean hasChildWithName(String childName) {
        for (GuiComponent child : this.getChildren()) {
            if (child.getName().equals(childName)) {
                return true;
            }
        }

        return false;
    }

    public void queueChildAddition(GuiComponent queuedChild) {
        if (queuedChild != null) {
            for (GuiComponent child : this.getChildren()) {
                if (child.getName().equals(queuedChild.getName())) {
                    throw new RuntimeException("Children with duplicate IDs!");
                }
            }

            queuedChild.setParent(this);
            this.childrenToAdd.add(queuedChild);
        }
    }

    public void showAlert(GuiComponent alertScreen) {
        if (alertScreen != null) {
            for (GuiComponent child : this.getChildren()) {
                if (child.getName().equals(alertScreen.getName())) {
                    throw new RuntimeException("Children with duplicate IDs!");
                }
            }

            alertScreen.setParent(this);

            try {
                this.children.add(alertScreen);
            } catch (ConcurrentModificationException ignored) {
            }
        }
    }

    public void queueChildRemoval(GuiComponent child) {
        if (this.updatingPanelDimensions) {
            this.childrenToRemove.add(child);
        } else {
            this.removeChildren(child);
        }
    }

    public void removeChildren(GuiComponent child) {
        this.children.remove(child);
        if (this.focusedChild != null && this.focusedChild.equals(child)) {
            this.focusedChild = null;
        }

        this.childrenToAdd.remove(child);
    }

    public void removeChildByName(String childName) {
        for (GuiComponent child : this.getChildren()) {
            if (child.name.equals(childName)) {
                this.queueChildRemoval(child);
            }
        }
    }

    public void clearChildren() {
        this.children.clear();
    }

    public boolean hasChild(GuiComponent child) {
        return this.children.contains(child);
    }

    public int findChild(GuiComponent child) {
        return this.children.indexOf(child);
    }

    public void requestFocus() {
        this.setFocused(true);
        if (this.parent != null) {
            this.parent.focusedChild = this;
            this.parent.requestFocus();
        }
    }

    public void defocusSiblings() {
        for (GuiComponent child : this.parent.getChildren()) {
            if (child == this) {
                return;
            }

            child.requestFocus();
        }
    }

    public JsonObject toPersistedConfig(JsonObject config) {
        if (this.isListening()) {
            config.addProperty("id", this.getName());
            config.addProperty("x", this.getX());
            config.addProperty("y", this.getY());
            if (this.shouldSaveSize()) {
                config.addProperty("width", this.getWidth());
                config.addProperty("height", this.getHeight());
            }

            config.addProperty("index", this.parent == null ? 0 : this.parent.findChild(this));
            return this.toConfig(config);
        } else {
            return config;
        }
    }

    public final JsonObject toConfig(JsonObject base) {
        JsonArray children = new JsonArray();

        for (GuiComponent child : this.children) {
            if (child.isListening()) {
                JsonObject json = child.toPersistedConfig(new JsonObject());
                if (json.size() != 0) {
                    children.add(json);
                }
            }
        }

        base.add("children", children);
        return base;
    }

    public void loadPersistedConfig(JsonObject config) {
        if (this.isListening()) {
            this.x = GsonUtils.getIntOrDefault(config, "x", this.x);
            this.y = GsonUtils.getIntOrDefault(config, "y", this.y);
            if (this.shouldSaveSize()) {
                this.width = GsonUtils.getIntOrDefault(config, "width", this.width);
                this.height = GsonUtils.getIntOrDefault(config, "height", this.height);
            }

            JsonArray children = GsonUtils.getJSONArrayOrNull(config, "children");
            if (children != null) {
                List<GuiComponent> childrenArray = new ArrayList<>(this.children);

                for (int i = 0; i < children.size(); i++) {
                    JsonObject childJson;

                    try {
                        childJson = children.get(i).getAsJsonObject();
                    } catch (JsonParseException e) {
                        throw new RuntimeException(e);
                    }

                    String id = GsonUtils.getStringOrDefault(childJson, "id", null);
                    int index = GsonUtils.getIntOrDefault(childJson, "index", -1);

                    for (GuiComponent child : childrenArray) {
                        if (child.getName().equals(id)) {
                            child.loadPersistedConfig(childJson);
                            if (index >= 0) {
                                this.children.remove(child);
                                if (index > this.children.size()) {
                                    this.children.add(child);
                                } else {
                                    this.children.add(index, child);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void accept(GuiComponentVisitor visitor) {
        visitor.visit(this);
    }

    public void addMouseButtonCallback(MouseButtonCallback callback) {
        this.mouseButtonCallbacks.add(callback);
    }

    public void callMouseButtonCallbacks(int mouseButton) {
        for (MouseButtonCallback callback : this.mouseButtonCallbacks) {
            callback.onMouseButtonEvent(this, mouseButton);
        }
    }

    public void addMouseListener(MouseListener listener) {
        this.mouseButtonListeners.add(listener);
    }

    public void onMouseButtonUsed(int mouseButton) {
        for (MouseListener mouse : this.mouseButtonListeners) {
            mouse.mouseButtonUsed(this, mouseButton);
        }
    }

    public GuiComponent onClick(IRunnable clickHandler) {
        this.clickHandlers.add(clickHandler);
        return this;
    }

    public void onClick(int mouseButton) {
        for (IRunnable clickHandler : this.clickHandlers) {
            clickHandler.run(this, mouseButton);
        }
    }

    public void addKeyPressListener(KeyPressedListener listener) {
        this.keyPressedListeners.add(listener);
    }

    public void callKeyPressedListeners(int key) {
        for (KeyPressedListener listener : this.keyPressedListeners) {
            listener.keyPressed(this, key);
        }
    }

    public void callCharTypedListeners(char chr) {
        for (CharTypedListener listener : this.charTypedListeners) {
            listener.charTyped(chr);
        }
    }

    public List<WidthSetter> getWidthSetters() {
        return this.widthSetters;
    }

    public void addWidthSetter(WidthSetter widthSetter) {
        this.widthSetters.add(widthSetter);
    }

    public int getAbsoluteX() {
        return this.parent == null ? this.x : this.parent.getAbsoluteX() + this.x;
    }

    public int getAbsoluteY() {
        return this.parent == null ? this.y : this.parent.getAbsoluteY() + this.y;
    }

    public float getScaleX() {
        return this.scaleX;
    }

    public float getScaleY() {
        return this.scaleY;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public int getTranslateX() {
        return this.translateX;
    }

    public int getTranslateY() {
        return this.translateY;
    }

    public void setTranslateX(int translateX) {
        this.translateX = translateX;
    }

    public void setTranslateY(int translateY) {
        this.translateY = translateY;
    }

    public void setTranslate(int translateX, int translateY) {
        this.translateX = translateX;
        this.translateY = translateY;
    }

    /**
     * @return If this screen is visible.
     * doesn't account for the parent, but {@link GuiComponent#isVisible()} does
     * @see GuiComponent#isVisible()
     */
    public boolean isSelfVisible() {
        return this.visible;
    }

    public void setSelfVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return If this screen and its parent (if it has one) are visible.
     * @see GuiComponent#isSelfVisible()
     */
    public boolean isVisible() {
        return this.parent == null ? this.visible : this.visible && this.parent.isVisible();
    }

    /**
     * used in {@link GuiComponent#reorderChildren} to re-add a child (if this returns true)
     */
    public boolean shouldReAddChildren() {
        return this.reAddChildren;
    }

    public boolean shouldBringToFront() {
        return this.bringToFront;
    }

    public void setBringToFront(boolean bringToFront) {
        this.bringToFront = bringToFront;
    }

    public boolean isHoveredInHierarchy() {
        return this.isHoveredInHierarchy;
    }

    public boolean isMouseDownOverComponent() {
        return this.isMouseDownOverComponent;
    }

    public boolean shouldSaveSize() {
        return this.saveSize;
    }

    public interface IRunnable {
        void run(GuiComponent parent, int mouseButton);
    }

    public interface CharTypedListener {
        void charTyped(char ch);
    }

    public interface MouseListener {
        void mouseButtonUsed(GuiComponent screen, int mouseButton);
    }

    public interface KeyPressedListener {
        void keyPressed(GuiComponent screen, int key);
    }

    public interface MouseButtonCallback {
        void onMouseButtonEvent(GuiComponent screen, int mouseButton);
    }
}
