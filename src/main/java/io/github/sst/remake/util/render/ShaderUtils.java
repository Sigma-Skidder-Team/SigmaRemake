package io.github.sst.remake.util.render;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.IMinecraft;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ShaderUtils implements IMinecraft {

    private static final Identifier BLUR_SHADER = new Identifier("shaders/post/blur.json");

    private static boolean canBlur() {
        return Client.INSTANCE.configManager.guiBlur;
    }

    private static ShaderEffect getShader() {
        return client.gameRenderer.getShader();
    }

    public static void applyBlurShader() {
        if (client.getCameraEntity() instanceof PlayerEntity && canBlur()) {
            if (getShader() != null) {
                getShader().close();
            }

            client.gameRenderer.loadShader(BLUR_SHADER);
        }

        setShaderRadius(20);
    }

    public static void resetShader() {
        if (client.gameRenderer.forcedShaderIndex == GameRenderer.SHADER_COUNT) {
            client.gameRenderer.shader = null;
        } else {
            client.gameRenderer.loadShader(GameRenderer.SHADERS_LOCATIONS[client.gameRenderer.forcedShaderIndex]);
        }
    }

    public static void setShaderRadius(int radius) {
        if (getShader() != null) {
            getShader().passes.get(0).getProgram().getUniformByName("Radius").set((float) radius);
            getShader().passes.get(1).getProgram().getUniformByName("Radius").set((float) radius);
        }
    }

    public static void setShaderRadiusRounded(float radius) {
        setShaderRadius(Math.round(radius * 20.0F));
    }

}
