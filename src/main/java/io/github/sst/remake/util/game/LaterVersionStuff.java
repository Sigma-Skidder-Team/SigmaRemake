package io.github.sst.remake.util.game;

import com.mojang.blaze3d.systems.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;

/**
 * Stuff that I'll remove when we catch up to 1.21.6+.
 * I'm putting stuff here that I know will be changed in 1.21.5+ (they did a major rendering overhaul).
 * Their rendering system will probably be bypassed in some places,
 * but we will at some point slowly start using it in places where we can.
 * Minecraft 26.~2 is going to consider rewriting the rendering system to use Vulkan instead of OpenGL,
 * so we should try to use their rendering system instead of bypassing it as much as possible.
 **/
@UtilityClass
public final class LaterVersionStuff {
    /**
     * Runs {@code renderThread} on the render thread.
     * This is a wrapper around RenderSystem.recordRenderCall because in 1.21.5 they replace it with `mc.execute`
     **/
    public static void execute(RenderCall renderThread) {
        // TODO(version/1.21.5): (I'm planning ahead) use `mc.execute`
        RenderSystem.recordRenderCall(renderThread);
    }
}
