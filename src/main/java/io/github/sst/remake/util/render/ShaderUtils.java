package io.github.sst.remake.util.render;

import io.github.sst.remake.Client;
import io.github.sst.remake.util.IMinecraft;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ShaderUtils implements IMinecraft {

    private static final Identifier BLUR_SHADER = new Identifier("shaders/post/blur.json");

    private static boolean canBlur() {
        return Client.INSTANCE.configManager.guiBlur;
    }

    private static PostEffectProcessor getPostProcessor() {
        return client.gameRenderer.getPostProcessor();
    }

    public static void applyBlurShader() {
        if (client.getCameraEntity() instanceof PlayerEntity && canBlur()) {
            if (getPostProcessor() != null) {
                getPostProcessor().close();
            }

            client.gameRenderer.loadPostProcessor(BLUR_SHADER);
        }

        setShaderRadius(20);
    }

    public static void resetShader() {
        if (client.gameRenderer.superSecretSettingIndex == GameRenderer.SUPER_SECRET_SETTING_COUNT) {
            client.gameRenderer.postProcessor = null;
        } else {
            client.gameRenderer.loadPostProcessor(GameRenderer.SUPER_SECRET_SETTING_PROGRAMS[client.gameRenderer.superSecretSettingIndex]);
        }
    }

    public static void setShaderRadius(int radius) {
        if (getPostProcessor() != null) {
            getPostProcessor().passes.get(0).getProgram().getUniformByName("Radius").set((float) radius);
            getPostProcessor().passes.get(1).getProgram().getUniformByName("Radius").set((float) radius);
        }
    }

    public static void setShaderRadiusRounded(float radius) {
        setShaderRadius(Math.round(radius * 20.0F));
    }

}
