package info.opensigma.mixin;

import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Window.class)
public class WindowMixin {
    @ModifyArg(
            method = "setTitle",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowTitle(JLjava/lang/CharSequence;)V"),
            index = 1
    )
    private CharSequence setTitle(CharSequence title) {
        title = ((String)title).replace("Minecraft", "OpenSigma for Minecraft");
        return title;
    }
}
