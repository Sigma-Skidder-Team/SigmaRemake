package info.opensigma.mixin;

import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

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
    @ModifyArgs(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"))
    private void modifyWindowTitle(Args args) {
        args.set(2, ((String)args.get(2)).replace("Minecraft", "OpenSigma"));
    }
}
