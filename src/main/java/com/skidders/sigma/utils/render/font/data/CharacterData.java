package com.skidders.sigma.utils.render.font.data;

import org.lwjgl.opengl.GL11;

public record CharacterData(char character, float width, float height, int textureId) {
    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
    }
}
