package io.github.sst.remake.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.sst.remake.Client;
import io.github.sst.remake.gui.element.impl.TextField;
import io.github.sst.remake.gui.interfaces.ICustomGuiScreenVisitor;
import io.github.sst.remake.gui.interfaces.IGuiEventListener;
import io.github.sst.remake.gui.interfaces.IWidthSetter;
import io.github.sst.remake.gui.panel.ScrollableContentPanel;
import io.github.sst.remake.util.io.GsonUtils;
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
public class CustomGuiScreen implements IGuiEventListener {
    private final List<IWidthSetter> widthSetters = new ArrayList<>();
    private final List<CustomGuiScreen> childrenToAdd = new ArrayList<>();
    private final List<CustomGuiScreen> childrenToRemove = new ArrayList<>();
    private final List<MouseButtonCallback> mouseButtonCallbacks = new ArrayList<>();

    private final List<MouseListener> mouseButtonListeners = new ArrayList<>();
    private final List<KeyPressedListener> keyPressedListeners = new ArrayList<>();
    private final List<CharTypedListener> charTypedListeners = new ArrayList<>();

    private final ArrayList<Runnable> runOnDimensionUpdate = new ArrayList<>();
    private final List<CustomGuiScreen> children = new ArrayList<>();
    private final List<IRunnable> clickHandlers = new ArrayList<>();

    public float scaleX = 1.0F;
    public float scaleY = 1.0F;

    public int translateX = 0;
    public int translateY = 0;

    public boolean bringToFront;
    public boolean isHoveredInHierarchy;
    public boolean isMouseDownOverComponent;

    private CustomGuiScreen focusedChild;
    private boolean updatingPanelDimensions;

    public String name;
    public int x;
    public int y;
    public int width;
    public int height;
    public String text;
    public TrueTypeFont font;
    public ColorHelper textColor;
    public CustomGuiScreen parent;

    private int mouseX;
    private int mouseY;

    public boolean visible;
    public boolean hovered;
    public boolean focused;

    public boolean listening;
    public boolean saveSize;

    public boolean reAddChildren;

    public CustomGuiScreen(CustomGuiScreen parent, String name) {
        this(parent, name, 0, 0, 0, 0);
    }

    public CustomGuiScreen(CustomGuiScreen parent, String name, int x, int y, int width, int height) {
        this(parent, name, x, y, width, height, ColorHelper.DEFAULT_COLOR);
    }

    public CustomGuiScreen(CustomGuiScreen parent, String name, int x, int y, int width, int height, ColorHelper textColor) {
        this(parent, name, x, y, width, height, textColor, null);
    }

    public CustomGuiScreen(CustomGuiScreen parent, String name, int x, int y, int width, int height, ColorHelper textColor, String text) {
        this(parent, name, x, y, width, height, textColor, text, FontUtils.HELVETICA_LIGHT_25);
    }

    public CustomGuiScreen(CustomGuiScreen parent, String name, int x, int y, int width, int height, ColorHelper textColor, String text, TrueTypeFont font) {
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
        for (CustomGuiScreen screen : new ArrayList<>(this.children)) {
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

    public CustomGuiScreen getChildByName(String childName) {
        for (CustomGuiScreen child : this.children) {
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
     * 1. Removes specified screens from iconPanelList and clears field20919 if necessary.
     * 2. Clears and repopulates iconPanelList with elements from field20916.
     * 3. Ensures field20919, if not null, is at the end of iconPanelList.
     * 4. Calls method13220() to further arrange the iconPanelList.
     * <p>
     * This method does not take any parameters and does not return a value.
     * It operates on the class's internal lists and fields.
     */
    private void processChildUpdates() {
        for (CustomGuiScreen child : this.childrenToRemove) {
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

        try {
            for (Runnable runnable : this.runOnDimensionUpdate) {
                if (runnable != null) {
                    runnable.run();
                }
            }
        } catch (ConcurrentModificationException e) {
            Client.LOGGER.info("Failed to run dimension update runnables", e);
        }

        this.runOnDimensionUpdate.clear();
        this.updatingPanelDimensions = true;

        try {
            for (CustomGuiScreen iconPanel : this.children) {
                iconPanel.updatePanelDimensions(mouseX, mouseY);
            }
        } catch (ConcurrentModificationException e) {
            Client.LOGGER.warn("Failed to update panel dimensions", e);
        }

        this.isMouseDownOverComponent = this.isMouseDownOverComponent & this.isHoveredInHierarchy;

        for (IWidthSetter widthSetter : this.getWidthSetters()) {
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

    public final void drawChildren(float partialTicks) {
        GlStateManager.enableAlphaTest();
        GL11.glAlphaFunc(519, 0.0F);
        GL11.glTranslatef((float) this.getX(), (float) this.getY(), 0.0F);

        for (CustomGuiScreen child : this.children) {
            if (child.isSelfVisible()) {
                GL11.glPushMatrix();
                child.draw(partialTicks);
                GL11.glPopMatrix();
            }
        }
    }

    public boolean hasFocusedTextField() {
        for (CustomGuiScreen child : this.getChildren()) {
            if (child instanceof TextField && child.focused) {
                return true;
            }

            if (child.hasFocusedTextField()) {
                return true;
            }
        }

        return false;
    }

    public void modifierPressed(int var1) {
        for (CustomGuiScreen var5 : this.children) {
            if (var5.isHovered() && var5.isSelfVisible()) {
                var5.modifierPressed(var1);
            }
        }
    }

    @Override
    public void charTyped(char typed) {
        for (CustomGuiScreen var5 : this.children) {
            if (var5.isHovered() && var5.isSelfVisible()) {
                var5.charTyped(typed);
            }
        }

        this.callCharTypedListeners(typed);
    }

    @Override
    public void keyPressed(int keyCode) {
        for (CustomGuiScreen child : this.children) {
            if (child.isHovered() && child.isSelfVisible()) {
                child.keyPressed(keyCode);
            }
        }

        this.callKeyPressedListeners(keyCode);
    }

    @Override
    public boolean onMouseDown(int mouseX, int mouseY, int mouseButton) {
        boolean var6 = false;

        for (int i = this.children.size() - 1; i >= 0; i--) {
            CustomGuiScreen var8 = this.children.get(i);
            boolean var9 = var8.getParent() != null
                    && var8.getParent() instanceof ScrollableContentPanel
                    && var8.getParent().method13114(mouseX, mouseY)
                    && var8.getParent().isSelfVisible()
                    && var8.getParent().isHovered();
            if (var6 || !var8.isHovered() || !var8.isSelfVisible() || !var8.method13114(mouseX, mouseY) && !var9) {
                var8.setFocused(false);
                if (var8 != null) {
                    for (CustomGuiScreen child : var8.getChildren()) {
                        child.setFocused(false);
                    }
                }
            } else {
                var8.onMouseDown(mouseX, mouseY, mouseButton);
                var6 = !var9;
            }
        }

        if (!var6) {
            this.field20909 = this.field20908 = true;
            this.method13242();
            this.method13248(mouseButton);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onMouseRelease(int mouseX, int mouseY, int mouseButton) {
        this.field20908 = this.method13114(mouseX, mouseY);

        for (CustomGuiScreen child : this.children) {
            if (child.isHovered() && child.isSelfVisible()) {
                child.onMouseRelease(mouseX, mouseY, mouseButton);
            }
        }

        this.onMouseButtonUsed(mouseButton);
        if (this.method13212() && this.method13298()) {
            this.onMouseClick(mouseX, mouseY, mouseButton);
        }

        this.field20909 = false;
    }

    @Override
    public void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        this.onClick(mouseButton);
    }

    @Override
    public void onScroll(float scroll) {
        for (CustomGuiScreen child : this.children) {
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
                for (CustomGuiScreen child : this.getChildren()) {
                    if (child.isSelfVisible() && child.isMouseOverComponent(mouseX, mouseY)) {
                        return false;
                    }
                }
            }

            CustomGuiScreen current = this;

            for (CustomGuiScreen parent = this.getParent(); parent != null; parent = parent.getParent()) {
                for (int i = parent.findChild(current) + 1; i < parent.getChildren().size(); i++) {
                    CustomGuiScreen sibling = parent.getChildren().get(i);
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

    public void addToList(CustomGuiScreen child) {
        if (child != null) {
            for (CustomGuiScreen var5 : this.getChildren()) {
                if (var5.getName().equals(child.getName())) {
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
        for (CustomGuiScreen child : this.getChildren()) {
            if (child.getName().equals(childName)) {
                return true;
            }
        }

        return false;
    }

    public void queueChildAddition(CustomGuiScreen child) {
        if (child != null) {
            for (CustomGuiScreen var5 : this.getChildren()) {
                if (var5.getName().equals(child.getName())) {
                    throw new RuntimeException("Children with duplicate IDs!");
                }
            }

            child.setParent(this);
            this.childrenToAdd.add(child);
        }
    }

    public void showAlert(CustomGuiScreen alertScreen) {
        if (alertScreen != null) {
            for (CustomGuiScreen var5 : this.getChildren()) {
                if (var5.getName().equals(alertScreen.getName())) {
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

    public void queueChildRemoval(CustomGuiScreen child) {
        if (this.updatingPanelDimensions) {
            this.childrenToRemove.add(child);
        } else {
            this.removeChildren(child);
        }
    }

    public void removeChildren(CustomGuiScreen child) {
        this.children.remove(child);
        if (this.focusedChild != null && this.focusedChild.equals(child)) {
            this.focusedChild = null;
        }

        this.childrenToAdd.remove(child);
    }

    public void removeChildByName(String childName) {
        for (CustomGuiScreen child : this.getChildren()) {
            if (child.name.equals(childName)) {
                this.queueChildRemoval(child);
            }
        }
    }

    public void clearChildren() {
        this.children.clear();
    }

    public boolean hasChild(CustomGuiScreen child) {
        return this.children.contains(child);
    }

    public int findChild(CustomGuiScreen child) {
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
        for (CustomGuiScreen child : this.parent.getChildren()) {
            if (child == this) {
                return;
            }

            child.requestFocus();
        }
    }

    public JsonObject toConfigWithExtra(JsonObject config) {
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

        for (CustomGuiScreen child : this.children) {
            if (child.isListening()) {
                JsonObject var7 = child.toConfigWithExtra(new JsonObject());
                if (!var7.isEmpty()) {
                    children.add(var7);
                }
            }
        }

        base.add("children", children);
        return base;
    }

    public void loadConfig(JsonObject config) {
        if (this.isListening()) {
            this.x = GsonUtils.getIntOrDefault(config, "x", this.x);
            this.y = GsonUtils.getIntOrDefault(config, "y", this.y);
            if (this.shouldSaveSize()) {
                this.width = GsonUtils.getIntOrDefault(config, "width", this.width);
                this.height = GsonUtils.getIntOrDefault(config, "height", this.height);
            }

            JsonArray children = GsonUtils.getJSONArrayOrNull(config, "children");
            if (children != null) {
                List<CustomGuiScreen> childrenArray = new ArrayList<>(this.children);

                for (int i = 0; i < children.size(); i++) {
                    JsonObject childJson;

                    try {
                        childJson = children.get(i).getAsJsonObject();
                    } catch (JsonParseException e) {
                        throw new RuntimeException(e);
                    }

                    String id = GsonUtils.getStringOrDefault(childJson, "id", null);
                    int index = GsonUtils.getIntOrDefault(childJson, "index", -1);

                    for (CustomGuiScreen child : childrenArray) {
                        if (child.getName().equals(id)) {
                            child.loadConfig(childJson);
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

    public void accept(ICustomGuiScreenVisitor visitor) {
        visitor.visit(this);
    }

    public final CustomGuiScreen addMouseButtonCallback(MouseButtonCallback callback) {
        this.mouseButtonCallbacks.add(callback);
        return this;
    }

    public void callMouseButtonCallbacks(int mouseButton) {
        for (MouseButtonCallback callback : this.mouseButtonCallbacks) {
            callback.onMouseButtonEvent(this, mouseButton);
        }
    }

    public CustomGuiScreen addMouseListener(MouseListener listener) {
        this.mouseButtonListeners.add(listener);
        return this;
    }

    public void onMouseButtonUsed(int mouseButton) {
        for (MouseListener mouse : this.mouseButtonListeners) {
            mouse.mouseButtonUsed(this, mouseButton);
        }
    }

    public CustomGuiScreen onClick(IRunnable clickHandler) {
        this.clickHandlers.add(clickHandler);
        return this;
    }

    public void onClick(int mouseButton) {
        for (IRunnable clickHandler : this.clickHandlers) {
            clickHandler.run(this, mouseButton);
        }
    }

    public final void addKeyPressListener(KeyPressedListener listener) {
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

    public List<IWidthSetter> getWidthSetters() {
        return this.widthSetters;
    }

    public void addWidthSetter(IWidthSetter widthSetter) {
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
     * doesn't account for the parent, but {@link CustomGuiScreen#isVisible()} does
     * @see CustomGuiScreen#isVisible()
     */
    public boolean isSelfVisible() {
        return this.visible;
    }

    public void setSelfVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return If this screen and its parent (if it has one) are visible.
     * @see CustomGuiScreen#isSelfVisible()
     */
    public boolean isVisible() {
        return this.parent == null ? this.visible : this.visible && this.parent.isVisible();
    }

    /**
     * used in {@link CustomGuiScreen#method13220} to re-add a child (if this returns true)
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
        void run(CustomGuiScreen parent, int mouseButton);
    }

    public interface CharTypedListener {
        void charTyped(char chr);
    }

    public interface MouseListener {
        void mouseButtonUsed(CustomGuiScreen screen, int mouseButton);
    }

    public interface KeyPressedListener {
        void keyPressed(CustomGuiScreen screen, int key);
    }

    public interface MouseButtonCallback {
        void onMouseButtonEvent(CustomGuiScreen screen, int mouseButton);
    }
}
