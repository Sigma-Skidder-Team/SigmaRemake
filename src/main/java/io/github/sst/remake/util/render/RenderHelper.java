package io.github.sst.remake.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.sst.remake.util.porting.StateManager;
import net.minecraft.util.Util;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class RenderHelper {
    private static final Vec3f DEFAULT_LIGHTING = Util.make(new Vec3f(0.2F, 1.0F, -0.7F), Vec3f::normalize);
    private static final Vec3f DIFFUSE_LIGHTING = Util.make(new Vec3f(-0.2F, 1.0F, 0.7F), Vec3f::normalize);
    private static final Vec3f GUI_FLAT_DIFFUSE_LIGHTING = Util.make(new Vec3f(0.2F, 1.0F, -0.7F), Vec3f::normalize);
    private static final Vec3f GUI_3D_DIFFUSE_LIGHTING = Util.make(new Vec3f(-0.2F, -1.0F, 0.7F), Vec3f::normalize);

    public static void enableStandardItemLighting() {
        StateManager.enableLighting();
        StateManager.enableColorMaterial();
        StateManager.colorMaterial(1032, 5634);
    }

    /**
     * Disables the OpenGL lighting properties enabled by enableStandardItemLighting
     */
    public static void disableStandardItemLighting() {
        StateManager.disableLighting();
        StateManager.disableColorMaterial();
    }
    public static void setupDiffuseGuiLighting(Matrix4f matrix) {
        RenderSystem.setupLevelDiffuseLighting(GUI_FLAT_DIFFUSE_LIGHTING, GUI_3D_DIFFUSE_LIGHTING, matrix);
    }

    public static void setupLevelDiffuseLighting(Matrix4f matrixIn) {
        RenderSystem.setupLevelDiffuseLighting(DEFAULT_LIGHTING, DIFFUSE_LIGHTING, matrixIn);
    }

    public static void setupGuiFlatDiffuseLighting() {
        RenderSystem.setupGuiFlatDiffuseLighting(DEFAULT_LIGHTING, DIFFUSE_LIGHTING);
    }

    public static void setupGui3DDiffuseLighting() {
        RenderSystem.setupGui3DDiffuseLighting(DEFAULT_LIGHTING, DIFFUSE_LIGHTING);
    }
}