package io.github.sst.remake.module.impl.gui;

import io.github.sst.remake.Client;
import io.github.sst.remake.bus.Subscribe;
import io.github.sst.remake.event.impl.client.KeyPressEvent;
import io.github.sst.remake.event.impl.client.RenderClient2DEvent;
import io.github.sst.remake.manager.impl.HUDManager;
import io.github.sst.remake.module.Category;
import io.github.sst.remake.module.Module;
import io.github.sst.remake.util.client.BindUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.ScissorUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import net.minecraft.client.option.KeyBinding;

import java.util.ArrayList;
import java.util.Iterator;

public class KeyStrokesModule extends Module {

    private final ArrayList<KeyAnimationData> animations = new ArrayList<>();

    public KeyStrokesModule() {
        super(Category.GUI, "KeyStrokes", "Shows what keybind you are pressing");
    }

    @Subscribe
    public void onRender(RenderClient2DEvent event) {
        if (client.player == null || client.options.debugEnabled || client.options.hudHidden) return;

        int y = event.getOffset();
        int x = 10;

        if (Client.INSTANCE.configManager.hqBlur) {
            for (Keystroke keystroke : Keystroke.values()) {
                KeyPosition tLeft = keystroke.getTopLeftPosition();
                KeyPosition tRight = keystroke.getBottomRightPosition();
                ScissorUtils.startScissor(x + tLeft.x, y + tLeft.y, x + tLeft.x + tRight.x, y + tLeft.y + tRight.y);
                HUDManager.registerBlurArea(x + tLeft.x, y + tLeft.y, tRight.x, tRight.y);
                HUDManager.renderFinalBlur();
                ScissorUtils.restoreScissor();
            }
        }

        for (Keystroke keystroke : Keystroke.values()) {
            KeyPosition tLeft = keystroke.getTopLeftPosition();
            KeyPosition bRight = keystroke.getBottomRightPosition();

            float tLeftOpacity = 1.0F;
            float bRightOpacity = 1.0F;

            if (Client.INSTANCE.configManager.hqBlur) {
                tLeftOpacity = 0.5F;
                bRightOpacity = 0.5F;
            }

            String keyName = BindUtils.getKeyName(keystroke.bind.boundKey.getCode());
            keyName = keystroke.bind == client.options.keyAttack ? "L" : keystroke.bind == client.options.keyUse ? "R" : keyName;

            RenderUtils.drawRoundedRect((float)(x + tLeft.x), (float)(y + tLeft.y), (float)(x + tLeft.x + bRight.x), (float)(y + tLeft.y + bRight.y), ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5f * tLeftOpacity));
            RenderUtils.drawRoundedRect((float)(x + tLeft.x), (float)(y + tLeft.y), (float)bRight.x, (float)bRight.y, 10.0f, 0.75f * bRightOpacity);
            RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_18, (float)(x + tLeft.x + (bRight.x - FontUtils.HELVETICA_LIGHT_18.getWidth(keyName)) / 2), (float)(y + tLeft.y + 12), keyName, ClientColors.LIGHT_GREYISH_BLUE.getColor());
        }

        Iterator<KeyAnimationData> iterator = this.animations.iterator();

        while (iterator.hasNext()) {
            KeyAnimationData data = iterator.next();
            Keystroke key = data.keyStroke;
            KeyPosition tLeft = key.getTopLeftPosition();
            KeyPosition bRight = key.getBottomRightPosition();

            ScissorUtils.startScissor(x + tLeft.x, y + tLeft.y, x + tLeft.x + bRight.x, y + tLeft.y + bRight.y);

            float animPercent = 0.7F;
            int duplicates = 0;

            Iterator<KeyAnimationData> animations = this.animations.iterator();
            while (animations.hasNext()) {
                if (!animations.next().keyStroke.equals(key)) {
                    continue;
                }
                ++duplicates;
            }

            if (key.getKeyBinding().isPressed()) {
                if (data.animation.calcPercent() >= animPercent) {
                    if (duplicates < 2) {
                        data.animation.updateStartTime(animPercent);
                    }
                }
            }

            float alpha = data.animation.calcPercent();
            int color = ColorHelper.applyAlpha(-5658199, (1.0f - alpha * (0.5f + alpha * 0.5f)) * 0.8f);

            if (Client.INSTANCE.configManager.hqBlur) {
                color = ColorHelper.applyAlpha(-1, (1.0f - alpha * (0.5f + alpha * 0.5f)) * 0.8f);
            }

            RenderUtils.drawFilledArc((float)(x + tLeft.x + bRight.x / 2), (float)(y + tLeft.y + bRight.y / 2), (bRight.x - 4) * alpha + 4.0f, color);
            ScissorUtils.restoreScissor();

            if (data.animation.calcPercent() != 1.0f) {
                continue;
            }
            iterator.remove();
        }

        event.increment(160);
    }

    @Subscribe
    public void onKeyPress(KeyPressEvent event) {
        if (client.player == null) return;

        if (this.getKeyStrokeForKey(event.key) != null) {
            if (!event.pressed) {
                this.animations.add(new KeyAnimationData(this.getKeyStrokeForKey(event.key)));
            }
        }
    }

    private Keystroke getKeyStrokeForKey(int key) {
        Keystroke[] keystrokes = Keystroke.values();
        for (Keystroke keystroke : keystrokes) {
            if (key == keystroke.getKeyBinding().boundKey.getCode()) {
                return keystroke;
            }
        }
        return null;
    }

    public enum Keystroke {
        LEFT(0.0F, 1.0F, client.options.keyLeft),
        RIGHT(2.0F, 1.0F, client.options.keyRight),
        FORWARD(1.0F, 0.0F, client.options.keyForward),
        BACK(1.0F, 1.0F, client.options.keyBack),
        ATTACK(0.0F, 2.0F, 74, client.options.keyAttack),
        USE(1.02F, 2.0F, 73, client.options.keyUse);

        public final float positionX;
        public final float positionY;

        public final int width;
        public final int height;
        public final int padding;

        public final KeyBinding bind;

        Keystroke(float positionX, float positionY, KeyBinding bind) {
            this(positionX, positionY, 48, 48, 3, bind);
        }

        Keystroke(float positionX, float positionY, int width, KeyBinding bind) {
            this(positionX, positionY, width, 48, 3, bind);
        }

        Keystroke(float positionX, float positionY, int width, int height, int padding, KeyBinding bind) {
            this.positionX = positionX;
            this.positionY = positionY;
            this.bind = bind;
            this.width = width;
            this.height = height;
            this.padding = padding;
        }

        public KeyPosition getTopLeftPosition() {
            return new KeyPosition(
                    this, (int) (this.positionX * (float) (this.width + this.padding)), (int) (this.positionY * (float) (this.height + this.padding))
            );
        }

        public KeyPosition getBottomRightPosition() {
            return new KeyPosition(this, this.width, this.height);
        }

        public KeyBinding getKeyBinding() {
            switch (this) {
                case LEFT:
                    return client.options.keyLeft;
                case RIGHT:
                    return client.options.keyRight;
                case FORWARD:
                    return client.options.keyForward;
                case BACK:
                    return client.options.keyBack;
                case ATTACK:
                    return client.options.keyAttack;
                case USE:
                    return client.options.keyUse;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private static class KeyAnimationData {
        public Keystroke keyStroke;
        public AnimationUtils animation;

        public KeyAnimationData(Keystroke keyStroke) {
            this.animation = new AnimationUtils(300, 0);
            this.keyStroke = keyStroke;
        }
    }

    public static class KeyPosition {
        public final int x;
        public final int y;
        public final Keystroke keystroke;

        public KeyPosition(Keystroke keystroke, int x, int y) {
            this.keystroke = keystroke;
            this.x = x;
            this.y = y;
        }
    }
}
