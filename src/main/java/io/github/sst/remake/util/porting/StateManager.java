package io.github.sst.remake.util.porting;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.RequiredArgsConstructor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Untracker;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

/**
 * Tiny little GlStateManager for porting to 1.17,
 * should probably reuse until maybe 1.21.5 where we should swap over to GPU Device stuff.
 */
@Environment(EnvType.CLIENT)
public class StateManager {
    private static final FloatBuffer MATRIX_BUFFER = GLX.make(MemoryUtil.memAllocFloat(16), fb -> Untracker.untrack(MemoryUtil.memAddress(fb)));
    private static final AlphaTestState ALPHA_TEST = new AlphaTestState();
    private static final ColorMaterialState COLOR_MATERIAL = new ColorMaterialState();
    private static final CapabilityTracker LIGHTING = new CapabilityTracker(2896);
    private static final Color4 COLOR = new Color4();
    private static int modelShadeMode = 7425;

    @Deprecated
    public static void disableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        ALPHA_TEST.capState.disable();
    }

    @Deprecated
    public static void enableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        ALPHA_TEST.capState.enable();
    }

    @Deprecated
    public static void alphaFunc(int func, float ref) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (func != ALPHA_TEST.func || ref != ALPHA_TEST.ref) {
            ALPHA_TEST.func = func;
            ALPHA_TEST.ref = ref;
        }
    }

    @Deprecated
    public static void enableColorMaterial() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
    }

    @Deprecated
    public static void disableColorMaterial() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
    }

    @Deprecated
    public static void colorMaterial(int face, int mode) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
    }

    private static int currentMatrixMode = 5888;

    @Deprecated
    public static void translatef(float x, float y, float z) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (currentMatrixMode == 5889) {
            Matrix4f mat = RenderSystem.getProjectionMatrix().copy();
            mat.multiply(Matrix4f.translate(x, y, z));
            RenderSystem.setProjectionMatrix(mat);
        } else {
            RenderSystem.getModelViewStack().translate(x, y, z);
            RenderSystem.applyModelViewMatrix();
        }
    }

    @Deprecated
    public static void scalef(float x, float y, float z) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (currentMatrixMode == 5889) {
            Matrix4f mat = RenderSystem.getProjectionMatrix().copy();
            mat.multiply(Matrix4f.scale(x, y, z));
            RenderSystem.setProjectionMatrix(mat);
        } else {
            RenderSystem.getModelViewStack().scale(x, y, z);
            RenderSystem.applyModelViewMatrix();
        }
    }

    @Deprecated
    public static void scaled(double x, double y, double z) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (currentMatrixMode == 5889) {
            Matrix4f mat = RenderSystem.getProjectionMatrix().copy();
            mat.multiply(Matrix4f.scale((float) x, (float) y, (float) z));
            RenderSystem.setProjectionMatrix(mat);
        } else {
            RenderSystem.getModelViewStack().scale((float) x, (float) y, (float) z);
            RenderSystem.applyModelViewMatrix();
        }
    }

    @Deprecated
    public static void rotatef(float angle, float x, float y, float z) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (currentMatrixMode == 5889) {
            Matrix4f mat = RenderSystem.getProjectionMatrix().copy();
            mat.multiply(new Quaternion(new Vec3f(x, y, z), angle, true));
            RenderSystem.setProjectionMatrix(mat);
        } else {
            RenderSystem.getModelViewStack().multiply(new Quaternion(new Vec3f(x, y, z), angle, true));
            RenderSystem.applyModelViewMatrix();
        }
    }

    @Deprecated
    public static void translated(double x, double y, double z) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (currentMatrixMode == 5889) {
            Matrix4f mat = RenderSystem.getProjectionMatrix().copy();
            mat.multiply(Matrix4f.translate((float) x, (float) y, (float) z));
            RenderSystem.setProjectionMatrix(mat);
        } else {
            RenderSystem.getModelViewStack().translate(x, y, z);
            RenderSystem.applyModelViewMatrix();
        }
    }

    @Deprecated
    public static void pushMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (currentMatrixMode == 5889) {
            throw new UnsupportedOperationException("Projection matrix stack not supported in 1.17 StateManager");
        }
        RenderSystem.getModelViewStack().push();
    }

    @Deprecated
    public static void popMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (currentMatrixMode == 5889) {
            throw new UnsupportedOperationException("Projection matrix stack not supported in 1.17 StateManager");
        }
        RenderSystem.getModelViewStack().pop();
        RenderSystem.applyModelViewMatrix();
    }

    @Deprecated
    public static void multMatrix(FloatBuffer matrix) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Matrix4f mat = new Matrix4f();
        mat.readColumnMajor(matrix);
        if (currentMatrixMode == 5889) {
            Matrix4f proj = RenderSystem.getProjectionMatrix().copy();
            proj.multiply(mat);
            RenderSystem.setProjectionMatrix(proj);
        } else {
            RenderSystem.getModelViewStack().peek().getModel().multiply(mat);
            RenderSystem.applyModelViewMatrix();
        }
    }

    @Deprecated
    public static void multMatrix(Matrix4f matrix) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (currentMatrixMode == 5889) {
            Matrix4f proj = RenderSystem.getProjectionMatrix().copy();
            proj.multiply(matrix);
            RenderSystem.setProjectionMatrix(proj);
        } else {
            RenderSystem.getModelViewStack().peek().getModel().multiply(matrix);
            RenderSystem.applyModelViewMatrix();
        }
    }

    @Deprecated
    public static void matrixMode(int mode) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        currentMatrixMode = mode;
    }

    @Deprecated
    public static void loadIdentity() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (currentMatrixMode == 5889) {
            Matrix4f mat = new Matrix4f();
            mat.loadIdentity();
            RenderSystem.setProjectionMatrix(mat);
        } else {
            RenderSystem.getModelViewStack().loadIdentity();
            RenderSystem.applyModelViewMatrix();
        }
    }

    @Deprecated
    public static void enableLighting() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
    }

    @Deprecated
    public static void disableLighting() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
    }

    @Deprecated
    public static void color4f(float red, float green, float blue, float alpha) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (red != COLOR.red || green != COLOR.green || blue != COLOR.blue || alpha != COLOR.alpha) {
            COLOR.red = red;
            COLOR.green = green;
            COLOR.blue = blue;
            COLOR.alpha = alpha;
            RenderSystem.setShaderColor(red, green, blue, alpha);
        }
    }

    @Deprecated
    public static void clearCurrentColor() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        COLOR.red = -1.0f;
        COLOR.green = -1.0f;
        COLOR.blue = -1.0f;
        COLOR.alpha = -1.0f;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Deprecated
    public static void shadeModel(int mode) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
    }

    @Deprecated
    public static void glMultiTexCoord2f(int texture, float s, float t) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
    }

    @Deprecated
    public static void ortho(double l, double r, double b, double t, double n, double f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        Matrix4f mat = Matrix4f.projectionMatrix((float) l, (float) r, (float) b, (float) t, (float) n, (float) f);
        RenderSystem.setProjectionMatrix(mat);
    }

    @Deprecated
    @Environment(EnvType.CLIENT)
    static class AlphaTestState {
        public final CapabilityTracker capState = new CapabilityTracker(3008);
        public int func = 519;
        public float ref = -1.0f;

        private AlphaTestState() {
        }
    }

    @Deprecated
    @Environment(EnvType.CLIENT)
    static class ColorMaterialState {
        public final CapabilityTracker capState = new CapabilityTracker(2903);
        public int face = 1032;
        public int mode = 5634;

        private ColorMaterialState() {
        }
    }

    @Deprecated
    @Environment(EnvType.CLIENT)
    static class Color4 {
        public float red = 1.0f;
        public float green = 1.0f;
        public float blue = 1.0f;
        public float alpha = 1.0f;

        public Color4() {
            this(1.0f, 1.0f, 1.0f, 1.0f);
        }

        public Color4(float red, float green, float blue, float alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }

    @Environment(EnvType.CLIENT)
    static class CapabilityTracker {
        private final int cap;
        private boolean state;

        public CapabilityTracker(int cap) {
            this.cap = cap;
        }

        public void disable() {
            this.setState(false);
        }

        public void enable() {
            this.setState(true);
        }

        public void setState(boolean state) {
            RenderSystem.assertThread(RenderSystem::isOnRenderThread);
            if (this.state != state) {
                this.state = state;
                /*
                if (state) {
                    GL11.glEnable(this.cap);
                } else {
                    GL11.glDisable(this.cap);
                }
                */
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @RequiredArgsConstructor
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
    }

    @RequiredArgsConstructor
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
    }
}
