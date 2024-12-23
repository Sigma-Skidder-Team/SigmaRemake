package info.opensigma.mixin;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Supplier;

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
    @ModifyArgs(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/loading/ImmediateWindowHandler;setupMinecraftWindow(Ljava/util/function/IntSupplier;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)J"))
    private void modifyWindowTitle(Args args) {
        String originalTitle = (((Supplier<String>) args.get(2)).get());
        String newTitle = originalTitle.replace("Minecraft", "OpenSigma");
        args.set(2, (Supplier<String>) () -> newTitle);
//        args.set(2, ((String)args.get(2)).replace("Minecraft", "OpenSigma"));
    }
}
