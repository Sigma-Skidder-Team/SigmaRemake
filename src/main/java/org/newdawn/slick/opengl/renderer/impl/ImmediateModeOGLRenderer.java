package org.newdawn.slick.opengl.renderer.impl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.util.porting.StateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import org.lwjgl.opengl.EXTSecondaryColor;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.renderer.SGL;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * The default OpenGL renderer, uses immediate mode for everything
 *
 * @author kevin
 */
public class ImmediateModeOGLRenderer implements SGL {
    private boolean texEnabled = true;
    private boolean drawing;
    private float[] currentTex = new float[2];
    private int currentMode;

    /**
     * The width of the display
     * ...
     */
    private int width;
    /**
     * The height of the display
     */
    private int height;
    /**
     * The current colour
     */
    private float[] current = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
    /**
     * The global colour scale
     */
    protected float alphaScale = 1.0F;

    /**
     * @see SGL#initDisplay(int, int)
     */
    @Override
    public void initDisplay(int width, int height) {
        this.width = width;
        this.height = height;

        RenderSystem.enableTexture();
        StateManager.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.disableDepthTest();
        StateManager.disableLighting();

        RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        RenderSystem.clearDepth(1.0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        GL11.glViewport(0, 0, width, height);
        StateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    /**
     * @see SGL#enterOrtho(int, int)
     */
    @Override
    public void enterOrtho(int xsize, int ysize) {
        StateManager.matrixMode(GL11.GL_PROJECTION);
        StateManager.loadIdentity();
        StateManager.ortho(0.0, this.width, this.height, 0.0, 1.0, -1.0);
        StateManager.matrixMode(GL11.GL_MODELVIEW);
        StateManager.translatef((float) ((this.width - xsize) / 2), (float) ((this.height - ysize) / 2), 0.0F);
    }

    /**
     * @see SGL#glBegin(int)
     */
    public void glBegin(int geomType) {
        this.currentMode = geomType;
        this.drawing = true;

        VertexFormat.DrawMode mode = switch (geomType) {
            case GL11.GL_LINES -> VertexFormat.DrawMode.LINES;
            case GL11.GL_LINE_STRIP -> VertexFormat.DrawMode.LINE_STRIP;
            case GL11.GL_TRIANGLES -> VertexFormat.DrawMode.TRIANGLES;
            case GL11.GL_TRIANGLE_STRIP -> VertexFormat.DrawMode.TRIANGLE_STRIP;
            case GL11.GL_TRIANGLE_FAN -> VertexFormat.DrawMode.TRIANGLE_FAN;
            default -> VertexFormat.DrawMode.QUADS;
        };

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(mode, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    /**
     * @see SGL#glBindTexture(int, int)
     */
    public void glBindTexture(int target, int id) {
        RenderSystem.setShaderTexture(0, id);
    }

    /**
     * @see SGL#glBlendFunc(int, int)
     */
    public void glBlendFunc(int src, int dest) {
        RenderSystem.blendFunc(src, dest);
    }

    /**
     * @see SGL#glCallList(int)
     */
    public void glCallList(int id) {
    }

    /**
     * @see SGL#glClear(int)
     */
    public void glClear(int value) {
        RenderSystem.clear(value, MinecraftClient.IS_SYSTEM_MAC);
    }

    /**
     * @see SGL#glClearColor(float, float, float, float)
     */
    public void glClearColor(float red, float green, float blue, float alpha) {
        RenderSystem.clearColor(red, green, blue, alpha);
    }

    /**
     * @see SGL#glClipPlane(int, DoubleBuffer)
     */
    public void glClipPlane(int plane, DoubleBuffer buffer) {
    }

    /**
     * @see SGL#glColor4f(float, float, float, float)
     */
    public void glColor4f(float r, float g, float b, float a) {
        a *= alphaScale;

        current[0] = r;
        current[1] = g;
        current[2] = b;
        current[3] = a;

        RenderSystem.setShaderColor(r, g, b, a);
    }

    /**
     * @see SGL#glColorMask(boolean, boolean, boolean, boolean)
     */
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        RenderSystem.colorMask(red, green, blue, alpha);
    }

    /**
     * @see SGL#glCopyTexImage2D(int, int, int, int, int, int, int, int)
     */
    public void glCopyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
    }

    /**
     * @see SGL#glDeleteTextures(IntBuffer)
     */
    public void glDeleteTextures(IntBuffer buffer) {
        RenderSystem.recordRenderCall(() -> {
            IntBuffer ids = buffer.duplicate();
            while (ids.hasRemaining()) {
                int id = ids.get();
                GL11.glDeleteTextures(id);
            }
        });
    }

    /**
     * @see SGL#glDisable(int)
     */
    public void glDisable(int item) {
        if (item == GL11.GL_TEXTURE_2D) {
            this.texEnabled = false;
        } else if (item == GL11.GL_BLEND) {
            RenderSystem.disableBlend();
        } else if (item == GL11.GL_DEPTH_TEST) {
            RenderSystem.disableDepthTest();
        } else if (item == GL11.GL_SCISSOR_TEST) {
            RenderSystem.disableScissor();
        }
    }

    /**
     * @see SGL#glEnable(int)
     */
    public void glEnable(int item) {
        if (item == GL11.GL_TEXTURE_2D) {
            this.texEnabled = true;
        } else if (item == GL11.GL_BLEND) {
            RenderSystem.enableBlend();
        } else if (item == GL11.GL_DEPTH_TEST) {
            RenderSystem.enableDepthTest();
        } else if (item == GL11.GL_SCISSOR_TEST) {
            RenderSystem.enableScissor(0, 0, 0, 0); // Need actual values, but usually set later
        }
    }

    /**
     * @see SGL#glEnd()
     */
    public void glEnd() {
        this.drawing = false;
        if (texEnabled) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
        }
        Tessellator.getInstance().draw();
    }

    /**
     * @see SGL#glEndList()
     */
    public void glEndList() {
        GL11.glEndList();
    }

    /**
     * @see SGL#glGenLists(int)
     */
    public int glGenLists(int count) {
        return GL11.glGenLists(count);
    }

    /**
     * @see SGL#glGetFloat(int, FloatBuffer)
     */
    public void glGetFloat(int id, FloatBuffer ret) {
        GL11.glGetFloat(id);
    }

    /**
     * @see SGL#glGetInteger(int, IntBuffer)
     */
    public void glGetInteger(int id, IntBuffer ret) {
        GL11.glGetInteger(id);
    }

    /**
     * @see SGL#glGetTexImage(int, int, int, int, ByteBuffer)
     */
    public void glGetTexImage(int target, int level, int format, int type, ByteBuffer pixels) {
        GL11.glGetTexImage(target, level, format, type, pixels);
    }

    /**
     * @see SGL#glLineWidth(float)
     */
    public void glLineWidth(float width) {
        RenderSystem.lineWidth(width);
    }

    /**
     * @see SGL#glLoadIdentity()
     */
    public void glLoadIdentity() {
        StateManager.loadIdentity();
    }

    /**
     * @see SGL#glNewList(int, int)
     */
    public void glNewList(int id, int option) {
        GL11.glNewList(id, option);
    }

    /**
     * @see SGL#glPointSize(float)
     */
    public void glPointSize(float size) {
        GL11.glPointSize(size);
    }

    /**
     * @see SGL#glPopMatrix()
     */
    public void glPopMatrix() {
        StateManager.popMatrix();
    }

    /**
     * @see SGL#glPushMatrix()
     */
    public void glPushMatrix() {
        StateManager.pushMatrix();
    }

    /**
     * @see SGL#glReadPixels(int, int, int, int, int, int, ByteBuffer)
     */
    public void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        RenderSystem.readPixels(x, y, width, height, format, type, pixels);
    }

    /**
     * @see SGL#glRotatef(float, float, float, float)
     */
    public void glRotatef(float angle, float x, float y, float z) {
        StateManager.rotatef(angle, x, y, z);
    }

    /**
     * @see SGL#glScalef(float, float, float)
     */
    public void glScalef(float x, float y, float z) {
        StateManager.scalef(x, y, z);
    }

    /**
     * @see SGL#glScissor(int, int, int, int)
     */
    public void glScissor(int x, int y, int width, int height) {
        RenderSystem.enableScissor(x, y, width, height);
    }

    /**
     * @see SGL#glTexCoord2f(float, float)
     */
    public void glTexCoord2f(float u, float v) {
        this.currentTex[0] = u;
        this.currentTex[1] = v;
    }

    /**
     * @see SGL#glTexEnvi(int, int, int)
     */
    public void glTexEnvi(int target, int mode, int value) {
    }

    /**
     * @see SGL#glTranslatef(float, float, float)
     */
    public void glTranslatef(float x, float y, float z) {
        StateManager.translatef(x, y, z);
    }

    /**
     * @see SGL#glVertex2f(float, float)
     */
    public void glVertex2f(float x, float y) {
        if (drawing) {
            Tessellator.getInstance().getBuffer().vertex(x, y, 0.0F).texture(currentTex[0], currentTex[1]).color(current[0], current[1], current[2], current[3]).next();
        }
    }

    /**
     * @see SGL#glVertex3f(float, float, float)
     */
    public void glVertex3f(float x, float y, float z) {
        if (drawing) {
            Tessellator.getInstance().getBuffer().vertex(x, y, z).texture(currentTex[0], currentTex[1]).color(current[0], current[1], current[2], current[3]).next();
        }
    }

    /**
     * @see SGL#flush()
     */
    public void flush() {
    }

    /**
     * @see SGL#glTexParameteri(int, int, int)
     */
    public void glTexParameteri(int target, int param, int value) {
        RenderSystem.texParameter(target, param, value);
    }

    /**
     * @see SGL#getCurrentColor()
     */
    public float[] getCurrentColor() {
        return current;
    }

    /**
     * @see SGL#glDeleteLists(int, int)
     */
    public void glDeleteLists(int list, int count) {
        GL11.glDeleteLists(list, count);
    }

    /**
     * @see SGL#glClearDepth(float)
     */
    public void glClearDepth(float value) {
        RenderSystem.clearDepth(value);
    }

    /**
     * @see SGL#glDepthFunc(int)
     */
    public void glDepthFunc(int func) {
        RenderSystem.depthFunc(func);
    }

    /**
     * @see SGL#glDepthMask(boolean)
     */
    public void glDepthMask(boolean mask) {
        RenderSystem.depthMask(mask);
    }

    /**
     * @see SGL#setGlobalAlphaScale(float)
     */
    public void setGlobalAlphaScale(float alphaScale) {
        this.alphaScale = alphaScale;
    }

    /**
     * @see SGL#glLoadMatrix(FloatBuffer)
     */
    public void glLoadMatrix(FloatBuffer buffer) {
        GL11.glLoadMatrixf(buffer);
    }

    /*
     * (non-Javadoc)
     * @see org.newdawn.slick.opengl.renderer.SGL#glGenTextures(java.nio.IntBuffer)
     */
    public void glGenTextures(IntBuffer ids) {
        GL11.glGenTextures(ids);
    }

    /*
     * (non-Javadoc)
     * @see org.newdawn.slick.opengl.renderer.SGL#glGetError()
     */
    public void glGetError() {
        GL11.glGetError();
    }

    /*
     * (non-Javadoc)
     * @see org.newdawn.slick.opengl.renderer.SGL#glTexImage2D(int, int, int, int, int, int, int, int, java.nio.ByteBuffer)
     */
    public void glTexImage2D(int target, int i, int dstPixelFormat,
                             int width, int height, int j, int srcPixelFormat,
                             int glUnsignedByte, ByteBuffer textureBuffer) {
        GL11.glTexImage2D(target, i, dstPixelFormat, width, height, j, srcPixelFormat, glUnsignedByte, textureBuffer);
    }

    public void glTexSubImage2D(int glTexture2d, int i, int pageX, int pageY,
                                int width, int height, int glBgra, int glUnsignedByte,
                                ByteBuffer scratchByteBuffer) {
        GL11.glTexSubImage2D(glTexture2d, i, pageX, pageY, width, height, glBgra, glUnsignedByte, scratchByteBuffer);
    }

    @Override
    public boolean canTextureMirrorClamp() {
        return false;
    }

    @Override
    public boolean canSecondaryColor() {
        return false;
    }

    @Override
    public void glSecondaryColor3ubEXT(byte b, byte c, byte d) {
        EXTSecondaryColor.glSecondaryColor3ubEXT(b, c, d);
    }
}