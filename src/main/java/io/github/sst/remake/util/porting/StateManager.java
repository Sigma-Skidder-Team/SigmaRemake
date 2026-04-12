package io.github.sst.remake.util.porting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

/**
 * Tiny little GlStateManager for porting to 1.17,
 * should probably reuse until maybe 1.21.5 where we should swap over to GPU Device stuff.
 */
@Environment(EnvType.CLIENT)
public class StateManager {
    public static void disableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
    }

    @Deprecated
    public static void enableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    @Deprecated
    public static void translatef(float x, float y, float z) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTranslatef(x, y, z);
    }

    @Environment(EnvType.CLIENT)
    public enum SrcFactor {
        CONSTANT_ALPHA(GL14.GL_CONSTANT_ALPHA),
        CONSTANT_COLOR(GL14.GL_CONSTANT_COLOR),
        DST_ALPHA(GL11.GL_DST_ALPHA),
        DST_COLOR(GL11.GL_DST_COLOR),
        ONE(GL11.GL_ONE),
        ONE_MINUS_CONSTANT_ALPHA(GL14.GL_ONE_MINUS_CONSTANT_ALPHA),
        ONE_MINUS_CONSTANT_COLOR(GL14.GL_ONE_MINUS_CONSTANT_COLOR),
        ONE_MINUS_DST_ALPHA(GL11.GL_ONE_MINUS_DST_ALPHA),
        ONE_MINUS_DST_COLOR(GL11.GL_ONE_MINUS_DST_COLOR),
        ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA),
        ONE_MINUS_SRC_COLOR(GL11.GL_ONE_MINUS_SRC_COLOR),
        SRC_ALPHA(GL11.GL_SRC_ALPHA),
        SRC_ALPHA_SATURATE(GL11.GL_SRC_ALPHA_SATURATE),
        SRC_COLOR(GL11.GL_SRC_COLOR),
        ZERO(GL11.GL_ZERO);

        public final int value;

        private SrcFactor(int value) {
            this.value = value;
        }
    }

    public enum DstFactor {
        CONSTANT_ALPHA(GL14.GL_CONSTANT_ALPHA),
        CONSTANT_COLOR(GL14.GL_CONSTANT_COLOR),
        DST_ALPHA(GL11.GL_DST_ALPHA),
        DST_COLOR(GL11.GL_DST_COLOR),
        ONE(GL11.GL_ONE),
        ONE_MINUS_CONSTANT_ALPHA(GL14.GL_ONE_MINUS_CONSTANT_ALPHA),
        ONE_MINUS_CONSTANT_COLOR(GL14.GL_ONE_MINUS_CONSTANT_COLOR),
        ONE_MINUS_DST_ALPHA(GL11.GL_ONE_MINUS_DST_ALPHA),
        ONE_MINUS_DST_COLOR(GL11.GL_ONE_MINUS_DST_COLOR),
        ONE_MINUS_SRC_ALPHA(GL11.GL_ONE_MINUS_SRC_ALPHA),
        ONE_MINUS_SRC_COLOR(GL11.GL_ONE_MINUS_SRC_COLOR),
        SRC_ALPHA(GL11.GL_SRC_ALPHA),
        SRC_COLOR(GL11.GL_SRC_COLOR),
        ZERO(GL11.GL_ZERO);

        public final int value;

        private DstFactor(int value) {
            this.value = value;
        }
    }
}
