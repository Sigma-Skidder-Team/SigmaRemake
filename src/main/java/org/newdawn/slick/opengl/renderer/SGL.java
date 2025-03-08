package org.newdawn.slick.opengl.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * The description of the OpenGL functions used Slick. Any other rendering method will
 * need to emulate these.
 *
 * @author kevin
 */
public interface SGL {

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_TEXTURE_2D = GL11.GL_TEXTURE_2D;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_RGBA = GL11.GL_RGBA;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_RGB = GL11.GL_RGB;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_UNSIGNED_BYTE = GL11.GL_UNSIGNED_BYTE;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_LINEAR = GL11.GL_LINEAR;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_NEAREST = GL11.GL_NEAREST;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_TEXTURE_MIN_FILTER = GL11.GL_TEXTURE_MIN_FILTER;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_TEXTURE_MAG_FILTER = GL11.GL_TEXTURE_MAG_FILTER;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_POINT_SMOOTH = GL11.GL_POINT_SMOOTH;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_POLYGON_SMOOTH = GL11.GL_POLYGON_SMOOTH;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_LINE_SMOOTH = GL11.GL_LINE_SMOOTH;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_SCISSOR_TEST = GL11.GL_SCISSOR_TEST;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_MODULATE = GL11.GL_MODULATE;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_TEXTURE_ENV = GL11.GL_TEXTURE_ENV;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_TEXTURE_ENV_MODE = GL11.GL_TEXTURE_ENV_MODE;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_QUADS = GL11.GL_QUADS;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_POINTS = GL11.GL_POINTS;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_LINES = GL11.GL_LINES;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_LINE_STRIP = GL11.GL_LINE_STRIP;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_TRIANGLES = GL11.GL_TRIANGLES;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_TRIANGLE_FAN = GL11.GL_TRIANGLE_FAN;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_SRC_ALPHA = GL11.GL_SRC_ALPHA;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_ONE = GL11.GL_ONE;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_ONE_MINUS_DST_ALPHA = GL11.GL_ONE_MINUS_DST_ALPHA;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_DST_ALPHA = GL11.GL_DST_ALPHA;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_ONE_MINUS_SRC_ALPHA = GL11.GL_ONE_MINUS_SRC_ALPHA;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_COMPILE = GL11.GL_COMPILE;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_MAX_TEXTURE_SIZE = GL11.GL_MAX_TEXTURE_SIZE;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_COLOR_BUFFER_BIT = GL11.GL_COLOR_BUFFER_BIT;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_DEPTH_BUFFER_BIT = GL11.GL_DEPTH_BUFFER_BIT;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_BLEND = GL11.GL_BLEND;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_COLOR_CLEAR_VALUE = GL11.GL_COLOR_CLEAR_VALUE;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_LINE_WIDTH = GL11.GL_LINE_WIDTH;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_CLIP_PLANE0 = GL11.GL_CLIP_PLANE0;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_CLIP_PLANE1 = GL11.GL_CLIP_PLANE1;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_CLIP_PLANE2 = GL11.GL_CLIP_PLANE2;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_CLIP_PLANE3 = GL11.GL_CLIP_PLANE3;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_COMPILE_AND_EXECUTE = GL11.GL_COMPILE_AND_EXECUTE;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_RGBA8 = GL11.GL_RGBA;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_RGBA16 = GL11.GL_RGBA16;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_BGRA = GL12.GL_BGRA;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_MIRROR_CLAMP_TO_EDGE_EXT = 34627;//EXTTextureMirrorClamp.GL_MIRROR_CLAMP_TO_EDGE_EXT;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_TEXTURE_WRAP_S = GL11.GL_TEXTURE_WRAP_S;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_TEXTURE_WRAP_T = GL11.GL_TEXTURE_WRAP_T;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_CLAMP = GL11.GL_CLAMP;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_COLOR_SUM_EXT = 33880;//EXTSecondaryColor.GL_COLOR_SUM_EXT;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_ALWAYS = GL11.GL_ALWAYS;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_DEPTH_TEST = GL11.GL_DEPTH_TEST;

   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_NOTEQUAL = GL11.GL_NOTEQUAL;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_EQUAL = GL11.GL_EQUAL;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_SRC_COLOR = GL11.GL_SRC_COLOR;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_ONE_MINUS_SRC_COLOR = GL11.GL_ONE_MINUS_SRC_COLOR;
   /** OpenGL Enum - @url http://www.opengl.org/documentation */
   int GL_MODELVIEW_MATRIX = GL11.GL_MODELVIEW_MATRIX;

   /**
    * Flush the current state of the renderer down to GL
    */
   void flush();

   /**
    * Initialise the display
    *
    * @param width The width of the display
    * @param height The height of the display
    */
   void initDisplay(int width, int height);

   /**
    * Enter orthographic mode
    *
    * @param xsize The size of the ortho display
    * @param ysize The size of the ortho display
    */
   void enterOrtho(int xsize, int ysize);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param red
    * @param green
    * @param blue
    * @param alpha
    */
   void glClearColor(float red, float green, float blue, float alpha);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param plane
    * @param buffer
    */
   void glClipPlane(int plane, DoubleBuffer buffer);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param x
    * @param y
    * @param width
    * @param height
    */
   void glScissor(int x, int y, int width, int height);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param width
    */
   void glLineWidth(float width);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param value
    */
   void glClear(int value);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param red
    * @param green
    * @param blue
    * @param alpha
    */
   void glColorMask(boolean red, boolean green, boolean blue, boolean alpha);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    */
   void glLoadIdentity();

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param id
    * @param ret
    */
   void glGetInteger(int id, IntBuffer ret);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param id
    * @param ret
    */
   void glGetFloat(int id, FloatBuffer ret);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param item
    */
   void glEnable(int item);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param item
    */
   void glDisable(int item);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param target
    * @param id
    */
   void glBindTexture(int target, int id);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param target
    * @param level
    * @param format
    * @param type
    * @param pixels
    */
   void glGetTexImage(int target, int level, int format, int type, ByteBuffer pixels);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param buffer
    */
   void glDeleteTextures(IntBuffer buffer);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param r
    * @param g
    * @param b
    * @param a
    */
   void glColor4f(float r, float g, float b, float a);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param u
    * @param v
    */
   void glTexCoord2f(float u, float v);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param x
    * @param y
    * @param z
    */
   void glVertex3f(float x, float y, float z);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param x
    * @param y
    */
   void glVertex2f(float x, float y);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param angle
    * @param x
    * @param y
    * @param z
    */
   void glRotatef(float angle, float x, float y, float z);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param x
    * @param y
    * @param z
    */
   void glTranslatef(float x, float y, float z);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param geomType
    */
   void glBegin(int geomType);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    */
   void glEnd();

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param target
    * @param mode
    * @param value
    */
   void glTexEnvi(int target, int mode, int value);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param size
    */
   void glPointSize(float size);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param x
    * @param y
    * @param z
    */
   void glScalef(float x, float y, float z);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    */
   void glPushMatrix();

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    */
   void glPopMatrix();

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param src
    * @param dest
    */
   void glBlendFunc(int src, int dest);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param count
    * @return The index of the lists
    */
   int glGenLists(int count);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param id
    * @param option
    */
   void glNewList(int id, int option);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    */
   void glEndList();

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param id
    */
   void glCallList(int id);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param target
    * @param level
    * @param internalFormat
    * @param x
    * @param y
    * @param width
    * @param height
    * @param border
    */
   void glCopyTexImage2D(int target, int level, int internalFormat,
						 int x, int y, int width, int height, int border);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param x
    * @param y
    * @param width
    * @param height
    * @param format
    * @param type
    * @param pixels
    */
   void glReadPixels(int x, int y, int width, int height, int format, int type,
					 ByteBuffer pixels);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param target
    * @param param
    * @param value
    */
   void glTexParameteri(int target, int param, int value);

   /**
    * Get the current colour being rendered
    *
    * @return The current colour being rendered
    */
   float[] getCurrentColor();

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param list
    * @param count
    */
   void glDeleteLists(int list, int count);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param mask
    */
   void glDepthMask(boolean mask);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param value
    */
   void glClearDepth(float value);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param func
    */
   void glDepthFunc(int func);

   /**
    * Set the scaling we'll apply to any colour binds in this renderer
    *
    * @param alphaScale The scale to apply to any colour binds
    */
   void setGlobalAlphaScale(float alphaScale);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param buffer
    */
   void glLoadMatrix(FloatBuffer buffer);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    *
    * @param ids
    */
   void glGenTextures(IntBuffer ids);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    */
   void glGetError();

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    */
   void glTexImage2D(int target, int i, int dstPixelFormat,
					 int get2Fold, int get2Fold2, int j, int srcPixelFormat,
					 int glUnsignedByte, ByteBuffer textureBuffer);

   /**
    * OpenGL Method - @url http://www.opengl.org/documentation/
    */
   void glTexSubImage2D(int glTexture2d, int i, int pageX, int pageY,
						int width, int height, int glBgra, int glUnsignedByte,
						ByteBuffer scratchByteBuffer);

   /**
    * Check if the mirror clamp extension is available
    *
    * @return True if the mirro clamp extension is available
    */
   boolean canTextureMirrorClamp();

   boolean canSecondaryColor();

   void glSecondaryColor3ubEXT(byte b, byte c, byte d);
}