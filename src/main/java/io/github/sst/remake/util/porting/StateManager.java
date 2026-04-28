package io.github.sst.remake.util.porting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

/**
 * Tiny little GlStateManager for porting to 1.17,
 * should probably reuse until maybe 1.21.5 where we should swap over to GPU Device stuff.
 */
@Environment(EnvType.CLIENT)
public class StateManager {
    private static final Color4 COLOR = new Color4();
    private static final float[] currentTex = new float[2];
    private static int currentMatrixMode = 5888;
    private static boolean drawing;

    @Deprecated
    public static void glBegin(int mode) {
        drawing = true;
        VertexFormat.DrawMode drawMode = switch (mode) {
            case GL11.GL_LINES -> VertexFormat.DrawMode.LINES;
            case GL11.GL_LINE_STRIP, GL11.GL_LINE_LOOP -> VertexFormat.DrawMode.LINE_STRIP;
            case GL11.GL_TRIANGLES -> VertexFormat.DrawMode.TRIANGLES;
            case GL11.GL_TRIANGLE_STRIP -> VertexFormat.DrawMode.TRIANGLE_STRIP;
            case GL11.GL_TRIANGLE_FAN -> VertexFormat.DrawMode.TRIANGLE_FAN;
            default -> VertexFormat.DrawMode.QUADS;
        };
        Tessellator.getInstance().getBuffer().begin(drawMode, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    @Deprecated
    public static void glEnd() {
        drawing = false;
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Tessellator.getInstance().draw();
    }

    @Deprecated
    public static void glVertex2d(double x, double y) {
        if (drawing) {
            Tessellator.getInstance().getBuffer().vertex(x, y, 0.0).texture(currentTex[0], currentTex[1]).color(COLOR.red, COLOR.green, COLOR.blue, COLOR.alpha).next();
        }
    }

    @Deprecated
    public static void glVertex2f(float x, float y) {
        if (drawing) {
            Tessellator.getInstance().getBuffer().vertex(x, y, 0.0F).texture(currentTex[0], currentTex[1]).color(COLOR.red, COLOR.green, COLOR.blue, COLOR.alpha).next();
        }
    }

    @Deprecated
    public static void glVertex3d(double x, double y, double z) {
        if (drawing) {
            Tessellator.getInstance().getBuffer().vertex(x, y, z).texture(currentTex[0], currentTex[1]).color(COLOR.red, COLOR.green, COLOR.blue, COLOR.alpha).next();
        }
    }

    @Deprecated
    public static void glVertex3f(float x, float y, float z) {
        if (drawing) {
            Tessellator.getInstance().getBuffer().vertex(x, y, z).texture(currentTex[0], currentTex[1]).color(COLOR.red, COLOR.green, COLOR.blue, COLOR.alpha).next();
        }
    }

    @Deprecated
    public static void glTexCoord2f(float u, float v) {
        currentTex[0] = u;
        currentTex[1] = v;
    }

    public static void glBindTexture(int target, int id) {
        RenderSystem.setShaderTexture(0, id);
    }

    @Deprecated
    public static void disableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
    }

    @Deprecated
    public static void enableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
    }

    @Deprecated
    public static void alphaFunc(int func, float ref) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
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

    public static void rotated(double angle, double x, double y, double z) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (currentMatrixMode == 5889) {
            Matrix4f mat = RenderSystem.getProjectionMatrix().copy();
            mat.multiply(new Quaternion(new Vec3f((float) x, (float) y, (float) z), (float) angle, true));
            RenderSystem.setProjectionMatrix(mat);
        } else {
            RenderSystem.getModelViewStack().multiply(new Quaternion(new Vec3f((float) x, (float) y, (float) z), (float) angle, true));
            RenderSystem.applyModelViewMatrix();
        }
    }

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

    public static void pushMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (currentMatrixMode == 5889) {
            throw new UnsupportedOperationException("Projection matrix stack not supported in 1.17 StateManager");
        }
        RenderSystem.getModelViewStack().push();
    }

    public static void popMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (currentMatrixMode == 5889) {
            throw new UnsupportedOperationException("Projection matrix stack not supported in 1.17 StateManager");
        }
        RenderSystem.getModelViewStack().pop();
        RenderSystem.applyModelViewMatrix();
    }

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

    public static void matrixMode(int mode) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        currentMatrixMode = mode;
    }

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

    public static void color4fv(float[] color) {
        if (color != null && color.length >= 4) {
            color4f(color[0], color[1], color[2], color[3]);
        }
    }

    @Deprecated
    public static void glNormal3f(float x, float y, float z) {
    }

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

    public static void ortho(double l, double r, double b, double t, double n, double f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        Matrix4f mat = Matrix4f.projectionMatrix((float) l, (float) r, (float) b, (float) t, (float) n, (float) f);
        RenderSystem.setProjectionMatrix(mat);
    }

    @Environment(EnvType.CLIENT)
    static class Color4 {
        public float red;
        public float green;
        public float blue;
        public float alpha;

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
}