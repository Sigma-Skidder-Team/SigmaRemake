package lexi.fontrenderer.data;

import org.lwjgl.opengl.GL11;

public record CharacterData(char character, float width, float height, int textureId) {
    public void bind() {
        GL11.glBindTexture(3553, textureId);
    }
}
