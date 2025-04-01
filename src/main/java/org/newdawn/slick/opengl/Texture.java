package org.newdawn.slick.opengl;

/**
 * The description of a texture loaded by the TextureLoader utility
 *
 * @author kevin
 */
public interface Texture {

    /**
     * Check if the texture has alpha
     *
     * @return True if the texture has alpha
     */
    boolean hasAlpha();

    /**
     * Get the reference from which this texture was loaded
     *
     * @return The reference from which this texture was loaded
     */
    String getTextureRef();

    /**
     * Bind the  GL context to a texture
     */
    void bind();

    /**
     * Get the height of the original image
     *
     * @return The height of the original image
     */
    int getImageHeight();

    /**
     * Get the width of the original image
     *
     * @return The width of the original image
     */
    int getImageWidth();

    /**
     * Get the height of the physical texture
     *
     * @return The height of physical texture
     */
    float getHeight();

    /**
     * Get the width of the physical texture
     *
     * @return The width of physical texture
     */
    float getWidth();

    /**
     * Get the height of the actual texture
     *
     * @return The height of the actual texture
     */
    int getTextureHeight();

    /**
     * Get the width of the actual texture
     *
     * @return The width of the actual texture
     */
    int getTextureWidth();

    /**
     * Destroy the texture reference
     */
    void release();

    /**
     * Get the OpenGL texture ID for this texture
     *
     * @return The OpenGL texture ID
     */
    int getTextureID();

    /**
     * Get the pixel data from the card for this texture
     *
     * @return The texture data from the card for this texture
     */
    byte[] getTextureData();

    /**
     * Apply a given texture filter to the texture
     *
     * @param textureFilter The texture filter to apply (GL_LINEAR, GL_NEAREST, etc..)
     */
    void setTextureFilter(int textureFilter);
}