package io.github.sst.remake.util.porting;

import com.mojang.blaze3d.systems.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;

/**
 * Stuff for us to use now, so porting the client to later versions is easier.
 */
@UtilityClass
public final class LaterVersionStuff {
    public static void execute(RenderCall call) {
        // TODO(version/1.21.5): replace with `mc.execute`
        RenderSystem.recordRenderCall(call);
    }
}
