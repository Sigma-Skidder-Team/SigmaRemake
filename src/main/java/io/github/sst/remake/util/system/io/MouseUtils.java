package io.github.sst.remake.util.system.io;

import io.github.sst.remake.util.IMinecraft;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.hit.HitResult;

public class MouseUtils implements IMinecraft {
    public static void pressLeft() {
        pressBind(client.options.keyAttack);
    }

    public static void releaseLeft() {
        releaseBind(client.options.keyAttack);
    }

    public static void pressRight() {
        pressBind(client.options.keyUse);
    }

    public static void releaseRight() {
        releaseBind(client.options.keyUse);
    }

    public static void placeBlock(HitResult hitResult) {
        HitResult original = client.crosshairTarget;

        client.crosshairTarget = hitResult;
        client.doItemUse();
        client.crosshairTarget = original;
    }

    private static void pressBind(KeyBinding key) {
        if (key == null) return;

        key.setPressed(true);
        key.timesPressed++;
    }

    private static void releaseBind(KeyBinding key) {
        if (key == null) return;

        key.setPressed(false);
    }
}