package info.opensigma.mixin;

import info.opensigma.OpenSigma;
import info.opensigma.event.impl.KeyPressEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (key != GLFW.GLFW_KEY_UNKNOWN) {
            final KeyPressEvent keyPressEvent = new KeyPressEvent(key, modifiers, action);
            OpenSigma.getInstance().getEventBus().post(keyPressEvent);
        }
    }

}
