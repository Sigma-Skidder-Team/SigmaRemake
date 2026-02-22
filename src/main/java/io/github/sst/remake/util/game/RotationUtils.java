package io.github.sst.remake.util.game;

import io.github.sst.remake.data.rotation.Rotation;
import io.github.sst.remake.util.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class RotationUtils implements IMinecraft {

    public static Rotation getBasicRotations(Entity target) {
        if (target == null || client.player == null) return null;

        double dX = target.getX() - client.player.getX();
        double dY = target.getY() - client.player.getY();
        double dZ = target.getZ() - client.player.getZ();

        double dist = Math.hypot(dX, dZ);

        float yaw = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dY, dist));

        return new Rotation(yaw, pitch);
    }

    public static double gcd() {
        double f = client.options.mouseSensitivity * (double)0.6f + (double)0.2f;
        double g = f * f * f * 8.0;

        return g;
    }

    public static float normalizeYaw(float yaw) {
        return MathHelper.wrapDegrees(yaw);
    }

    public static float normalizePitch(float pitch) {
        return MathHelper.clamp(pitch, -90f, 90f);
    }

    public static float getYawDifference(float yaw1, float yaw2) {
        float difference = Math.abs(yaw1 - yaw2) % 360.0F;

        if (difference > 180.0F) {
            difference = 360.0F - difference;
        }

        return difference;
    }

}
