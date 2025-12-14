package io.github.sst.remake.mixin;

import io.github.sst.remake.event.impl.window.CharEvent;
import io.github.sst.remake.event.impl.window.KeyEvent;
import net.minecraft.client.Keyboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Inject(method = "onKey", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J", ordinal = 0, shift = At.Shift.BEFORE), cancellable = true)
    private void injectOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        KeyEvent event = new KeyEvent(window, key, scancode, action, modifiers);
        event.call();

        if (event.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "onChar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", ordinal = 0, shift = At.Shift.BEFORE, opcode = Opcodes.GETFIELD), cancellable = true)
    private void injectOnKey(long window, int codepoint, int modifiers, CallbackInfo ci) {
        CharEvent event = new CharEvent(window, codepoint, modifiers);
        event.call();

        if (event.cancelled) {
            ci.cancel();
        }
    }

}
